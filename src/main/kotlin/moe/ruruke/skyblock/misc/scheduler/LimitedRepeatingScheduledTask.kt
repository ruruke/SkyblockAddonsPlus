package moe.ruruke.skyblock.misc.scheduler

/**
 * This is a repeating scheduled task that runs a maximum of `RUN_LIMIT` times.
 */
class LimitedRepeatingScheduledTask : ScheduledTask {
    private val RUN_LIMIT: Int

    private var runCount: Int

    /**
     * Creates a new Limited Repeating Scheduled Task.
     * This task is a repeating task that runs for a maximum of `runLimit` times.
     *
     * @param delay The delay (in ticks) to wait before the task is run.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @param async If the task should be run asynchronously.
     * @param runLimit the maximum number of times this task should be run.
     */
    constructor(delay: Int, period: Int, async: Boolean, runLimit: Int) : super(delay, period, async) {
        runCount = 0
        this.RUN_LIMIT = runLimit
    }

    /**
     * Creates a new Limited Repeating Scheduled Task.
     * This task is a repeating task that runs for a maximum of `runLimit` times.
     *
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is run.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @param async If the task should be run asynchronously.
     * @param runLimit the maximum number of times this task should be run.
     */
    constructor(task: SkyblockRunnable, delay: Int, period: Int, async: Boolean, runLimit: Int) : super(
        task,
        delay,
        period,
        async
    ) {
        runCount = 0
        this.RUN_LIMIT = runLimit
    }

    /**
     * Starts the task. The run count is incremented every time the task runs until `RUN_LIMIT` is reached,
     * at which point the task is cancelled.
     */
    override fun start() {
        if (runCount < RUN_LIMIT) {
            runCount++
            super.start()
        } else {
            cancel()
        }
    }
}
