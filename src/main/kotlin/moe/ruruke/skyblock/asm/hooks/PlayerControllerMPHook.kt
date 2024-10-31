package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.asm.utils.ReturnValue
import moe.ruruke.skyblock.core.InventoryType
import moe.ruruke.skyblock.events.SkyblockBlockBreakEvent
import moe.ruruke.skyblock.features.backpacks.BackpackColor
import moe.ruruke.skyblock.features.backpacks.BackpackInventoryManager
import moe.ruruke.skyblock.features.craftingpatterns.CraftingPattern
import moe.ruruke.skyblock.features.craftingpatterns.CraftingPatternResult
import moe.ruruke.skyblock.utils.ItemUtils
import net.minecraft.block.BlockPrismarine
import net.minecraft.block.BlockStone
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerPlayer
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockPos
import net.minecraftforge.common.MinecraftForge

class PlayerControllerMPHook {
    companion object {

        /**
         * clickModifier value in [.onWindowClick]  for shift-clicks
         */
        private const val SHIFTCLICK_CLICK_TYPE = 1

        /**
         * Cooldown between playing error sounds to avoid stacking up
         */
        private const val CRAFTING_PATTERN_SOUND_COOLDOWN = 400

        @JvmStatic
        private var lastCraftingSoundPlayed: Long = 0
        @JvmStatic
        private val ORES: Set<Int> = com.google.common.collect.Sets.newHashSet(
            net.minecraft.block.Block.getIdFromBlock(Blocks.coal_ore),
            net.minecraft.block.Block.getIdFromBlock(Blocks.iron_ore),
            net.minecraft.block.Block.getIdFromBlock(Blocks.gold_ore),
            net.minecraft.block.Block.getIdFromBlock(Blocks.redstone_ore),
            net.minecraft.block.Block.getIdFromBlock(Blocks.emerald_ore),
            net.minecraft.block.Block.getIdFromBlock(Blocks.lapis_ore),
            net.minecraft.block.Block.getIdFromBlock(Blocks.diamond_ore),
            net.minecraft.block.Block.getIdFromBlock(Blocks.lit_redstone_ore),
            moe.ruruke.skyblock.utils.Utils.getBlockMetaId(Blocks.stone, BlockStone.EnumType.DIORITE_SMOOTH.getMetadata()),
            moe.ruruke.skyblock.utils.Utils.getBlockMetaId(Blocks.stained_hardened_clay, EnumDyeColor.CYAN.getMetadata()),
            moe.ruruke.skyblock.utils.Utils.getBlockMetaId(Blocks.prismarine, BlockPrismarine.EnumType.ROUGH.getMetadata()),
            moe.ruruke.skyblock.utils.Utils.getBlockMetaId(Blocks.prismarine, BlockPrismarine.EnumType.DARK.getMetadata()),
            moe.ruruke.skyblock.utils.Utils.getBlockMetaId(
                Blocks.prismarine,
                BlockPrismarine.EnumType.BRICKS.getMetadata()
            ),
            moe.ruruke.skyblock.utils.Utils.getBlockMetaId(Blocks.wool, EnumDyeColor.LIGHT_BLUE.getMetadata()),
            moe.ruruke.skyblock.utils.Utils.getBlockMetaId(Blocks.wool, EnumDyeColor.GRAY.getMetadata())
        )

        /**
         * Checks if an item is being dropped and if an item is being dropped, whether it is allowed to be dropped.
         * This check works only for mouse clicks, not presses of the "Drop Item" key.
         *
         * @param clickModifier the click modifier
         * @param slotNum the number of the slot that was clicked on
         * @param heldStack the item stack the player is holding with their mouse
         * @return `true` if the action should be cancelled, `false` otherwise
         */
        @JvmStatic
        fun checkItemDrop(clickModifier: Int, slotNum: Int, heldStack: ItemStack?): Boolean {
            // Is this a left or right click?
            if ((clickModifier == 0 || clickModifier == 1)) {
                // Is the player clicking outside their inventory?
                if (slotNum == -999) {
                    // Is the player holding an item stack with their mouse?
                    if (heldStack != null) {
                        return !SkyblockAddonsPlus.utils!!.getItemDropChecker().canDropItem(heldStack)
                    }
                }
            }

            // The player is not dropping an item. Don't cancel this action.
            return false
        }
        @JvmStatic
        fun onPlayerDestroyBlock(blockPos: BlockPos) {
            val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
            val mc: Minecraft = Minecraft.getMinecraft()

            if (main.utils!!.isOnSkyblock()) {
                val block: IBlockState = mc.theWorld.getBlockState(blockPos)
                // Use vanilla break mechanic to get breaking time
                val perTickIncrease: Double =
                    block.getBlock().getPlayerRelativeBlockHardness(mc.thePlayer, mc.thePlayer.worldObj, blockPos)
                        .toDouble()
                val MILLISECONDS_PER_TICK = 1000 / 20
                MinecraftForge.EVENT_BUS.post(
                    SkyblockBlockBreakEvent(
                        blockPos,
                        (MILLISECONDS_PER_TICK / perTickIncrease).toLong()
                    )
                )
            }
        }
        @JvmStatic
        fun onResetBlockRemoving() {
            MinecraftHook.prevClickBlock = BlockPos(-1, -1, -1)
        }

        /**
         * Cancels clicking a locked inventory slot, even from other mods
         */
        @JvmStatic
        fun onWindowClick(
            slotNum: Int,
            mouseButtonClicked: Int,
            mode: Int,
            player: EntityPlayer,
            returnValue: ReturnValue<ItemStack?>
        ) { // return null
            //if (Minecraft.getMinecraft().thePlayer.openContainer != null) {
            //    SkyblockAddons.getLogger().info("Handling windowclick--slotnum: " + slotNum + " should be locked: " + SkyblockAddonsPlus.configValues!!.getLockedSlots().contains(slotNum) + " mousebutton: " + mouseButtonClicked + " mode: " + mode + " container class: " + player.openContainer.getClass().toString());
            //}

            // Handle blocking the next click, sorry I did it this way

            var slotNum = slotNum
            if (moe.ruruke.skyblock.utils.Utils.blockNextClick) {
                moe.ruruke.skyblock.utils.Utils.blockNextClick = false
                returnValue.cancel()
                return
            }

            val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
            val slotId = slotNum
            val itemStack: ItemStack = player.inventory.getItemStack()

            if (main.utils!!.isOnSkyblock()) {
                // Prevent dropping rare items
                if (main.configValues!!
                        .isEnabled(moe.ruruke.skyblock.core.Feature.STOP_DROPPING_SELLING_RARE_ITEMS) && !main.utils!!
                        .isInDungeon()
                ) {
                    if (checkItemDrop(mode, slotNum, itemStack)) {
                        returnValue.cancel()
                    }
                }

                if (player.openContainer != null) {
                    slotNum += main.inventoryUtils!!.getSlotDifference(player.openContainer)

                    val slots: net.minecraft.inventory.Container = player.openContainer
                    var slotIn = try {
                        slots.getSlot(slotId)
                    } catch (e: java.lang.IndexOutOfBoundsException) {
                        null
                    }

                    if (mouseButtonClicked == 1 && slotIn != null && slotIn.hasStack && slotIn.stack.item === net.minecraft.init.Items.skull) {
                        val color: BackpackColor = ItemUtils.getBackpackColor(slotIn.stack)!!
                        if (color != null) {
                            BackpackInventoryManager.setBackpackColor(color)
                        }
                    }

                    // Prevent clicking on locked slots.
                    if (main.configValues!!.isEnabled(moe.ruruke.skyblock.core.Feature.LOCK_SLOTS)
                        && main.configValues!!.getLockedSlots().contains(slotNum)
                        && (slotNum >= 9 || player.openContainer is ContainerPlayer && slotNum >= 5)
                    ) {
                        if (mouseButtonClicked == 1 && mode == 0 && slotIn != null && slotIn.hasStack && slotIn.stack.item === net.minecraft.init.Items.skull) {
                            var itemID: String = ItemUtils.getSkyblockItemID(slotIn.stack)!!
                            if (itemID == null) itemID = ""

                            // Now that right clicking backpacks is removed, remove this check and block right clicking on backpacks if locked
                            if ( /*ItemUtils.isBuildersWand(slotIn.getStack()) || ItemUtils.isBackpack(slotIn.getStack()) || */itemID.contains(
                                    "SACK"
                                )
                            ) {
                                return
                            }
                        }

                        main.utils!!.playLoudSound("note.bass", 0.5)
                        returnValue.cancel()
                    }

                    // Crafting patterns
                    if (false && slotIn != null && main.inventoryUtils!!.getInventoryType() === InventoryType.CRAFTING_TABLE /*&& main.configValues!!.isEnabled(Feature.CRAFTING_PATTERNS)*/
                    ) {
                        val selectedPattern: CraftingPattern =
                            main.persistentValuesManager!!.getPersistentValues().getSelectedCraftingPattern()
                        val clickedItem: ItemStack? = slotIn.stack
                        if (selectedPattern != CraftingPattern.FREE && clickedItem != null) {
                            val craftingGrid: Array<ItemStack?> = arrayOfNulls<ItemStack>(9)
                            for (i in CraftingPattern.CRAFTING_GRID_SLOTS.indices) {
                                val slotIndex: Int = CraftingPattern.CRAFTING_GRID_SLOTS.get(i)
                                craftingGrid[i] = slots.getSlot(slotIndex).stack
                            }

                            val result: CraftingPatternResult = selectedPattern.checkAgainstGrid(craftingGrid)

                            if (slotIn.inventory == Minecraft.getMinecraft().thePlayer.inventory) {
                                if (result.isFilled && !result.fitsItem(clickedItem) && mode == SHIFTCLICK_CLICK_TYPE) {
                                    // cancel shift-clicking items from the inventory if the pattern is already filled
                                    if (System.currentTimeMillis() > lastCraftingSoundPlayed + CRAFTING_PATTERN_SOUND_COOLDOWN) {
                                        main.utils!!.playSound("note.bass", 0.5)
                                        lastCraftingSoundPlayed = System.currentTimeMillis()
                                    }
                                    returnValue.cancel()
                                }
                            } else {
                                if (slotIn.slotIndex == CraftingPattern.CRAFTING_RESULT_INDEX && !result.isSatisfied
                                    && main.persistentValuesManager!!.getPersistentValues()
                                        .isBlockCraftingIncompletePatterns()
                                ) {
                                    // cancel clicking the result if the pattern isn't satisfied
                                    if (System.currentTimeMillis() > lastCraftingSoundPlayed + CRAFTING_PATTERN_SOUND_COOLDOWN) {
                                        main.utils!!.playSound("note.bass", 0.5)
                                        lastCraftingSoundPlayed = System.currentTimeMillis()
                                    }
                                    returnValue.cancel()
                                }
                            }
                        }
                    }
                }
            } else {
                if (checkItemDrop(mode, slotNum, itemStack)) {
                    returnValue.cancel()
                }
            }
        }
    }
}
