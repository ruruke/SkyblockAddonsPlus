package moe.ruruke.skyblock.core.npc

import moe.ruruke.skyblock.utils.ItemUtils
import moe.ruruke.skyblock.utils.TextUtils
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Blocks
import net.minecraft.inventory.IInventory
import net.minecraft.item.Item
import net.minecraft.util.Vec3
import java.util.*
import kotlin.collections.HashMap

/**
 * This is a set of utility methods relating to Skyblock NPCs
 *
 * @author Biscuit
 * @author ILikePlayingGames
 * @version 2.0
 */
object NPCUtils {
    private val HIDE_RADIUS_SQUARED = Math.round(2.5 * 2.5).toInt()

    private val npcLocations: HashMap<UUID, Vec3> = HashMap()
    fun getNpcLocations(): HashMap<UUID, Vec3> {
        return npcLocations
    }

    /**
     * Checks if the NPC is a merchant with both buying and selling capabilities
     *
     * @param inventory The inventory to check
     * @return `true` if the NPC is a merchant with buying and selling capabilities, `false` otherwise
     */
    fun isSellMerchant(inventory: IInventory): Boolean {
        //TODO Fix for Hypixel localization
        val sellSlot = inventory.sizeInventory - 4 - 1
        val itemStack = inventory.getStackInSlot(sellSlot)

        if (itemStack != null) {
            if (itemStack.item === Item.getItemFromBlock(Blocks.hopper) && itemStack.hasDisplayName() &&
                TextUtils.stripColor(itemStack.displayName).equals("Sell Item")
            ) {
                return true
            }

            val tooltip = ItemUtils.getItemLore(itemStack)
            for (line in tooltip) {
                if (TextUtils.stripColor(line).equals("Click to buyback!")) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Checks if a given entity is near any NPC.
     *
     * @param entityToCheck The entity to check
     * @return `true` if the entity is near an NPC, `false` otherwise
     */
    fun isNearNPC(entityToCheck: Entity): Boolean {
        for (npcLocation in npcLocations.values) {
            if (entityToCheck.getDistanceSq(
                    npcLocation.xCoord,
                    npcLocation.yCoord,
                    npcLocation.zCoord
                ) <= HIDE_RADIUS_SQUARED
            ) {
                return true
            }
        }

        return false
    }

    /**
     * Checks if the given entity is an NPC
     *
     * @param entity the entity to check
     * @return `true` if the entity is an NPC, `false` otherwise
     */
    fun isNPC(entity: Entity): Boolean {
        if (entity !is EntityOtherPlayerMP) {
            return false
        }

        val entityLivingBase = entity as EntityLivingBase

        return entity.getUniqueID()
            .version() == 2 && entityLivingBase.health == 20.0f && !entityLivingBase.isPlayerSleeping
    }
}
