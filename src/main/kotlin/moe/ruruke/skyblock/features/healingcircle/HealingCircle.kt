package moe.ruruke.skyblock.features.healingcircle

import java.awt.geom.Point2D
import kotlin.math.cos
import kotlin.math.sin


class HealingCircle(healingCircleParticle: HealingCircleParticle) {
    private val healingCircleParticles: MutableList<HealingCircleParticle> = ArrayList()
    private val creation = System.currentTimeMillis()
    private var cachedCenterPoint: Point2D.Double? = null

    private var totalX = 0.0
    private var totalZ = 0.0
    private var totalParticles = 0
    fun getCreation(): Long {
        return creation;
    }
    init {
        addPoint(healingCircleParticle)
    }

    fun addPoint(healingCircleParticle: HealingCircleParticle) {
        totalParticles++
        totalX += healingCircleParticle.getPoint().getX()
        totalZ += healingCircleParticle.getPoint().getY()
        healingCircleParticles.add(healingCircleParticle)
    }

    val averageX: Double
        get() = totalX / totalParticles.toDouble()

    val averageZ: Double
        get() = totalZ / totalParticles.toDouble()

    val particlesPerSecond: Double
        get() {
            var particlesPerSecond = 0
            val now = System.currentTimeMillis()
            for (healingCircleParticle in healingCircleParticles) {
                if (now - healingCircleParticle.getCreation() < 1000) {
                    particlesPerSecond++
                }
            }
            return particlesPerSecond.toDouble()
        }

    val circleCenter: Point2D.Double
        get() {
            if (cachedCenterPoint != null) {
                return cachedCenterPoint!!
            }

            if (healingCircleParticles.size < 3) {
                return Point2D.Double(Double.NaN, Double.NaN)
            }

            // The middle point, which is the first point for consistency. The circle will not appear
            // until two other points exist, one that is left of this one, and one right.
            val middlePoint: Point2D.Double = healingCircleParticles[0].getPoint()

            // The first point, which can be anywhere on the circle as long as its a decent
            // distance away from the middle.
            var firstPoint: Point2D.Double? = null
            for (healingCircleParticle in healingCircleParticles) {
                val point: Point2D.Double = healingCircleParticle.getPoint()
                if (point !== middlePoint && point.distance(middlePoint) > 2) {
                    firstPoint = point
                    break
                }
            }
            if (firstPoint == null) {
                return Point2D.Double(Double.NaN, Double.NaN)
            }

            // The second point, which can be anywhere on the circle as long as its a decent
            // distance away from the middle + its on the opposite side of the first point.
            var secondPoint: Point2D.Double? = null
            for (healingCircleParticle in healingCircleParticles) {
                val point: Point2D.Double = healingCircleParticle.getPoint()
                if (point !== middlePoint && point !== firstPoint) {
                    val distanceToMiddle = point.distance(middlePoint)
                    // Make sure that the point is closer to the middle point than the first
                    // point, or else both points will be on the same side.
                    if (distanceToMiddle > 2 && point.distance(firstPoint) > distanceToMiddle) {
                        secondPoint = point
                        break
                    }
                }
            }
            if (secondPoint == null) {
                return Point2D.Double(Double.NaN, Double.NaN)
            }

            val firstChordMidpoint =
                Point2D.Double((middlePoint.x + firstPoint.x) / 2.0, (middlePoint.y + firstPoint.y) / 2.0)
            val secondChordMidpoint = Point2D.Double(
                (middlePoint.x + secondPoint.x) / 2.0,
                (middlePoint.y + secondPoint.y) / 2.0
            )

            val firstChordFirst =
                rotatePoint(middlePoint, firstChordMidpoint, 90.0)
            val firstChordSecond =
                rotatePoint(firstPoint, firstChordMidpoint, 90.0)

            val secondChordFirst =
                rotatePoint(middlePoint, secondChordMidpoint, 90.0)
            val secondChordSecond =
                rotatePoint(secondPoint, secondChordMidpoint, 90.0)

            val center = lineLineIntersection(
                firstChordFirst,
                firstChordSecond,
                secondChordFirst,
                secondChordSecond
            )

            checkIfCenterIsPerfect(center)

            return center
        }

    fun checkIfCenterIsPerfect(center: Point2D.Double?) {
        // Not large enough sample size to check
        if (this.totalParticles < 25) {
            return
        }

        var perfectParticles = 0

        for (healingCircleParticle in healingCircleParticles) {
            val point: Point2D.Double = healingCircleParticle.getPoint()

            val distance = point.distance(center)
            if (distance > (DIAMETER - 1) / 2f && distance < (DIAMETER + 1) / 2f) {
                perfectParticles++
            }
        }

        val percentagePerfect = perfectParticles / totalParticles.toFloat()

        if (percentagePerfect > 0.75) {
            this.cachedCenterPoint = center!!
        }
    }

    fun removeOldParticles() {
        val healingCircleParticleIterator = healingCircleParticles.iterator()
        while (healingCircleParticleIterator.hasNext()) {
            val healingCircleParticle = healingCircleParticleIterator.next()

            if (System.currentTimeMillis() - healingCircleParticle.getCreation() > 10000) {
                this.totalX -= healingCircleParticle.getPoint().getX()
                this.totalZ -= healingCircleParticle.getPoint().getY()
                totalParticles--

                healingCircleParticleIterator.remove()
            }
        }
    }

    fun hasCachedCenterPoint(): Boolean {
        return cachedCenterPoint != null
    }

    companion object {
        const val DIAMETER: Float = 12f
        const val RADIUS: Float = 12 / 2f

        private fun rotatePoint(point: Point2D.Double, center: Point2D.Double, degrees: Double): Point2D.Double {
            val radians = Math.toRadians(degrees)

            val newX =
                center.getX() + (point.getX() - center.getX()) * cos(radians) - (point.getY() - center.getY()) * sin(
                    radians
                )
            val newY =
                center.getY() + (point.getX() - center.getX()) * sin(radians) + (point.getY() - center.getY()) * cos(
                    radians
                )

            return Point2D.Double(newX, newY)
        }

        private fun lineLineIntersection(
            a: Point2D.Double,
            b: Point2D.Double,
            c: Point2D.Double,
            d: Point2D.Double
        ): Point2D.Double {
            // Line AB represented as a1x + b1y = c1
            val a1 = b.y - a.y
            val b1 = a.x - b.x
            val c1 = a1 * (a.x) + b1 * (a.y)

            // Line CD represented as a2x + b2y = c2
            val a2 = d.y - c.y
            val b2 = c.x - d.x
            val c2 = a2 * (c.x) + b2 * (c.y)

            val determinant = a1 * b2 - a2 * b1

            if (determinant == 0.0) {
                // The lines are parallel.
                return Point2D.Double(Double.NaN, Double.NaN)
            } else {
                val x = (b2 * c1 - b1 * c2) / determinant
                val y = (a1 * c2 - a2 * c1) / determinant
                return Point2D.Double(x, y)
            }
        }
    }
}
