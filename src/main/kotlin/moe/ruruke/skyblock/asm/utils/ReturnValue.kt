package moe.ruruke.skyblock.asm.utils

class ReturnValue<R> {
    private var cancelled = false

    private var returnValue: R? = null


    fun cancel() {
        cancel(null)
    }

    fun cancel(returnValue: R?) {
        cancelled = true
        this.returnValue = returnValue
    }

    fun isCancelled(): Boolean {
        return cancelled
    }

    fun getReturnValue(): R {
        return returnValue!!
    }
}
