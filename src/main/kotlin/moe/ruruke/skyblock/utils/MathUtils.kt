package moe.ruruke.skyblock.utils

import net.minecraft.entity.Entity
import net.minecraft.util.MathHelper

object MathUtils {
    fun isInside(x: Int, y: Int, minX: Int, minY: Int, maxX: Int, maxY: Int): Boolean {
        return x >= minX && x <= maxX && y > minY && y < maxY
    }

    fun isInside(x: Float, y: Float, minX: Float, minY: Float, maxX: Float, maxY: Float): Boolean {
        return x >= minX && x <= maxX && y > minY && y < maxY
    }

    /**
     * Converts a regular value to a normalized slider value.
     *
     *
     * For example, if a slider's value can be from 0 to 5, and the value given is 2.5, that is half
     * of the slider's total value, so this will return 0.5.
     *
     *
     * This will also snap to the given step value. If the step value is 0.1 and the given value is
     * 0.05, that will round to 0.1 giving a slider its "snapping" effect.
     *
     * @param value The denormalized slider value (usually min -> max)
     * @param min   The min slider value
     * @param max   The max slider value
     * @param step  The step value
     * @return The normalized slider value (0 -> 1F)
     */
    fun normalizeSliderValue(value: Float, min: Float, max: Float, step: Float): Float {
        return clamp((snapToStep(value, step) - min) / (max - min), 0.0f, 1.0f)
    }

    /**
     * Converts a normalized slider value to a regular value.
     *
     *
     * For example, if a slider's value can be from 0 to 5, and the value given is 0.5,
     * this number will by multiplied by the slider's range, returning 2.5.
     *
     *
     * This will also snap to the given step value. If the step value is 0.1 and the value that was
     * going to be returned is 0.05, that will round to 0.1 giving a slider its "snapping" effect.
     *
     * @param value The normalized slider value (usually 0 -> 1F)
     * @param min   The min slider value
     * @param max   The max slider value
     * @param step  The step value
     * @return The denormalized slider value (min -> max)
     */
    fun denormalizeSliderValue(value: Float, min: Float, max: Float, step: Float): Float {
        return clamp(snapToStep(min + (max - min) * clamp(value, 0f, 1f), step), min, max)
    }

    /**
     * Snaps the given value to the step amount.
     *
     *
     * For example, if the step is 0.1 and the value is 0.15, that will round up to 0.2 giving a
     * slider its "snapping" effect.
     *
     * @param value The value to round
     * @param step  The step amount
     * @return The value rounded to the step amount
     */
    private fun snapToStep(value: Float, step: Float): Float {
        return step * Math.round(value / step).toFloat()
    }

    /**
     * Clamps a value between two bounds.
     *
     *
     * For example, if the given value is 2.5, and the max is 2, this will round down to 2.
     *
     * @param value The value to round
     * @param min   The bottom bound
     * @param max   The top bounds
     * @return The value rounded to the given bounds
     */
    fun clamp(value: Float, min: Float, max: Float): Float {
        return if (value < min) min else (if (value > max) max else value)
    }

    fun interpolateX(entity: Entity, partialTicks: Float): Double {
        return interpolate(entity.prevPosX, entity.posX, partialTicks)
    }

    fun interpolateY(entity: Entity, partialTicks: Float): Double {
        return interpolate(entity.prevPosY, entity.posY, partialTicks)
    }

    fun interpolateZ(entity: Entity, partialTicks: Float): Double {
        return interpolate(entity.prevPosZ, entity.posZ, partialTicks)
    }

    fun interpolate(first: Double, second: Double, partialTicks: Float): Double {
        return first + (second - first) * partialTicks.toDouble()
    }

    fun distance(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Double {
        return distance(x1.toFloat(), y1.toFloat(), z1.toFloat(), x2.toFloat(), y2.toFloat(), z2.toFloat())
    }

    fun distance(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float): Double {
        val deltaX = x1 - x2
        val deltaY = y1 - y2
        val deltaZ = z1 - z2
        return MathHelper.sqrt_float(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()
    }

    fun distance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return distance(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())
    }

    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        val deltaX = x1 - x2
        val deltaY = y1 - y2
        return MathHelper.sqrt_float(deltaX * deltaX + deltaY * deltaY).toDouble()
    }
}
