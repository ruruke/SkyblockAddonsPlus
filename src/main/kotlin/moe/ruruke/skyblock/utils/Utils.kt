package moe.ruruke.skyblock.utils

import com.google.common.collect.Sets
import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Attribute
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.Location
import moe.ruruke.skyblock.core.SkyblockDate
import moe.ruruke.skyblock.events.SkyblockJoinedEvent
import moe.ruruke.skyblock.events.SkyblockLeftEvent
import moe.ruruke.skyblock.features.itemdrops.ItemDropChecker
import moe.ruruke.skyblock.misc.scheduler.Scheduler
import moe.ruruke.skyblock.utils.RomanNumeralParser.parseNumeral
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.Item
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemStack
import net.minecraft.util.ChatComponentText
import net.minecraft.util.MathHelper
import net.minecraft.util.Vector3d
import net.minecraft.world.WorldSettings
import net.minecraft.world.WorldType
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Loader
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.mutable.MutableFloat
import org.apache.commons.lang3.text.WordUtils
import org.apache.logging.log4j.Logger
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Matrix4f
import java.awt.Color
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.text.ParseException
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.sqrt

class Utils {
    /**
     * Get a player's attributes. This includes health, mana, and defence.
     */
    private var attributes: MutableMap<Attribute, MutableFloat> = mutableMapOf()
    fun getAttributes(): MutableMap<Attribute, MutableFloat> {
        return attributes
    }
    fun setAttributes(attributes: MutableMap<Attribute, MutableFloat>){
        this.attributes = attributes
    }

    /**
     * This is the item checker that makes sure items being dropped or sold are allowed to be dropped or sold.
     */
    private val itemDropChecker: ItemDropChecker = ItemDropChecker()
    fun getItemDropChecker(): ItemDropChecker{
        return itemDropChecker
    }

    /**
     * List of reforges that the player is looking to find.
     */
    private var reforgeMatches: List<String> = LinkedList()
    fun getReforgeMatches(): List<String> {
        return reforgeMatches
    }
    fun setReforgeMatches(reforgeMatches: List<String>){
        this.reforgeMatches = reforgeMatches
    }

    /**
     * List of reforge substrings that the player doesn't want to match.
     */
    private var reforgeExclusions: List<String> = LinkedList()
    fun getReforgeExclusions(): List<String> {return reforgeExclusions}
    fun setReforgeExclusions(reforgeExclusions: List<String>) {
        this.reforgeExclusions = reforgeExclusions
    }

    /**
     * Whether the player is on skyblock.
     */
    private var onSkyblock = false
        get() = field

    /**
     * The player's current location in Skyblock
     */
    private var location: Location = Location.UNKNOWN

    fun getLocation(): Location {
        if (main.config!!.forceLocation != 0){
            return when(main.config!!.forceLocation) {
                1 -> Location.ISLAND
                2 -> Location.DRAGONS_NEST
                3 -> Location.CRIMSON_ISLE
                else -> Location.UNKNOWN
            }
        }
        return location;
    }

    /**
     * The skyblock profile that the player is currently on. Ex. "Grapefruit"
     */
    private var profileName = "Unknown"
    fun setProfileName(name: String){
        main.utils!!.sendMessage("Debug > setProfineName > "+name)
        profileName = name
    }

    public fun getProfileName(): String {
        return profileName
    }

    /**
     * Whether a loud sound is being played by the mod.
     */
    private var playingSound = false
    fun isPlayingSound(): Boolean {
        return playingSound
    }
    fun setPlayingSound(playingSound: Boolean) {
        this.playingSound = playingSound
    }

    /**
     * The current serverID that the player is on.
     */
    private var serverID = ""
        get() = field
        set(value) {
            field = value
        }
    private var lastHoveredSlot = -1
    fun setLastHoveredSlot(lastHoveredSlot: Int) {
        this.lastHoveredSlot = lastHoveredSlot;
    }
    fun getLastHoveredSlot(): Int{
        return this.lastHoveredSlot
    }

    /**
     * Whether the player is using the FSR container preview
     */
    private val usingFSRcontainerPreviewTexture = false
        get() = field
    fun isUsingFSRcontainerPreviewTexture(): Boolean{
        return usingFSRcontainerPreviewTexture
    }

    private var currentDate: SkyblockDate = SkyblockDate(SkyblockDate.SkyblockMonth.EARLY_WINTER, 1, 1, 1, "am")
        get() = field
        set(value) {
            field = value
        }
    private var purse = 0.0
        get() = field
        set(value) {
            field = value
        }
    private var bits = 0.0
        get() = field
        set(value) {
            field = value
        }
    private var jerryWave = -1
        get() = field
        set(value) {
            field = value
        }

    private var alpha = false
        get() = field
        set(value) {
            field = value
        }
    private var inDungeon = false
    fun isInDungeon(): Boolean
    {
        return inDungeon
    }

    private var fadingIn = false
        get() = field
    fun isFadingIn(): Boolean{
        return fadingIn
    }
    fun setFadingIn(bool: Boolean) {
        fadingIn = bool
    }

    private var slayerQuest: EnumUtils.SlayerQuest? = null
        get() = field
        set(value) {
            field = value
        }
    private var slayerQuestLevel = 1
        get() = field
        set(value) {
            field = value
        }
    private var slayerBossAlive = false
        get() = field
        set(value) {
            field = value
        }

    private fun addDefaultStats() {
        for (attribute in Attribute.entries) {
            attributes[attribute] = MutableFloat(attribute.getDefaultValue())
        }
    }

    @JvmOverloads
    fun sendMessage(text: String, prefix: Boolean = true) {
        val event = ClientChatReceivedEvent(1.toByte(), ChatComponentText((if (prefix) MESSAGE_PREFIX else "") + text))
        MinecraftForge.EVENT_BUS.post(event) // Let other mods pick up the new message
        if (!event.isCanceled) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(event.message) // Just for logs
        }
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

    fun sendErrorMessage(errorText: String) {
        sendMessage(ColorCode.RED.toString() + "Error: " + errorText)
    }

    val isOnHypixel: Boolean
        /**
         * Checks if the player is on the Hypixel Network
         *
         * @return `true` if the player is on Hypixel, `false` otherwise
         */
        get() {
            if (main.config!!.forceOnSkyblock) return true
            val player = Minecraft.getMinecraft().thePlayer ?: return false
            val brand = player.clientBrand
            if (brand != null) {
                for (p in main.getOnlineData()!!.getHypixelBrands()!!) {
                    if (p.matcher(brand).matches()) {
                        return true
                    }
                }
            }
            return false
        }

    fun setOnSkyblock(v: Boolean){
        onSkyblock = v
    }
    fun isOnSkyblock(): Boolean {
        //TODO: DEBUG
//        return true
        if(main.config!!.forceOnSkyblock){
            return true
        }
        //TODO:
        return onSkyblock
    }
    fun parseSidebar() {
        var foundScoreboard = false

        var foundActiveSoup = false
        var foundLocation = false
        var foundJerryWave = false
        var foundAlphaIP = false
        var foundInDungeon = false
        var foundSlayerQuest = false
        var foundBossAlive = false
        var foundSkyblockTitle = false

        // TODO: This can be optimized more.
        if (isOnHypixel && ScoreboardManager.hasScoreboard()) {
            foundScoreboard = true

            // Check title for skyblock
            val strippedScoreboardTitle: String = ScoreboardManager.getStrippedScoreboardTitle()!!
            for (skyblock in SKYBLOCK_IN_ALL_LANGUAGES) {
                if (strippedScoreboardTitle.startsWith(skyblock)) {
                    foundSkyblockTitle = true
                    break
                }
            }

            if (foundSkyblockTitle) {
                // If it's a Skyblock scoreboard and the player has not joined Skyblock yet,
                // this indicates that they did so.
                if (!isOnSkyblock()) {
                    MinecraftForge.EVENT_BUS.post(SkyblockJoinedEvent())
                }

                // If the title line ends with "GUEST", then the player is visiting someone else's island.
                if (strippedScoreboardTitle.endsWith("GUEST")) {
                    location = Location.GUEST_ISLAND
                    foundLocation = true
                }

                var timeString: String? = null
                var dateString: String? = null

                for (lineNumber in 0 until ScoreboardManager.getNumberOfLines()) {
                    val scoreboardLine: String = ScoreboardManager.getScoreboardLines()!!.get(lineNumber)
                    val strippedScoreboardLine: String = ScoreboardManager.getStrippedScoreboardLines()!!.get(lineNumber)
                    var matcher: Matcher

                    when (lineNumber) {
                        0 -> {
                            // Server ID
                            matcher = SERVER_REGEX.matcher(strippedScoreboardLine)

                            if (matcher.find()) {
                                val serverType = matcher.group("serverType")
                                if (serverType == "m") {
                                    serverID = "mini" + matcher.group("serverCode")
                                } else if (serverType == "M") {
                                    serverID = "mega" + matcher.group("serverCode")
                                }
                            }
                        }

                        1 -> {}
                        2 ->                             // Date
                            dateString = strippedScoreboardLine

                        3 ->                             // Time
                            timeString = strippedScoreboardLine

                        4 ->                             // Location
                            if (!foundLocation) {
                                // Catacombs contains the floor number so it's a special case...
                                if (strippedScoreboardLine.contains(Location.THE_CATACOMBS.getScoreboardName())) {
                                    location = Location.THE_CATACOMBS
                                    foundLocation = true
                                } else {
                                    for (loopLocation in Location.values()) {
                                        if (strippedScoreboardLine.endsWith(loopLocation.getScoreboardName())) {
                                            // TODO: Special case causes Dwarven Village to map to Village since endsWith...idk if
                                            //  changing to "equals" will mess it up for other locations
                                            if (loopLocation === Location.VILLAGE && strippedScoreboardLine.contains("Dwarven")) {
                                                continue
                                            }
                                            location = loopLocation
                                            foundLocation = true
                                            break
                                        }
                                    }
                                }
                            }

                        5 -> {}
                        6 ->                             /*
                             If the player has Mushroom Soup active, this line will show the remaining duration.
                             This shifts the following lines down.
                             */
                            if (strippedScoreboardLine.startsWith("Flight")) {
                                foundActiveSoup = true
                            } else {
                                parseCoins(strippedScoreboardLine)
                            }

                        7 -> if (foundActiveSoup) {
                            parseCoins(strippedScoreboardLine)
                        } else {
                            parseBits(strippedScoreboardLine)
                        }

                        8 -> if (foundActiveSoup) {
                            parseBits(strippedScoreboardLine)
                        }
                    }

                    if (strippedScoreboardLine.endsWith("Combat XP") || strippedScoreboardLine.endsWith("Kills")) {
                        parseSlayerProgress(strippedScoreboardLine)
                    }

                    if (!foundJerryWave && (location === Location.JERRYS_WORKSHOP || location === Location.JERRY_POND)) {
                        if (strippedScoreboardLine.startsWith("Wave")) {
                            foundJerryWave = true
                            var newJerryWave = try {
                                TextUtils.keepIntegerCharactersOnly(strippedScoreboardLine).toInt()
                            } catch (ignored: NumberFormatException) {
                                0
                            }
                            if (jerryWave != newJerryWave) {
                                jerryWave = newJerryWave
                            }
                        }
                    }

                    //TODO:
//                    if (!foundInDungeon && strippedScoreboardLine.startsWith("Cleared: ")) {
//                        foundInDungeon = true
//                        inDungeon = true
//
//                        val lastServer: String = main.getDungeonManager().getLastServerId()
//                        if (lastServer != null && lastServer != serverID) {
//                            main.getDungeonManager().reset()
//                        }
//                        main.getDungeonManager().setLastServerId(serverID)
//                    }

                    matcher = SLAYER_TYPE_REGEX.matcher(strippedScoreboardLine)
                    if (matcher.matches()) {
                        val type = matcher.group("type")
                        val levelRomanNumeral = matcher.group("level")

                        val detectedSlayerQuest: EnumUtils.SlayerQuest = EnumUtils.SlayerQuest.fromName(type)!!
                        if (detectedSlayerQuest != null) {
                            try {
                                val level = parseNumeral(levelRomanNumeral)
                                slayerQuest = detectedSlayerQuest
                                slayerQuestLevel = level
                                foundSlayerQuest = true
                            } catch (ex: IllegalArgumentException) {
                                logger.error("Failed to parse slayer level (" + ex.message + ")", ex)
                            }
                        }
                    }

                    if (strippedScoreboardLine == "Slay the boss!") {
                        foundBossAlive = true
                        slayerBossAlive = true
                    }

                    if (inDungeon) {
                        //TODO:
//                        try {
//                            main.getDungeonManager().updateDungeonPlayer(scoreboardLine)
//                        } catch (ex: NumberFormatException) {
//                            logger.error("Failed to update a dungeon player from the line $scoreboardLine.", ex)
//                        }
                    }

                    // Check if the player is on the Hypixel Alpha Network
                    if (lineNumber == ScoreboardManager.getNumberOfLines() - 1 && !foundAlphaIP && strippedScoreboardLine.contains(
                            "alpha.hypixel.net"
                        )
                    ) {
                        foundAlphaIP = true
                        alpha = true
                        profileName = "Alpha"
                    }
                }

                currentDate = SkyblockDate.parse(dateString, timeString)!!
            }
            if (!foundLocation) {
                location = Location.UNKNOWN
            }
            if (!foundJerryWave) {
                jerryWave = -1
            }
            if (!foundAlphaIP) {
                alpha = false
            }
            if (!foundInDungeon) {
                inDungeon = false
            }
            if (!foundSlayerQuest) {
                slayerQuestLevel = 1
                slayerQuest = null
            }
            if (!foundBossAlive) {
                slayerBossAlive = false
            }
        }

        // If it's not a Skyblock scoreboard, the player must have left Skyblock and
        // be in some other Hypixel lobby or game.
        if (!foundSkyblockTitle && this.isOnSkyblock()) {
            // Check if we found a scoreboard in general. If not, its possible they are switching worlds.
            // If we don't find a scoreboard for 10s, then we know they actually left the server.

            if (foundScoreboard || System.currentTimeMillis() - ScoreboardManager.getLastFoundScoreboard() > 10000) {
                MinecraftForge.EVENT_BUS.post(SkyblockLeftEvent())
            }
        }
    }

    private var triggeredSlayerWarning = false
    private var lastCompletion = 0f

    private fun parseSlayerProgress(line: String) {
        if (!main.configValues!!.isEnabled(Feature.BOSS_APPROACH_ALERT)) return

        val matcher = SLAYER_PROGRESS_REGEX.matcher(line)
        if (matcher.find()) {
            val progressString = matcher.group("progress")
            val totalString = matcher.group("total")

            var progress: Float = TextUtils.keepFloatCharactersOnly(progressString).toFloat()
            var total: Float = TextUtils.keepFloatCharactersOnly(totalString).toFloat()

            if (progressString.contains("k")) progress *= 1000f
            if (totalString.contains("k")) total *= 1000f

            val completion = progress / total

            if (completion > 0.85) {
                if (!triggeredSlayerWarning || (main.configValues!!
                        .isEnabled(Feature.REPEAT_SLAYER_BOSS_WARNING) && completion != lastCompletion)
                ) {
                    triggeredSlayerWarning = true
                    main.utils!!.playLoudSound("random.orb", 0.5)
                    //TODO:
                    //main.getRenderListener().setTitleFeature(Feature.BOSS_APPROACH_ALERT)
                    main.scheduler!!.schedule(Scheduler.CommandType.RESET_TITLE_FEATURE, main.configValues!!.getWarningSeconds())
                }
            } else {
                triggeredSlayerWarning =
                    false // Reset warning flag when completion is below 85%, meaning they started a new quest.
            }

            lastCompletion = completion
        }
    }

    private fun onCoinsChange(coinsChange: Double) {
    }

    fun getDefaultColor(alphaFloat: Float): Int {
        val alpha = alphaFloat.toInt()
        return Color(150, 236, 255, alpha).rgb
    }

    /**
     * When you use this function, any sound played will bypass the player's
     * volume setting, so make sure to only use this for like warnings or stuff like that.
     */
    fun playLoudSound(sound: String?, pitch: Double) {
        playingSound = true
        Minecraft.getMinecraft().thePlayer.playSound(sound, 1f, pitch.toFloat())
        playingSound = false
    }

    /**
     * This one plays the sound normally. See [Utils.playLoudSound] for playing
     * a sound that bypasses the user's volume settings.
     */
    fun playSound(sound: String?, pitch: Double) {
        Minecraft.getMinecraft().thePlayer.playSound(sound, 1f, pitch.toFloat())
    }

    fun playSound(sound: String?, volume: Double, pitch: Double) {
        Minecraft.getMinecraft().thePlayer.playSound(sound, volume.toFloat(), pitch.toFloat())
    }

    /**
     * Checks if the given reforge is similar to any reforges on the desired/exclusions lists from the reforge filter feature.
     *
     * @param reforge the reforge to check
     * @return `true` if the given reforge is similar to a desired reforge and dissimilar to all excluded reforges,
     * `false` otherwise
     */
    fun enchantReforgeMatches(reforge: String): Boolean {
        var reforge = reforge
        reforge = reforge.trim { it <= ' ' }.lowercase()
        for (desiredReforge in reforgeMatches) {
            var desiredReforge = desiredReforge
            desiredReforge = desiredReforge.trim { it <= ' ' }.lowercase()
            if (StringUtils.isNotEmpty(desiredReforge) && reforge.contains(desiredReforge)) {
                var foundExclusion = false
                for (excludedReforge in reforgeExclusions) {
                    var excludedReforge = excludedReforge
                    excludedReforge = excludedReforge.trim { it <= ' ' }.lowercase()
                    if (StringUtils.isNotEmpty(excludedReforge) && reforge.contains(excludedReforge)) {
                        foundExclusion = true
                        break
                    }
                }
                if (!foundExclusion) {
                    return true
                }
            }
        }
        return false
    }

    val sBAFolder: File
        /**
         * Returns the folder that SkyblockAddons is located in.
         *
         * @return the folder the SkyblockAddons jar is located in
         */
        get() = Loader.instance().activeModContainer().source.parentFile

    /**
     * Checks if it is currently Halloween according to the system calendar.
     *
     * @return `true` if it is Halloween, `false` otherwise
     */
    fun isHalloween(): Boolean {
        val calendar = Calendar.getInstance()
        return calendar[Calendar.MONTH] === Calendar.OCTOBER && calendar[Calendar.DAY_OF_MONTH] === 31
    }
    fun getDefaultBlue(alpha: Int): Int {
        return Color(160, 225, 229, alpha).rgb
    }

    fun normalizeValueNoStep(value: Float, min: Float, max: Float): Float {
        return MathHelper.clamp_float((snapNearDefaultValue(value) - min) / (max - min), 0.0f, 1.0f)
    }

    /**
     * Rounds the given value to 1f if it is between 0.95f and 1.05f exclusive.
     *
     * @param value the value to round
     * @return 1f if 0.95f > `value` > 1.05f or `value` otherwise
     */
    fun snapNearDefaultValue(value: Float): Float {
        if (value != 1f && value > 1 - 0.05 && value < 1 + 0.05) {
            return 1f
        }

        return value
    }

    fun wrapSplitText(text: String?, wrapLength: Int): Array<String> {
        return WordUtils.wrap(text, wrapLength).replace("\r", "").split(Pattern.quote("\n").toRegex())
            .dropLastWhile { it.isEmpty() }.toTypedArray()
    }

    fun itemIsInHotbar(itemStack: ItemStack): Boolean {
        val inventory = Minecraft.getMinecraft().thePlayer.inventory.mainInventory

        for (slot in 0..8) {
            if (inventory[slot] == itemStack) {
                return true
            }
        }
        return false
    }

    fun isAxe(item: Item?): Boolean {
        return item is ItemAxe
    }

    private var depthEnabled = false
    private var blendEnabled = false
    private var alphaEnabled = false
    private var blendFunctionSrcFactor = 0
    private var blendFunctionDstFactor = 0

    fun enableStandardGLOptions() {
        depthEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST)
        blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND)
        alphaEnabled = GL11.glIsEnabled(GL11.GL_ALPHA_TEST)
        blendFunctionSrcFactor = GL11.glGetInteger(GL11.GL_BLEND_SRC)
        blendFunctionDstFactor = GL11.glGetInteger(GL11.GL_BLEND_DST)

        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.enableAlpha()
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    fun restoreGLOptions() {
        if (depthEnabled) {
            GlStateManager.enableDepth()
        }
        if (!alphaEnabled) {
            GlStateManager.disableAlpha()
        }
        if (!blendEnabled) {
            GlStateManager.disableBlend()
        }
        GlStateManager.blendFunc(blendFunctionSrcFactor, blendFunctionDstFactor)
    }

    fun isModLoaded(modId: String): Boolean {
        return isModLoaded(modId, null)
    }

    /**
     * Check if another mod is loaded.
     *
     * @param modId   The modid to check.
     * @param version The version of the mod to match (optional).
     */
    fun isModLoaded(modId: String, version: String?): Boolean {
        val isLoaded = Loader.isModLoaded(modId) // Check for the modid...

        if (isLoaded && version != null) { // Check for the specific version...
            for (modContainer in Loader.instance().modList) {
                if (modContainer.modId == modId && modContainer.version == version) {
                    return true
                }
            }

            return false
        }

        return isLoaded
    }

    val currentGLTransformations: FloatArray
        get() {
            val buf = BufferUtils.createFloatBuffer(16)
            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buf)
            buf.rewind()
            val mat = Matrix4f()
            mat.load(buf)

            val x = mat.m30
            val y = mat.m31
            val z = mat.m32

            val scale = sqrt((mat.m00 * mat.m00 + mat.m01 * mat.m01 + mat.m02 * mat.m02).toDouble()) as Float

            return floatArrayOf(x, y, z, scale)
        }

    fun isEmptyGlassPane(itemStack: ItemStack?): Boolean {
        return itemStack != null && (itemStack.item === Item.getItemFromBlock(Blocks.stained_glass_pane)
                || itemStack.item === Item.getItemFromBlock(Blocks.glass_pane)) && itemStack.hasDisplayName() && TextUtils.stripColor(
            itemStack.displayName.trim { it <= ' ' }).isEmpty()
    }

    fun isGlassPaneColor(itemStack: ItemStack?, color: EnumDyeColor): Boolean {
        return itemStack != null && itemStack.metadata == color.metadata
    }

    init {
        addDefaultStats()
    }

    /**
     * Parses the player's coins balance from a given scoreboard line. The balance will be set to 0 if parsing fails.
     *
     * @param strippedScoreboardLine the scoreboard line (without formatting codes) to parse coins from
     */
    private fun parseCoins(strippedScoreboardLine: String) {
        // The player's coins balance
        val matcher = PURSE_REGEX.matcher(strippedScoreboardLine)

        if (matcher.matches()) {
            try {
                val oldCoins = purse
                purse = TextUtils.NUMBER_FORMAT.parse(matcher.group("coins")).toDouble()

                if (oldCoins != purse) {
                    onCoinsChange(purse - oldCoins)
                }
            } catch (e: NumberFormatException) {
                purse = 0.0
            } catch (e: ParseException) {
                purse = 0.0
            }
        }
    }

    /**
     * Parses the player's bits balance from a given scoreboard line. The balance will be set to 0 if parsing fails.
     *
     * @param strippedScoreboardLine the scoreboard line (without formatting codes) to parse bits from
     */
    private fun parseBits(strippedScoreboardLine: String) {
        // If the line is empty, the player has no bits.
        if (strippedScoreboardLine.isEmpty()) {
            bits = 0.0
            return
        }

        val matcher = BITS_REGEX.matcher(strippedScoreboardLine)

        if (matcher.matches()) {
            bits = try {
                TextUtils.NUMBER_FORMAT.parse(matcher.group("bits")).toDouble()
            } catch (ignored: ParseException) {
                0.0
            }
        }
    }

    companion object {
        private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        private val logger: Logger = SkyblockAddonsPlus.getLogger()

        /**
         * Added to the beginning of messages sent by the mod.
         */
        val MESSAGE_PREFIX: String =
            (ColorCode.GRAY.toString() + "[" + ColorCode.AQUA + SkyblockAddonsPlus.NAME + ColorCode.GRAY).toString() + "] "
        val MESSAGE_PREFIX_SHORT: String =
            ColorCode.GRAY.toString() + "[" + ColorCode.AQUA + "SBA" + ColorCode.GRAY + "] " + ColorCode.RESET

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
        val dummyWorld: WorldClient = WorldClient(
            null, WorldSettings(
                0L, WorldSettings.GameType.SURVIVAL,
                false, false, WorldType.DEFAULT
            ), 0, null, null
        )

        /**
         * Used for web requests.
         */
        val USER_AGENT: String = "SkyblockAddons/" + SkyblockAddonsPlus.VERSION

        // I know this is messy af, but frustration led me to take this dark path - said someone not biscuit
        var blockNextClick: Boolean = false

        /**
         * Rounds a float value for when it is being displayed as a string.
         *
         *
         * For example, if the given value is 123.456789 and the decimal places is 2, this will round
         * to 1.23.
         *
         * @param value         The value to round
         * @param decimalPlaces The decimal places to round to
         * @return A string representation of the value rounded
         */
        fun roundForString(value: Float, decimalPlaces: Int): String {
            return String.format("%." + decimalPlaces + "f", value)
        }

        fun getPlayerFromName(name: String?): EntityPlayer {
            return Minecraft.getMinecraft().theWorld.getPlayerEntityByName(name)
        }



        fun getPartialTicks(): Float {
            return main.getTimer(); //なんか動いてる:thinking:
        }

        fun getCurrentTick(): Long {
            return SkyblockAddonsPlus.instance.newScheduler!!.totalTicks
        }

        private val interpolatedPlayerPosition = Vector3d()
        private var lastTick: Long = 0
        private var lastPartialTicks = 0f

        fun getPlayerViewPosition(): Vector3d {
            val currentTick = getCurrentTick()
            val currentPartialTicks = getPartialTicks()

            if (currentTick != lastTick || currentPartialTicks != lastPartialTicks) {
                val renderViewEntity = Minecraft.getMinecraft().renderViewEntity
                interpolatedPlayerPosition.x = MathUtils.interpolateX(renderViewEntity, currentPartialTicks)
                interpolatedPlayerPosition.y = MathUtils.interpolateY(renderViewEntity, currentPartialTicks)
                interpolatedPlayerPosition.z = MathUtils.interpolateZ(renderViewEntity, currentPartialTicks)

                lastTick = currentTick
                lastPartialTicks = currentPartialTicks
            }

            return interpolatedPlayerPosition
        }

        @Throws(IOException::class)
        fun toByteArray(inputStream: BufferedInputStream): ByteArray {
            val bytes: ByteArray
            try {
                bytes = IOUtils.toByteArray(inputStream)
            } finally {
                inputStream.close()
            }
            return bytes
        }

        fun getEntityByUUID(uuid: UUID?): Entity? {
            if (uuid == null) {
                return null
            }

            for (entity in Minecraft.getMinecraft().theWorld.loadedEntityList) {
                if (entity.uniqueID == uuid) {
                    return entity
                }
            }

            return null
        }

        fun getBlockMetaId(block: Block, meta: Int): Int {
            return Block.getStateId(block.getStateFromMeta(meta))
        }
    }
}