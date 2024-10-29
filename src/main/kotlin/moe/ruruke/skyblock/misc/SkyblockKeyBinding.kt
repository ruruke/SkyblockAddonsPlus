package moe.ruruke.skyblock.misc

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.getLogger
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.fml.client.registry.ClientRegistry
import org.apache.commons.lang3.ArrayUtils
import org.lwjgl.input.Keyboard

class SkyblockKeyBinding(
    private val name: String,
    private val defaultKeyCode: Int,
    private val translationKey: String
) {
    private val keyBinding = KeyBinding(
        "key.skyblockaddons." + this.name,
        defaultKeyCode,
        SkyblockAddonsPlus.NAME
    )
    private var registered = false
    private var isFirstRegistration = true

    /*
    This is the key code stored before the key binding is de-registered
    It's set to a number larger than Keyboard.KEYBOARD_SIZE by default to indicate no previous key code is stored.
     */
    private var previousKeyCode = 999

    // TODO localize errors?
    val keyCode: Int
        /**
         * Returns the current key code for this key binding.
         *
         * @return the current key code for this key binding
         */
        get() = keyBinding.keyCode

    val isKeyDown: Boolean
        /**
         * Returns true if the key is pressed (used for continuous querying). Should be used in tickers.
         * JavaDoc is from linked method.
         *
         * @see KeyBinding.isKeyDown
         */
        get() = if (registered) {
            keyBinding.isKeyDown
        } else {
            false
        }

    val isPressed: Boolean
        /**
         * Returns true on the initial key press. For continuous querying use [this.isKeyDown]. Should be used in key
         * events.
         * JavaDoc is from linked method.
         *
         * @see KeyBinding.isPressed
         */
        get() = if (registered) {
            keyBinding.isPressed
        } else {
            false
        }

    /**
     * Adds this keybinding to [Minecraft.gameSettings]. If the key binding is not being registered for the first
     * time, its previous keycode setting from before its last de-registration is restored.
     */
    fun register() {
        if (registered) {
            logger.error("Tried to register a key binding with the name \"$name\" which is already registered.")
            return
        }

        ClientRegistry.registerKeyBinding(keyBinding)

        if (isFirstRegistration) {
            isFirstRegistration = false
        } else if (previousKeyCode < Keyboard.KEYBOARD_SIZE) {
            keyBinding.keyCode = defaultKeyCode
            KeyBinding.resetKeyBindingArrayAndHash()
        }
        registered = true
    }

    /**
     * Removes this keybinding from [Minecraft.gameSettings].
     */
    fun deRegister() {
        if (registered) {
            val index = ArrayUtils.indexOf(Minecraft.getMinecraft().gameSettings.keyBindings, keyBinding)

            if (index == ArrayUtils.INDEX_NOT_FOUND) {
                logger.error(
                    "Keybinding was registered but no longer exists in the registry. Something else must have removed it." +
                            " This shouldn't happen; please inform an SBA developer."
                )
                registered = false
                return
            }

            Minecraft.getMinecraft().gameSettings.keyBindings =
                ArrayUtils.remove(Minecraft.getMinecraft().gameSettings.keyBindings, index)

            /*
            The key binding still exists in the internal list even though it's removed from the settings menu.
            We have to set its key to KEY_NONE so it does not conflict with other key bindings.
             */
            previousKeyCode = keyBinding.keyCode
            keyBinding.keyCode = Keyboard.KEY_NONE
            KeyBinding.resetKeyBindingArrayAndHash()
            registered = false
        } else {
            logger.error("Tried to de-register a key binding with the name \"$name\" which wasn't registered.")
        }
    }

    companion object {
        private val logger = getLogger()
    }
}
