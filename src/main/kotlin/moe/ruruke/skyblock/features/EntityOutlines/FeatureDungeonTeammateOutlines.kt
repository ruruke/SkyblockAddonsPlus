package moe.ruruke.skyblock.features.EntityOutlines

import moe.ruruke.skyblock.config.NewConfig
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.events.RenderEntityOutlineEvent
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.FontRenderer
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.scoreboard.Team.EnumVisible
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.function.Function

/**
 * Controls the behavior of [codes.biscuit.skyblockaddons.core.Feature.MAKE_DUNGEON_TEAMMATES_GLOW]
 */
class FeatureDungeonTeammateOutlines {
    /**
     * Queues items to be outlined that satisfy global- and entity-level predicates.
     *
     * @param e the outline event
     */
    @SubscribeEvent
    fun onRenderEntityOutlines(e: RenderEntityOutlineEvent) {
        if (e.getType() === RenderEntityOutlineEvent.Type.XRAY) {
            // Test whether we should add any entities at all
            if (GLOBAL_TEST()) {
                // Queue specific items for outlining
                e.queueEntitiesToOutline(OUTLINE_COLOR)
            }
        }
    }

    companion object {
        /**
         * Entity-level predicate to determine whether a specific entity should be outlined, and if so, what color.
         * Should be used in conjunction with the global-level predicate, [.GLOBAL_TEST].
         *
         *
         * Return `null` if the entity should not be outlined, or the integer color of the entity to be outlined iff the entity should be outlined
         */
        private val OUTLINE_COLOR: Function<Entity, Int?> =
            Function { e: Entity? ->
                // Only accept other player entities
                if (e is EntityOtherPlayerMP) {
                    val scoreplayerteam = (e as EntityPlayer).team as ScorePlayerTeam
                    // Must be visible on the scoreboard
                    if (scoreplayerteam != null && scoreplayerteam.nameTagVisibility != EnumVisible.NEVER) {
                        val formattedName = FontRenderer.getFormatFromString(scoreplayerteam.colorPrefix)
                        // Return the color of the corresponding team the player is on
                        if (formattedName.length >= 2) {
                            return@Function Minecraft.getMinecraft().fontRendererObj.getColorCode(formattedName[1])
                        }
                    }
                    // NPCs don't have a color on their team. Don't show them on outlines.
                    return@Function null
                }
                null
            }

        /**
         * Global-level predicate to determine whether any entities should outlined.
         * Should be used in conjunction with the entity-level predicate, [.OUTLINE_COLOR].
         *
         * @return `false` iff no entities should be outlined (i.e., accept if the player is in a dungeon)
         */
        private fun GLOBAL_TEST(): Boolean {
//            return false
            return NewConfig.isEnabled(Feature.MAKE_DUNGEON_TEAMMATES_GLOW)
//                    &&
//                    SkyblockAddonsPlus.configValues.isInDungeon()
        }
    }
}
