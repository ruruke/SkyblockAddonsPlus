package moe.ruruke.skyblock.hud

import cc.polyfrost.oneconfig.hud.SingleTextHud

/**
 * An example OneConfig HUD that is started in the config and displays text.
 *
 * @see TestConfig.hud
 */
class TestHud : SingleTextHud("Test", true) {
    public override fun getText(example: Boolean): String {
        return "I'm an example HUD"
    }
}
