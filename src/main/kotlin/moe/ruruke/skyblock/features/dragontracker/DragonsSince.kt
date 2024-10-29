package moe.ruruke.skyblock.features.dragontracker

import com.google.common.base.CaseFormat
import lombok.AllArgsConstructor
import lombok.Getter
import moe.ruruke.skyblock.core.ItemRarity
import moe.ruruke.skyblock.core.Translations
import moe.ruruke.skyblock.core.Translations.getMessage


@AllArgsConstructor
enum class DragonsSince(legendary: ItemRarity) {
    SUPERIOR(ItemRarity.LEGENDARY),
    ASPECT_OF_THE_DRAGONS(ItemRarity.LEGENDARY),
    ENDER_DRAGON_PET(ItemRarity.LEGENDARY);

    @Getter
    private val itemRarity: ItemRarity? = null

    val displayName: String
        get() = Translations.getMessage(
            "dragonTracker." + CaseFormat.UPPER_UNDERSCORE.to(
                CaseFormat.LOWER_CAMEL,
                this.name
            )
        )!!
}
