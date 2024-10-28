package moe.ruruke.skyblock.core

enum class Attribute(private val defaultValue: Float) {
    DEFENCE(0.toFloat()),
    HEALTH(100.toFloat()),
    MAX_HEALTH(100.toFloat()),
    MANA(100.toFloat()),
    MAX_MANA(100.toFloat()),
    FUEL(3000.toFloat()),
    MAX_FUEL(3000.toFloat()),
    OVERFLOW_MANA(20.toFloat())
}
