package moe.ruruke.skyblock.core

import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.world.World
import java.util.*

/**
 * An abstraction of an "entity" that may be composed of several entity parts.
 * Examples:
 * - A jerry present (3 armor stands)
 * - A zealot (enderman and name tag armor stand)
 */
class EntityAggregate {
    private val entities: List<UUID> = listOf()

    val isDead: Boolean
        /**
         * The aggregate entity is dead when all of its components are dead
         */
        get() {
            val world: World = Minecraft.getMinecraft().theWorld ?: return true

            for (uuid in entities) {
                val entity = getEntityFromUUID(uuid)
                if (entity != null && !entity.isDead) {
                    return false
                }
            }
            return true
        }

    private fun getEntityFromUUID(uuid: UUID): Entity? {
        for (entity in Minecraft.getMinecraft().theWorld.getLoadedEntityList()) {
            if (entity.uniqueID == uuid) {
                return entity
            }
        }
        return null
    }
}
