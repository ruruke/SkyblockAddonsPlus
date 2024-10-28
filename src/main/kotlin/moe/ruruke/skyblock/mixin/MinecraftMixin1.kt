package moe.ruruke.skyblock.mixin

import net.minecraft.client.Minecraft
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

/**
 * An example mixin using SpongePowered's Mixin library
 *
 * @see Inject
 *
 * @see Mixin
 */
@Mixin(Minecraft::class)
class MinecraftMixin {
    @Inject(method = ["startGame"], at = [At(value = "HEAD")])
    private fun onStartGame(ci: CallbackInfo) {
        println("This is a message from an example mod!")
    }
}
