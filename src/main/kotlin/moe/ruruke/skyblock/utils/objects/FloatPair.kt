package moe.ruruke.skyblock.utils.objects

import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.apache.commons.lang3.mutable.MutableFloat

class FloatPair(x: Float, y: Float) {
    var x = x.toFloat()
    var y = y.toFloat()

//    fun getX(): Float {
//        return x
//    }
//
//    fun getY(): Float {
//        return y
//    }
//
//    fun setY(y: Float) {
//        this.y = y
//    }
//
//    fun setX(x: Float) {
//        this.x = x
//    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other === this) {
            return true
        }
        if (other.javaClass != javaClass) {
            return false
        }
        val otherFloatPair = other as FloatPair
        return EqualsBuilder().append(x, otherFloatPair.x).append(y, otherFloatPair.y).isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(83, 11).append(x).append(y).toHashCode()
    }

    override fun toString(): String {
        return x.toString() + "|" + y
    }

    fun cloneCoords(): FloatPair {
        return FloatPair(x, y)
    }
}
