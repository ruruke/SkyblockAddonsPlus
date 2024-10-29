package moe.ruruke.skyblock.core.enchantedItemBlacklist

import cc.polyfrost.oneconfig.libs.checker.nullness.qual.NonNull
import com.google.common.collect.Lists
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.utils
import moe.ruruke.skyblock.core.Location
import moe.ruruke.skyblock.utils.ItemUtils.Companion.getRarity
import moe.ruruke.skyblock.utils.ItemUtils.Companion.getSkyblockItemID
import moe.ruruke.skyblock.utils.ItemUtils.Companion.isMaterialForRecipe
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.*
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.event.entity.player.FillBucketEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * This class is the main class of the "Avoid Placing Enchanted Items" feature. Whenever a player tries to place an item
 * on their private island, this class is used to check if the action should be blocked or allowed.
 */
class EnchantedItemPlacementBlocker {

    companion object {

        fun setItemLists(itemLists: EnchantedItemLists?) {
            Companion.itemLists = itemLists
        }
        private val INTERACTIVE_BLOCKS: ArrayList<Block> = Lists.newArrayList(
            Blocks.acacia_door, Blocks.anvil, Blocks.beacon, Blocks.birch_door, Blocks.brewing_stand, Blocks.chest,
            Blocks.powered_comparator, Blocks.unpowered_comparator, Blocks.crafting_table, Blocks.dark_oak_door,
            Blocks.daylight_detector, Blocks.daylight_detector_inverted, Blocks.dispenser, Blocks.dropper,
            Blocks.enchanting_table, Blocks.furnace, Blocks.hopper, Blocks.iron_door, Blocks.iron_trapdoor,
            Blocks.jungle_door, Blocks.lever, Blocks.lit_furnace, Blocks.oak_door, Blocks.powered_repeater,
            Blocks.unpowered_repeater, Blocks.stone_button, Blocks.trapdoor, Blocks.trapped_chest, Blocks.wooden_button
        )

        private val CLASSES_OF_ITEMS_THAT_CAN_BE_PLACED: ArrayList<Class<*>> = Lists.newArrayList(
            ItemBucket::class.java, ItemRedstone::class.java,
            ItemReed::class.java, ItemSeedFood::class.java, ItemSeeds::class.java, ItemSkull::class.java
        )

        private var itemLists: EnchantedItemLists? = null

        /*
     This is the item stack from the last PlayerInteractEvent with the RIGHT_CLICK_BLOCK action that was processed.
     This is used to check for and block the PlayerInteractEvent with the RIGHT_CLICK_AIR action that always follows
     RIGHT_CLICK_BLOCK events.
     */
        private var lastItemStack: ItemStack? = null
        private var lastBucketEventBlocked = false

        //TODO: block using of enchanted dyes on sheep
        /**
         * Determine if the placement of this item should be blocked.
         *
         * @param itemStack the item being placed
         * @param event the event that was triggered when the player used the item, can be either [PlayerInteractEvent]
         * or [FillBucketEvent]
         * @return `true` if the usage should be blocked, `false` otherwise.
         */
        fun shouldBlockPlacement(itemStack: @NonNull ItemStack, event: Event): Boolean {
            /*
        Block placing blocks only on the private island since that's the only place players can place blocks at.
         */
            if (utils!!.getLocation() == Location.ISLAND && canBePlaced(itemStack.item)) {
                val heldItemId = getSkyblockItemID(itemStack)

                /*
             Don't block non-Skyblock items and non-enchanted items.
             Also, If the item is a material for a recipe, placement will be blocked server-side.
             */
                if (heldItemId == null || !itemStack.isItemEnchanted || isMaterialForRecipe(itemStack)) {
                    return false
                } else if (event is PlayerInteractEvent) {
                    val interactEvent = event

                    if (interactEvent.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
                        // Left clicking doesn't place a block.
                        return false
                    } else if (interactEvent.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                        lastItemStack = itemStack
                    } else if (isSecondaryRightClickAirEvent(interactEvent, itemStack)) {
                        return true
                    }
                }

                if (itemLists!!.whitelistedIDs.contains(heldItemId)) {
                    return false
                }

                if (itemLists!!.blacklistedIDs.contains(heldItemId)) {
                    return willBePlaced(event, itemStack)
                }

                val rarity = getRarity(itemStack)

                // If this item isn't found in the blacklist, check if it's rarity is above the rarity limit.
                if (rarity != null && itemLists!!.rarityLimit.compareTo(rarity) <= 0) {
                    return willBePlaced(event, itemStack)
                }
            }

            return false
        }

        /*
    Checks if the given event is the corresponding PlayerInteractEvent with an Action of RIGHT_CLICK_AIR that comes after
    a PlayerInteractEvent with an Action of RIGHT_CLICK_BLOCK.
    Minecraft sends a PlayerInteractEvent with an Action of RIGHT_CLICK_BLOCK first and then one with RIGHT_CLICK_AIR
    directly after. We need to block both of them.
     */
        private fun isSecondaryRightClickAirEvent(event: PlayerInteractEvent, itemStack: ItemStack): Boolean {
            if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && lastItemStack != null &&
                itemStack.getIsItemStackEqual(lastItemStack)
            ) {
                lastItemStack = null
                return true
            } else {
                return false
            }
        }


        /*
    Checks if the given item can be placed down like a block.
     */
        private fun canBePlaced(item: Item): Boolean {
            return Block.getBlockFromItem(item) != null || CLASSES_OF_ITEMS_THAT_CAN_BE_PLACED.contains(item.javaClass)
        }

        /*
    Checks if the item will be successfully placed based on the player's action and what the player is clicking on.
     */
        private fun willBePlaced(event: Event, itemStack: ItemStack): Boolean {
            if (event is PlayerInteractEvent) {
                val interactEvent = event

                /*
            Check if the player will use the held item or activate the clicked block if the player is
            right clicking on a block.
             */
                if (interactEvent.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                    val clickedBlock = Minecraft.getMinecraft().theWorld.getBlockState(interactEvent.pos).block

                    return willNotActivateBlock(interactEvent.action, interactEvent.entityPlayer, clickedBlock)
                } else {
                    /*
                Buckets can be placed slightly farther than the player's standard reach for placing blocks.
                When a player tries to place a bucket beyond their block-placing reach, a PlayerInteractEvent with the
                action RIGHT_CLICK_AIR is fired first. Then the client sends a packet to the server indicating the player
                used the bucket. Finally, the client checks if the bucket can be placed. The objective of this block is
                to run the check before the packet is sent, so the mod can block the packet from being sent.
                 */
                    if (itemStack.item is ItemBucket) {
                        // This fires a FillBucketEvent which is checked by the else block below.
                        itemStack.item.onItemRightClick(itemStack, interactEvent.world, interactEvent.entityPlayer)

                        if (lastBucketEventBlocked) {
                            lastBucketEventBlocked = false
                            return true
                        }
                    }
                    return false
                }
            } else {
                // The player is trying to use a bucket.
                if ((event as FillBucketEvent).target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    lastBucketEventBlocked = true
                    return true
                } else {
                    return false
                }
            }
        }

        /*
    Checks if the given action the given player is doing will not activate the given block.
    If the player right clicks on an interactive block like a chest, the item won't be used.
    The player will activate the block instead.
     */
        private fun willNotActivateBlock(
            action: PlayerInteractEvent.Action,
            player: EntityPlayer,
            block: Block
        ): Boolean {
            requireNotNull(block) { "Block cannot be null!" }

            return action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || player.isSneaking || !INTERACTIVE_BLOCKS.contains(
                block
            )
        }
    }
}