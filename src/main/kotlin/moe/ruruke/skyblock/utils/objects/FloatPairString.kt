package moe.ruruke.skyblock.utils.objects




class FloatPairString(x: Float, y: Float, private var enchant: String) {
    private var floatPair: FloatPair = FloatPair(x, y)

    fun FloatPairString(x: Float, y: Float, enchant: String?) {
        this.floatPair = FloatPair(x, y)
        this.enchant = enchant!!
    }
    fun getX(): Float {
        return floatPair.getX()
    }

    fun getY(): Float {
        return floatPair.getY()
    }
}
