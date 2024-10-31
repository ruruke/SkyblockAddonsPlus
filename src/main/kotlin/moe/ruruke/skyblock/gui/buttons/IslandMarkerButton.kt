//package moe.ruruke.skyblock.gui.buttons
//
//
//import moe.ruruke.skyblock.gui.IslandWarpGui
//import moe.ruruke.skyblock.gui.IslandWarpGui.UnlockedStatus
//import moe.ruruke.skyblock.utils.*
//import net.minecraft.client.Minecraft
//import net.minecraft.client.gui.GuiButton
//import net.minecraft.client.renderer.GlStateManager
//import net.minecraft.util.ResourceLocation
//import java.awt.geom.Point2D
//import kotlin.math.max
//
//class IslandMarkerButton(marker: IslandWarpGui.Marker) : GuiButton(0, 0, 0, marker.getLabel()) {
//    
//    private val marker: IslandWarpGui.Marker = marker
//
//    private var centerX = 0f
//    private var centerY = 0f
//    private var unlocked = false
//    fun getMaker(): IslandWarpGui.Marker {
//        return marker
//    }
//
//    fun drawButton(
//        islandX: Float,
//        islandY: Float,
//        expansion: Float,
//        hovered: Boolean,
//        islandUnlocked: Boolean,
//        status: IslandWarpGui.UnlockedStatus?
//    ) {
//        var status: UnlockedStatus? = status
//        val mc: Minecraft = Minecraft.getMinecraft()
//        status = IslandWarpGui.UnlockedStatus.UNLOCKED
//
//        val width = 50 * expansion
//        val height = width * (100 / 81f) // Ratio is 81w : 100h
//
//        val centerX: Float = islandX + (marker.getX()) * expansion
//        val centerY: Float = islandY + (marker.getY()) * expansion
//
//        this.centerX = centerX
//        this.centerY = centerY
//
//        this.unlocked = status === IslandWarpGui.UnlockedStatus.UNLOCKED ||
//                status === IslandWarpGui.UnlockedStatus.IN_COMBAT
//
//        val x = centerX - (width / 2)
//        val y = centerY - (height / 2)
//
//        if (this.unlocked) {
//            if (hovered) {
//                GlStateManager.color(1f, 1f, 1f, 1f)
//            } else {
//                GlStateManager.color(1f, 1f, 1f, 0.6f)
//            }
//        } else {
//            if (islandUnlocked) {
//                GlStateManager.color(0.3f, 0.3f, 0.3f, 1f)
//            } else {
//                GlStateManager.color(0.3f, 0.3f, 0.3f, 0.6f)
//            }
//        }
//
//        mc.getTextureManager().bindTexture(PORTAL_ICON)
//        DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, width, height, width, height, true)
//
//        if (hovered) {
//            GlStateManager.pushMatrix()
//            var textScale = 2.5f
//            textScale *= expansion
//            GlStateManager.scale(textScale, textScale, 1f)
//
//            val alpha = max((((expansion - 1) / 0.1) * 255).toInt().toDouble(), 4.0).toInt()
//            val color: Int = if (this.unlocked) {
//                ColorCode.WHITE.getColor()
//            } else {
//                ColorUtils.setColorAlpha(0x999999, alpha)
//            }
//
//            mc.fontRendererObj.drawStringWithShadow(
//                displayString,
//                (x + (width / 2)) / textScale - mc.fontRendererObj.getStringWidth(displayString) / 2f,
//                (y - 20) / textScale,
//                color
//            )
//            GlStateManager.color(1f, 1f, 1f, 1f)
//
//            GlStateManager.popMatrix()
//        }
//    }
//
//    fun getDistance(mouseX: Int, mouseY: Int): Double {
//        var distance = Point2D.Double(mouseX.toDouble(), mouseY.toDouble()).distance(
//            Point2D.Double(
//                centerX.toDouble(), centerY.toDouble()
//            )
//        )
//
//        if (distance > MAX_SELECT_RADIUS || !unlocked) distance = -1.0
//
//        return distance
//    }
//
//    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
//        return false
//    }
//
//    companion object {
//        const val MAX_SELECT_RADIUS: Int = 90
//
//        private val PORTAL_ICON = ResourceLocation("skyblockaddonsplus", "portal.png")
//    }
//}
