package moe.ruruke.skyblock.misc.scheduler

import moe.ruruke.skyblock.SkyblockAddonsPlus
import kotlin.concurrent.Volatile

open class ScheduledTask {
    /**
     * Returns the added time for the task.
     *
     * @return When the task was added.
     */
    val addedTime: Long = System.currentTimeMillis()

    /**
     * Returns the added ticks for the task.
     *
     * @return Ticks when the task was added.
     */
    var addedTicks: Long = SkyblockAddonsPlus.instance.newScheduler!!.totalTicks
        private set

    /**
     * Returns the id for the task.
     *
     * @return Task id number.
     */
    var id: Int = 0
    private var delay: Int

    /**
     * Returns the delay (in ticks) for the task to repeat itself.
     *
     * @return How long until the task repeats itself.
     */
    val period: Int

    /**
     * Gets if the current task is an asynchronous task.
     *
     * @return True if the task is not run by main thread.
     */
    val isAsync: Boolean

    /**
     * Gets if the current task is running.
     *
     * @return True if the task is running.
     */
    var isRunning: Boolean = false
        private set

    /**
     * Gets if the current task is canceled.
     *
     * @return True if the task is canceled.
     */
    var isCanceled: Boolean = false
        private set

    /**
     * Gets if the current task is a repeating task.
     *
     * @return True if the task is a repeating task.
     */
    var isRepeating: Boolean
        private set
    private var task: Runnable? = null

    /**
     * Creates a new Scheduled Task.
     *
     * @param delay The delay (in ticks) to wait before the task is run.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @param async If the task should be run asynchronously.
     */
    constructor(delay: Int, period: Int, async: Boolean) {
        synchronized(anchor) {
            this.id = currentId++
        }

        this.delay = delay
        this.period = period
        this.isAsync = async
        this.isRepeating = this.period > 0
    }

    /**
     * Creates a new Scheduled Task.
     *
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is ran.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @param async If the task should be run asynchronously.
     */
    constructor(task: SkyblockRunnable, delay: Int, period: Int, async: Boolean) {
        synchronized(anchor) {
            this.id = currentId++
        }

        this.delay = delay
        this.period = period
        this.isAsync = async
        this.isRepeating = this.period > 0

        task.setThisTask(this)

        this.task = Runnable {
            this.isRunning = true
            task.run()
            this.isRunning = false
        }
    }

    /**
     * Will attempt to cancel this task if running.
     */
    fun cancel() {
        this.isRepeating = false
        this.isRunning = false
        this.isCanceled = true
    }

    /**
     * Returns the delay (in ticks) for the task.
     *
     * @return How long the task will wait to run.
     */
    fun getDelay(): Int {
        return this.delay
    }

    fun setDelay(delay: Int) {
        this.addedTicks = SkyblockAddonsPlus.instance.newScheduler!!.totalTicks
        this.delay = delay
    }

    /**
     * Starts the task.
     */
    open fun start() {
        if (this.isAsync) {
            SkyblockAddonsPlus.runAsync(this.task)
        } else {
            task!!.run()
        }
    }

    fun setTask(task: SkyblockRunnable) {
        this.task = Runnable {
            this.isRunning = true
            task.run()
            this.isRunning = false
        }
    }

    companion object {
        @Volatile
        private var currentId = 1
        private val anchor = Any()
    }
}
