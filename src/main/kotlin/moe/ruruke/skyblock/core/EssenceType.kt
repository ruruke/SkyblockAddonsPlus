package moe.ruruke.skyblock.core

import net.minecraft.util.ResourceLocation
import org.apache.commons.lang3.text.WordUtils

enum class EssenceType {
    WITHER,
    SPIDER,
    UNDEAD,
    DRAGON,
    GOLD,
    DIAMOND,
    ICE;

    private val niceName: String =
        WordUtils.capitalizeFully(this.name)

    private val resourceLocation = ResourceLocation(
        "skyblockaddonsplus",
        "essences/" + name.lowercase() + ".png"
    )
    fun getResourceLocation(): ResourceLocation {
        return resourceLocation
    }

    companion object {
        fun fromName(name: String): EssenceType? {
            for (essenceType in entries) {
                if (essenceType.niceName == name) {
                    return essenceType
                }
            }

            return null
        }
    }
}
