package moe.ruruke.skyblock.features.healingcircle

import java.awt.geom.Point2D

class HealingCircleParticle(x: Double, z: Double) {
    private val point = Point2D.Double(x, z)
    private val creation = System.currentTimeMillis()

    fun getPoint(): Point2D.Double {
        return point
    }

    fun getCreation(): Long {
        return creation
    }
}
