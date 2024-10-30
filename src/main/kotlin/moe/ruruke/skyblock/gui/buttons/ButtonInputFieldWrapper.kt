package moe.ruruke.skyblock.gui.buttons

import moe.ruruke.skyblock.utils.ColorCode
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiTextField
import org.apache.commons.lang3.StringUtils


class ButtonInputFieldWrapper(
    x: Int,
    y: Int,
    w: Int,
    h: Int,
    buttonText: String?,
    private val placeholderText: String?,
    maxLength: Int,
    focused: Boolean,
    private val textUpdated: UpdateCallback<String?>
) :
    GuiButton(-1, x, y, buttonText) {
    private val textField: GuiTextField

    constructor(
        x: Int,
        y: Int,
        w: Int,
        h: Int,
        buttonText: String?,
        maxLength: Int,
        focused: Boolean,
        textUpdated: UpdateCallback<String?>
    ) : this(x, y, w, h, buttonText, null, maxLength, focused, textUpdated)

    init {
        textField = GuiTextField(-1, Minecraft.getMinecraft().fontRendererObj, x, y, w, h)
        textField.setMaxStringLength(maxLength)
        textField.setFocused(focused)
        textField.setText(buttonText)
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        textField.drawTextBox()
        if (placeholderText != null && StringUtils.isEmpty(textField.getText())) {
            mc.fontRendererObj.drawString(placeholderText, xPosition + 4, yPosition + 3, ColorCode.DARK_GRAY.getColor())
        }
    }

    protected fun keyTyped(typedChar: Char, keyCode: Int) {
        if (textField.isFocused()) {
            textField.textboxKeyTyped(typedChar, keyCode)
        }
        textUpdated.onUpdate(textField.getText())
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        textField.mouseClicked(mouseX, mouseY, 0)

        return textField.isFocused()
    }

    fun updateScreen() {
        textField.updateCursorCounter()
    }

    companion object {
        fun callKeyTyped(buttonList: List<GuiButton?>, typedChar: Char, keyCode: Int) {
            for (button in buttonList) {
                if (button is ButtonInputFieldWrapper) {
                    (button as ButtonInputFieldWrapper).keyTyped(typedChar, keyCode)
                }
            }
        }

        fun callUpdateScreen(buttonList: List<GuiButton?>) {
            for (button in buttonList) {
                if (button is ButtonInputFieldWrapper) {
                    (button as ButtonInputFieldWrapper).updateScreen()
                }
            }
        }
    }
}
