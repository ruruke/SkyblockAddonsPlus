package moe.ruruke.skyblock.gui.listeners

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.configValues
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.getLogger
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.utils
import moe.ruruke.skyblock.config.NewConfig
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.features.enchants.EnchantManager.parseEnchants
import moe.ruruke.skyblock.utils.ItemUtils
import moe.ruruke.skyblock.utils.RomanNumeralParser.replaceNumeralsWithIntegers
import net.minecraft.client.Minecraft
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class PlayerListener {
    /**
     * Modifies item enchantments on tooltips as well as roman numerals
     */
    val logger = getLogger()
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onItemTooltipLast(e: ItemTooltipEvent) {
        val hoveredItem = e.itemStack

        if (e.toolTip != null && utils!!.isOnSkyblock()) {
            if (configValues!!.isEnabled(Feature.ENCHANTMENT_LORE_PARSING)) {
                parseEnchants(e.toolTip, hoveredItem)
            }
            if (NewConfig.isEnabled(Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS)) {
                val startIndex =
                    if (configValues!!.isEnabled(Feature.DONT_REPLACE_ROMAN_NUMERALS_IN_ITEM_NAME)) 1 else 0

                for (i in startIndex until e.toolTip.size) {
                    e.toolTip[i] = replaceNumeralsWithIntegers(e.toolTip[i])
                }
            }

            if (configValues!!.isEnabled(Feature.SHOW_SKYBLOCK_ITEM_ID) ||
                configValues!!.isEnabled(Feature.DEVELOPER_MODE)
            ) {
                val itemId = ItemUtils.getSkyblockItemID(e.itemStack)
                val tooltipLine = EnumChatFormatting.DARK_GRAY.toString() + "skyblock:" + itemId

                if (itemId != null) {
                    if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
                        var i = e.toolTip.size
                        while (i-- > 0) {
                            if (e.toolTip[i].startsWith(EnumChatFormatting.DARK_GRAY.toString() + "minecraft:")) {
                                e.toolTip.add(i + 1, tooltipLine)
                                break
                            }
                        }
                    } else {
                        e.toolTip.add(tooltipLine)
                    }
                }
            }
        }
    }
}
