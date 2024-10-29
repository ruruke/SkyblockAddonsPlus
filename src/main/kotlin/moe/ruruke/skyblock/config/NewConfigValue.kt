package moe.ruruke.skyblock.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.*
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
class NewConfigValue : Config(Mod(SkyblockAddonsPlus.NAME, ModType.SKYBLOCK), SkyblockAddonsPlus.MODID + "_v2.json") {
    init {
        initialize()
    }
    @Switch(name = "Replace Roman Numerals on Items", size = OptionSize.SINGLE)
    var replaceRomanNumeralsWithNumbers: Boolean = false // The default value for the boolean Switch.

    //Dummy
//    @Exclude
//    @HUD(name = "Example HUD", category = "dummy")
//    var hud: TestHud = TestHud()

//    @Switch(name = "Example Switch", size = OptionSize.SINGLE, category = "dummy")
//    var exampleSwitch: Boolean = false // The default value for the boolean Switch.
//
//    @Slider(name = "Example Slider", min = 0f, max = 100f, step = 10, category = "dummy")
//    var exampleSlider: Float = 50f // The default value for the float Slider.
//
//    @Dropdown(name = "Example Dropdown", options = ["Option 1", "Option 2", "Option 3", "Option 4"], category = "dummy")
//    var exampleDropdown: Int = 1 // Default option (in this case "Option 2")
//

    private val disabledFeatures: MutableSet<Feature> = EnumSet.noneOf(
        Feature::class.java
    )
}

