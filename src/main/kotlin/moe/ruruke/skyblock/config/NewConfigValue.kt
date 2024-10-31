package moe.ruruke.skyblock.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.*
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.config.data.OptionSize
import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.hud.TestHud
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

    //            WIP           //
    @Switch(name = "Hide Food and Armor Bar", size = OptionSize.SINGLE, category = "QoL")
    var hideFoodAndArmorBar: Boolean = false
    @Switch(name = "Hide Health Bar", size = OptionSize.SINGLE, category = "QoL")
    var hideHealthBar: Boolean = false

    @Switch(name = "Custom Health Bar", size = OptionSize.SINGLE, category = "QoL")
    var healthBar: Boolean = false
    @Switch(name = "Health Text", size = OptionSize.SINGLE, category = "QoL")
    var healthText: Boolean = false
    @Switch(name = "Custom Mana Bar", size = OptionSize.SINGLE, category = "QoL")
    var manaBar: Boolean = false
    @Switch(name = "Mana Text", size = OptionSize.SINGLE, category = "QoL")
    var manaText: Boolean = false

    @Switch(name = "Full Inventory Warning", size = OptionSize.SINGLE, category = "QoL")
    var fullInventoryWarning: Boolean = false

    @Switch(name = "Drop Confirm(DummyName)", size = OptionSize.SINGLE, category = "QoL")
    var dropConfirm: Boolean = false

    @Switch(name = "CHANGE_ZEALOT_COLOR(DummyName)", size = OptionSize.SINGLE, category = "QoL")
    var changeZealotColor: Boolean = false

    @Switch(name = "Backpack Preview (DummyName)", size = OptionSize.SINGLE, category = "Interface")
    var backpackPreview: Boolean = false

    @Switch(name = "EnderChest Preview (DummyName)", size = OptionSize.SINGLE, category = "Interface")
    var enderChestPreview: Boolean = false
//    @Switch(name = "Full Inventory Warning", size = OptionSize.SINGLE, category = "WIP")
//    var fullInventoryWarning: Boolean = false // The default value for the boolean Switch.
    //            WIP           //


    //            DEBUG           //
    @Switch(name = "Force OnSkyblock", size = OptionSize.SINGLE, category = "Debug")
    var forceOnSkyblock: Boolean = false // The default value for the boolean Switch.

    @Dropdown(
        name = "Force Location",        // name of the component
        options = ["None", "Island", "DRAGONS_NEST"],
        category = "Debug"
    )
    var forceLocation: Int = 0 // default option (here "Another Option")

    //            DEBUG           //
    //Dummy
    @Exclude
    @HUD(name = "Development Ver.")
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
}

