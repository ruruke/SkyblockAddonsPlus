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
        "skyblockaddons",
        "essences/" + name.lowercase() + ".png"
    )

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
