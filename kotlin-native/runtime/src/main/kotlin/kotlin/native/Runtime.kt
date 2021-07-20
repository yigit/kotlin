/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package kotlin.native

import kotlin.native.concurrent.InvalidMutabilityException
import kotlin.native.internal.ExportForCppRuntime
import kotlin.native.internal.GCUnsafeCall
import kotlin.native.internal.UnhandledExceptionHookHolder
import kotlin.native.internal.runUnhandledExceptionHook
import kotlin.native.internal.ReportUnhandledException

/**
 * Initializes Kotlin runtime for the current thread, if not inited already.
 */
@GCUnsafeCall("Kotlin_initRuntimeIfNeededFromKotlin")
external public fun initRuntimeIfNeeded(): Unit

/**
 * Deinitializes Kotlin runtime for the current thread, if was inited.
 * Cannot be called from Kotlin frames holding references, thus deprecated.
 */
@GCUnsafeCall("Kotlin_deinitRuntimeIfNeeded")
@Deprecated("Deinit runtime can not be called from Kotlin", level = DeprecationLevel.ERROR)
external public fun deinitRuntimeIfNeeded(): Unit

/**
 * Exception thrown when top level variable is accessed from incorrect execution context.
 */
public class IncorrectDereferenceException : RuntimeException {
    constructor() : super()

    constructor(message: String) : super(message)
}

/**
 * Typealias describing custom exception reporting hook.
 */
public typealias ReportUnhandledExceptionHook = Function1<Throwable, Unit>

/**
 * Install custom unhandled exception hook. Returns old hook, or null if it was not specified.
 * Hook is invoked whenever there's uncaught exception reaching boundaries of the Kotlin world,
 * i.e. top level main(), or when Objective-C to Kotlin call not marked with @Throws throws an exception.
 * Hook must be a frozen lambda, so that it could be called from any thread/worker.
 */
public fun setUnhandledExceptionHook(hook: ReportUnhandledExceptionHook): ReportUnhandledExceptionHook? {
    try {
        return UnhandledExceptionHookHolder.hook.swap(hook)
    } catch (e: InvalidMutabilityException) {
        throw InvalidMutabilityException("Unhandled exception hook must be frozen")
    }
}

/**
 * Retrieve custom unhandled exception hook set by [setUnhandledExceptionHook].
 */
public fun getUnhandledExceptionHook(): ReportUnhandledExceptionHook? {
    return UnhandledExceptionHookHolder.hook.value
}

/**
 * Perform the default processing of unhandled exception.
 */
@ExportForCppRuntime("Kotlin_processUnhandledException")
public fun processUnhandledException(throwable: Throwable) {
    try {
        runUnhandledExceptionHook(throwable)
    } catch (e: Throwable) {
        ReportUnhandledException(e)
        terminateWithUnhandledException(e)
    }
}

/*
 * Terminate the program with unhandled exception. Does not run unhandled exception hook from
 * [setUnhandledExceptionHook].
 */
@GCUnsafeCall("Kotlin_terminateWithUnhandledException")
public external fun terminateWithUnhandledException(throwable: Throwable): Nothing

/**
 * Compute stable wrt potential object relocations by the memory manager identity hash code.
 * @return 0 for `null` object, identity hash code otherwise.
 */
@GCUnsafeCall("Kotlin_Any_hashCode")
public external fun Any?.identityHashCode(): Int
