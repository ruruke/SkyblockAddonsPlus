package moe.ruruke.skyblock.newgui.sizes

import moe.ruruke.skyblock.SkyblockAddonsPlus


open class SizeBase {
    private val lastPositionsUpdate = 0
    private val lastSizesUpdate = 0
    private var forceUpdate = false
    var x: Float = 0f
    var y: Float = 0f
    var w: Float = 0f
    var h: Float = 0f
//
//    fun getX(): Float {
//        if (positionsNeedUpdate()) {
//            updatePositions()
//        }
//        return x
//    }
//
//    fun getY(): Float {
//        if (positionsNeedUpdate()) {
//            updatePositions()
//        }
//        return y
//    }
//
//    fun getW(): Float {
//        if (sizesNeedUpdate()) {
//            updateSizes()
//        }
//        return w
//    }
//
//    fun getH(): Float {
//        if (sizesNeedUpdate()) {
//            updateSizes()
//        }
//        return h
//    }

    fun setForceUpdate() {
        forceUpdate = true
    }

    private fun positionsNeedUpdate(): Boolean {
        if (forceUpdate) {
            return true
        }

        return SkyblockAddonsPlus.newScheduler!!.totalTicks !== lastPositionsUpdate.toLong()
    }

    private fun sizesNeedUpdate(): Boolean {
        if (forceUpdate) {
            return true
        }

        return SkyblockAddonsPlus.newScheduler!!.totalTicks !== lastSizesUpdate.toLong()
    }

    open fun updatePositions() {
    }

    open fun updateSizes() {
    }
}
