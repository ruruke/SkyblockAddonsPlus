package moe.ruruke.skyblock.gui.buttons

fun interface UpdateCallback<T> {
    fun onUpdate(updatedValue: T)
}
