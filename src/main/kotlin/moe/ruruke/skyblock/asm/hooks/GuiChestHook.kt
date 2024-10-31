package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.asm.utils.ReturnValue
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.InventoryType
import moe.ruruke.skyblock.core.Translations
import moe.ruruke.skyblock.core.npc.NPCUtils
import moe.ruruke.skyblock.features.backpacks.BackpackColor
import moe.ruruke.skyblock.features.backpacks.BackpackInventoryManager
import moe.ruruke.skyblock.utils.ColorCode
import moe.ruruke.skyblock.utils.DrawUtils
import moe.ruruke.skyblock.utils.ItemUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.init.Items
import net.minecraft.inventory.Container
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import org.apache.commons.lang3.StringUtils
import org.lwjgl.input.Keyboard
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

//TODO Fix for Hypixel localization
class GuiChestHook {
    companion object {
        @JvmStatic
        private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        @JvmStatic
        private val mc: Minecraft = Minecraft.getMinecraft()
        @JvmStatic
        private val fontRenderer: FontRenderer = mc.fontRendererObj

        /** Strings for reforge filter  */
        @JvmStatic
        private val TYPE_TO_MATCH: String = Translations.getMessage("messages.reforges")!!
        @JvmStatic
        private val TYPE_ENCHANTMENTS: String = Translations.getMessage("messages.typeEnchantmentsHere", TYPE_TO_MATCH)!!
        @JvmStatic
        private val SEPARATE_MULTIPLE: String = Translations.getMessage("messages.separateMultiple")!!
        @JvmStatic
        private val ENCHANTS_TO_INCLUDE: String = Translations.getMessage("messages.enchantsToMatch", TYPE_TO_MATCH)!!
        @JvmStatic
        private val INCLUSION_EXAMPLE: String = Translations.getMessage("messages.reforgeInclusionExample")!!
        @JvmStatic
        private val ENCHANTS_TO_EXCLUDE: String = Translations.getMessage("messages.enchantsToExclude", TYPE_TO_MATCH)!!
        @JvmStatic
        private val EXCLUSION_EXAMPLE: String = Translations.getMessage("messages.reforgeExclusionExample")!!

        private const val REFORGE_MENU_HEIGHT = 222 - 108 + 5 * 18

//    @Getter
//    private var islandWarpGui: IslandWarpGui? = null

        /** Reforge filter text field for reforges to match  */
        @JvmStatic
        private var textFieldMatches: GuiTextField? = null
        @JvmStatic
        fun getTextFieldMatches(): GuiTextField? {
            return textFieldMatches
        }

        /** Reforge filter text field for reforges to exclude  */
        @JvmStatic
        private var textFieldExclusions: GuiTextField? = null
        @JvmStatic
        private val warpPattern: Pattern = Pattern.compile("(?:§5§o)?§8/warp ([a-z_]*)")
        @JvmStatic
        private val unlockedPattern: Pattern = Pattern.compile("(?:§5§o)?§eClick to warp!")
        @JvmStatic
        private val notUnlockedPattern: Pattern = Pattern.compile("(?:§5§o)?§cWarp not unlocked!")
        @JvmStatic
        private val inCombatPattern: Pattern = Pattern.compile("(?:§5§o)?§cYou're in combat!")
        @JvmStatic
        private val youAreHerePattern: Pattern = Pattern.compile("(?:§5§o)?§aYou are here!")
        @JvmStatic
        private var reforgeFilterHeight = 0

        /** String dimensions for reforge filter  */
        @JvmStatic
        private var maxStringWidth = 0
        @JvmStatic
        private var typeEnchantmentsHeight = 0
        @JvmStatic
        private var enchantsToIncludeHeight = 0
        @JvmStatic
        private var enchantsToExcludeHeight = 0

        /**
         * @see codes.biscuit.skyblockaddons.asm.GuiChestTransformer.transform
         */
        @JvmStatic
        @Suppress("unused")
        fun updateScreen() {
            if (textFieldMatches != null && textFieldExclusions != null) {
                textFieldMatches!!.updateCursorCounter()
                textFieldExclusions!!.updateCursorCounter()
            }
        }

        /**
         * Resets variables when the chest is closed
         *
         * @see codes.biscuit.skyblockaddons.asm.GuiChestTransformer.transform
         */
        @Suppress("unused")
        @JvmStatic
        fun onGuiClosed() {
            Keyboard.enableRepeatEvents(false)

            //TODO:
//            islandWarpGui = null
            BackpackInventoryManager.setBackpackColor(null)

            if (main.configValues!!.isEnabled(Feature.SHOW_SALVAGE_ESSENCES_COUNTER)) {
                val inventoryType: InventoryType = main.inventoryUtils!!.getInventoryType()!!

                //TODO:
//                if (inventoryType === InventoryType.SALVAGING) {
//                    main.getDungeonManager().getSalvagedEssences().clear()
//                }
            }
        }

        /**
         * @see codes.biscuit.skyblockaddons.asm.GuiChestTransformer.transform
         */
        @Suppress("unused")
        @JvmStatic
        fun drawScreenIslands(mouseX: Int, mouseY: Int, returnValue: ReturnValue<*>) {
            if (!SkyblockAddonsPlus.utils!!.isOnSkyblock()) {
                return  // don't draw any overlays outside SkyBlock
            }

            //TODO:
//            val playerContainer: Container = mc.thePlayer.openContainer
//            if (playerContainer is ContainerChest && SkyblockAddonsPlus.configValues!!
//                    .isEnabled(Feature.FANCY_WARP_MENU)
//            ) {
//                val chestInventory: IInventory = (playerContainer as ContainerChest).getLowerChestInventory()
//                if (chestInventory.hasCustomName()) {
//                    val chestName: String = chestInventory.getDisplayName().getUnformattedText()
//                    if (chestName == "Fast Travel") {
////                        val markers: MutableMap<IslandWarpGui.Marker?, IslandWarpGui.UnlockedStatus> = mutableMapOf<Any?, Any?>(
////                            IslandWarpGui.Marker::class.java
////                        )
//
//                        for (slot in 0 until chestInventory.getSizeInventory()) {
//                            val itemStack: ItemStack = chestInventory.getStackInSlot(slot)
//
//                            if (itemStack != null && (Items.skull === itemStack.getItem() || Items.paper === itemStack.getItem())) {
//                                val lore: List<String> = ItemUtils.getItemLore(itemStack)
//                                var marker: IslandWarpGui.Marker? = null
//                                var status: IslandWarpGui.UnlockedStatus = IslandWarpGui.UnlockedStatus.UNKNOWN
//
//                                for (loreLine in lore) {
//                                    var matcher = warpPattern.matcher(loreLine)
//                                    if (matcher.matches()) {
//                                        marker = IslandWarpGui.Marker.fromWarpName(matcher.group(1))
//                                    }
//
//                                    matcher = unlockedPattern.matcher(loreLine)
//                                    if (matcher.matches() || youAreHerePattern.matcher(loreLine).matches()) {
//                                        status = IslandWarpGui.UnlockedStatus.UNLOCKED
//                                        break
//                                    }
//
//                                    matcher = notUnlockedPattern.matcher(loreLine)
//                                    if (matcher.matches()) {
//                                        status = IslandWarpGui.UnlockedStatus.NOT_UNLOCKED
//                                        break
//                                    }
//
//                                    matcher = inCombatPattern.matcher(loreLine)
//                                    if (matcher.matches()) {
//                                        status = IslandWarpGui.UnlockedStatus.IN_COMBAT
//                                        break
//                                    }
//                                }
//
//                                if (marker != null) {
//                                    markers[marker] = status
//                                }
//                            }
//                        }
//
//                        for (marker in IslandWarpGui.Marker.values()) {
//                            if (!markers.containsKey(marker)) {
//                                markers[marker] = IslandWarpGui.UnlockedStatus.UNKNOWN
//                            }
//                        }
//
//                        /*
//                        Special case: We have an extra dungeon hub warp as a separate island for convenience, so we have to
//                        add it manually.
//                         */
//                        markers[IslandWarpGui.Marker.DUNGEON_HUB_ISLAND] =
//                            markers.getOrDefault(IslandWarpGui.Marker.DUNGEON_HUB, IslandWarpGui.UnlockedStatus.UNKNOWN)
//
//                        if (islandWarpGui == null || !islandWarpGui.getMarkers().equals(markers)) {
//                            islandWarpGui = IslandWarpGui(markers)
//                            val scaledresolution: ScaledResolution = ScaledResolution(mc)
//                            val i: Int = scaledresolution.getScaledWidth()
//                            val j: Int = scaledresolution.getScaledHeight()
//                            islandWarpGui.setWorldAndResolution(mc, i, j)
//                        }
//
//                        try {
//                            islandWarpGui.drawScreen(mouseX, mouseY, 0)
//                        } catch (ex: Throwable) {
//                            ex.printStackTrace()
//                        }
//
//                        returnValue.cancel()
//                    } else {
//                        islandWarpGui = null
//                    }
//                } else {
//                    islandWarpGui = null
//                }
//            } else {
//                islandWarpGui = null
//            }
        }
        @JvmStatic
        fun drawScreen(guiLeft: Int, guiTop: Int) {
            if (!SkyblockAddonsPlus.utils!!.isOnSkyblock()) {
                return  // don't draw any overlays outside SkyBlock
            }

            val inventoryType: InventoryType = SkyblockAddonsPlus.inventoryUtils!!.getInventoryType() ?: return
            if (inventoryType === InventoryType.SALVAGING) {
                val ySize = 222 - 108 + 6 * 18
                val x = (guiLeft - 69 - 5).toFloat()
                val y = guiTop + ySize / 2f - 72 / 2f

                SkyblockAddonsPlus.renderListener!!.drawCollectedEssences(x, y, false, false)
            }

            if (SkyblockAddonsPlus.configValues!!.isEnabled(Feature.REFORGE_FILTER)) {
                if ((inventoryType === InventoryType.BASIC_REFORGING) &&
                    textFieldMatches != null
                ) {
                    val defaultBlue: Int = main.utils!!.getDefaultBlue(255)
                    var x = guiLeft - 160
                    if (x < 0) {
                        x = 20
                    }
                    var y = guiTop + REFORGE_MENU_HEIGHT / 2 - reforgeFilterHeight / 2

                    GlStateManager.color(1f, 1f, 1f)
                    fontRenderer.drawSplitString(TYPE_ENCHANTMENTS, x, y, maxStringWidth, defaultBlue)
                    y = y + typeEnchantmentsHeight
                    fontRenderer.drawSplitString(SEPARATE_MULTIPLE, x, y, maxStringWidth, defaultBlue)

                    val placeholderTextX: Int = textFieldMatches!!.xPosition + 4
                    var placeholderTextY: Int = textFieldMatches!!.yPosition + (textFieldMatches!!.height - 8) / 2

                    y = textFieldMatches!!.yPosition - enchantsToIncludeHeight - 1
                    fontRenderer.drawSplitString(ENCHANTS_TO_INCLUDE, x, y, maxStringWidth, defaultBlue)

                    textFieldMatches!!.drawTextBox()
                    if (StringUtils.isEmpty(textFieldMatches!!.getText())) {
                        fontRenderer.drawString(
                            fontRenderer.trimStringToWidth(INCLUSION_EXAMPLE, textFieldMatches!!.width),
                            placeholderTextX,
                            placeholderTextY,
                            ColorCode.DARK_GRAY.getColor()
                        )
                    }

                    y = textFieldExclusions!!.yPosition - enchantsToExcludeHeight - 1
                    fontRenderer.drawSplitString(ENCHANTS_TO_EXCLUDE, x, y, maxStringWidth, defaultBlue)

                    placeholderTextY = textFieldExclusions!!.yPosition + (textFieldExclusions!!.height - 8) / 2
                    textFieldExclusions!!.drawTextBox()
                    if (StringUtils.isEmpty(textFieldExclusions!!.getText())) {
                        fontRenderer.drawString(
                            fontRenderer.trimStringToWidth(
                                EXCLUSION_EXAMPLE,
                                textFieldExclusions!!.width
                            ), placeholderTextX, placeholderTextY, ColorCode.DARK_GRAY.getColor()
                        )
                    }
                }
            }
        }

        /**
         * @see codes.biscuit.skyblockaddons.asm.GuiChestTransformer.transform
         */
        @JvmStatic
        @Suppress("unused")
        fun initGui(lowerChestInventory: IInventory?, guiLeft: Int, guiTop: Int, fontRendererObj: FontRenderer) {
            if (!SkyblockAddonsPlus.utils!!.isOnSkyblock()) {
                return  // don't draw any overlays outside SkyBlock
            }

            val inventoryType: InventoryType? = SkyblockAddonsPlus.inventoryUtils?.getInventoryType()

            if (inventoryType != null) {
                if (SkyblockAddonsPlus.configValues!!.isEnabled(Feature.REFORGE_FILTER) && inventoryType ===
                    InventoryType.BASIC_REFORGING
                ) {
                    var xPos = guiLeft - 160
                    if (xPos < 0) {
                        xPos = 20
                    }
                    val textFieldWidth = guiLeft - 20 - xPos
                    val textFieldHeight = REFORGE_MENU_HEIGHT / 10
                    val textFieldSpacing = (textFieldHeight * 1.5).toInt()

                    // Calculate the height of the whole thing to center it vertically in relation to the chest UI.
                    maxStringWidth = textFieldWidth + 5
                    typeEnchantmentsHeight = fontRenderer.splitStringWidth(TYPE_ENCHANTMENTS, maxStringWidth)
                    val separateEnchantmentsHeight: Int =
                        fontRenderer.splitStringWidth(SEPARATE_MULTIPLE, maxStringWidth) + fontRendererObj.FONT_HEIGHT
                    enchantsToIncludeHeight = fontRenderer.splitStringWidth(ENCHANTS_TO_INCLUDE, maxStringWidth)
                    enchantsToExcludeHeight = fontRenderer.splitStringWidth(ENCHANTS_TO_EXCLUDE, maxStringWidth)
                    reforgeFilterHeight =
                        typeEnchantmentsHeight + separateEnchantmentsHeight + enchantsToIncludeHeight + 2 * textFieldHeight + textFieldSpacing

                    var yPos =
                        guiTop + REFORGE_MENU_HEIGHT / 2 - reforgeFilterHeight / 2

                    // Matches text field
                    yPos = yPos + typeEnchantmentsHeight + separateEnchantmentsHeight + enchantsToIncludeHeight
                    textFieldMatches = GuiTextField(2, fontRendererObj, xPos, yPos, textFieldWidth, textFieldHeight)
                    textFieldMatches!!.setMaxStringLength(500)
                    val reforgeMatches: List<String> = SkyblockAddonsPlus.utils!!.getReforgeMatches()
                    var reforgeBuilder = StringBuilder()

                    for (i in reforgeMatches.indices) {
                        reforgeBuilder.append(reforgeMatches[i])
                        if (i < reforgeMatches.size - 1) {
                            reforgeBuilder.append(',')
                        }
                    }
                    var text = reforgeBuilder.toString()
                    if (text.length > 0) {
                        textFieldMatches!!.setText(text)
                    }

                    // Exclusions text field
                    yPos = yPos + textFieldHeight + textFieldSpacing
                    textFieldExclusions = GuiTextField(2, fontRendererObj, xPos, yPos, textFieldWidth, textFieldHeight)
                    textFieldExclusions!!.setMaxStringLength(500)
                    val reforgeExclusions: List<String> = SkyblockAddonsPlus.utils!!.getReforgeExclusions()
                    reforgeBuilder = StringBuilder()

                    for (i in reforgeExclusions.indices) {
                        reforgeBuilder.append(reforgeExclusions[i])
                        if (i < reforgeExclusions.size - 1) {
                            reforgeBuilder.append(',')
                        }
                    }
                    text = reforgeBuilder.toString()
                    if (text.length > 0) {
                        textFieldExclusions!!.setText(text)
                    }

                    Keyboard.enableRepeatEvents(true)
                }
            }
        }
        @JvmStatic
        fun keyTyped(
            typedChar: Char,
            keyCode: Int
        ): Boolean { // return whether to continue (super.keyTyped(typedChar, keyCode);)
            if (main.utils!!.isOnSkyblock() && main.configValues!!.isEnabled(Feature.REFORGE_FILTER)) {
                val inventoryType: InventoryType = main.inventoryUtils!!.getInventoryType()!!

                if (inventoryType === InventoryType.BASIC_REFORGING) {
                    if (keyCode != mc.gameSettings.keyBindInventory.getKeyCode() ||
                        (!textFieldMatches!!.isFocused() && !textFieldExclusions!!.isFocused())
                    ) {
                        processTextFields(typedChar, keyCode)
                        return true
                    }
                    processTextFields(typedChar, keyCode)
                } else {
                    return true
                }
                return false
            } else {
                return true
            }
        }
        @JvmStatic
        private fun processTextFields(typedChar: Char, keyCode: Int) {
            if (main.configValues!!.isEnabled(Feature.REFORGE_FILTER) && textFieldMatches != null) {
                textFieldMatches!!.textboxKeyTyped(typedChar, keyCode)
                textFieldExclusions!!.textboxKeyTyped(typedChar, keyCode)
                var reforges: List<String> = LinkedList<String>(
                    Arrays.asList<String>(
                        *textFieldMatches!!.getText().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    )
                )
                main.utils!!.setReforgeMatches(reforges)
                reforges = LinkedList<String>(
                    Arrays.asList<String>(
                        *textFieldExclusions!!.getText().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    )
                )
                main.utils!!.setReforgeExclusions(reforges)
            }
        }

        /**
         * @see codes.biscuit.skyblockaddons.asm.GuiChestTransformer.transform
         */
        @JvmStatic
        @Suppress("unused")
        fun handleMouseClick(
            slotIn: Slot?,
            slots: Container,
            lowerChestInventory: IInventory?,
            returnValue: ReturnValue<*>
        ) {
            if (main.utils!!.isOnSkyblock()) {
                if (main.configValues!!.isEnabled(Feature.REFORGE_FILTER) && !main.utils!!.getReforgeMatches()
                        .isEmpty()
                ) {
                    if (slotIn != null && slotIn.inventory != mc.thePlayer.inventory && slotIn.hasStack) {
                        val inventoryType: InventoryType = main.inventoryUtils!!.getInventoryType()!!

                        if (slotIn.slotIndex == 22 && (inventoryType === InventoryType.BASIC_REFORGING)) {
                            val itemSlot = slots.getSlot(13)

                            if (itemSlot != null && itemSlot.hasStack) {
                                val item: ItemStack = itemSlot.stack
                                if (item.hasDisplayName()) {
                                    val reforge: String = ItemUtils.getReforge(item)!!
                                    if (reforge != null) {
                                        if (main.utils!!.enchantReforgeMatches(reforge)) {
                                            main.utils!!.playLoudSound("random.orb", 0.1)
                                            returnValue.cancel()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (main.configValues!!.isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) && !main.utils!!
                        .isInDungeon() &&
                    NPCUtils.isSellMerchant(lowerChestInventory!!) && slotIn != null && slotIn.inventory is InventoryPlayer
                ) {
                    if (!main.utils!!.getItemDropChecker().canDropItem(slotIn)) {
                        returnValue.cancel()
                    }
                }
            }
        }

        /**
         * Handles mouse clicks for the Fancy Warp GUI and the Reforge Filter text fields.
         *
         * @param mouseX x coordinate of the mouse pointer
         * @param mouseY y coordinate of the mouse pointer
         * @param mouseButton mouse button that was clicked
         */
        @JvmStatic
        @Throws(IOException::class)
        fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int, returnValue: ReturnValue<*>) {
            //TODO:
//            if (islandWarpGui != null) {
//                islandWarpGui.mouseClicked(mouseX, mouseY, mouseButton)
//                returnValue.cancel()
//                return
//            }

            if (textFieldMatches != null) {
                textFieldMatches!!.mouseClicked(mouseX, mouseY, mouseButton)
                textFieldExclusions!!.mouseClicked(mouseX, mouseY, mouseButton)
            }
        }
        @JvmStatic
        fun color(
            colorRed: Float,
            colorGreen: Float,
            colorBlue: Float,
            colorAlpha: Float,
            lowerChestInventory: IInventory
        ) { //Item item, ItemStack stack
            if (!main.utils!!.isOnSkyblock()) {
                return
            }

            if (main.configValues!!.isEnabled(Feature.SHOW_BACKPACK_PREVIEW) &&
                main.configValues!!
                    .isEnabled(Feature.MAKE_BACKPACK_INVENTORIES_COLORED) && lowerChestInventory.hasCustomName()
            ) {
                if (lowerChestInventory.getDisplayName().getUnformattedText().contains("Backpack")) {
                    if (BackpackInventoryManager.getBackpackColor() != null) {
                        val color: BackpackColor = BackpackInventoryManager.getBackpackColor()!!
                        GlStateManager.color(color.getR(), color.getG(), color.getB(), 1f)
                        return
                    }
                } else if (lowerChestInventory.getDisplayName().getUnformattedText().contains("Bank")) {
                    val item: ItemStack = mc.thePlayer.getHeldItem() // easter egg question mark
                    if (item != null && item.hasDisplayName() && item.getDisplayName().contains("Piggy Bank")) {
                        val color: BackpackColor = BackpackColor.PINK
                        GlStateManager.color(color.getR(), color.getG(), color.getB(), 1f)
                    }
                    return
                }
            }
            GlStateManager.color(colorRed, colorGreen, colorBlue, colorAlpha)
        }

        @JvmStatic
        fun drawString(fontRenderer: FontRenderer, text: String?, x: Int, y: Int, color: Int): Int {
            if (main.utils!!.isOnSkyblock() && main.configValues!!.isEnabled(Feature.SHOW_BACKPACK_PREVIEW) &&
                main.configValues!!
                    .isEnabled(Feature.MAKE_BACKPACK_INVENTORIES_COLORED) && BackpackInventoryManager.getBackpackColor() != null
            ) {
                return fontRenderer.drawString(
                    text,
                    x,
                    y,
                    BackpackInventoryManager.getBackpackColor()!!.getInventoryTextColor()
                )
            }
            return fontRenderer.drawString(text, x, y, color)
        }

        /**
         * @see codes.biscuit.skyblockaddons.asm.GuiChestTransformer.transform
         */
        @Suppress("unused")
        @JvmStatic
        fun mouseReleased(returnValue: ReturnValue<*>) {
            //TODO:
//            if (islandWarpGui != null) {
//                returnValue.cancel()
//            }
        }

        /**
         * @see codes.biscuit.skyblockaddons.asm.GuiChestTransformer.transform
         */
        @Suppress("unused")
        @JvmStatic
        fun mouseClickMove(returnValue: ReturnValue<*>) {
            //TODO:
//            if (islandWarpGui != null) {
//                returnValue.cancel()
//            }
        }

        /**
         * @see codes.biscuit.skyblockaddons.asm.GuiChestTransformer.transform
         */
        @Suppress("unused")
        @JvmStatic
        fun onRenderChestForegroundLayer(guiChest: GuiChest) {
            if (!SkyblockAddonsPlus.utils!!.isOnSkyblock()) {
                return  // don't draw any overlays outside SkyBlock
            }

            if (main.configValues!!.isEnabled(Feature.SHOW_REFORGE_OVERLAY)) {
                if (guiChest.inventorySlots.inventorySlots.size > 13) {
                    val slot: Slot = guiChest.inventorySlots.inventorySlots.get(13)
                    if (slot != null) {
                        val item: ItemStack? = slot.stack
                        if (item != null) {
                            var reforge: String? = null
                            if (main.inventoryUtils!!.getInventoryType() === InventoryType.BASIC_REFORGING) {
                                reforge = ItemUtils.getReforge(item)
                            }

                            if (reforge != null) {
                                var color: Int = ColorCode.YELLOW.getColor()
                                if (main.configValues!!.isEnabled(Feature.REFORGE_FILTER) &&
                                    !main.utils!!.getReforgeMatches().isEmpty() &&
                                    main.utils!!.enchantReforgeMatches(reforge)
                                ) {
                                    color = ColorCode.RED.getColor()
                                }

                                val x = slot.xDisplayPosition
                                val y = slot.yDisplayPosition

                                val stringWidth: Int = mc.fontRendererObj.getStringWidth(reforge)
                                val renderX = x - 28 - stringWidth / 2f
                                val renderY = y + 22

                                GlStateManager.disableDepth()
                                drawTooltipBackground(renderX, renderY.toFloat(), stringWidth.toFloat())
                                mc.fontRendererObj.drawString(reforge, renderX, renderY.toFloat(), color, true)
                                GlStateManager.enableDepth()
                            }
                        }
                    }
                }
            }
        }
        @JvmStatic
        private fun drawTooltipBackground(x: Float, y: Float, width: Float) {
            val l = -267386864
            DrawUtils.drawRectAbsolute(x - 3.0, y - 4.0, x + width + 3.0, y - 3.0, l)
            DrawUtils.drawRectAbsolute(x - 3.0, y + 8.0 + 3.0, x + width + 3.0, y + 8.0 + 4.0, l)
            DrawUtils.drawRectAbsolute(x - 3.0, y - 3.0, x + width + 3.0, y + 8.0 + 3.0, l)
            DrawUtils.drawRectAbsolute(x - 4.0, y - 3.0, x - 3.0, y + 8.0 + 3.0, l)
            DrawUtils.drawRectAbsolute(x + width + 3.0, y - 3.0, x + width + 4.0, y + 8.0 + 3.0, l)

            val borderColor = 1347420415
            DrawUtils.drawRectAbsolute(x - 3.0, y - 3.0 + 1.0, x - 3.0 + 1.0, y + 8.0 + 3.0 - 1.0, borderColor)
            DrawUtils.drawRectAbsolute(x + width + 2.0, y - 3.0 + 1.0, x + width + 3.0, y + 8.0 + 3.0 - 1.0, borderColor)
            DrawUtils.drawRectAbsolute(x - 3.0, y - 3.0, x + width + 3.0, y - 3.0 + 1.0, borderColor)
            DrawUtils.drawRectAbsolute(x - 3.0, y + 8.0 + 2.0, x + width + 3.0, y + 8.0 + 3.0, borderColor)
        }
    }
}
