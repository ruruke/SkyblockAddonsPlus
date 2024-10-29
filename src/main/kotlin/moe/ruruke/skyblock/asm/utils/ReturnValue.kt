package moe.ruruke.skyblock.asm.utils

class ReturnValue<R> {
    var isCancelled: Boolean = false
        private set

    var returnValue: R? = null
        private set

    @JvmOverloads
    fun cancel(returnValue: R? = null) {
        isCancelled = true
        this.returnValue = returnValue
    }
}
