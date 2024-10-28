package moe.ruruke.skyblock.utils

import moe.ruruke.skyblock.SkyblockAddonsPlus
import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge


class Utils {

    private var main = SkyblockAddonsPlus.instance;
    private val MESSAGE_PREFIX: String = ("§"+ColorCode.GRAY.code + "[" + ColorCode.AQUA + SkyblockAddonsPlus.NAME + ColorCode.GRAY) + "] "
    private val MESSAGE_PREFIX_SHORT: String =
        (("§"+ColorCode.GRAY.code + "[" + ColorCode.AQUA) + "SBA" + ColorCode.GRAY) + "] " + ColorCode.RESET
    fun sendMessage(text: String, prefix: Boolean) {
        val event = ClientChatReceivedEvent(1.toByte(), ChatComponentText((if (prefix) MESSAGE_PREFIX else "") + text))
        MinecraftForge.EVENT_BUS.post(event) // Let other mods pick up the new message
        if (!event.isCanceled) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(event.message) // Just for logs
        }
    }

    fun sendMessage(text: String) {
        sendMessage(text, true)
    }

    fun sendMessage(text: ChatComponentText?, prefix: Boolean) {
        var text = text
        if (prefix) { // Add the prefix in front.
            val newText = ChatComponentText(MESSAGE_PREFIX)
            newText.appendSibling(text)
            text = newText
        }

        val event = ClientChatReceivedEvent(1.toByte(), text)
        MinecraftForge.EVENT_BUS.post(event) // Let other mods pick up the new message
        if (!event.isCanceled) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(event.message) // Just for logs
        }
    }
}