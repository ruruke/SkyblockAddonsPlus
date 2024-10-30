package moe.ruruke.skyblock.misc.scheduler

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.apache.commons.lang3.mutable.MutableInt
import java.util.*

class Scheduler {
    private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
    private var totalTicks: Long = 0
    private val queue: MutableMap<Long, MutableSet<Command>> = HashMap()

    /**
     * This class is a little something I came up with in order to schedule things
     * by client ticks reliably.
     *
     * @param commandType What you want to schedule
     * @param delaySeconds The delay in seconds (must be greater than 0)
     */
    fun schedule(commandType: CommandType, delaySeconds: Int, vararg data: Any?) {
        // If the delay isn't greater than zero, the command never gets executed.
        require(delaySeconds > 0) { "Delay must be greater than zero!" }

        val ticks = totalTicks + (delaySeconds * 20L)
        val commands = queue[ticks]
        if (commands != null) {
            for (command in commands) {
                if (command.commandType === commandType) {
                    command.addCount(*arrayOf(data))
                    return
                }
            }
            commands.add(Command(commandType, *arrayOf(data)))
        } else {
            val commandSet: MutableSet<Command> = HashSet()
            commandSet.add(Command(commandType, *arrayOf(data)))
            queue[ticks] = commandSet
        }
    }

    /**
     * Removes all queued full inventory warnings.
     */
    fun removeQueuedFullInventoryWarnings() {
        val queueIterator: Iterator<Map.Entry<Long, MutableSet<Command>>> = queue.entries.iterator()
        val resetTitleFeatureTicks: MutableList<Long> = LinkedList()

        while (queueIterator.hasNext()) {
            val entry = queueIterator.next()

            if (entry.value.removeIf { command: Command -> CommandType.SHOW_FULL_INVENTORY_WARNING == command.commandType }) {
                resetTitleFeatureTicks.add(entry.key + main.configValues!!.getWarningSeconds() * 20L)
            }

            // Remove the corresponding reset title feature command.
            if (resetTitleFeatureTicks.contains(entry.key)) {
                val commands = entry.value
                val commandIterator = commands.iterator()

                while (commandIterator.hasNext()) {
                    val command = commandIterator.next()
                    if (command.commandType == CommandType.RESET_TITLE_FEATURE) {
                        commandIterator.remove()
                        break
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun ticker(e: TickEvent.ClientTickEvent) {
        if (e.phase == TickEvent.Phase.START) {
            totalTicks++
            val commands: Set<Command>? = queue[totalTicks]
            if (commands != null) {
                for (command in commands) {
                    for (times in 0 until command.getCount().getValue()) {
                        command.commandType.execute()
                    }
                }
                queue.remove(totalTicks)
            }
        }
    }

    private class Command(val commandType: CommandType, vararg data: Any) {
        private val count = MutableInt(1)
        private val countData: MutableMap<Int, Array<Any>> = HashMap()

        init {
            if (data.size > 0) {
                countData[1] = arrayOf(data)
            }
        }

        fun getCount(): MutableInt {
            return count
        }

        fun addCount(vararg data: Any) {
            count.increment()
            if (data.size > 0) {
                countData[count.value] = arrayOf(data)
            }
        }

        fun getData(count: Int): Array<Any> {
            return countData[count]!!
        }
    }

    enum class CommandType {
        RESET_TITLE_FEATURE,
        RESET_SUBTITLE_FEATURE,
        ERASE_UPDATE_MESSAGE,
        SHOW_FULL_INVENTORY_WARNING,
        CHECK_FOR_UPDATE;

        fun execute() {
            val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
            when {
                this == SHOW_FULL_INVENTORY_WARNING -> {
                    val mc: Minecraft = Minecraft.getMinecraft()
                    if (mc.theWorld == null || mc.thePlayer == null || !main.utils!!.isOnSkyblock()) {
                        return
                    }

                    main.inventoryUtils!!.showFullInventoryWarning()

                    // Schedule a repeat if needed.
                    if (main.configValues!!.isEnabled(Feature.REPEAT_FULL_INVENTORY_WARNING)) {
                        main.scheduler!!.schedule(SHOW_FULL_INVENTORY_WARNING, 10)
                        main.scheduler!!.schedule(RESET_TITLE_FEATURE, 10 + main.configValues!!.getWarningSeconds())
                    }
                }
                this == RESET_TITLE_FEATURE -> {
                    main.renderListener!!.setTitleFeature(null)
                }
                this == RESET_SUBTITLE_FEATURE -> {
                    main.renderListener!!.setSubtitleFeature(null)
                }
                this == ERASE_UPDATE_MESSAGE -> {
                    main.renderListener!!.setUpdateMessageDisplayed(true)
                }
            }
            //TODO
//            else if (this == CHECK_FOR_UPDATE) {
//                main.getUpdater().checkForUpdate()
//            }
        }
    }
}
