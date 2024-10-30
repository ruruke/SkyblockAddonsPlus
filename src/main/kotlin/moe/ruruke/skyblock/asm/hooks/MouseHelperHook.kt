package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus


object MouseHelperHook {
    fun ungrabMouseCursor(new_x: Int, new_y: Int) {
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        if (main.configValues!!
                .isDisabled(moe.ruruke.skyblock.core.Feature.DONT_RESET_CURSOR_INVENTORY) || main.getPlayerListener()
                .shouldResetMouse()
        ) {
            org.lwjgl.input.Mouse.setCursorPosition(new_x, new_y)
            org.lwjgl.input.Mouse.setGrabbed(false)
        }
    }
}
