package moe.ruruke.skyblock.utils

import com.google.common.collect.Sets
import moe.ruruke.skyblock.SkyblockAddonsPlus
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.util.ChatComponentText
import net.minecraft.world.WorldSettings
import net.minecraft.world.WorldType
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import java.util.regex.Pattern


class Utils {

    private var main = SkyblockAddonsPlus.instance;
    private val MESSAGE_PREFIX: String = ("§"+ColorCode.GRAY.code + "[" + ColorCode.AQUA + SkyblockAddonsPlus.NAME + ColorCode.GRAY) + "] "
    private val MESSAGE_PREFIX_SHORT: String =
        (("§"+ColorCode.GRAY.code + "[" + ColorCode.AQUA) + "SBA" + ColorCode.GRAY) + "] " + ColorCode.RESET


    /**
     * "Skyblock" as shown on the scoreboard title in English, Chinese Simplified, Traditional Chinese.
     */
    private val SKYBLOCK_IN_ALL_LANGUAGES: Set<String> =
        Sets.newHashSet("SKYBLOCK", "\u7A7A\u5C9B\u751F\u5B58", "\u7A7A\u5CF6\u751F\u5B58")

    /**
     * Matches the server ID (mini##/Mega##) line on the Skyblock scoreboard
     */
    private val SERVER_REGEX: Pattern = Pattern.compile("(?<serverType>[Mm])(?<serverCode>[0-9]+[A-Z])$")
    /**
     * Matches the coins balance (purse/piggy bank) line on the Skyblock scoreboard
     */
    private val PURSE_REGEX: Pattern = Pattern.compile("(?:Purse|Piggy): (?<coins>[0-9.,]*)")
    /**
     * Matches the bits balance line on the Skyblock scoreboard
     */
    private val BITS_REGEX: Pattern = Pattern.compile("Bits: (?<bits>[0-9,]*)")
    /**
     * Matches the active slayer quest type line on the Skyblock scoreboard
     */
    private val SLAYER_TYPE_REGEX: Pattern =
        Pattern.compile("(?<type>Tarantula Broodfather|Revenant Horror|Sven Packmaster|Voidgloom Seraph) (?<level>[IV]+)")
    /**
     * Matches the active slayer quest progress line on the Skyblock scoreboard
     */
    private val SLAYER_PROGRESS_REGEX: Pattern =
        Pattern.compile("(?<progress>[0-9.k]*)/(?<total>[0-9.k]*) (?:Kills|Combat XP)$")

    /**
     * A dummy world object used for spawning fake entities for GUI features without affecting the actual world
     */
    private val DUMMY_WORLD: WorldClient = WorldClient(
        null, WorldSettings(
            0L, WorldSettings.GameType.SURVIVAL,
            false, false, WorldType.DEFAULT
        ), 0, null, null
    )


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

    //TODO: まだ移植できない。
//    fun isOnHypiel():  Boolean {
//        var player: EntityPlayerSP = Minecraft.getMinecraft().thePlayer as EntityPlayerSP ?: return false;
//        val brand = player.clientBrand;
//        if(brand != null) {
//            for (p in main.onlineData.getHypixelBrands()) {
//                if (p.matcher(brand).matches()) {
//                    return true
//                }
//            }
//        }
//    }
}