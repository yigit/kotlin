/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

#include "KAssert.h"

#include <array>
#include <cstdarg>

#include "cpp_support/Span.hpp"
#include "Format.h"
#include "StackTrace.hpp"

using namespace kotlin;

namespace {

// TODO: Enable stacktraces for asserts when stacktrace printing is more mature.
inline constexpr bool kEnableStacktraces = false;

void PrintAssert(const char* location, const char* format, std::va_list args) noexcept {
    std::array<char, 1024> bufferStorage;
    std_support::span<char> buffer(bufferStorage);

    // Write the title with a source location.
    if (location != nullptr) {
        buffer = FormatToSpan(buffer, "%s: runtime assert: ", location);
    } else {
        buffer = FormatToSpan(buffer, "runtime assert: ");
    }

    // Write the message.
    buffer = VFormatToSpan(buffer, format, args);

    konan::consoleErrorUtf8(bufferStorage.data(), bufferStorage.size() - buffer.size());
    konan::consoleErrorf("\n");
    if constexpr (kEnableStacktraces) {
        kotlin::PrintStackTraceStderr();
    }
}

} // namespace

void internal::RuntimeAssertFailedLog(const char* location, const char* format, ...) {
    std::va_list args;
    va_start(args, format);
    PrintAssert(location, format, args);
    va_end(args);
}

RUNTIME_NORETURN void internal::RuntimeAssertFailedPanic(const char* location, const char* format, ...) {
    std::va_list args;
    va_start(args, format);
    PrintAssert(location, format, args);
    va_end(args);
    konan::abort();
}
