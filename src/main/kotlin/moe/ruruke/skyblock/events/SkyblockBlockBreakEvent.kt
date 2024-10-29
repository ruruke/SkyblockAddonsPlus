package moe.ruruke.skyblock.events

import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * As of the Dwarven Mines update, all block-breaking on public islands has been migrated to server-side control.
 * Whereas vanilla Minecraft uses [net.minecraft.client.multiplayer.PlayerControllerMP.curBlockDamageMP] to track
 * block-breaking progress, and then fires [net.minecraft.client.multiplayer.PlayerControllerMP.onPlayerDestroyBlock]
 * when [net.minecraft.client.multiplayer.PlayerControllerMP.curBlockDamageMP] > 1F, this no longer fires in public islands
 */
class SkyblockBlockBreakEvent @JvmOverloads constructor(var blockPos: BlockPos, var timeToBreak: Long = 0) : Event()
