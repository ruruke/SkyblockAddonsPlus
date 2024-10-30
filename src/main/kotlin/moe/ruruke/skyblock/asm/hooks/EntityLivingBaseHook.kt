package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.misc.scheduler.SkyblockRunnable
import net.minecraft.client.Minecraft
import net.minecraft.entity.EntityLivingBase
import net.minecraft.potion.PotionEffect

object EntityLivingBaseHook {
    fun onResetHurtTime(entity: EntityLivingBase?) {
    }

    private val nightVisionEffectsToRemove: MutableSet<Long> = HashSet()

    fun onRemovePotionEffect(entityLivingBase: EntityLivingBase, potionID: Int): Boolean {
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        // 16 -> Night Vision
        if (potionID == 16 && entityLivingBase === Minecraft.getMinecraft().thePlayer &&
            main.utils!!.isOnSkyblock() && main.configValues!!.isEnabled(Feature.AVOID_BLINKING_NIGHT_VISION)
        ) {
            val now = System.currentTimeMillis()
            nightVisionEffectsToRemove.add(now)

            main.newScheduler!!.scheduleDelayedTask(object : SkyblockRunnable() {
                override fun run() {
                    if (nightVisionEffectsToRemove.remove(now)) {
                        entityLivingBase.removePotionEffect(potionID)
                    }
                }
            }, 2)

            return true
        }

        return false
    }

    fun onAddPotionEffect(entityLivingBase: EntityLivingBase, potionEffect: PotionEffect) {
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        // 16 -> Night Vision, Night Vision Charm duration is 300 ticks...
        if (potionEffect.potionID == 16 && potionEffect.duration == 300 && entityLivingBase === Minecraft.getMinecraft().thePlayer &&
            main.utils!!.isOnSkyblock() && main.configValues!!.isEnabled(Feature.AVOID_BLINKING_NIGHT_VISION)
        ) {
            nightVisionEffectsToRemove.clear()
        }
    }
}
