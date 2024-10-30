package moe.ruruke.skyblock.gui.buttons

import moe.ruruke.skyblock.core.Feature
import net.minecraft.client.gui.GuiButton

open class ButtonFeature internal constructor(buttonId: Int, x: Int, y: Int, buttonText: String?, feature: Feature?) :
    GuiButton(buttonId, x, y, buttonText) {
    // The feature that this button moves.
    open var feature: Feature? = feature
}
