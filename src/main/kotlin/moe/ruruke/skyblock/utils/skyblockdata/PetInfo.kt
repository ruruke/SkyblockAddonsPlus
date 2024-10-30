package moe.ruruke.skyblock.utils.skyblockdata

import moe.ruruke.skyblock.core.ItemRarity


@Suppress("unused")
class PetInfo {
    private val type: String? = null
    private val active = false
    private val exp = 0.0
    private val tier: ItemRarity? = null
    private val hideInfo = false
    private val heldItem: String? = null
    private val candyUsed = 0
    fun getType(): String {
        return type!!
    }
}
