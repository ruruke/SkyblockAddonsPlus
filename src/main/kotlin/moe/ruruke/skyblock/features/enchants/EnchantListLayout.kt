package moe.ruruke.skyblock.features.enchants

import moe.ruruke.skyblock.core.Translations
import moe.ruruke.skyblock.gui.buttons.ButtonSelect.SelectItem

/**
 * Statuses that are shown on the Discord RPC feature
 *
 *
 * This file has LF line endings because ForgeGradle is weird and will throw a NullPointerException if it's CRLF.
 */
enum class EnchantListLayout() : SelectItem {
    NORMAL("enchantLayout.titleNormal", "enchantLayout.descriptionNormal"),
    COMPRESS("enchantLayout.titleCompress", "enchantLayout.descriptionCompress"),
    EXPAND("enchantLayout.titleExpand", "enchantLayout.descriptionExpand");

    private var title: String? = null
    override var description: String = ""

    constructor(title: String, description: String?) : this() {
        this.title = title
        this.description = description!!
    }

//    fun getName(): String {
//        return Translations.getMessage(title!!)!!
//    }
//
//    override fun getDesc(): String {
//        return Translations.getMessage(description)!!
//    }
}