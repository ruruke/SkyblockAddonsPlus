package moe.ruruke.skyblock.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.Dropdown
import cc.polyfrost.oneconfig.config.annotations.HUD
import cc.polyfrost.oneconfig.config.annotations.Slider
import cc.polyfrost.oneconfig.config.annotations.Switch
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.config.data.OptionSize
import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.getLogger
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.hud.TestHud
import net.minecraft.client.Minecraft
import java.util.*

/**
 * The main Config entrypoint that extends the Config type and inits the config options.
 * See [this link](https://docs.polyfrost.cc/oneconfig/config/adding-options) for more config Options
 */
class NewConfigValue : Config(Mod(SkyblockAddonsPlus.NAME, ModType.UTIL_QOL), SkyblockAddonsPlus.MODID + ".json") {
    init {
        initialize()
    }
    @Switch(name = "Replace Roman Numerals on Items", size = OptionSize.SINGLE, category = "WIP")
    var replaceRomanNumeralsWithNumbers: Boolean = false // The default value for the boolean Switch.

    //Dummy
    @HUD(name = "Example HUD", category = "dummy")
    var hud: TestHud = TestHud()

    @Switch(name = "Example Switch", size = OptionSize.SINGLE, category = "dummy")
    var exampleSwitch: Boolean = false // The default value for the boolean Switch.

    @Slider(name = "Example Slider", min = 0f, max = 100f, step = 10, category = "dummy")
    var exampleSlider: Float = 50f // The default value for the float Slider.

    @Dropdown(name = "Example Dropdown", options = ["Option 1", "Option 2", "Option 3", "Option 4"], category = "dummy")
    var exampleDropdown: Int = 1 // Default option (in this case "Option 2")


    private val disabledFeatures: MutableSet<Feature> = EnumSet.noneOf(
        Feature::class.java
    )
    private val logger = getLogger()
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

        val disabledFeatures: HashMap<String, List<Int>>? = SkyblockAddonsPlus.onlineData!!.getDisabledFeatures()//main.onlineData.getDisabledFeatures()

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
        if(isRemoteDisabled(feature)){
            return true
        }
        when(feature.getId()) {
            45 -> return replaceRomanNumeralsWithNumbers
        }
        return false
    }

    /**
     * @param feature The feature to check.
     * @return Whether the feature is enabled.
     */
    fun isEnabled(feature: Feature): Boolean {
        return isDisabled(feature)
    }
}

