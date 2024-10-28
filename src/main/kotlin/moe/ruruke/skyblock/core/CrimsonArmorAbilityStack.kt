package moe.ruruke.skyblock.core


enum class CrimsonArmorAbilityStack(
    private val armorName: String,
    private val abilityName: String,
    private val symbol: String
) {
    CRIMSON("Crimson", "Dominus", "ᝐ"),
    TERROR("Terror", "Hydra Strike", "⁑"),
    AURORA("Aurora", "Arcane Vision", "Ѫ"),
    FERVOR("Fervor", "Fervor", "҉");

    fun getArmorName() = armorName
    fun getAbilityName() = abilityName
    fun getSymbol() = symbol
    //lombok plugin moment
    private var currentValue = 0
    fun getCurrentValue() = currentValue
    fun setCurrentValue(_currentValue: Int) {
        currentValue = _currentValue
    }
}
