package moe.ruruke.skyblock.utils.skyblockdata

import net.minecraft.nbt.NBTTagCompound

class Rune(runeData: NBTTagCompound) {
    private var type: String? = null
    private var level = 0
    fun getType(): String? {
        return type
    }
    fun getLevel(): Int {
        return level
    }
    init {
        // There should only be 1 rune type

        for (runeType in runeData.keySet) {
            type = runeType
            level = runeData.getInteger(runeType)
        }
    }
}
