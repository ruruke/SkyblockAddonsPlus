package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.asm.hooks.utils.ReturnValue
import moe.ruruke.skyblock.core.Feature.*
import net.minecraft.client.Minecraft
import net.minecraft.entity.item.EntityItem
import net.minecraft.inventory.ContainerPlayer
import net.minecraft.item.ItemStack

object EntityPlayerSPHook {
    private var lastItemName: String? = null
    private var lastDrop: Long = Minecraft.getSystemTime()

    fun dropOneItemConfirmation(returnValue: ReturnValue<*>): EntityItem? {
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        val mc: Minecraft = Minecraft.getMinecraft()
        val heldItemStack: ItemStack = mc.thePlayer.getHeldItem()

        if ((main.utils!!.isOnSkyblock() || main.getPlayerListener().aboutToJoinSkyblockServer())) {
            if (main.configValues!!.isEnabled(LOCK_SLOTS) && !main.utils!!
                    .isInDungeon()
            ) {
                val slot: Int = mc.thePlayer.inventory.currentItem + 36
                if (main.configValues!!.getLockedSlots()
                        .contains(slot) && (slot >= 9 || mc.thePlayer.openContainer is ContainerPlayer && slot >= 5)
                ) {
                    main.utils!!.playLoudSound("note.bass", 0.5)
                    main.utils!!.sendMessage(
                        "${main.configValues!!.getRestrictedColor(DROP_CONFIRMATION)}${moe.ruruke.skyblock.core.Translations.getMessage("messages.slotLocked")}"
                    )
                    returnValue.cancel()
                    return null
                }

                if (System.currentTimeMillis() - MinecraftHook.getLastLockedSlotItemChange() < 200) {
                    main.utils!!.playLoudSound("note.bass", 0.5)
                    main.utils!!.sendMessage(
                        main.configValues!!
                            .getRestrictedColor(DROP_CONFIRMATION).toString() + moe.ruruke.skyblock.core.Translations.getMessage(
                            "messages.switchedSlots"
                        )
                    )
                    returnValue.cancel()
                    return null
                }
            }

            if (heldItemStack != null && main.configValues!!
                    .isEnabled(STOP_DROPPING_SELLING_RARE_ITEMS) && !main.utils!!
                    .isInDungeon()
            ) {
                if (!main.utils!!.getItemDropChecker().canDropItem(heldItemStack, true)) {
                    main.utils!!.sendMessage(
                        main.configValues!!
                            .getRestrictedColor(STOP_DROPPING_SELLING_RARE_ITEMS).toString() + moe.ruruke.skyblock.core.Translations.getMessage(
                            "messages.cancelledDropping"
                        )
                    )
                    returnValue.cancel()
                    return null
                }

                if (System.currentTimeMillis() - MinecraftHook.getLastLockedSlotItemChange() < 200) {
                    main.utils!!.playLoudSound("note.bass", 0.5)
                    main.utils!!.sendMessage(
                        main.configValues!!
                            .getRestrictedColor(DROP_CONFIRMATION).toString() + moe.ruruke.skyblock.core.Translations.getMessage(
                            "messages.switchedSlots"
                        )
                    )
                    returnValue.cancel()
                    return null
                }
            }
        }

        if (heldItemStack != null && main.configValues!!
                .isEnabled(DROP_CONFIRMATION) && !main.utils!!
                .isInDungeon() && (main.utils!!.isOnSkyblock() || main.getPlayerListener()
                .aboutToJoinSkyblockServer()
                    || main.configValues!!.isEnabled(DOUBLE_DROP_IN_OTHER_GAMES))
        ) {
            lastDrop = Minecraft.getSystemTime()

            val heldItemName: String =
                if (heldItemStack.hasDisplayName()) heldItemStack.getDisplayName() else heldItemStack.getUnlocalizedName()

            if (lastItemName == null || lastItemName != heldItemName || Minecraft.getSystemTime() - lastDrop >= 3000L) {
                main.utils!!.sendMessage(
                    main.configValues!!
                        .getRestrictedColor(DROP_CONFIRMATION).toString() + moe.ruruke.skyblock.core.Translations.getMessage(
                        "messages.dropConfirmation"
                    )
                )
                lastItemName = heldItemName
                returnValue.cancel()
            }
        }

        return null
    }
}
