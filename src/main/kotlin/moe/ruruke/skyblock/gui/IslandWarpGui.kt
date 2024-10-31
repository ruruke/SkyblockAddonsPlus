//package moe.ruruke.skyblock.gui
//
//import moe.ruruke.skyblock.SkyblockAddons
//
//
//import net.minecraft.init.Items
//import net.minecraft.inventory.Slot
//import net.minecraft.util.ResourceLocation
//import java.awt.Color
//import java.awt.image.BufferedImage
//import java.io.IOException
//import java.util.*
//
//class IslandWarpGui : GuiScreen {
//    
//    private val markers: Map<Marker, UnlockedStatus>
//
//    private var selectedMarker: Marker? = null
//    private var guiIsActualWarpMenu = false
//
//    
//    private var foundAdvancedWarpToggle = false
//
//    constructor() : super() {
//        val markers: MutableMap<Marker, UnlockedStatus> = EnumMap(
//            Marker::class.java
//        )
//        for (marker in Marker.entries) {
//            markers[marker] = UnlockedStatus.UNLOCKED
//        }
//        this.markers = markers
//    }
//
//    constructor(markers: Map<Marker, UnlockedStatus>) : super() {
//        this.markers = markers
//        this.guiIsActualWarpMenu = true
//    }
//
//    override fun initGui() {
//        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
//
//        val islands: MutableMap<Island, UnlockedStatus> = EnumMap(
//            Island::class.java
//        )
//
//        for ((key, newStatus) in markers) {
//            val island: Island = key.getIsland()
//            val currentStatus = islands[island]
//
//            if (currentStatus == null || newStatus.ordinal > currentStatus.ordinal) {
//                islands[island] = newStatus
//            }
//        }
//
//        for ((key, value) in islands) {
//            this.buttonList.add(IslandButton(key, value, markers))
//        }
//
//        val screenWidth: Int = mc.displayWidth
//        val screenHeight: Int = mc.displayHeight
//
//        ISLAND_SCALE = 0.7f / 1080 * screenHeight
//
//        val scale = ISLAND_SCALE
//        val totalWidth = TOTAL_WIDTH * scale
//        val totalHeight = TOTAL_HEIGHT * scale
//        SHIFT_LEFT = (screenWidth / 2f - totalWidth / 2f) / scale
//        SHIFT_TOP = (screenHeight / 2f - totalHeight / 2f) / scale
//
//        val x = Math.round(screenWidth / ISLAND_SCALE - SHIFT_LEFT - 475)
//        val y = Math.round(screenHeight / ISLAND_SCALE - SHIFT_TOP)
//
//        if (guiIsActualWarpMenu) {
//            this.buttonList.add(
//                ButtonToggleNew(x, y - 30 - 60 * 3, 50,
//                    label@{
//                        // Finds the advanced mode toggle button to see if it's enabled or not.
//                        val guiScreen: GuiScreen = Minecraft.getMinecraft().currentScreen
//                        if (guiScreen is GuiChest) {
//                            val gui: GuiChest = guiScreen as GuiChest
//
//                            val toggleAdvancedModeSlot: Slot = gui.inventorySlots.getSlot(51)
//                            if (toggleAdvancedModeSlot != null && toggleAdvancedModeSlot.hasStack) {
//                                val toggleAdvancedModeItem: ItemStack = toggleAdvancedModeSlot.stack
//
//                                if (Items.dye === toggleAdvancedModeItem.getItem()) {
//                                    val damage: Int = toggleAdvancedModeItem.getItemDamage()
//                                    if (damage == 10) { // Lime Dye
//                                        foundAdvancedWarpToggle = true
//
//                                        return@label true
//                                    } else if (damage == 8) { // Grey Dye
//                                        foundAdvancedWarpToggle = true
//
//                                        return@label false
//                                    }
//                                }
//                            }
//                        }
//                        false
//                    },
//                    label@{
//                        if (!foundAdvancedWarpToggle) return@label
//                        // This will click the advanced mode button for you.
//                        val guiScreen: GuiScreen = Minecraft.getMinecraft().currentScreen
//                        if (guiScreen is GuiChest) {
//                            val gui: GuiChest = guiScreen as GuiChest
//                            this.mc.playerController.windowClick(
//                                gui.inventorySlots.windowId,
//                                51,
//                                0,
//                                0,
//                                this.mc.thePlayer
//                            )
//                        }
//                    })
//            )
//            this.buttonList.add(ButtonToggleNew(
//                x, y - 30 - 60 * 2, 50
//            ) { SkyblockAddonsPlus.configValues })
//            !!
//            TODO(
//                """
//                |Cannot convert element
//                |With text:
//                |isEnabled(Feature.FANCY_WARP_MENU),
//                |                    () -> Feature.FANCY_WARP_MENU.setEnabled(!SkyblockAddonsPlus.configValues
//                """.trimMargin()
//            )
//            !!
//            isEnabled(Feature.FANCY_WARP_MENU)
//        }
//        this.buttonList.add(ButtonToggleNew(
//            x, y - 30 - 60, 50
//        ) { SkyblockAddonsPlus.configValues })
//        !!
//        TODO(
//            """
//            |Cannot convert element
//            |With text:
//            |isEnabled(Feature.DOUBLE_WARP),
//            |                () -> Feature.DOUBLE_WARP.setEnabled(!SkyblockAddonsPlus.configValues
//            """.trimMargin()
//        )
//        !!
//        isEnabled(Feature.DOUBLE_WARP)
//    }
//
//    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
//        val sr: ScaledResolution = ScaledResolution(mc)
//        val guiScale: Int = sr.getScaleFactor()
//
//        val startColor = Color(0, 0, 0, Math.round(255 / 3f)).rgb
//        val endColor = Color(0, 0, 0, Math.round(255 / 2f)).rgb
//        drawGradientRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), startColor, endColor)
//
//        drawCenteredString(
//            mc.fontRendererObj,
//            Translations.getMessage("warpMenu.click"),
//            sr.getScaledWidth() / 2,
//            10,
//            -0x1
//        )
//        drawCenteredString(
//            mc.fontRendererObj,
//            Translations.getMessage("warpMenu.mustUnlock"),
//            sr.getScaledWidth() / 2,
//            20,
//            -0x1
//        )
//
//        GlStateManager.pushMatrix()
//        ISLAND_SCALE = 0.7f / 1080 * mc.displayHeight
//        val scale = ISLAND_SCALE
//        GlStateManager.scale(1f / guiScale, 1f / guiScale, 1f)
//        GlStateManager.scale(scale, scale, 1f)
//
//        val totalWidth = TOTAL_WIDTH * scale
//        val totalHeight = TOTAL_HEIGHT * scale
//
//        SHIFT_LEFT = (mc.displayWidth / 2f - totalWidth / 2f) / scale
//        SHIFT_TOP = (mc.displayHeight / 2f - totalHeight / 2f) / scale
//        GlStateManager.translate(SHIFT_LEFT, SHIFT_TOP, 0f)
//
//        GlStateManager.enableAlpha()
//        GlStateManager.enableBlend()
//
//        var lastHoveredButton: IslandButton? = null
//
//        for (button in buttonList) {
//            if (button is IslandButton) {
//                val islandButton: IslandButton = button as IslandButton
//
//                // Call this just so it calculates the hover, don't actually draw.
//                islandButton.drawButton(mc, mouseX, mouseY, false)
//
//                if (islandButton.isHovering()) {
//                    if (lastHoveredButton != null) {
//                        lastHoveredButton.setDisableHover(true)
//                    }
//                    lastHoveredButton = islandButton
//                }
//            }
//        }
//
//        for (guiButton in this.buttonList) {
//            guiButton.drawButton(this.mc, mouseX, mouseY)
//        }
//
//
//        val x = Math.round(mc.displayWidth / ISLAND_SCALE - SHIFT_LEFT - 500)
//            .toInt()
//        val y = Math.round(mc.displayHeight / ISLAND_SCALE - SHIFT_TOP)
//            .toInt()
//
//        GlStateManager.pushMatrix()
//        val textScale = 3f
//        GlStateManager.scale(textScale, textScale, 1f)
//        if (guiIsActualWarpMenu) {
//            mc.fontRendererObj.drawStringWithShadow(
//                Feature.WARP_ADVANCED_MODE.getMessage(),
//                x / textScale + 50,
//                (y - 30 - 60 * 3) / textScale + 5,
//                -0x1
//            )
//            mc.fontRendererObj.drawStringWithShadow(
//                Feature.FANCY_WARP_MENU.getMessage(),
//                x / textScale + 50,
//                (y - 30 - 60 * 2) / textScale + 5,
//                -0x1
//            )
//        }
//        mc.fontRendererObj.drawStringWithShadow(
//            Feature.DOUBLE_WARP.getMessage(),
//            x / textScale + 50,
//            (y - 30 - 60) / textScale + 5,
//            -0x1
//        )
//        GlStateManager.popMatrix()
//
//        GlStateManager.popMatrix()
//
//        detectClosestMarker(mouseX, mouseY)
//    }
//
//    @Throws(IOException::class)
//    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
//        var mouseX = mouseX
//        var mouseY = mouseY
//        if (mouseButton == 0 && selectedMarker != null) {
//            Minecraft.getMinecraft().displayGuiScreen(null)
//
//            if (SkyblockAddonsPlus.configValues);
//            !!
//            isEnabled(Feature.DOUBLE_WARP)
//            run {
//                doubleWarpMarker = selectedMarker
//                // Remove the marker if it didn't trigger for some reason...
//                SkyblockAddons.getInstance().getNewScheduler().scheduleDelayedTask(object : SkyblockRunnable() {
//                    override fun run() {
//                        if (doubleWarpMarker != null) {
//                            doubleWarpMarker = null
//                        }
//                    }
//                }, 20)
//            }
//            if (selectedMarker != null) {
//                Minecraft.getMinecraft().thePlayer.sendChatMessage("/warp " + selectedMarker.getWarpName())
//            } /*else {
//                // Weirdly, this command is /warpforge instead of /warp forge
//                Minecraft.getMinecraft().thePlayer.sendChatMessage("/warp" + selectedMarker.getWarpName());
//            }*/
//        }
//
//        val minecraftScale: Int = ScaledResolution(mc).getScaleFactor()
//        val islandGuiScale = ISLAND_SCALE
//
//        mouseX *= minecraftScale
//        mouseY *= minecraftScale
//
//        mouseX = (mouseX / islandGuiScale).toInt()
//        mouseY = (mouseY / islandGuiScale).toInt()
//
//        mouseX = (mouseX - SHIFT_LEFT).toInt()
//        mouseY = (mouseY - SHIFT_TOP).toInt()
//
//        super.mouseClicked(mouseX, mouseY, mouseButton)
//    }
//
//
//    fun detectClosestMarker(mouseX: Int, mouseY: Int) {
//        var mouseX = mouseX
//        var mouseY = mouseY
//        val minecraftScale: Int = ScaledResolution(mc).getScaleFactor()
//        val islandGuiScale = ISLAND_SCALE
//
//        mouseX *= minecraftScale
//        mouseY *= minecraftScale
//
//        mouseX = (mouseX / islandGuiScale).toInt()
//        mouseY = (mouseY / islandGuiScale).toInt()
//
//        mouseX = (mouseX - SHIFT_LEFT).toInt()
//        mouseY = (mouseY - SHIFT_TOP).toInt()
//
//        var hoveredMarker: Marker? = null
//        var markerDistance: Double = IslandMarkerButton.MAX_SELECT_RADIUS + 1
//
//        for (button in this.buttonList) {
//            if (button is IslandButton) {
//                val islandButton: IslandButton = button as IslandButton
//
//                for (marker in islandButton.getMarkerButtons()) {
//                    val distance: Double = marker.getDistance(mouseX, mouseY)
//
//                    if (distance != -1.0 && distance < markerDistance) {
//                        hoveredMarker = marker.getMarker()
//                        markerDistance = distance
//                    }
//                }
//            }
//        }
//
//        selectedMarker = hoveredMarker
//
//        //        if (hoveredMarker != null) System.out.println(hoveredMarker.getLabel()+" "+markerDistance);
//    }
//
//    
//    enum class Island(private val label: String, private val x: Int, private val y: Int) {
//        THE_END("The End", 290, -10),
//        CRIMSON_ISLE("Crimson Isle", 900, -50),
//        THE_PARK("The Park", 103, 370),
//        SPIDERS_DEN("Spider's Den", 500, 420),
//        DEEP_CAVERNS("Deep Caverns", 1400, 200),
//        GOLD_MINE("Gold Mine", 1130, 475),
//        MUSHROOM_DESERT("Mushroom Desert", 1470, 475),
//        THE_BARN("The Barn", 1125, 800),
//        HUB("Hub", 300, 724),
//        PRIVATE_ISLAND("Private Island", 275, 1122),
//        DUNGEON_HUB("Dungeon Hub", 1500, 1050);
//
//        private var w: Int
//        private var h: Int
//
//        private val resourceLocation =
//            ResourceLocation("skyblockaddonsplus", "islands/" + name.lowercase().replace("_", "") + ".png")
//        private var bufferedImage: BufferedImage? = null
//
//        init {
//            try {
//                bufferedImage = TextureUtil.readBufferedImage(
//                    Minecraft.getMinecraft().getResourceManager().getResource(
//                        this.resourceLocation
//                    ).getInputStream()
//                )
//                this.w = bufferedImage.getWidth()
//                this.h = bufferedImage.getHeight()
//
//                if (label == "The End") {
//                    IMAGE_SCALED_DOWN_FACTOR = this.w / 573f // The original end HD texture is 573 pixels wide.
//                }
//            } catch (ex: IOException) {
//                ex.printStackTrace()
//            }
//
//            this.w = (this.w / IMAGE_SCALED_DOWN_FACTOR).toInt()
//            this.h = (this.h / IMAGE_SCALED_DOWN_FACTOR).toInt()
//
//            if (this.y + this.h > TOTAL_HEIGHT) {
//                TOTAL_HEIGHT = this.y + this.h
//            }
//            if (this.x + this.w > TOTAL_WIDTH) {
//                TOTAL_WIDTH = this.x + this.w
//            }
//        }
//    }
//
//    //TODO: Maybe change these to load from a file at some point
//    
//    enum class Marker(
//        private val warpName: String,
//        private val label: String?,
//        private val island: Island?,
//        private val advanced: Boolean,
//        private val x: Int,
//        private val y: Int
//    ) {
//        PRIVATE_ISLAND("home", Translations.getMessage("warpMenu.home"), Island.PRIVATE_ISLAND, true, 72, 90),
//
//        HUB("hub", Translations.getMessage("warpMenu.spawn"), Island.HUB, true, 600, 200),
//        CASTLE("castle", "Castle", Island.HUB, 130, 80),
//        DARK_AUCTION("da", "Sirius Shack", Island.HUB, 385, 415),
//        CRYPT("crypt", "Crypts", Island.HUB, 550, 100),
//        DUNGEON_HUB("dungeon_hub", "Dungeon Hub", Island.HUB, false, 400, 175),
//        MUSEUM("museum", "Museum", Island.HUB, true, 310, 200),
//
//        SPIDERS_DEN("spider", Translations.getMessage("warpMenu.spawn"), Island.SPIDERS_DEN, true, 345, 240),
//        SPIDERS_DEN_NEST("nest", "Top of Nest", Island.SPIDERS_DEN, 450, 30),
//
//        THE_PARK("park", Translations.getMessage("warpMenu.spawn"), Island.THE_PARK, true, 263, 308),
//        HOWLING_CAVE("howl", "Howling Cave", Island.THE_PARK, 254, 202),
//        THE_PARK_JUNGLE("jungle", "Jungle", Island.THE_PARK, 194, 82),
//
//        THE_END("end", Translations.getMessage("warpMenu.spawn"), Island.THE_END, true, 440, 291),
//        DRAGONS_NEST("drag", "Dragon's Nest", Island.THE_END, 260, 248),
//        VOID_SEPULTURE("void", "Void Sepulture", Island.THE_END, true, 370, 227),
//
//        CRIMSON_ISLE("nether", Translations.getMessage("warpMenu.spawn"), Island.CRIMSON_ISLE, true, 80, 350),
//        FORGOTTEN_SKULL("kuudra", "Forgotten Skull", Island.CRIMSON_ISLE, true, 275, 150),
//        THE_WASTELAND("wasteland", "The Wasteland", Island.CRIMSON_ISLE, true, 275, 180),
//        DRAGONTAIL("dragontail", "Dragontail", Island.CRIMSON_ISLE, true, 60, 200),
//        SCARLETON("scarleton", "Scarleton", Island.CRIMSON_ISLE, true, 400, 180),
//        SMOLDERING_TOMB("smold", "Smoldering Tomb", Island.CRIMSON_ISLE, true, 275, 250),
//
//        THE_BARN("barn", Translations.getMessage("warpMenu.spawn"), Island.THE_BARN, true, 140, 150),
//        MUSHROOM_DESERT("desert", Translations.getMessage("warpMenu.spawn"), Island.MUSHROOM_DESERT, true, 210, 295),
//
//        GOLD_MINE("gold", Translations.getMessage("warpMenu.spawn"), Island.GOLD_MINE, true, 86, 259),
//
//        DEEP_CAVERNS("deep", Translations.getMessage("warpMenu.spawn"), Island.DEEP_CAVERNS, true, 97, 213),
//        DWARVEN_MINES("mines", "Dwarven Mines", Island.DEEP_CAVERNS, false, 280, 205),
//        DWARVEN_FORGE("forge", "Forge", Island.DEEP_CAVERNS, true, 260, 280),
//        CRYSTAL_HOLLOWS("crystals", "Crystal Hollows", Island.DEEP_CAVERNS, true, 220, 350),
//        CRYSTAL_NUCLEUS("nucleus", "Crystal Nucleus", Island.DEEP_CAVERNS, true, 170, 380),
//
//        DUNGEON_HUB_ISLAND("dungeon_hub", Translations.getMessage("warpMenu.spawn"), Island.DUNGEON_HUB, false, 35, 80),
//        ;
//
//        constructor(warpName: String, label: String?, island: Island?, x: Int, y: Int) : this(
//            warpName,
//            label,
//            island,
//            false,
//            x,
//            y
//        )
//
//        companion object {
//            fun fromWarpName(warpName: String): Marker? {
//                for (marker in entries) {
//                    if (marker.warpName == warpName) {
//                        return marker
//                    }
//                }
//
//                return null
//            }
//        }
//    }
//
//    
//    enum class UnlockedStatus(private val message: String) {
//        UNKNOWN(Translations.getMessage("warpMenu.unknown")),
//        NOT_UNLOCKED(Translations.getMessage("warpMenu.notUnlocked")),
//        IN_COMBAT(Translations.getMessage("warpMenu.inCombat")),
//        UNLOCKED(null),
//    }
//
//    companion object {
//        
//        
//        private var doubleWarpMarker: Marker? = null
//
//        private var TOTAL_WIDTH = 0
//        private var TOTAL_HEIGHT = 0
//
//        var SHIFT_LEFT: Float = 0f
//        var SHIFT_TOP: Float = 0f
//
//        var ISLAND_SCALE: Float = 0f
//
//        var IMAGE_SCALED_DOWN_FACTOR: Float = 0.75f
//    }
//}
