package moe.ruruke.skyblock.utils

import codes.biscuit.hypixellocalizationlib.HypixelLanguage
import lombok.Getter

@Getter
class PlayerData {
    private var language: HypixelLanguage? = null

    fun setLanguage(lang: HypixelLanguage) {
        language = lang
    }
    fun getLanguage(): HypixelLanguage? {
        return language
    }
}
