package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import net.minecraft.client.gui.GuiButton


object GuiIngameMenuHook {
    fun addMenuButtons(buttonList: MutableList<GuiButton?>, width: Int, height: Int) {
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance

        if (main.utils!!.isOnSkyblock() && main.configValues!!
                .isEnabled(moe.ruruke.skyblock.core.Feature.SKYBLOCK_ADDONS_BUTTON_IN_PAUSE_MENU)
        ) {
            buttonList.add(GuiButton(53, width - 120 - 5, height - 20 - 5, 120, 20, "SkyblockAddons Menu"))
        }
    }

    fun onButtonClick() {
        //TODO:
        val skyblockAddons: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        skyblockAddons.renderListener!!.setGuiToOpen(
            moe.ruruke.skyblock.utils.EnumUtils.GUIType.MAIN,
            1,
            moe.ruruke.skyblock.utils.EnumUtils.GuiTab.MAIN
        )
    }
}
