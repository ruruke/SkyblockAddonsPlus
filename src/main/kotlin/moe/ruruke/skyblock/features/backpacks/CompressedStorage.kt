package moe.ruruke.skyblock.features.backpacks

import moe.ruruke.skyblock.utils.gson.GsonInitializable

class CompressedStorage : GsonInitializable {
    /**
     * The gson serialized string. This is used to reduce the number of lines in the persistent config.
     * We use gson's pretty printing, so serializing a long byte array means a lot of line breaks and a very long file...
     */
    private var storage = "[]"

    /**
     * We immediately convert the string to byte[] form after deserialization using the [.gsonInit] function.
     * This is cached internally so we don't have to parse out the string every time we want to get the storage.
     */
    @Transient
    private var transientStorage = ByteArray(0)


    constructor()

    constructor(compressedStorage: ByteArray) {
        transientStorage = compressedStorage
        storage = convertByteArrayToString(compressedStorage)
    }

    /**
     * Gets the cached value of the storage to prevent string parsing every time.
     *
     * @return the cached storage
     */
    fun getStorage(): ByteArray {
        return transientStorage
    }

    /**
     * Special setter.
     * Sets the cached value and also updates the serializable string.
     *
     * @param storageBytes the bytes to store
     */
    fun setStorage(storageBytes: ByteArray) {
        transientStorage = storageBytes
        storage = convertByteArrayToString(storageBytes)
    }

    /**
     * Converts a byte array into a string enclosed in brackets, with each byte separated by commas.
     *
     * @param byteArray a byte array"
     * @return the equivalent string of the form "\\[(([0-9]+,)*[0-9]+)?\\]
     */
    private fun convertByteArrayToString(byteArray: ByteArray): String {
        val builder = StringBuilder()
        builder.append("[")
        for (b in byteArray) {
            builder.append(b.toInt()).append(",")
        }
        // Delete the hanging comma
        if (builder.length > 1) {
            builder.delete(builder.length - 1, builder.length)
        }
        builder.append("]")
        return builder.toString()
    }

    /**
     * Converts a string of bytes separated by commas and enclosed in brackets
     * into a byte array with each string of numbers forming the array.
     *
     * @param formattedString a string of the form "\\[(([0-9]+,)*[0-9]+)?\\]"
     * @return the equivalent byte array
     */
    private fun convertStringToByteArray(formattedString: String?): ByteArray {
        if (formattedString == null || formattedString.length < 2) {
            return ByteArray(0)
        }
        val list = formattedString.substring(1, formattedString.length - 1)
        val bytes = list.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val ret = ByteArray(bytes.size)
        for (i in bytes.indices) {
            ret[i] = bytes[i].toByte()
        }
        return ret
    }

    /**
     * Called immediately after serialization by SBA's GSON parser.
     * Converts the deserialized string into a byte array for caching purposes
     */
    override fun gsonInit() {
        transientStorage = convertStringToByteArray(storage)
    }
}
