package moe.ruruke.skyblock.features.EntityOutlines

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.config.ConfigValues
import moe.ruruke.skyblock.config.NewConfig
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.Location
import moe.ruruke.skyblock.events.RenderEntityOutlineEvent
import moe.ruruke.skyblock.utils.ItemUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import java.util.function.Function

class FeatureItemOutlines {
    /**
     * Queues items to be outlined that satisfy global- and entity-level predicates.
     *
     * @param e the outline event
     */
    @SubscribeEvent
    fun onRenderEntityOutlines(e: RenderEntityOutlineEvent) {
        // Cache constants
        location = SkyblockAddonsPlus.instance.utils!!.getLocation()
        config = SkyblockAddonsPlus.instance.configValues

        if (e.getType() == RenderEntityOutlineEvent.Type.XRAY) {
            // Test whether we should add any entities at all
            if (GLOBAL_TEST()) {
                // Queue specific items for outlining
                e.queueEntitiesToOutline(OUTLINE_COLOR)
            }
        }
    }

    companion object {
        /**
         * List of skyblock locations where we might see items in showcases
         */
        private val SHOWCASE_ITEM_LOCATIONS = HashSet(
            Arrays.asList(
                Location.VILLAGE, Location.AUCTION_HOUSE, Location.BANK, Location.BAZAAR,
                Location.COAL_MINE, Location.LIBRARY, Location.JERRYS_WORKSHOP, Location.THE_END
            )
        )


        /**
         * Cached value of the client's skyblock location
         */
        private var location: Location? = null

        /**
         * Cached value of the client's config
         */
        private var config: ConfigValues? = null


        /**
         * Entity-level predicate to determine whether a specific entity should be outlined, and if so, what color.
         * Should be used in conjunction with the global-level predicate, [.GLOBAL_TEST].
         *
         *
         * Return `null` if the entity should not be outlined, or the integer color of the entity to be outlined iff the entity should be outlined
         */
        private val OUTLINE_COLOR =
            Function<Entity, Int?> { e: Entity? ->
                // Only accept items that aren't showcase items
                if (e is EntityItem && (!SHOWCASE_ITEM_LOCATIONS.contains(location) || !isShopShowcaseItem(
                        e
                    ))
                ) {
                    val itemRarity = ItemUtils.getRarity(e.entityItem)
                    if (itemRarity != null) {
                        // Return the rarity color of the item
                        return@Function itemRarity.getColorCode().getColor()
                    }
                    // Return null if the item doesn't have a rarity for some reason...
                    return@Function null
                }
                null
            }

        /**
         * Global-level predicate to determine whether any entities should outlined.
         * Should be used in conjunction with the entity-level predicate, [.OUTLINE_COLOR].
         *
         *
         * Don't accept if the player is on a personal island and the
         *
         * @return `false` iff no entities should be outlined (i.e., accept if the player has item outlines enabled for the current skyblock location)
         */
        private fun GLOBAL_TEST(): Boolean {
            return NewConfig.isEnabled(Feature.MAKE_DROPPED_ITEMS_GLOW) //&& (NewConfig.isEnabled(Feature.SHOW_GLOWING_ITEMS_ON_ISLAND) || location != Location.ISLAND)
        }

        /**
         * This method checks if the given EntityItem is an item being showcased in a shop.
         * It works by detecting glass case the item is in.
         *
         * @param entityItem the potential shop showcase item.
         * @return true iff the entity is a shop showcase item.
         */
        private fun isShopShowcaseItem(entityItem: EntityItem): Boolean {
            for (entityArmorStand in entityItem.worldObj.getEntitiesWithinAABB(
                EntityArmorStand::class.java, entityItem.entityBoundingBox
            )) {
                if (entityArmorStand.isInvisible && entityArmorStand.getEquipmentInSlot(4) != null && entityArmorStand.getEquipmentInSlot(
                        4
                    ).item === Item.getItemFromBlock(Blocks.glass)
                ) {
                    return true
                }
            }
            return false
        }
    }
}
