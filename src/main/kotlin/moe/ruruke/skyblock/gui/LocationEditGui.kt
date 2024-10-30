//package moe.ruruke.skyblock.gui
//
//import codes.biscuit.skyblockaddons.SkyblockAddons
//import com.google.common.collect.Sets
//
//import org.lwjgl.input.Keyboard
//import org.lwjgl.input.Mouse
//import java.awt.Color
//import java.io.IOException
//import java.util.*
//import kotlin.math.abs
//import kotlin.math.max
//import kotlin.math.min
//
//class LocationEditGui(private val lastPage: Int, lastTab: GuiTab?) : GuiScreen() {
//    private var editMode: EditMode? = EditMode.RESCALE
//    private var showColorIcons = true
//    private var enableSnapping = true
//    private var showFeatureNameOnHover = false
//
//    private val main: SkyblockAddons = SkyblockAddons.getInstance()
//
//    // The feature that is currently being dragged, or null for nothing.
//    private var draggedFeature: Feature? = null
//
//    // The feature the mouse is currently hovering over, null for nothing.
//    private var hoveredFeature: Feature? = null
//
//    private var resizing = false
//    private var resizingCorner: Corner? = null
//
//    private val originalHeight = 0
//    private val originalWidth = 0
//
//    private var xOffset = 0f
//    private var yOffset = 0f
//
//    private val lastTab: GuiTab? = lastTab
//
//    private val buttonLocations: MutableMap<Feature?, ButtonLocation> = EnumMap<Any?, Any?>(
//        Feature::class.java
//    )
//
//    private var closing = false
//
//    override fun initGui() {
//        // Add all gui elements that can be edited to the gui.
//        for (feature in Feature.getGuiFeatures()) {
//            if (feature.getGuiFeatureData() != null && feature.getGuiFeatureData()
//                    .getDrawType() === EnumUtils.DrawType.TEXT ||
//                (feature.getGuiFeatureData() == null || feature.getGuiFeatureData()
//                    .getDrawType() !== EnumUtils.DrawType.TEXT) &&
//                !SkyblockAddonsPlus.configValues
//            );
//            !!
//            isDisabled(feature)
//            run {
//                // Don't display features that have been disabled
//                val buttonLocation: ButtonLocation = ButtonLocation(feature)
//                buttonList.add(buttonLocation)
//                buttonLocations.put(feature, buttonLocation)
//            }
//        }
//
//        if (this.editMode == EditMode.RESIZE_BARS) {
//            addResizeButtonsToBars()
//        } else if (this.editMode == EditMode.RESCALE) {
//            addResizeButtonsToAllFeatures()
//        }
//
//        addColorWheelsToAllFeatures()
//
//        val features: Array<Feature> = arrayOf<Feature>(
//            Feature.RESET_LOCATION, Feature.RESCALE_FEATURES, Feature.RESIZE_BARS, Feature.SHOW_COLOR_ICONS,
//            Feature.ENABLE_FEATURE_SNAPPING, Feature.SHOW_FEATURE_NAMES_ON_HOVER
//        )
//
//        val scaledResolution: ScaledResolution = ScaledResolution(Minecraft.getMinecraft())
//        val boxHeight = 20
//        val numButtons = features.size
//        var x: Int
//        var y: Int = scaledResolution.getScaledHeight() / 2
//        // List may change later
//        if (numButtons % 2 == 0) {
//            y = (y - (Math.round((numButtons / 2f) * (boxHeight + 5)) - 2.5)).toInt()
//        } else {
//            y -= Math.round(((numButtons - 1) / 2f) * (boxHeight + 5)) + 10
//        }
//
//        for (feature in features) {
//            val featureName: String = feature.getMessage()
//            var boxWidth: Int = mc.fontRendererObj.getStringWidth(featureName) + 10
//            if (boxWidth > BUTTON_MAX_WIDTH) boxWidth = BUTTON_MAX_WIDTH
//            x = scaledResolution.getScaledWidth() / 2 - boxWidth / 2
//            y += boxHeight + 5
//            buttonList.add(ButtonSolid(x, y, boxWidth, boxHeight, featureName, main, feature))
//        }
//    }
//
//    private fun clearAllResizeButtons() {
//        buttonList.removeIf { button: GuiButton? -> button is ButtonResize }
//    }
//
//    private fun clearAllColorWheelButtons() {
//        buttonList.removeIf { button: GuiButton? -> button is ButtonColorWheel }
//    }
//
//    private fun addResizeButtonsToAllFeatures() {
//        clearAllResizeButtons()
//        // Add all gui elements that can be edited to the gui.
//        for (feature in Feature.getGuiFeatures()) {
//            if (!SkyblockAddonsPlus.configValues);
//            !!
//            isDisabled(feature)
//            run { // Don't display features that have been disabled
//                addResizeCorners(feature)
//            }
//        }
//    }
//
//    private fun addResizeButtonsToBars() {
//        clearAllResizeButtons()
//        // Add all gui elements that can be edited to the gui.
//        for (feature in Feature.getGuiFeatures()) {
//            if (!SkyblockAddonsPlus.configValues);
//            !!
//            isDisabled(feature)
//            run { // Don't display features that have been disabled
//                if (feature.getGuiFeatureData() != null && feature.getGuiFeatureData()
//                        .getDrawType() === EnumUtils.DrawType.BAR
//                ) {
//                    addResizeCorners(feature)
//                }
//            }
//        }
//    }
//
//    private fun addColorWheelsToAllFeatures() {
//        for (buttonLocation in buttonLocations.values) {
//            val feature: Feature = buttonLocation.getFeature()
//
//            if (feature.getGuiFeatureData() == null || feature.getGuiFeatureData().getDefaultColor() == null) {
//                continue
//            }
//
//            val anchorPoint: AnchorPoint = SkyblockAddonsPlus.configValues
//            !!
//            getAnchorPoint(feature)
//            val scaleX: Float = if (feature.getGuiFeatureData()
//                    .getDrawType() === EnumUtils.DrawType.BAR
//            ) SkyblockAddonsPlus.configValues
//            !!
//            getSizesX(feature)
//            1
//            val scaleY: Float = if (feature.getGuiFeatureData()
//                    .getDrawType() === EnumUtils.DrawType.BAR
//            ) SkyblockAddonsPlus.configValues
//            !!
//            getSizesY(feature)
//            1
//            val boxXOne: Float = buttonLocation.getBoxXOne() * scaleX
//            val boxXTwo: Float = buttonLocation.getBoxXTwo() * scaleX
//            val boxYOne: Float = buttonLocation.getBoxYOne() * scaleY
//            val boxYTwo: Float = buttonLocation.getBoxYTwo() * scaleY
//            val y: Float = boxYOne + (boxYTwo - boxYOne) / 2f - ButtonColorWheel.getSize() / 2f
//
//            var x =
//                if (anchorPoint === EnumUtils.AnchorPoint.TOP_LEFT || anchorPoint === EnumUtils.AnchorPoint.BOTTOM_LEFT) {
//                    boxXTwo + 2
//                } else {
//                    boxXOne - ButtonColorWheel.getSize() - 2
//                }
//
//            buttonList.add(ButtonColorWheel(Math.round(x), Math.round(y), feature))
//        }
//    }
//
//    private fun addResizeCorners(feature: Feature) {
//        buttonList.removeIf { button: GuiButton -> button is ButtonResize && (button as ButtonResize).getFeature() === feature }
//
//        val buttonLocation: ButtonLocation = buttonLocations[feature] ?: return
//
//        val boxXOne: Float = buttonLocation.getBoxXOne()
//        val boxXTwo: Float = buttonLocation.getBoxXTwo()
//        val boxYOne: Float = buttonLocation.getBoxYOne()
//        val boxYTwo: Float = buttonLocation.getBoxYTwo()
//        val scaleX: Float =
//            if (feature.getGuiFeatureData().getDrawType() === EnumUtils.DrawType.BAR) SkyblockAddonsPlus.configValues
//        !!
//        getSizesX(feature)
//        1
//        val scaleY: Float =
//            if (feature.getGuiFeatureData().getDrawType() === EnumUtils.DrawType.BAR) SkyblockAddonsPlus.configValues
//        !!
//        getSizesY(feature)
//        1
//        buttonList.add(ButtonResize(boxXOne * scaleX, boxYOne * scaleY, feature, ButtonResize.Corner.TOP_LEFT))
//        buttonList.add(ButtonResize(boxXTwo * scaleX, boxYOne * scaleY, feature, ButtonResize.Corner.TOP_RIGHT))
//        buttonList.add(ButtonResize(boxXOne * scaleX, boxYTwo * scaleY, feature, ButtonResize.Corner.BOTTOM_LEFT))
//        buttonList.add(ButtonResize(boxXTwo * scaleX, boxYTwo * scaleY, feature, ButtonResize.Corner.BOTTOM_RIGHT))
//    }
//
//    /**
//     * Returns the `ButtonLocation` the mouse is currently hovering over. Returns `null` if the mouse is not
//     * hovering over a `ButtonLocation`.
//     *
//     * @param mouseX the x-coordinate of the mouse
//     * @param mouseY the y-coordinate of the mouse
//     * @return the `ButtonLocation` the mouse is currently hovering over or `null` if the mouse is not hovering
//     * over any
//     */
//    private fun getHoveredFeatureButton(mouseX: Int, mouseY: Int): ButtonLocation? {
//        for (button in buttonList) {
//            if (button is ButtonLocation) {
//                val buttonLocation: ButtonLocation = button as ButtonLocation
//                if (mouseX >= buttonLocation.getBoxXOne() && mouseX <= buttonLocation.getBoxXTwo() && mouseY >= buttonLocation.getBoxYOne() && mouseY <= buttonLocation.getBoxYTwo()) {
//                    return buttonLocation
//                }
//            }
//        }
//
//        return null
//    }
//
//    private fun recalculateResizeButtons() {
//        for (button in this.buttonList) {
//            if (button is ButtonResize) {
//                val buttonResize: ButtonResize = button as ButtonResize
//                val corner: Corner = buttonResize.getCorner()
//                val feature: Feature = buttonResize.getFeature()
//                val buttonLocation: ButtonLocation = buttonLocations[feature] ?: continue
//
//                val scaleX: Float = if (feature.getGuiFeatureData()
//                        .getDrawType() === EnumUtils.DrawType.BAR
//                ) SkyblockAddonsPlus.configValues
//                !!
//                getSizesX(feature)
//                1
//                val scaleY: Float = if (feature.getGuiFeatureData()
//                        .getDrawType() === EnumUtils.DrawType.BAR
//                ) SkyblockAddonsPlus.configValues
//                !!
//                getSizesY(feature)
//                1
//                val boxXOne: Float = buttonLocation.getBoxXOne() * scaleX
//                val boxXTwo: Float = buttonLocation.getBoxXTwo() * scaleX
//                val boxYOne: Float = buttonLocation.getBoxYOne() * scaleY
//                val boxYTwo: Float = buttonLocation.getBoxYTwo() * scaleY
//
//                if (corner === ButtonResize.Corner.TOP_LEFT) {
//                    buttonResize.x = boxXOne
//                    buttonResize.y = boxYOne
//                } else if (corner === ButtonResize.Corner.TOP_RIGHT) {
//                    buttonResize.x = boxXTwo
//                    buttonResize.y = boxYOne
//                } else if (corner === ButtonResize.Corner.BOTTOM_LEFT) {
//                    buttonResize.x = boxXOne
//                    buttonResize.y = boxYTwo
//                } else if (corner === ButtonResize.Corner.BOTTOM_RIGHT) {
//                    buttonResize.x = boxXTwo
//                    buttonResize.y = boxYTwo
//                }
//            }
//        }
//    }
//
//    private fun recalculateColorWheels() {
//        for (button in this.buttonList) {
//            if (button is ButtonColorWheel) {
//                val buttonColorWheel: ButtonColorWheel = button as ButtonColorWheel
//                val feature: Feature = buttonColorWheel.getFeature()
//                val buttonLocation: ButtonLocation = buttonLocations[feature] ?: continue
//
//                val anchorPoint: AnchorPoint = SkyblockAddonsPlus.configValues
//                !!
//                getAnchorPoint(feature)
//                val scaleX: Float = if (feature.getGuiFeatureData()
//                        .getDrawType() === EnumUtils.DrawType.BAR
//                ) SkyblockAddonsPlus.configValues
//                !!
//                getSizesX(feature)
//                1
//                val scaleY: Float = if (feature.getGuiFeatureData()
//                        .getDrawType() === EnumUtils.DrawType.BAR
//                ) SkyblockAddonsPlus.configValues
//                !!
//                getSizesY(feature)
//                1
//                val boxXOne: Float = buttonLocation.getBoxXOne() * scaleX
//                val boxXTwo: Float = buttonLocation.getBoxXTwo() * scaleX
//                val boxYOne: Float = buttonLocation.getBoxYOne() * scaleY
//                val boxYTwo: Float = buttonLocation.getBoxYTwo() * scaleY
//                val y: Float = boxYOne + (boxYTwo - boxYOne) / 2f - ButtonColorWheel.getSize() / 2f
//
//                var x =
//                    if (anchorPoint === EnumUtils.AnchorPoint.TOP_LEFT || anchorPoint === EnumUtils.AnchorPoint.BOTTOM_LEFT) {
//                        boxXTwo + 2
//                    } else {
//                        boxXOne - ButtonColorWheel.getSize() - 2
//                    }
//
//                buttonColorWheel.x = x
//                buttonColorWheel.y = y
//            }
//        }
//    }
//
//    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
//        val snaps = checkSnapping()
//
//        onMouseMove(mouseX, mouseY, snaps)
//
//        if (this.editMode == EditMode.RESCALE) {
//            recalculateResizeButtons()
//        }
//        recalculateColorWheels()
//
//        val startColor = Color(0, 0, 0, 64).rgb
//        val endColor = Color(0, 0, 0, 128).rgb
//        drawGradientRect(0, 0, width, height, startColor, endColor)
//        for (anchorPoint in EnumUtils.AnchorPoint.values()) {
//            val sr: ScaledResolution = ScaledResolution(Minecraft.getMinecraft())
//            val x: Int = anchorPoint.getX(sr.getScaledWidth())
//            val y: Int = anchorPoint.getY(sr.getScaledHeight())
//            var color: Int = ColorCode.RED.getColor(127)
//            val lastHovered: Feature = ButtonLocation.getLastHoveredFeature()
//            if (lastHovered != null && SkyblockAddonsPlus.configValues);
//            !!
//            getAnchorPoint(lastHovered) === anchorPoint
//            run {
//                color = ColorCode.YELLOW.getColor(127)
//            }
//            DrawUtils.drawRectAbsolute(x - 4, y - 4, x + 4, y + 4, color)
//        }
//        super.drawScreen(mouseX, mouseY, partialTicks) // Draw buttons.
//
//        if (snaps != null) {
//            for (snap in snaps) {
//                if (snap != null) {
//                    var left: Float = snap.getRectangle().get(Edge.LEFT)
//                    var top: Float = snap.getRectangle().get(Edge.TOP)
//                    var right: Float = snap.getRectangle().get(Edge.RIGHT)
//                    var bottom: Float = snap.getRectangle().get(Edge.BOTTOM)
//
//                    if (snap.width < 0.5) {
//                        val averageX = (left + right) / 2
//                        left = averageX - 0.25f
//                        right = averageX + 0.25f
//                    }
//                    if (snap.height < 0.5) {
//                        val averageY = (top + bottom) / 2
//                        top = averageY - 0.25f
//                        bottom = averageY + 0.25f
//                    }
//
//                    if ((right - left).toDouble() == 0.5 || (bottom - top).toDouble() == 0.5) {
//                        DrawUtils.drawRectAbsolute(left, top, right, bottom, -0xff0100)
//                    } else {
//                        DrawUtils.drawRectAbsolute(left, top, right, bottom, -0x10000)
//                    }
//                }
//            }
//        }
//
//        if (showFeatureNameOnHover) {
//            val hoveredButton: ButtonLocation? = getHoveredFeatureButton(mouseX, mouseY)
//
//            if (hoveredButton != null) {
//                drawHoveringText(listOf<T>(hoveredButton.getFeature().getMessage()), mouseX, mouseY)
//            }
//        }
//    }
//
//    fun checkSnapping(): Array<Snap?>? {
//        if (!enableSnapping) return null
//
//        if (draggedFeature != null) {
//            val thisButton: ButtonLocation = buttonLocations[draggedFeature] ?: return null
//
//            var horizontalSnap: Snap? = null
//            var verticalSnap: Snap? = null
//
//            for ((_, otherButton) in this.buttonLocations) {
//                if (otherButton === thisButton) continue
//
//                for (otherEdge in Edge.getHorizontalEdges()) {
//                    for (thisEdge in Edge.getHorizontalEdges()) {
//                        val deltaX = otherEdge.getCoordinate(otherButton) - thisEdge.getCoordinate(thisButton)
//
//                        if (abs(deltaX.toDouble()) <= SNAP_PULL) {
//                            val deltaY = Edge.TOP.getCoordinate(otherButton) - Edge.TOP.getCoordinate(thisButton)
//
//                            var topY: Float
//                            var bottomY: Float
//                            if (deltaY > 0) {
//                                topY = Edge.BOTTOM.getCoordinate(thisButton)
//                                bottomY = Edge.TOP.getCoordinate(otherButton)
//                            } else {
//                                topY = Edge.BOTTOM.getCoordinate(otherButton)
//                                bottomY = Edge.TOP.getCoordinate(thisButton)
//                            }
//
//                            val snapX = otherEdge.getCoordinate(otherButton)
//                            val thisSnap = Snap(
//                                otherEdge.getCoordinate(otherButton),
//                                topY,
//                                thisEdge.getCoordinate(thisButton),
//                                bottomY,
//                                thisEdge,
//                                otherEdge,
//                                snapX
//                            )
//
//                            if (thisSnap.height < SNAPPING_RADIUS) {
//                                if (horizontalSnap == null || thisSnap.height < horizontalSnap.height) {
//                                    if (SkyblockAddonsPlus.configValues);
//                                    !!
//                                    isEnabled(Feature.DEVELOPER_MODE)
//                                    run {
//                                        DrawUtils.drawRectAbsolute(
//                                            snapX - 0.5,
//                                            0,
//                                            snapX + 0.5,
//                                            mc.displayHeight,
//                                            -0xffff01
//                                        )
//                                    }
//                                    horizontalSnap = thisSnap
//                                }
//                            }
//                        }
//                    }
//                }
//
//                for (otherEdge in Edge.getVerticalEdges()) {
//                    for (thisEdge in Edge.getVerticalEdges()) {
//                        val deltaY = otherEdge.getCoordinate(otherButton) - thisEdge.getCoordinate(thisButton)
//
//                        if (abs(deltaY.toDouble()) <= SNAP_PULL) {
//                            val deltaX = Edge.LEFT.getCoordinate(otherButton) - Edge.LEFT.getCoordinate(thisButton)
//
//                            var leftX: Float
//                            var rightX: Float
//                            if (deltaX > 0) {
//                                leftX = Edge.RIGHT.getCoordinate(thisButton)
//                                rightX = Edge.LEFT.getCoordinate(otherButton)
//                            } else {
//                                leftX = Edge.RIGHT.getCoordinate(otherButton)
//                                rightX = Edge.LEFT.getCoordinate(thisButton)
//                            }
//                            val snapY = otherEdge.getCoordinate(otherButton)
//                            val thisSnap = Snap(
//                                leftX,
//                                otherEdge.getCoordinate(otherButton),
//                                rightX,
//                                thisEdge.getCoordinate(thisButton),
//                                thisEdge,
//                                otherEdge,
//                                snapY
//                            )
//
//                            if (thisSnap.width < SNAPPING_RADIUS) {
//                                if (verticalSnap == null || thisSnap.width < verticalSnap.width) {
//                                    if (SkyblockAddonsPlus.configValues);
//                                    !!
//                                    isEnabled(Feature.DEVELOPER_MODE)
//                                    run {
//                                        DrawUtils.drawRectAbsolute(
//                                            0,
//                                            snapY - 0.5,
//                                            mc.displayWidth,
//                                            snapY + 0.5,
//                                            -0xffff01
//                                        )
//                                    }
//                                    verticalSnap = thisSnap
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            return arrayOf(horizontalSnap, verticalSnap)
//        }
//
//        return null
//    }
//
//    /**
//     * Called during each frame a [ButtonLocation] in this GUI is being hovered over by the mouse.
//     *
//     * @param button the button being hovered over
//     */
//    fun onButtonHoverFrame(button: ButtonLocation) {
//        if (showFeatureNameOnHover && (hoveredFeature == null || hoveredFeature.ordinal() !== button.feature.ordinal())) {
//            hoveredFeature = button.getFeature()
//        }
//    }
//
//    internal enum class Edge {
//        LEFT,
//        TOP,
//        RIGHT,
//        BOTTOM,
//
//        HORIZONTAL_MIDDLE,
//        VERTICAL_MIDDLE,
//        ;
//
//        fun getCoordinate(button: ButtonLocation): Float {
//            return when (this) {
//                LEFT -> button.getBoxXOne() * button.getScale()
//                TOP -> button.getBoxYOne() * button.getScale()
//                RIGHT -> button.getBoxXTwo() * button.getScale()
//                BOTTOM -> button.getBoxYTwo() * button.getScale()
//                HORIZONTAL_MIDDLE -> getCoordinate(
//                    button
//                ) + (getCoordinate(button) - getCoordinate(
//                    button
//                )) / 2f
//
//                VERTICAL_MIDDLE -> getCoordinate(
//                    button
//                ) + (getCoordinate(button) - getCoordinate(
//                    button
//                )) / 2f
//
//                else -> 0
//            }
//        }
//
//        companion object {
//            
//            private val verticalEdges: Set<Edge> = Sets.newHashSet(TOP, BOTTOM, HORIZONTAL_MIDDLE)
//
//            
//            private val horizontalEdges: Set<Edge> = Sets.newHashSet(LEFT, RIGHT, VERTICAL_MIDDLE)
//        }
//    }
//
//    /**
//     * Set the coordinates when the mouse moves.
//     */
//    protected fun onMouseMove(mouseX: Int, mouseY: Int, snaps: Array<Snap?>?) {
//        val sr: ScaledResolution = ScaledResolution(mc)
//        val minecraftScale: Float = sr.getScaleFactor().toFloat()
//        val floatMouseX = Mouse.getX() / minecraftScale
//        val floatMouseY: Float = (mc.displayHeight - Mouse.getY()) / minecraftScale
//
//        if (resizing) {
//            val x = mouseX - xOffset
//            val y = mouseY - yOffset
//            if (this.editMode == EditMode.RESIZE_BARS) {
//                val buttonLocation: ButtonLocation = buttonLocations[draggedFeature] ?: return
//
//                val middleX: Float = (buttonLocation.getBoxXTwo() + buttonLocation.getBoxXOne()) / 2
//                val middleY: Float = (buttonLocation.getBoxYTwo() + buttonLocation.getBoxYOne()) / 2
//
//                var scaleX = (floatMouseX - middleX) / (xOffset - middleX)
//                var scaleY = (floatMouseY - middleY) / (yOffset - middleY)
//                scaleX = max(min(scaleX.toDouble(), 5.0), .25) as Float
//                scaleY = max(min(scaleY.toDouble(), 5.0), .25) as Float
//
//                SkyblockAddonsPlus.configValues
//                !!
//                setScaleX(draggedFeature, scaleX)
//                SkyblockAddonsPlus.configValues
//                !!
//                setScaleY(draggedFeature, scaleY)
//
//                buttonLocation.drawButton(mc, mouseX, mouseY)
//                recalculateResizeButtons()
//            } else if (this.editMode == EditMode.RESCALE) {
//                val buttonLocation: ButtonLocation = buttonLocations[draggedFeature] ?: return
//
//                val scale: Float = buttonLocation.getScale()
//                val scaledX1: Float = buttonLocation.getBoxXOne() * buttonLocation.getScale()
//                val scaledY1: Float = buttonLocation.getBoxYOne() * buttonLocation.getScale()
//                val scaledX2: Float = buttonLocation.getBoxXTwo() * buttonLocation.getScale()
//                val scaledY2: Float = buttonLocation.getBoxYTwo() * buttonLocation.getScale()
//                val scaledWidth = scaledX2 - scaledX1
//                val scaledHeight = scaledY2 - scaledY1
//
//                val width: Float = (buttonLocation.getBoxXTwo() - buttonLocation.getBoxXOne())
//                val height: Float = (buttonLocation.getBoxYTwo() - buttonLocation.getBoxYOne())
//
//                val middleX = scaledX1 + scaledWidth / 2f
//                val middleY = scaledY1 + scaledHeight / 2f
//
//                var xOffset = floatMouseX - this.xOffset * scale - middleX
//                var yOffset = floatMouseY - this.yOffset * scale - middleY
//
//                if (resizingCorner === ButtonResize.Corner.TOP_LEFT) {
//                    xOffset *= -1f
//                    yOffset *= -1f
//                } else if (resizingCorner === ButtonResize.Corner.TOP_RIGHT) {
//                    yOffset *= -1f
//                } else if (resizingCorner === ButtonResize.Corner.BOTTOM_LEFT) {
//                    xOffset *= -1f
//                }
//
//                val newWidth = xOffset * 2f
//                val newHeight = yOffset * 2f
//
//                val scaleX = newWidth / width
//                val scaleY = newHeight / height
//
//                val newScale = max(scaleX.toDouble(), scaleY.toDouble()).toFloat()
//
//                val normalizedScale: Float = ConfigValues.normalizeValueNoStep(newScale)
//                SkyblockAddonsPlus.configValues
//                !!
//                setGuiScale(draggedFeature, normalizedScale)
//                buttonLocation.drawButton(mc, mouseX, mouseY)
//                recalculateResizeButtons()
//            }
//        } else if (draggedFeature != null) {
//            val buttonLocation: ButtonLocation = buttonLocations[draggedFeature] ?: return
//
//            var horizontalSnap: Snap? = null
//            var verticalSnap: Snap? = null
//            if (snaps != null) {
//                horizontalSnap = snaps[0]
//                verticalSnap = snaps[1]
//            }
//
//            var x: Float = floatMouseX - SkyblockAddonsPlus.configValues
//            !!
//            getAnchorPoint(draggedFeature).getX(sr.getScaledWidth())
//            var y: Float = floatMouseY - SkyblockAddonsPlus.configValues
//            !!
//            getAnchorPoint(draggedFeature).getY(sr.getScaledHeight())
//
//            val scaledX1: Float = buttonLocation.getBoxXOne() * buttonLocation.getScale()
//            val scaledY1: Float = buttonLocation.getBoxYOne() * buttonLocation.getScale()
//            val scaledX2: Float = buttonLocation.getBoxXTwo() * buttonLocation.getScale()
//            val scaledY2: Float = buttonLocation.getBoxYTwo() * buttonLocation.getScale()
//            val scaledWidth = scaledX2 - scaledX1
//            val scaledHeight = scaledY2 - scaledY1
//
//            var xSnapped = false
//            var ySnapped = false
//
//            if (horizontalSnap != null) {
//                val snapX: Float = horizontalSnap.getSnapValue()
//
//                if (horizontalSnap.getThisSnapEdge() == Edge.LEFT) {
//                    val snapOffset = abs(((floatMouseX - this.xOffset) - (snapX + scaledWidth / 2f)).toDouble())
//                        .toFloat()
//                    if (snapOffset <= SNAP_PULL * minecraftScale) {
//                        xSnapped = true
//                        x = snapX - SkyblockAddonsPlus.configValues
//                        !!
//                        getAnchorPoint(draggedFeature).getX(sr.getScaledWidth()) + scaledWidth / 2f
//                    }
//                } else if (horizontalSnap.getThisSnapEdge() == Edge.RIGHT) {
//                    val snapOffset = abs(((floatMouseX - this.xOffset) - (snapX - scaledWidth / 2f)).toDouble())
//                        .toFloat()
//                    if (snapOffset <= SNAP_PULL * minecraftScale) {
//                        xSnapped = true
//                        x = snapX - SkyblockAddonsPlus.configValues
//                        !!
//                        getAnchorPoint(draggedFeature).getX(sr.getScaledWidth()) - scaledWidth / 2f
//                    }
//                } else if (horizontalSnap.getThisSnapEdge() == Edge.VERTICAL_MIDDLE) {
//                    val snapOffset =
//                        abs(((floatMouseX - this.xOffset) - (snapX)).toDouble()).toFloat()
//                    if (snapOffset <= SNAP_PULL * minecraftScale) {
//                        xSnapped = true
//                        x = snapX - SkyblockAddonsPlus.configValues
//                        !!
//                        getAnchorPoint(draggedFeature).getX(sr.getScaledWidth())
//                    }
//                }
//            }
//
//            if (verticalSnap != null) {
//                val snapY: Float = verticalSnap.getSnapValue()
//
//                if (verticalSnap.getThisSnapEdge() == Edge.TOP) {
//                    val snapOffset = abs(((floatMouseY - this.yOffset) - (snapY + scaledHeight / 2f)).toDouble())
//                        .toFloat()
//                    if (snapOffset <= SNAP_PULL * minecraftScale) {
//                        ySnapped = true
//                        y = snapY - SkyblockAddonsPlus.configValues
//                        !!
//                        getAnchorPoint(draggedFeature).getY(sr.getScaledHeight()) + scaledHeight / 2f
//                    }
//                } else if (verticalSnap.getThisSnapEdge() == Edge.BOTTOM) {
//                    val snapOffset = abs(((floatMouseY - this.yOffset) - (snapY - scaledHeight / 2f)).toDouble())
//                        .toFloat()
//                    if (snapOffset <= SNAP_PULL * minecraftScale) {
//                        ySnapped = true
//                        y = snapY - SkyblockAddonsPlus.configValues
//                        !!
//                        getAnchorPoint(draggedFeature).getY(sr.getScaledHeight()) - scaledHeight / 2f
//                    }
//                } else if (verticalSnap.getThisSnapEdge() == Edge.HORIZONTAL_MIDDLE) {
//                    val snapOffset =
//                        abs(((floatMouseY - this.yOffset) - (snapY)).toDouble()).toFloat()
//                    if (snapOffset <= SNAP_PULL * minecraftScale) {
//                        ySnapped = true
//                        y = snapY - SkyblockAddonsPlus.configValues
//                        !!
//                        getAnchorPoint(draggedFeature).getY(sr.getScaledHeight())
//                    }
//                }
//            }
//
//            if (!xSnapped) {
//                x -= xOffset
//            }
//
//            if (!ySnapped) {
//                y -= yOffset
//            }
//
//            if (xSnapped || ySnapped) {
//                val xChange: Float = Math.abs(SkyblockAddonsPlus.configValues)
//                !!
//                getRelativeCoords(draggedFeature).getX() - x
//
//                val yChange: Float = Math.abs(SkyblockAddonsPlus.configValues)
//                !!
//                getRelativeCoords(draggedFeature).getY() - y
//
//                if (xChange < 0.001 && yChange < 0.001) {
//                    return
//                }
//            }
//
//            SkyblockAddonsPlus.configValues
//            !!
//            setCoords(draggedFeature, x, y)
//            SkyblockAddonsPlus.configValues
//            !!
//            setClosestAnchorPoint(draggedFeature)
//            if (draggedFeature === Feature.HEALTH_BAR || draggedFeature === Feature.MANA_BAR || draggedFeature === Feature.DRILL_FUEL_BAR) {
//                addResizeCorners(draggedFeature)
//            }
//        }
//    }
//
//    /**
//     * If button is pressed, update the currently dragged button.
//     * Otherwise, they clicked the reset button, so reset the coordinates.
//     */
//    override fun actionPerformed(abstractButton: GuiButton) {
//        if (abstractButton is ButtonLocation) {
//            val buttonLocation: ButtonLocation = abstractButton as ButtonLocation
//            draggedFeature = buttonLocation.getFeature()
//
//            val sr: ScaledResolution = ScaledResolution(mc)
//            val minecraftScale: Float = sr.getScaleFactor().toFloat()
//            val floatMouseX = Mouse.getX() / minecraftScale
//            val floatMouseY: Float = (mc.displayHeight - Mouse.getY()) / minecraftScale
//
//            xOffset = floatMouseX - SkyblockAddonsPlus.configValues
//            !!
//            getActualX(buttonLocation.getFeature())
//            yOffset = floatMouseY - SkyblockAddonsPlus.configValues
//            !!
//            getActualY(buttonLocation.getFeature())
//        } else if (abstractButton is ButtonSolid) {
//            val buttonSolid: ButtonSolid = abstractButton as ButtonSolid
//            val feature: Feature = buttonSolid.getFeature()
//            if (feature === Feature.RESET_LOCATION) {
//                SkyblockAddonsPlus.configValues
//                !!
//                setAllCoordinatesToDefault()
//                SkyblockAddonsPlus.configValues
//                !!
//                putDefaultBarSizes()
//                for (guiFeature in Feature.getGuiFeatures()) {
//                    if (!SkyblockAddonsPlus.configValues);
//                    !!
//                    isDisabled(guiFeature)
//                    run { // Don't display features that have been disabled
//                        if (guiFeature === Feature.HEALTH_BAR || guiFeature === Feature.MANA_BAR || guiFeature === Feature.DRILL_FUEL_BAR) {
//                            addResizeCorners(guiFeature)
//                        }
//                    }
//                }
//            } else if (feature === Feature.RESIZE_BARS) {
//                if (editMode != EditMode.RESIZE_BARS) {
//                    editMode = EditMode.RESIZE_BARS
//                    addResizeButtonsToBars()
//                } else {
//                    editMode = null
//                    clearAllResizeButtons()
//                }
//            } else if (feature === Feature.RESCALE_FEATURES) {
//                if (editMode != EditMode.RESCALE) {
//                    editMode = EditMode.RESCALE
//                    addResizeButtonsToAllFeatures()
//                } else {
//                    editMode = null
//                    clearAllResizeButtons()
//                }
//            } else if (feature === Feature.SHOW_COLOR_ICONS) {
//                if (showColorIcons) {
//                    showColorIcons = false
//                    clearAllColorWheelButtons()
//                } else {
//                    showColorIcons = true
//                    addColorWheelsToAllFeatures()
//                }
//            } else if (feature === Feature.ENABLE_FEATURE_SNAPPING) {
//                enableSnapping = !enableSnapping
//            } else if (feature === Feature.SHOW_FEATURE_NAMES_ON_HOVER) {
//                showFeatureNameOnHover = !showFeatureNameOnHover
//            }
//        } else if (abstractButton is ButtonResize) {
//            val buttonResize: ButtonResize = abstractButton as ButtonResize
//            draggedFeature = buttonResize.getFeature()
//            resizing = true
//
//            val sr: ScaledResolution = ScaledResolution(mc)
//            val minecraftScale: Float = sr.getScaleFactor().toFloat()
//            val floatMouseX = Mouse.getX() / minecraftScale
//            val floatMouseY: Float = (mc.displayHeight - Mouse.getY()) / minecraftScale
//
//            val scale: Float = SkyblockAddonsPlus.configValues
//            !!
//            getGuiScale(buttonResize.getFeature())
//            if (editMode == EditMode.RESCALE) {
//                xOffset = (floatMouseX - buttonResize.getX() * scale) / scale
//                yOffset = (floatMouseY - buttonResize.getY() * scale) / scale
//            } else {
//                xOffset = floatMouseX
//                yOffset = floatMouseY
//            }
//
//            resizingCorner = buttonResize.getCorner()
//        } else if (abstractButton is ButtonColorWheel) {
//            val buttonColorWheel: ButtonColorWheel = abstractButton as ButtonColorWheel
//
//            closing = true
//            mc.displayGuiScreen(
//                ColorSelectionGui(
//                    buttonColorWheel.getFeature(), EnumUtils.GUIType.EDIT_LOCATIONS, lastTab,
//                    lastPage
//                )
//            )
//        }
//    }
//
//    
//    class Snap(
//        left: Float,
//        top: Float,
//        right: Float,
//        bottom: Float,
//        thisSnapEdge: Edge,
//        otherSnapEdge: Edge,
//        snapValue: Float
//    ) {
//        private val thisSnapEdge: Edge
//        private val otherSnapEdge: Edge
//        private val snapValue: Float
//        private val rectangle: MutableMap<Edge, Float> = EnumMap(
//            Edge::class.java
//        )
//
//        init {
//            rectangle[Edge.LEFT] = left
//            rectangle[Edge.TOP] = top
//            rectangle[Edge.RIGHT] = right
//            rectangle[Edge.BOTTOM] = bottom
//
//            rectangle[Edge.HORIZONTAL_MIDDLE] = top + height / 2
//            rectangle[Edge.VERTICAL_MIDDLE] = left + width / 2
//
//            this.otherSnapEdge = otherSnapEdge
//            this.thisSnapEdge = thisSnapEdge
//            this.snapValue = snapValue
//        }
//
//        val height: Float
//            get() = rectangle[Edge.BOTTOM]!! - rectangle[Edge.TOP]!!
//
//        val width: Float
//            get() = rectangle[Edge.RIGHT]!! - rectangle[Edge.LEFT]!!
//    }
//
//    /**
//     * Allow moving the last hovered feature with arrow keys.
//     */
//    @Throws(IOException::class)
//    override fun keyTyped(typedChar: Char, keyCode: Int) {
//        super.keyTyped(typedChar, keyCode)
//        val hoveredFeature: Feature = ButtonLocation.getLastHoveredFeature()
//        if (hoveredFeature != null) {
//            var xOffset = 0
//            var yOffset = 0
//            if (keyCode == Keyboard.KEY_LEFT) {
//                xOffset--
//            } else if (keyCode == Keyboard.KEY_UP) {
//                yOffset--
//            } else if (keyCode == Keyboard.KEY_RIGHT) {
//                xOffset++
//            } else if (keyCode == Keyboard.KEY_DOWN) {
//                yOffset++
//            }
//            if (keyCode == Keyboard.KEY_A) {
//                xOffset -= 10
//            } else if (keyCode == Keyboard.KEY_W) {
//                yOffset -= 10
//            } else if (keyCode == Keyboard.KEY_D) {
//                xOffset += 10
//            } else if (keyCode == Keyboard.KEY_S) {
//                yOffset += 10
//            }
//            SkyblockAddonsPlus.configValues
//            !!
//            setCoords(hoveredFeature, SkyblockAddonsPlus.configValues)
//            !!
//            TODO(
//                """
//                |Cannot convert element
//                |With text:
//                |getRelativeCoords(hoveredFeature).getX()+xOffset,
//                |                    SkyblockAddonsPlus.configValues
//                """.trimMargin()
//            )
//            !!
//            getRelativeCoords(hoveredFeature).getY() + yOffset
//        }
//    }
//
//    /**
//     * Reset the dragged feature when the mouse is released.
//     */
//    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
//        super.mouseReleased(mouseX, mouseY, state)
//        draggedFeature = null
//        resizing = false
//    }
//
//    /**
//     * Open up the last GUI (main), and save the config.
//     */
//    override fun onGuiClosed() {
//        SkyblockAddonsPlus.configValues
//        !!
//        saveConfig()
//        if (lastTab != null && !closing) {
//            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, lastPage, lastTab)
//        }
//    }
//
//    private enum class EditMode {
//        RESCALE,
//        RESIZE_BARS
//    }
//
//    companion object {
//        private const val SNAPPING_RADIUS = 120
//        private const val SNAP_PULL = 1
//    }
//}
