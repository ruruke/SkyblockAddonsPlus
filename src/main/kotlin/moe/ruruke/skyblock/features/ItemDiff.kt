package moe.ruruke.skyblock.features



import net.minecraft.nbt.NBTTagCompound


class ItemDiff @JvmOverloads constructor(
    /** The item's display name.  */
    private val displayName: String,
    /** The changed amount.  */
    private var amount: Int,
    /**
     * The item's ExtraAttributes from the NBT
     */
    private val extraAttributes: NBTTagCompound? = null
) {
    
    private var timestamp: Long

    /**
     * @param displayName The item's display name.
     * @param amount      The changed amount.
     * @param extraAttributes The Skyblock NBT data of the first item detected
     */
    /**
     * @param displayName The item's display name.
     * @param amount      The changed amount.
     */
    init {
        this.timestamp = System.currentTimeMillis()
    }

    fun getExtraAttributes(): NBTTagCompound? {
        return extraAttributes
    }
    fun getDisplayName(): String {
        return displayName
    }
    fun getAmount(): Int {
        return amount
    }
    /**
     * Update the changed amount of the item.
     *
     * @param amount Amount to be added
     */
    fun add(amount: Int) {
        this.amount += amount
        if (this.amount == 0) {
            this.timestamp -= LIFESPAN
        } else {
            this.timestamp = System.currentTimeMillis()
        }
    }

    val lifetime: Long
        /**
         * @return Amount of time in ms since the ItemDiff was created.
         */
        get() = System.currentTimeMillis() - timestamp

    companion object {
        /**
         * How long items in the log should be displayed before they are removed in ms
         */
        const val LIFESPAN: Long = 5000
    }
}
