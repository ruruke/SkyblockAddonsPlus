package moe.ruruke.skyblock.asm.hooks


import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.configValues
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.instance
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.utils
import moe.ruruke.skyblock.asm.hooks.utils.ReturnValue
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.Location
import moe.ruruke.skyblock.core.Translations.getMessage
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.Minecraft
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import java.util.*

object MinecraftHook {
    
    private var lastLockedSlotItemChange: Long = -1
    fun getLastLockedSlotItemChange(): Long {
        return lastLockedSlotItemChange
    }

    private val DEEP_CAVERNS_LOCATIONS: Set<Location> = EnumSet.of(
        Location.DEEP_CAVERNS,
        Location.GUNPOWDER_MINES,
        Location.LAPIS_QUARRY,
        Location.PIGMAN_DEN,
        Location.SLIMEHILL,
        Location.DIAMOND_RESERVE,
        Location.OBSIDIAN_SANCTUARY
    )

    private val DWARVEN_MINES_LOCATIONS: Set<Location> = EnumSet.of(
        Location.DWARVEN_MINES,
        Location.THE_LIFT,
        Location.DWARVEN_VILLAGE,
        Location.GATES_TO_THE_MINES,
        Location.THE_FORGE,
        Location.FORGE_BASIN,
        Location.LAVA_SPRINGS,
        Location.PALACE_BRIDGE,
        Location.ROYAL_PALACE,
        Location.ARISTOCRAT_PASSAGE,
        Location.HANGING_TERRACE,
        Location.CLIFFSIDE_VEINS,
        Location.RAMPARTS_QUARRY,
        Location.DIVANS_GATEWAY,
        Location.FAR_RESERVE,
        Location.GOBLIN_BURROWS,
        Location.UPPER_MINES,
        Location.MINERS_GUILD,
        Location.GREAT_ICE_WALL,
        Location.THE_MIST,
        Location.CC_MINECARTS_CO,
        Location.GRAND_LIBRARY,
        Location.HANGING_COURT,
        Location.ROYAL_MINES
    )

    // The room with the puzzle is made of wood that you have to mine
    private val DWARVEN_PUZZLE_ROOM = AxisAlignedBB(171.0, 195.0, 125.0, 192.0, 196.0, 146.0)

    private val DEEP_CAVERNS_MINEABLE_BLOCKS: Set<Block> = HashSet(
        Arrays.asList(
            Blocks.coal_ore, Blocks.iron_ore, Blocks.gold_ore, Blocks.redstone_ore, Blocks.emerald_ore,
            Blocks.diamond_ore, Blocks.diamond_block, Blocks.obsidian, Blocks.lapis_ore, Blocks.lit_redstone_ore
        )
    )

    private val NETHER_MINEABLE_BLOCKS: Set<Block> =
        HashSet(Arrays.asList(Blocks.glowstone, Blocks.quartz_ore, Blocks.nether_wart))

    // TODO: Make this less computationally expensive
    // More specifically, should be cyan hardened clay, grey/light blue wool, dark prismarine, prismarine brick, prismarine, polished diorite
    private val DWARVEN_MINEABLE_BLOCKS: Set<String> = HashSet(
        mutableListOf(
            "minecraft:prismarine0",
            "minecraft:prismarine1", "minecraft:prismarine2", "minecraft:stone4", "minecraft:wool3", "minecraft:wool7",
            "minecraft:stained_hardened_clay9"
        )
    )

    private val PARK_LOCATIONS: Set<Location> = EnumSet.of(
        Location.BIRCH_PARK, Location.SPRUCE_WOODS, Location.SAVANNA_WOODLAND, Location.DARK_THICKET,
        Location.JUNGLE_ISLAND, Location.MELODYS_PLATEAU
    )

    private val LOGS: Set<Block> = HashSet(Arrays.asList(Blocks.log, Blocks.log2))

    private const val lastStemMessage: Long = -1
    private const val lastUnmineableMessage: Long = -1
    var prevClickBlock: BlockPos = BlockPos(-1, -1, -1)
    var startMineTime: Long = Long.MAX_VALUE

    var recentlyClickedBlocks: LinkedHashMap<BlockPos, Long> = LinkedHashMap()

    fun rightClickMouse(returnValue: ReturnValue<*>) {
        val main = instance
        if (utils!!.isOnSkyblock()) {
            val mc = Minecraft.getMinecraft()
            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                val entityIn = mc.objectMouseOver.entityHit

                if (configValues!!.isEnabled(Feature.LOCK_SLOTS) && entityIn is EntityItemFrame && entityIn.displayedItem == null) {
                    val slot = mc.thePlayer.inventory.currentItem + 36
                    if (configValues!!.getLockedSlots().contains(slot) && slot >= 9) {
                        utils!!.playLoudSound("note.bass", 0.5)
                        utils!!.sendMessage(
                            configValues!!.getRestrictedColor(Feature.DROP_CONFIRMATION)
                                .toString() + getMessage("messages.slotLocked")
                        )
                        returnValue.cancel()
                    }
                }
            }
        }
    }

    fun updatedCurrentItem() {
        val mc = Minecraft.getMinecraft()
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance

        if (main.configValues!!.isEnabled(Feature.LOCK_SLOTS) && (main.utils!!.isOnSkyblock() || main.getPlayerListener().aboutToJoinSkyblockServer())
        ) {
            val slot = mc.thePlayer.inventory.currentItem + 36
            if (main.configValues!!.isEnabled(Feature.LOCK_SLOTS) && main.configValues!!.getLockedSlots()
                    .contains(slot)
                && (slot >= 9 || mc.thePlayer.openContainer is ContainerPlayer && slot >= 5)
            ) {
                lastLockedSlotItemChange = System.currentTimeMillis()
            }

            val heldItemStack = mc.thePlayer.heldItem
            if (heldItemStack != null && main.configValues!!.isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) && !main.utils!!.isInDungeon()
                && !main.utils!!.getItemDropChecker().canDropItem(heldItemStack, true, false)
            ) {
                lastLockedSlotItemChange = System.currentTimeMillis()
            }
        }
    }

    fun onClickMouse(returnValue: ReturnValue<*>) {
        val mc = Minecraft.getMinecraft()
        if (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return
        }
        val blockPos = mc.objectMouseOver.blockPos
        if (mc.theWorld.getBlockState(blockPos).block.material === Material.air) {
            return
        }


        if (!returnValue.isCancelled && prevClickBlock != blockPos) {
            startMineTime = System.currentTimeMillis()
        }
        prevClickBlock = blockPos
        if (!returnValue.isCancelled) {
            recentlyClickedBlocks[blockPos] = System.currentTimeMillis()
        }
    }

    fun onSendClickBlockToController(leftClick: Boolean, returnValue: ReturnValue<*>) {
        // If we aren't trying to break anything, don't change vanilla behavior (was causing false positive chat messages)
        if (!leftClick) {
            return
        }
        onClickMouse(returnValue)
        // Canceling this is tricky. Not only do we have to reset block removing, but also reset the position we are breaking
        // This is because we want playerController.onClick to be called when they go back to that block
        // It's also important to resetBlockRemoving before changing current block, since then we'd be sending the server inaccurate info that could trigger wdr
        // This mirrors PlayerControllerMP.clickBlock(), which sends an ABORT_DESTROY message, before calling onPlayerDestroyBlock, which changes "currentBlock"
        if (returnValue.isCancelled) {
            Minecraft.getMinecraft().playerController.resetBlockRemoving()
            Minecraft.getMinecraft().playerController.currentBlock = BlockPos(-1, -1, -1)
        }
    }
}
