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
import moe.ruruke.skyblock.hud.TestHud

/**
 * The main Config entrypoint that extends the Config type and inits the config options.
 * See [this link](https://docs.polyfrost.cc/oneconfig/config/adding-options) for more config Options
 */
class TestConfig : Config(Mod(SkyblockAddonsPlus.NAME, ModType.UTIL_QOL), SkyblockAddonsPlus.MODID + ".json") {
    @HUD(name = "Example HUD")
    var hud: TestHud = TestHud()

    init {
        initialize()
    }

    @Switch(name = "Example Switch", size = OptionSize.SINGLE)
    var exampleSwitch: Boolean = false // The default value for the boolean Switch.

    @Slider(name = "Example Slider", min = 0f, max = 100f, step = 10)
    var exampleSlider: Float = 50f // The default value for the float Slider.

    @Dropdown(name = "Example Dropdown", options = ["Option 1", "Option 2", "Option 3", "Option 4"])
    var exampleDropdown: Int = 1 // Default option (in this case "Option 2")
}

