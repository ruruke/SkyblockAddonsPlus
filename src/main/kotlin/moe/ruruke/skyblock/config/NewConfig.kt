package moe.ruruke.skyblock.config

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.getLogger
import moe.ruruke.skyblock.core.Feature
import net.minecraft.client.Minecraft
import java.util.HashMap

class NewConfig {
    companion object{

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
            if(isRemoteDisabled(feature)){
                return true
            }
            when(feature.getId()) {
                45 -> return SkyblockAddonsPlus.config!!.replaceRomanNumeralsWithNumbers
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
}