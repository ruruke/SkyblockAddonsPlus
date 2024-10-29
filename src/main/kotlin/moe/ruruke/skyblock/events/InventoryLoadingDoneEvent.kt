package moe.ruruke.skyblock.events

import net.minecraftforge.fml.common.eventhandler.Event

/**
 * Fired when all the slots in an open `GuiChest` are done loading.
 * This is used to run logic that depends on Skyblock menus being fully loaded.
 */
class InventoryLoadingDoneEvent : Event()
