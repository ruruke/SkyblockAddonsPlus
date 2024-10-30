package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import net.minecraft.client.audio.ISound
import net.minecraft.client.audio.SoundCategory
import net.minecraft.client.audio.SoundManager
import net.minecraft.client.audio.SoundPoolEntry

object SoundManagerHook {
    fun getNormalizedVolume(
        soundManager: SoundManager,
        sound: ISound,
        entry: SoundPoolEntry,
        category: SoundCategory?
    ): Float {
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        return if (main != null && main.utils!! != null && main.utils!!.isPlayingSound()) {
            1f
        } else {
            soundManager.getNormalizedVolume(sound, entry, category)
        }
    }
}
