package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.InventoryType
import moe.ruruke.skyblock.features.backpacks.ContainerPreviewManager
import moe.ruruke.skyblock.features.cooldowns.CooldownManager
import moe.ruruke.skyblock.utils.InventoryUtils
import net.minecraft.item.ItemStack
import net.minecraft.util.IChatComponent


class GuiScreenHook {
    companion object {
        private const val MADDOX_BATPHONE_COOLDOWN = 20 * 1000
        @JvmStatic
        fun onRenderTooltip(itemStack: ItemStack, x: Int, y: Int): Boolean {
            val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance

            if (main.configValues!!
                    .isEnabled(moe.ruruke.skyblock.core.Feature.DISABLE_EMPTY_GLASS_PANES) && main.utils!!
                    .isEmptyGlassPane(itemStack)
            ) {
                return true
            }

            if (main.configValues!!
                    .isDisabled(moe.ruruke.skyblock.core.Feature.SHOW_EXPERIMENTATION_TABLE_TOOLTIPS) && (main.inventoryUtils!!
                    .getInventoryType() === InventoryType.ULTRASEQUENCER || main.inventoryUtils!!
                    .getInventoryType() === InventoryType.CHRONOMATRON)
            ) {
                return true
            }

            return ContainerPreviewManager.onRenderTooltip(itemStack, x, y)
        }

        @JvmStatic
        //TODO: Fix for Hypixel localization
        fun handleComponentClick(component: IChatComponent?) {
            val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
            if (main.utils!!
                    .isOnSkyblock() && component != null && "§2§l[OPEN MENU]" == component.getUnformattedText() &&
                !CooldownManager.isOnCooldown(InventoryUtils.MADDOX_BATPHONE_ID)
            ) { // The prompt when Maddox picks up the phone.
                CooldownManager.put(InventoryUtils.MADDOX_BATPHONE_ID)
            }
        }
    }
}
