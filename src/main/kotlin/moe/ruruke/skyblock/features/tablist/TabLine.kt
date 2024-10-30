package moe.ruruke.skyblock.features.tablist

import net.minecraft.client.Minecraft




class TabLine(private val text: String?, private val type: TabStringType) {
    fun getText(): String?
    {
        return text
    }
    fun getType(): TabStringType{
        return type
    }
    fun getWidth(): Int {
        val mc = Minecraft.getMinecraft()

        var width = mc.fontRendererObj.getStringWidth(text)

        if (type == TabStringType.PLAYER) {
            width += 8 + 2 // Player head
        }

        if (type == TabStringType.TEXT) {
            width += 4 // Space is 4
        }

        return width
    }
}
