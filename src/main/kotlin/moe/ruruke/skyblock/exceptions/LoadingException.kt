package moe.ruruke.skyblock.exceptions

import com.google.common.base.Throwables
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.GuiErrorScreen
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException

open class LoadingException @JvmOverloads constructor(
    message: String?,
    cause: Throwable?,
    private val DRAW_ROOT_CAUSE: Boolean = false
) :
    CustomModLoadingErrorDisplayException(message, cause) {
    private var rootCauseString: String? = null
    private var maxWidth = 0
    private var xCenter = 0
    private var yStart = 0

    init {
        if (DRAW_ROOT_CAUSE) {
            rootCauseString = Throwables.getRootCause(this).toString()
        }
    }

    override fun initGui(errorScreen: GuiErrorScreen, fontRenderer: FontRenderer) {
        maxWidth = errorScreen.width - 80
        yStart = 50
        xCenter = errorScreen.width / 2
    }

    override fun drawScreen(
        errorScreen: GuiErrorScreen,
        fontRenderer: FontRenderer,
        mouseRelX: Int,
        mouseRelY: Int,
        tickTime: Float
    ) {
        var yPos = yStart
        errorScreen.drawCenteredString(
            fontRenderer, String.format(
                "%sSkyblockAddons%s has encountered an error while loading.",
                EnumChatFormatting.AQUA,
                EnumChatFormatting.RESET
            ), xCenter,
            yPos, 0xFFFFFF
        )
        yPos += 20
        if (message != null) {
            for (errorLine in fontRenderer.listFormattedStringToWidth(message, maxWidth)) {
                errorScreen.drawCenteredString(fontRenderer, errorLine, xCenter, yPos, 0xFFFFFF)
                yPos += 10
            }
        }
        if (DRAW_ROOT_CAUSE) {
            yPos += 10
            errorScreen.drawCenteredString(fontRenderer, "Cause:", xCenter, yPos, 0xFFFFFF)
            yPos += 10
            errorScreen.drawCenteredString(fontRenderer, rootCauseString, xCenter, yPos, 0xFFFFFF)
        }
        yPos += 30
        errorScreen.drawCenteredString(fontRenderer, "Please restart your game.", xCenter, yPos, 0xFFFFFF)
        yPos += 10
        val errorPersistString = fontRenderer.listFormattedStringToWidth(
            String.format(
                "If error persists after restarting, please report it at " +
                        "%shttps://discord.gg/zWyr3f5GXz.%s", EnumChatFormatting.BOLD, EnumChatFormatting.RESET
            ), maxWidth
        )
        for (line in errorPersistString) {
            errorScreen.drawCenteredString(fontRenderer, line, xCenter, yPos, 0xFFFFFF)
            yPos += 10
        }
    }
}
