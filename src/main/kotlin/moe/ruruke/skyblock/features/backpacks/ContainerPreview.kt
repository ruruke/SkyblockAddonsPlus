package moe.ruruke.skyblock.features.backpacks

import net.minecraft.item.ItemStack
import kotlin.math.min

class ContainerPreview(
    items: List<ItemStack>,
    private val name: String?,
    private val backpackColor: BackpackColor?, rows: Int, cols: Int
) {
    private var x = 0
    fun setX(x: Int) {
        this.x = x
    }
    fun getX(): Int {
        return x
    }
    private var y = 0
    fun setY(y: Int) {
        this.y = y
    }
    fun getY(): Int {
        return y
    }
    fun getName(): String? {
        return name
    }
    fun getBackpackColor(): BackpackColor?{
        return backpackColor
    }
    private val numRows = min(rows.toDouble(), 6.0).toInt()
    fun getNumRows(): Int = numRows
    private val numCols = min(cols.toDouble(), 9.0).toInt()
    fun getNumCols(): Int = numCols

    private val items: List<ItemStack> = items
    fun getItems(): List<ItemStack> {
        return items
    }

    constructor(
        items: List<ItemStack>,
        backpackName: String?,
        backpackColor: BackpackColor,
        rows: Int,
        cols: Int,
        x: Int,
        y: Int
    ) : this(items, backpackName, backpackColor, rows, cols) {
        this.x = x
        this.y = y
    }
}
