package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import net.minecraft.util.IChatComponent


class GuiNewChatHook {
    companion object {
        @JvmStatic
        fun getUnformattedText(iChatComponent: IChatComponent): String {

            return iChatComponent.getFormattedText() // For logging colored messages...
//        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
//        if (main != null && main.configValues!!.isEnabled(moe.ruruke.skyblock.core.Feature.DEVELOPER_MODE)) {
//            return iChatComponent.getFormattedText() // For logging colored messages...
//        }
//        return iChatComponent.getUnformattedText()
        }
    }
}
