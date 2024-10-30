package moe.ruruke.skyblock.features

import com.google.common.util.concurrent.ThreadFactoryBuilder
import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.Location
import net.minecraft.client.Minecraft
import net.minecraft.entity.monster.EntityIronGolem
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import org.apache.logging.log4j.Logger
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object EndstoneProtectorManager {
    private val mc: Minecraft = Minecraft.getMinecraft()
    private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
    private val logger: Logger = main.getLogger()

    private var canDetectSkull = false
    fun isCanDetectSkull(): Boolean {
        return canDetectSkull
    }

    private var minibossStage: Stage? = null
    fun getMinibossStage(): Stage? {
        return minibossStage
    }

    private var zealotCount = 0
    fun getZealotCount(): Int {
        return zealotCount
    }

    private var lastWaveStart: Long = -1

    fun checkGolemStatus() {
        if (mc.theWorld != null && (main.utils!!.getLocation() === Location.THE_END || main.utils!!.getLocation() === Location.DRAGONS_NEST) &&
            main.configValues!!.isEnabled(Feature.ENDSTONE_PROTECTOR_DISPLAY)
        ) {
            val world: World = mc.theWorld

            val chunk: Chunk = world.getChunkFromBlockCoords(BlockPos(-689, 5, -273)) // This is the original spawn.
            if (chunk == null || !chunk.isLoaded) {
                canDetectSkull = false
                return
            }

            var newStage = Stage.detectStage()
            for (entity in world.loadedEntityList) {
                if (entity is EntityIronGolem) {
                    newStage = Stage.GOLEM_ALIVE
                    break
                }
            }

            canDetectSkull = true
            if (minibossStage != newStage) {
                val timeTaken = (System.currentTimeMillis() - lastWaveStart).toInt()
                val previousStage = (if (minibossStage == null) "null" else minibossStage!!.name)

                var zealotsKilled = "N/A"
                if (minibossStage != null) {
                    zealotsKilled = zealotCount.toString()
                }

                val totalSeconds = timeTaken / 1000
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60

                logger.info(
                    "Endstone protector stage updated from " + previousStage + " to " + newStage!!.name + ". " +
                            "Your zealot kill count was " + zealotsKilled + ". This took " + minutes + "m " + seconds + "s."
                )

                if (minibossStage == Stage.GOLEM_ALIVE && newStage == Stage.NO_HEAD) {
                    zealotCount = 0
                }

                minibossStage = newStage
                lastWaveStart = System.currentTimeMillis()
            }
        } else {
            canDetectSkull = false
        }
    }

    fun onKill() {
        zealotCount++
    }

    fun reset() {
        minibossStage = null
        zealotCount = 0
        canDetectSkull = false
    }

    enum class Stage(private val blocksUp: Int) {
        NO_HEAD(-1),
        STAGE_1(0),
        STAGE_2(1),
        STAGE_3(2),
        STAGE_4(3),
        STAGE_5(4),
        GOLEM_ALIVE(-1);

        companion object {
            private var lastStage: Stage? = null
            private var lastPos: BlockPos? = null

            private val EXECUTOR: ExecutorService = Executors.newSingleThreadExecutor(
                ThreadFactoryBuilder().setNameFormat(SkyblockAddonsPlus.NAME + " - Endstone Protector #%d").build()
            )

            fun detectStage(): Stage? {
                EXECUTOR.submit {
                    try {
                        val world: World = Minecraft.getMinecraft().theWorld

                        if (lastStage != null && lastPos != null) {
                            if (Blocks.skull === world.getBlockState(lastPos)
                                    .getBlock()
                            ) {
                                return@submit
                            }
                        }

                        for (stage in entries) {
                            if (stage.blocksUp != -1) {
                                // These 4 coordinates are the bounds of the dragon's nest.
                                for (x in -749 until -602) {
                                    for (z in -353 until -202) {
                                        val blockPos: BlockPos = BlockPos(x, 5 + stage.blocksUp, z)
                                        if (Blocks.skull == world.getBlockState(blockPos).getBlock()) {
                                            lastStage =
                                                stage
                                            lastPos =
                                                blockPos
                                            return@submit
                                        }
                                    }
                                }
                            }
                        }
                        lastStage =
                            NO_HEAD
                        lastPos = null
                    } catch (ex: Throwable) {
                        ex.printStackTrace()
                        // It's fine I guess, just try checking next tick...
                    }
                }

                return lastStage
            }
        }
    }
}
