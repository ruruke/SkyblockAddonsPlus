package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.npc.NPCUtils
import moe.ruruke.skyblock.events.SkyblockBlockBreakEvent
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraftforge.common.MinecraftForge


class WorldClientHook {
    companion object {
        @JvmStatic
        fun onEntityRemoved(entityIn: net.minecraft.entity.Entity) {
            NPCUtils.getNpcLocations().remove(entityIn.uniqueID)
        }

        @JvmStatic
        fun blockUpdated(pos: BlockPos, state: IBlockState) {
            val mc: Minecraft = Minecraft.getMinecraft()
            if (mc.thePlayer != null) {
                val BEDROCK_STATE = net.minecraft.block.Block.getStateId(Blocks.bedrock.getDefaultState())
                val AIR_STATE = net.minecraft.block.Block.getStateId(Blocks.air.getDefaultState())
                val stateBefore = net.minecraft.block.Block.getStateId(mc.theWorld.getBlockState(pos))
                val itr: MutableIterator<Map.Entry<BlockPos, Long>> = MinecraftHook.recentlyClickedBlocks.entries.iterator()
                val currTime = System.currentTimeMillis()
                while (itr.hasNext()) {
                    val entry: Map.Entry<BlockPos, Long> = itr.next()
                    if (currTime - entry.value < 300) {
                        break
                    }
                    itr.remove()
                }
                // Fire event if the client is breaking a block that is not being broken by another player, and the block is changing
                // One infrequent bug is if client mining stone and it turns into ore randomly. This will trigger this method currently
                if ( /*mc.playerController.getIsHittingBlock() && */MinecraftHook.recentlyClickedBlocks.containsKey(pos) && stateBefore != net.minecraft.block.Block.getStateId(
                        state
                    ) && stateBefore != BEDROCK_STATE && stateBefore != AIR_STATE
                ) {
                    // Get the player's ID (0 on public islands and the player's entity ID on private islands)
                    val location: moe.ruruke.skyblock.core.Location = SkyblockAddonsPlus.utils!!.getLocation()
                    // Blocks broken on guest islands don't count
                    if (location == moe.ruruke.skyblock.core.Location.GUEST_ISLAND || location == moe.ruruke.skyblock.core.Location.ISLAND) {
                        return
                    }
                    val playerID =
                        /*location == Location.ISLAND || location == Location.GUEST_ISLAND ? mc.thePlayer.getEntityId() :*/0
                    // Don't fire if anyone else is mining the same block...
                    // This will undercount your blocks if you broke the block before the other person
                    // But the alternative is to overcount your blocks if someone else breaks the block before you...not much better
                    // Also could mathematically determine a probability based on pos, yaw, pitch of other entities...worth it? ehh...
                    var noOneElseMining = true
                    for ((key, value) in mc.renderGlobal.damagedBlocks.entries) {
                        if (key != playerID && value.getPosition() == pos) {
                            noOneElseMining = false
                        }
                    }
                    if (noOneElseMining) {
                        val mineTime = kotlin.math.max(
                            (System.currentTimeMillis() - MinecraftHook.startMineTime).toDouble(),
                            0.0
                        ).toLong()
                        MinecraftForge.EVENT_BUS.post(SkyblockBlockBreakEvent(pos, mineTime))
                    }
                }
            }
        }
    }
}
