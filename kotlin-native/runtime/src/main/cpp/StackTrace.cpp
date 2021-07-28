/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

#include "StackTrace.hpp"

#include <array>

#if KONAN_NO_BACKTRACE
// Nothing to include
#elif USE_GCC_UNWIND
// GCC unwinder for backtrace.
#include <unwind.h>
#else
// Glibc backtrace() function.
#include <execinfo.h>
#endif

#include "Common.h"
#include "ExecFormat.h"
#include "Format.h"
#include "Porting.h"
#include "SourceInfo.h"
#include "Types.h"

#include "utf8.h"

using namespace kotlin;

namespace {

#if USE_GCC_UNWIND
struct Backtrace {
    Backtrace(int count, int skip) : skipCount(skip) {
        uint32_t size = count - skipCount;
        if (size < 0) {
            size = 0;
        }
        array.reserve(size);
    }

    void setNextElement(_Unwind_Ptr element) { array.push_back(reinterpret_cast<void*>(element)); }

    int skipCount;
    KStdVector<void*> array;
};

_Unwind_Reason_Code depthCountCallback(struct _Unwind_Context* context, void* arg) {
    int* result = reinterpret_cast<int*>(arg);
    (*result)++;
    return _URC_NO_REASON;
}

_Unwind_Reason_Code unwindCallback(struct _Unwind_Context* context, void* arg) {
    Backtrace* backtrace = reinterpret_cast<Backtrace*>(arg);
    if (backtrace->skipCount > 0) {
        backtrace->skipCount--;
        return _URC_NO_REASON;
    }

#if (__MINGW32__ || __MINGW64__)
    _Unwind_Ptr address = _Unwind_GetRegionStart(context);
#else
    _Unwind_Ptr address = _Unwind_GetIP(context);
#endif
    backtrace->setNextElement(address);

    return _URC_NO_REASON;
}
#endif

THREAD_LOCAL_VARIABLE bool disallowSourceInfo = false;

#if !KONAN_NO_BACKTRACE && !USE_GCC_UNWIND
SourceInfo getSourceInfo(void* symbol) {
    return disallowSourceInfo ? SourceInfo{.fileName = nullptr, .lineNumber = -1, .column = -1} : Kotlin_getSourceInfo(symbol);
}
#endif

} // namespace

// TODO: this implementation is just a hack, e.g. the result is inexact;
// however it is better to have an inexact stacktrace than not to have any.
NO_INLINE KStdVector<void*> kotlin::GetCurrentStackTrace(int extraSkipFrames) noexcept {
#if KONAN_NO_BACKTRACE
    return {};
#else
    // Skips this function frame + anything asked by the caller.
    const int kSkipFrames = 1 + extraSkipFrames;
#if USE_GCC_UNWIND
    int depth = 0;
    _Unwind_Backtrace(depthCountCallback, static_cast<void*>(&depth));
    Backtrace result(depth, kSkipFrames);
    if (result.array.capacity() > 0) {
        _Unwind_Backtrace(unwindCallback, static_cast<void*>(&result));
    }
    return std::move(result.array);
#else
    const int maxSize = 32;
    void* buffer[maxSize];

    int size = backtrace(buffer, maxSize);
    if (size < kSkipFrames) return {};

    KStdVector<void*> result;
    result.reserve(size - kSkipFrames);
    for (int index = kSkipFrames; index < size; ++index) {
        result.push_back(buffer[index]);
    }
    return result;
#endif
#endif // !KONAN_NO_BACKTRACE
}

KStdVector<KStdString> kotlin::GetStackTraceStrings(void* const* stackTrace, size_t stackTraceSize) noexcept {
#if KONAN_NO_BACKTRACE
    KStdVector<KStdString> strings;
    strings.push_back("<UNIMPLEMENTED>");
    return strings;
#else
    KStdVector<KStdString> strings;
    strings.reserve(stackTraceSize);
#if USE_GCC_UNWIND
    for (size_t index = 0; index < stackTraceSize; ++index) {
        KNativePtr address = stackTrace[index];
        char symbol[512];
        if (!AddressToSymbol(address, symbol, sizeof(symbol))) {
            // Make empty string:
            symbol[0] = '\0';
        }
        std::array<char, 512> line;
        FormatToSpan(line, "%s (%p)", symbol, (void*)(intptr_t)address);
        strings.push_back(line.data());
    }
#else
    if (stackTraceSize > 0) {
        char** symbols = backtrace_symbols(stackTrace, static_cast<int>(stackTraceSize));
        RuntimeCheck(symbols != nullptr, "Not enough memory to retrieve the stacktrace");

        for (size_t index = 0; index < stackTraceSize; ++index) {
            KNativePtr address = stackTrace[index];
            auto sourceInfo = getSourceInfo(address);
            const char* symbol = symbols[index];
            const char* result;
            std::array<char, 1024> line;
            if (sourceInfo.fileName != nullptr) {
                if (sourceInfo.lineNumber != -1) {
                    FormatToSpan(line, "%s (%s:%d:%d)", symbol, sourceInfo.fileName, sourceInfo.lineNumber, sourceInfo.column);
                } else {
                    FormatToSpan(line, "%s (%s:<unknown>)", symbol, sourceInfo.fileName);
                }
                result = line.data();
            } else {
                result = symbol;
            }
            strings.push_back(result);
        }
        // Not konan::free. Used to free memory allocated in backtrace_symbols where malloc is used.
        free(symbols);
    }
#endif
    return strings;
#endif // !KONAN_NO_BACKTRACE
}

void kotlin::DisallowSourceInfo() {
    disallowSourceInfo = true;
}

NO_INLINE void kotlin::PrintStackTraceStderr() {
    // NOTE: This might be called from both runnable and native states (including in uninitialized runtime)
    // TODO: This is intended for runtime use. Try to avoid memory allocations and signal unsafe functions.

    // TODO: This might have to go into `GetCurrentStackTrace`, but this changes the generated stacktrace for
    //       `Throwable`.
#if KONAN_WINDOWS
    // Skip this function and `_Unwind_Backtrace`.
    constexpr int kSkipFrames = 2;
#else
    // Skip this function.
    constexpr int kSkipFrames = 1;
#endif
    auto stackTrace = GetCurrentStackTrace(kSkipFrames);
    auto stackTraceStrings = GetStackTraceStrings(stackTrace.data(), stackTrace.size());
    for (auto& frame : stackTraceStrings) {
        konan::consoleErrorUtf8(frame.c_str(), frame.size());
        konan::consoleErrorf("\n");
    }
}
