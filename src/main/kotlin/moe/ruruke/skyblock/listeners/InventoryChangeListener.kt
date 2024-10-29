package moe.ruruke.skyblock.listeners

import net.minecraft.inventory.IInvBasic
import net.minecraft.inventory.InventoryBasic

/**
 * This listener is used for [GuiScreenListener]. Its
 * `onInventoryChanged` method is called when an item in the [InventoryBasic] it is listening
 * to changes.
 */
class InventoryChangeListener
/**
 * Creates a new `InventoryChangeListener` with the given `GuiScreenListener` reference.
 *
 * @param guiScreenListener the `GuiScreenListener` reference
 */(private val GUI_SCREEN_LISTENER: GuiScreenListener) : IInvBasic {
    /**
     * This is called when an item in the `InventoryBasic` being listened to changes.
     *
     * @param inventory the `InventoryBasic` after the change
     */
    override fun onInventoryChanged(inventory: InventoryBasic) {
        GUI_SCREEN_LISTENER.onInventoryChanged(inventory)
    }
}
