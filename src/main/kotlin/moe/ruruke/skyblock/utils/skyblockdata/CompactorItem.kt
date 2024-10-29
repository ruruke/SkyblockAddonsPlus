package moe.ruruke.skyblock.utils.skyblockdata

import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.getLogger
import moe.ruruke.skyblock.utils.ItemUtils
import moe.ruruke.skyblock.utils.gson.GsonInitializable
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagList


/**
 * For storing any skyblock item. Very much a work in progress, with other things (like pets perhaps) on the way
 * Another potential addition is prevent placing enchanted items (blacklist + whitelist), item cooldown amounts, etc.
 */
class CompactorItem : GsonInitializable {
    private var itemId: String? = null
    private var displayName: String? = null
    private var enchanted = false
    private var skullId: String? = null
    private var texture: String? = null

    @Transient
    var itemStack: ItemStack? = null
        private set

    /** Set by reflection, so ignore null error with itemid  */
    constructor()

    /**
     * Generic constructor
     */
    /**
     * Made for a regular item instead of a skull
     */
    @JvmOverloads
    constructor(
        theItemId: String?,
        theDisplayName: String?,
        isEnchanted: Boolean,
        theSkullId: String? = null,
        theTexture: String? = null
    ) {
        itemId = theItemId
        displayName = theDisplayName
        enchanted = isEnchanted
        skullId = theSkullId
        texture = theTexture
        makeItemStack()
    }

    override fun gsonInit() {
        makeItemStack()
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("{")
        for (f in CompactorItem::class.java.declaredFields) {
            try {
                builder.append(f.name).append(": ").append(f[this]).append(", ")
            } catch (e: IllegalAccessException) {
                logger.error(e.message)
            }
        }
        builder.append("}")
        return builder.toString()
    }


    private fun makeItemStack() {
        try {
            if (itemId != null) {
                if (itemId == "skull") {
                    itemStack = ItemUtils.createSkullItemStack(displayName, "", skullId, texture.toString())
                } else {
                    val minecraftIdArray = itemId!!.split(":".toRegex(), limit = 2).toTypedArray()
                    val meta = if (minecraftIdArray.size == 2) minecraftIdArray[1].toInt() else 0
                    val item = Item.getByNameOrId(minecraftIdArray[0])

                    if (item != null) {
                        itemStack = if (minecraftIdArray.size == 1) ItemStack(item) else ItemStack(item, 1, meta)
                        if (enchanted) {
                            itemStack!!.setTagInfo("ench", NBTTagList())
                        }
                    }
                }
                if (itemStack != null) {
                    itemStack!!.setStackDisplayName(displayName)
                }
            }
        } catch (ex: Exception) {
            itemStack = ItemUtils.createItemStack(
                Item.getItemFromBlock(Blocks.stone),
                if (displayName != null) displayName else "",
                if (itemId != null) itemId else "",
                false
            )
            logger.error("An error occurred while making an item stack with ID $itemId and name $displayName.", ex)
        }
    }

    companion object {
        private val logger = getLogger()
    }
}
