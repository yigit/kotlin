package runtime.workers.worker_exceptions

import kotlin.test.*

import kotlin.native.concurrent.*

@Test
fun testExecuteAfterStartQuiet() {
    val worker = Worker.start(errorReporting = false)
    worker.executeAfter(0L, {
        throw Error("some error")
    }.freeze())
    worker.requestTermination().result
}
