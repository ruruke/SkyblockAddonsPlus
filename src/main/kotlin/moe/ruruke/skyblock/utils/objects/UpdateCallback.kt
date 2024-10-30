package moe.ruruke.skyblock.utils.objects

fun interface UpdateCallback<T> {
    fun onUpdate(updatedValue: T)
}
