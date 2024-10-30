package moe.ruruke.skyblock.gui.buttons

import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.utils
import moe.ruruke.skyblock.utils.ColorCode
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation

/**
 * Button that lets the user select one item in a given set of items.
 */
class ButtonSelect(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    items: List<SelectItem>,
    selectedIndex: Int,
    callback: OnItemSelectedCallback?
) :
    GuiButton(0, x, y, "") {
    /**
     * Item that can be used in this Select button
     */
    interface SelectItem {
        var name: String

        var description: String
//        fun getName(): String
//        fun getDesc(): String?
    }

    fun interface OnItemSelectedCallback {
        /**
         * Called whenever the selected item changes by clicking the next or previous button.
         *
         * @param index The new selected index
         */
        fun onItemSelected(index: Int)
    }

    private val itemList: List<SelectItem>
    private var index: Int

    private val textWidth: Int
    private val callback: OnItemSelectedCallback?

    /*
     * Rough sketch of the button
     *  __ __________ __
     * |< |          |> |
     *  -- ---------- --
     */
    /**
     * Create a new Select button at (x, y) with a given width and height and set of items to select from.
     * Initially selects the given `selectedIndex` or `0` if that is out of bounds of the given list.
     * Optionally accept a callback that is called whenever a new item is selected.
     * Note: Effective width for text is about `width - 2 * height` as the arrow buttons are squares with
     * a side length of `height`.
     * Text will be trimmed and marked with ellipses `…` if it is too long to fit in the text area.
     *
     * @param x             x position
     * @param y             y position
     * @param width         total width
     * @param height        height
     * @param items         non-null and non-empty List of items to choose from
     * @param selectedIndex initially selected index in the given list of items
     * @param callback      Nullable callback when a new item is selected
     */
    init {
        require(!(items == null || items.isEmpty())) { "Item list must have at least one element." }

        textWidth = width - (2 * height) - 6 // 2 * 3 text padding on both sides
        this.width = width
        this.height = height
        itemList = items
        this.index = if (selectedIndex > 0 && selectedIndex < itemList.size) selectedIndex else 0
        this.callback = callback
    }

    override fun drawButton(minecraft: Minecraft, mouseX: Int, mouseY: Int) {
        val endX = xPosition + width

        val color = utils!!.getDefaultColor(100f)
        val leftColor = utils!!.getDefaultColor((if (isOverLeftButton(mouseX, mouseY)) 200 else 90).toFloat())
        val rightColor = utils!!.getDefaultColor((if (isOverRightButton(mouseX, mouseY)) 200 else 90).toFloat())

        val name = itemList[index].name
        var trimmedName = minecraft.fontRendererObj.trimStringToWidth(name, textWidth)
        if (name != trimmedName) {
            trimmedName = ellipsize(trimmedName)
        }
        val description = itemList[index].description
        // background / text area
        drawRect(xPosition, yPosition, endX, yPosition + height, color)
        // left button
        drawRect(xPosition, yPosition, xPosition + height, yPosition + height, leftColor)
        //right button
        drawRect(endX - height, yPosition, endX, yPosition + height, rightColor)

        // inside text
        drawCenteredString(
            minecraft.fontRendererObj,
            trimmedName,
            xPosition + width / 2,
            yPosition + height / 4,
            ColorCode.WHITE.getColor()
        )
        // description
        drawCenteredString(
            minecraft.fontRendererObj,
            description,
            xPosition + width / 2,
            yPosition + height + 2,
            ColorCode.GRAY.getColor()
        )

        GlStateManager.color(1f, 1f, 1f, 1f)

        // Arrow buttons are square so width = height
        minecraft.textureManager.bindTexture(ARROW_LEFT)
        drawModalRectWithCustomSizedTexture(
            xPosition,
            yPosition,
            0f,
            0f,
            height,
            height,
            height.toFloat(),
            height.toFloat()
        )

        minecraft.textureManager.bindTexture(ARROW_RIGHT)
        drawModalRectWithCustomSizedTexture(
            endX - height,
            yPosition,
            0f,
            0f,
            height,
            height,
            height.toFloat(),
            height.toFloat()
        )

        if (name != trimmedName) {
            if (isOverText(mouseX, mouseY)) {
                // draw tooltip next to the cursor showing the full title
                val stringWidth = minecraft.fontRendererObj.getStringWidth(name)
                val rectLeft = mouseX + 3
                val rectTop = mouseY + 3
                val rectRight = rectLeft + stringWidth + 8
                val rectBottom = rectTop + 12
                drawRect(rectLeft, rectTop, rectRight, rectBottom, ColorCode.BLACK.getColor())
                minecraft.fontRendererObj.drawString(name, rectLeft + 4, rectTop + 2, ColorCode.WHITE.getColor())
            }
        }
    }

    override fun mousePressed(minecraft: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        if (isOverLeftButton(mouseX, mouseY)) {
            index = if (index == itemList.size - 1) 0 else index + 1
            notifyCallback(index)
            return true
        }
        if (isOverRightButton(mouseX, mouseY)) {
            index = if (index == 0) itemList.size - 1 else index - 1
            notifyCallback(index)
            return true
        }
        return false
    }

    /**
     * Notifies the callback - if it's not null - that the given index was selected.
     *
     * @param index Selected index
     */
    private fun notifyCallback(index: Int) {
        callback?.onItemSelected(index)
    }

    private fun isOverText(mouseX: Int, mouseY: Int): Boolean {
        return mouseX > xPosition + height && mouseX < xPosition + width - height && mouseY > yPosition && mouseY < yPosition + height
    }

    /**
     * @return Whether the the given mouse position is hovering over the left arrow button
     */
    private fun isOverLeftButton(mouseX: Int, mouseY: Int): Boolean {
        return mouseX > xPosition && mouseX < xPosition + height && mouseY > yPosition && mouseY < yPosition + height
    }

    /**
     * @return Whether the the given mouse position is hovering over the right arrow button
     */
    private fun isOverRightButton(mouseX: Int, mouseY: Int): Boolean {
        return mouseX > xPosition + width - height && mouseX < xPosition + width && mouseY > yPosition && mouseY < yPosition + height
    }

    /**
     * Replaces the last character in the given string with the ellipses character `…`
     *
     * @param text Text to ellipsize
     * @return Input text with … at the end
     */
    private fun ellipsize(text: String): String {
        return StringBuilder(text)
            .replace(text.length - 1, text.length, "…")
            .toString()
    }

    companion object {
        val ARROW_LEFT = ResourceLocation("skyblockaddons", "gui/flatarrowleft.png")
        val ARROW_RIGHT = ResourceLocation("skyblockaddons", "gui/flatarrowright.png")
    }
}