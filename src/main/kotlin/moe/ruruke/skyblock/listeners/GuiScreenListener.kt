package moe.ruruke.skyblock.listeners


import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.asm.hooks.GuiChestHook
import moe.ruruke.skyblock.config.NewConfig
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.InventoryType
import moe.ruruke.skyblock.events.InventoryLoadingDoneEvent
import moe.ruruke.skyblock.features.backpacks.ContainerPreviewManager
import moe.ruruke.skyblock.misc.scheduler.ScheduledTask
import moe.ruruke.skyblock.misc.scheduler.SkyblockRunnable
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.InventoryBasic
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent
import net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.util.concurrent.ThreadLocalRandom

/**
 * This listener listens for events that happen while a [GuiScreen] is open.
 *
 * @author ILikePlayingGames
 * @version 1.5
 */
class GuiScreenListener {
    private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance

    private var inventoryChangeListener: InventoryChangeListener? = null
    private var listenedInventory: InventoryBasic? = null
    private var inventoryChangeTimeCheckTask: ScheduledTask? = null

    /** Time in milliseconds of the last time a `GuiContainer` was closed  */
    
    private var lastContainerCloseMs: Long = -1

    /** Time in milliseconds of the last time a backpack was opened, used by [Feature.BACKPACK_OPENING_SOUND].  */
    
    private var lastBackpackOpenMs: Long = -1

    /** Time in milliseconds of the last time an item in the currently open `GuiContainer` changed  */
    private var lastInventoryChangeMs: Long = -1

    @SubscribeEvent
    fun beforeInit(e: InitGuiEvent.Pre) {
        if (!main.utils!!.isOnSkyblock()) {
            return
        }

        val guiScreen = e.gui

        if (guiScreen is GuiChest) {
            val mc = Minecraft.getMinecraft()
            val guiChest = guiScreen
            val inventoryType: InventoryType? = main.inventoryUtils!!.updateInventoryType(guiChest)
            val chestInventory = guiChest.lowerChestInventory as InventoryBasic
            addInventoryChangeListener(chestInventory)

            // Backpack opening sound
            if (main.configValues!!.isEnabled(Feature.BACKPACK_OPENING_SOUND) && chestInventory.hasCustomName()) {
                if (chestInventory.displayName.unformattedText.contains("Backpack")) {
                    lastBackpackOpenMs = System.currentTimeMillis()

                    if (ThreadLocalRandom.current().nextInt(0, 2) == 0) {
                        mc.thePlayer.playSound("mob.horse.armor", 0.5f, 1f)
                    } else {
                        mc.thePlayer.playSound("mob.horse.leather", 0.5f, 1f)
                    }
                }
            }

            if (NewConfig.isEnabled(Feature.SHOW_BACKPACK_PREVIEW)) {
                if (inventoryType === InventoryType.STORAGE_BACKPACK || inventoryType === InventoryType.ENDER_CHEST) {
                    ContainerPreviewManager.onContainerOpen(chestInventory)
                }
            }
        }
    }

    @SubscribeEvent
    fun onGuiOpen(e: GuiOpenEvent) {
        if (!main.utils!!.isOnSkyblock()) {
            return
        }

        val guiScreen = e.gui
        val oldGuiScreen = Minecraft.getMinecraft().currentScreen

        // Closing a container
        if (guiScreen == null && oldGuiScreen is GuiContainer) {
            lastContainerCloseMs = System.currentTimeMillis()
        }

        // Closing or switching to a different GuiChest
        if (oldGuiScreen is GuiChest) {
            if (inventoryChangeListener != null) {
                removeInventoryChangeListener(listenedInventory!!)
            }

            ContainerPreviewManager.onContainerClose()
            GuiChestHook.onGuiClosed()
        }
    }
//
//    /**
//     * Listens for key presses while a GUI is open
//     *
//     * @param event the `GuiScreenEvent.KeyboardInputEvent` to listen for
//     */
//    @SubscribeEvent
//    fun onKeyInput(event: KeyboardInputEvent.Pre) {
//        val eventKey = Keyboard.getEventKey()
//
//        if (main.configValues!!.isEnabled(Feature.DEVELOPER_MODE) && eventKey == main.getDeveloperCopyNBTKey()
//                .getKeyCode() && Keyboard.getEventKeyState()
//        ) {
//            // Copy Item NBT
//            val currentScreen = event.gui
//
//            // Check if the player is in an inventory.
//            if (GuiContainer::class.java.isAssignableFrom(currentScreen.javaClass)) {
//                val currentSlot = (currentScreen as GuiContainer).slotUnderMouse
//
//                if (currentSlot != null && currentSlot.hasStack) {
//                    DevUtils.copyNBTTagToClipboard(
//                        currentSlot.stack.serializeNBT(),
//                        ColorCode.GREEN + "Item data was copied to clipboard!"
//                    )
//                }
//            }
//        }
//
//        if (main.configValues!!.isEnabled(Feature.DUNGEONS_MAP_DISPLAY) &&
//            main.configValues!!.isEnabled(Feature.CHANGE_DUNGEON_MAP_ZOOM_WITH_KEYBOARD) &&
//            Minecraft.getMinecraft().currentScreen is LocationEditGui
//        ) {
//            if (Keyboard.isKeyDown(main.getKeyBindings().get(5).getKeyCode()) && Keyboard.getEventKeyState()) {
//                DungeonMapManager.decreaseZoomByStep()
//            } else if (Keyboard.isKeyDown(main.getKeyBindings().get(4).getKeyCode()) && Keyboard.getEventKeyState()) {
//                DungeonMapManager.increaseZoomByStep()
//            }
//        }
//    }
//
//    @SubscribeEvent
//    fun onInventoryLoadingDone(e: InventoryLoadingDoneEvent?) {
//        removeInventoryChangeListener(listenedInventory!!)
//        lastInventoryChangeMs = -1
//    }
//
//    @SubscribeEvent
//    fun onMouseClick(event: GuiScreenEvent.MouseInputEvent.Pre) {
//        if (!main.utils!!.isOnSkyblock()) {
//            return
//        }
//
//        val eventButton = Mouse.getEventButton()
//
//        // Ignore button up
//        if (!Mouse.getEventButtonState()) {
//            return
//        }
//
//        if (main.configValues!!.isEnabled(Feature.LOCK_SLOTS) && event.gui is GuiContainer) {
//            val guiContainer = event.gui as GuiContainer
//
//            if (eventButton >= 0) {
//                /*
//                This prevents swapping items in/out of locked hotbar slots when using a hotbar key binding that is bound
//                to a mouse button.
//                 */
//                for (i in 0..8) {
//                    if (eventButton - 100 == Minecraft.getMinecraft().gameSettings.keyBindsHotbar[i].keyCode) {
//                        val slot = guiContainer.slotUnderMouse
//                        val hotbarSlot =
//                            guiContainer.inventorySlots.getSlot(guiContainer.inventorySlots.inventorySlots.size - (9 - i))
//
//                        if (slot == null || hotbarSlot == null) {
//                            return
//                        }
//
//                        if (main.configValues!!.getLockedSlots().contains(i + 36)) {
//                            if (!slot.hasStack && !hotbarSlot.hasStack) {
//                                return
//                            } else {
//                                main.utils!!.playLoudSound("note.bass", 0.5)
//                                main.utils!!.sendMessage(
//                                    main.configValues!!
//                                        .getRestrictedColor(Feature.DROP_CONFIRMATION) + Translations.getMessage("messages.slotLocked")
//                                )
//                                event.isCanceled = true
//                            }
//                        }
//                    }
//                }
//
//                //TODO: Cover shift-clicking into locked slots
//            }
//        }
//    }
//
    /**
     * Called when a slot in the currently opened `GuiContainer` changes. Used to determine if all its items have been loaded.
     */
    fun onInventoryChanged(inventory: InventoryBasic) {
        val currentTimeMs = System.currentTimeMillis()

        if (inventory.getStackInSlot(inventory.sizeInventory - 1) != null) {
            MinecraftForge.EVENT_BUS.post(InventoryLoadingDoneEvent())
        } else {
            lastInventoryChangeMs = currentTimeMs
        }
    }

    /**
     * Adds a change listener to a given inventory.
     *
     * @param inventory the inventory to add the change listener to
     */
    private fun addInventoryChangeListener(inventory: InventoryBasic) {
        if (inventory == null) {
            throw NullPointerException("Tried to add listener to null inventory.")
        }

        lastInventoryChangeMs = System.currentTimeMillis()
        inventoryChangeListener = InventoryChangeListener(this)
        inventory.addInventoryChangeListener(inventoryChangeListener)
        listenedInventory = inventory
        inventoryChangeTimeCheckTask = main.newScheduler!!.scheduleRepeatingTask(object : SkyblockRunnable() {
            override fun run() {
                checkLastInventoryChangeTime()
            }
        }, 20, 5)
    }

    /**
     * Checks whether it has been more than one second since the last inventory change, which indicates inventory
     * loading is most likely finished. Could trigger incorrectly with a lag spike.
     */
    private fun checkLastInventoryChangeTime() {
        if (listenedInventory != null) {
            if (lastInventoryChangeMs > -1 && System.currentTimeMillis() - lastInventoryChangeMs > 1000) {
                MinecraftForge.EVENT_BUS.post(InventoryLoadingDoneEvent())
            }
        }
    }

    /**
     * Removes [.inventoryChangeListener] from a given [InventoryBasic].
     *
     * @param inventory the `InventoryBasic` to remove the listener from
     */
    private fun removeInventoryChangeListener(inventory: InventoryBasic) {
        if (inventory == null) {
            throw NullPointerException("Tried to remove listener from null inventory.")
        }

        if (inventoryChangeListener != null) {
            try {
                inventory.removeInventoryChangeListener(inventoryChangeListener)
            } catch (e: NullPointerException) {
                SkyblockAddonsPlus.utils!!.sendErrorMessage(
                    "Tried to remove an inventory listener from a container that has no listeners."
                )
            }

            if (inventoryChangeTimeCheckTask != null) {
                if (!inventoryChangeTimeCheckTask!!.isCanceled()) {
                    inventoryChangeTimeCheckTask!!.cancel()
                }
            }

            inventoryChangeListener = null
            listenedInventory = null
            inventoryChangeTimeCheckTask = null
        }
    }
}
