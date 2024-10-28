package moe.ruruke.skyblock.core

import java.util.regex.Pattern

class OnlineData {
    private val bannerImageURL: String? = null
    private val bannerLink: String? = null

    private val updateInfo: UpdateInfo? = null

    private val languageJSONFormat: String? = null

    /**
     * This is the list of features in the mod that should be disabled. Features in this list will be disabled for all
     * versions of the mod v1.5.5 and above. The first key in this map is "all". It contains a list of features to be disabled
     * in all mod versions. Version numbers can be added as additional keys to disable features in specific mod versions.
     * An example of this is shown below:
     * <br></br><br></br>
     * `"1.5.5": [3]`
     * <br></br><br></br>
     * Versions must follow the semver format (e.g. `1.6.0`) and cannot be pre-release versions (e.g. `1.6.0-beta.10`).
     * Pre-release versions of the mod adhere to the disabled features list of their release version. For example, the version
     * `1.6.0-beta.10` will adhere to the list with the key `1.6.0`. Disabling features for unique pre-release
     * versions is not supported.
     */
    private val disabledFeatures: HashMap<String, List<Int>>? = null

    private val dropSettings: DropSettings? = null

    private val hypixelBrands: HashSet<Pattern>? = null

    fun getDisabledFeatures(): HashMap<String, List<Int>>? {
        return disabledFeatures
    }

    class DropSettings {
        //        private ItemRarity minimumInventoryRarity;
        //        private ItemRarity minimumHotbarRarity;
        private val dontDropTheseItems: List<String>? = null

        private val allowDroppingTheseItems: List<String>? = null
    }
}
