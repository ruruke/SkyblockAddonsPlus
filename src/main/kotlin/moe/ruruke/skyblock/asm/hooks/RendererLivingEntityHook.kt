package moe.ruruke.skyblock.asm.hooks

import com.google.common.collect.Sets
import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.features.EntityOutlines.EntityOutlineRenderer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EnumPlayerModelParts

// cough nothing to see here
object RendererLivingEntityHook {
    // TODO: Convert this to UUIDs instead of names
    // no don't ask to be added lol, for now these are just like my admins
    private val coolPeople: Set<String> = Sets.newHashSet(
        "Dinnerbone",
        "Biscut",
        "Pinpointed",
        "Berded",
        "Potat_owo",
        "Pnda__",
        "Throwpo",
        "StopUsingSBE"
    )
    private var isCoolPerson = false

    fun equals(string: String, otherString: Any?): Boolean {
        isCoolPerson = coolPeople.contains(string)
        return isCoolPerson
    }

    fun isWearing(entityPlayer: EntityPlayer, p_175148_1_: EnumPlayerModelParts?): Boolean {
        return (!isCoolPerson && entityPlayer.isWearing(p_175148_1_)) ||
                (isCoolPerson && !entityPlayer.isWearing(p_175148_1_))
    }

    fun setOutlineColor(entity: EntityLivingBase, originalColor: Int): Int {
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        //TODO
        val i: Int = EntityOutlineRenderer.getCustomOutlineColor(entity)!!
        if (i != null) {
            return i
        }
        //TODO:
//        if (main.configValues!!.isEnabled(Feature.SHOW_CRITICAL_DUNGEONS_TEAMMATES) &&
//            main.utils!!.isInDungeon() && main.getDungeonManager().getTeammates().containsKey(entity.getName())
//        ) {
//            val dungeonPlayer: DungeonPlayer = main.getDungeonManager().getTeammates().get(entity.getName())
//
//            if (dungeonPlayer.isCritical()) {
//                return Minecraft.getMinecraft().fontRendererObj.getColorCode('c')
//            } else if (dungeonPlayer.isLow()) {
//                return Minecraft.getMinecraft().fontRendererObj.getColorCode('e')
//            }
//        } else {
//            val i: Int = EntityOutlineRenderer.getCustomOutlineColor(entity)
//            if (i != null) {
//                return i
//            }
//        }
        return originalColor
    }
}
