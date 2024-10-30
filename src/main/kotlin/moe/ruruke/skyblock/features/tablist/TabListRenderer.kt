package moe.ruruke.skyblock.features.tablist

import moe.ruruke.skyblock.utils.TextUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiPlayerTabOverlay
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EnumPlayerModelParts

object TabListRenderer {
    const val MAX_LINES: Int = 22
    private const val LINE_HEIGHT = 8 + 1
    private const val PADDING = 3
    private const val COLUMN_SPACING = 6

    fun render() {
        val mc: Minecraft = Minecraft.getMinecraft()

        val columns: List<RenderColumn> = TabListParser.getRenderColumns() ?: return

        // Calculate maximums...
        var maxLines = 0
        for (column in columns) {
            maxLines = Math.max(maxLines, column.getLines().size)
        }
        var totalWidth = 0
        for (renderColumn in columns) {
            totalWidth += renderColumn.maxWidth + COLUMN_SPACING
        }
        totalWidth -= COLUMN_SPACING
        var totalHeight = maxLines * LINE_HEIGHT

        // Filter header and footer to only show hypixel advertisements...
        val tabList: GuiPlayerTabOverlay = mc.ingameGUI.getTabList()
        var header: MutableList<String>? = null
        if (tabList.header != null) {
            header = java.util.ArrayList<String>(
                java.util.Arrays.asList<String>(
                    *tabList.header.getFormattedText().split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()))
            header.removeIf { line: String -> !line.contains(TabListParser.HYPIXEL_ADVERTISEMENT_CONTAINS) }

            totalHeight += header.size * LINE_HEIGHT + PADDING
        }
        var footer: MutableList<String>? = null
        if (tabList.footer != null) {
            footer = java.util.ArrayList<String>(
                java.util.Arrays.asList<String>(
                    *tabList.footer.getFormattedText().split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()))
            footer.removeIf { line: String -> !line.contains(TabListParser.HYPIXEL_ADVERTISEMENT_CONTAINS) }

            totalHeight += footer.size * LINE_HEIGHT + PADDING
        }

        // Starting x & y, using the player's GUI scale
        val scaledResolution: ScaledResolution = ScaledResolution(mc)
        val screenWidth: Int = scaledResolution.getScaledWidth() / 2
        val x = screenWidth - totalWidth / 2
        val y = 10

        // Large background
        Gui.drawRect(
            x - COLUMN_SPACING,
            y - PADDING,
            screenWidth + totalWidth / 2 + COLUMN_SPACING,
            10 + totalHeight + PADDING,
            -0x80000000
        )

        // Draw header
        var headerY = y
        if (header != null) {
            for (line in header) {
                mc.fontRendererObj.drawStringWithShadow(
                    line,
                    x + totalWidth / 2f - mc.fontRendererObj.getStringWidth(line) / 2f,
                    headerY.toFloat(),
                    -0x1
                )
                headerY += 8 + 1
            }
            headerY += PADDING
        }

        // Draw the middle lines
        var middleX = x
        for (renderColumn in columns) {
            var middleY = headerY

            // Column background
            Gui.drawRect(
                middleX - PADDING + 1, middleY - PADDING + 1, middleX + renderColumn.maxWidth + PADDING - 2,
                middleY + renderColumn.getLines().size * LINE_HEIGHT + PADDING - 2, 0x20AAAAAA
            )

            for (tabLine in renderColumn.getLines()) {
                val savedX = middleX

                if (tabLine.getType() === TabStringType.PLAYER) {
                    val networkPlayerInfo: NetworkPlayerInfo =
                        mc.getNetHandler().getPlayerInfo(TextUtils.stripUsername(tabLine.getText()!!))
                    if (networkPlayerInfo != null) {
                        val entityPlayer: EntityPlayer =
                            mc.theWorld.getPlayerEntityByUUID(networkPlayerInfo.getGameProfile().getId())

                        mc.getTextureManager().bindTexture(networkPlayerInfo.getLocationSkin())
                        GlStateManager.color(1f, 1f, 1f, 1f)
                        Gui.drawScaledCustomSizeModalRect(middleX, middleY, 8f, 8f, 8, 8, 8, 8, 64.0f, 64.0f)
                        if (entityPlayer != null && entityPlayer.isWearing(EnumPlayerModelParts.HAT)) {
                            Gui.drawScaledCustomSizeModalRect(middleX, middleY, 40.0f, 8f, 8, 8, 8, 8, 64.0f, 64.0f)
                        }
                    }
                    middleX += 8 + 2
                }

                if (tabLine.getType() === TabStringType.TITLE) {
                    mc.fontRendererObj.drawStringWithShadow(
                        tabLine.getText(),
                        (middleX + renderColumn.maxWidth / 2f - tabLine.getWidth() / 2f),
                        middleY.toFloat(),
                        -0x1
                    )
                } else {
                    mc.fontRendererObj.drawStringWithShadow(
                        tabLine.getText(),
                        middleX.toFloat(),
                        middleY.toFloat(),
                        -0x1
                    )
                }
                middleY += LINE_HEIGHT
                middleX = savedX
            }

            middleX += renderColumn.maxWidth + COLUMN_SPACING
        }

        // Draw the footer
        if (footer != null) {
            var footerY = y + totalHeight - footer.size * LINE_HEIGHT
            for (line in footer) {
                mc.fontRendererObj.drawStringWithShadow(
                    line,
                    x + totalWidth / 2f - mc.fontRendererObj.getStringWidth(line) / 2f,
                    footerY.toFloat(),
                    -0x1
                )
                footerY += LINE_HEIGHT
            }
        }
    }
}
