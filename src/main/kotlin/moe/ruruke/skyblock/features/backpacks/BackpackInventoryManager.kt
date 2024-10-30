package moe.ruruke.skyblock.features.backpacks


/**
 * Class with information on the currently opened backpack
 */
object BackpackInventoryManager {
    private var backpackColor: BackpackColor? = null
    fun setBackpackColor(backpackColor: BackpackColor) {
        this.backpackColor = backpackColor
    }
    fun getBackpackColor(): BackpackColor? {
        return backpackColor
    }
}
