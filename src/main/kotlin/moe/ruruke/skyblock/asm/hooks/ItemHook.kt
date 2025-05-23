package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.asm.utils.ReturnValue
import moe.ruruke.skyblock.features.cooldowns.CooldownManager
import net.minecraft.item.ItemStack


class ItemHook {
    companion object {
        @JvmStatic
        fun isItemDamaged(stack: ItemStack): Boolean {
            val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
            if (main.utils!!.isOnSkyblock() && main.configValues!!
                    .isEnabled(moe.ruruke.skyblock.core.Feature.SHOW_ITEM_COOLDOWNS)
            ) {
                if (CooldownManager.isOnCooldown(stack)) {
                    return true
                }
            }
            return stack.isItemDamaged()
        }
        @JvmStatic
        fun getDurabilityForDisplay(stack: ItemStack, returnValue: ReturnValue<Double?>) { //Item item, ItemStack stack
            val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
            if (main.utils!!.isOnSkyblock() && main.configValues!!
                    .isEnabled(moe.ruruke.skyblock.core.Feature.SHOW_ITEM_COOLDOWNS)
            ) {
                if (CooldownManager.isOnCooldown(stack)) {
                    returnValue.cancel(CooldownManager.getRemainingCooldownPercent(stack))
                }
            }
        }
    }
}
