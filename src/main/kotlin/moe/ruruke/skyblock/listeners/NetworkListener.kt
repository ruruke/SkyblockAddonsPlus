package moe.ruruke.skyblock.listeners

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.getLogger
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.getPlayerListener
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.instance
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.newScheduler
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.utils
import moe.ruruke.skyblock.events.SkyblockJoinedEvent
import moe.ruruke.skyblock.events.SkyblockLeftEvent
import moe.ruruke.skyblock.misc.scheduler.ScheduledTask
import moe.ruruke.skyblock.misc.scheduler.SkyblockRunnable
import moe.ruruke.skyblock.utils.data.DataUtils.onSkyblockJoined
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent

class NetworkListener {
    private val main = SkyblockAddonsPlus.instance
    private var updateHealth: ScheduledTask? = null

    @SubscribeEvent
    fun onDisconnect(event: ClientDisconnectionFromServerEvent?) {
        // Leave Skyblock when the player disconnects
        MinecraftForge.EVENT_BUS.post(SkyblockLeftEvent())
    }

    @SubscribeEvent
    fun onSkyblockJoined(event: SkyblockJoinedEvent?) {
        println("aaaa.")
        logger.info("Detected joining skyblock!")
        utils!!.setOnSkyblock(true)
        //TODO:
//        if (main.getConfigValues().isEnabled(Feature.DISCORD_RPC)) {
//            main.getDiscordRPCManager().start();
//        }
        updateHealth = newScheduler!!.scheduleRepeatingTask(object : SkyblockRunnable() {
            override fun run() {
                getPlayerListener().updateLastSecondHealth()
            }
        }, 0, 20)

        onSkyblockJoined()
    }

    @SubscribeEvent
    fun onSkyblockLeft(event: SkyblockLeftEvent?) {
        logger.info("Detected leaving skyblock!")
        utils!!.setOnSkyblock(false)
        utils!!.setProfileName("Unknown")
        //        if (main.getDiscordRPCManager().isActive()) {
//            main.getDiscordRPCManager().stop();
//        }
        if (updateHealth != null) {
            newScheduler!!.cancel(updateHealth!!)
            updateHealth = null
        }
    }

    companion object {
        private val logger = SkyblockAddonsPlus.getLogger()
    }
}