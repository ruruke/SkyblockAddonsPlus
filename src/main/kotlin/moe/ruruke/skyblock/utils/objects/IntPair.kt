package moe.ruruke.skyblock.utils.objects

import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.apache.commons.lang3.mutable.MutableInt

class IntPair(x: Int, y: Int) {
    private val x = MutableInt(x)
    private val y = MutableInt(y)

    fun getX(): Int {
        return x.value
    }

    fun getY(): Int {
        return y.value
    }

    fun setY(y: Int) {
        this.y.setValue(y)
    }

    fun setX(x: Int) {
        this.x.setValue(x)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (obj === this) {
            return true
        }
        if (obj.javaClass != javaClass) {
            return false
        }
        val chunkCoords = obj as IntPair
        return EqualsBuilder().append(getX(), chunkCoords.getX()).append(getY(), chunkCoords.getY()).isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder(83, 11).append(getX()).append(getY()).toHashCode()
    }

    override fun toString(): String {
        return getX().toString() + "|" + getY()
    }

    fun cloneCoords(): IntPair {
        return IntPair(getX(), getY())
    }
}
