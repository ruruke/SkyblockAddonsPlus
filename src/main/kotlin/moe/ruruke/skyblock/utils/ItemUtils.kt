package moe.ruruke.skyblock.utils

import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.getGson
import moe.ruruke.skyblock.core.ItemRarity
import moe.ruruke.skyblock.core.ItemType
import moe.ruruke.skyblock.features.backpacks.BackpackColor
import moe.ruruke.skyblock.utils.TextUtils.Companion.encodeSkinTextureURL
import moe.ruruke.skyblock.utils.TextUtils.Companion.stripColor
import moe.ruruke.skyblock.utils.skyblockdata.CompactorItem
import moe.ruruke.skyblock.utils.skyblockdata.ContainerData
import moe.ruruke.skyblock.utils.skyblockdata.PetInfo
import moe.ruruke.skyblock.utils.skyblockdata.Rune
import net.minecraft.enchantment.Enchantment
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemPickaxe
import net.minecraft.item.ItemStack
import net.minecraft.nbt.*
import net.minecraftforge.common.util.Constants.NBT
import org.apache.commons.lang3.text.WordUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

/**
 * Utility methods for Skyblock Items
 */
class ItemUtils {
    companion object {
        val NBT_INTEGER: Int = 3
        val NBT_STRING: Int = 8
        val NBT_LIST: Int = 9
        /**
         * This expression matches the line with a Skyblock item's rarity and item type that's at the end of its lore.
         */
        private val ITEM_TYPE_AND_RARITY_PATTERN: Pattern =
            Pattern.compile("§l(?<rarity>[A-Z]+) ?(?<type>[A-Z ]+)?(?:§[0-9a-f]§l§ka)?$")
        private var compactorItems: Map<String, CompactorItem>? = null

        fun setCompactorItems(compactorItems: HashMap<String, CompactorItem>?) {
            ItemUtils.compactorItems = compactorItems
        }

        private var containers: Map<String?, ContainerData>? = null

        fun setContainers(containers: HashMap<String?, ContainerData>?) {
            ItemUtils.containers = containers
        }

        /**
         * Returns the rarity of a given Skyblock item. The rarity is read from the item's lore.
         * The item must not be `null`.
         *
         * @param item the Skyblock item to check, can't be `null`
         * @return the rarity of the item if a valid rarity is found, or `null` if item is `null` or no valid rarity is found
         */
        fun getRarity(item: ItemStack): ItemRarity? {
            if (item == null) {
                throw NullPointerException("The item cannot be null!")
            }
            if (!item.hasTagCompound()) {
                return null
            }

            return getRarity(getItemLore(item))
        }

        /**
         * Returns the item type of a given Skyblock item.
         * The item must not be `null`.
         *
         * @param item the Skyblock item to check, can't be `null`
         * @return the item type of the item or `null` if no item type was found
         */
        fun getItemType(item: ItemStack): ItemType? {
            if (item == null) {
                throw NullPointerException("The item cannot be null!")
            }
            if (!item.hasTagCompound()) {
                return null
            }

            return getType(getItemLore(item))
        }

        /**
         * Returns the itemstack that this personal compactor skyblock ID represents. Note that
         * a personal compactor skyblock ID is not the same as an item's regular skyblock id!
         *
         * @param personalCompactorSkyblockID The personal compactor skyblock ID (ex. ENCHANTED_ACACIA_LOG)
         * @return The itemstack that this personal compactor skyblock ID represents
         */
        fun getPersonalCompactorItemStack(personalCompactorSkyblockID: String): ItemStack? {
            val compactorItem = compactorItems!![personalCompactorSkyblockID]
            return if (compactorItem != null) compactorItem.itemStack else createSkullItemStack(
                "§7Unknown ($personalCompactorSkyblockID)",
                listOf("§6also biscut was here hi!!"),
                personalCompactorSkyblockID,
                "724c64a2-fc8b-4842-852b-6b4c2c6ef241",
                "e0180f4aeb6929f133c9ff10476ab496f74c46cf8b3be6809798a974929ccca3"
            )
        }

        /**
         * Returns data about the container that is passed in.
         *
         * @param skyblockID The skyblock ID of the container
         * @return A [ContainerData] object containing info about the container in general
         */
        fun getContainerData(skyblockID: String?): ContainerData? {
            return containers!![skyblockID]
        }

        /**
         * Returns the Skyblock Item ID of a given Skyblock item
         *
         * @param item the Skyblock item to check
         * @return the Skyblock Item ID of this item or `null` if this isn't a valid Skyblock item
         */
        fun getSkyblockItemID(item: ItemStack): String? {
            if (item == null) {
                return null
            }

            val extraAttributes = getExtraAttributes(item) ?: return null

            if (!extraAttributes.hasKey("id", NBT_STRING)) {
                return null
            }

            return extraAttributes.getString("id")
        }

        /**
         * Returns the `ExtraAttributes` compound tag from the item's NBT data. The item must not be `null`.
         *
         * @param item the item to get the tag from
         * @return the item's `ExtraAttributes` compound tag or `null` if the item doesn't have one
         */
        fun getExtraAttributes(item: ItemStack): NBTTagCompound? {
            if (item == null) {
                throw NullPointerException("The item cannot be null!")
            }
            if (!item.hasTagCompound()) {
                return null
            }

            return item.getSubCompound("ExtraAttributes", false)
        }


        /**
         * Returns the `enchantments` compound tag from the item's NBT data.
         *
         * @param item the item to get the tag from
         * @return the item's `enchantments` compound tag or `null` if the item doesn't have one
         */
        fun getEnchantments(item: ItemStack): NBTTagCompound? {
            val extraAttributes = getExtraAttributes(item)
            return extraAttributes?.getCompoundTag("enchantments")
        }

        /**
         * @return The Skyblock reforge of a given itemstack
         */
        fun getReforge(item: ItemStack): String? {
            if (item.hasTagCompound()) {
                var extraAttributes = item.tagCompound
                if (extraAttributes.hasKey("ExtraAttributes")) {
                    extraAttributes = extraAttributes.getCompoundTag("ExtraAttributes")
                    if (extraAttributes.hasKey("modifier")) {
                        var reforge = WordUtils.capitalizeFully(extraAttributes.getString("modifier"))

                        reforge = reforge.replace("_sword", "") //fixes reforges like "Odd_sword"
                        reforge = reforge.replace("_bow", "")

                        return reforge
                    }
                }
            }
            return null
        }

        /**
         * Checks if the given item is a material meant to be used in a crafting recipe. Dragon fragments are an example
         * since they are used to make dragon armor.
         *
         * @param itemStack the item to check
         * @return `true` if this item is a material, `false` otherwise
         */
        //TODO: Fix for Hypixel localization
        fun isMaterialForRecipe(itemStack: ItemStack?): Boolean {
            val lore = getItemLore(itemStack)
            for (loreLine in lore) {
                if ("Right-click to view recipes!" == stripColor(loreLine)) {
                    return true
                }
            }
            return false
        }

        /**
         * Checks if the given item is a mining tool (pickaxe or drill).
         *
         * @param itemStack the item to check
         * @return `true` if this item is a pickaxe/drill, `false` otherwise
         */
        fun isMiningTool(itemStack: ItemStack): Boolean {
            return itemStack.item is ItemPickaxe || isDrill(itemStack)
        }


        /**
         * Checks if the given `ItemStack` is a drill. It works by checking for the presence of the `drill_fuel` NBT tag,
         * which only drills have.
         *
         * @param itemStack the item to check
         * @return `true` if this item is a drill, `false` otherwise
         */
        fun isDrill(itemStack: ItemStack?): Boolean {
            if (itemStack == null) {
                return false
            }

            val extraAttributes = getExtraAttributes(itemStack)

            return extraAttributes?.hasKey("drill_fuel", NBT.TAG_INT) ?: false
        }


        /**
         * Returns the Skyblock Item ID of a given Skyblock Extra Attributes NBT Compound
         *
         * @param extraAttributes the NBT to check
         * @return the Skyblock Item ID of this item or `null` if this isn't a valid Skyblock NBT
         */
        fun getSkyblockItemID(extraAttributes: NBTTagCompound?): String? {
            if (extraAttributes == null) {
                return null
            }

            val itemId = extraAttributes.getString("id")
            if (itemId == "") {
                return null
            }

            return itemId
        }

        /**
         * Checks if the given `ItemStack` is a backpack
         *
         * @param stack the `ItemStack` to check
         * @return `true` if `stack` is a backpack, `false` otherwise
         */
        fun isBackpack(stack: ItemStack): Boolean {
            val extraAttributes = getExtraAttributes(stack)
            val containerData = containers!![getSkyblockItemID(extraAttributes)]
            return containerData != null && containerData.isBackpack()
        }

        fun isBuildersWand(stack: ItemStack): Boolean {
            val extraAttributes = getExtraAttributes(stack)
            val containerData = containers!![getSkyblockItemID(extraAttributes)]
            return containerData != null && containerData.isBuildersWand()
        }

        /**
         * Gets the color of the backpack in the given `ItemStack`
         *
         * @param stack the `ItemStack` containing the backpack
         * @return The color of the backpack; or `WHITE` if there is no color; or `null` if it is not a container
         */
        fun getBackpackColor(stack: ItemStack): BackpackColor? {
            val extraAttributes = getExtraAttributes(stack)
            val containerData = containers!![getSkyblockItemID(extraAttributes)]
            if (extraAttributes != null) {
                if (containerData != null) {
                    try {
                        return BackpackColor.valueOf(extraAttributes.getString(containerData.colorTag))
                    } catch (ignored: IllegalArgumentException) {
                    }
                    return BackpackColor.WHITE
                } else if (extraAttributes.hasKey("backpack_color")) {
                    try {
                        return BackpackColor.valueOf(extraAttributes.getString("backpack_color"))
                    } catch (ignored: IllegalArgumentException) {
                    }
                    return BackpackColor.WHITE
                }
            }

            return null
        }

        /**
         * Returns the Skyblock Reforge of a given Skyblock Extra Attributes NBT Compound
         *
         * @param extraAttributes the NBT to check
         * @return the Reforge (in lowercase) of this item or `null` if this isn't a valid Skyblock NBT or reforge
         */
        fun getReforge(extraAttributes: NBTTagCompound?): String? {
            if (extraAttributes != null && extraAttributes.hasKey("modifier", NBT_STRING)) {
                return extraAttributes.getString("modifier")
            }

            return null
        }

        /**
         * Returns a [Rune] from the ExtraAttributes Skyblock data
         * This can ge retrieved from a rune itself or an infused item
         *
         * @param extraAttributes the Skyblock Data to check
         * @return A [Rune] or `null` if it doesn't have it
         */
        fun getRuneData(extraAttributes: NBTTagCompound?): Rune? {
            if (extraAttributes != null) {
                if (!extraAttributes.hasKey("runes")) {
                    return null
                }

                return Rune(extraAttributes.getCompoundTag("runes"))
            }

            return null
        }

        /**
         * Returns a [PetInfo] from the ExtraAttributes Skyblock data
         *
         * @param extraAttributes the Skyblock Data to check
         * @return A [PetInfo] or `null` if it isn't a pet
         */
        fun getPetInfo(extraAttributes: NBTTagCompound?): PetInfo? {
            if (extraAttributes != null) {
                val itemId = extraAttributes.getString("id")

                if (itemId != "PET" || !extraAttributes.hasKey("petInfo")) {
                    return null
                }

                return getGson().fromJson(
                    extraAttributes.getString("petInfo"),
                    PetInfo::class.java
                )
            }

            return null
        }

        /**
         * Returns a string list containing the NBT lore of an `ItemStack`, or
         * an empty list if this item doesn't have a lore tag.
         * The itemStack argument must not be `null`. The returned lore list is unmodifiable since it has been
         * converted from an `NBTTagList`.
         *
         * @param itemStack the ItemStack to get the lore from
         * @return the lore of an ItemStack as a string list
         */
        fun getItemLore(itemStack: ItemStack?): List<String> {
            if (itemStack != null) {
                if (itemStack.hasTagCompound()) {
                    val display = itemStack.getSubCompound("display", false)

                    if (display != null && display.hasKey("Lore", NBT_LIST)) {
                        val lore = display.getTagList("Lore", NBT_STRING)

                        val loreAsList: MutableList<String> = ArrayList()
                        for (lineNumber in 0 until lore.tagCount()) {
                            loreAsList.add(lore.getStringTagAt(lineNumber))
                        }

                        return Collections.unmodifiableList(loreAsList)
                    }
                }

                return emptyList()
            } else {
                throw NullPointerException("Cannot get lore from null item!")
            }
        }

        /**
         * Sets the lore text of a given `ItemStack`.
         *
         * @param itemStack the `ItemStack` to set the lore for
         * @param lore the new lore
         */
        fun setItemLore(itemStack: ItemStack, lore: List<String>) {
            val display = itemStack.getSubCompound("display", true)

            val loreTagList = NBTTagList()
            for (loreLine in lore) {
                loreTagList.appendTag(NBTTagString(loreLine))
            }

            display.setTag("Lore", loreTagList)
        }

        /**
         * Check if the given `ItemStack` is an item shown in a menu as a preview or placeholder
         * (e.g. items in the recipe book).
         *
         * @param itemStack the `ItemStack` to check
         * @return `true` if `itemStack` is an item shown in a menu as a preview or placeholder, `false` otherwise
         */
        fun isMenuItem(itemStack: ItemStack): Boolean {
            if (itemStack == null) {
                throw NullPointerException("Item stack cannot be null!")
            }

            val extraAttributes = getExtraAttributes(itemStack)
            return if (extraAttributes != null) {
                // If this item stack is a menu item, it won't have this key.
                !extraAttributes.hasKey("uuid")
            } else {
                false
            }
        }

        /**
         * Creates a new `ItemStack` instance with the given item and a fake enchantment to enable the enchanted "glint"
         * effect if `enchanted` is true. This method should be used when you want to create a bare-bones `ItemStack`
         * to render as part of a GUI.
         *
         * @param item the `Item` the created `ItemStack` should be
         * @param enchanted the item has the enchanted "glint" effect enabled if `true`, disabled if `false`
         * @return a new `ItemStack` instance with the given item and a fake enchantment if applicable
         */
        fun createItemStack(item: Item?, enchanted: Boolean): ItemStack {
            return createItemStack(item, 0, null, null, enchanted)
        }

        fun createItemStack(item: Item?, name: String?, skyblockID: String?, enchanted: Boolean): ItemStack {
            return createItemStack(item, 0, name, skyblockID, enchanted)
        }

        fun createItemStack(item: Item?, meta: Int, name: String?, skyblockID: String?, enchanted: Boolean): ItemStack {
            val stack = ItemStack(item, 1, meta)

            if (name != null) {
                stack.setStackDisplayName(name)
            }

            if (enchanted) {
                stack.addEnchantment(Enchantment.protection, 0)
            }

            if (skyblockID != null) {
                setItemStackSkyblockID(stack, skyblockID)
            }

            return stack
        }

        fun createEnchantedBook(
            name: String?,
            skyblockID: String?,
            enchantName: String?,
            enchantLevel: Int
        ): ItemStack {
            val stack = createItemStack(Items.enchanted_book, name, skyblockID, false)

            val enchantments = NBTTagCompound()
            enchantments.setInteger(enchantName, enchantLevel)

            val extraAttributes = stack.tagCompound.getCompoundTag("ExtraAttributes")
            extraAttributes.setTag("enchantments", enchantments)

            return stack
        }

        fun createSkullItemStack(name: String?, skyblockID: String?, skullID: String?, textureURL: String): ItemStack {
            val stack = ItemStack(Items.skull, 1, 3)

            val texture = NBTTagCompound()
            texture.setString("Value", encodeSkinTextureURL(textureURL))

            val textures = NBTTagList()
            textures.appendTag(texture)

            val properties = NBTTagCompound()
            properties.setTag("textures", textures)

            val skullOwner = NBTTagCompound()
            skullOwner.setTag("Properties", properties)

            skullOwner.setString("Id", skullID)

            stack.setTagInfo("SkullOwner", skullOwner)

            if (name != null) {
                stack.setStackDisplayName(name)
            }

            if (skyblockID != null) {
                setItemStackSkyblockID(stack, skyblockID)
            }

            return stack
        }

        fun createSkullItemStack(
            name: String?,
            lore: List<String>,
            skyblockID: String?,
            skullID: String?,
            textureURL: String
        ): ItemStack {
            val stack = ItemStack(Items.skull, 1, 3)

            val texture = NBTTagCompound()
            texture.setString("Value", encodeSkinTextureURL(textureURL))

            val textures = NBTTagList()
            textures.appendTag(texture)

            val properties = NBTTagCompound()
            properties.setTag("textures", textures)

            val skullOwner = NBTTagCompound()
            skullOwner.setTag("Properties", properties)

            skullOwner.setString("Id", skullID)

            stack.setTagInfo("SkullOwner", skullOwner)

            if (name != null) {
                stack.setStackDisplayName(name)
                setItemLore(stack, lore)
            }

            if (skyblockID != null) {
                setItemStackSkyblockID(stack, skyblockID)
            }

            return stack
        }

        fun setItemStackSkyblockID(itemStack: ItemStack, skyblockID: String?) {
            val extraAttributes = NBTTagCompound()
            extraAttributes.setString("id", skyblockID)
            itemStack.setTagInfo("ExtraAttributes", extraAttributes)
        }

        /**
         * Given a skull ItemStack, returns the skull owner ID, or null if it doesn't exist.
         */
        fun getSkullOwnerID(skull: ItemStack?): String? {
            if (skull == null || !skull.hasTagCompound()) {
                return null
            }

            var nbt = skull.tagCompound
            if (nbt.hasKey("SkullOwner", 10)) {
                nbt = nbt.getCompoundTag("SkullOwner")
                if (nbt.hasKey("Id", 8)) {
                    return nbt.getString("Id")
                }
            }
            return null
        }

        fun getCompressedNBT(items: Array<ItemStack?>?): NBTTagByteArray? {
            if (items == null) {
                return null
            }
            // Add each item's nbt to a tag list
            val list = NBTTagList()
            for (item in items) {
                if (item == null) {
                    list.appendTag((ItemStack(null as Item?)).serializeNBT())
                } else {
                    list.appendTag(item.serializeNBT())
                }
            }
            // Append standard "i" tag for compression
            val nbt = NBTTagCompound()
            nbt.setTag("i", list)
            val stream = ByteArrayOutputStream()
            try {
                CompressedStreamTools.writeCompressed(nbt, stream)
            } catch (e: IOException) {
                return null
            }
            return NBTTagByteArray(stream.toByteArray())
        }

        /**
         * Returns the rarity of a Skyblock item given its lore. This method takes the item's lore as a string list as input.
         * This method is split up from the method that takes the `ItemStack` instance for easier unit testing.
         *
         * @param lore the `List<String>` containing the item's lore
         * @return the rarity of the item if a valid rarity is found, or `null` if item is `null` or no valid rarity is found
         */
        private fun getRarity(lore: List<String>): ItemRarity? {
            // Start from the end since the rarity is usually the last line or one of the last.
            for (i in lore.indices.reversed()) {
                val currentLine = lore[i]

                val rarityMatcher = ITEM_TYPE_AND_RARITY_PATTERN.matcher(currentLine)
                if (rarityMatcher.find()) {
                    val rarity = rarityMatcher.group("rarity")

                    for (itemRarity in ItemRarity.entries) {
                        // Use a "startsWith" check here because "VERY SPECIAL" has two words and only "VERY" is matched.
                        if (itemRarity.getLoreName().startsWith(rarity)) {
                            return itemRarity
                        }
                    }
                }
            }

            return null
        }

        /**
         * Returns the item type of a Skyblock item given its lore. This method takes the item's lore as a string list as input.
         * This method is split up from the method that takes the `ItemStack` instance for easier unit testing.
         *
         * @param lore the `List<String>` containing the item's lore
         * @return the rarity of the item if a valid rarity is found, or `null` if item is `null` or no valid rarity is found
         */
        private fun getType(lore: List<String>): ItemType? {
            // Start from the end since the rarity is usually the last line or one of the last.
            for (i in lore.indices.reversed()) {
                val currentLine = lore[i]

                val itemTypeMatcher = ITEM_TYPE_AND_RARITY_PATTERN.matcher(currentLine)
                if (itemTypeMatcher.find()) {
                    val type = itemTypeMatcher.group("type")

                    if (type != null) {
                        for (itemType in ItemType.entries) {
                            if (itemType.getLoreName().startsWith(type)) {
                                return itemType
                            }
                        }
                    }
                }
            }

            return null
        }
    }
}
