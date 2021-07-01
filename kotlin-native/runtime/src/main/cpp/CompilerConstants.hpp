/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

#ifndef RUNTIME_COMPILER_CONSTANTS_H
#define RUNTIME_COMPILER_CONSTANTS_H

#include <cstdint>

#include "Common.h"

// Prefer to use getter functions below. These constants are exposed to simplify the job of the inliner.

// These are defined by setRuntimeConstGlobals in IrToBitcode.kt
extern "C" const int32_t KonanNeedDebugInfo;

namespace kotlin {
namespace compiler {

// Must match DestroyRuntimeMode in DestroyRuntimeMode.kt
enum class DestroyRuntimeMode : int32_t {
    kLegacy = 0,
    kOnShutdown = 1,
};

DestroyRuntimeMode destroyRuntimeMode() noexcept;

bool gcAggressive() noexcept;

ALWAYS_INLINE inline bool shouldContainDebugInfo() noexcept {
    return KonanNeedDebugInfo != 0;
}

} // namespace compiler
} // namespace kotlin

#endif // RUNTIME_COMPILER_CONSTANTS_H
