//package moe.ruruke.skyblock.gui.buttons
//
//
//import moe.ruruke.skyblock.gui.IslandWarpGui
//import moe.ruruke.skyblock.gui.IslandWarpGui.Island
//import moe.ruruke.skyblock.gui.IslandWarpGui.UnlockedStatus
//import moe.ruruke.skyblock.utils.*
//import net.minecraft.client.Minecraft
//import net.minecraft.client.gui.GuiButton
//import net.minecraft.client.gui.ScaledResolution
//import net.minecraft.client.renderer.GlStateManager
//import kotlin.math.max
//
//class IslandButton(
//    island: Island,
//    unlockedStatus: UnlockedStatus?,
//    markers: Map<IslandWarpGui.Marker, UnlockedStatus>
//) :
//    GuiButton(0, island.getX(), island.getY(), island.getLabel()) {
//    
//    private val markerButtons: MutableList<IslandMarkerButton> = ArrayList()
//
//    private var disableHover = false
//
//    private var startedHover: Long = -1
//    private var stoppedHover: Long = -1
//
//    private val island: Island = island
//
//    private val unlockedStatus: UnlockedStatus = IslandWarpGui.UnlockedStatus.UNLOCKED
//    private val markers: Map<IslandWarpGui.Marker, UnlockedStatus> = markers
//
//    init {
//        for (marker in IslandWarpGui.Marker.values()) {
//            if (marker.getIsland() === island) {
//                markerButtons.add(IslandMarkerButton(marker))
//            }
//        }
//    }
//
//    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
//        drawButton(mc, mouseX, mouseY, true)
//    }
//
//    fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int, actuallyDraw: Boolean) {
//        var mouseX = mouseX
//        var mouseY = mouseY
//        val minecraftScale: Int = ScaledResolution(mc).getScaleFactor()
//        val islandGuiScale: Float = IslandWarpGui.ISLAND_SCALE
//
//        mouseX *= minecraftScale
//        mouseY *= minecraftScale
//
//        mouseX = (mouseX / islandGuiScale).toInt()
//        mouseY = (mouseY / islandGuiScale).toInt()
//
//        mouseX -= IslandWarpGui.SHIFT_LEFT.toInt()
//        mouseY -= IslandWarpGui.SHIFT_TOP.toInt()
//
//        var x: Float = island.getX().toFloat()
//        var y: Float = island.getY().toFloat()
//        var h: Float = island.getH().toFloat()
//        var w: Float = island.getW().toFloat()
//
//        val centerX = x + (w / 2f)
//        val centerY = y + (h / 2f)
//        var expansion = 1f
//        var hovered = false
//
//        if (isHovering) {
//            var hoverTime = (System.currentTimeMillis() - startedHover).toInt()
//            if (hoverTime > ANIMATION_TIME) {
//                hoverTime = ANIMATION_TIME
//            }
//            expansion = hoverTime / ANIMATION_TIME.toFloat()
//            expansion *= 0.10.toFloat()
//            expansion += 1f
//
//            h = h * expansion
//            w = w * expansion
//            x = centerX - (w / 2f)
//            y = centerY - (h / 2f)
//        } else if (isStoppingHovering) {
//            var hoverTime = (System.currentTimeMillis() - stoppedHover).toInt()
//
//            if (hoverTime < ANIMATION_TIME) {
//                hoverTime = ANIMATION_TIME - hoverTime
//                expansion = hoverTime / ANIMATION_TIME.toFloat()
//                expansion *= 0.10.toFloat()
//                expansion += 1f
//
//                h = h * expansion
//                w = w * expansion
//                x = centerX - (w / 2f)
//                y = centerY - (h / 2f)
//            } else {
//                stoppedHover = -1
//            }
//        }
//
//        val unlocked =
//            unlockedStatus === IslandWarpGui.UnlockedStatus.UNLOCKED || unlockedStatus === IslandWarpGui.UnlockedStatus.IN_COMBAT
//
//        if (!unlocked) {
//            expansion = 1f
//            x = island.getX().toFloat()
//            y = island.getY().toFloat()
//            h = island.getH().toFloat()
//            w = island.getW().toFloat()
//        }
//
//        if (mouseX > x && mouseY > y && mouseX < x + w && mouseY < y + h) {
//            if (island.getBufferedImage() != null) {
//                val xPixel =
//                    Math.round(((mouseX - x) * IslandWarpGui.IMAGE_SCALED_DOWN_FACTOR) / expansion).toInt()
//                val yPixel =
//                    Math.round(((mouseY - y) * IslandWarpGui.IMAGE_SCALED_DOWN_FACTOR) / expansion).toInt()
//
//                try {
//                    val rgb: Int = island.getBufferedImage().getRGB(xPixel, yPixel)
//                    val alpha = (rgb and -0x1000000) shr 24
//                    if (alpha != 0) {
//                        hovered = true
//                    }
//                } catch (ex: IndexOutOfBoundsException) {
//                    // Can't find pixel, its okay just leave it grey.
//                }
//            } else {
//                hovered = true
//            }
//        }
//
//        if (disableHover) {
//            disableHover = false
//
//            hovered = false
//        }
//
//        if (hovered) {
//            if (!isHovering) {
//                startedHover = System.currentTimeMillis()
//
//                if (isStoppingHovering) {
//                    var timeSoFar = (System.currentTimeMillis() - stoppedHover).toInt()
//                    if (timeSoFar > ANIMATION_TIME) {
//                        timeSoFar = ANIMATION_TIME
//                    }
//
//                    startedHover -= (ANIMATION_TIME - timeSoFar).toLong()
//                    stoppedHover = -1
//                }
//            }
//        } else if (isHovering) {
//            stoppedHover = System.currentTimeMillis()
//
//            var timeSoFar = (System.currentTimeMillis() - startedHover).toInt()
//            if (timeSoFar > ANIMATION_TIME) {
//                timeSoFar = ANIMATION_TIME
//            }
//
//            stoppedHover -= (ANIMATION_TIME - timeSoFar).toLong()
//            startedHover = -1
//        }
//
//        if (actuallyDraw) {
//            if (unlocked) {
//                if (unlockedStatus === IslandWarpGui.UnlockedStatus.IN_COMBAT) {
//                    GlStateManager.color(1f, 0.6f, 0.6f, 1f)
//                } else {
//                    if (hovered) {
//                        GlStateManager.color(1f, 1f, 1f, 1f)
//                    } else {
//                        GlStateManager.color(0.9f, 0.9f, 0.9f, 1f)
//                    }
//                }
//            } else {
//                GlStateManager.color(0.3f, 0.3f, 0.3f, 1f)
//            }
//
//            mc.getTextureManager().bindTexture(island.getResourceLocation())
//            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, w, h, w, h)
//
//            for (marker in markerButtons) {
//                marker.drawButton(x, y, expansion, hovered, unlocked, markers[marker.getMaker()])
//            }
//
//            GlStateManager.pushMatrix()
//            var textScale = 3f
//            textScale *= expansion
//            GlStateManager.scale(textScale, textScale, 1f)
//
//            val alpha = max((255 - (((expansion - 1) / 0.1) * 255).toInt()).toDouble(), 4.0).toInt()
//            val color: Int = if (unlocked) {
//                ColorCode.WHITE.getColor()
//            } else {
//                ColorUtils.setColorAlpha(0x999999, alpha)
//            }
//
//            mc.fontRendererObj.drawStringWithShadow(
//                displayString,
//                centerX / textScale - mc.fontRendererObj.getStringWidth(displayString) / 2f,
//                centerY / textScale,
//                color
//            )
//
//            if (unlockedStatus !== IslandWarpGui.UnlockedStatus.UNLOCKED) {
//                mc.fontRendererObj.drawStringWithShadow(
//                    unlockedStatus.getMessage(),
//                    centerX / textScale - mc.fontRendererObj.getStringWidth(unlockedStatus.getMessage()) / 2f,
//                    (centerY + 30) / textScale,
//                    color
//                )
//            }
//
//            GlStateManager.color(1f, 1f, 1f, 1f)
//
//            GlStateManager.popMatrix()
//        }
//    }
//
//    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
////        int minecraftScale = Minecraft.getMinecraft().gameSettings.guiScale;
////        float islandGuiScale = ISLAND_SCALE;
////
////        mouseX *= minecraftScale;
////        mouseY *= minecraftScale;
////
////        mouseX /= islandGuiScale;
////        mouseY /= islandGuiScale;
////
////        mouseX -= IslandWarpGui.SHIFT_LEFT;
////        mouseY -= IslandWarpGui.SHIFT_TOP;
//
////        for (IslandWarpGui.Island island : IslandWarpGui.Island.values()) {
////            System.out.println(island.getLabel()+" "+(mouseX-island.getX()) + " " + (mouseY-island.getY()));
////        }
//
//        return false
//    }
//
//    val isHovering: Boolean
//        get() = startedHover != -1L
//
//    private val isStoppingHovering: Boolean
//        get() = stoppedHover != -1L
//
//    fun setDisableHover(disableHover: Boolean) {
//        this.disableHover = disableHover
//    }
//
//    companion object {
//        private const val ANIMATION_TIME = 200
//    }
//}
