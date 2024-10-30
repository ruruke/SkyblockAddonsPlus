package moe.ruruke.skyblock.utils



import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.getLogger
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.instance
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.persistentValuesManager
import moe.ruruke.skyblock.core.Translations.getMessage
import moe.ruruke.skyblock.utils.data.DataUtils.readLocalAndFetchOnline
import net.minecraft.block.Block
import net.minecraft.block.properties.IProperty
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.resources.SimpleReloadableResourceManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityList
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.*
import net.minecraft.scoreboard.Score
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.StringUtils
import net.minecraft.world.WorldType
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fml.client.FMLClientHandler
import org.lwjgl.input.Keyboard
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors
import kotlin.math.max

/**
 * This is a class of utilities for SkyblockAddons developers.
 *
 * @author ILikePlayingGames
 * @version 2.3
 */
object DevUtils {
    //TODO: Add options to enter custom action bar messages to test ActionBarParser
    //      Add an option to log changed action bar messages only to reduce log spam
    private val mc: Minecraft = Minecraft.getMinecraft()
    private val main = instance
    private val logger = getLogger()

    /** Pattern used for removing the placeholder emoji player names from the Hypixel scoreboard  */
    val SIDEBAR_PLAYER_NAME_PATTERN: Pattern =
        Pattern.compile("[\uD83D\uDD2B\uD83C\uDF6B\uD83D\uDCA3\uD83D\uDC7D\uD83D\uDD2E\uD83D\uDC0D\uD83D\uDC7E\uD83C\uDF20\uD83C\uDF6D\u26BD\uD83C\uDFC0\uD83D\uDC79\uD83C\uDF81\uD83C\uDF89\uD83C\uDF82]+")

    /** All possible Minecraft entity names, for tab completion  */
    val ALL_ENTITY_NAMES: MutableList<String> = EntityList.getEntityNameList()

    // If you change this, please change it in the string "commands.usage.sba.help.copyEntity" as well.
    const val DEFAULT_ENTITY_COPY_RADIUS: Int = 3
    private val DEFAULT_ENTITY_NAMES = listOf<Class<out Entity>>(
        EntityLivingBase::class.java
    )
    private const val DEFAULT_SIDEBAR_FORMATTED = false

    
    
    private val loggingActionBarMessages = false
    private var copyMode = CopyMode.ENTITY
    private var entityNames = DEFAULT_ENTITY_NAMES
    private var entityCopyRadius = DEFAULT_ENTITY_COPY_RADIUS
    private var sidebarFormatted = DEFAULT_SIDEBAR_FORMATTED

    init {
        ALL_ENTITY_NAMES.add("PlayerSP")
        ALL_ENTITY_NAMES.add("PlayerMP")
        ALL_ENTITY_NAMES.add("OtherPlayerMP")
    }

    fun setSidebarFormatted(formatted: Boolean) {
        sidebarFormatted = formatted
    }

    fun resetSidebarFormattedToDefault() {
        sidebarFormatted = DEFAULT_SIDEBAR_FORMATTED
    }

    /**
     * Copies the objective and scores that are being displayed on a scoreboard's sidebar.
     * When copying the sidebar, the control codes (e.g. §a) are removed.
     */
    fun copyScoreboardSideBar() {
        copyScoreboardSidebar(sidebarFormatted)
    }

    /**
     * Copies the objective and scores that are being displayed on a scoreboard's sidebar.
     *
     * @param stripControlCodes if `true`, the control codes will be removed, otherwise they will be copied
     */
    private fun copyScoreboardSidebar(stripControlCodes: Boolean) {
        val scoreboard = mc.theWorld.scoreboard
        if (scoreboard == null) {
            main.utils!!.sendErrorMessage("Nothing is being displayed in the sidebar!")
            return
        }

        val sideBarObjective = scoreboard.getObjectiveInDisplaySlot(1)
        if (sideBarObjective == null) {
            main.utils!!.sendErrorMessage("Nothing is being displayed in the sidebar!")
            return
        }

        val stringBuilder = StringBuilder()

        var objectiveName = sideBarObjective.displayName
        var scores: List<Score>? = scoreboard.getSortedScores(sideBarObjective) as List<Score>

        if (scores == null || scores.isEmpty()) {
            main.utils!!.sendErrorMessage("No scores were found!")
            return
        }

        if (stripControlCodes) {
            objectiveName = StringUtils.stripControlCodes(objectiveName)
        }

        // Remove scores that aren't rendered.
        scores =
            scores.stream().filter { input: Score -> input.playerName != null && !input.playerName.startsWith("#") }
                .skip(max((scores.size - 15).toDouble(), 0.0).toLong()).collect(Collectors.toList())

        /*
        Minecraft renders the scoreboard from bottom to top so to keep the same order when writing it from top
        to bottom, we need to reverse the scores' order.
        */
        Collections.reverse(scores)

        stringBuilder.append(objectiveName).append("\n")

        for (score in scores) {
            val scoreplayerteam = scoreboard.getPlayersTeam(score.playerName)
            var playerName = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.playerName)

            // Strip colours and emoji player names.
            playerName = SIDEBAR_PLAYER_NAME_PATTERN.matcher(playerName).replaceAll("")

            if (stripControlCodes) {
                playerName = StringUtils.stripControlCodes(playerName)
            }

            val points = score.scorePoints

            stringBuilder.append(playerName).append("[").append(points).append("]").append("\n")
        }

        copyStringToClipboard(stringBuilder.toString(), ColorCode.GREEN.toString() + "Sidebar copied to clipboard!")
    }

    /**
     * Copies the NBT data of entities around the player. The classes of [Entity] to include and the radius
     * around the player to copy from can be customized.
     *
     * @param includedEntityClasses the classes of entities that should be included when copying NBT data
     * @param copyRadius copy the NBT data of entities inside this radius(in blocks) around the player
     */
    private fun copyEntityData(includedEntityClasses: List<Class<out Entity>>, copyRadius: Int) {
        val player = mc.thePlayer
        val loadedEntitiesCopy: List<Entity> = LinkedList(mc.theWorld.loadedEntityList)
        val stringBuilder = StringBuilder()

        val loadedEntitiesCopyIterator =
            loadedEntitiesCopy.listIterator()

        // Copy the NBT data from the loaded entities.
        while (loadedEntitiesCopyIterator.hasNext()) {
            val entity = loadedEntitiesCopyIterator.next()
            val entityData = NBTTagCompound()
            var isPartOfIncludedClasses = false

            // Checks to ignore entities if they're irrelevant
            if (entity.getDistanceToEntity(player) > copyRadius) {
                continue
            }

            for (entityClass in includedEntityClasses) {
                if (entityClass.isAssignableFrom(entity.javaClass)) {
                    isPartOfIncludedClasses = true
                }
            }

            if (!isPartOfIncludedClasses) {
                continue
            }

            entity.writeToNBT(entityData)

            // Add spacing before each new entry.
            if (stringBuilder.length > 0) {
                stringBuilder.append(System.lineSeparator()).append(System.lineSeparator())
            }

            stringBuilder.append("Class: ").append(entity.javaClass.simpleName).append(System.lineSeparator())
            if (entity.hasCustomName() || EntityPlayer::class.java.isAssignableFrom(entity.javaClass)) {
                stringBuilder.append("Name: ").append(entity.name).append(System.lineSeparator())
            }

            stringBuilder.append("NBT Data:").append(System.lineSeparator())
            stringBuilder.append(prettyPrintNBT(entityData))
        }

        if (stringBuilder.length > 0) {
            copyStringToClipboard(
                stringBuilder.toString(),
                ColorCode.GREEN.toString() + "Entity data was copied to clipboard!"
            )
        } else {
            main.utils!!.sendErrorMessage("No entities matching the given parameters were found.")
        }
    }

    fun setEntityNamesFromString(includedEntityNames: String) {
        val entityClasses = getEntityClassListFromString(includedEntityNames)
        if (entityClasses == null || entityClasses.isEmpty()) {
            main.utils!!.sendErrorMessage("The entity class list is not valid or is empty! Falling back to default.")
            resetEntityNamesToDefault()
        } else {
            entityNames = entityClasses
        }
    }

    fun setEntityCopyRadius(copyRadius: Int) {
        if (copyRadius <= 0) {
            main.utils!!
                .sendErrorMessage("Radius cannot be negative! Falling back to " + DEFAULT_ENTITY_COPY_RADIUS + ".")
            resetEntityCopyRadiusToDefault()
        } else {
            entityCopyRadius = copyRadius
        }
    }

    fun resetEntityNamesToDefault() {
        entityNames = DEFAULT_ENTITY_NAMES
    }

    fun resetEntityCopyRadiusToDefault() {
        entityCopyRadius = DEFAULT_ENTITY_COPY_RADIUS
    }

    /**
     *
     * Copies the NBT data of nearby entities using the default settings.
     * <br></br>
     *
     * Default settings:
     *
     * Included Entity Types: players, armor stands, and mobs
     *
     * Radius: [DevUtils.DEFAULT_ENTITY_COPY_RADIUS]
     *
     * Include own NBT data: `true`
     *
     * @see EntityList
     */
    fun copyEntityData() {
        copyEntityData(entityNames, entityCopyRadius)
    }

    /**
     * Compiles a list of entity classes from a string.
     *
     * @param text The string to parse
     * @return The list of entities
     */
    private fun getEntityClassListFromString(text: String): List<Class<out Entity>>? {
        val listMatcher = Pattern.compile("(^[A-Z_]+)(?:,[A-Z_]+)*$", Pattern.CASE_INSENSITIVE).matcher(text)

        if (!listMatcher.matches()) {
            return null
        }

        val entityClasses: MutableList<Class<out Entity>> = ArrayList()
        val entityNamesArray = text.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        for (entityName in entityNamesArray) {
            if (EntityList.isStringValidEntityName(entityName)) {
                val entityId = EntityList.getIDFromString(entityName)

                // The default ID returned when a match isn't found is the pig's id for some reason.
                if (entityId != 90 || entityName == "Pig") {
                    entityClasses.add(EntityList.getClassFromID(entityId))
                } else if (entityName == "Player") {
                    entityClasses.add(EntityPlayerSP::class.java)
                    entityClasses.add(EntityOtherPlayerMP::class.java)
                }
            } else if (entityName == "PlayerSP") {
                entityClasses.add(EntityPlayerSP::class.java)
            } else if ((entityName == "PlayerMP") or (entityName == "OtherPlayerMP")) {
                entityClasses.add(EntityOtherPlayerMP::class.java)
            } else {
                main.utils!!.sendErrorMessage("The entity name \"$entityName\" is invalid. Skipping!")
            }
        }

        return entityClasses
    }

    fun copyData() {
        if (copyMode == CopyMode.ENTITY) {
            copyEntityData()
        } else if (copyMode == CopyMode.BLOCK) {
            copyBlockData()
        } else if (copyMode == CopyMode.SIDEBAR) {
            copyScoreboardSideBar()
        } else if (copyMode == CopyMode.TAB_LIST) {
            copyTabListHeaderAndFooter()
        }
    }

    /**
     * Compresses the provided `NBTTagCompound`, encodes it as Base64, converts it into a UTF-8 string,
     * and copies it to the clipboard. The NBT tag cannot be `null`.
     *
     * @param nbtTag the NBT tag to copy
     * @param message the message to show in chat when the NBT tag is copied successfully
     */
    fun copyCompressedNBTTagToClipboard(nbtTag: NBTTagCompound, message: String?) {
        if (nbtTag == null) {
            throw NullPointerException("NBT tag cannot be null!")
        }

        val outputStream = ByteArrayOutputStream()

        try {
            CompressedStreamTools.writeCompressed(nbtTag, outputStream)
            writeToClipboard(
                String(Base64.getEncoder().encode(outputStream.toByteArray()), StandardCharsets.UTF_8),
                message
            )
        } catch (e: IOException) {
            logger.error("Failed to write NBT tag to clipboard!", e)
        }
    }

    /**
     * Copies the provided NBT tag to the clipboard as a pretty-printed string.
     *
     * @param nbtTag the NBT tag to copy
     * @param message the message to show in chat when the NBT tag is copied successfully
     */
    fun copyNBTTagToClipboard(nbtTag: NBTBase?, message: String?) {
        if (nbtTag == null) {
            main.utils!!.sendErrorMessage("This item has no NBT data!")
            return
        }
        writeToClipboard(prettyPrintNBT(nbtTag), message)
    }

    /**
     * Copies the header and footer of the tab player list to the clipboard
     *
     * @see net.minecraft.client.gui.GuiPlayerTabOverlay
     */
    fun copyTabListHeaderAndFooter() {
        val tabHeader = mc.ingameGUI.tabList.header
        val tabFooter = mc.ingameGUI.tabList.footer

        if (tabHeader == null && tabFooter == null) {
            main.utils!!.sendErrorMessage("There is no header or footer!")
            return
        }

        val output = StringBuilder()

        if (tabHeader != null) {
            output.append("Header:").append("\n")
            output.append(tabHeader.formattedText)
            output.append("\n\n")
        }

        if (tabHeader != null) {
            output.append("Footer:").append("\n")
            output.append(tabFooter!!.formattedText)
        }

        copyStringToClipboard(
            output.toString(),
            ColorCode.GREEN.toString() + "Successfully copied the tab list header and footer!"
        )
    }

    /**
     *
     * Copies a string to the clipboard
     *
     * Also shows the provided message in chat when successful
     *
     * @param string the string to copy
     * @param successMessage the custom message to show after successful copy
     */
    fun copyStringToClipboard(string: String, successMessage: String?) {
        writeToClipboard(string, successMessage)
    }

    val serverBrand: String?
        /**
         * Retrieves the server brand from the Minecraft client.
         *
         * @return the server brand if the client is connected to a server, `null` otherwise
         */
        get() {
            val SERVER_BRAND_PATTERN = Pattern.compile("(.+) <- .+")

            if (!mc.isSingleplayer) {
                val matcher =
                    SERVER_BRAND_PATTERN.matcher(mc.thePlayer.clientBrand)

                return if (matcher.find()) {
                    // Group 1 is the server brand.
                    matcher.group(1)
                } else {
                    null
                }
            } else {
                return null
            }
        }

    /**
     * Copy the block data with its tile entity data if the block has one.
     */
    fun copyBlockData() {
        if (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || mc.objectMouseOver.blockPos == null) {
            main.utils!!.sendErrorMessage("You are not looking at a block!")
            return
        }

        val blockPos = mc.objectMouseOver.blockPos

        var blockState = mc.theWorld.getBlockState(blockPos)
        if (mc.theWorld.worldType !== WorldType.DEBUG_WORLD) {
            blockState = blockState.block.getActualState(blockState, mc.theWorld, blockPos)
        }

        val tileEntity = mc.theWorld.getTileEntity(blockPos)
        val nbt = NBTTagCompound()
        if (tileEntity != null) {
            val nbtTileEntity = NBTTagCompound()
            tileEntity.writeToNBT(nbtTileEntity)
            nbt.setTag("tileEntity", nbtTileEntity)
        } else {
            nbt.setInteger("x", blockPos.x)
            nbt.setInteger("y", blockPos.y)
            nbt.setInteger("z", blockPos.z)
        }

        nbt.setString("type", Block.blockRegistry.getNameForObject(blockState.block).toString())
        blockState.properties.forEach { (key: IProperty<*>?, value: Comparable<*>?) ->
            nbt.setString(
                key.name,
                value.toString()
            )
        }

        writeToClipboard(prettyPrintNBT(nbt), ColorCode.GREEN.toString() + "Successfully copied the block data!")
    }


    // FIXME add support for TAG_LONG_ARRAY when updating to 1.12
    /**
     *
     * Converts an NBT tag into a pretty-printed string.
     *
     * For constant definitions, see [Constants.NBT]
     *
     * @param nbt the NBT tag to pretty print
     * @return pretty-printed string of the NBT data
     */
    fun prettyPrintNBT(nbt: NBTBase): String {
        val INDENT = "    "

        val tagID = nbt.id.toInt()
        var stringBuilder = StringBuilder()

        // Determine which type of tag it is.
        if (tagID == Constants.NBT.TAG_END) {
            stringBuilder.append('}')
        } else if (tagID == Constants.NBT.TAG_BYTE_ARRAY || tagID == Constants.NBT.TAG_INT_ARRAY) {
            stringBuilder.append('[')
            if (tagID == Constants.NBT.TAG_BYTE_ARRAY) {
                val nbtByteArray = nbt as NBTTagByteArray
                val bytes = nbtByteArray.byteArray

                for (i in bytes.indices) {
                    stringBuilder.append(bytes[i].toInt())

                    // Don't add a comma after the last element.
                    if (i < (bytes.size - 1)) {
                        stringBuilder.append(", ")
                    }
                }
            } else {
                val nbtIntArray = nbt as NBTTagIntArray
                val ints = nbtIntArray.intArray

                for (i in ints.indices) {
                    stringBuilder.append(ints[i])

                    // Don't add a comma after the last element.
                    if (i < (ints.size - 1)) {
                        stringBuilder.append(", ")
                    }
                }
            }
            stringBuilder.append(']')
        } else if (tagID == Constants.NBT.TAG_LIST) {
            val nbtTagList = nbt as NBTTagList

            stringBuilder.append('[')
            for (i in 0 until nbtTagList.tagCount()) {
                val currentListElement = nbtTagList[i]

                stringBuilder.append(prettyPrintNBT(currentListElement))

                // Don't add a comma after the last element.
                if (i < (nbtTagList.tagCount() - 1)) {
                    stringBuilder.append(", ")
                }
            }
            stringBuilder.append(']')
        } else if (tagID == Constants.NBT.TAG_COMPOUND) {
            val nbtTagCompound = nbt as NBTTagCompound

            stringBuilder.append('{')
            if (!nbtTagCompound.hasNoTags()) {
                val iterator: Iterator<String> = nbtTagCompound.keySet.iterator()

                stringBuilder.append(System.lineSeparator())

                while (iterator.hasNext()) {
                    val key = iterator.next()
                    val currentCompoundTagElement = nbtTagCompound.getTag(key)

                    stringBuilder.append(key).append(": ").append(
                        prettyPrintNBT(currentCompoundTagElement)
                    )

                    if (key.contains("backpack_data") && currentCompoundTagElement is NBTTagByteArray) {
                        try {
                            val backpackData = CompressedStreamTools.readCompressed(
                                ByteArrayInputStream(
                                    currentCompoundTagElement.byteArray
                                )
                            )

                            stringBuilder.append(",").append(System.lineSeparator())
                            stringBuilder.append(key).append("(decoded): ").append(
                                prettyPrintNBT(backpackData)
                            )
                        } catch (e: IOException) {
                            logger.error("Couldn't decompress backpack data into NBT, skipping!", e)
                        }
                    }

                    // Don't add a comma after the last element.
                    if (iterator.hasNext()) {
                        stringBuilder.append(",").append(System.lineSeparator())
                    }
                }

                // Indent all lines
                val indentedString =
                    stringBuilder.toString().replace(System.lineSeparator().toRegex(), System.lineSeparator() + INDENT)
                stringBuilder = StringBuilder(indentedString)
            }

            stringBuilder.append(System.lineSeparator()).append('}')
        } else {
            stringBuilder.append(nbt)
        }

        return stringBuilder.toString()
    }

    /**
     * This method reloads all of the mod's settings and resources from the corresponding files.
     */
    fun reloadAll() {
        reloadConfig()
        reloadResources()
    }

    /**
     * This method reloads all of the mod's settings from the settings file.
     */
    fun reloadConfig() {
        logger.info("Reloading settings...")
        main.configValues!!.loadValues()
        logger.info("Settings reloaded")
    }

    /**
     * This method reloads all of the mod's resources from the corresponding files.
     */
    fun reloadResources() {
        logger.info("Reloading resources...")
        readLocalAndFetchOnline()
        persistentValuesManager!!.loadValues()
        (mc.resourceManager as SimpleReloadableResourceManager).reloadResourcePack(
            FMLClientHandler.instance().getResourcePackFor(SkyblockAddonsPlus.MODID)
        )
        try {
            val notifyReloadListenersMethod =
                SimpleReloadableResourceManager::class.java.getDeclaredMethod("notifyReloadListeners")
            notifyReloadListenersMethod.isAccessible = true
            notifyReloadListenersMethod.invoke(mc.resourceManager)
        } catch (e: NoSuchMethodException) {
            logger.error("An error occurred while reloading the mod's resources.", e)
        } catch (e: IllegalAccessException) {
            logger.error("An error occurred while reloading the mod's resources.", e)
        } catch (e: InvocationTargetException) {
            logger.error("An error occurred while reloading the mod's resources.", e)
        }
        logger.info("Resources reloaded")
    }

    /*
     Internal methods
     */
    private fun writeToClipboard(text: String, successMessage: String?) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val output = StringSelection(text)

        try {
            clipboard.setContents(output, output)
            if (successMessage != null) {
                main.utils!!.sendMessage(successMessage)
            }
        } catch (exception: IllegalStateException) {
            main.utils!!.sendErrorMessage("Clipboard not available!")
        }
    }

    /**
     * Sets the copy mode to a `CopyMode` value.
     *
     * @param copyMode the new copy mode
     */
    fun setCopyMode(copyMode: CopyMode) {
        DevUtils.copyMode = copyMode
        main.utils!!.sendMessage(
            ColorCode.YELLOW.toString() + getMessage(
                "messages.copyModeSet", copyMode, Keyboard.getKeyName(
                    main.getDeveloperCopyNBTKey().getKeyCode()
                )
            )
        )
    }

    enum class CopyMode {
        BLOCK,
        ENTITY,
        ITEM,
        ITEM_COMPRESSED,
        SIDEBAR,
        TAB_LIST
    }
}
