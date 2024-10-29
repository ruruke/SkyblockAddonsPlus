package moe.ruruke.skyblock.config

import cc.polyfrost.oneconfig.libs.checker.units.qual.N
import com.google.gson.*
import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.getGson
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.getLogger
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.instance
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.Language
import moe.ruruke.skyblock.core.Language.Companion.getFromPath
import moe.ruruke.skyblock.features.enchants.EnchantListLayout
import moe.ruruke.skyblock.features.enchants.EnchantManager
import moe.ruruke.skyblock.utils.*
import moe.ruruke.skyblock.utils.EnumUtils.*
import moe.ruruke.skyblock.utils.EnumUtils.AnchorPoint
import moe.ruruke.skyblock.utils.objects.FloatPair
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.crash.CrashReport
import net.minecraft.util.MathHelper
import net.minecraft.util.ReportedException
import org.apache.commons.lang3.mutable.Mutable
import org.apache.commons.lang3.mutable.MutableFloat
import org.apache.commons.lang3.mutable.MutableInt
import org.apache.commons.lang3.mutable.MutableObject
import org.apache.commons.lang3.text.WordUtils
import java.awt.geom.Point2D
import java.beans.Introspector
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.max
import kotlin.math.min


class ConfigValues //    private final MutableObject<EnchantListLayout> enchantLayout = new MutableObject<>(EnchantListLayout.NORMAL);
    (private val settingsConfigFile: File) {
    private val defaultCoordinates: MutableMap<Feature, FloatPair> = EnumMap(
        Feature::class.java
    )
    private val defaultAnchorPoints: MutableMap<Feature, AnchorPoint> = EnumMap(
        Feature::class.java
    )
    private val defaultGuiScales: MutableMap<Feature, Float> = EnumMap(
        Feature::class.java
    )
    private val defaultBarSizes: MutableMap<Feature, FloatPair> = EnumMap(
        Feature::class.java
    )

    private var loadedConfig = JsonObject()


    private var languageConfig = JsonObject()


    private val disabledFeatures: MutableSet<Feature> = EnumSet.noneOf(
        Feature::class.java
    )
    private val colors: MutableMap<Feature, Int> = HashMap()
    private var guiScales: MutableMap<Feature, Float> = EnumMap(
        Feature::class.java
    )
    private val barSizes: MutableMap<Feature, FloatPair> = EnumMap(
        Feature::class.java
    )
    private val warningSeconds = MutableInt(4)
    private val coordinates: MutableMap<Feature, FloatPair> = EnumMap(
        Feature::class.java
    )
    private var anchorPoints: MutableMap<Feature, AnchorPoint> = EnumMap(
        Feature::class.java
    )
    private val language = MutableObject(Language.ENGLISH)
    private val backpackStyle = MutableObject(BackpackStyle.GUI)
    private val powerOrbDisplayStyle = MutableObject(PowerOrbDisplayStyle.COMPACT)
    private val textStyle = MutableObject(EnumUtils.TextStyle.STYLE_ONE)
    private val profileLockedSlots: MutableMap<String, Set<Int>> = HashMap()

    private val chromaFeatures: MutableSet<Feature> = EnumSet.noneOf(
        Feature::class.java
    )

    @Deprecated("")
    private val oldChromaSpeed = MutableFloat(0.19354838f) // 2.0
    private val chromaMode = MutableObject(ChromaMode.FADE)
    private val chromaFadeWidth = MutableFloat(0.22580644f) // 10° Hue

    //    private final MutableObject<DiscordStatus> discordDetails = new MutableObject<>(DiscordStatus.LOCATION);
    //    private final MutableObject<DiscordStatus> discordStatus = new MutableObject<>(DiscordStatus.AUTO_STATUS);
    //    private final MutableObject<DiscordStatus> discordAutoDefault = new MutableObject<>(DiscordStatus.NONE);
    private val discordCustomStatuses: MutableList<String> = ArrayList()

    private val mapZoom = MutableFloat(0.18478261f) // 1.3

    private val healingCircleOpacity = MutableFloat(0.4)


    private val chromaSize = MutableFloat(30f)

    private val chromaSpeed = MutableFloat(6f)

    private val chromaSaturation = MutableFloat(0.75f)

    private val chromaBrightness = MutableFloat(0.9f)

    fun loadValues() {
        try {
            javaClass.classLoader.getResourceAsStream("default.json").use { inputStream ->
                InputStreamReader(
                    Objects.requireNonNull(inputStream),
                    StandardCharsets.UTF_8
                ).use { inputStreamReader ->
                    val defaultValues = getGson().fromJson(
                        inputStreamReader,
                        JsonObject::class.java
                    )
                    deserializeFeatureFloatCoordsMapFromID(defaultValues, defaultCoordinates, "coordinates")
                    deserializeEnumEnumMapFromIDS(
                        defaultValues, defaultAnchorPoints, "anchorPoints",
                        Feature::class.java,
                        AnchorPoint::class.java
                    )
                    deserializeEnumNumberMapFromID(
                        defaultValues, defaultGuiScales, "guiScales",
                        Feature::class.java,
                        Float::class.javaPrimitiveType
                    )
                    deserializeFeatureIntCoordsMapFromID(defaultValues, defaultBarSizes, "barSizes")
                }
            }
        } catch (ex: Exception) {
            val crashReport = CrashReport.makeCrashReport(ex, "Reading default settings file")
            throw ReportedException(crashReport)
        }

        if (settingsConfigFile.exists()) {
            try {
                FileReader(settingsConfigFile).use { reader ->
                    val fileElement = JsonParser().parse(reader)
                    if (fileElement == null || fileElement.isJsonNull) {
                        throw JsonParseException("File is null!")
                    }
                    loadedConfig = fileElement.asJsonObject
                }
            } catch (ex: JsonParseException) {
                logger.error("There was an error loading the config. Resetting all settings to default.")
                logger.catching(ex)
                addDefaultsAndSave()
                return
            } catch (ex: IllegalStateException) {
                logger.error("There was an error loading the config. Resetting all settings to default.")
                logger.catching(ex)
                addDefaultsAndSave()
                return
            } catch (ex: IOException) {
                logger.error("There was an error loading the config. Resetting all settings to default.")
                logger.catching(ex)
                addDefaultsAndSave()
                return
            }
            val configVersion = if (loadedConfig.has("configVersion")) {
                loadedConfig["configVersion"].asInt
            } else {
                CONFIG_VERSION
            }

            deserializeFeatureSetFromID(disabledFeatures, "disabledFeatures")
            deserializeStringIntSetMap(profileLockedSlots, "profileLockedSlots")
            deserializeNumber(warningSeconds, "warningSeconds", Int::class.javaPrimitiveType)

            try {
                if (loadedConfig.has("language")) {
                    val languageKey = loadedConfig["language"].asString
                    val configLanguage = getFromPath(languageKey)
                    if (configLanguage != null) {
                        setLanguage(configLanguage) // TODO Will this crash?
                        //                        language.setValue(configLanguage);
                    }
                }
            } catch (ex: Exception) {
                logger.error("Failed to deserialize path: language")
                logger.catching(ex)
            }

            deserializeEnumValueFromOrdinal(backpackStyle, "backpackStyle")
            deserializeEnumValueFromOrdinal(powerOrbDisplayStyle, "powerOrbStyle")
            deserializeEnumEnumMapFromIDS(
                anchorPoints, "anchorPoints",
                Feature::class.java,
                AnchorPoint::class.java
            )
            deserializeEnumNumberMapFromID(
                guiScales, "guiScales",
                Feature::class.java,
                Float::class.javaPrimitiveType
            )

            try {
                for (feature in Feature.entries) { // TODO Legacy format from 1.3.4, remove in the future.
                    val property =
                        Introspector.decapitalize(WordUtils.capitalizeFully(feature.toString().replace("_", " ")))
                            .replace(" ", "")
                    val x = property + "X"
                    val y = property + "Y"
                    if (loadedConfig.has(x)) {
                        coordinates[feature] = FloatPair(loadedConfig[x].asFloat, loadedConfig[y].asFloat)
                    }
                }
            } catch (ex: Exception) {
                logger.error("Failed to deserialize path: coordinates (legacy)")
                logger.catching(ex)
            }

            if (loadedConfig.has("coordinates")) {
                deserializeFeatureFloatCoordsMapFromID(coordinates, "coordinates")
            } else {
                deserializeFeatureFloatCoordsMapFromID(
                    coordinates,
                    "guiPositions"
                ) // TODO Legacy format from 1.4.2/1.5-betas, remove in the future.
            }
            deserializeFeatureIntCoordsMapFromID(barSizes, "barSizes")

            if (loadedConfig.has("featureColors")) { // TODO Legacy format from 1.3.4, remove in the future.
                try {
                    for ((key, value) in loadedConfig.getAsJsonObject("featureColors").entrySet()) {
                        val feature = Feature.fromId(key.toInt())
                        if (feature != null) {
                            val colorCode: ColorCode = ColorCode.values().get(value.asInt)
                            if (colorCode.isColor() && colorCode !== ColorCode.RED) { // Red is default, no need to set it.
                                colors[feature] = colorCode.getColor()
                            }
                        }
                    }
                } catch (ex: Exception) {
                    logger.error("Failed to deserialize path: featureColors")
                    logger.catching(ex)
                }
            } else {
                deserializeEnumNumberMapFromID(
                    colors, "colors",
                    Feature::class.java,
                    Int::class.javaPrimitiveType
                )
            }

            deserializeEnumValueFromOrdinal(textStyle, "textStyle")
            deserializeFeatureSetFromID(chromaFeatures, "chromaFeatures")
            if (configVersion <= 8) {
                deserializeNumber(oldChromaSpeed, "chromaSpeed", Float::class.javaPrimitiveType)
                chromaSpeed.setValue(MathUtils.denormalizeSliderValue(oldChromaSpeed.toFloat(), 0.1f, 10f, 0.5f))
            } else {
                deserializeNumber(chromaSpeed, "chromaSpeed", Float::class.javaPrimitiveType)
            }
            deserializeNumber(chromaSize, "chromaSize", Float::class.javaPrimitiveType)
            deserializeEnumValueFromOrdinal(chromaMode, "chromaMode")
            deserializeNumber(chromaFadeWidth, "chromaFadeWidth", Float::class.javaPrimitiveType)
//            deserializeEnumValueFromOrdinal(discordStatus, "discordStatus")
//            deserializeEnumValueFromOrdinal(discordDetails, "discordDetails")
//            deserializeEnumValueFromOrdinal(discordAutoDefault, "discordAutoDefault")
            deserializeStringCollection(discordCustomStatuses, "discordCustomStatuses")
            deserializeEnumValueFromOrdinal(enchantLayout, "enchantLayout")

            deserializeNumber(mapZoom, "mapZoom", Float::class.javaPrimitiveType)
            deserializeNumber(chromaSaturation, "chromaSaturation", Float::class.javaPrimitiveType)
            deserializeNumber(chromaBrightness, "chromaBrightness", Float::class.javaPrimitiveType)

            if (configVersion <= 5) {
                disabledFeatures.add(Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS)
            } else if (configVersion <= 6) {
                putDefaultBarSizes()
                for ((key, coords) in coordinates) {
                    if (getAnchorPoint(key!!) == AnchorPoint.BOTTOM_MIDDLE) {
                        coords.x = coords.x - 91
                        coords.y = coords.y - 39
                    }
                }
            } else if (configVersion <= 7) {
                for ((feature, coords) in coordinates) {
                    if (feature === Feature.DARK_AUCTION_TIMER || feature === Feature.FARM_EVENT_TIMER || feature === Feature.ZEALOT_COUNTER || feature === Feature.SKILL_DISPLAY || feature === Feature.SHOW_TOTAL_ZEALOT_COUNT || feature === Feature.SHOW_SUMMONING_EYE_COUNT || feature === Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE || feature === Feature.BIRCH_PARK_RAINMAKER_TIMER || feature === Feature.ENDSTONE_PROTECTOR_DISPLAY) {
                        coords.y = coords.y + 2 / 2f
                        coords.x = coords.x - 18 / 2f
                        coords.y = coords.y - 9 / 2f
                    }

                    if (feature.getGuiFeatureData() != null && feature.getGuiFeatureData()!!.getDrawType() === DrawType.BAR
                    ) {
                        coords.y = coords.y + 1
                    }
                }
            }

            val lastFeatureID: Int
            if (loadedConfig.has("lastFeatureID")) {
                lastFeatureID = loadedConfig["lastFeatureID"].asInt
            } else {
                // This system was added after this feature.
                lastFeatureID = Feature.SKYBLOCK_ADDONS_BUTTON_IN_PAUSE_MENU.getId()
            }
            // This will go through every feature, and if they are new features that didn't exist before
            // that should be disabled by default, and their coordinates are default, this will disable those features.
            for (feature in Feature.entries) {
                if (feature.getId() > lastFeatureID && feature.isDefaultDisabled() && featureCoordinatesAreDefault(
                        feature
                    )
                ) {
                    this.disabledFeatures.add(feature)
                }
            }
        } else {
            addDefaultsAndSave()
        }
    }

    private fun addDefaultsAndSave() {
        val mc = Minecraft.getMinecraft()
        if (mc != null) {
            if (mc.languageManager != null && mc.languageManager.currentLanguage.languageCode != null) {
                val minecraftLanguage =
                    Minecraft.getMinecraft().languageManager.currentLanguage.languageCode.lowercase()
                val configLanguage = getFromPath(minecraftLanguage)
                if (configLanguage != null) { // Check if we have the exact locale they are using for Minecraft
                    language.setValue(configLanguage)
                } else { // Check if we at least have the same language (different locale)
                    val languageCode = minecraftLanguage.split("_".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[0]
                    for (loopLanguage in Language.entries) {
                        val loopLanguageCode: String = loopLanguage.getPath().split("_").get(0)
                        if (loopLanguageCode == languageCode) {
                            language.setValue(loopLanguage)
                            break
                        }
                    }
                }
            }
        }

        for (feature in Feature.entries) {
            val color: ColorCode? = feature.defaultColor
            if (color != null) {
                colors[feature] = color.getColor()
            }
            if (feature.isDefaultDisabled()) {
                disabledFeatures.add(feature)
            }
        }

        setAllCoordinatesToDefault()
        putDefaultBarSizes()
        saveConfig()
    }

    fun saveConfig() {
        EnchantManager.markCacheDirty()
        SkyblockAddonsPlus.instance.run {
            if (!SAVE_LOCK.tryLock()) {
                return
            }
            logger.info("Saving config")

            try {
                settingsConfigFile.createNewFile()

                val saveConfig = JsonObject()

                val jsonArray = JsonArray()
                for (element in disabledFeatures) {
                    jsonArray.add(GsonBuilder().create().toJsonTree(element.getId()))
                }
                saveConfig.add("disabledFeatures", jsonArray)

                val profileSlotsObject = JsonObject()
                for ((key, value) in profileLockedSlots) {
                    val lockedSlots = JsonArray()
                    for (slot in value) {
                        lockedSlots.add(GsonBuilder().create().toJsonTree(slot))
                    }
                    profileSlotsObject.add(key, lockedSlots)
                }
                saveConfig.add("profileLockedSlots", profileSlotsObject)

                val anchorObject = JsonObject()
                for (feature in Feature.guiFeatures) {
                    anchorObject.addProperty(java.lang.String.valueOf(feature.getId()), getAnchorPoint(feature).id)
                }
                saveConfig.add("anchorPoints", anchorObject)

                val scalesObject = JsonObject()
                for (feature in guiScales.keys) {
                    scalesObject.addProperty(java.lang.String.valueOf(feature.getId()), guiScales[feature])
                }
                saveConfig.add("guiScales", scalesObject)

                val colorsObject = JsonObject()
                for (feature in colors.keys) {
                    val featureColor = colors[feature]!!
                    if (featureColor != ColorCode.RED.getColor()) { // Red is default, no need to save it!
                        colorsObject.addProperty(java.lang.String.valueOf(feature.getId()), colors[feature])
                    }
                }
                saveConfig.add("colors", colorsObject)

                // Old gui coordinates, for backwards compatibility...
                var coordinatesObject = JsonObject()
                for (feature in coordinates.keys) {
                    val coordinatesArray = JsonArray()
                    coordinatesArray.add(
                        GsonBuilder().create().toJsonTree(
                            Math.round(
                                coordinates[feature]!!.x
                            )
                        )
                    )
                    coordinatesArray.add(
                        GsonBuilder().create().toJsonTree(
                            Math.round(
                                coordinates[feature]!!.y
                            )
                        )
                    )
                    coordinatesObject.add(java.lang.String.valueOf(feature.getId()), coordinatesArray)
                }
                saveConfig.add("guiPositions", coordinatesObject)
                // New gui coordinates
                coordinatesObject = JsonObject()
                for (feature in coordinates.keys) {
                    val coordinatesArray = JsonArray()
                    coordinatesArray.add(
                        GsonBuilder().create().toJsonTree(coordinates[feature]!!.x)
                    )
                    coordinatesArray.add(
                        GsonBuilder().create().toJsonTree(coordinates[feature]!!.y)
                    )
                    coordinatesObject.add(java.lang.String.valueOf(feature.getId()), coordinatesArray)
                }
                saveConfig.add("coordinates", coordinatesObject)

                val barSizesObject = JsonObject()
                for (feature in barSizes.keys) {
                    val sizesArray = JsonArray()
                    sizesArray.add(GsonBuilder().create().toJsonTree(barSizes[feature]!!.x))
                    sizesArray.add(GsonBuilder().create().toJsonTree(barSizes[feature]!!.y))
                    barSizesObject.add(java.lang.String.valueOf(feature.getId()), sizesArray)
                }
                saveConfig.add("barSizes", barSizesObject)

                saveConfig.addProperty("warningSeconds", warningSeconds)

                saveConfig.addProperty("textStyle", textStyle.value.ordinal)
                saveConfig.addProperty("language", language.value.getPath())
                saveConfig.addProperty("backpackStyle", backpackStyle.value.ordinal)
                saveConfig.addProperty("powerOrbStyle", powerOrbDisplayStyle.value.ordinal)

                val chromaFeaturesArray = JsonArray()
                for (feature in chromaFeatures) {
                    chromaFeaturesArray.add(GsonBuilder().create().toJsonTree(feature.getId()))
                }
                saveConfig.add("chromaFeatures", chromaFeaturesArray)
                saveConfig.addProperty("chromaSpeed", chromaSpeed)
                saveConfig.addProperty("chromaMode", chromaMode.value.ordinal)
                saveConfig.addProperty("chromaSize", chromaSize)

//                saveConfig.addProperty("discordStatus", discordStatus.getValue().ordinal())
//                saveConfig.addProperty("discordDetails", discordDetails.getValue().ordinal())
//                saveConfig.addProperty("discordAutoDefault", discordAutoDefault.getValue().ordinal())
                saveConfig.addProperty("enchantLayout", enchantLayout.value.ordinal)

                val discordCustomStatusesArray = JsonArray()
                for (string in discordCustomStatuses) {
                    discordCustomStatusesArray.add(GsonBuilder().create().toJsonTree(string))
                }
                saveConfig.add("discordCustomStatuses", discordCustomStatusesArray)

                saveConfig.addProperty("mapZoom", mapZoom)
                saveConfig.addProperty("chromaSaturation", chromaSaturation)
                saveConfig.addProperty("chromaBrightness", chromaBrightness)

                saveConfig.addProperty(
                    "configVersion",
                    CONFIG_VERSION
                )
                var largestFeatureID = 0
                for (feature in Feature.entries) {
                    if (feature.getId() > largestFeatureID) largestFeatureID = feature.getId()
                }
                saveConfig.addProperty("lastFeatureID", largestFeatureID)

                FileWriter(settingsConfigFile).use { writer ->
                    SkyblockAddonsPlus.getGson().toJson(saveConfig, writer)
                }
            } catch (ex: Exception) {
                logger.error("An error occurred while attempting to save the config!")
                logger.catching(ex)
            }

            SAVE_LOCK.unlock()
            logger.info("Config saved")
        }
    }


    private fun deserializeFeatureSetFromID(collection: MutableCollection<Feature>, path: String) {
        try {
            if (loadedConfig.has(path)) {
                for (element in loadedConfig.getAsJsonArray(path)) {
                    val feature = Feature.fromId(element.asInt)
                    if (feature != null) {
                        collection.add(feature)
                    }
                }
            }
        } catch (ex: Exception) {
            logger.error("Failed to deserialize path: $path")
            logger.catching(ex)
        }
    }

    private fun deserializeStringCollection(collection: MutableCollection<String>, path: String) {
        try {
            if (loadedConfig.has(path)) {
                for (element in loadedConfig.getAsJsonArray(path)) {
                    val string = element.asString
                    if (string != null) {
                        collection.add(string)
                    }
                }
            }
        } catch (ex: Exception) {
            logger.error("Failed to deserialize path: $path")
            logger.catching(ex)
        }
    }

    private fun deserializeStringIntSetMap(map: MutableMap<String, Set<Int>>, path: String) {
        try {
            if (loadedConfig.has(path)) {
                val profileSlotsObject = loadedConfig.getAsJsonObject(path)
                for ((key, value) in profileSlotsObject.entrySet()) {
                    val slots: MutableSet<Int> = HashSet()
                    for (element in value.asJsonArray) {
                        slots.add(element.asInt)
                    }
                    map[key] = slots
                }
            }
        } catch (ex: Exception) {
            logger.error("Failed to deserialize path: $path")
            logger.catching(ex)
        }
    }

    private fun <E : Enum<*>?, F : Enum<*>?> deserializeEnumEnumMapFromIDS(
        map: MutableMap<E, F>,
        path: String,
        keyClass: Class<E>,
        valueClass: Class<F>
    ) {
        deserializeEnumEnumMapFromIDS(loadedConfig, map, path, keyClass, valueClass)
    }

    private fun <E : Enum<*>?, F : Enum<*>?> deserializeEnumEnumMapFromIDS(
        jsonObject: JsonObject,
        map: MutableMap<E, F>,
        path: String,
        keyClass: Class<E>,
        valueClass: Class<F>
    ) {
//        try {
//            if (jsonObject.has(path)) {
//                for ((key1, value1) in jsonObject.getAsJsonObject(path).entrySet()) {
//                    var fromId = keyClass.getDeclaredMethod("fromId", Int::class.javaPrimitiveType)
//                    val key: E? = fromId.invoke(null, key1.toInt()) as E
//
//                    fromId = valueClass.getDeclaredMethod("fromId", Int::class.javaPrimitiveType)
//                    val value: F? = fromId.invoke(null, value1.asInt) as F
//
//                    if (key != null && value != null) {
//                        map[key] = value
//                    }
//                }
//            }
//        } catch (ex: Exception) {
//            logger.error("Failed to deserialize path: $path")
//            logger.catching(ex)
//        }
    }

    private fun <E : Enum<*>, N : Number> deserializeEnumNumberMapFromID(
        map: MutableMap<E, N>,
        path: String,
        keyClass: Class<E>,
        numberClass: Class<N>?
    ) {
        deserializeEnumNumberMapFromID(loadedConfig, map, path, keyClass, numberClass)
    }

    private fun <E : Enum<*>?, N : Number?> deserializeEnumNumberMapFromID(
        jsonObject: JsonObject,
        map: MutableMap<E, N>,
        path: String,
        keyClass: Class<E>,
        numberClass: Class<N>?
    ) {
        try {
            if (jsonObject.has(path)) {
                for ((element, value) in jsonObject.getAsJsonObject(path).entrySet()) {
                    var fromId = keyClass.getDeclaredMethod("fromId", Int::class.javaPrimitiveType)
                    val key: E? = fromId.invoke(null, element.toInt()) as E

                    fromId = value.javaClass.getDeclaredMethod("fromId", Int::class.javaPrimitiveType)
                    val value: N = fromId.invoke(null, element.toInt()) as N

                    if (key != null && value != null) {
                        map[key] = value
                    }
                }
            }
        } catch (ex: java.lang.Exception) {
            logger.error("Failed to deserialize path: $path")
            logger.catching(ex)
        }
    }

    private fun <N : Number> deserializeNumber(number: Mutable<Number?>, path: String, numberClass: Class<N>?) {
        try {
            if (loadedConfig.has(path)) {
                number.setValue(getNumber(loadedConfig[path], numberClass))
            }
        } catch (ex: Exception) {
            logger.error("Failed to deserialize path: $path")
            logger.catching(ex)
        }
    }

    private fun getNumber(jsonElement: JsonElement, numberClass: Class<out Number>?): Number? {
        if (numberClass == Byte::class.javaPrimitiveType) {
            return jsonElement.asByte
        } else if (numberClass == Short::class.javaPrimitiveType) {
            return jsonElement.asShort
        } else if (numberClass == Int::class.javaPrimitiveType) {
            return jsonElement.asInt
        } else if (numberClass == Long::class.javaPrimitiveType) {
            return jsonElement.asLong
        } else if (numberClass == Float::class.javaPrimitiveType) {
            return jsonElement.asFloat
        } else if (numberClass == Double::class.javaPrimitiveType) {
            return jsonElement.asDouble
        }

        return null
    }

    private fun <E : Enum<*>> deserializeEnumValueFromOrdinal(value: MutableObject<E>, path: String) {
        try {
            val enumClass: Class<*>? = value.value.javaClass.declaringClass
            val method = enumClass!!.getDeclaredMethod("values")
            val valuesObject = method.invoke(null)
            val values = valuesObject as Array<E>

            if (loadedConfig.has(path)) {
                val ordinal = loadedConfig[path].asInt
                if (values.size > ordinal) {
                    val enumValue: E? = values[ordinal]
                    if (enumValue != null) {
                        value.setValue(values[ordinal])
                    }
                }
            }
        } catch (ex: Exception) {
            logger.error("Failed to deserialize path: $path")
            logger.catching(ex)
        }
    }

    private fun deserializeFeatureFloatCoordsMapFromID(map: MutableMap<Feature, FloatPair>, path: String) {
        deserializeFeatureFloatCoordsMapFromID(loadedConfig, map, path)
    }

    private fun deserializeFeatureFloatCoordsMapFromID(
        jsonObject: JsonObject,
        map: MutableMap<Feature, FloatPair>,
        path: String
    ) {
        try {
            if (jsonObject.has(path)) {
                for ((key, value) in jsonObject.getAsJsonObject(path).entrySet()) {
                    val feature = Feature.fromId(key.toInt())
                    if (feature != null) {
                        val coords = value.asJsonArray
                        map[feature] = FloatPair(coords[0].asFloat, coords[1].asFloat)
                    }
                }
            }
        } catch (ex: Exception) {
            logger.error("Failed to deserialize path: $path")
            logger.catching(ex)
        }
    }

    private fun deserializeFeatureIntCoordsMapFromID(map: MutableMap<Feature, FloatPair>, path: String) {
        deserializeFeatureIntCoordsMapFromID(loadedConfig, map, path)
    }

    private fun deserializeFeatureIntCoordsMapFromID(
        jsonObject: JsonObject,
        map: MutableMap<Feature, FloatPair>,
        path: String
    ) {
        try {
            if (jsonObject.has(path)) {
                for ((key, value) in jsonObject.getAsJsonObject(path).entrySet()) {
                    val feature = Feature.fromId(key.toInt())
                    if (feature != null) {
                        val coords = value.asJsonArray
                        map[feature] = FloatPair(coords[0].asFloat, coords[1].asFloat)
                    }
                }
            }
        } catch (ex: Exception) {
            logger.error("Failed to deserialize path: $path")
            logger.catching(ex)
        }
    }

    fun setAllCoordinatesToDefault() {
        coordinates.clear()
        for ((key, value) in defaultCoordinates) {
            coordinates[key] = value.cloneCoords()
        }

        anchorPoints = HashMap(defaultAnchorPoints)

        guiScales = HashMap(defaultGuiScales)
    }

    private fun putDefaultCoordinates(feature: Feature) {
        val coords = defaultCoordinates[feature]
        if (coords != null) {
            coordinates[feature] = coords
        }
    }

    fun putDefaultBarSizes() {
        barSizes.clear()
        for ((key, value) in defaultBarSizes) {
            barSizes[key] = value.cloneCoords()
        }
    }

    /**
     * Checks the received `OnlineData` to determine if the given feature should be disabled.
     * This method checks the list of features to be disabled for all versions first and then checks the list of features that
     * should be disabled for this specific version.
     *
     * @param feature The feature to check
     * @return `true` if the feature should be disabled, `false` otherwise
     */
    fun isRemoteDisabled(feature: Feature?): Boolean {
        if (feature == null) return false
        if(Minecraft.getMinecraft().isSingleplayer) return false;

        val disabledFeatures: HashMap<String, List<Int>>? = SkyblockAddonsPlus.getOnlineData()!!.getDisabledFeatures()//main.onlineData.getDisabledFeatures()

        if (disabledFeatures!!.containsKey("all")) {
            if (disabledFeatures["all"] != null) {
                if (disabledFeatures["all"]!!.contains(feature.getId())) {
                    return true
                }
            } else {
                logger.error("\"all\" key in disabled features map has value of null. Please fix online data.")
            }
        }

        /*
        Check for disabled features for this mod version. Pre-release versions will follow the disabled features
        list for their release version. For example, the version {@code 1.6.0-beta.10} will adhere to the list
        for version {@code 1.6.0}
         */
        var version: String = SkyblockAddonsPlus.VERSION
        if (version.contains("-")) {
            version = version.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        }
        if (disabledFeatures.containsKey(version)) {
            if (disabledFeatures[version] != null) {
                return disabledFeatures[version]!!.contains(feature.getId())
            } else {
                logger.error("\"$version\" key in disabled features map has value of null. Please fix online data.")
            }
        }

        return false
    }

    /**
     * @param feature The feature to check.
     * @return Whether the feature is disabled.
     */
    fun isDisabled(feature: Feature): Boolean {
        return disabledFeatures.contains(feature) || isRemoteDisabled(feature)
    }

    /**
     * @param feature The feature to check.
     * @return Whether the feature is enabled.
     */
    fun isEnabled(feature: Feature): Boolean {
        return false//!isDisabled(feature)
    }

    // TODO Don't force alpha in the future...
    fun getColor(feature: Feature): Int {
        return this.getColor(feature, 255)
    }

    fun getColor(feature: Feature, alpha: Int): Int {
        // If the minimum alpha value is being limited let's make sure we are a little higher than that
//        if (GlStateManager.alphaState.alphaTest && GlStateManager.alphaState.func == GL11.GL_GREATER && alpha / 255F <= GlStateManager.alphaState.ref) {
//            alpha = ColorUtils.getAlphaIntFromFloat( GlStateManager.alphaState.ref + 0.001F);
//        }

        if (chromaFeatures.contains(feature)) {
            //TODO:
//            return ManualChromaManager.getChromaColor(0, 0, alpha)
        }

        if (colors.containsKey(feature)) {
            return colors[feature]?.let { ColorUtils.setColorAlpha(it, alpha) }!!
        }

        val defaultColor: ColorCode? = feature.defaultColor
        return ColorUtils.setColorAlpha(
            if (defaultColor != null) defaultColor.getColor() else ColorCode.RED.getColor(),
            alpha
        )
    }

    /**
     * Return skyblock color compatible with new shaders. Can bind the color (white) unconditionally
     * @param feature the feature
     * @return the color
     */
    fun getSkyblockColor(feature: Feature): SkyblockColor {
        val color: SkyblockColor = ColorUtils.getDummySkyblockColor(getColor(feature), chromaFeatures.contains(feature))
        // If chroma is enabled, and we are using shaders, set color to white
        if (color.drawMulticolorUsingShader()) {
            color.setColor(-0x1)
        }
        return color
    }

    fun getRestrictedColor(feature: Feature): ColorCode? {
        val featureColor = colors[feature]

        if (featureColor != null) {
            for (colorCode in ColorCode.values()) {
                if (!colorCode.isColor()) {
                    continue
                }

                if (colorCode.getColor() === featureColor) {
                    return colorCode
                }
            }
        }

        return feature.defaultColor
    }

    private fun featureCoordinatesAreDefault(feature: Feature): Boolean {
        if (!defaultCoordinates.containsKey(feature)) {
            return true
        }
        if (!coordinates.containsKey(feature)) {
            return true
        }

        return coordinates[feature] == defaultCoordinates[feature]
    }

    fun setColor(feature: Feature, color: Int) {
        colors[feature] = color
    }

    fun getActualX(feature: Feature): Float {
        val maxX = ScaledResolution(Minecraft.getMinecraft()).scaledWidth
        return getAnchorPoint(feature).getX(maxX) + getRelativeCoords(feature)!!.x
    }

    fun getActualY(feature: Feature): Float {
        val maxY = ScaledResolution(Minecraft.getMinecraft()).scaledHeight
        return getAnchorPoint(feature).getY(maxY) + getRelativeCoords(feature)!!.y
    }

    fun getSizes(feature: Feature): FloatPair {
        return barSizes.getOrDefault(
            feature,
            if (defaultBarSizes.containsKey(feature)) defaultBarSizes[feature]!!.cloneCoords() else FloatPair(1f, 1f)
        )
    }

    fun getSizesX(feature: Feature): Float {
        return min(max(getSizes(feature).x.toDouble(), .25), 1.0).toFloat()
    }

    fun getSizesY(feature: Feature): Float {
        return min(max(getSizes(feature).y.toDouble(), .25), 1.0).toFloat()
    }

    fun setScaleX(feature: Feature, x: Float) {
        val coords = getSizes(feature)
        coords.x = x
    }

    fun setScaleY(feature: Feature, y: Float) {
        val coords = getSizes(feature)
        coords.y = y
    }

    fun getRelativeCoords(feature: Feature): FloatPair? {
        if (coordinates.containsKey(feature)) {
            return coordinates[feature]
        } else {
            putDefaultCoordinates(feature)
            return if (coordinates.containsKey(feature)) {
                coordinates[feature]
            } else {
                FloatPair(0f, 0f)
            }
        }
    }

    fun setCoords(feature: Feature, x: Float, y: Float) {
        if (coordinates.containsKey(feature)) {
            coordinates[feature]!!.x = x
            coordinates[feature]!!.y = y
        } else {
            coordinates[feature] = FloatPair(x, y)
        }
    }

    fun getClosestAnchorPoint(x: Float, y: Float): AnchorPoint {
        val sr = ScaledResolution(Minecraft.getMinecraft())
        val maxX = sr.scaledWidth
        val maxY = sr.scaledHeight
        var shortestDistance = -1.0
        var closestAnchorPoint = AnchorPoint.BOTTOM_MIDDLE // default
        for (point in AnchorPoint.entries) {
            val distance =
                Point2D.distance(x.toDouble(), y.toDouble(), point.getX(maxX).toDouble(), point.getY(maxY).toDouble())
            if (shortestDistance == -1.0 || distance < shortestDistance) {
                closestAnchorPoint = point
                shortestDistance = distance
            }
        }
        return closestAnchorPoint
    }

    fun setClosestAnchorPoint(feature: Feature) {
        val x1 = getActualX(feature)
        val y1 = getActualY(feature)
        val sr = ScaledResolution(Minecraft.getMinecraft())
        val maxX = sr.scaledWidth
        val maxY = sr.scaledHeight
        var shortestDistance = -1.0
        var closestAnchorPoint = AnchorPoint.BOTTOM_MIDDLE // default
        for (point in AnchorPoint.entries) {
            val distance =
                Point2D.distance(x1.toDouble(), y1.toDouble(), point.getX(maxX).toDouble(), point.getY(maxY).toDouble())
            if (shortestDistance == -1.0 || distance < shortestDistance) {
                closestAnchorPoint = point
                shortestDistance = distance
            }
        }
        if (this.getAnchorPoint(feature) == closestAnchorPoint) {
            return
        }
        val targetX = getActualX(feature)
        val targetY = getActualY(feature)
        val x = targetX - closestAnchorPoint.getX(maxX)
        val y = targetY - closestAnchorPoint.getY(maxY)
        anchorPoints[feature] = closestAnchorPoint
        setCoords(feature, x, y)
    }

    fun getAnchorPoint(feature: Feature): AnchorPoint {
        return anchorPoints.getOrDefault(feature, defaultAnchorPoints.getOrDefault(feature, AnchorPoint.BOTTOM_MIDDLE))
    }

    val lockedSlots: Set<Int>
        get() {
            val profile: String =
                main.utils!!.getProfileName()
            if (!profileLockedSlots.containsKey(profile)) {
                profileLockedSlots[profile] = HashSet()
            }

            return profileLockedSlots[profile]!!
        }

    fun setGuiScale(feature: Feature, scale: Float) {
        guiScales[feature] = scale
    }

    fun getGuiScale(feature: Feature): Float {
        return getGuiScale(feature, true)
    }

    fun getGuiScale(feature: Feature, denormalized: Boolean): Float {
        var value = DEFAULT_GUI_SCALE
        if (guiScales.containsKey(feature)) {
            value = guiScales[feature]!!
        }
        if (denormalized) {
            value = denormalizeScale(value)
        }
        return value
    }

    fun setChroma(feature: Feature, enabled: Boolean) {
        if (enabled) {
            chromaFeatures.add(feature)
        } else {
            chromaFeatures.remove(feature)
        }
    }

    fun getWarningSeconds(): Int {
        return warningSeconds.value
    }

    fun setWarningSeconds(warningSeconds: Int) {
        this.warningSeconds.setValue(warningSeconds)
    }

    fun getLanguage(): Language {
        return language.value
    }
    fun getLanguageConfig(): JsonObject {
        return languageConfig;
    }
    fun setLanguageConfig(langConfig: JsonObject){
        languageConfig = langConfig;
    }

    fun setLanguage(language: Language) {
        this.language.value = language
    }

    fun getBackpackStyle(): BackpackStyle {
        return backpackStyle.value
    }

    fun setBackpackStyle(backpackStyle: BackpackStyle) {
        this.backpackStyle.value = backpackStyle
    }

    fun getPowerOrbDisplayStyle(): PowerOrbDisplayStyle {
        return powerOrbDisplayStyle.value
    }

    fun setPowerOrbDisplayStyle(powerOrbDisplayStyle: PowerOrbDisplayStyle) {
        this.powerOrbDisplayStyle.value = powerOrbDisplayStyle
    }

    fun getTextStyle(): EnumUtils.TextStyle {
        return textStyle.value
    }

    fun setTextStyle(textStyle: EnumUtils.TextStyle) {
        this.textStyle.value = textStyle
    }

    fun getChromaMode(): ChromaMode {
        return chromaMode.value
    }

    fun setChromaMode(chromaMode: ChromaMode) {
        this.chromaMode.setValue(chromaMode)
    }

    fun setChromaFadeWidth(chromaFadeWidth: Float) {
        this.chromaFadeWidth.setValue(chromaFadeWidth)
    }

    fun getChromaFadeWidth(): Float {
        return chromaFadeWidth.value
    }
//
//    var discordStatus: DiscordStatus
//        get() = if (field != null) field.getValue() else DiscordStatus.NONE
//        set(discordStatus) {
//            field.setValue(discordStatus)
//        }
//
//    var discordDetails: DiscordStatus
//        get() = if (field != null) field.getValue() else DiscordStatus.NONE
//        set(discordDetails) {
//            field.setValue(discordDetails)
//        }
//
//    var discordAutoDefault: DiscordStatus
//        get() = if (field != null) field.getValue() else DiscordStatus.NONE
//        set(discordAutoDefault) {
//            field.setValue(discordAutoDefault)
//        }

//    fun getCustomStatus(statusEntry: DiscordStatusEntry): String {
//        while (main.configValues.getDiscordCustomStatuses().size() < 2) {
//            main.configValues.getDiscordCustomStatuses().add("")
//        }
//
//        return discordCustomStatuses[statusEntry.getId()]
//    }
//
//    fun setCustomStatus(statusEntry: DiscordStatusEntry, text: String): String {
//        while (main.getConfigValues().getDiscordCustomStatuses().size() < 2) {
//            main.getConfigValues().getDiscordCustomStatuses().add("")
//        }
//
//        return discordCustomStatuses.set(statusEntry.getId(), text)
//    }


    val enchantLayout = MutableObject(EnchantListLayout.NORMAL)
    companion object {
        private const val CONFIG_VERSION = 9

        private val DEFAULT_GUI_SCALE = normalizeValueNoStep(1f)
        private const val GUI_SCALE_MINIMUM = 0.5f
        private const val GUI_SCALE_MAXIMUM = 5f

        private val SAVE_LOCK = ReentrantLock()

        private val main = instance
        private val logger = getLogger()

        fun normalizeValueNoStep(value: Float): Float {
            return MathHelper.clamp_float(
                (snapNearDefaultValue(value) - GUI_SCALE_MINIMUM) /
                        (GUI_SCALE_MAXIMUM - GUI_SCALE_MINIMUM), 0.0f, 1.0f
            )
        }

        /** These two are taken from GuiOptionSlider.  */
        fun denormalizeScale(value: Float): Float {
            return snapNearDefaultValue(
                GUI_SCALE_MINIMUM + (GUI_SCALE_MAXIMUM - GUI_SCALE_MINIMUM) *
                        MathHelper.clamp_float(value, 0.0f, 1.0f)
            )
        }

        fun snapNearDefaultValue(value: Float): Float {
            if (value != 1f && value > 1 - 0.05 && value < 1 + 0.05) {
                return 1f
            }

            return value
        }
    }
}
