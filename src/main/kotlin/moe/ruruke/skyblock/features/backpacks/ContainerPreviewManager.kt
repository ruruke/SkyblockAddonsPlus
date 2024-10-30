package moe.ruruke.skyblock.features.backpacks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.InventoryType
import moe.ruruke.skyblock.utils.ColorCode
import moe.ruruke.skyblock.utils.EnumUtils
import moe.ruruke.skyblock.utils.EnumUtils.*
import moe.ruruke.skyblock.utils.ItemUtils
import moe.ruruke.skyblock.utils.TextUtils
import moe.ruruke.skyblock.utils.skyblockdata.ContainerData
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.entity.RenderItem
import net.minecraft.inventory.Container
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.InventoryBasic
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTTagByteArray
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.util.Constants
import org.apache.logging.log4j.Logger
import org.lwjgl.input.Keyboard
import java.io.ByteArrayInputStream
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.ceil
import kotlin.math.min

/**
 * This class contains utility methods for backpacks and stores the color of the backpack the player has open.
 */
object ContainerPreviewManager {
    private val logger: Logger = SkyblockAddonsPlus.getLogger()

    private val CHEST_GUI_TEXTURE = ResourceLocation("skyblockaddons", "containerPreview.png")
    private val BACKPACK_STORAGE_PATTERN: Pattern = Pattern.compile("Backpack Slot (?<slot>\\d+)")
    private val ENDERCHEST_STORAGE_PATTERN: Pattern = Pattern.compile("Ender Chest Page (?<page>\\d+)")

    /**
     * The container preview to render
     */
    private var currentContainerPreview: ContainerPreview? = null

    /**
     * The current container inventory that will be saved to the cache after it is fully initialized
     */
    private var containerInventory: InventoryBasic? = null

    /**
     * The storage key to save [ContainerPreviewManager.containerInventory] to
     */
    private var storageKey: String? = null

    /**
     * Whether we are currently frozen in the container preview
     */
    private var frozen = false
    fun isFrozen(): Boolean {
        return frozen
    }

    /**
     * The last (epoch) time we toggled the freeze button
     */
    private var lastToggleFreezeTime: Long = 0

    /**
     * True when we are drawing an `ItemStack`'s tooltip while [.isFrozen] is true
     */
    private var drawingFrozenItemTooltip = false

    /**
     * Creates and returns a `ContainerPreview` object representing the given `ItemStack` if it is a backpack
     *
     * @param stack the `ItemStack` to create a `Backpack` instance from
     * @return a `ContainerPreview` object representing `stack` if it is a backpack, or `null` otherwise
     */
    fun getFromItem(stack: ItemStack?): ContainerPreview? {
        if (stack == null) {
            return null
        }

        val extraAttributes: NBTTagCompound = ItemUtils.getExtraAttributes(stack)!!
        val skyblockID: String = ItemUtils.getSkyblockItemID(extraAttributes)!!
        val containerData: ContainerData = ItemUtils.getContainerData(skyblockID)!!
        if (extraAttributes != null && containerData != null) {
            val containerSize: Int = containerData.getSize()

            // Parse out a list of items in the container
            var items: MutableList<ItemStack>? = null
            val compressedDataTag: String = containerData.getCompressedDataTag()!!
            val itemStackDataTags: List<String> = containerData.getItemStackDataTags()!!

            if (compressedDataTag != null) {
                if (extraAttributes.hasKey(compressedDataTag, Constants.NBT.TAG_BYTE_ARRAY)) {
                    val bytes: ByteArray = extraAttributes.getByteArray(compressedDataTag)
                    items = decompressItems(bytes)
                }
            } else if (itemStackDataTags != null) {
                items = ArrayList<ItemStack>(containerSize)
                val itemStackDataTagsIterator = itemStackDataTags.iterator()
                var itemNumber = 0
                while (itemNumber < containerSize && itemStackDataTagsIterator.hasNext()) {
                    val key = itemStackDataTagsIterator.next()
                    if (!extraAttributes.hasKey(key)) {
                        itemNumber++
                        continue
                    }
                    items!!.add(ItemUtils.getPersonalCompactorItemStack(extraAttributes.getString(key))!!)
                    itemNumber++
                }
            }
            if (items == null) {
                return null
            }

            // Get the container color
            val color: BackpackColor = ItemUtils.getBackpackColor(stack)!!
            val name: String? =
                if (containerData.isPersonalCompactor()) null else TextUtils.stripColor(stack.getDisplayName())
            containerData.getSize()
            return ContainerPreview(items, name, color, containerData.getNumRows(), containerData.getNumCols())
        }
        return null
    }

    /**
     * Saves `containerInventory` to the container inventory cache if it's not `null` when a
     * [net.minecraft.client.gui.inventory.GuiChest] is closed.
     *
     * @see codes.biscuit.skyblockaddons.listeners.GuiScreenListener.onGuiOpen
     */
    fun onContainerClose() {
        if (containerInventory != null) {
            saveStorageContainerInventory()
        }
    }

    /**
     * Prepares for saving the container inventory when the container is closed.
     * Called when a [net.minecraft.client.gui.inventory.GuiChest] is opened.
     *
     * @param containerInventory the container inventory
     */
    fun onContainerOpen(containerInventory: InventoryBasic) {
        ContainerPreviewManager.containerInventory = containerInventory
        storageKey = SkyblockAddonsPlus.inventoryUtils!!.getInventoryKey()!!
    }

    private fun decompressItems(bytes: ByteArray): MutableList<ItemStack>? {
        var items: MutableList<ItemStack>? = null
        try {
            val decompressedData: NBTTagCompound = CompressedStreamTools.readCompressed(ByteArrayInputStream(bytes))
            val list: NBTTagList = decompressedData.getTagList("i", Constants.NBT.TAG_COMPOUND)
            if (list.hasNoTags()) {
                throw Exception("Decompressed container list has no item tags")
            }
            val size = min(list.tagCount().toDouble(), 54.0).toInt()
            items = ArrayList<ItemStack>(size)

            for (i in 0 until size) {
                val item: NBTTagCompound = list.getCompoundTagAt(i)
                // This fixes an issue in Hypixel where enchanted potatoes have the wrong id (potato block instead of item).
                val itemID: Short = item.getShort("id")
                if (itemID.toInt() == 142) { // Potato Block -> Potato Item
                    item.setShort("id", 392.toShort())
                } else if (itemID.toInt() == 141) { // Carrot Block -> Carrot Item
                    item.setShort("id", 391.toShort())
                }
                items!!.add(ItemStack.loadItemStackFromNBT(item))
            }
        } catch (ex: Exception) {
            logger.error("There was an error decompressing container data.", ex)
        }
        return items
    }

    fun drawContainerPreviews(guiContainer: GuiContainer, mouseX: Int, mouseY: Int) {
        val mc: Minecraft = Minecraft.getMinecraft()
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance

        if (currentContainerPreview != null) {
            var x: Int = currentContainerPreview!!.getX()
            var y: Int = currentContainerPreview!!.getY()

            val items: List<ItemStack> = currentContainerPreview!!.getItems()
            val length = items.size
            val rows: Int = currentContainerPreview!!.getNumRows()
            val cols: Int = currentContainerPreview!!.getNumCols()

            val screenHeight: Int = guiContainer.height
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)

            var tooltipItem: ItemStack? = null

            if (main.configValues!!.getBackpackStyle() === EnumUtils.BackpackStyle.GUI) {
                mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE)
                GlStateManager.disableLighting()
                GlStateManager.pushMatrix()
                GlStateManager.translate(0f, 0f, 300f)
                var textColor = 4210752
                if (main.configValues!!.isEnabled(Feature.MAKE_BACKPACK_INVENTORIES_COLORED)) {
                    val color: BackpackColor = currentContainerPreview!!.getBackpackColor()
                    if (color != null) {
                        GlStateManager.color(color.getR(), color.getG(), color.getB(), 1f)
                        textColor = color.inventoryTextColor
                    }
                }

                val textureBorder = 7
                val textureTopBorder = 17
                val textureItemSquare = 18

                // Our chest has these properties
                val topBorder = if (currentContainerPreview!!.getName() == null) textureBorder else textureTopBorder
                val totalWidth = cols * textureItemSquare + 2 * textureBorder
                val totalHeight = rows * textureItemSquare + topBorder + textureBorder
                val squaresEndWidth = totalWidth - textureBorder
                val squaresEndHeight = totalHeight - textureBorder

                if (x + totalWidth > guiContainer.width) {
                    x -= totalWidth
                }

                if (y + totalHeight > screenHeight) {
                    y = screenHeight - totalHeight
                }

                // If there is no name, don't render the full top of the chest to make things look cleaner
                if (currentContainerPreview!!.getName() == null) {
                    // Draw top border
                    guiContainer.drawTexturedModalRect(x, y, 0, 0, squaresEndWidth, topBorder)
                    // Draw left-side and all GUI display rows ("squares")
                    guiContainer.drawTexturedModalRect(
                        x,
                        y + topBorder,
                        0,
                        textureTopBorder,
                        squaresEndWidth,
                        squaresEndHeight - topBorder
                    )
                } else {
                    // Draw the top-left of the container
                    guiContainer.drawTexturedModalRect(x, y, 0, 0, squaresEndWidth, squaresEndHeight)
                }
                // Draw the top-right of the container
                guiContainer.drawTexturedModalRect(x + squaresEndWidth, y, 169, 0, textureBorder, squaresEndHeight)
                // Draw the bottom-left of the container
                guiContainer.drawTexturedModalRect(x, y + squaresEndHeight, 0, 125, squaresEndWidth, textureBorder)
                // Draw the bottom-right of the container
                guiContainer.drawTexturedModalRect(
                    x + squaresEndWidth,
                    y + squaresEndHeight,
                    169,
                    125,
                    textureBorder,
                    textureBorder
                )

                if (currentContainerPreview!!.getName() != null) {
                    var name: String = currentContainerPreview!!.getName()!!
                    if (main.utils!!.isUsingFSRcontainerPreviewTexture()) {
                        name = "${ColorCode.GOLD}${TextUtils.stripColor(name)}"
                    }
                    mc.fontRendererObj.drawString(name, x + 8, y + 6, textColor)
                }

                GlStateManager.popMatrix()
                GlStateManager.enableLighting()

                RenderHelper.enableGUIStandardItemLighting()
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
                GlStateManager.enableRescaleNormal()
                val itemStartX = x + textureBorder + 1
                val itemStartY = y + topBorder + 1
                for (i in 0 until length) {
                    val item: ItemStack = items[i]
                    if (item != null) {
                        val itemX = itemStartX + ((i % cols) * textureItemSquare)
                        val itemY = itemStartY + ((i / cols) * textureItemSquare)

                        val renderItem: RenderItem = mc.getRenderItem()
                        guiContainer.zLevel = 200f
                        renderItem.zLevel = 200f
                        renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY)
                        renderItem.renderItemOverlayIntoGUI(mc.fontRendererObj, item, itemX, itemY, null)
                        guiContainer.zLevel = 0f
                        renderItem.zLevel = 0f

                        if (frozen && mouseX > itemX && mouseX < itemX + 16 && mouseY > itemY && mouseY < itemY + 16) {
                            tooltipItem = item
                        }
                    }
                }
            } else {
                val totalWidth = (16 * cols) + 3
                if (x + totalWidth > guiContainer.width) {
                    x -= totalWidth
                }
                val totalHeight = (16 * rows) + 3
                if (y + totalHeight > screenHeight) {
                    y = screenHeight - totalHeight
                }

                GlStateManager.disableLighting()
                GlStateManager.pushMatrix()
                GlStateManager.translate(0f, 0f, 300f)
                Gui.drawRect(x, y, x + totalWidth, y + totalHeight, ColorCode.DARK_GRAY.getColor(250))
                GlStateManager.popMatrix()
                GlStateManager.enableLighting()

                RenderHelper.enableGUIStandardItemLighting()
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
                GlStateManager.enableRescaleNormal()
                for (i in 0 until length) {
                    val item: ItemStack = items[i]
                    if (item != null) {
                        val itemX = x + ((i % cols) * 16)
                        val itemY = y + ((i / cols) * 16)

                        val renderItem: RenderItem = mc.getRenderItem()
                        guiContainer.zLevel = 200f
                        renderItem.zLevel = 200f
                        renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY)
                        renderItem.renderItemOverlayIntoGUI(mc.fontRendererObj, item, itemX, itemY, null)
                        guiContainer.zLevel = 0f
                        renderItem.zLevel = 0f

                        if (frozen && mouseX > itemX && mouseX < itemX + 16 && mouseY > itemY && mouseY < itemY + 16) {
                            tooltipItem = item
                        }
                    }
                }
            }
            if (tooltipItem != null) {
                // Translate up to fix patcher glitch
                GlStateManager.pushMatrix()
                GlStateManager.translate(0f, 0f, 302f)
                drawingFrozenItemTooltip = true
                guiContainer.drawHoveringText(
                    tooltipItem.getTooltip(
                        mc.thePlayer,
                        mc.gameSettings.advancedItemTooltips
                    ), mouseX, mouseY
                )
                drawingFrozenItemTooltip = false
                GlStateManager.popMatrix()
            }
            if (!frozen) {
                currentContainerPreview = null
            }
            GlStateManager.enableLighting()
            GlStateManager.enableDepth()
            RenderHelper.enableStandardItemLighting()
        }
    }

    /**
     * Create a [ContainerPreview] from a backpack `ItemStack` in the storage menu and the list of items in that preview
     *
     * @param stack the backpack `ItemStack` that's being hovered over
     * @param items the items in the backpack
     * @return the container preview
     */
    fun getFromStorageBackpack(stack: ItemStack, items: List<ItemStack>?): ContainerPreview? {
        if (items == null) {
            return null
        }

        // Get the container color
        val color: BackpackColor = ItemUtils.getBackpackColor(stack)!!
        // Relying on item lore here. Once hypixel settles on a standard for backpacks, we should figure out a better way
        val skyblockID: String =
            TextUtils.stripColor(ItemUtils.getItemLore(stack).get(0)).toUpperCase().replace(" ", "_").trim()
        val containerData: ContainerData = ItemUtils.getContainerData(skyblockID)!!
        var rows = 6
        var cols = 9
        if (containerData != null) {
            // Hybrid system for jumbo backpacks means they get only 5 rows in the container (but old ones that haven't been converted get 6 outside of it)
            rows = Math.min(containerData.getNumRows(), 5)
            cols = containerData.getNumCols()
        } else if (TextUtils.stripColor(stack.getDisplayName()).toUpperCase().startsWith("ENDER CHEST")) {
            rows = min(5.0, (ceil((items.size / 9f).toDouble()) as Int).toDouble()).toInt()
        }

        return ContainerPreview(items, TextUtils.stripColor(stack.getDisplayName()), color, rows, cols)
    }
    private fun isFreezeKeyDown(): Boolean {
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance

        if (main.getFreezeBackpackKey().isKeyDown()) return true
        try {
            if (Keyboard.isKeyDown(main.getFreezeBackpackKey().getKeyCode())) return true
        } catch (ignored: java.lang.Exception) {
        }

        return false
    }


    /**
     * Called when a key is typed in a [GuiContainer]. Used to control backpack preview freezing.
     *
     * @param keyCode the key code of the key that was typed
     * @see codes.biscuit.skyblockaddons.asm.hooks.GuiContainerHook.keyTyped
     */
    fun onContainerKeyTyped(keyCode: Int) {
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance

        if (main.utils!!.isOnSkyblock()) {
            if (keyCode == 1 || keyCode == Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode()) {
                frozen = false
                currentContainerPreview = null
            }
            //TODO:
//            if (keyCode == main.getFreezeBackpackKey()
//                    .getKeyCode() && frozen && System.currentTimeMillis() - lastToggleFreezeTime > 500
//            ) {
//                lastToggleFreezeTime = System.currentTimeMillis()
//                frozen = false
//            }
        }
    }

    /**
     * Renders the corresponding container preview if the given `ItemStack` is a container.
     * If a container preview is rendered, `true` is returned to cancel the original tooltip render event.
     *
     * @param itemStack the `ItemStack` to render the container preview for
     * @param x the x-coordinate where the item's tooltip is rendered
     * @param y the y-coordinate where the item's tooltip is rendered
     * @return `true` if a container preview is rendered, `false` otherwise
     */
    fun onRenderTooltip(itemStack: ItemStack, x: Int, y: Int): Boolean {
        val main: SkyblockAddonsPlus.Companion =SkyblockAddonsPlus.instance

        // Cancel tooltips while containers are frozen and we aren't trying to render a tooltip in the backpack
        if (frozen && !drawingFrozenItemTooltip) {
            return true
        }

        if (main.configValues!!.isEnabled(Feature.SHOW_BACKPACK_PREVIEW)) {
            // Don't show if we only want to show while holding shift, and the player isn't holding shift
            if (main.configValues!!.isEnabled(Feature.SHOW_BACKPACK_HOLDING_SHIFT) && !GuiScreen.isShiftKeyDown()) {
                return false
            }
            // Don't render the preview the item represents a crafting recipe or the result of one.
            if (ItemUtils.isMenuItem(itemStack)) {
                return false
            }

            var containerPreview: ContainerPreview? = null
            // Check for cached storage previews
            if (main.inventoryUtils!!.getInventoryType() === InventoryType.STORAGE) {
                val strippedName: String = TextUtils.stripColor(itemStack.getDisplayName())
                var m: Matcher
                var storageKey: String? = null
                if ((BACKPACK_STORAGE_PATTERN.matcher(strippedName).also { m = it }).matches()) {
                    val pageNum = m.group("slot").toInt()
                    storageKey = InventoryType.STORAGE_BACKPACK.getInventoryName() + pageNum
                } else if (main.configValues!!.isEnabled(Feature.SHOW_ENDER_CHEST_PREVIEW) &&
                    (ENDERCHEST_STORAGE_PATTERN.matcher(strippedName).also { m = it }).matches()
                ) {
                    val pageNum = m.group("page").toInt()
                    storageKey = InventoryType.ENDER_CHEST.getInventoryName() + pageNum
                }
                if (storageKey != null) {
                    val cache: Map<String, CompressedStorage?> =
                        SkyblockAddonsPlus.persistentValuesManager!!.getPersistentValues()
                            .getStorageCache()
                    if (cache[storageKey] != null) {
                        val bytes = cache[storageKey]!!.getStorage()
                        var items: List<ItemStack>? = decompressItems(bytes)
                        // Clip out the top
                        items = items!!.subList(9, items.size)
                        containerPreview = getFromStorageBackpack(itemStack, items)
                    }
                }
            }
            // Check for normal previews
            if (containerPreview == null) {
                // Check the subfeature conditions
                val extraAttributes: NBTTagCompound = ItemUtils.getExtraAttributes(itemStack)!!
                val containerData: ContainerData =
                    ItemUtils.getContainerData(ItemUtils.getSkyblockItemID(extraAttributes))!!

                // TODO: Does checking menu item handle the baker inventory thing?
                if (containerData == null || (containerData.isCakeBag() && main.configValues!!
                        .isDisabled(Feature.CAKE_BAG_PREVIEW)) ||
                    (containerData.isPersonalCompactor() && main.configValues!!
                        .isDisabled(Feature.SHOW_PERSONAL_COMPACTOR_PREVIEW))
                ) {
                    return false
                }

                //TODO: Probably some optimizations here we can do. Can we check chest equivalence?
                // Avoid showing backpack preview in auction stuff.
                val playerContainer: Container = Minecraft.getMinecraft().thePlayer.openContainer
                if (playerContainer is ContainerChest) {
                    val chestInventory: IInventory = (playerContainer as ContainerChest).getLowerChestInventory()
                    if (chestInventory.hasCustomName()) {
                        val chestName: String = chestInventory.getDisplayName().getUnformattedText()
                        if (chestName.contains("Auction") || "Your Bids" == chestName) {
                            // Make sure this backpack is in the auction house and not just in your inventory before cancelling.

                            for (slotNumber in 0 until chestInventory.getSizeInventory()) {
                                if (chestInventory.getStackInSlot(slotNumber) == itemStack) {
                                    return false
                                }
                            }
                        }
                    }
                }

                containerPreview = getFromItem(itemStack)
            }

            if (containerPreview != null) {
                containerPreview.setX(x)
                containerPreview.setY(y)

                // Handle the freeze container toggle
                if (isFreezeKeyDown() && System.currentTimeMillis() - lastToggleFreezeTime > 500) {
                    lastToggleFreezeTime = System.currentTimeMillis()
                    frozen = !frozen
                    currentContainerPreview = containerPreview
                }

                if (!frozen) {
                    currentContainerPreview = containerPreview
                }
                return true
            }
        }

        return frozen
    }

    /**
     * Compresses the contents of the inventory
     *
     * @param inventory the inventory to be compressed
     * @return an nbt byte array of the compressed contents of the backpack
     */
    fun getCompressedInventoryContents(inventory: IInventory?): NBTTagByteArray? {
        if (inventory == null) {
            return null
        }
        val list: Array<ItemStack?> = arrayOfNulls<ItemStack>(inventory.getSizeInventory())
        for (slotNumber in 0 until inventory.getSizeInventory()) {
            list[slotNumber] = inventory.getStackInSlot(slotNumber)
        }
        return ItemUtils.getCompressedNBT(list)
    }

    /**
     * Saves the currently opened menu inventory to the backpack cache.
     * Triggers [PersistentValuesManager.saveValues] if the inventory has changed from the cached version.
     *
     * @param inventory the inventory to save the contents of
     * @param storageKey the key in which to store the data
     * @throws NullPointerException if `inventory` or `storageKey` are `null`
     */
    /**
     * Saves the currently opened menu inventory to the backpack cache.
     * Triggers [PersistentValuesManager.saveValues] if the inventory has changed from the cached version.
     *
     * @throws NullPointerException if [ContainerPreviewManager.containerInventory] or
     * [ContainerPreviewManager.storageKey] are `null`
     */
    @JvmOverloads
    fun saveStorageContainerInventory(
        inventory: InventoryBasic = containerInventory!!,
        storageKey: String = ContainerPreviewManager.storageKey!!,
    ) {
        if (inventory == null) {
            throw NullPointerException("Cannot save contents of a null inventory.")
        } else if (storageKey == null) {
            throw NullPointerException("Storage key is required to save the container's inventory.")
        }

        if (storageKey != ContainerPreviewManager.storageKey) {
            if (containerInventory != null) {
                saveStorageContainerInventory()
            }

            ContainerPreviewManager.storageKey = storageKey
        } else {
            // Get the cached storage containers
            val cache: MutableMap<String, CompressedStorage> =
                SkyblockAddonsPlus.persistentValuesManager!!.getPersistentValues().getStorageCache()
            // Get the cached container stored at this key
            val cachedContainer = cache[storageKey]
            val previousCache = cachedContainer?.getStorage()

            // Compute the compressed inventory of the current open inventory
            val inventoryContents: ByteArray = getCompressedInventoryContents(inventory)!!.getByteArray()

            // Check if the cache is dirty
            val dirty = previousCache == null || !previousCache.contentEquals(inventoryContents)

            if (dirty) {
                if (cachedContainer == null) {
                    cache[storageKey] = CompressedStorage(inventoryContents)
                    logger.info("Cached new container $storageKey.")
                } else {
                    cachedContainer.setStorage(inventoryContents)
                    logger.info("Refreshed cache for container $storageKey.")
                }

                SkyblockAddonsPlus.persistentValuesManager!!.saveValues()
            }

            resetCurrentContainer()
        }
    }

    /**
     * Resets the current container being cached by the manager.
     */
    private fun resetCurrentContainer() {
        containerInventory = null
        storageKey = null
    }
}
