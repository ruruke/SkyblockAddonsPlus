package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.asm.hooks.utils.ReturnValue
import moe.ruruke.skyblock.core.npc.NPCUtils
import moe.ruruke.skyblock.features.JerryPresent
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.particle.EntityFX
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.BlockPos

object RenderManagerHook {
    private const val HIDE_RADIUS_SQUARED = 7 * 7

    fun shouldRender(entityIn: net.minecraft.entity.Entity, returnValue: ReturnValue<Boolean?>) {
        val mc: Minecraft = Minecraft.getMinecraft()
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance

        if (main.utils!!.isOnSkyblock()) {
            val currentLocation: moe.ruruke.skyblock.core.Location = main.utils!!.getLocation()

            if (main.configValues!!
                    .isEnabled(moe.ruruke.skyblock.core.Feature.HIDE_BONES) && main.inventoryUtils!!
                    .isWearingSkeletonHelmet()
            ) {
                if (entityIn is EntityItem && entityIn.ridingEntity is EntityArmorStand && entityIn.ridingEntity.isInvisible()) {
                    val entityItem: EntityItem = entityIn as EntityItem
                    if (entityItem.getEntityItem().getItem() == net.minecraft.init.Items.bone) {
                        returnValue.cancel()
                    }
                }
            }
            if (mc.theWorld != null && main.configValues!!
                    .isEnabled(moe.ruruke.skyblock.core.Feature.HIDE_PLAYERS_NEAR_NPCS) && currentLocation != moe.ruruke.skyblock.core.Location.GUEST_ISLAND && currentLocation != moe.ruruke.skyblock.core.Location.THE_CATACOMBS
            ) {
                if (entityIn is EntityOtherPlayerMP && !NPCUtils.isNPC(entityIn) && NPCUtils.isNearNPC(entityIn)) {
                    returnValue.cancel()
                }
            }
            if (main.configValues!!.isEnabled(moe.ruruke.skyblock.core.Feature.HIDE_SPAWN_POINT_PLAYERS)) {
                val entityPosition: BlockPos = entityIn.position
                if (entityIn is EntityPlayer && entityPosition.getX() == -2 && entityPosition.getY() == 70 && entityPosition.getZ() == -69 && currentLocation == moe.ruruke.skyblock.core.Location.VILLAGE) {
                    returnValue.cancel()
                }
            }
            if (main.configValues!!.isEnabled(moe.ruruke.skyblock.core.Feature.HIDE_PLAYERS_IN_LOBBY)) {
                if (currentLocation == moe.ruruke.skyblock.core.Location.VILLAGE || currentLocation == moe.ruruke.skyblock.core.Location.AUCTION_HOUSE || currentLocation == moe.ruruke.skyblock.core.Location.BANK) {
                    if ((entityIn is EntityOtherPlayerMP || entityIn is EntityFX || entityIn is EntityItemFrame) &&
                        !NPCUtils.isNPC(entityIn) && entityIn.getDistanceSqToEntity(mc.thePlayer) > HIDE_RADIUS_SQUARED
                    ) {
                        returnValue.cancel()
                    }
                }
            }
            if (main.configValues!!.isEnabled(moe.ruruke.skyblock.core.Feature.HIDE_OTHER_PLAYERS_PRESENTS)) {
                val present: JerryPresent = JerryPresent.getJerryPresents().get(entityIn.uniqueID)!!
                if (present != null && present.shouldHide()) {
                    returnValue.cancel()
                }
            }
        }
    }
}
