package moe.ruruke.skyblock.features

import moe.ruruke.skyblock.utils.ColorCode
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack


class SlayerArmorProgress {
    /** The itemstack that this progress is representing.  */
    private val itemStack: ItemStack

    /** The current slayer progress % of the item.  */
    
    private var percent: String

    /** The current slayer defence reward of the item.  */
    
    private var defence: String

    constructor(itemStack: ItemStack) {
        this.itemStack = ItemStack(itemStack.getItem()) // Cloned because we change the helmet color later.
        this.percent = "55"
        this.defence = "§a40❈"

        setHelmetColor()
    }

    constructor(itemStack: ItemStack, percent: String, defence: String) {
        this.itemStack = itemStack
        this.percent = percent
        this.defence = defence
    }

    private fun setHelmetColor() {
        if (itemStack.getItem() == net.minecraft.init.Items.leather_helmet) {
            (itemStack.getItem() as ItemArmor).setColor(itemStack, ColorCode.BLACK.getColor())
        }
    }

    fun getItemStack(): ItemStack {
        return itemStack
    }
    fun setDefence(defence: String) {
        this.defence = defence
    }

    fun setPercent(percent: String) {
        this.percent = percent
    }
}
