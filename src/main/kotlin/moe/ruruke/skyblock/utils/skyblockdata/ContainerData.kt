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
    val compressedDataTag: String? = null

    /**
     * Data tags where individual item stacks are stored.
     */
    val itemStackDataTags: List<String>? = null

    /**
     * The ExtraAttributes NBT tag for retrieving backpack color
     */
    val colorTag: String? = null

    /**
     * The container (item array) dimensions
     */
    private val dimensions = intArrayOf(6, 9)


    val isBackpack: Boolean
        /* Functions that check the container type */
        get() = type == ContainerType.BACKPACK

    val isCakeBag: Boolean
        get() = type == ContainerType.NEW_YEARS_CAKE

    val isPersonalCompactor: Boolean
        get() = type == ContainerType.PERSONAL_COMPACTOR

    val isBuildersWand: Boolean
        get() = type == ContainerType.BUILDERS_WAND

    /* Functions that check the size of the container */
    /**
     * @return the item capacity of the container, or a maximum of 54
     */
    fun getSize(): Int {
        return min(size.toDouble(), 54.0).toInt()
    }

    val numRows: Int
        /**
         * @return the number of rows in the container, or a maximum of 6
         */
        get() = if (dimensions.size == 2) min(dimensions[0].toDouble(), 6.0).toInt() else 6

    val numCols: Int
        /**
         * @return the number of columns in the container, or a maximum of 9
         */
        get() = if (dimensions.size == 2) min(dimensions[1].toDouble(), 9.0).toInt() else 9
}
