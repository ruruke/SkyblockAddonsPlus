package moe.ruruke.skyblock.shader




class UniformType<T>(i: Int) {
    private val amount = 0

    companion object {
        val FLOAT: UniformType<Float> = UniformType(1)
        val VEC3: UniformType<Array<Float>> = UniformType(3)
    }
}
