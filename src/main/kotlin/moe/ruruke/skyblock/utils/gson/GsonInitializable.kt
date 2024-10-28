package moe.ruruke.skyblock.utils.gson

/**
 * Use this interface in order to mark a class that needs to be
 * initialized. The method this interface provides will be
 * called after:
 *
 *  1. The object's constructor has been called
 *  1. Gson has filled in all the fields
 *
 *
 * Use the method provided as a hook to do any processing after
 * Gson finishes deserialization.
 */
interface GsonInitializable {
    fun gsonInit()
}
