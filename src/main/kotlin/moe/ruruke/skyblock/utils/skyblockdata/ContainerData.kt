package moe.ruruke.skyblock.utils.skyblockdata

import kotlin.math.min


class ContainerData {
    private enum class ContainerType {
        BACKPACK,
        NEW_YEARS_CAKE,
        PERSONAL_COMPACTOR,
        BUILDERS_WAND
    }

    /**
     * The container type (see [ContainerType]).
     */
    private val type: ContainerType? = null

    /**
     * The size of the container
     */
    private val size = 0

    /**
     * The data tag where a compressed array of item stacks are stored.
     */
    private val compressedDataTag: String? = null
    fun getCompressedDataTag(): String? {
        return compressedDataTag
    }
    /**
     * Data tags where individual item stacks are stored.
     */
    private val itemStackDataTags: List<String>? = null
    fun getItemStackDataTags(): List<String>? {
        return itemStackDataTags
    }
    /**
     * The ExtraAttributes NBT tag for retrieving backpack color
     */
    val colorTag: String? = null
//    fun getColorTag(): String? {
//        return colorTag
//    }

    /**
     * The container (item array) dimensions
     */
    private val dimensions = intArrayOf(6, 9)


    fun isBackpack(): Boolean {
        return type == ContainerType.BACKPACK
    }

    fun isCakeBag(): Boolean {
        return type == ContainerType.NEW_YEARS_CAKE
    }

    fun isPersonalCompactor(): Boolean {
        return type == ContainerType.PERSONAL_COMPACTOR
    }

    fun isBuildersWand(): Boolean {
        return type == ContainerType.BUILDERS_WAND
    }


    /* Functions that check the size of the container */
    /**
     * @return the item capacity of the container, or a maximum of 54
     */
    fun getSize(): Int {
        return min(size.toDouble(), 54.0).toInt()
    }

    private val numRows: Int = 0


    private val numCols: Int = 0

    /**
     * @return the number of rows in the container, or a maximum of 6
     */
    fun getNumRows(): Int {
        return if (dimensions.size === 2) Math.min(dimensions[0], 6) else 6
    }

    /**
     * @return the number of columns in the container, or a maximum of 9
     */
    fun getNumCols(): Int {
        return if (dimensions.size === 2) Math.min(dimensions[1], 9) else 9
    }

}
