package moe.ruruke.skyblock.features.EntityOutlines

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.configValues
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.utils
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.Location
import moe.ruruke.skyblock.core.Translations
import moe.ruruke.skyblock.events.RenderEntityOutlineEvent
import moe.ruruke.skyblock.features.cooldowns.CooldownManager
import moe.ruruke.skyblock.gui.buttons.ButtonLocation
import moe.ruruke.skyblock.listeners.RenderListener
import moe.ruruke.skyblock.utils.ColorCode
import moe.ruruke.skyblock.utils.DrawUtils
import moe.ruruke.skyblock.utils.TextUtils
import moe.ruruke.skyblock.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.passive.*
import net.minecraft.potion.Potion
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.abs

class FeatureTrackerQuest {
    @SubscribeEvent
    fun onEntityOutline(e: RenderEntityOutlineEvent) {
        if (e.getType() === RenderEntityOutlineEvent.Type.NO_XRAY) {
            if (SkyblockAddonsPlus.configValues!!.isEnabled(Feature.TREVOR_THE_TRAPPER_FEATURES) &&
                SkyblockAddonsPlus.configValues!!.isEnabled(Feature.TREVOR_HIGHLIGHT_TRACKED_ENTITY) &&
                isTrackingAnimal && entityToOutline != null && entityToOutline!!.getAnimal() != null &&
                !Minecraft.getMinecraft().thePlayer.isPotionActive(Potion.blindness)
            ) {
                e.queueEntityToOutline(entityToOutline!!.getAnimal(), entityToOutline!!.getRarity().getColorInt())
            }
        }
    }

    @SubscribeEvent
    fun onEntityEvent(e: LivingUpdateEvent) {
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        val entity: Entity = e.entity
        if (SkyblockAddonsPlus.configValues!!.isEnabled(Feature.TREVOR_THE_TRAPPER_FEATURES) &&
            (main.configValues!!
                .isEnabled(Feature.TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR) || main.configValues!!
                .isEnabled(Feature.TREVOR_HIGHLIGHT_TRACKED_ENTITY)) &&
            mushroomIslandLocations.contains(main.utils!!.getLocation())
        ) {
            if (SkyblockAddonsPlus.configValues!!.isEnabled(Feature.TREVOR_THE_TRAPPER_FEATURES) &&
                (configValues!!.isEnabled(Feature.TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR) || configValues!!.isEnabled(
                    Feature.TREVOR_HIGHLIGHT_TRACKED_ENTITY
                )) &&
                mushroomIslandLocations.contains(utils!!.getLocation())
            ) {
                if (entity is EntityArmorStand && entity.hasCustomName() && entity.ticksExisted > 30) {
                    val m: Matcher =
                        TRACKED_ANIMAL_NAME_PATTERN.matcher(TextUtils.stripColor(entity.getCustomNameTag()))
                    if (m.matches()) {
                        val rarity = TrackerRarity.getFromString(m.group("rarity"))
                        val animal = TrackerType.getFromString(m.group("animal"))
                        if (rarity != null && animal != null) {
                            try {
                                val trackedEntity = TrackedEntity((entity as EntityArmorStand)!!, animal, rarity)
                                trackedEntity.attachAnimal(
                                    Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(
                                        animal.getClazz(),
                                        AxisAlignedBB(
                                            entity.posX - 2,
                                            entity.posY - 2,
                                            entity.posZ - 2,
                                            entity.posX + 2,
                                            entity.posY + 2,
                                            entity.posZ + 2
                                        )
                                    )
                                )
                                entityToOutline = trackedEntity
                            } catch (ignored: NullPointerException) {
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onChatReceived(e: ClientChatReceivedEvent) {
        if (SkyblockAddonsPlus.configValues!!
                .isEnabled(Feature.TREVOR_THE_TRAPPER_FEATURES) && e.type.toInt() != 2 && SkyblockAddonsPlus.utils!!.isOnSkyblock()
        ) {
            val stripped: String = TextUtils.stripColor(e.message.getFormattedText())
            // Once the player has started the hunt, start some timers
            if (TREVOR_FIND_ANIMAL_PATTERN.matcher(stripped).matches()) {
                // Start the quest
                isTrackingAnimal = true
                // The player has 10 minutes to kill the animal
                CooldownManager.put("TREVOR_THE_TRAPPER_HUNT", 600000)
                // The player has 30 seconds before they can receive another animal after killing the current one
                CooldownManager.put("TREVOR_THE_TRAPPER_RETURN", 30000)
            } else if (ANIMAL_DIED_PATTERN.matcher(stripped).matches() || ANIMAL_KILLED_PATTERN.matcher(stripped)
                    .matches()
            ) {
                CooldownManager.remove("TREVOR_THE_TRAPPER_HUNT")
                onQuestEnded()
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onNameTagRender(e: RenderLivingEvent.Specials.Pre<EntityLivingBase?>) {
        val entity: Entity = e.entity
        if (SkyblockAddonsPlus.configValues!!.isEnabled(Feature.TREVOR_THE_TRAPPER_FEATURES) &&
            !e.isCanceled() && SkyblockAddonsPlus.configValues!!
                .isEnabled(Feature.TREVOR_SHOW_QUEST_COOLDOWN) &&
            CooldownManager.isOnCooldown("TREVOR_THE_TRAPPER_RETURN")
        ) {
            val p = Pattern.compile("Trevor The Trapper")
            val s: String = TextUtils.stripColor(entity.customNameTag)
            if (p.matcher(s).matches()) {
                val str: String = Utils.MESSAGE_PREFIX_SHORT + Translations.getMessage(
                    "messages.worldRenderedCooldownTime",
                    CooldownManager.getRemainingCooldown("TREVOR_THE_TRAPPER_RETURN") / 1000
                )
                DrawUtils.drawTextInWorld(str, e.x, e.y + entity.height + .75, e.z)
            }
        }
    }

    @SubscribeEvent
    fun onClientTick(e: ClientTickEvent) {
        if (SkyblockAddonsPlus.configValues!!
                .isEnabled(Feature.TREVOR_THE_TRAPPER_FEATURES) && e.phase == TickEvent.Phase.START && Minecraft.getMinecraft().thePlayer != null
        ) {
            if (isTrackingAnimal && CooldownManager.getRemainingCooldown("TREVOR_THE_TRAPPER_HUNT") === 0.toLong()) {
                onQuestEnded()
            } else if (entityToOutline != null) {
                entityToOutline!!.cacheDistanceToPlayer()
            }
        }
    }

    private fun onQuestEnded() {
        isTrackingAnimal = false
        entityToOutline = null
    }


    private enum class TrackerType {
        COW("Cow", EntityCow::class.java),
        PIG("Pig", EntityPig::class.java),
        SHEEP("Sheep", EntitySheep::class.java),
        RABBIT("Rabbit", EntityRabbit::class.java),
        CHICKEN("Chicken", EntityChicken::class.java);

        private var _name: String? = null
        private var clazz: Class<out Entity?>? = null

        constructor(entityName: String, entityClass: Class<out Entity?>) {
            _name = entityName
            clazz = entityClass
        }
        fun getName(): String? {
            return _name
        }
         fun getClazz(): Class<out Entity?>? {
             return clazz
         }
        companion object {

            fun getFromString(s: String): TrackerType? {
                for (type in values()) {
                    if (type.name == s) {
                        return type
                    }
                }
                return null
            }
        }
    }

    private enum class TrackerRarity(private val nameTagName: String, color: ColorCode) {
        TRACKABLE("Trackable", ColorCode.WHITE),
        UNTRACKABLE("Untrackable", ColorCode.DARK_GREEN),
        UNDETECTED("Undetected", ColorCode.DARK_BLUE),
        ENDANGERED("Endangered", ColorCode.DARK_PURPLE),
        ELUSIVE("Elusive", ColorCode.GOLD);
        fun getNameTagName(): String {
            return nameTagName
        }

        private val colorCode: ColorCode = color
        fun getColorCode(): ColorCode {
            return colorCode
        }

        private val colorInt: Int = color.getColor()
        fun getColorInt(): Int {
            return colorInt
        }

        companion object {
            fun getFromString(s: String): TrackerRarity? {
                for (type in entries) {
                    if (type.nameTagName == s) {
                        return type
                    }
                }
                return null
            }
        }
    }

    private class TrackedEntity(
        theArmorStand: EntityArmorStand,
        trackerType: TrackerType,
        trackerRarity: TrackerRarity,
    ) {
        private val armorStand: EntityArmorStand = theArmorStand
        fun getArmorStand(): EntityArmorStand {
            return armorStand
        }

        private val type = trackerType
        fun getType(): TrackerType {
            return type
        }

        private val rarity = trackerRarity
        fun getRarity(): TrackerRarity {
            return rarity
        }

        private var animal: Entity? = null
        fun getAnimal(): Entity? {
            return animal
        }

        private var distanceToPlayer = 0.0
        fun getDistanceToPlayer(): Double {
            return distanceToPlayer
        }

        init {
            cacheDistanceToPlayer()
        }

        fun attachAnimal(animalList: MutableList<Entity?>) {
            if (animalList.size == 0) {
                animal = null
            }
            //System.out.println("hi");
            var minDist = Double.MAX_VALUE
            for (e in animalList) {
                // Minimize the distance between entities on the horizontal plane
                val horizDist: Double =
                    (e!!.posX - armorStand.posX) * (e!!.posX - armorStand.posX) + (e!!.posZ - armorStand.posZ) * (e!!.posZ - armorStand.posZ)
                //System.out.println(Math.abs(e.posY - armorStand.posZ));
                if (horizDist < minDist && abs(e.posY - armorStand.posY) < 2) {
                    minDist = horizDist
                    animal = e
                }
            }
        }

        fun cacheDistanceToPlayer() {
            distanceToPlayer = if (animal != null) {
                Minecraft.getMinecraft().thePlayer.getDistanceToEntity(animal).toDouble()
            } else {
                Minecraft.getMinecraft().thePlayer.getDistanceToEntity(armorStand).toDouble()
            }
        }
    }

    companion object {
        private val mushroomIslandLocations: EnumSet<Location> = EnumSet.of<Location>(
            Location.MUSHROOM_DESERT,
            Location.TRAPPERS_DEN,
            Location.DESERT_SETTLEMENT,
            Location.OASIS,
            Location.GLOWING_MUSHROOM_CAVE,
            Location.MUSHROOM_GORGE,
            Location.SHEPHERDS_KEEP,
            Location.OVERGROWN_MUSHROOM_CAVE,
            Location.JAKES_HOUSE,
            Location.TREASURE_HUNTER_CAMP
        )

        private val TRACKED_ANIMAL_NAME_PATTERN: Pattern =
            Pattern.compile("\\[Lv[0-9]+] (?<rarity>[a-zA-Z]+) (?<animal>[a-zA-Z]+) .*❤")
        private val TREVOR_FIND_ANIMAL_PATTERN: Pattern =
            Pattern.compile("\\[NPC] Trevor The Trapper: You can find your [A-Z]+ animal near the [a-zA-Z ]+.")
        private val ANIMAL_DIED_PATTERN: Pattern =
            Pattern.compile("Your mob died randomly, you are rewarded [0-9]+ pelts?.")
        private val ANIMAL_KILLED_PATTERN: Pattern = Pattern.compile("Killing the animal rewarded you [0-9]+ pelts?.")

        private val TICKER_SYMBOL = ResourceLocation("skyblockaddons", "tracker.png")
        private var isTrackingAnimal = false
        private var entityToOutline: TrackedEntity? = null

        /**
         * Draws cell-service-like bars to indicate the proximity to the tracked entity
         *
         * @param mc             the minecraft
         * @param scale          the button scale
         * @param buttonLocation the button location in gui location menu
         */
        // TODO: This should not be static after the feature refactor
        fun drawTrackerLocationIndicator(mc: Minecraft, scale: Float, buttonLocation: ButtonLocation?) {
            val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
            if (buttonLocation != null || isTrackingAnimal) {
                val listener: RenderListener = main.renderListener!!
                var x: Float = main.configValues!!.getActualX(Feature.TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR)
                var y: Float = main.configValues!!.getActualY(Feature.TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR)

                val height = 9
                val width = 3 * 11 + 9

                x = listener.transformXY(x, width, scale)
                y = listener.transformXY(y, height, scale)

                if (buttonLocation != null) {
                    buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale)
                }

                main.utils!!.enableStandardGLOptions()

                val maxTickers = 4
                val fullTickers = if (buttonLocation != null) {
                    3
                } else if (entityToOutline == null) {
                    flashingTickers
                } else if (entityToOutline!!.getDistanceToPlayer() < 16) {
                    4
                } else if (entityToOutline!!.getDistanceToPlayer() < 32) {
                    3
                } else if (entityToOutline!!.getDistanceToPlayer() < 48) {
                    2
                } else if (entityToOutline!!.getDistanceToPlayer() < 64) {
                    1
                } else {
                    flashingTickers
                }
                // Draw the indicator
                for (tickers in 0 until maxTickers) {
                    mc.getTextureManager().bindTexture(TICKER_SYMBOL)
                    GlStateManager.enableAlpha()
                    if (tickers < fullTickers) {
                        DrawUtils.drawModalRectWithCustomSizedTexture(x + tickers * 11, y, 0f, 0f, 9f, 9f, 18f, 9f, false)
                    } else {
                        DrawUtils.drawModalRectWithCustomSizedTexture(x + tickers * 11, y, 9f, 0f, 9f, 9f, 18f, 9f, false)
                    }
                }

                main.utils!!.restoreGLOptions()
            }
        }

        private val flashingTickers: Int
            get() {
                if (CooldownManager.getRemainingCooldown("TREVOR_THE_TRAPPER_HUNT") % 2000 < 1000) {
                    return 0
                }
                return 1
            }
    }
}
