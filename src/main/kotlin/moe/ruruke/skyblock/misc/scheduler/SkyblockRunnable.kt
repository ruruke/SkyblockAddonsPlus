package moe.ruruke.skyblock.misc.scheduler

import moe.ruruke.skyblock.SkyblockAddonsPlus

abstract class SkyblockRunnable : Runnable {
    private var thisTask: ScheduledTask? = null

    fun setThisTask(task: ScheduledTask) {
        thisTask = task
    }

    fun cancel() {
        SkyblockAddonsPlus.instance.newScheduler!!.cancel(thisTask!!)
    }
}
