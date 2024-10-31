package moe.ruruke.skyblock.misc.scheduler

import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.util.function.Consumer
import kotlin.concurrent.Volatile

class NewScheduler {
    private val queuedTasks: MutableList<ScheduledTask> = ArrayList()
    private val pendingTasks: MutableList<ScheduledTask> = ArrayList()
    private val anchor = Any()

    @get:Synchronized
    @Volatile
    var totalTicks: Long = 0
        private set

    @SubscribeEvent
    fun ticker(event: ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START) {
            synchronized(this.anchor) {
                totalTicks++
            }

            if (Minecraft.getMinecraft() != null) {
                pendingTasks.removeIf { obj: ScheduledTask -> obj.isCanceled() }

                pendingTasks.addAll(queuedTasks)
                queuedTasks.clear()

                try {
                    for (scheduledTask in this.pendingTasks) {
                        if (this.totalTicks >= (scheduledTask.addedTicks + scheduledTask.getDelay())) {
                            scheduledTask.start()

                            if (scheduledTask.isRepeating) {
                                if (!scheduledTask.isCanceled()) {
                                    scheduledTask.setDelay(scheduledTask.period)
                                }
                            } else {
                                scheduledTask.cancel()
                            }
                        }
                    }
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                }
            }
        }
    }

    @Synchronized
    fun cancel(id: Int) {
        pendingTasks.forEach(Consumer { scheduledTask: ScheduledTask ->
            if (scheduledTask.id == id) scheduledTask.cancel()
        })
    }

    fun cancel(task: ScheduledTask) {
        task.cancel()
    }

    /**
     * Repeats a task (synchronously) every tick.<br></br><br></br>
     *
     * Warning: This method is run on the main thread, don't do anything heavy.
     * @param task The task to run.
     * @return The scheduled task.
     */
    fun repeat(task: SkyblockRunnable): ScheduledTask {
        return this.scheduleRepeatingTask(task, 0, 1)
    }

    /**
     * Repeats a task (asynchronously) every tick.
     *
     * @param task The task to run.
     * @return The scheduled task.
     */
    fun repeatAsync(task: SkyblockRunnable): ScheduledTask {
        return this.runAsync(task, 0, 1)
    }

    /**
     * Runs a task (asynchronously) on the next tick.
     *
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is ran.
     * @return The scheduled task.
     */
    /**
     * Runs a task (asynchronously) on the next tick.
     *
     * @param task The task to run.
     * @return The scheduled task.
     */
    @JvmOverloads
    fun runAsync(task: SkyblockRunnable, delay: Int = 0): ScheduledTask {
        return this.runAsync(task, delay, 0)
    }

    /**
     * Runs a task (asynchronously) on the next tick.
     *
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is ran.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @return The scheduled task.
     */
    fun runAsync(task: SkyblockRunnable, delay: Int, period: Int): ScheduledTask {
        val scheduledTask = ScheduledTask(task, delay, period, true)
        pendingTasks.add(scheduledTask)
        return scheduledTask
    }

    /**
     * Runs a task (synchronously) on the next tick.
     *
     * @param task The task to run.
     * @return The scheduled task.
     */
    fun scheduleTask(task: SkyblockRunnable): ScheduledTask {
        return this.scheduleDelayedTask(task, 0)
    }

    /**
     * Runs a task (synchronously) on the next tick.
     *
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is ran.
     * @return The scheduled task.
     */
    fun scheduleDelayedTask(task: SkyblockRunnable, delay: Int): ScheduledTask {
        return this.scheduleRepeatingTask(task, delay, 0)
    }

    /**
     * Runs a task (synchronously) on the next tick.
     *
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is ran.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @return The scheduled task.
     */
    fun scheduleRepeatingTask(task: SkyblockRunnable, delay: Int, period: Int): ScheduledTask {
        return this.scheduleRepeatingTask(task, delay, period, false)
    }

    /**
     * Runs a task (synchronously) on the next tick.
     *
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is run.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @param queued Whether to queue this task to be run next loop; to be used for scheduling tasks directly from a
     * synchronous task.
     * @return The scheduled task.
     */
    fun scheduleRepeatingTask(task: SkyblockRunnable, delay: Int, period: Int, queued: Boolean): ScheduledTask {
        val scheduledTask = ScheduledTask(task, delay, period, false)
        if (queued) {
            queuedTasks.add(scheduledTask)
        } else {
            pendingTasks.add(scheduledTask)
        }
        return scheduledTask
    }

    /**
     * Runs a task (synchronously) on the next tick.
     *
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is ran.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @param runLimit The maximum number of times the task should be run.
     * @return The scheduled task.
     */
    fun scheduleLimitedRepeatingTask(
        task: SkyblockRunnable,
        delay: Int,
        period: Int,
        runLimit: Int
    ): LimitedRepeatingScheduledTask {
        return this.scheduleLimitedRepeatingTask(task, delay, period, runLimit, false)
    }

    /**
     * Runs a task (synchronously) on the next tick.
     *
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is run.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @param runLimit The maximum number of times the task should be run.
     * @param queued Whether to queue this task to be run next loop; to be used for scheduling tasks directly from a
     * synchronous task.
     * @return The scheduled task.
     */
    fun scheduleLimitedRepeatingTask(
        task: SkyblockRunnable,
        delay: Int,
        period: Int,
        runLimit: Int,
        queued: Boolean
    ): LimitedRepeatingScheduledTask {
        val limitedRepeatingScheduledTask = LimitedRepeatingScheduledTask(task, delay, period, false, runLimit)
        if (queued) {
            queuedTasks.add(limitedRepeatingScheduledTask)
        } else {
            pendingTasks.add(limitedRepeatingScheduledTask)
        }
        return limitedRepeatingScheduledTask
    }

    /**
     * Runs a task  on the next tick.
     *
     * @param scheduledTask The ScheduledTask to run.
     */
    fun schedule(scheduledTask: ScheduledTask) {
        pendingTasks.add(scheduledTask)
    }

    companion object {
        /**
         * Causes the currently executing thread to sleep (temporarily cease execution) for the specified number of milliseconds, subject to the precision and accuracy of system timers and schedulers. The thread does not lose ownership of any monitors.
         *
         * @param millis the length of time to sleep in milliseconds
         */
        fun sleep(millis: Long) {
            try {
                Thread.sleep(millis)
            } catch (ignored: InterruptedException) {
            }
        }
    }
}
