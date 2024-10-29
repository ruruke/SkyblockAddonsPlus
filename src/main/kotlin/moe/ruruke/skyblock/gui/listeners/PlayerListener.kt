package moe.ruruke.skyblock.gui.listeners

import com.google.common.collect.Sets
import com.google.common.math.DoubleMath
import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.config.NewConfig
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.InventoryType
import moe.ruruke.skyblock.core.ItemRarity
import moe.ruruke.skyblock.core.SkillType
import moe.ruruke.skyblock.features.enchants.EnchantManager
import moe.ruruke.skyblock.utils.ActionBarParser
import moe.ruruke.skyblock.utils.ColorCode
import moe.ruruke.skyblock.utils.ItemUtils
import moe.ruruke.skyblock.utils.RomanNumeralParser.replaceNumeralsWithIntegers
import moe.ruruke.skyblock.utils.ScoreboardManager.tick
import moe.ruruke.skyblock.utils.TextUtils
import net.minecraft.block.Block
import net.minecraft.block.BlockPrismarine
import net.minecraft.block.BlockStone
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.audio.SoundCategory
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.client.settings.KeyBinding
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntityMagmaCube
import net.minecraft.entity.monster.EntitySlime
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.*
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.sound.PlaySoundEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityEvent.EnteringChunk
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.EnderTeleportEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent
import net.minecraftforge.event.entity.player.AttackEntityEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.apache.logging.log4j.Logger
import org.lwjgl.input.Keyboard
import java.math.RoundingMode
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

//TODO Fix for Hypixel localization
class PlayerListener {
    private var lastWorldJoin: Long = -1
    private var lastBoss: Long = -1
    private var lastBal: Long = -1
    private var lastBroodmother: Long = -1
    private var balTick = -1
    private var timerTick = 1
    private var lastMinionSound: Long = -1
    private var lastFishingAlert: Long = 0
    private var lastBobberEnteredWater = Long.MAX_VALUE
    private var lastSkyblockServerJoinAttempt: Long = 0
    private var lastDeath: Long = 0
    private var lastRevive: Long = 0
    private var lastMaddoxLevelTime: Long = 0
    private var lastMaddoxSlayerType: String? = null

    private var rainmakerTimeEnd: Long = -1
    fun getRainmakerTimeEnd(): Long {
        return rainmakerTimeEnd
    }

    private var oldBobberIsInWater = false
    private var oldBobberPosY = 0.0

    private val countedEndermen: MutableSet<UUID> = HashSet()

    private val recentlyKilledZealots = TreeMap<Long, MutableSet<Vec3>>()
    fun getRecentlyKilledZealots(): TreeMap<Long, MutableSet<Vec3>> {
        return recentlyKilledZealots
    }

    private var spiritSceptreHitEnemies = 0
    fun getSpiritSceptreHitEnemies(): Int {
        return spiritSceptreHitEnemies
    }

    private var spiritSceptreDealtDamage = 0f

    fun getSpiritSceptreDealtDamage(): Float {
        return spiritSceptreDealtDamage
    }

    private val explosiveBowExplosions = TreeMap<Long, Vec3>()
    fun getExplosiveBowExplosions(): TreeMap<Long, Vec3> {
        return explosiveBowExplosions
    }

    private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
    private val actionBarParser: ActionBarParser = ActionBarParser()

    // For caching for the PROFILE_TYPE_IN_CHAT feature, saves the last MAX_SIZE names.
    private val namesWithSymbols: LinkedHashMap<String?, String?> = object : LinkedHashMap<String?, String?>() {
        private val MAX_SIZE = 80

        override fun removeEldestEntry(eldest: Map.Entry<String?, String?>): Boolean {
            return size > MAX_SIZE
        }
    }

    /**
     * Reset all the timers and stuff when joining a new world.
     */
    @SubscribeEvent
    fun onWorldJoin(e: EntityJoinWorldEvent) {
        val entity = e.entity

        if (entity === Minecraft.getMinecraft().thePlayer) {
            lastWorldJoin = Minecraft.getSystemTime()
            lastBoss = -1
            timerTick = 1
            main.instance.getInventoryUtil().resetPreviousInventory()
            countedEndermen.clear()
            //TODO:
//            EndstoneProtectorManager.reset()

            //TODO:
//            val doubleWarpMarker: IslandWarpGui.Marker = IslandWarpGui.getDoubleWarpMarker()
//            if (doubleWarpMarker != null) {
//                IslandWarpGui.setDoubleWarpMarker(null)
//                Minecraft.getMinecraft().thePlayer.sendChatMessage("/warp " + doubleWarpMarker.getWarpName())
//            }
//
//            NPCUtils.getNpcLocations().clear()
//            JerryPresent.getJerryPresents().clear()
//            FishParticleManager.clearParticleCache()
        }
    }

//    /**
//     * Interprets the action bar to extract mana, health, and defence. Enables/disables mana/health prediction,
//     * and looks for mana usage messages in chat while predicting.
//     */
//    @SubscribeEvent(priority = EventPriority.HIGH)
//    fun onChatReceive(e: ClientChatReceivedEvent) {
//        if (!main.getUtils().isOnHypixel()) {
//            return
//        }
//
//        val formattedText = e.message.formattedText
//        val unformattedText = e.message.unformattedText
//        val strippedText: String = TextUtils.stripColor(formattedText)
//
//        if (formattedText.startsWith("§7Sending to server ")) {
//            lastSkyblockServerJoinAttempt = Minecraft.getSystemTime()
//            DragonTracker.getInstance().reset()
//            return
//        }
//
//        if (main.getConfigValues()
//                .isEnabled(Feature.OUTBID_ALERT_SOUND) && formattedText.matches("§6\\[Auction] §..*§eoutbid you .*".toRegex())
//            && (main.getConfigValues().isEnabled(Feature.OUTBID_ALERT_SOUND_IN_OTHER_GAMES) || main.getUtils()
//                .isOnSkyblock())
//        ) {
//            main.getUtils().playLoudSound("random.orb", 0.5)
//        }
//
//        if (main.getUtils().isOnSkyblock()) {
//            // Type 2 means it's an action bar message.
//            if (e.type.toInt() == 2) {
//                // Log the message to the game log if action bar message logging is enabled.
//                if (main.getConfigValues().isEnabled(Feature.DEVELOPER_MODE) && DevUtils.isLoggingActionBarMessages()) {
//                    logger.info("[ACTION BAR] $unformattedText")
//                }
//
//                // Parse using ActionBarParser and display the rest message instead
//                val restMessage: String = actionBarParser.parseActionBar(unformattedText)
//                if (main.isUsingOofModv1() && restMessage.trim { it <= ' ' }.length == 0) {
//                    e.isCanceled = true
//                    return
//                }
//
//                if (main.getUtils().isInDungeon()) {
//                    if (main.getConfigValues().isEnabled(Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY)) {
//                        main.getDungeonManager().addEssence(restMessage)
//                    }
//
//                    if (main.getConfigValues().isEnabled(Feature.DUNGEONS_SECRETS_DISPLAY)) {
//                        main.getDungeonManager().addSecrets(restMessage)
//                    }
//                }
//                // Mark the message for change
//            } else {
//                var matcher: Matcher
//
//                if (main.getRenderListener()
//                        .isPredictMana() && unformattedText.startsWith("Used ") && unformattedText.endsWith("Mana)")
//                ) {
//                    val manaLost = unformattedText.split(Pattern.quote("! (").toRegex()).dropLastWhile { it.isEmpty() }
//                        .toTypedArray()[1].split(
//                        Pattern.quote(" Mana)").toRegex()
//                    ).dropLastWhile { it.isEmpty() }.toTypedArray()[0].toInt()
//                    changeMana(-manaLost.toFloat())
//                } else if ((DEATH_MESSAGE_PATTERN.matcher(unformattedText).also { matcher = it }).matches()) {
//                    // Hypixel's dungeon reconnect messages look exactly like death messages.
//                    val causeOfDeath = matcher.group("causeOfDeath")
//                    if (causeOfDeath != "reconnected") {
//                        val username = matcher.group("username")
//                        val deadPlayer = if (username == "You") {
//                            Minecraft.getMinecraft().thePlayer
//                        } else {
//                            Minecraft.getMinecraft().theWorld.getPlayerEntityByName(username)
//                        }
//
//                        MinecraftForge.EVENT_BUS.post(SkyblockPlayerDeathEvent(deadPlayer, username, causeOfDeath))
//                    }
//                } else if (main.getConfigValues()
//                        .isEnabled(Feature.SUMMONING_EYE_ALERT) && formattedText == "§r§6§lRARE DROP! §r§5Summoning Eye§r"
//                ) {
//                    main.getUtils().playLoudSound("random.orb", 0.5) // credits to tomotomo, thanks lol
//                    main.getRenderListener().setTitleFeature(Feature.SUMMONING_EYE_ALERT)
//                    main.getScheduler()
//                        .schedule(Scheduler.CommandType.RESET_TITLE_FEATURE, main.getConfigValues().getWarningSeconds())
//                } else if (formattedText == "§r§aA special §r§5Zealot §r§ahas spawned nearby!§r") {
//                    if (main.getConfigValues().isEnabled(Feature.SPECIAL_ZEALOT_ALERT)) {
//                        main.getUtils().playLoudSound("random.orb", 0.5)
//                        main.getRenderListener().setTitleFeature(Feature.SUMMONING_EYE_ALERT)
//                        main.getRenderListener().setTitleFeature(Feature.SPECIAL_ZEALOT_ALERT)
//                        main.getScheduler().schedule(
//                            Scheduler.CommandType.RESET_TITLE_FEATURE,
//                            main.getConfigValues().getWarningSeconds()
//                        )
//                    }
//                    if (main.getConfigValues().isEnabled(Feature.ZEALOT_COUNTER)) {
//                        // Edit the message to include counter.
//                        e.message = ChatComponentText(
//                            (formattedText + ColorCode.GRAY + " (" + main.getPersistentValuesManager()
//                                .getPersistentValues().getKills()).toString() + ")"
//                        )
//                    }
//                    main.getPersistentValuesManager().addEyeResetKills()
//                    // TODO: Seems like leg warning and num sc killed should be separate features
//                } else if (main.getConfigValues()
//                        .isEnabled(Feature.LEGENDARY_SEA_CREATURE_WARNING) && SeaCreatureManager.getInstance()
//                        .getAllSeaCreatureSpawnMessages().contains(unformattedText)
//                ) {
//                    main.getPersistentValuesManager().getPersistentValues().setSeaCreaturesKilled(
//                        main.getPersistentValuesManager().getPersistentValues().getSeaCreaturesKilled() + 1
//                    )
//                    if (SeaCreatureManager.getInstance().getLegendarySeaCreatureSpawnMessages()
//                            .contains(unformattedText)
//                    ) {
//                        main.getUtils().playLoudSound("random.orb", 0.5)
//                        main.getRenderListener().setTitleFeature(Feature.LEGENDARY_SEA_CREATURE_WARNING)
//                        main.getScheduler().schedule(
//                            Scheduler.CommandType.RESET_TITLE_FEATURE,
//                            main.getConfigValues().getWarningSeconds()
//                        )
//                    }
//                } else if (main.getConfigValues()
//                        .isEnabled(Feature.DISABLE_MAGICAL_SOUP_MESSAGES) && SOUP_RANDOM_MESSAGES.contains(
//                        unformattedText
//                    )
//                ) {
//                    e.isCanceled = true
//                } else if (main.getConfigValues()
//                        .isEnabled(Feature.DISABLE_TELEPORT_PAD_MESSAGES) && (formattedText.startsWith("§r§aWarped from ") || formattedText == "§r§cThis Teleport Pad does not have a destination set!§r")
//                ) {
//                    e.isCanceled = true
//                } else if (main.getConfigValues()
//                        .isEnabled(Feature.DISABLE_MORT_MESSAGES) && strippedText.startsWith("[NPC] Mort:")
//                ) {
//                    e.isCanceled = true
//                } else if (main.getConfigValues()
//                        .isEnabled(Feature.DISABLE_BOSS_MESSAGES) && strippedText.startsWith("[BOSS] ")
//                ) {
//                    e.isCanceled = true
//                } else if (main.getConfigValues()
//                        .isEnabled(Feature.SPIRIT_SCEPTRE_DISPLAY) && strippedText.startsWith("Your Implosion hit") || strippedText.startsWith(
//                        "Your Spirit Sceptre hit"
//                    )
//                ) {
//                    matcher = SPIRIT_SCEPTRE_MESSAGE_PATTERN.matcher(unformattedText)
//                    // Ensure matcher.group gets what it wants, we don't need the whole result
//                    if (matcher.find()) {
//                        this.spiritSceptreHitEnemies = matcher.group("hitEnemies").toInt()
//                        this.spiritSceptreDealtDamage = matcher.group("dealtDamage").replace(",", "").toFloat()
//
//                        if (main.getConfigValues().isEnabled(Feature.DISABLE_SPIRIT_SCEPTRE_MESSAGES)) {
//                            e.isCanceled = true
//                        }
//                    }
//                } else if (SlayerTracker.getInstance().isTrackerEnabled() &&
//                    (SLAYER_COMPLETED_PATTERN.matcher(strippedText).also { matcher = it }).matches()
//                ) { // §r   §r§5§l» §r§7Talk to Maddox to claim your Wolf Slayer XP!§r
//                    SlayerTracker.getInstance().completedSlayer(matcher.group("slayerType"))
//                } else if (SlayerTracker.getInstance().isTrackerEnabled() &&
//                    (SLAYER_COMPLETED_PATTERN_AUTO1.matcher(strippedText).also { matcher = it }).matches()
//                ) { // Spider Slayer LVL 7 - Next LVL in 181,000 XP!
//                    lastMaddoxLevelTime = System.currentTimeMillis()
//                    lastMaddoxSlayerType = matcher.group("slayerType")
//                } else if (SLAYER_COMPLETED_PATTERN_AUTO2.matcher(strippedText)
//                        .matches() && System.currentTimeMillis() - lastMaddoxLevelTime < 100
//                ) {
//                    SlayerTracker.getInstance().completedSlayer(lastMaddoxSlayerType)
//                } else if (main.getConfigValues().isEnabled(Feature.DRAGON_STATS_TRACKER) &&
//                    strippedText.startsWith("☬ You placed a Summoning Eye!")
//                ) { // §r§5☬ §r§dYou placed a Summoning Eye! §r§7(§r§e5§r§7/§r§a8§r§7)§r
//                    DragonTracker.getInstance().addEye()
//                } else if (main.getConfigValues().isEnabled(Feature.DRAGON_STATS_TRACKER) &&
//                    strippedText == "You recovered a Summoning Eye!"
//                ) {
//                    DragonTracker.getInstance().removeEye()
//                } else if (main.getConfigValues().isEnabled(Feature.DRAGON_STATS_TRACKER) &&
//                    (DRAGON_SPAWNED_PATTERN.matcher(strippedText).also { matcher = it }).matches()
//                ) {
//                    DragonTracker.getInstance().dragonSpawned(matcher.group("dragonType"))
//                } else if (main.getConfigValues().isEnabled(Feature.DRAGON_STATS_TRACKER) &&
//                    DRAGON_KILLED_PATTERN.matcher(strippedText).matches()
//                ) {
//                    DragonTracker.getInstance().dragonKilled()
//                } else if (main.getConfigValues().isEnabled(Feature.SHOW_ITEM_COOLDOWNS) &&
//                    unformattedText == "You laid an egg!"
//                ) { // Put the Chicken Head on cooldown for 20 seconds when the player lays an egg.
//                    CooldownManager.put(InventoryUtils.CHICKEN_HEAD_ID)
//                } else if (main.getConfigValues().isEnabled(Feature.BIRCH_PARK_RAINMAKER_TIMER) &&
//                    formattedText.startsWith("§r§eYou added a minute of rain!")
//                ) {
//                    if (this.rainmakerTimeEnd == -1L || this.rainmakerTimeEnd < System.currentTimeMillis()) {
//                        this.rainmakerTimeEnd =
//                            System.currentTimeMillis() + (1000 * 60) // Set the timer to a minute from now.
//                    } else {
//                        this.rainmakerTimeEnd += (1000 * 60).toLong() // Extend the timer one minute.
//                    }
//                } else if (main.getConfigValues().isEnabled(Feature.FETCHUR_TODAY) &&
//                    formattedText.startsWith("§e[NPC] Fetchur§f:")
//                ) {
//                    val fetchur: FetchurManager = FetchurManager.getInstance()
//                    // Triggered if player has just given the correct item to Fetchur, or if sba isn't in sync (already handed in quest)
//                    if (unformattedText.contains(fetchur.getFetchurTaskCompletedPhrase()) ||
//                        !fetchur.hasFetchedToday() && unformattedText.contains(fetchur.getFetchurAlreadyDidTaskPhrase())
//                    ) {
//                        FetchurManager.getInstance().saveLastTimeFetched()
//                    }
//                    // Tries to check if a message is from a player to add the player profile icon
//                } else if (main.getConfigValues().isEnabled(Feature.PLAYER_SYMBOLS_IN_CHAT) &&
//                    unformattedText.contains(":")
//                ) {
//                    playerSymbolsDisplay(e, unformattedText)
//                }
//
//                if (main.getConfigValues().isEnabled(Feature.NO_ARROWS_LEFT_ALERT)) {
//                    if (NO_ARROWS_LEFT_PATTERN.matcher(formattedText).matches()) {
//                        main.getUtils().playLoudSound("random.orb", 0.5)
//                        main.getRenderListener().setSubtitleFeature(Feature.NO_ARROWS_LEFT_ALERT)
//                        main.getRenderListener().setArrowsLeft(-1)
//                        main.getScheduler().schedule(
//                            Scheduler.CommandType.RESET_SUBTITLE_FEATURE,
//                            main.getConfigValues().getWarningSeconds()
//                        )
//                    } else if ((ONLY_HAVE_ARROWS_LEFT_PATTERN.matcher(formattedText).also { matcher = it }).matches()) {
//                        val arrowsLeft = matcher.group("arrows").toInt()
//                        main.getUtils().playLoudSound("random.orb", 0.5)
//                        main.getRenderListener().setSubtitleFeature(Feature.NO_ARROWS_LEFT_ALERT)
//                        main.getRenderListener().setArrowsLeft(arrowsLeft)
//                        main.getScheduler().schedule(
//                            Scheduler.CommandType.RESET_SUBTITLE_FEATURE,
//                            main.getConfigValues().getWarningSeconds()
//                        )
//                    }
//                }
//
//                if (main.getInventoryUtils().getInventoryType() === InventoryType.SALVAGING && main.getConfigValues()
//                        .isEnabled(Feature.SHOW_SALVAGE_ESSENCES_COUNTER)
//                ) {
//                    main.getDungeonManager().addSalvagedEssences(unformattedText)
//                }
//
//                if (main.getUtils().isInDungeon()) {
//                    val reviveMessageMatcher = REVIVE_MESSAGE_PATTERN.matcher(unformattedText)
//
//                    if (reviveMessageMatcher.matches()) {
//                        val players = Minecraft.getMinecraft().theWorld.playerEntities
//
//                        val revivedPlayerName = reviveMessageMatcher.group("revivedPlayer")
//                        val reviverName = reviveMessageMatcher.group("reviver")
//                        var revivedPlayer: EntityPlayer? = null
//                        var revivingPlayer: EntityPlayer? = null
//
//                        for (player in players) {
//                            if (revivedPlayer != null && revivingPlayer != null) {
//                                break
//                            }
//
//                            if (player.name == revivedPlayerName) {
//                                revivedPlayer = player
//                                lastRevive = Minecraft.getSystemTime()
//                            }
//
//                            if (reviverName != null && player.name == reviverName) {
//                                revivingPlayer = player
//                            }
//                        }
//
//                        MinecraftForge.EVENT_BUS.post(DungeonPlayerReviveEvent(revivedPlayer, revivingPlayer))
//                    }
//
//                    if (main.getConfigValues().isEnabled(Feature.SHOW_DUNGEON_MILESTONE)) {
//                        val dungeonMilestone: DungeonMilestone = main.getDungeonManager().parseMilestone(formattedText)
//                        if (dungeonMilestone != null) {
//                            main.getDungeonManager().setDungeonMilestone(dungeonMilestone)
//                        }
//                    }
//
//                    if (main.getConfigValues().isEnabled(Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY)) {
//                        main.getDungeonManager().addBonusEssence(formattedText)
//                    }
//                }
//
//
//                if (ABILITY_CHAT_PATTERN.matcher(formattedText).matches()) {
//                    CooldownManager.put(Minecraft.getMinecraft().thePlayer.heldItem)
//                } else if ((PROFILE_CHAT_PATTERN.matcher(strippedText).also { matcher = it }).matches()) {
//                    val profile = matcher.group(1)
//
//                    // TODO: Slothpixel can no longer handle our queries
//                    /*                    if (!profile.equals(main.getUtils().getProfileName())) {
//                        APIManager.getInstance().onProfileSwitch(profile);
//                    }*/
//                    main.getUtils().setProfileName(profile)
//                } else if ((SWITCH_PROFILE_CHAT_PATTERN.matcher(strippedText).also { matcher = it }).matches()) {
//                    val profile = matcher.group(1)
//
//                    /*                    if (!profile.equals(main.getUtils().getProfileName())) {
//                        APIManager.getInstance().onProfileSwitch(profile);
//                    }*/
//                    main.getUtils().setProfileName(profile)
//                }
//            }
//        }
//    }

//    private fun playerSymbolsDisplay(e: ClientChatReceivedEvent, unformattedText: String) {
//        // For some reason guild chat messages still contain color codes in the unformatted text
//        var username: String =
//            TextUtils.stripColor(unformattedText.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
//        // Remove chat channel prefix
//        if (username.contains(">")) {
//            username = username.substring(username.indexOf('>') + 1)
//        }
//        // Remove rank prefix and guild rank suffix if exists
//        username = TextUtils.trimWhitespaceAndResets(username.replace("\\[[^\\[\\]]*\\]".toRegex(), ""))
//        // Check if stripped username is a real username or the player
//        if (TextUtils.isUsername(username) || username == "**MINECRAFTUSERNAME**") {
//            val chattingPlayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(username)
//            // Put player in cache if found nearby
//            if (chattingPlayer != null) {
//                namesWithSymbols[username] = chattingPlayer.displayName.siblings[0].unformattedText
//            } else {
//                val networkPlayerInfos = Minecraft.getMinecraft().thePlayer.sendQueue.playerInfoMap
//                val finalUsername = username
//                val result = networkPlayerInfos.stream().filter { npi: NetworkPlayerInfo -> npi.displayName != null }
//                    .filter { npi: NetworkPlayerInfo ->
//                        TextUtils.stripUsername(npi.displayName.unformattedText).equals(finalUsername)
//                    }.findAny()
//                // Put in cache if found
//                if (result.isPresent) {
//                    namesWithSymbols[username] = result.get().displayName.formattedText
//                }
//            }
//            // Check cache regardless if found nearby
//            if (namesWithSymbols.containsKey(username)) {
//                val oldMessage = e.message
//                val usernameWithSymbols = namesWithSymbols[username]
//                var suffix = " "
//                if (main.getConfigValues().isEnabled(Feature.SHOW_PROFILE_TYPE)) {
//                    val m = PROFILE_TYPE_SYMBOL.matcher(usernameWithSymbols)
//                    if (m.find()) {
//                        suffix += m.group(0)
//                    }
//                }
//                if (main.getConfigValues().isEnabled(Feature.SHOW_NETHER_FACTION)) {
//                    val m = NETHER_FACTION_SYMBOL.matcher(usernameWithSymbols)
//                    if (m.find()) {
//                        suffix += m.group(0)
//                    }
//                }
//                if (suffix != " ") {
//                    val finalSuffix = suffix
//                    val finalUsername = username
//                    TextUtils.transformAnyChatComponent(
//                        oldMessage
//                    ) { component ->
//                        if (component is ChatComponentText and(component as ChatComponentText).text.contains(
//                            finalUsername
//                        )) {
//                        val textComponent = component as ChatComponentText
//                        textComponent.text = textComponent.text.replace(finalUsername, finalUsername + finalSuffix)
//                        return@transformAnyChatComponent true
//                    }
//                        false
//                    }
//                }
//            }
//        }
//    }

    /**
     * Acts as a callback to set the actionbar message after other mods have a chance to look at the message
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    fun onChatReceiveLast(e: ClientChatReceivedEvent) {
        if (e.type.toInt() == 2 && !e.isCanceled) {
//            val itr: Iterator<String> = actionBarParser.getStringsToRemove().iterator()
//            var message = e.message.unformattedText
//            while (itr.hasNext()) {
//                message = message.replace((" *" + Pattern.quote(itr.next())).toRegex(), "")
//            }
//            message = message.trim { it <= ' ' }
//            e.message = ChatComponentText(message)
        }
    }


//    /**
//     * This method is triggered by the player right-clicking on something.
//     * Yes, it says it works for left-clicking blocks too, but it actually doesn't, so please don't use it to detect that.
//     * <br></br>
//     * Also, when the player right-clicks on a block, `PlayerInteractEvent` gets fired twice. The first time,
//     * the correct action type `Action.RIGHT_CLICK_BLOCK`, is used. The second time, the action type is
//     * `Action.RIGHT_CLICK_AIR` for some reason. Both of these events will cause a `C08PacketPlayerBlockPlacement`
//     * packet to be sent to the server, so block both of them if you want to prevent a block from being placed.
//     * <br></br>
//     * Look at `Minecraft#rightClickMouse()` to see when the event is fired.
//     *
//     * @see Minecraft
//     */
//    @SubscribeEvent
//    fun onInteract(e: PlayerInteractEvent) {
//        val mc = Minecraft.getMinecraft()
//        val heldItem = e.entityPlayer.heldItem
//
//        if (main.getUtils().isOnSkyblock() && heldItem != null) {
//            // Change the GUI background color when a backpack is opened to match the backpack's color.
//            if (heldItem.item === Items.skull) {
//                val color: BackpackColor? = ItemUtils.getBackpackColor(heldItem)
//                if (color != null) {
//                    BackpackInventoryManager.setBackpackColor(color)
//                }
//            } else if (heldItem.item === Items.fishing_rod
//                && (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || e.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR)
//            ) {
//                // Update fishing status if the player is fishing and reels in their rod.
//                if (main.getConfigValues().isEnabled(Feature.FISHING_SOUND_INDICATOR) && BaitManager.getInstance()
//                        .isHoldingRod()
//                ) {
//                    oldBobberIsInWater = false
//                    lastBobberEnteredWater = Long.MAX_VALUE
//                    oldBobberPosY = 0.0
//                }
//                if (main.getConfigValues().isEnabled(Feature.SHOW_ITEM_COOLDOWNS)) {
//                    val itemId = ItemUtils.getSkyblockItemID(heldItem)
//                    // Grappling hook cool-down
//                    if (itemId != null && itemId == InventoryUtils.GRAPPLING_HOOK_ID && mc.thePlayer.fishEntity != null) {
//                        val wearingFullBatPerson: Boolean =
//                            InventoryUtils.isWearingFullSet(mc.thePlayer, InventoryUtils.BAT_PERSON_SET_IDS)
//                        val cooldownTime = if (wearingFullBatPerson) 0 else CooldownManager.getItemCooldown(itemId)
//                        CooldownManager.put(itemId, cooldownTime)
//                    }
//                }
//            }
//        }
//    }

    /**
     * The main timer for a bunch of stuff.
     */
    @SubscribeEvent
    fun onTick(e: ClientTickEvent) {
//        if (e.phase == TickEvent.Phase.START) {
//            val mc = Minecraft.getMinecraft()
//            timerTick++
//
//            if (mc != null) { // Predict health every tick if needed.
//                tick()
//
//                if (actionBarParser.getHealthUpdate() != null && System.currentTimeMillis() - actionBarParser.getLastHealthUpdate() > 3000) {
//                    actionBarParser.setHealthUpdate(null)
//                }
//                val p = mc.thePlayer
//                if (p != null && main.getConfigValues()
//                        .isEnabled(Feature.HEALTH_PREDICTION)
//                ) { //Reverse calculate the player's health by using the player's vanilla hearts. Also calculate the health change for the gui item.
//                    val newHealth =
//                        if (getAttribute(Attribute.HEALTH) > getAttribute(Attribute.MAX_HEALTH)) getAttribute(Attribute.HEALTH) else Math.round(
//                            getAttribute(Attribute.MAX_HEALTH) * ((p.health) / p.maxHealth)
//                        ).toFloat()
//                    setAttribute(Attribute.HEALTH, newHealth)
//                }
//
//                if (timerTick == 20) {
//                    // Add natural mana every second (increase is based on your max mana).
//                    if (main.getRenderListener().isPredictMana()) {
//                        // If regen-ing, cap at the max mana
//                        if (getAttribute(Attribute.MANA) < getAttribute(Attribute.MAX_MANA)) {
//                            setAttribute(
//                                Attribute.MANA,
//                                min(
//                                    (getAttribute(Attribute.MANA) + getAttribute(Attribute.MAX_MANA) / 50).toDouble(),
//                                    getAttribute(Attribute.MAX_MANA).toDouble()
//                                )
//                                    .toFloat()
//                            )
//                        }
//                        // If above mana cap, do nothing
//                    }
//
//                    this.parseTabList()
//
//                    if (main.getConfigValues().isEnabled(Feature.DUNGEON_DEATH_COUNTER) && main.getUtils().isInDungeon()
//                        && main.getDungeonManager().isPlayerListInfoEnabled()
//                    ) {
//                        main.getDungeonManager().updateDeathsFromPlayerListInfo()
//                    }
//                } else if (timerTick % 5 == 0) { // Check inventory, location, updates, and skeleton helmet every 1/4 second.
//                    val player = mc.thePlayer
//
//                    if (player != null) {
//                        EndstoneProtectorManager.checkGolemStatus()
//                        TabListParser.parse()
//                        main.getUtils().parseSidebar()
//                        main.getInventoryUtils().checkIfInventoryIsFull(mc, player)
//
//                        if (main.getUtils().isOnSkyblock()) {
//                            main.getInventoryUtils().checkIfWearingSkeletonHelmet(player)
//                            main.getInventoryUtils().checkIfUsingToxicArrowPoison(player)
//                            main.getInventoryUtils().checkIfWearingSlayerArmor(player)
//                            if (shouldTriggerFishingIndicator()) { // The logic fits better in its own function
//                                main.getUtils().playLoudSound("random.successful_hit", 0.8)
//                            }
//                            if (main.getConfigValues().isEnabled(Feature.FETCHUR_TODAY)) {
//                                FetchurManager.getInstance().recalculateFetchurItem()
//                            }
//
//                            // Update mining/fishing pet tracker numbers when the player opens the skill menu
//                            if (main.getInventoryUtils().getInventoryType() === InventoryType.SKILL_TYPE_MENU) {
//                                val skill = SkillType.getFromString(main.getInventoryUtils().getInventorySubtype())
//                                if (skill == SkillType.MINING || skill == SkillType.FISHING) {
//                                    try {
//                                        val cc =
//                                            ((Minecraft.getMinecraft().currentScreen as GuiChest).inventorySlots as ContainerChest).lowerChestInventory
//                                        val lore = ItemUtils.getItemLore(cc.getStackInSlot(51))
//                                        val milestoneProgress: String = TextUtils.stripColor(lore[lore.size - 1])
//                                        var m = NEXT_TIER_PET_PROGRESS.matcher(milestoneProgress)
//                                        var total = -1
//                                        if (m.matches()) {
//                                            total = m.group("total").replace(",".toRegex(), "").toInt()
//                                        } else if ((MAXED_TIER_PET_PROGRESS.matcher(milestoneProgress)
//                                                .also { m = it }).matches()
//                                        ) {
//                                            total = m.group("total").replace(",".toRegex(), "").toInt()
//                                        }
//                                        if (total > 0) {
//                                            val persistentValues: PersistentValuesManager.PersistentValues =
//                                                main.getPersistentValuesManager().getPersistentValues()
//                                            val original: Int
//                                            if (skill == SkillType.FISHING) {
//                                                original = persistentValues.getSeaCreaturesKilled()
//                                                main.getPersistentValuesManager().getPersistentValues()
//                                                    .setSeaCreaturesKilled(total)
//                                            } else {
//                                                original = persistentValues.getOresMined()
//                                                main.getPersistentValuesManager().getPersistentValues()
//                                                    .setOresMined(total)
//                                            }
//                                            if (original != total) {
//                                                main.getPersistentValuesManager().saveValues()
//                                            }
//                                        }
//                                    } catch (ignored: Exception) {
//                                    }
//                                }
//                            }
//                        }
//
//                        if (mc.currentScreen == null && main.getPlayerListener().didntRecentlyJoinWorld() &&
//                            (!main.getUtils().isInDungeon() || Minecraft.getSystemTime() - lastDeath > 1000 &&
//                                    Minecraft.getSystemTime() - lastRevive > 1000)
//                        ) {
//                            main.getInventoryUtils().getInventoryDifference(player.inventory.mainInventory)
//                        }
//
//                        if (main.getConfigValues().isEnabled(Feature.BAIT_LIST) && BaitManager.getInstance()
//                                .isHoldingRod()
//                        ) {
//                            BaitManager.getInstance().refreshBaits()
//                        }
//                    }
//                    main.getInventoryUtils().cleanUpPickupLog()
//                } else if (timerTick > 20) { // To keep the timer going from 1 to 21 only.
//                    timerTick = 1
//                }
//            }
//        }
    }

    // TODO Feature Rewrite
//    fun parseTabList() {
//        val tabFooterChatComponent = Minecraft.getMinecraft().ingameGUI.tabList.footer
//
//        var tabFooterString: String? = null
//        var strippedTabFooterString: String? = null
//        if (tabFooterChatComponent != null) {
//            tabFooterString = tabFooterChatComponent.formattedText
//            strippedTabFooterString = TextUtils.stripColor(tabFooterString)
//        }
//        //TODO:
////        if (main.getUtils().isOnSkyblock()) {
////            if (main.getConfigValues().isEnabled(Feature.TAB_EFFECT_TIMERS)) {
////                TabEffectManager.getInstance().update(tabFooterString, strippedTabFooterString)
////            }
////        }
//    }

//    @SubscribeEvent
//    fun onEntityEvent(e: LivingUpdateEvent) {
//        if (!main.getUtils().isOnSkyblock()) {
//            return
//        }
//
//        val entity = e.entity
//
//        if (entity.ticksExisted < 5) {
//            if (main.getConfigValues().isEnabled(Feature.HIDE_OTHER_PLAYERS_PRESENTS)) {
//                if (!JerryPresent.getJerryPresents().containsKey(entity.uniqueID)) {
//                    val present: JerryPresent = JerryPresent.getJerryPresent(entity)
//                    if (present != null) {
//                        JerryPresent.getJerryPresents().put(entity.uniqueID, present)
//                        return
//                    }
//                }
//            }
//
//            if (entity is EntityOtherPlayerMP && main.getConfigValues()
//                    .isEnabled(Feature.HIDE_PLAYERS_NEAR_NPCS) && main.getUtils()
//                    .getLocation() !== Location.GUEST_ISLAND && main.getUtils().getLocation() !== Location.THE_CATACOMBS
//            ) {
//                val health = entity.health
//
//                if (NPCUtils.getNpcLocations().containsKey(entity.getUniqueID())) {
//                    if (health != 20.0f) {
//                        NPCUtils.getNpcLocations().remove(entity.getUniqueID())
//                        return
//                    }
//                } else if (NPCUtils.isNPC(entity)) {
//                    NPCUtils.getNpcLocations().put(entity.getUniqueID(), entity.getPositionVector())
//                    return
//                }
//            }
//        }
//
//        if (entity is EntityArmorStand && entity.hasCustomName()) {
//            PowerOrbManager.getInstance().detectPowerOrb(entity)
//
//            if (main.getUtils().getLocation() === Location.ISLAND) {
//                val cooldown: Int = main.getConfigValues().getWarningSeconds() * 1000 + 5000
//                if (main.getConfigValues().isEnabled(Feature.MINION_FULL_WARNING) &&
//                    entity.getCustomNameTag() == "§cMy storage is full! :("
//                ) {
//                    val now = System.currentTimeMillis()
//                    if (now - lastMinionSound > cooldown) {
//                        lastMinionSound = now
//                        main.getUtils().playLoudSound("random.pop", 1)
//                        main.getRenderListener().setSubtitleFeature(Feature.MINION_FULL_WARNING)
//                        main.getScheduler().schedule(
//                            Scheduler.CommandType.RESET_SUBTITLE_FEATURE,
//                            main.getConfigValues().getWarningSeconds()
//                        )
//                    }
//                } else if (main.getConfigValues().isEnabled(Feature.MINION_STOP_WARNING)) {
//                    val matcher = MINION_CANT_REACH_PATTERN.matcher(entity.getCustomNameTag())
//                    if (matcher.matches()) {
//                        val now = System.currentTimeMillis()
//                        if (now - lastMinionSound > cooldown) {
//                            lastMinionSound = now
//                            main.getUtils().playLoudSound("random.orb", 1)
//
//                            val mobName = matcher.group("mobName")
//                            main.getRenderListener().setCannotReachMobName(mobName)
//                            main.getRenderListener().setSubtitleFeature(Feature.MINION_STOP_WARNING)
//                            main.getScheduler().schedule(
//                                Scheduler.CommandType.RESET_SUBTITLE_FEATURE,
//                                main.getConfigValues().getWarningSeconds()
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }

    @SubscribeEvent
    fun onAttack(e: AttackEntityEvent) {
        if (e.target is EntityEnderman) {
            if (isZealot(e.target)) {
                countedEndermen.add(e.target.uniqueID)
            }
        }
    }

//    @SubscribeEvent
//    fun onDeath(e: LivingDeathEvent) {
//        if (e.entity is EntityEnderman) {
//            if (countedEndermen.remove(e.entity.uniqueID)) {
//                main.getPersistentValuesManager().getPersistentValues()
//                    .setKills(main.getPersistentValuesManager().getPersistentValues().getKills() + 1)
//                main.getPersistentValuesManager().saveValues()
//                EndstoneProtectorManager.onKill()
//            } else if (main.getUtils().isOnSkyblock() && main.getConfigValues()
//                    .isEnabled(Feature.ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT)
//            ) {
//                if (isZealot(e.entity)) {
//                    val now = System.currentTimeMillis()
//                    if (recentlyKilledZealots.containsKey(now)) {
//                        recentlyKilledZealots[now]!!.add(e.entity.positionVector)
//                    } else {
//                        recentlyKilledZealots[now] = Sets.newHashSet(e.entity.positionVector)
//                    }
//
//                    explosiveBowExplosions.keys.removeIf { explosionTime: Long -> now - explosionTime > 150 }
//                    val latestExplosion = explosiveBowExplosions.lastEntry() ?: return
//
//                    val explosionLocation = latestExplosion.value
//
//                    //                    int possibleZealotsKilled = 1;
////                    System.out.println("This means "+possibleZealotsKilled+" may have been killed...");
////                    int originalPossibleZealotsKilled = possibleZealotsKilled;
//                    val deathLocation = e.entity.positionVector
//
//                    //                    double distance = explosionLocation.distanceTo(deathLocation);
////                    System.out.println("Distance was "+distance+"!");
//                    if (explosionLocation.distanceTo(deathLocation) < 4.6) {
////                        possibleZealotsKilled--;
//
//                        main.getPersistentValuesManager().getPersistentValues()
//                            .setKills(main.getPersistentValuesManager().getPersistentValues().getKills() + 1)
//                        main.getPersistentValuesManager().saveValues()
//                        EndstoneProtectorManager.onKill()
//                    }
//
//                    //                    System.out.println((originalPossibleZealotsKilled-possibleZealotsKilled)+" zealots were actually killed...");
//                }
//            }
//        }
//
//        NPCUtils.getNpcLocations().remove(e.entity.uniqueID)
//    }

    fun isZealot(enderman: Entity): Boolean {
        val stands = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(
            EntityArmorStand::class.java,
            AxisAlignedBB(
                enderman.posX - 1,
                enderman.posY,
                enderman.posZ - 1,
                enderman.posX + 1,
                enderman.posY + 5,
                enderman.posZ + 1
            )
        )
        if (stands.isEmpty()) return false

        val armorStand = stands[0]
        return armorStand.hasCustomName() && armorStand.customNameTag.contains("Zealot")
    }

//    @SubscribeEvent
//    fun onEntitySpawn(e: EnteringChunk) {
//        val entity = e.entity
//
//        // Detect Brood Mother spawn
//        if (main.getUtils().isOnSkyblock() && main.getConfigValues()
//                .isEnabled(Feature.BROOD_MOTHER_ALERT) && main.getUtils().getLocation() === Location.SPIDERS_DEN
//        ) {
//            if (entity.hasCustomName() && entity.posY > 165) {
//                if (entity.name.contains("Brood Mother") && (lastBroodmother == -1L || System.currentTimeMillis() - lastBroodmother > 15000)) { //Brood Mother
//                    lastBroodmother = System.currentTimeMillis()
//                    //                  main.getUtils().sendMessage("Broodmother spawned."); //testers said to remove message
//                    main.getRenderListener().setTitleFeature(Feature.BROOD_MOTHER_ALERT)
//                    main.getScheduler()
//                        .schedule(Scheduler.CommandType.RESET_TITLE_FEATURE, main.getConfigValues().getWarningSeconds())
//                    main.getUtils().playLoudSound("random.orb", 0.5)
//                }
//            }
//        }
//        if (main.getUtils().isOnSkyblock()) {
//            val mc = Minecraft.getMinecraft()
//            for (cubes in mc.theWorld.loadedEntityList) {
//                if (main.getConfigValues().isEnabled(Feature.BAL_BOSS_ALERT) && main.getUtils()
//                        .isOnSkyblock() && LocationUtils.isInCrystalHollows(
//                        main.getUtils().getLocation().getScoreboardName()
//                    )
//                ) {
//                    if (cubes is EntityMagmaCube) {
//                        val magma = cubes as EntitySlime
//                        if (magma.slimeSize > 10) { // Find a big bal boss
//                            if ((lastBal == -1L || System.currentTimeMillis() - lastBal > 240000)) {
//                                lastBal = System.currentTimeMillis()
//                                main.getRenderListener()
//                                    .setTitleFeature(Feature.BAL_BOSS_ALERT) // Enable warning and disable again in four seconds.
//                                balTick = 16 // so the sound plays instantly
//                                main.getScheduler().schedule(
//                                    Scheduler.CommandType.RESET_TITLE_FEATURE,
//                                    main.getConfigValues().getWarningSeconds()
//                                )
//                            }
//                            if (main.getRenderListener()
//                                    .getTitleFeature() === Feature.BAL_BOSS_ALERT && balTick % 4 == 0
//                            ) { // Play sound every 4 ticks or 1/5 second.
//                                main.getUtils().playLoudSound("random.orb", 0.5)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        if (main.getUtils().isOnSkyblock() && main.getConfigValues()
//                .isEnabled(Feature.ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT) && entity is EntityArrow
//        ) {
//            val arrow = entity
//
//            val p = Minecraft.getMinecraft().thePlayer
//            val heldItem = p.heldItem
//            if (heldItem != null && "EXPLOSIVE_BOW" == ItemUtils.getSkyblockItemID(heldItem)) {
//                val playerRadius = AxisAlignedBB(p.posX - 3, p.posY - 3, p.posZ - 3, p.posX + 3, p.posY + 3, p.posZ + 3)
//                if (playerRadius.isVecInside(arrow.positionVector)) {
//                    //                    System.out.println("Spawned explosive arrow!");
//
//                    main.getNewScheduler().scheduleRepeatingTask(object : SkyblockRunnable() {
//                        override fun run() {
//                            if (arrow.isDead || arrow.isCollided || arrow.inGround) {
//                                cancel()
//
//                                //                                System.out.println("Arrow is done, added an explosion!");
//                                val explosionLocation = Vec3(arrow.posX, arrow.posY, arrow.posZ)
//                                explosiveBowExplosions[System.currentTimeMillis()] = explosionLocation
//
//                                recentlyKilledZealots.keys.removeIf { killedTime: Long -> System.currentTimeMillis() - killedTime > 150 }
//                                val filteredRecentlyKilledZealots: MutableSet<Vec3> = HashSet()
//                                for ((_, value) in recentlyKilledZealots) {
//                                    filteredRecentlyKilledZealots.addAll(value)
//                                }
//                                if (filteredRecentlyKilledZealots.isEmpty()) return
//
//                                //                                int possibleZealotsKilled = filteredRecentlyKilledZealots.size();
////                                System.out.println("This means "+possibleZealotsKilled+" may have been killed...");
////                                int originalPossibleZealotsKilled = possibleZealotsKilled;
//                                for (zealotDeathLocation in filteredRecentlyKilledZealots) {
//                                    val distance = explosionLocation.distanceTo(zealotDeathLocation)
//                                    //                                    System.out.println("Distance was "+distance+"!");
//                                    if (distance < 4.6) {
////                                        possibleZealotsKilled--;
//
//                                        main.getPersistentValuesManager().getPersistentValues().setKills(
//                                            main.getPersistentValuesManager().getPersistentValues().getKills() + 1
//                                        )
//                                        main.getPersistentValuesManager().saveValues()
//                                        EndstoneProtectorManager.onKill()
//                                    }
//                                }
//
//                                //                                System.out.println((originalPossibleZealotsKilled-possibleZealotsKilled)+" zealots were actually killed...");
//                            }
//                        }
//                    }, 0, 1)
//                }
//            }
//        }
//    }

//    @SubscribeEvent
//    fun onEnderTeleport(e: EnderTeleportEvent) {
////        if (main.getUtils().isOnSkyblock() && main.getConfigValues()
////                .isEnabled(Feature.DISABLE_ENDERMAN_TELEPORTATION_EFFECT)
////        ) {
////            e.isCanceled = true
////        }
//    }

//    /**
//     * Modifies bottom of item tooltips and activates the copy item nbt feature
//     */
//    @SubscribeEvent(priority = EventPriority.NORMAL)
//    fun onItemTooltipFirst(e: ItemTooltipEvent) {
//        val hoveredItem = e.itemStack
//
//        if (e.toolTip != null && main.getUtils().isOnSkyblock()) {
//            var insertAt = e.toolTip.size
//            insertAt-- // 1 line for the rarity
//            if (e.showAdvancedItemTooltips) {
//                insertAt -= 2 // 1 line for the item name, and 1 line for the nbt
//                if (e.itemStack.isItemDamaged) {
//                    insertAt-- // 1 line for damage
//                }
//            }
//            insertAt = max(0.0, insertAt.toDouble()).toInt()
//
//            val extraAttributes = ItemUtils.getExtraAttributes(hoveredItem)
//            if (extraAttributes != null) {
//                if (main.getConfigValues().isEnabled(Feature.SHOW_ITEM_ANVIL_USES) && extraAttributes.hasKey(
//                        "anvil_uses",
//                        ItemUtils.NBT_INTEGER
//                    )
//                ) {
//                    // Anvil Uses ~ original done by Dahn#6036
//                    var anvilUses = extraAttributes.getInteger("anvil_uses")
//                    if (extraAttributes.hasKey("hot_potato_count", ItemUtils.NBT_INTEGER)) {
//                        anvilUses -= extraAttributes.getInteger("hot_potato_count")
//                    }
//                    if (anvilUses > 0) {
//                        e.toolTip.add(insertAt++, Translations.getMessage(anvilUses.toString()))
//                    }
//                }
//
//                if (main.getConfigValues().isEnabled(Feature.SHOW_BROKEN_FRAGMENTS) && hoveredItem.displayName.contains(
//                        "Dragon Fragment"
//                    ) &&
//                    extraAttributes.hasKey("bossId") && extraAttributes.hasKey("spawnedFor")
//                ) {
//                    e.toolTip.add(insertAt++, "§c§lBROKEN FRAGMENT")
//                }
//
//                if (main.getConfigValues().isEnabled(Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE) && extraAttributes.hasKey(
//                        "baseStatBoostPercentage",
//                        ItemUtils.NBT_INTEGER
//                    )
//                ) {
//                    val baseStatBoost = extraAttributes.getInteger("baseStatBoostPercentage")
//
//                    var colorCode: ColorCode =
//                        main.getConfigValues().getRestrictedColor(Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE)
//                    if (main.getConfigValues().isEnabled(Feature.BASE_STAT_BOOST_COLOR_BY_RARITY)) {
//                        var rarityIndex = baseStatBoost / 10
//                        if (rarityIndex < 0) rarityIndex = 0
//                        if (rarityIndex >= ItemRarity.entries.size) rarityIndex = ItemRarity.entries.size - 1
//
//                        colorCode = ItemRarity.entries[rarityIndex].getColorCode()
//                    }
//                    e.toolTip.add(insertAt++, "§7Base Stat Boost: $colorCode+$baseStatBoost%")
//                }
//
//                if (main.getConfigValues().isEnabled(Feature.SHOW_STACKING_ENCHANT_PROGRESS)) {
//                    insertAt = EnchantManager.insertStackingEnchantProgress(e.toolTip, extraAttributes, insertAt)
//                }
//
//                if (main.getConfigValues().isEnabled(Feature.SHOW_SWORD_KILLS) && extraAttributes.hasKey(
//                        "sword_kills",
//                        ItemUtils.NBT_INTEGER
//                    )
//                ) {
//                    val colorCode: ColorCode = main.getConfigValues().getRestrictedColor(Feature.SHOW_SWORD_KILLS)
//                    e.toolTip.add(insertAt++, "§7Sword Kills: " + colorCode + extraAttributes.getInteger("sword_kills"))
//                }
//
//                if (main.getConfigValues().isEnabled(Feature.SHOW_ITEM_DUNGEON_FLOOR) && extraAttributes.hasKey(
//                        "item_tier",
//                        ItemUtils.NBT_INTEGER
//                    )
//                ) {
//                    val floor = extraAttributes.getInteger("item_tier")
//                    val colorCode: ColorCode =
//                        main.getConfigValues().getRestrictedColor(Feature.SHOW_ITEM_DUNGEON_FLOOR)
//                    e.toolTip.add(
//                        insertAt++,
//                        "§7Obtained on Floor: " + colorCode + (if (floor == 0) "Entrance" else floor)
//                    )
//                }
//
//                if (main.getConfigValues().isEnabled(Feature.SHOW_RARITY_UPGRADED) && extraAttributes.hasKey(
//                        "rarity_upgrades",
//                        ItemUtils.NBT_INTEGER
//                    )
//                ) {
//                    e.toolTip.add(
//                        insertAt,
//                        main.getConfigValues().getRestrictedColor(Feature.SHOW_RARITY_UPGRADED) + "§lRARITY UPGRADED"
//                    )
//                }
//            }
//        }
//    }

    /**
     * Modifies item enchantments on tooltips as well as roman numerals
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onItemTooltipLast(e: ItemTooltipEvent) {
        val hoveredItem = e.itemStack

        if (e.toolTip != null && main.utils!!.isOnSkyblock()) {
            if (SkyblockAddonsPlus.configValues!!.isEnabled(Feature.ENCHANTMENT_LORE_PARSING)) {
                EnchantManager.parseEnchants(e.toolTip, hoveredItem)
            }
            if (NewConfig.isEnabled(Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS)) {
                val startIndex =
                    if (main.configValues!!.isEnabled(Feature.DONT_REPLACE_ROMAN_NUMERALS_IN_ITEM_NAME)) 1 else 0

                for (i in startIndex until e.toolTip.size) {
                    e.toolTip[i] = replaceNumeralsWithIntegers(e.toolTip[i])
                }
            }

            if (main.configValues!!.isEnabled(Feature.SHOW_SKYBLOCK_ITEM_ID) ||
                main.configValues!!.isEnabled(Feature.DEVELOPER_MODE)
            ) {
                val itemId = ItemUtils.getSkyblockItemID(e.itemStack)
                val tooltipLine = EnumChatFormatting.DARK_GRAY.toString() + "skyblock:" + itemId

                if (itemId != null) {
                    if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
                        var i = e.toolTip.size
                        while (i-- > 0) {
                            if (e.toolTip[i].startsWith(EnumChatFormatting.DARK_GRAY.toString() + "minecraft:")) {
                                e.toolTip.add(i + 1, tooltipLine)
                                break
                            }
                        }
                    } else {
                        e.toolTip.add(tooltipLine)
                    }
                }
            }
        }
    }

//    /**
//     * This method handles key presses while the player is in-game.
//     * For handling of key presses while a GUI (e.g. chat, pause menu, F3) is open,
//     * see [GuiScreenListener.onKeyInput]
//     *
//     * @param e the `KeyInputEvent` that occurred
//     */
//    @SubscribeEvent
//    fun onKeyInput(e: InputEvent.KeyInputEvent?) {
//        if (main.getOpenSettingsKey().isPressed()) {
//            main.getUtils().setFadingIn(true)
//            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN)
//        } else if (main.getOpenEditLocationsKey().isPressed()) {
//            main.getUtils().setFadingIn(false)
//            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.EDIT_LOCATIONS, 0, null)
//        } else if (main.getConfigValues().isEnabled(Feature.DEVELOPER_MODE) && main.getDeveloperCopyNBTKey()
//                .isPressed()
//        ) {
//            DevUtils.copyData()
//        }
//
//        if (main.getConfigValues().isEnabled(Feature.DUNGEONS_MAP_DISPLAY) &&
//            main.getConfigValues().isEnabled(Feature.CHANGE_DUNGEON_MAP_ZOOM_WITH_KEYBOARD) &&
//            main.getUtils().isInDungeon()
//        ) {
//            if (Keyboard.isKeyDown(main.getKeyBindings().get(5).getKeyCode()) && Keyboard.getEventKeyState()) {
//                DungeonMapManager.decreaseZoomByStep()
//            } else if (Keyboard.isKeyDown(main.getKeyBindings().get(4).getKeyCode()) && Keyboard.getEventKeyState()) {
//                DungeonMapManager.increaseZoomByStep()
//            }
//        }
//    }
//
//    /**
//     * This method is called when a sound is played.
//     *
//     * @param event the event that caused this method to be called
//     */
//    @SubscribeEvent
//    fun onPlaySound(event: PlaySoundEvent) {
//        if (!main.getUtils().isOnSkyblock()) {
//            return
//        }
//
//        // Ignore sounds that don't have a specific location like GUIs
//        if (event.sound is PositionedSoundRecord) {
//            val eventSound = event.sound as PositionedSoundRecord
//
//            if (main.getConfigValues().isEnabled(Feature.STOP_RAT_SOUNDS)) {
//                if (event.category == SoundCategory.ANIMALS) {
//                    for (sound in RAT_SOUNDS) {
//                        // Check that the sound matches the rat sound
//                        if (eventSound.soundLocation == sound.soundLocation && eventSound.pitch == sound.pitch && eventSound.volume == sound.volume) {
//                            if (main.getConfigValues().isDisabled(Feature.STOP_ONLY_RAT_SQUEAK) ||
//                                eventSound.soundLocation.toString().endsWith("mob.bat.idle")
//                            ) {
//                                // Cancel the result
//                                event.result = null
//                            }
//                        }
//                    }
//                }
//            }
//
//            if (main.getConfigValues().isEnabled(Feature.BACKPACK_OPENING_SOUND) &&
//                System.currentTimeMillis() - main.getGuiScreenListener().getLastBackpackOpenMs() < 500
//            ) {
//                if (event.name == "random.chestopen") {
//                    val thePlayer = Minecraft.getMinecraft().thePlayer
//
//                    // When a player opens a backpack, a chest open sound is played at the player's location.
//                    if (DoubleMath.roundToInt(
//                            event.sound.xPosF.toDouble(),
//                            RoundingMode.HALF_UP
//                        ) == thePlayer.position.x && DoubleMath.roundToInt(
//                            event.sound.yPosF.toDouble(),
//                            RoundingMode.HALF_UP
//                        ) == thePlayer.position.y && DoubleMath.roundToInt(
//                            event.sound.zPosF.toDouble(),
//                            RoundingMode.HALF_UP
//                        ) == thePlayer.position.z
//                    ) {
//                        event.result = null
//                    }
//                }
//            }
//        }
//
//        if (main.getConfigValues()
//                .isEnabled(Feature.STOP_BONZO_STAFF_SOUNDS) && BONZO_STAFF_SOUNDS.contains(event.name)
//        ) {
//            event.result = null
//        }
//    }

//    /**
//     * This method is called when a player dies in Skyblock.
//     *
//     * @param e the event that caused this method to be called
//     */
//    @SubscribeEvent
//    fun onPlayerDeath(e: SkyblockPlayerDeathEvent) {
//        val thisPlayer = Minecraft.getMinecraft().thePlayer
//
//        //  Resets all user input on death as to not walk backwards or strafe into the portal
//        if (main.getConfigValues().isEnabled(Feature.PREVENT_MOVEMENT_ON_DEATH) && e.entityPlayer === thisPlayer) {
//            KeyBinding.unPressAllKeys()
//        }
//
//        /*
//        Don't show log for losing all items when the player dies in dungeons.
//         The items come back after the player is revived and the large log causes a distraction.
//         */
//        if (main.getConfigValues()
//                .isEnabled(Feature.ITEM_PICKUP_LOG) && e.entityPlayer === thisPlayer && main.getUtils().isInDungeon()
//        ) {
//            lastDeath = Minecraft.getSystemTime()
//            main.getInventoryUtils().resetPreviousInventory()
//        }
//
//        if (main.getConfigValues().isEnabled(Feature.DUNGEON_DEATH_COUNTER) && main.getUtils().isInDungeon()) {
//            val dungeonPlayer: DungeonPlayer = main.getDungeonManager().getDungeonPlayerByName(e.username)
//            if (dungeonPlayer != null) {
//                // Hypixel sends another death message if the player disconnects. Don't count two deaths if the player
//                // disconnects while dead.
//                if (e.cause.contains("disconnected") && dungeonPlayer.isGhost()) {
//                    return
//                }
//                main.getDungeonManager().addDeath()
//            } else if (e.entity === thisPlayer) { // TODO Keep track of a variable in the manager for the player's dungeon state
//                // Hypixel sends another death message if the player disconnects. Don't count two deaths if the player
//                // disconnects while dead. We can use flying state to check if player is a ghost.
//                if (e.cause.contains("disconnected") && thisPlayer.capabilities.allowFlying) {
//                    return
//                }
//                main.getDungeonManager().addDeath()
//            } else {
//                logger.warn(("Could not record death for " + e.username).toString() + ". This dungeon player isn't in the registry.")
//            }
//        }
//    }
//
//    /**
//     * This method is called when a player in Dungeons gets revived.
//     *
//     * @param e the event that caused this method to be called
//     */
//    @SubscribeEvent
//    fun onDungeonPlayerRevive(e: DungeonPlayerReviveEvent) {
//        if (e.revivedPlayer === Minecraft.getMinecraft().thePlayer) {
//            lastRevive = Minecraft.getSystemTime()
//        }
//
//        // Reset the previous inventory so the screen doesn't get spammed with a large pickup log
//        if (main.getConfigValues().isEnabled(Feature.ITEM_PICKUP_LOG)) {
//            main.getInventoryUtils().resetPreviousInventory()
//        }
//    }
//
//    @SubscribeEvent
//    fun onBlockBreak(e: SkyblockBlockBreakEvent) {
//        val blockState = Minecraft.getMinecraft().theWorld.getBlockState(e.blockPos)
//        if (ORES.contains(Block.getStateId(blockState))) {
//            var shouldIncrement = true
//            if (main.getUtils().getLocation() === Location.ISLAND) {
//                if (blockState.block === Blocks.diamond_block) {
//                    shouldIncrement = false
//                }
//                // TODO: Check if a minion is nearby to eliminate false positives
//            }
//            if (shouldIncrement) {
//                main.getPersistentValuesManager().getPersistentValues()
//                    .setOresMined(main.getPersistentValuesManager().getPersistentValues().getOresMined() + 1)
//            }
//        }
//        if (main.getConfigValues().isEnabled(Feature.SHOW_ITEM_COOLDOWNS)) {
//            val itemId = ItemUtils.getSkyblockItemID(Minecraft.getMinecraft().thePlayer.heldItem)
//            if (itemId != null) {
//                val block = blockState.block
//                if ((itemId == InventoryUtils.JUNGLE_AXE_ID || itemId == InventoryUtils.TREECAPITATOR_ID) &&
//                    (block == Blocks.log || block == Blocks.log2)
//                ) {
//                    // Weirdly, the level 100 leg monkey doesn't seem to be a full 50% reduction when accounting for break time
//                    val multiplier = if (main.getConfigValues().isEnabled(Feature.LEG_MONKEY_LEVEL_100)) .6f else 1f
//                    var cooldownTime = (CooldownManager.getItemCooldown(itemId) * multiplier) as Long
//                    cooldownTime -= (if (main.getConfigValues()
//                            .isEnabled(Feature.COOLDOWN_PREDICTION)
//                    ) e.timeToBreak - 50 else 0).toLong()
//                    // TODO: Pet detection
//                    // Min cooldown time is 400 because anything lower than that can allow the player to hit a block already marked for block removal by treecap/jungle axe ability
//                    CooldownManager.put(itemId, max(cooldownTime.toDouble(), 400.0))
//                }
//            }
//        }
//    }

//    fun aboutToJoinSkyblockServer(): Boolean {
//        return Minecraft.getSystemTime() - lastSkyblockServerJoinAttempt < 6000
//    }
//
    fun didntRecentlyJoinWorld(): Boolean {
        return (Minecraft.getSystemTime() - lastWorldJoin) > 3000
    }
//
//    val maxTickers: Int
//        get() = actionBarParser.getMaxTickers()
//
//    val tickers: Int
//        get() = actionBarParser.getTickers()
//
//    fun updateLastSecondHealth() {
//        val health = getAttribute(Attribute.HEALTH)
//        // Update the health gained/lost over the last second
//        if (main.getConfigValues()
//                .isEnabled(Feature.HEALTH_UPDATES) && actionBarParser.getLastSecondHealth() !== health
//        ) {
//            actionBarParser.setHealthUpdate(health - actionBarParser.getLastSecondHealth())
//            actionBarParser.setLastHealthUpdate(System.currentTimeMillis())
//        }
//        actionBarParser.setLastSecondHealth(health)
//    }
//
//    fun shouldResetMouse(): Boolean {
//        return System.currentTimeMillis() - main.getGuiScreenListener().getLastContainerCloseMs() > 100
//    }
//
//    val healthUpdate: Float
//        get() = actionBarParser.getHealthUpdate()
//
//    private fun changeMana(change: Float) {
//        setAttribute(Attribute.MANA, getAttribute(Attribute.MANA) + change)
//    }
//
//    private fun getAttribute(attribute: Attribute): Float {
//        return main.getUtils().getAttributes().get(attribute).getValue()
//    }
//
//    private fun setAttribute(attribute: Attribute, value: Float) {
//        main.getUtils().getAttributes().get(attribute).setValue(value)
//    }
//
//    /**
//     * Checks if the fishing indicator sound should be played. To play the sound, these conditions have to be met:
//     *
//     * 1. Fishing sound indicator feature is enabled
//     *
//     * 2. The player is on skyblock (checked in [.onTick])
//     *
//     * 3. The player is holding a fishing rod
//     *
//     * 4. The fishing rod is in the water
//     *
//     * 5. The bobber suddenly moves downwards, indicating a fish has been caught
//     *
//     * @return `true` if the fishing alert sound should be played, `false` otherwise
//     * @see Feature.FISHING_SOUND_INDICATOR
//     */
//    private fun shouldTriggerFishingIndicator(): Boolean {
//        val mc = Minecraft.getMinecraft()
//
//        if (main.getConfigValues()
//                .isEnabled(Feature.FISHING_SOUND_INDICATOR) && mc.thePlayer.fishEntity != null && BaitManager.getInstance()
//                .isHoldingRod()
//        ) {
//            // Highly consistent detection by checking when the hook has been in the water for a while and
//            // suddenly moves downward. The client may rarely bug out with the idle bobbing and trigger a false positive.
//            val bobber = mc.thePlayer.fishEntity
//            val currentTime = System.currentTimeMillis()
//            if (bobber.isInWater && !oldBobberIsInWater) lastBobberEnteredWater = currentTime
//            oldBobberIsInWater = bobber.isInWater
//            if (bobber.isInWater && abs(bobber.motionX) < 0.01 && abs(bobber.motionZ) < 0.01 && currentTime - lastFishingAlert > 1000 && currentTime - lastBobberEnteredWater > 1500) {
//                val movement = bobber.posY - oldBobberPosY // The Entity#motionY field is inaccurate for this purpose
//                oldBobberPosY = bobber.posY
//                if (movement < -0.04) {
//                    lastFishingAlert = currentTime
//                    return true
//                }
//            }
//        }
//        return false
//    }
//
//    fun getActionBarParser(): ActionBarParser {
//        return actionBarParser
//    }
//
//    companion object {
//        private val logger: Logger = SkyblockAddons.getLogger()
//
//        private val NO_ARROWS_LEFT_PATTERN: Pattern =
//            Pattern.compile("(?:§r)?§cYou don't have any more Arrows left in your Quiver!§r")
//        private val ONLY_HAVE_ARROWS_LEFT_PATTERN: Pattern =
//            Pattern.compile("(?:§r)?§cYou only have (?<arrows>[0-9]+) Arrows left in your Quiver!§r")
//        private const val ENCHANT_LINE_STARTS_WITH = "§5§o§9"
//        private val ABILITY_CHAT_PATTERN: Pattern =
//            Pattern.compile("§r§aUsed §r§6[A-Za-z ]+§r§a! §r§b\\([0-9]+ Mana\\)§r")
//        private val PROFILE_CHAT_PATTERN: Pattern = Pattern.compile("You are playing on profile: ([A-Za-z]+).*")
//        private val SWITCH_PROFILE_CHAT_PATTERN: Pattern = Pattern.compile("Your profile was changed to: ([A-Za-z]+).*")
//        private val MINION_CANT_REACH_PATTERN: Pattern = Pattern.compile("§cI can't reach any (?<mobName>[A-Za-z]*)s")
//        private val DRAGON_KILLED_PATTERN: Pattern = Pattern.compile(" *[A-Z]* DRAGON DOWN!")
//        private val DRAGON_SPAWNED_PATTERN: Pattern =
//            Pattern.compile("☬ The (?<dragonType>[A-Za-z ]+) Dragon has spawned!")
//        private val SLAYER_COMPLETED_PATTERN: Pattern =
//            Pattern.compile(" {3}» Talk to Maddox to claim your (?<slayerType>[A-Za-z]+) Slayer XP!")
//        private val SLAYER_COMPLETED_PATTERN_AUTO1: Pattern =
//            Pattern.compile(" *(?<slayerType>[A-Za-z]+) Slayer LVL \\d+ - Next LVL in [\\d,]+ XP!")
//        private val SLAYER_COMPLETED_PATTERN_AUTO2: Pattern = Pattern.compile(" *SLAYER QUEST STARTED!")
//        private val DEATH_MESSAGE_PATTERN: Pattern = Pattern.compile(" ☠ (?<username>\\w+) (?<causeOfDeath>.+)\\.")
//        private val REVIVE_MESSAGE_PATTERN: Pattern =
//            Pattern.compile(" ❣ (?<revivedPlayer>\\w+) was revived(?: by (?<reviver>\\w+))*!")
//        private val NEXT_TIER_PET_PROGRESS: Pattern = Pattern.compile("Next tier: (?<total>[0-9,]+)/.*")
//        private val MAXED_TIER_PET_PROGRESS: Pattern = Pattern.compile(".*: (?<total>[0-9,]+)")
//        private val SPIRIT_SCEPTRE_MESSAGE_PATTERN: Pattern =
//            Pattern.compile("Your (?:Implosion|Spirit Sceptre) hit (?<hitEnemies>[0-9]+) enem(?:y|ies) for (?<dealtDamage>[0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]+)*) damage\\.")
//        private val PROFILE_TYPE_SYMBOL: Pattern = Pattern.compile("(?i)§[0-9A-FK-ORZ][♲Ⓑ]")
//        private val NETHER_FACTION_SYMBOL: Pattern = Pattern.compile("(?i)§[0-9A-FK-ORZ][⚒ቾ]")
//
//        private val SOUP_RANDOM_MESSAGES: Set<String> = HashSet(
//            mutableListOf(
//                "I feel like I can fly!",
//                "What was in that soup?",
//                "Hmm… tasty!",
//                "Hmm... tasty!",
//                "You can now fly for 2 minutes.",
//                "Your flight has been extended for 2 extra minutes.",
//                "You can now fly for 200 minutes.",
//                "Your flight has been extended for 200 extra minutes."
//            )
//        )
//
//        private val BONZO_STAFF_SOUNDS: Set<String> = HashSet(
//            mutableListOf(
//                "fireworks.blast", "fireworks.blast_far",
//                "fireworks.twinkle", "fireworks.twinkle_far", "mob.ghast.moan"
//            )
//        )
//
//        // All Rat pet sounds as instance with their respective sound categories, except the sound when it lays a cheese
//        private val RAT_SOUNDS: Set<PositionedSoundRecord> = HashSet(
//            Arrays.asList(
//                PositionedSoundRecord(
//                    ResourceLocation("minecraft", "mob.bat.idle"), 1.0f, 1.1904762f, 0.0f, 0.0f, 0.0f
//                ),
//                PositionedSoundRecord(ResourceLocation("minecraft", "mob.chicken.step"), 0.15f, 1.0f, 0.0f, 0.0f, 0.0f)
//            )
//        )
//
//        private val ORES: Set<Int> = Sets.newHashSet<E>(
//            Block.getIdFromBlock(Blocks.coal_ore),
//            Block.getIdFromBlock(Blocks.iron_ore),
//            Block.getIdFromBlock(Blocks.gold_ore),
//            Block.getIdFromBlock(Blocks.redstone_ore),
//            Block.getIdFromBlock(Blocks.emerald_ore),
//            Block.getIdFromBlock(Blocks.lapis_ore),
//            Block.getIdFromBlock(Blocks.diamond_ore),
//            Block.getIdFromBlock(Blocks.lit_redstone_ore),
//            Block.getIdFromBlock(Blocks.obsidian),
//            Block.getIdFromBlock(Blocks.diamond_block),
//            Utils.getBlockMetaId(Blocks.stone, BlockStone.EnumType.DIORITE_SMOOTH.metadata),
//            Utils.getBlockMetaId(Blocks.stained_hardened_clay, EnumDyeColor.CYAN.metadata),
//            Utils.getBlockMetaId(Blocks.prismarine, BlockPrismarine.EnumType.ROUGH.metadata),
//            Utils.getBlockMetaId(Blocks.prismarine, BlockPrismarine.EnumType.DARK.metadata),
//            Utils.getBlockMetaId(Blocks.prismarine, BlockPrismarine.EnumType.BRICKS.metadata),
//            Utils.getBlockMetaId(Blocks.wool, EnumDyeColor.LIGHT_BLUE.metadata),
//            Utils.getBlockMetaId(Blocks.wool, EnumDyeColor.GRAY.metadata)
//        )
}

