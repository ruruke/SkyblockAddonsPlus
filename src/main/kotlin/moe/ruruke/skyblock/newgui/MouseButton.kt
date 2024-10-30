package moe.ruruke.skyblock.newgui

enum class MouseButton {
    LEFT,
    RIGHT,
    MIDDLE;

    companion object {
        @JvmStatic
        fun fromKeyCode(keyCode: Int): MouseButton? {
            if (keyCode == 1) {
                return RIGHT
            }

            return null
        }
    }
}
