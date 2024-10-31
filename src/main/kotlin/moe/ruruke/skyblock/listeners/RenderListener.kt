package moe.ruruke.skyblock.listeners

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.asm.hooks.FontRendererHook
import moe.ruruke.skyblock.config.NewConfig
import moe.ruruke.skyblock.core.*
import moe.ruruke.skyblock.features.EndstoneProtectorManager
import moe.ruruke.skyblock.features.ItemDiff
import moe.ruruke.skyblock.features.SlayerArmorProgress
import moe.ruruke.skyblock.features.spookyevent.CandyType
import moe.ruruke.skyblock.features.spookyevent.SpookyEventManager
import moe.ruruke.skyblock.gui.buttons.ButtonLocation
import moe.ruruke.skyblock.utils.*
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityCaveSpider
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntitySpider
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.entity.passive.EntityWolf
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.GuiIngameForge
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import java.util.*
import kotlin.math.ceil
import kotlin.math.min


class RenderListener {
    private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
    private var predictHealth = false
    fun isPredictHealth(): Boolean {
        return predictHealth
    }

    fun setPredictHealth(ph: Boolean) {
        predictHealth = ph
    }

    private var predictMana = false
    fun isPredictMana(): Boolean {
        return predictMana
    }

    fun setPredictMana(pm: Boolean) {
        predictMana = pm
    }


    private var updateMessageDisplayed = false
    fun setUpdateMessageDisplayed(boolean: Boolean){
        updateMessageDisplayed =boolean
    }
    fun getUpdateMessageDisplayed(): Boolean {
        return updateMessageDisplayed
    }

    private var subtitleFeature: Feature? = null
    fun getSubtitleFeature(): Feature? {
        return subtitleFeature
    }


    private var titleFeature: Feature? = null
    fun getTitleFeature(): Feature? {
        return titleFeature
    }
    fun setTitleFeature(feature: Feature?){
        titleFeature = feature
    }


    private var arrowsLeft = 0
    fun setArrowsLeft(v: Int){
        arrowsLeft = v
    }
    fun getArrowsLeft(): Int {
        return arrowsLeft
    }

    private val cannotReachMobName: String? = null
    fun getCannotReachMobName(): String {
        return cannotReachMobName!!
    }

    private val skillFadeOutTime: Long = -1
    fun getSkillFadeOutTime(): Long {
        return skillFadeOutTime
    }

    private val skill: SkillType? = null
    fun getSkill(): SkillType {
        return skill!!
    }

    private val skillText: String? = null
    fun getSkillText(): String {
        return skillText!!
    }

    private var guiToOpen: EnumUtils.GUIType? = null
    private var guiPageToOpen = 1
    private var guiTabToOpen: EnumUtils.GuiTab = EnumUtils.GuiTab.MAIN
    private var guiFeatureToOpen: Feature? = null

    /**
     * Render overlays and warnings for clients without labymod.
     */
    @SubscribeEvent
    fun onRenderRegular(e: RenderGameOverlayEvent.Post) {
        //TODO: very hacky way to accomplish update every frame. Fix in feature refactor?
        val mc: Minecraft = Minecraft.getMinecraft()
        if (mc != null) {
            val p: EntityPlayerSP = mc.thePlayer
            if (p != null && main.configValues!!.isEnabled(Feature.HEALTH_PREDICTION)
            ) { //Reverse calculate the player's health by using the player's vanilla hearts. Also calculate the health change for the gui item.
                val newHealth = if (getAttribute(Attribute.HEALTH) > getAttribute(Attribute.MAX_HEALTH)) getAttribute(
                    Attribute.HEALTH
                ) else Math.round(getAttribute(Attribute.MAX_HEALTH) * ((p.getHealth()) / p.getMaxHealth())).toFloat()
                main.utils!!.getAttributes().get(Attribute.HEALTH)!!.setValue(newHealth)
            }
        }

        if ((!main.isUsingLabyMod() || Minecraft.getMinecraft().ingameGUI is GuiIngameForge)) {
            if (e.type == RenderGameOverlayEvent.ElementType.EXPERIENCE || e.type == RenderGameOverlayEvent.ElementType.JUMPBAR) {
                if (main.utils!!.isOnSkyblock()) {
                    renderOverlays()
                    renderWarnings(e.resolution)
                } else {
                    renderTimersOnly()
                }
                //TODO:
//                drawUpdateMessage()
            }
        }
    }
//
//    /**
//     * Render overlays and warnings for clients with labymod.
//     * Labymod creates its own ingame gui and replaces the forge one, and changes the events that are called.
//     * This is why the above method can't work for both.
//     */
//    @SubscribeEvent
//    fun onRenderLabyMod(e: RenderGameOverlayEvent) {
//        if (e.type == null && main.isUsingLabymod()) {
//            if (main.getUtils().isOnSkyblock()) {
//                renderOverlays()
//                renderWarnings(e.resolution)
//            } else {
//                renderTimersOnly()
//            }
//            drawUpdateMessage()
//        }
//    }
//
//    @SubscribeEvent
//    fun onRenderLiving(e: RenderLivingEvent.Specials.Pre<EntityLivingBase?>) {
//        val entity: Entity = e.entity
//        if (entity.hasCustomName()) {
//            if (main.configValues!!.isEnabled(Feature.MINION_DISABLE_LOCATION_WARNING)) {
//                if (entity.customNameTag.startsWith("§cThis location isn't perfect! :(")) {
//                    e.setCanceled(true)
//                }
//                if (entity.customNameTag.startsWith("§c/!\\")) {
//                    for (listEntity in Minecraft.getMinecraft().theWorld.loadedEntityList) {
//                        if (listEntity.hasCustomName() && listEntity.customNameTag.startsWith("§cThis location isn't perfect! :(") && listEntity.posX == entity.posX && listEntity.posZ == entity.posZ && listEntity.posY + 0.375 == entity.posY) {
//                            e.setCanceled(true)
//                            break
//                        }
//                    }
//                }
//            }
//
//            if (main.configValues!!.isEnabled(Feature.HIDE_SVEN_PUP_NAMETAGS)) {
//                if (entity is EntityArmorStand && entity.hasCustomName() && entity.customNameTag.contains("Sven Pup")) {
//                    e.setCanceled(true)
//                }
//            }
//        }
//    }
//
    /**
     * I have an option so you can see dark auction timer and farm event timer in other games so that's why.
     */
    private fun renderTimersOnly() {
        val mc: Minecraft = Minecraft.getMinecraft()
        //TODO:
//        if (mc.currentScreen !is LocationEditGui && mc.currentScreen !is GuiNotification) {
            GlStateManager.disableBlend()
            if (NewConfig.isEnabled(Feature.DARK_AUCTION_TIMER) && NewConfig.isEnabled(Feature.SHOW_DARK_AUCTION_TIMER_IN_OTHER_GAMES)
            ) {
                val scale: Float = main.configValues!!.getGuiScale(Feature.DARK_AUCTION_TIMER)
                GlStateManager.pushMatrix()
                GlStateManager.scale(scale, scale, 1f)
                drawText(Feature.DARK_AUCTION_TIMER, scale, mc, null)
                GlStateManager.popMatrix()
            }
            if (main.configValues!!.isEnabled(Feature.FARM_EVENT_TIMER) && main.configValues!!
                    .isEnabled(Feature.SHOW_FARM_EVENT_TIMER_IN_OTHER_GAMES)
            ) {
                val scale: Float = main.configValues!!.getGuiScale(Feature.FARM_EVENT_TIMER)
                GlStateManager.pushMatrix()
                GlStateManager.scale(scale, scale, 1f)
                drawText(Feature.FARM_EVENT_TIMER, scale, mc, null)
                GlStateManager.popMatrix()
            }
//        }
    }

    /**
     * This renders all the title/subtitle warnings from features.
     */
    private fun renderWarnings(scaledResolution: ScaledResolution) {
        val mc: Minecraft = Minecraft.getMinecraft()
        if (mc.theWorld == null || mc.thePlayer == null || !main.utils!!.isOnSkyblock()) {
            return
        }

        val scaledWidth: Int = scaledResolution.getScaledWidth()
        val scaledHeight: Int = scaledResolution.getScaledHeight()
        if (titleFeature != null) {
            var translationKey: String? = null
            when (titleFeature) {
                Feature.FULL_INVENTORY_WARNING -> translationKey = "messages.fullInventory"
                Feature.SUMMONING_EYE_ALERT -> translationKey = "messages.summoningEyeFound"
                Feature.SPECIAL_ZEALOT_ALERT -> translationKey = "messages.specialZealotFound"
                Feature.LEGENDARY_SEA_CREATURE_WARNING -> translationKey = "messages.legendarySeaCreatureWarning"
                Feature.BOSS_APPROACH_ALERT -> translationKey = "messages.bossApproaching"
                Feature.WARN_WHEN_FETCHUR_CHANGES -> translationKey = "messages.fetchurWarning"
                Feature.BROOD_MOTHER_ALERT -> translationKey = "messages.broodMotherWarning"
                Feature.BAL_BOSS_ALERT -> translationKey = "messages.balBossWarning"
                else -> {}
            }
            if (translationKey != null) {
                val text = Translations.getMessage(translationKey)
                val stringWidth: Int = mc.fontRendererObj.getStringWidth(text)

                var scale = 4f // Scale is normally 4, but if its larger than the screen, scale it down...
                if (stringWidth * scale > (scaledWidth * 0.9f)) {
                    scale = (scaledWidth * 0.9f) / stringWidth.toFloat()
                }

                GlStateManager.pushMatrix()
                GlStateManager.translate((scaledWidth / 2).toFloat(), (scaledHeight / 2).toFloat(), 0.0f)
                GlStateManager.enableBlend()
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
                GlStateManager.pushMatrix()
                GlStateManager.scale(scale, scale, scale) // TODO Check if changing this scale breaks anything...

                FontRendererHook.setupFeatureFont(titleFeature)
                DrawUtils.drawText(
                    text,
                    (-mc.fontRendererObj.getStringWidth(text) / 2).toFloat(),
                    -20.0f,
                    main.configValues!!.getColor(titleFeature!!)
                )
                FontRendererHook.endFeatureFont()

                GlStateManager.popMatrix()
                GlStateManager.popMatrix()
            }
        }
        if (subtitleFeature != null) {
            var text: String? = null
            when (subtitleFeature) {
                Feature.MINION_STOP_WARNING -> text =
                    Translations.getMessage("messages.minionCannotReach", cannotReachMobName)

                Feature.MINION_FULL_WARNING -> text = Translations.getMessage("messages.minionIsFull")
                Feature.NO_ARROWS_LEFT_ALERT -> if (arrowsLeft != -1) {
                    Translations.getMessage(
                        "messages.noArrowsLeft",
                        TextUtils.NUMBER_FORMAT.format(arrowsLeft.toLong())
                    )
                }

                else -> {}
            }

            if (text != null) {
                val stringWidth: Int = mc.fontRendererObj.getStringWidth(text)

                var scale = 2f // Scale is normally 2, but if it's larger than the screen, scale it down...
                if (stringWidth * scale > (scaledWidth * 0.9f)) {
                    scale = (scaledWidth * 0.9f) / stringWidth.toFloat()
                }

                GlStateManager.pushMatrix()
                GlStateManager.translate((scaledWidth / 2).toFloat(), (scaledHeight / 2).toFloat(), 0.0f)
                GlStateManager.enableBlend()
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
                GlStateManager.pushMatrix()
                GlStateManager.scale(scale, scale, scale) // TODO Check if changing this scale breaks anything...

                FontRendererHook.setupFeatureFont(subtitleFeature)
                DrawUtils.drawText(
                    text,
                    -mc.fontRendererObj.getStringWidth(text) / 2f,
                    -23.0f,
                    main.configValues!!.getColor(subtitleFeature!!)
                )
                FontRendererHook.endFeatureFont()

                GlStateManager.popMatrix()
                GlStateManager.popMatrix()
            }
        }
    }
//
    /**
     * This renders all the gui elements (bars, icons, texts, skeleton bar, etc.).
     */
    private fun renderOverlays() {
        val mc: Minecraft = Minecraft.getMinecraft()
        //TODO:
//        if (mc.currentScreen !is LocationEditGui && mc.currentScreen !is GuiNotification) {
            GlStateManager.disableBlend()

            for (feature in Feature.guiFeatures) {
                if (NewConfig.isEnabled(feature)) {
                    if (feature == Feature.SKELETON_BAR && !main.inventoryUtils!!.isWearingSkeletonHelmet()) continue
                    if (feature == Feature.HEALTH_UPDATES && main.getPlayerListener().getHealthUpdate() == null
                    ) continue

                    val scale: Float = main.configValues!!.getGuiScale(feature)
                    GlStateManager.pushMatrix()
                    GlStateManager.scale(scale, scale, 1f)
                    feature.draw(scale, mc, null)
                    GlStateManager.popMatrix()
                }
            }
        }
//    }
//
//    /**
//     * This draws all Skyblock Addons Bars, including the Health, Mana, Drill, and Skill XP bars
//     *
//     * @param feature        for which to render the bars
//     * @param scale          the scale of the feature
//     * @param mc             link to the minecraft session
//     * @param buttonLocation the resizing gui, if present
//     */
//    fun drawBar(feature: Feature, scale: Float, mc: Minecraft, buttonLocation: ButtonLocation?) {
//        // The fill of the bar from 0 to 1
//        var fill: Float
//        // Whether the player has absorption hearts
//        var hasAbsorption = false
//        if (feature == Feature.MANA_BAR) {
//            fill = getAttribute(Attribute.MANA) / getAttribute(Attribute.MAX_MANA)
//        } else if (feature == Feature.DRILL_FUEL_BAR) {
//            fill = getAttribute(Attribute.FUEL) / getAttribute(Attribute.MAX_FUEL)
//        } else if (feature == Feature.SKILL_PROGRESS_BAR) {
//            val parser: ActionBarParser = main.getPlayerListener().getActionBarParser()
//            fill = if (buttonLocation == null) {
//                if (parser.getPercent() === 0 || parser.getPercent() === 100) {
//                    return
//                } else {
//                    parser.getPercent() / 100
//                }
//            } else {
//                0.40f
//            }
//        } else {
//            fill = getAttribute(Attribute.HEALTH) / getAttribute(Attribute.MAX_HEALTH)
//        }
//        if (fill > 1) fill = 1f
//
//        var x: Float = main.configValues!!.getActualX(feature)
//        var y: Float = main.configValues!!.getActualY(feature)
//        val scaleX: Float = main.configValues!!.getSizesX(feature)
//        val scaleY: Float = main.configValues!!.getSizesY(feature)
//        GlStateManager.scale(scaleX, scaleY, 1f)
//
//        x = transformXY(x, 71, scale * scaleX)
//        y = transformXY(y, 5, scale * scaleY)
//
//        // Render the button resize box if necessary
//        if (buttonLocation != null) {
//            buttonLocation.checkHoveredAndDrawBox(x, x + 71, y, y + 5, scale, scaleX, scaleY)
//        }
//
//        var color: SkyblockColor = ColorUtils.getDummySkyblockColor(
//            main.configValues!!.getColor(feature),
//            main.configValues!!.getChromaFeatures().contains(feature)
//        )
//
//        if (feature == Feature.SKILL_PROGRESS_BAR && buttonLocation == null) {
//            val remainingTime = (skillFadeOutTime - System.currentTimeMillis()).toInt()
//
//            if (remainingTime < 0) {
//                if (remainingTime < -2000) {
//                    return  // Will be invisible, no need to render.
//                }
//
//                val textAlpha = Math.round(255 - (-remainingTime / 2000f * 255f))
//                color = ColorUtils.getDummySkyblockColor(
//                    main.configValues!!.getColor(feature, textAlpha),
//                    main.configValues!!.getChromaFeatures().contains(feature)
//                ) // so it fades out, 0.016 is the minimum alpha
//            }
//        }
//
//        if (feature == Feature.DRILL_FUEL_BAR && buttonLocation == null && !ItemUtils.isDrill(mc.thePlayer.getHeldItem())) {
//            return
//        }
//
//        if (feature == Feature.HEALTH_BAR && main.configValues!!.isEnabled(Feature.CHANGE_BAR_COLOR_FOR_POTIONS)) {
//            if (mc.thePlayer.isPotionActive(19 /* Poison */)) {
//                color = ColorUtils.getDummySkyblockColor(
//                    ColorCode.DARK_GREEN.getColor(),
//                    main.configValues!!.getChromaFeatures().contains(feature)
//                )
//            } else if (mc.thePlayer.isPotionActive(20 /* Wither */)) {
//                color = ColorUtils.getDummySkyblockColor(
//                    ColorCode.DARK_GRAY.getColor(),
//                    main.configValues!!.getChromaFeatures().contains(feature)
//                )
//            } else if (mc.thePlayer.isPotionActive(22) /* Absorption */) {
//                if (getAttribute(Attribute.HEALTH) > getAttribute(Attribute.MAX_HEALTH)) {
//                    fill = getAttribute(Attribute.MAX_HEALTH) / getAttribute(Attribute.HEALTH)
//                    hasAbsorption = true
//                }
//            }
//        }
//
//        main.getUtils().enableStandardGLOptions()
//        // Draw the actual bar
//        drawMultiLayeredBar(mc, color, x, y, fill, hasAbsorption)
//
//        main.getUtils().restoreGLOptions()
//    }
//
//    /**
//     * Draws a multitextured bar:
//     * Begins by coloring and rendering the empty bar.
//     * Then, colors and renders the full bar up to the fraction {@param fill}.
//     * Then, overlays the absorption portion of the bar in gold if the player has absorption hearts
//     * Then, overlays (and does not color) an additional texture centered on the current progress of the bar.
//     * Then, overlays (and does not color) a final style texture over the bar
//     * @param mc link to the current minecraft session
//     * @param color the color with which to render the bar
//     * @param x the x position of the bar
//     * @param y the y position of the bar
//     * @param fill the fraction (from 0 to 1) of the bar that's full
//     * @param hasAbsorption `true` if the player has absorption hearts
//     */
//    private fun drawMultiLayeredBar(
//        mc: Minecraft,
//        color: SkyblockColor,
//        x: Float,
//        y: Float,
//        fill: Float,
//        hasAbsorption: Boolean
//    ) {
//        val barHeight = 5
//        val barWidth = 71
//        val barFill = barWidth * fill
//        mc.getTextureManager().bindTexture(BARS)
//        if (color.color == ColorCode.BLACK.getColor()) {
//            GlStateManager.color(0.25f, 0.25f, 0.25f, ColorUtils.getAlpha(color.color) / 255f) // too dark normally
//        } else { // A little darker for contrast...
//            ColorUtils.bindColor(color.color, 0.9f)
//        }
//        // If chroma, draw the empty bar much darker than the filled bar
//        if (color.drawMulticolorUsingShader()) {
//            GlStateManager.color(.5f, .5f, .5f)
//            ShaderManager.getInstance().enableShader(ChromaScreenTexturedShader::class.java)
//        }
//        // Empty bar first
//        DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 1, 1, barWidth, barHeight, 80, 50)
//
//        if (color.drawMulticolorUsingShader()) {
//            ColorUtils.bindWhite()
//            ShaderManager.getInstance().enableShader(ChromaScreenTexturedShader::class.java)
//        }
//
//        // Filled bar next
//        if (fill != 0f) {
//            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 1, 7, barFill, barHeight, 80, 50)
//        }
//        // Disable coloring
//        if (color.drawMulticolorUsingShader()) {
//            ShaderManager.getInstance().disableShader()
//        }
//
//        // Overlay absorption health if needed
//        if (hasAbsorption) {
//            ColorUtils.bindColor(ColorCode.GOLD.getColor())
//            DrawUtils.drawModalRectWithCustomSizedTexture(
//                x + barFill,
//                y,
//                barFill + 1,
//                7,
//                barWidth - barFill,
//                barHeight,
//                80,
//                50
//            )
//        }
//        ColorUtils.bindWhite()
//
//        // Overlay uncolored progress indicator next (texture packs can use this to overlay their own static bar colors)
//        if (fill > 0 && fill < 1) {
//            // Make sure that the overlay doesn't go outside the bounds of the bar.
//            // It's 4 pixels wide, so ensure we only render the texture between 0 <= x <= barWidth
//            // Start rendering at x => 0 (for small fill values, also don't render before the bar starts)
//            // Adding padding ensures that no green bar gets rendered from the texture...?
//            val padding = .01f
//            val oneSide = 2 - padding
//            val startX = max(0.0, (barFill - oneSide).toDouble()).toFloat()
//            // Start texture at x >= 0 (for small fill values, also start the texture so indicator is always centered)
//            val startTexX = max(padding.toDouble(), (oneSide - barFill).toDouble()).toFloat()
//            // End texture at x <= barWidth and 4 <= startTexX + endTexX (total width of overlay texture). Cut off for large fill values.
//            val endTexX =
//                min((2 * oneSide - startTexX).toDouble(), (barWidth - barFill + oneSide).toDouble()).toFloat()
//            DrawUtils.drawModalRectWithCustomSizedTexture(x + startX, y, 1 + startTexX, 24, endTexX, barHeight, 80, 50)
//        }
//        // Overlay uncolored bar display next (texture packs can use this to overlay their own static bar colors)
//        DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 1, 13, barWidth, barHeight, 80, 50)
//    }
//
//    /**
//     * Renders the messages from the SkyblockAddons Updater
//     */
//    private fun drawUpdateMessage() {
//        val updater: Updater = main.getUpdater()
//        val message: String = updater.getMessageToRender()
//
//        if (updater.hasUpdate() && message != null && !updateMessageDisplayed) {
//            val mc: Minecraft = Minecraft.getMinecraft()
//            val textList: Array<String> = main.getUtils().wrapSplitText(message, 36)
//
//            val halfWidth: Int = ScaledResolution(mc).getScaledWidth() / 2
//            Gui.drawRect(
//                halfWidth - 110,
//                20,
//                halfWidth + 110,
//                53 + textList.size * 10,
//                main.getUtils().getDefaultBlue(140)
//            )
//            val title: String = SkyblockAddons.MOD_NAME
//            GlStateManager.pushMatrix()
//            val scale = 1.5f
//            GlStateManager.scale(scale, scale, 1f)
//            DrawUtils.drawCenteredText(
//                title,
//                (halfWidth / scale).toInt(),
//                (30 / scale).toInt(),
//                ColorCode.WHITE.getColor()
//            )
//            GlStateManager.popMatrix()
//            var y = 45
//            for (line in textList) {
//                DrawUtils.drawCenteredText(line, halfWidth, y, ColorCode.WHITE.getColor())
//                y += 10
//            }
//
//            main.getScheduler().schedule(Scheduler.CommandType.ERASE_UPDATE_MESSAGE, 10)
//
//            if (!main.getUpdater().hasSentUpdateMessage()) {
//                main.getUpdater().sendUpdateMessage()
//            }
//        }
//    }
//
//    /**
//     * This renders a bar for the skeleton hat bones bar.
//     */
//    fun drawSkeletonBar(mc: Minecraft, scale: Float, buttonLocation: ButtonLocation?) {
//        var x: Float = main.configValues!!.getActualX(Feature.SKELETON_BAR)
//        var y: Float = main.configValues!!.getActualY(Feature.SKELETON_BAR)
//        var bones = 0
//        if (mc.currentScreen !is LocationEditGui) {
//            for (listEntity in mc.theWorld.loadedEntityList) {
//                if (listEntity is EntityItem &&
//                    listEntity.ridingEntity is EntityArmorStand && listEntity.ridingEntity.isInvisible() && listEntity.getDistanceToEntity(
//                        mc.thePlayer
//                    ) <= 8
//                ) {
//                    bones++
//                }
//            }
//        } else {
//            bones = 3
//        }
//        if (bones > 3) bones = 3
//
//        val height = 16
//        val width = 3 * 16
//
//        x = transformXY(x, width, scale)
//        y = transformXY(y, height, scale)
//
//        if (buttonLocation != null) {
//            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale)
//            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
//        }
//
//        main.getUtils().enableStandardGLOptions()
//
//        for (boneCounter in 0 until bones) {
//            renderItem(BONE_ITEM, x + boneCounter * 16, y)
//        }
//
//        main.getUtils().restoreGLOptions()
//    }
//
//    /**
//     * This renders the skeleton bar.
//     */
//    fun drawScorpionFoilTicker(mc: Minecraft, scale: Float, buttonLocation: ButtonLocation?) {
//        if (buttonLocation != null || main.getPlayerListener().getTickers() !== -1) {
//            var x: Float = main.configValues!!.getActualX(Feature.TICKER_CHARGES_DISPLAY)
//            var y: Float = main.configValues!!.getActualY(Feature.TICKER_CHARGES_DISPLAY)
//
//            val height = 9
//            val width = 3 * 11 + 9
//
//            x = transformXY(x, width, scale)
//            y = transformXY(y, height, scale)
//
//            if (buttonLocation != null) {
//                buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale)
//            }
//
//            main.getUtils().enableStandardGLOptions()
//
//            val maxTickers = if ((buttonLocation == null)) main.getPlayerListener().getMaxTickers() else 4
//            for (tickers in 0 until maxTickers) {
//                mc.getTextureManager().bindTexture(TICKER_SYMBOL)
//                GlStateManager.enableAlpha()
//                if (tickers < (if (buttonLocation == null) main.getPlayerListener().getTickers() else 3)) {
//                    DrawUtils.drawModalRectWithCustomSizedTexture(x + tickers * 11, y, 0, 0, 9, 9, 18, 9, false)
//                } else {
//                    DrawUtils.drawModalRectWithCustomSizedTexture(x + tickers * 11, y, 9, 0, 9, 9, 18, 9, false)
//                }
//            }
//
//            main.getUtils().restoreGLOptions()
//        }
//    }
//
//    /**
//     * This renders the defence icon.
//     */
//    fun drawIcon(scale: Float, mc: Minecraft, buttonLocation: ButtonLocation?) {
//        if (main.configValues!!.isDisabled(Feature.USE_VANILLA_TEXTURE_DEFENCE)) {
//            mc.getTextureManager().bindTexture(Gui.icons)
//        } else {
//            mc.getTextureManager().bindTexture(DEFENCE_VANILLA)
//        }
//        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
//        // The height and width of this element (box not included)
//        val height = 9
//        val width = 9
//        var x: Float = main.configValues!!.getActualX(Feature.DEFENCE_ICON)
//        var y: Float = main.configValues!!.getActualY(Feature.DEFENCE_ICON)
//        x = main.getRenderListener().transformXY(x, width, scale)
//        y = main.getRenderListener().transformXY(y, height, scale)
//
//        main.getUtils().enableStandardGLOptions()
//
//        if (buttonLocation == null) {
//            mc.ingameGUI.drawTexturedModalRect(x, y, 34, 9, width, height)
//        } else {
//            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale)
//            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
//            buttonLocation.drawTexturedModalRect(x, y, 34, 9, width, height)
//        }
//
//        main.getUtils().restoreGLOptions()
//    }
//
    /**
     * This renders all the different types gui text elements.
     */
    fun drawText(feature: Feature, scale: Float, mc: Minecraft, buttonLocation: ButtonLocation?) {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        var text: String = ""
        var color: Int = main.configValues!!.getColor(feature)
        when (feature) {
//            Feature.MANA_TEXT -> {
//                text = NUMBER_FORMAT.format(getAttribute(Attribute.MANA)) + "/" + NUMBER_FORMAT.format(
//                    getAttribute(
//                        Attribute.MAX_MANA
//                    )
//                )
//            }
//            Feature.OVERFLOW_MANA -> {
//                if (getAttribute(Attribute.OVERFLOW_MANA) != 0f || buttonLocation != null) {
//                    text = getAttribute(Attribute.OVERFLOW_MANA).toString() + "ʬ"
//                } else {
//                    return
//                }
//            }
//            Feature.HEALTH_TEXT -> {
//                text = NUMBER_FORMAT.format(getAttribute(Attribute.HEALTH)) + "/" + NUMBER_FORMAT.format(
//                    getAttribute(
//                        Attribute.MAX_HEALTH
//                    )
//                )
//            }
//            Feature.CRIMSON_ARMOR_ABILITY_STACKS -> {
//                text = crimsonArmorAbilityStacks
//                if (text == null) return
//            }
//            Feature.DEFENCE_TEXT -> {
//                text = NUMBER_FORMAT.format(getAttribute(Attribute.DEFENCE))
//            }
//            Feature.OTHER_DEFENCE_STATS -> {
//                text = main.getPlayerListener().getActionBarParser().getOtherDefense()
//                if (buttonLocation != null && (text == null || text.length == 0)) {
//                    text = "|||  T3!"
//                }
//                if (text == null || text.length == 0) {
//                    return
//                }
//            }
//            Feature.EFFECTIVE_HEALTH_TEXT -> {
//                text =
//                    NUMBER_FORMAT.format(Math.round(getAttribute(Attribute.HEALTH) * (1 + getAttribute(Attribute.DEFENCE) / 100f)))
//            }
//            Feature.DRILL_FUEL_TEXT -> {
//                if (!ItemUtils.isDrill(mc.thePlayer.getHeldItem())) {
//                    return
//                }
//                text = (getAttribute(Attribute.FUEL).toString() + "/" + getAttribute(Attribute.MAX_FUEL)).replace(
//                    "000$".toRegex(),
//                    "k"
//                )
//            }
//            Feature.DEFENCE_PERCENTAGE -> {
//                val doubleDefence = getAttribute(Attribute.DEFENCE).toDouble()
//                val percentage =
//                    ((doubleDefence / 100) / ((doubleDefence / 100) + 1)) * 100 //Taken from https://hypixel.net/threads/how-armor-works-and-the-diminishing-return-of-higher-defence.2178928/
//                val bigDecimal = BigDecimal(percentage).setScale(1, RoundingMode.HALF_UP)
//                text = "$bigDecimal%"
//            }
//            Feature.SPEED_PERCENTAGE -> {
//                val walkSpeed: String =
//                    NUMBER_FORMAT.format(Minecraft.getMinecraft().thePlayer.capabilities.getWalkSpeed() * 1000)
//                text = walkSpeed.substring(0, min(walkSpeed.length.toDouble(), 3.0).toInt())
//
//                if (text.endsWith(".")) text = text.substring(0, text.indexOf('.')) //remove trailing periods
//
//
//                text += "%"
//            }
//            Feature.HEALTH_UPDATES -> {
//                val healthUpdate: Float = main.getPlayerListener().getHealthUpdate()
//                if (buttonLocation == null) {
//                    if (healthUpdate != null) {
//                        color = if (healthUpdate > 0) ColorCode.GREEN.getColor() else ColorCode.RED.getColor()
//                        text = (if (healthUpdate > 0) "+" else "-") + NUMBER_FORMAT.format(abs(healthUpdate.toDouble()))
//                    } else {
//                        return
//                    }
//                } else {
//                    text = "+123"
//                    color = ColorCode.GREEN.getColor()
//                }
//            }
//            Feature.DARK_AUCTION_TIMER -> { // The timezone of the server, to avoid problems with like timezones that are 30 minutes ahead or whatnot.
//                val nextDarkAuction = Calendar.getInstance(TimeZone.getTimeZone("EST"))
//                if (nextDarkAuction[Calendar.MINUTE] >= 55) {
//                    nextDarkAuction.add(Calendar.HOUR_OF_DAY, 1)
//                }
//                nextDarkAuction[Calendar.MINUTE] = 55
//                nextDarkAuction[Calendar.SECOND] = 0
//                val difference = (nextDarkAuction.timeInMillis - System.currentTimeMillis()).toInt()
//                val minutes = difference / 60000
//                val seconds = Math.round((difference % 60000).toDouble() / 1000).toInt()
//                val timestamp = StringBuilder()
//                if (minutes < 10) {
//                    timestamp.append("0")
//                }
//                timestamp.append(minutes).append(":")
//                if (seconds < 10) {
//                    timestamp.append("0")
//                }
//                timestamp.append(seconds)
//                text = timestamp.toString()
//            }
//            Feature.FARM_EVENT_TIMER -> { // The timezone of the server, to avoid problems with like timezones that are 30 minutes ahead or whatnot.
//                val nextFarmEvent = Calendar.getInstance(TimeZone.getTimeZone("EST"))
//                if (nextFarmEvent[Calendar.MINUTE] >= 15) {
//                    nextFarmEvent.add(Calendar.HOUR_OF_DAY, 1)
//                }
//                nextFarmEvent[Calendar.MINUTE] = 15
//                nextFarmEvent[Calendar.SECOND] = 0
//                val difference = (nextFarmEvent.timeInMillis - System.currentTimeMillis()).toInt()
//                val minutes = difference / 60000
//                val seconds = Math.round((difference % 60000).toDouble() / 1000).toInt()
//                if (minutes < 40) {
//                    val timestamp = StringBuilder()
//                    if (minutes < 10) {
//                        timestamp.append("0")
//                    }
//                    timestamp.append(minutes).append(":")
//                    if (seconds < 10) {
//                        timestamp.append("0")
//                    }
//                    timestamp.append(seconds)
//                    text = timestamp.toString()
//                } else {
//                    val timestampActive = StringBuilder()
//                    timestampActive.append("Active: ")
//                    if (minutes - 40 < 10) {
//                        timestampActive.append("0")
//                    }
//                    timestampActive.append(minutes - 40).append(":")
//                    if (seconds < 10) {
//                        timestampActive.append("0")
//                    }
//                    timestampActive.append(seconds)
//                    text = timestampActive.toString()
//                }
//            }
//            Feature.SKILL_DISPLAY -> {
//                if (buttonLocation == null) {
//                    text = skillText
//                    if (text == null) return
//                } else {
//                    val previewBuilder = StringBuilder()
//                    if (main.configValues!!.isEnabled(Feature.SHOW_SKILL_XP_GAINED)) {
//                        previewBuilder.append("+123 ")
//                    }
//                    if (main.configValues!!.isEnabled(Feature.SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP)) {
//                        previewBuilder.append("40% ")
//                    } else {
//                        previewBuilder.append("(2000/5000) ")
//                    }
//                    if (main.configValues!!.isEnabled(Feature.SKILL_ACTIONS_LEFT_UNTIL_NEXT_LEVEL)) {
//                        previewBuilder.append(" - ").append(Translations.getMessage("messages.actionsLeft", 3000))
//                            .append(" ")
//                    }
//                    previewBuilder.setLength(previewBuilder.length - 1)
//                    text = previewBuilder.toString()
//                }
//                if (buttonLocation == null) {
//                    val remainingTime = (skillFadeOutTime - System.currentTimeMillis()).toInt()
//
//                    if (remainingTime < 0) {
//                        if (remainingTime < -1968) {
//                            return  // Will be invisible, no need to render.
//                        }
//
//                        val textAlpha = Math.round(255 - (-remainingTime / 2000f * 255f))
//                        color = main.configValues!!
//                            .getColor(feature, textAlpha) // so it fades out, 0.016 is the minimum alpha
//                    }
//                }
//            }
//            Feature.ZEALOT_COUNTER -> {
//                if (main.configValues!!.isEnabled(Feature.ZEALOT_COUNTER_ZEALOT_SPAWN_AREAS_ONLY) &&
//                    !LocationUtils.isZealotSpawnLocation(
//                        main.getUtils().getLocation().getScoreboardName()
//                    ) && buttonLocation == null
//                ) {
//                    return
//                }
//                text = java.lang.String.valueOf(main.getPersistentValuesManager().getPersistentValues().getKills())
//            }
//            Feature.SHOW_TOTAL_ZEALOT_COUNT -> {
//                if (main.configValues!!.isEnabled(Feature.SHOW_TOTAL_ZEALOT_COUNT_ZEALOT_SPAWN_AREAS_ONLY) &&
//                    !LocationUtils.isZealotSpawnLocation(
//                        main.getUtils().getLocation().getScoreboardName()
//                    ) && buttonLocation == null
//                ) {
//                    return
//                }
//                if (main.getPersistentValuesManager().getPersistentValues().getTotalKills() <= 0) {
//                    text = java.lang.String.valueOf(main.getPersistentValuesManager().getPersistentValues().getKills())
//                } else {
//                    text = java.lang.String.valueOf(
//                        main.getPersistentValuesManager().getPersistentValues()
//                            .getTotalKills() + main.getPersistentValuesManager().getPersistentValues().getKills()
//                    )
//                }
//            }
//            Feature.SHOW_SUMMONING_EYE_COUNT -> {
//                if (main.configValues!!.isEnabled(Feature.SHOW_SUMMONING_EYE_COUNT_ZEALOT_SPAWN_AREAS_ONLY) &&
//                    !LocationUtils.isZealotSpawnLocation(
//                        main.getUtils().getLocation().getScoreboardName()
//                    ) && buttonLocation == null
//                ) {
//                    return
//                }
//                text =
//                    java.lang.String.valueOf(main.getPersistentValuesManager().getPersistentValues().getSummoningEyeCount())
//            }
//            Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE -> {
//                if (main.configValues!!.isEnabled(Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE_ZEALOT_SPAWN_AREAS_ONLY) &&
//                    !LocationUtils.isZealotSpawnLocation(
//                        main.getUtils().getLocation().getScoreboardName()
//                    ) && buttonLocation == null
//                ) {
//                    return
//                }
//                val summoningEyeCount: Int = main.getPersistentValuesManager().getPersistentValues().getSummoningEyeCount()
//
//                text = if (summoningEyeCount > 0) {
//                    java.lang.String.valueOf(
//                        Math.round(
//                            main.getPersistentValuesManager().getPersistentValues()
//                                .getTotalKills() / main.getPersistentValuesManager().getPersistentValues()
//                                .getSummoningEyeCount() as Double
//                        )
//                    )
//                } else {
//                    "0" // Avoid zero division.
//                }
//            }
//            Feature.BIRCH_PARK_RAINMAKER_TIMER -> {
//                val rainmakerTime: Long = main.getPlayerListener().getRainmakerTimeEnd()
//
//                if ((main.getUtils()
//                        .getLocation() !== Location.BIRCH_PARK || rainmakerTime == -1L) && buttonLocation == null
//                ) {
//                    return
//                }
//
//                val totalSeconds = (rainmakerTime - System.currentTimeMillis()).toInt() / 1000
//
//                if (TabListParser.getParsedRainTime() != null) {
//                    text = TabListParser.getParsedRainTime()
//                } else if (rainmakerTime != -1L && totalSeconds > 0) {
//                    val timerBuilder = StringBuilder()
//
//                    val hours = totalSeconds / 3600
//                    val minutes = totalSeconds / 60 % 60
//                    val seconds = totalSeconds % 60
//
//                    if (hours > 0) {
//                        timerBuilder.append(hours).append(":")
//                    }
//                    if (minutes < 10 && hours > 0) {
//                        timerBuilder.append("0")
//                    }
//                    timerBuilder.append(minutes).append(":")
//                    if (seconds < 10) {
//                        timerBuilder.append("0")
//                    }
//                    timerBuilder.append(seconds)
//
//                    text = timerBuilder.toString()
//                } else {
//                    if (buttonLocation == null) {
//                        return
//                    }
//
//                    text = "1:23"
//                }
//            }
//            Feature.ENDSTONE_PROTECTOR_DISPLAY -> {
//                if (((main.getUtils().getLocation() !== Location.THE_END && main.getUtils()
//                        .getLocation() !== Location.DRAGONS_NEST)
//                            || EndstoneProtectorManager.getMinibossStage() == null || !EndstoneProtectorManager.isCanDetectSkull()) && buttonLocation == null
//                ) {
//                    return
//                }
//
//                var stage: EndstoneProtectorManager.Stage = EndstoneProtectorManager.getMinibossStage()
//
//                if (buttonLocation != null && stage == null) {
//                    stage = EndstoneProtectorManager.Stage.STAGE_3
//                }
//
//                val stageNum = min(stage.ordinal.toDouble(), 5.0).toInt()
//                text = Translations.getMessage("messages.stage", stageNum.toString())
//            }
//            Feature.SHOW_DUNGEON_MILESTONE -> {
//                if (buttonLocation == null && !main.getUtils().isInDungeon()) {
//                    return
//                }
//
//                var dungeonMilestone: DungeonMilestone? = main.getDungeonManager().getDungeonMilestone()
//                if (dungeonMilestone == null) {
//                    if (buttonLocation != null) {
//                        dungeonMilestone = DungeonMilestone(DungeonClass.HEALER)
//                    } else {
//                        return
//                    }
//                }
//                text = "Milestone " + dungeonMilestone.getLevel()
//            }
//            Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY -> {
//                if (buttonLocation == null && !main.getUtils().isInDungeon()) {
//                    return
//                }
//                text = ""
//            }
//            Feature.DUNGEON_DEATH_COUNTER -> {
//                val deaths: Int = main.getDungeonManager().getDeathCount()
//
//                if (buttonLocation == null) {
//                    if (!main.getUtils().isInDungeon()) {
//                        return
//                    }
//                }
//                text = deaths.toString()
//            }
//            Feature.ROCK_PET_TRACKER -> {
//                text = java.lang.String.valueOf(main.getPersistentValuesManager().getPersistentValues().getOresMined())
//            }
//            Feature.DOLPHIN_PET_TRACKER -> {
//                text = java.lang.String.valueOf(
//                    main.getPersistentValuesManager().getPersistentValues().getSeaCreaturesKilled()
//                )
//            }
//            Feature.DUNGEONS_SECRETS_DISPLAY -> {
//                if (buttonLocation == null && !main.getUtils().isInDungeon()) {
//                    return
//                }
//
//                text = "Secrets"
//            }
//            Feature.SPIRIT_SCEPTRE_DISPLAY -> {
//                val holdingItem: ItemStack = mc.thePlayer.getCurrentEquippedItem()
//                val held: ItemStack = Minecraft.getMinecraft().thePlayer.getHeldItem()
//                val skyblockItemID: String = ItemUtils.getSkyblockItemID(held)
//                text = if (buttonLocation != null) {
//                    "Hyperion"
//                } else if (holdingItem == null || skyblockItemID == null) {
//                    return
//                } else if (skyblockItemID == "HYPERION" || skyblockItemID == "VALKYRIE" || skyblockItemID == "ASTRAEA" || skyblockItemID == "SCYLLA" || skyblockItemID == "BAT_WAND") {
//                    holdingItem.getDisplayName().replace("§[a-f0-9]?✪".toRegex(), "")
//                } else {
//                    return
//                }
//            }
//            Feature.CANDY_POINTS_COUNTER -> {
//                if (buttonLocation == null && !SpookyEventManager.isActive) {
//                    return
//                }
//
//                text = "Test"
//            }
//            Feature.FETCHUR_TODAY -> {
//                val fetchurItem: FetchurItem = FetchurManager.getInstance().getCurrentFetchurItem()
//                text = if (!FetchurManager.getInstance().hasFetchedToday() || buttonLocation != null) {
//                    if (main.configValues!!.isEnabled(Feature.SHOW_FETCHUR_ITEM_NAME)) {
//                        Translations.getMessage(
//                            "messages.fetchurItem",
//                            fetchurItem.getItemStack().stackSize.toString() + "x " + fetchurItem.getItemText()
//                        )
//                    } else {
//                        Translations.getMessage("messages.fetchurItem", "")
//                    }
//                } else {
//                    "" // If it has made fetchur, then no need for text
//                }
//            }
            else -> {
                main.utils!!.sendErrorMessage("未実装のdrawのTypeを受信 [${feature.name}]")
            }
        }
        var x: Float = main.configValues!!.getActualX(feature)
        var y: Float = main.configValues!!.getActualY(feature)

        var height = 7
        var width: Int = mc.fontRendererObj.getStringWidth(text)

        // Constant width overrides for some features.
        if (feature == Feature.ZEALOT_COUNTER || feature == Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE) {
            width = mc.fontRendererObj.getStringWidth("500")
        } else if (feature == Feature.SHOW_TOTAL_ZEALOT_COUNT) {
            width = mc.fontRendererObj.getStringWidth("30000")
        } else if (feature == Feature.SHOW_SUMMONING_EYE_COUNT) {
            width = mc.fontRendererObj.getStringWidth("100")
        }

        if (feature == Feature.DARK_AUCTION_TIMER || feature == Feature.FARM_EVENT_TIMER || feature == Feature.ZEALOT_COUNTER || feature == Feature.SKILL_DISPLAY || feature == Feature.SHOW_TOTAL_ZEALOT_COUNT || feature == Feature.SHOW_SUMMONING_EYE_COUNT || feature == Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE || feature == Feature.BIRCH_PARK_RAINMAKER_TIMER || feature == Feature.ENDSTONE_PROTECTOR_DISPLAY || feature == Feature.DUNGEON_DEATH_COUNTER || feature == Feature.DOLPHIN_PET_TRACKER || feature == Feature.ROCK_PET_TRACKER) {
            width += 18
            height += 9
        }

        if (feature == Feature.ENDSTONE_PROTECTOR_DISPLAY) {
            width += 2 + 16 + 2 + mc.fontRendererObj.getStringWidth(
                EndstoneProtectorManager.getZealotCount().toString()
            )
        }

        if (feature == Feature.SHOW_DUNGEON_MILESTONE) {
            width += 16 + 2
            height += 10
        }

        if (feature == Feature.DUNGEONS_SECRETS_DISPLAY) {
            width += 16 + 2
            height += 12
        }

        if (feature == Feature.FETCHUR_TODAY) {
            if (main.configValues!!.isDisabled(Feature.SHOW_FETCHUR_ITEM_NAME)) {
                width += 18
                height += 9
            }
        }

        if (feature == Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY) {
            val maxNumberWidth: Int = mc.fontRendererObj.getStringWidth("99")
            width = 18 + 2 + maxNumberWidth + 5 + 18 + 2 + maxNumberWidth
            height = 18 * ceil((EssenceType.entries.size / 2f).toDouble()).toInt()
        }
        if (feature == Feature.SPIRIT_SCEPTRE_DISPLAY) {
            val maxNumberWidth: Int = mc.fontRendererObj.getStringWidth("12345")
            width += 18 + maxNumberWidth
            height += 20
        }


        if (feature == Feature.CANDY_POINTS_COUNTER) {
            width = 0

            var candyCounts: Map<CandyType, Int> = SpookyEventManager.getCandyCounts()!!
            if (!SpookyEventManager.isActive) {
                if (buttonLocation == null) {
                    return
                }

                candyCounts = SpookyEventManager.getDummyCandyCounts()
            }

            val green = candyCounts[CandyType.GREEN]!!
            val purple = candyCounts[CandyType.PURPLE]!!
            if (buttonLocation != null || green > 0) {
                width += 16 + 1 + mc.fontRendererObj.getStringWidth(green.toString())
            }
            if (buttonLocation != null || purple > 0) {
                if (green > 0) {
                    width += 1
                }

                width += 16 + 1 + mc.fontRendererObj.getStringWidth(purple.toString()) + 1
            }
            height = 16 + 8
        }

        x = transformXY(x, width, scale)
        y = transformXY(y, height, scale)

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale)
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        }

        main.utils!!.enableStandardGLOptions()

        when {
//            feature == Feature.DARK_AUCTION_TIMER -> {
//                mc.getTextureManager().bindTexture(SIRIUS_ICON)
//                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, 16f, 16f, 16f, 16f)
//
//                FontRendererHook.setupFeatureFont(feature)
//                DrawUtils.drawText(text, x + 18, y + 4, color)
//                FontRendererHook.endFeatureFont()
//            }
//            feature == Feature.FARM_EVENT_TIMER -> {
//                mc.getTextureManager().bindTexture(FARM_ICON)
//                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, 16f, 16f, 16f, 16f)
//
//                FontRendererHook.setupFeatureFont(feature)
//                DrawUtils.drawText(text, x + 18, y + 4, color)
//                FontRendererHook.endFeatureFont()
//            }
//            feature == Feature.ZEALOT_COUNTER -> {
//                mc.getTextureManager().bindTexture(ENDERMAN_ICON)
//                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, 16f, 16f, 16f, 16f)
//
//                FontRendererHook.setupFeatureFont(feature)
//                DrawUtils.drawText(text, x + 18, y + 4, color)
//                FontRendererHook.endFeatureFont()
//            }
//            feature == Feature.SHOW_TOTAL_ZEALOT_COUNT -> {
//                mc.getTextureManager().bindTexture(ENDERMAN_GROUP_ICON)
//                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, 16f, 16f, 16f, 16f)
//
//                FontRendererHook.setupFeatureFont(feature)
//                DrawUtils.drawText(text, x + 18, y + 4, color)
//                FontRendererHook.endFeatureFont()
//            }
//            feature == Feature.SHOW_SUMMONING_EYE_COUNT -> {
//                mc.getTextureManager().bindTexture(SUMMONING_EYE_ICON)
//                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, 16f, 16f, 16f, 16f)
//
//                FontRendererHook.setupFeatureFont(feature)
//                DrawUtils.drawText(text, x + 18, y + 4, color)
//                FontRendererHook.endFeatureFont()
//            }
//            feature == Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE -> {
//                mc.getTextureManager().bindTexture(ZEALOTS_PER_EYE_ICON)
//                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, 16f, 16f, 16f, 16f)
//                mc.getTextureManager().bindTexture(SLASH_ICON)
//                ColorUtils.bindColor(color)
//                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, 16f, 16f, 16f, 16f, true)
//
//                FontRendererHook.setupFeatureFont(feature)
//                DrawUtils.drawText(text, x + 18, y + 4, color)
//                FontRendererHook.endFeatureFont()
//            }
//            feature == Feature.SKILL_DISPLAY && ((skill != null && skill.getItem() != null) || buttonLocation != null) -> {
//                renderItem(if (buttonLocation == null) skill.getItem() else SkillType.FARMING.getItem(), x, y)
//
//                FontRendererHook.setupFeatureFont(feature)
//                DrawUtils.drawText(text, x + 18, y + 4, color)
//                FontRendererHook.endFeatureFont()
//            }
//            feature == Feature.BIRCH_PARK_RAINMAKER_TIMER -> {
//                renderItem(WATER_BUCKET, x, y)
//
//                FontRendererHook.setupFeatureFont(feature)
//                DrawUtils.drawText(text, x + 18, y + 4, color)
//                FontRendererHook.endFeatureFont()
//            }
//            feature == Feature.ENDSTONE_PROTECTOR_DISPLAY -> {
//                mc.getTextureManager().bindTexture(IRON_GOLEM_ICON)
//                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, 16f, 16f, 16f, 16f)
//
//                FontRendererHook.setupFeatureFont(feature)
//                DrawUtils.drawText(text, x + 18, y + 4, color)
//                FontRendererHook.endFeatureFont()
//
//                x += (16 + 2 + mc.fontRendererObj.getStringWidth(text) + 2).toFloat()
//
//                GlStateManager.color(1f, 1f, 1f, 1f)
//                mc.getTextureManager().bindTexture(ENDERMAN_GROUP_ICON)
//                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, 16f, 16f, 16f, 16f)
//
//                val count: Int = EndstoneProtectorManager.getZealotCount()
//
//                FontRendererHook.setupFeatureFont(feature)
//                DrawUtils.drawText(count.toString(), x + 16 + 2, y + 4, color)
//                FontRendererHook.endFeatureFont()
//            }
//            feature == Feature.SHOW_DUNGEON_MILESTONE -> {
//                var dungeonMilestone: DungeonMilestone = main.getDungeonManager().getDungeonMilestone()
//                if (buttonLocation != null) {
//                    dungeonMilestone = DungeonMilestone(DungeonClass.HEALER)
//                }
//
//                renderItem(dungeonMilestone.getDungeonClass().getItem(), x, y)
//                FontRendererHook.setupFeatureFont(feature)
//                DrawUtils.drawText(text, x + 18, y, color)
//                val amount: Double = dungeonMilestone.getValue().toDouble()
//                val formatter = DecimalFormat("#,###")
//                DrawUtils.drawText(
//                    formatter.format(amount), x + 18 + mc.fontRendererObj.getStringWidth(text) / 2f
//                            - mc.fontRendererObj.getStringWidth(formatter.format(amount)) / 2f, y + 9, color
//                )
//                FontRendererHook.endFeatureFont()
//            }
//            feature == Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY -> {
//                this.drawCollectedEssences(x, y, buttonLocation != null, true)
//            }
//            feature == Feature.DUNGEON_DEATH_COUNTER -> {
//                renderItem(SKULL, x, y)
//                FontRendererHook.setupFeatureFont(feature)
//                DrawUtils.drawText(text, x + 18, y + 4, color)
//                FontRendererHook.endFeatureFont()
//            }
//            feature == Feature.ROCK_PET_TRACKER -> {
//                renderItem(PET_ROCK, x, y)
//
//                FontRendererHook.setupFeatureFont(feature)
//                DrawUtils.drawText(text, x + 18, y + 4, color)
//                FontRendererHook.endFeatureFont()
//            }
//            feature == Feature.DOLPHIN_PET_TRACKER -> {
//                renderItem(DOLPHIN_PET, x, y)
//
//                FontRendererHook.setupFeatureFont(feature)
//                DrawUtils.drawText(text, x + 18, y + 4, color)
//                FontRendererHook.endFeatureFont()
//            }
//            feature == Feature.DUNGEONS_SECRETS_DISPLAY -> {
//                var secrets: Int = main.getDungeonManager().getSecrets()
//                var maxSecrets: Int = main.getDungeonManager().getMaxSecrets()
//
//                FontRendererHook.setupFeatureFont(feature)
//                DrawUtils.drawText(text, x + 16 + 2, y, color)
//                FontRendererHook.endFeatureFont()
//
//                if (secrets == -1 && buttonLocation != null) {
//                    secrets = 5
//                    maxSecrets = 10
//                }
//
//                if ((secrets == -1) or (maxSecrets == 0)) {
//                    FontRendererHook.setupFeatureFont(feature)
//                    val none = Translations.getMessage("messages.none")
//                    DrawUtils.drawText(
//                        none,
//                        x + 16 + 2 + mc.fontRendererObj.getStringWidth(text) / 2f - mc.fontRendererObj.getStringWidth(none) / 2f,
//                        y + 10,
//                        color
//                    )
//                    FontRendererHook.endFeatureFont()
//                } else {
//                    if (secrets > maxSecrets) {
//                        // Assume the max secrets equals to found secrets
//                        maxSecrets = secrets
//                    }
//
//                    var percent = secrets / maxSecrets.toFloat()
//                    if (percent < 0) {
//                        percent = 0f
//                    } else if (percent > 1) {
//                        percent = 1f
//                    }
//
//                    val r: Float
//                    val g: Float
//                    if (percent <= 0.5) { // Fade from red -> yellow
//                        r = 1f
//                        g = (percent * 2) * 0.66f + 0.33f
//                    } else { // Fade from yellow -> green
//                        r = (1 - percent) * 0.66f + 0.33f
//                        g = 1f
//                    }
//                    val secretsColor: Int = Color(min(1.0, r.toDouble()).toFloat(), g, 0.33f).getRGB()
//
//                    val secretsWidth: Float = mc.fontRendererObj.getStringWidth(secrets.toString()).toFloat()
//                    val slashWidth: Float = mc.fontRendererObj.getStringWidth("/").toFloat()
//                    val maxSecretsWidth: Float = mc.fontRendererObj.getStringWidth(maxSecrets.toString()).toFloat()
//
//                    val totalWidth = secretsWidth + slashWidth + maxSecretsWidth
//
//                    FontRendererHook.setupFeatureFont(feature)
//                    DrawUtils.drawText(
//                        "/",
//                        x + 16 + 2 + mc.fontRendererObj.getStringWidth(text) / 2f - totalWidth / 2f + secretsWidth,
//                        y + 11,
//                        color
//                    )
//                    FontRendererHook.endFeatureFont()
//
//                    DrawUtils.drawText(
//                        secrets.toString(),
//                        x + 16 + 2 + mc.fontRendererObj.getStringWidth(text) / 2f - totalWidth / 2f,
//                        y + 11,
//                        secretsColor
//                    )
//                    DrawUtils.drawText(
//                        maxSecrets.toString(),
//                        x + 16 + 2 + mc.fontRendererObj.getStringWidth(text) / 2f - totalWidth / 2f + secretsWidth + slashWidth,
//                        y + 11,
//                        secretsColor
//                    )
//                }
//
//                GlStateManager.color(1f, 1f, 1f, 1f)
//                renderItem(CHEST, x, y)
//            }
//            feature == Feature.SPIRIT_SCEPTRE_DISPLAY -> {
//                val hitEnemies: Int = main.getPlayerListener().getSpiritSceptreHitEnemies()
//                val dealtDamage: Float = main.getPlayerListener().getSpiritSceptreDealtDamage()
//                FontRendererHook.setupFeatureFont(feature)
//                DrawUtils.drawText(text, x + 16 + 2, y, color)
//                if (hitEnemies == 1) {
//                    DrawUtils.drawText(String.format("%d enemy hit", hitEnemies), x + 16 + 2, y + 9, color)
//                } else {
//                    DrawUtils.drawText(String.format("%d enemies hit", hitEnemies), x + 16 + 2, y + 9, color)
//                }
//                DrawUtils.drawText(String.format("%,d damage dealt", Math.round(dealtDamage)), x + 16 + 2, y + 18, color)
//                FontRendererHook.endFeatureFont()
//                val held: ItemStack = Minecraft.getMinecraft().thePlayer.getHeldItem()
//                val skyblockItemID: String? = ItemUtils.getSkyblockItemID(held)
//                if (buttonLocation != null) {
//                    renderItem(HYPERION, x, y)
//                } else if (skyblockItemID == "HYPERION") {
//                    renderItem(HYPERION, x, y)
//                } else if (skyblockItemID == "VALKYRIE") {
//                    renderItem(VALKYRIE, x, y)
//                } else if (skyblockItemID == "ASTRAEA") {
//                    renderItem(ASTRAEA, x, y)
//                } else if (skyblockItemID == "SCYLLA") {
//                    renderItem(SCYLLA, x, y)
//                } else if (skyblockItemID == "BAT_WAND") {
//                    renderItem(SCPETRE, x, y)
//                }
//            }
//            feature == Feature.CANDY_POINTS_COUNTER -> {
//                var candyCounts: Map<CandyType?, Int?> = SpookyEventManager.getCandyCounts()
//                if (!SpookyEventManager.isActive) {
//                    candyCounts = SpookyEventManager.getDummyCandyCounts()
//                }
//                val green = candyCounts[CandyType.GREEN]!!
//                val purple = candyCounts[CandyType.PURPLE]!!
//
//                var points: Int = SpookyEventManager.getPoints()
//                if (!SpookyEventManager.isActive) {
//                    points = 5678
//                }
//
//                var currentX = x
//                if (buttonLocation != null || green > 0) {
//                    renderItem(GREEN_CANDY, currentX, y)
//
//                    currentX += (16 + 1).toFloat()
//                    FontRendererHook.setupFeatureFont(feature)
//                    DrawUtils.drawText(green.toString(), currentX, y + 4, color)
//                    FontRendererHook.endFeatureFont()
//                }
//                if (buttonLocation != null || purple > 0) {
//                    if (buttonLocation != null || green > 0) {
//                        currentX += (mc.fontRendererObj.getStringWidth(green.toString()) + 1).toFloat()
//                    }
//
//                    renderItem(PURPLE_CANDY, currentX, y)
//
//                    currentX += (16 + 1).toFloat()
//                    FontRendererHook.setupFeatureFont(feature)
//                    DrawUtils.drawText(purple.toString(), currentX, y + 4, color)
//                    FontRendererHook.endFeatureFont()
//                }
//
//                FontRendererHook.setupFeatureFont(feature)
//                text = "$points Points"
//                DrawUtils.drawText(text, x + width / 2f - mc.fontRendererObj.getStringWidth(text) / 2f, y + 16, color)
//                FontRendererHook.endFeatureFont()
//            }
//            feature == Feature.FETCHUR_TODAY -> {
//                val showDwarven = main.configValues!!.isDisabled(Feature.SHOW_FETCHUR_ONLY_IN_DWARVENS) ||
//                        LocationUtils.isInDwarvenMines(main.getUtils().getLocation().getScoreboardName())
//                val showInventory = main.configValues!!.isDisabled(Feature.SHOW_FETCHUR_INVENTORY_OPEN_ONLY) ||
//                        Minecraft.getMinecraft().currentScreen != null
//                val fetchurItem: FetchurItem = FetchurManager.getInstance().getCurrentFetchurItem()
//                // Show if it's the gui button position, or the player hasn't given Fetchur, and it shouldn't be hidden b/c of dwarven mines or inventory
//                if (fetchurItem != null && (buttonLocation != null ||
//                            (!FetchurManager.getInstance().hasFetchedToday() && showDwarven && showInventory))
//                ) {
//                    FontRendererHook.setupFeatureFont(feature)
//
//                    if (main.configValues!!.isDisabled(Feature.SHOW_FETCHUR_ITEM_NAME)) {
//                        DrawUtils.drawText(text, x + 1, y + 4, color) // Line related to the "Fetchur wants" text
//                        val offsetX: Float = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text).toFloat()
//                        renderItemAndOverlay(
//                            fetchurItem.getItemStack(),
//                            fetchurItem.getItemStack().stackSize.toString(),
//                            x + offsetX,
//                            y
//                        )
//                    } else {
//                        DrawUtils.drawText(text, x, y, color) // Line related to the "Fetchur wants" text
//                    }
//                    FontRendererHook.endFeatureFont()
//                }
//            }
//            feature == Feature.HEALTH_TEXT -> {
//                if (mc.thePlayer.isPotionActive(22 /* Absorption */)
//                    && getAttribute(Attribute.HEALTH) > getAttribute(Attribute.MAX_HEALTH)
//                ) {
//                    val formattedHealth: String = NUMBER_FORMAT.format(getAttribute(Attribute.HEALTH))
//                    val formattedHealthWidth: Int = mc.fontRendererObj.getStringWidth(formattedHealth)
//
//                    color = ColorUtils.getDummySkyblockColor(
//                        ColorCode.GOLD.getColor(),
//                        main.configValues!!.getChromaFeatures().contains(feature)
//                    ).color
//                    FontRendererHook.setupFeatureFont(feature)
//                    DrawUtils.drawText(formattedHealth, x, y, color)
//                    color = main.configValues!!.getColor(feature)
//                    DrawUtils.drawText(
//                        "/" + NUMBER_FORMAT.format(getAttribute(Attribute.MAX_HEALTH)),
//                        x + formattedHealthWidth, y, color
//                    )
//                } else {
//                    FontRendererHook.setupFeatureFont(feature)
//                    DrawUtils.drawText(text, x, y, color)
//                    FontRendererHook.endFeatureFont()
//                }
//            }
            else -> {
                FontRendererHook.setupFeatureFont(feature)
                DrawUtils.drawText(text, x, y, color)
                FontRendererHook.endFeatureFont()
            }
        }

        main.utils!!.restoreGLOptions()
    }
//
//    private val crimsonArmorAbilityStacks: String?
//        get() {
//            val player: EntityPlayerSP = Minecraft.getMinecraft().thePlayer
//            val itemStacks: Array<ItemStack> = player.inventory.armorInventory
//
//            val builder = StringBuilder()
//            out@ for (crimsonArmorAbilityStack in CrimsonArmorAbilityStack.entries) {
//                for (itemStack in itemStacks) {
//                    if (itemStack == null) continue
//                    for (line in ItemUtils.getItemLore(itemStack)) {
//                        if (line.contains("§6Tiered Bonus: ")) {
//                            val abilityName: String = crimsonArmorAbilityStack.getAbilityName()
//                            if (line.contains(abilityName)) {
//                                val symbol: String = crimsonArmorAbilityStack.getSymbol()
//                                val stack: Int = crimsonArmorAbilityStack.getCurrentValue()
//                                builder.append("$abilityName $symbol $stack")
//                                continue@out
//                            }
//                        }
//                    }
//                }
//            }
//            return if (builder.length == 0) null else builder.toString()
//        }
//
    fun drawCollectedEssences(x: Float, y: Float, usePlaceholders: Boolean, hideZeroes: Boolean) {
        val mc: Minecraft = Minecraft.getMinecraft()

        var currentX = x
        var currentY: Float

        val maxNumberWidth: Int = mc.fontRendererObj.getStringWidth("99")

        val color: Int = main.configValues!!.getColor(Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY)

        var count = 0
        if (main.configValues!!.isEnabled(Feature.SHOW_SALVAGE_ESSENCES_COUNTER)) {
            for (essenceType in EssenceType.entries) {
                var value: Int = 0

//                value = if (main.inventoryUtils.getInventoryType() === InventoryType.SALVAGING) {
//                    main.getDungeonManager().getSalvagedEssences().getOrDefault(essenceType, 0)
//                } else {
//                    main.getDungeonManager().getCollectedEssences().getOrDefault(essenceType, 0)
//                }

                if (usePlaceholders) {
                    value = 99
                } else if (value <= 0 && hideZeroes) {
                    continue
                }

                val column = count % 2
                val row = count / 2

                if (column == 0) {
                    currentX = x
                } else if (column == 1) {
                    currentX = x + 18 + 2 + maxNumberWidth + 5
                }
                currentY = y + row * 18

                GlStateManager.color(1f, 1f, 1f, 1f)
                mc.getTextureManager().bindTexture(essenceType.getResourceLocation())
                DrawUtils.drawModalRectWithCustomSizedTexture(currentX, currentY, 0f, 0f, 16f, 16f, 16f, 16f)

                FontRendererHook.setupFeatureFont(Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY)
                DrawUtils.drawText(value.toString(), currentX + 18 + 2, currentY + 5, color)
                FontRendererHook.endFeatureFont()

                count++
            }
        }
    }

//    /**
//     * Displays the bait list. Only shows bait with count > 0.
//     */
//    fun drawBaitList(mc: Minecraft?, scale: Float, buttonLocation: ButtonLocation?) {
//        if (!BaitManager.getInstance().isHoldingRod() && buttonLocation == null) return
//
//        var baits: Map<BaitType?, Int?> = BaitManager.getInstance().getBaitsInInventory()
//        if (buttonLocation != null) {
//            baits = BaitManager.DUMMY_BAITS
//        }
//
//        var longestLineWidth = 0
//        for ((_, value) in baits) {
//            longestLineWidth = max(
//                longestLineWidth.toDouble(),
//                Minecraft.getMinecraft().fontRendererObj.getStringWidth(value.toString()).toDouble()
//            ).toInt()
//        }
//
//        var x: Float = main.configValues!!.getActualX(Feature.BAIT_LIST)
//        var y: Float = main.configValues!!.getActualY(Feature.BAIT_LIST)
//
//        val spacing = 1
//        val iconSize = 16
//        val width = iconSize + spacing + longestLineWidth
//        val height = iconSize * baits.size
//
//        x = transformXY(x, width, scale)
//        y = transformXY(y, height, scale)
//
//        if (buttonLocation != null) {
//            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale)
//        }
//
//        main.getUtils().enableStandardGLOptions()
//
//        for ((key, value) in baits) {
//            if (value == 0) continue
//
//            GlStateManager.color(1f, 1f, 1f, 1f)
//            renderItem(key.getItemStack(), x, y)
//
//            val color: Int = main.configValues!!.getColor(Feature.BAIT_LIST)
//            FontRendererHook.setupFeatureFont(Feature.BAIT_LIST)
//            DrawUtils.drawText(value.toString(), x + iconSize + spacing, y + (iconSize / 2f) - (8 / 2f), color)
//            FontRendererHook.endFeatureFont()
//
//            y += iconSize.toFloat()
//        }
//
//        main.getUtils().restoreGLOptions()
//    }
//
//    fun drawSlayerTrackers(feature: Feature, mc: Minecraft, scale: Float, buttonLocation: ButtonLocation?) {
//        val colorByRarity: Boolean
//        val textMode: Boolean
//        val slayerBoss: SlayerBoss
//        val quest: SlayerQuest = main.getUtils().getSlayerQuest()
//        val location: Location = main.getUtils().getLocation()
//        val config: ConfigValues = main.configValues!!
//        if (feature == Feature.REVENANT_SLAYER_TRACKER) {
//            if (buttonLocation == null && config.isEnabled(Feature.HIDE_WHEN_NOT_IN_CRYPTS) &&
//                (quest != EnumUtils.SlayerQuest.REVENANT_HORROR || location != Location.GRAVEYARD && location != Location.COAL_MINE)
//            ) {
//                return
//            }
//
//            colorByRarity = config.isEnabled(Feature.REVENANT_COLOR_BY_RARITY)
//            textMode = config.isEnabled(Feature.REVENANT_TEXT_MODE)
//            slayerBoss = SlayerBoss.REVENANT
//        } else if (feature == Feature.TARANTULA_SLAYER_TRACKER) {
//            if (buttonLocation == null && config.isEnabled(Feature.HIDE_WHEN_NOT_IN_SPIDERS_DEN) &&
//                (quest != EnumUtils.SlayerQuest.TARANTULA_BROODFATHER || location != Location.SPIDERS_DEN)
//            ) {
//                return
//            }
//
//            colorByRarity = config.isEnabled(Feature.TARANTULA_COLOR_BY_RARITY)
//            textMode = config.isEnabled(Feature.TARANTULA_TEXT_MODE)
//            slayerBoss = SlayerBoss.TARANTULA
//        } else if (feature == Feature.SVEN_SLAYER_TRACKER) {
//            if (buttonLocation == null && config.isEnabled(Feature.HIDE_WHEN_NOT_IN_CASTLE) &&
//                (quest != EnumUtils.SlayerQuest.SVEN_PACKMASTER || (location != Location.RUINS && location != Location.HOWLING_CAVE))
//            ) {
//                return
//            }
//
//            colorByRarity = config.isEnabled(Feature.SVEN_COLOR_BY_RARITY)
//            textMode = config.isEnabled(Feature.SVEN_TEXT_MODE)
//            slayerBoss = SlayerBoss.SVEN
//        } else if (feature == Feature.VOIDGLOOM_SLAYER_TRACKER) {
//            if (buttonLocation == null && config.isEnabled(Feature.HIDE_WHEN_NOT_IN_END) &&
//                (quest != EnumUtils.SlayerQuest.VOIDGLOOM_SERAPH || (location != Location.THE_END && location != Location.DRAGONS_NEST && location != Location.VOID_SEPULTURE))
//            ) {
//                return
//            }
//
//            colorByRarity = config.isEnabled(Feature.ENDERMAN_COLOR_BY_RARITY)
//            textMode = config.isEnabled(Feature.ENDERMAN_TEXT_MODE)
//            slayerBoss = SlayerBoss.VOIDGLOOM
//        } else {
//            return
//        }
//
//        var x = config.getActualX(feature)
//        var y = config.getActualY(feature)
//        val color = config.getColor(feature)
//
//        if (textMode) {
//            val lineHeight = 8
//            val spacer = 3
//
//            var lines = 0
//            var spacers = 0
//
//            val longestLineWidth: Int = mc.fontRendererObj.getStringWidth(slayerBoss.displayName)
//            lines++
//            spacers++
//
//            var longestSlayerDropLineWidth: Int =
//                mc.fontRendererObj.getStringWidth(Translations.getMessage("slayerTracker.bossesKilled"))
//            var longestCount: Int = mc.fontRendererObj.getStringWidth(
//                java.lang.String.valueOf(
//                    SlayerTracker.getInstance().getSlayerKills(slayerBoss)
//                )
//            )
//            lines++
//            spacers++
//
//            for (drop in slayerBoss.getDrops()) {
//                longestSlayerDropLineWidth = max(
//                    longestSlayerDropLineWidth.toDouble(),
//                    mc.fontRendererObj.getStringWidth(drop.displayName).toDouble()
//                ).toInt()
//                longestCount = max(
//                    longestCount.toDouble(),
//                    mc.fontRendererObj.getStringWidth(
//                        java.lang.String.valueOf(
//                            SlayerTracker.getInstance().getDropCount(drop)
//                        )
//                    )
//                        .toDouble()
//                ).toInt()
//                lines++
//            }
//
//            val width = max(
//                longestLineWidth.toDouble(),
//                (longestSlayerDropLineWidth + 8 + longestCount).toDouble()
//            ).toInt()
//            val height = lines * 8 + spacer * spacers
//
//            x = transformXY(x, width, scale)
//            y = transformXY(y, height, scale)
//
//            if (buttonLocation != null) {
//                buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale)
//            }
//
//            FontRendererHook.setupFeatureFont(feature)
//
//            DrawUtils.drawText(slayerBoss.displayName, x, y, color)
//            y += (lineHeight + spacer).toFloat()
//            DrawUtils.drawText(Translations.getMessage("slayerTracker.bossesKilled"), x, y, color)
//            var text: String? = java.lang.String.valueOf(SlayerTracker.getInstance().getSlayerKills(slayerBoss))
//            DrawUtils.drawText(text, x + width - mc.fontRendererObj.getStringWidth(text), y, color)
//            y += (lineHeight + spacer).toFloat()
//
//            FontRendererHook.endFeatureFont()
//
//            for (slayerDrop in slayerBoss.getDrops()) {
//                var currentColor = color
//                if (colorByRarity) {
//                    currentColor = slayerDrop.getRarity().getColorCode().getColor()
//                } else {
//                    FontRendererHook.setupFeatureFont(feature)
//                }
//
//                DrawUtils.drawText(slayerDrop.displayName, x, y, currentColor)
//
//                if (!colorByRarity) {
//                    FontRendererHook.endFeatureFont()
//                }
//
//                FontRendererHook.setupFeatureFont(feature)
//                text = java.lang.String.valueOf(SlayerTracker.getInstance().getDropCount(slayerDrop))
//                DrawUtils.drawText(text, x + width - mc.fontRendererObj.getStringWidth(text), y, currentColor)
//                FontRendererHook.endFeatureFont()
//
//                y += lineHeight.toFloat()
//            }
//        } else {
//            val entityRenderY: Int
//            val textCenterX: Int
//            if (feature == Feature.REVENANT_SLAYER_TRACKER) {
//                entityRenderY = 30
//                textCenterX = 15
//            } else if (feature == Feature.TARANTULA_SLAYER_TRACKER) {
//                entityRenderY = 36
//                textCenterX = 28
//            } else if (feature == Feature.VOIDGLOOM_SLAYER_TRACKER) {
//                entityRenderY = 25
//                textCenterX = 20
//            } else {
//                entityRenderY = 36
//                textCenterX = 15
//            }
//
//            val iconWidth = 16
//
//            val entityWidth = textCenterX * 2
//            val entityIconSpacingHorizontal = 2
//            val iconTextOffset = -2
//            var row = 0
//            var column = 0
//            val maxItemsPerRow = ceil(slayerBoss.getDrops().size() / 3.0) as Int
//            val maxTextWidths = IntArray(maxItemsPerRow)
//            for (slayerDrop in slayerBoss.getDrops()) {
//                val width: Int = mc.fontRendererObj.getStringWidth(
//                    TextUtils.abbreviate(
//                        SlayerTracker.getInstance().getDropCount(slayerDrop)
//                    )
//                )
//
//                maxTextWidths[column] = max(maxTextWidths[column].toDouble(), width.toDouble()).toInt()
//
//                column++
//                if (column == maxItemsPerRow) {
//                    column = 0
//                    row++
//                }
//            }
//
//            var totalColumnWidth = 0
//            for (i in maxTextWidths) {
//                totalColumnWidth += i
//            }
//            val iconSpacingVertical = 4
//
//            val width =
//                entityWidth + entityIconSpacingHorizontal + maxItemsPerRow * iconWidth + totalColumnWidth + iconTextOffset
//            val height = (iconWidth + iconSpacingVertical) * 3 - iconSpacingVertical
//
//            x = transformXY(x, width, scale)
//            y = transformXY(y, height, scale)
//
//            if (buttonLocation != null) {
//                buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale)
//            }
//
//            if (feature == Feature.REVENANT_SLAYER_TRACKER) {
//                if (revenant == null) {
//                    revenant = EntityZombie(Utils.getDummyWorld())
//
//                    revenant.getInventory().get(0) = ItemUtils.createItemStack(Items.diamond_hoe, true)
//                    revenant.getInventory().get(1) = ItemUtils.createItemStack(Items.diamond_boots, false)
//                    revenant.getInventory().get(2) = ItemUtils.createItemStack(Items.diamond_leggings, true)
//                    revenant.getInventory().get(3) = ItemUtils.createItemStack(Items.diamond_chestplate, true)
//                    revenant.getInventory().get(4) = ItemUtils.createSkullItemStack(
//                        null,
//                        null,
//                        "45012ee3-29fd-42ed-908b-648c731c7457",
//                        "1fc0184473fe882d2895ce7cbc8197bd40ff70bf10d3745de97b6c2a9c5fc78f"
//                    )
//                }
//                GlStateManager.color(1f, 1f, 1f, 1f)
//                revenant.ticksExisted = main.getNewScheduler().getTotalTicks()
//                drawEntity(revenant, x + 15, y + 53, -15f) // left is 35
//            } else if (feature == Feature.TARANTULA_SLAYER_TRACKER) {
//                if (tarantula == null) {
//                    tarantula = EntitySpider(Utils.getDummyWorld())
//
//                    caveSpider = EntityCaveSpider(Utils.getDummyWorld())
//
//                    tarantula.riddenByEntity = caveSpider
//                    caveSpider.ridingEntity = tarantula
//                }
//                GlStateManager.color(1f, 1f, 1f, 1f)
//                drawEntity(tarantula, x + 28, y + 38, -30f)
//                drawEntity(caveSpider, x + 25, y + 23, -30f)
//            } else if (feature == Feature.VOIDGLOOM_SLAYER_TRACKER) {
//                if (enderman == null) {
//                    enderman = EntityEnderman(Utils.getDummyWorld())
//
//                    enderman.setHeldBlockState(Blocks.beacon.getBlockState().getBaseState())
//                }
//                GlStateManager.color(1f, 1f, 1f, 1f)
//                enderman.ticksExisted = main.getNewScheduler().getTotalTicks()
//                GlStateManager.scale(.7, .7, 1.0)
//                drawEntity(enderman, (x + 15) / .7f, (y + 51) / .7f, -30f)
//                GlStateManager.scale(1 / .7, 1 / .7, 1.0)
//            } else {
//                if (sven == null) {
//                    sven = EntityWolf(Utils.getDummyWorld())
//                    sven.setAngry(true)
//                }
//                GlStateManager.color(1f, 1f, 1f, 1f)
//                drawEntity(sven, x + 17, y + 38, -35f)
//            }
//
//            GlStateManager.disableDepth()
//            FontRendererHook.setupFeatureFont(feature)
//            val text = TextUtils.abbreviate(SlayerTracker.getInstance().getSlayerKills(slayerBoss)) + " Kills"
//            DrawUtils.drawText(
//                text,
//                x + textCenterX - mc.fontRendererObj.getStringWidth(text) / 2f,
//                y + entityRenderY,
//                color
//            )
//            FontRendererHook.endFeatureFont()
//
//            row = 0
//            column = 0
//            var currentX = x + entityIconSpacingHorizontal + entityWidth
//            for (slayerDrop in slayerBoss.getDrops()) {
//                if (column > 0) {
//                    currentX += (iconWidth + maxTextWidths[column - 1]).toFloat()
//                }
//
//                val currentY = y + row * (iconWidth + iconSpacingVertical)
//
//                GlStateManager.color(1f, 1f, 1f, 1f)
//                renderItem(slayerDrop.getItemStack(), currentX, currentY)
//
//                GlStateManager.disableDepth()
//
//                var currentColor = color
//                if (colorByRarity) {
//                    currentColor = slayerDrop.getRarity().getColorCode().getColor()
//                } else {
//                    FontRendererHook.setupFeatureFont(feature)
//                }
//
//                DrawUtils.drawText(
//                    TextUtils.abbreviate(SlayerTracker.getInstance().getDropCount(slayerDrop)),
//                    currentX + iconWidth + iconTextOffset,
//                    currentY + 8,
//                    currentColor
//                )
//                if (!colorByRarity) {
//                    FontRendererHook.endFeatureFont()
//                }
//
//                column++
//                if (column == maxItemsPerRow) {
//                    currentX = x + entityIconSpacingHorizontal + entityWidth
//                    column = 0
//                    row++
//                }
//            }
//            GlStateManager.enableDepth()
//        }
//    }
//
//    fun drawDragonTrackers(mc: Minecraft, scale: Float, buttonLocation: ButtonLocation?) {
//        if (main.configValues!!.isEnabled(Feature.DRAGON_STATS_TRACKER_NEST_ONLY) && main.getUtils()
//                .getLocation() !== Location.DRAGONS_NEST && buttonLocation == null
//        ) {
//            return
//        }
//
//        var recentDragons: List<DragonType?> = DragonTracker.getInstance().getRecentDragons()
//        if (recentDragons.isEmpty() && buttonLocation != null) {
//            recentDragons = DragonTracker.getDummyDragons()
//        }
//
//        val colorByRarity: Boolean = main.configValues!!.isEnabled(Feature.DRAGON_STATS_TRACKER_COLOR_BY_RARITY)
//        val textMode: Boolean = main.configValues!!.isEnabled(Feature.DRAGON_STATS_TRACKER_TEXT_MODE)
//
//        val spacerHeight = 3
//        val never = Translations.getMessage("dragonTracker.never")
//        val width: Int
//        val height: Int
//        if (textMode) {
//            var lines = 0
//            var spacers = 0
//
//            var longestLineWidth: Int =
//                mc.fontRendererObj.getStringWidth(Translations.getMessage("dragonTracker.recentDragons"))
//            lines++
//            spacers++
//
//            spacers++
//            longestLineWidth = max(
//                longestLineWidth.toDouble(),
//                mc.fontRendererObj.getStringWidth(Translations.getMessage("dragonTracker.dragonsSince"))
//                    .toDouble()
//            ).toInt()
//            lines++
//            spacers++
//
//            for (dragon in recentDragons) {
//                longestLineWidth =
//                    max(
//                        longestLineWidth.toDouble(),
//                        mc.fontRendererObj.getStringWidth(dragon.displayName).toDouble()
//                    ).toInt()
//                lines++
//            }
//
//            var longestCount = 0
//            var longestDragonsSinceLineWidth = 0
//            for (dragonsSince in DragonsSince.entries) {
//                longestDragonsSinceLineWidth = max(
//                    longestDragonsSinceLineWidth.toDouble(),
//                    mc.fontRendererObj.getStringWidth(dragonsSince.displayName).toDouble()
//                ).toInt()
//                val dragonsSinceValue: Int = DragonTracker.getInstance().getDragsSince(dragonsSince)
//                longestCount = max(
//                    longestCount.toDouble(),
//                    mc.fontRendererObj.getStringWidth(if (dragonsSinceValue == 0) never else dragonsSinceValue.toString())
//                        .toDouble()
//                ).toInt()
//                lines++
//            }
//            width = max(
//                longestLineWidth.toDouble(),
//                (longestDragonsSinceLineWidth + 8 + longestCount).toDouble()
//            ).toInt()
//
//            height = lines * 8 + spacerHeight * spacers
//        } else {
//            width = 100
//            height = 100
//        }
//
//        var x: Float = main.configValues!!.getActualX(Feature.DRAGON_STATS_TRACKER)
//        var y: Float = main.configValues!!.getActualY(Feature.DRAGON_STATS_TRACKER)
//        x = transformXY(x, width, scale)
//        y = transformXY(y, height, scale)
//
//        if (buttonLocation != null) {
//            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale)
//        }
//
//        var color: Int = main.configValues!!.getColor(Feature.DRAGON_STATS_TRACKER)
//
//        if (textMode) {
//            FontRendererHook.setupFeatureFont(Feature.DRAGON_STATS_TRACKER)
//            DrawUtils.drawText(Translations.getMessage("dragonTracker.recentDragons"), x, y, color)
//            y += (8 + spacerHeight).toFloat()
//            FontRendererHook.endFeatureFont()
//
//            for (dragon in recentDragons) {
//                var currentColor = color
//                if (colorByRarity) {
//                    currentColor = dragon.getColor().getColor()
//                } else {
//                    FontRendererHook.setupFeatureFont(Feature.DRAGON_STATS_TRACKER)
//                }
//
//                DrawUtils.drawText(dragon.displayName, x, y, currentColor)
//
//                if (!colorByRarity) {
//                    FontRendererHook.endFeatureFont()
//                }
//
//                y += 8f
//            }
//            y += spacerHeight.toFloat()
//
//            FontRendererHook.setupFeatureFont(Feature.DRAGON_STATS_TRACKER)
//            color = main.configValues!!.getColor(Feature.DRAGON_STATS_TRACKER)
//            DrawUtils.drawText(Translations.getMessage("dragonTracker.dragonsSince"), x, y, color)
//            y += (8 + spacerHeight).toFloat()
//            FontRendererHook.endFeatureFont()
//
//            for (dragonsSince in DragonsSince.entries) {
//                GlStateManager.disableDepth()
//                GlStateManager.enableBlend()
//                GlStateManager.color(1f, 1f, 1f, 1f)
//                GlStateManager.disableBlend()
//                GlStateManager.enableDepth()
//
//                var currentColor = color
//                if (colorByRarity) {
//                    currentColor = dragonsSince.getItemRarity().getColorCode().getColor()
//                } else {
//                    FontRendererHook.setupFeatureFont(Feature.DRAGON_STATS_TRACKER)
//                }
//
//                DrawUtils.drawText(dragonsSince.displayName, x, y, currentColor)
//
//                if (!colorByRarity) {
//                    FontRendererHook.endFeatureFont()
//                }
//
//                FontRendererHook.setupFeatureFont(Feature.DRAGON_STATS_TRACKER)
//                val dragonsSinceValue: Int = DragonTracker.getInstance().getDragsSince(dragonsSince)
//                val text = if (dragonsSinceValue == 0) never else dragonsSinceValue.toString()
//                DrawUtils.drawText(text, x + width - mc.fontRendererObj.getStringWidth(text), y, color)
//                y += 8f
//                FontRendererHook.endFeatureFont()
//            }
//        }
//    }
//
//    fun drawRevenantIndicator(scale: Float, mc: Minecraft, buttonLocation: ButtonLocation?) {
//        var x: Float = main.configValues!!.getActualX(Feature.SLAYER_INDICATOR)
//        var y: Float = main.configValues!!.getActualY(Feature.SLAYER_INDICATOR)
//
//        var longest = -1
//        var progresses: Array<SlayerArmorProgress?> = main.getInventoryUtils().getSlayerArmorProgresses()
//        if (buttonLocation != null) progresses = DUMMY_PROGRESSES
//        for (progress in progresses) {
//            if (progress == null) continue
//
//            val textWidth: Int =
//                mc.fontRendererObj.getStringWidth((progress.getPercent() + "% (" + progress.getDefence()).toString() + ")")
//            if (textWidth > longest) {
//                longest = textWidth
//            }
//        }
//        if (longest == -1) return
//
//        val height = 15 * 4
//        val width = 16 + 2 + longest
//
//        x = transformXY(x, width, scale)
//        y = transformXY(y, height, scale)
//
//        if (buttonLocation != null) {
//            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale)
//            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
//        }
//
//        main.getUtils().enableStandardGLOptions()
//
//        val anchorPoint: AnchorPoint = main.configValues!!.getAnchorPoint(Feature.SLAYER_INDICATOR)
//        val downwards =
//            (anchorPoint == EnumUtils.AnchorPoint.TOP_LEFT || anchorPoint == EnumUtils.AnchorPoint.TOP_RIGHT)
//
//        val color: Int = main.configValues!!.getColor(Feature.SLAYER_INDICATOR)
//
//        var drawnCount = 0
//        for (armorPiece in 3 downTo 0) {
//            val progress: SlayerArmorProgress? = progresses[if (downwards) armorPiece else 3 - armorPiece]
//            if (progress == null) continue
//            var fixedY = if (downwards) {
//                y + drawnCount * 15
//            } else {
//                (y + 45) - drawnCount * 15
//            }
//            renderItem(progress.getItemStack(), x, fixedY)
//
//            var currentX = x + 19
//            FontRendererHook.setupFeatureFont(Feature.SLAYER_INDICATOR)
//            DrawUtils.drawText(progress.getPercent() + "% (", currentX, fixedY + 5, color)
//            FontRendererHook.endFeatureFont()
//
//            currentX += mc.fontRendererObj.getStringWidth(progress.getPercent() + "% (").toFloat()
//            DrawUtils.drawText(progress.getDefence(), currentX, fixedY + 5, -0x1)
//
//            currentX += mc.fontRendererObj.getStringWidth(progress.getDefence()).toFloat()
//            FontRendererHook.setupFeatureFont(Feature.SLAYER_INDICATOR)
//            DrawUtils.drawText(")", currentX, fixedY + 5, color)
//            FontRendererHook.endFeatureFont()
//
//            drawnCount++
//        }
//
//        main.getUtils().restoreGLOptions()
//    }
//
//    fun drawPotionEffectTimers(scale: Float, buttonLocation: ButtonLocation?) {
//        var x: Float = main.configValues!!.getActualX(Feature.TAB_EFFECT_TIMERS)
//        var y: Float = main.configValues!!.getActualY(Feature.TAB_EFFECT_TIMERS)
//
//        val tabEffect: TabEffectManager = TabEffectManager.getInstance()
//
//        var potionTimers: List<TabEffect?> = tabEffect.getPotionTimers()
//        var powerupTimers: List<TabEffect?> = tabEffect.getPowerupTimers()
//
//        if (buttonLocation == null) {
//            if (potionTimers.isEmpty() && powerupTimers.isEmpty() && TabEffectManager.getInstance()
//                    .getEffectCount() === 0
//            ) {
//                return
//            }
//        } else { // When editing GUI draw dummy timers.
//            potionTimers = TabEffectManager.getDummyPotionTimers()
//            powerupTimers = TabEffectManager.getDummyPowerupTimers()
//        }
//
//        val anchorPoint: AnchorPoint = main.configValues!!.getAnchorPoint(Feature.TAB_EFFECT_TIMERS)
//        val topDown = (anchorPoint == EnumUtils.AnchorPoint.TOP_LEFT || anchorPoint == EnumUtils.AnchorPoint.TOP_RIGHT)
//
//        val totalEffects: Int =
//            TabEffectManager.getDummyPotionTimers().size() + TabEffectManager.getDummyPowerupTimers()
//                .size() + 1 // + 1 to account for the "x Effects Active" line
//        val spacer =
//            if ((!TabEffectManager.getDummyPotionTimers().isEmpty() && !TabEffectManager.getDummyPowerupTimers()
//                    .isEmpty())
//            ) 3 else 0
//
//        val lineHeight = 8 + 1 // 1 pixel between each line.
//
//        //9 px per effect + 3px spacer between Potions and Powerups if both exist.
//        val height = (totalEffects * lineHeight) + spacer - 1 // -1 Because last line doesn't need a pixel under.
//        val width = 156 //String width of "Enchanting XP Boost III 1:23:45"
//
//        x = transformXY(x, width, scale)
//        y = transformXY(y, height, scale)
//
//        if (buttonLocation != null) {
//            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale)
//            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
//        }
//
//        main.getUtils().enableStandardGLOptions()
//
//        val alignRight =
//            (anchorPoint == EnumUtils.AnchorPoint.TOP_RIGHT || anchorPoint == EnumUtils.AnchorPoint.BOTTOM_RIGHT)
//
//        val color: Int = main.configValues!!.getColor(Feature.TAB_EFFECT_TIMERS)
//
//        val mc: Minecraft = Minecraft.getMinecraft()
//
//        // Draw the "x Effects Active" line
//        FontRendererHook.setupFeatureFont(Feature.TAB_EFFECT_TIMERS)
//        val effectCount: Int = TabEffectManager.getInstance().getEffectCount()
//        val text = if (effectCount == 1) Translations.getMessage("messages.effectActive") else Translations.getMessage(
//            "messages.effectsActive",
//            effectCount.toString()
//        )
//        var lineY: Float
//        lineY = if (topDown) {
//            y
//        } else {
//            y + height - 8
//        }
//        if (alignRight) {
//            DrawUtils.drawText(text, x + width - mc.fontRendererObj.getStringWidth(text), lineY, color)
//        } else {
//            DrawUtils.drawText(text, x, lineY, color)
//        }
//        FontRendererHook.endFeatureFont()
//
//        var drawnCount = 1 // 1 to account for the line above
//        for (potion in potionTimers) {
//            lineY = if (topDown) {
//                y + drawnCount * lineHeight
//            } else {
//                y + height - drawnCount * lineHeight - 8
//            }
//
//            val effect: String = potion.getEffect()
//            val duration: String = potion.durationForDisplay
//
//            if (alignRight) {
//                FontRendererHook.setupFeatureFont(Feature.TAB_EFFECT_TIMERS)
//                DrawUtils.drawText(
//                    "$duration ", (x + width - mc.fontRendererObj.getStringWidth("$duration ")
//                            - mc.fontRendererObj.getStringWidth(effect.trim { it <= ' ' })), lineY, color
//                )
//                FontRendererHook.endFeatureFont()
//                DrawUtils.drawText(
//                    effect.trim { it <= ' ' },
//                    x + width - mc.fontRendererObj.getStringWidth(effect.trim { it <= ' ' }),
//                    lineY,
//                    color
//                )
//            } else {
//                DrawUtils.drawText(effect, x, lineY, color)
//                FontRendererHook.setupFeatureFont(Feature.TAB_EFFECT_TIMERS)
//                DrawUtils.drawText(duration, x + mc.fontRendererObj.getStringWidth(effect), lineY, color)
//                FontRendererHook.endFeatureFont()
//            }
//            drawnCount++
//        }
//        for (powerUp in powerupTimers) {
//            lineY = if (topDown) {
//                y + spacer + drawnCount * lineHeight
//            } else {
//                y + height - drawnCount * lineHeight - spacer - 8
//            }
//
//            val effect: String = powerUp.getEffect()
//            val duration: String = powerUp.durationForDisplay
//
//            if (alignRight) {
//                FontRendererHook.setupFeatureFont(Feature.TAB_EFFECT_TIMERS)
//                DrawUtils.drawText(
//                    "$duration ", (x + width - mc.fontRendererObj.getStringWidth("$duration ")
//                            - mc.fontRendererObj.getStringWidth(effect.trim { it <= ' ' })), lineY, color
//                )
//                FontRendererHook.endFeatureFont()
//                DrawUtils.drawText(
//                    effect,
//                    x + width - mc.fontRendererObj.getStringWidth(effect.trim { it <= ' ' }),
//                    lineY,
//                    color
//                )
//            } else {
//                DrawUtils.drawText(effect, x, lineY, color)
//                FontRendererHook.setupFeatureFont(Feature.TAB_EFFECT_TIMERS)
//                DrawUtils.drawText(duration, x + mc.fontRendererObj.getStringWidth(effect), lineY, color)
//                FontRendererHook.endFeatureFont()
//            }
//            drawnCount++
//        }
//
//        main.getUtils().restoreGLOptions()
//    }
//
    private fun renderItem(item: ItemStack, x: Float, y: Float) {
        GlStateManager.enableRescaleNormal()
        RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.enableDepth()

        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0f)
        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(item, 0, 0)
        GlStateManager.popMatrix()

        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
    }
//
//    private fun renderItemAndOverlay(item: ItemStack, name: String, x: Float, y: Float) {
//        GlStateManager.enableRescaleNormal()
//        RenderHelper.enableGUIStandardItemLighting()
//        GlStateManager.enableDepth()
//
//        GlStateManager.pushMatrix()
//        GlStateManager.translate(x, y, 0f)
//        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(item, 0, 0)
//        Minecraft.getMinecraft().getRenderItem()
//            .renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRendererObj, item, 0, 0, name)
//        GlStateManager.popMatrix()
//
//        RenderHelper.disableStandardItemLighting()
//        GlStateManager.disableRescaleNormal()
//    }
//
//    fun drawItemPickupLog(scale: Float, buttonLocation: ButtonLocation?) {
//        var x: Float = main.configValues!!.getActualX(Feature.ITEM_PICKUP_LOG)
//        var y: Float = main.configValues!!.getActualY(Feature.ITEM_PICKUP_LOG)
//
//        val anchorPoint: AnchorPoint = main.configValues!!.getAnchorPoint(Feature.ITEM_PICKUP_LOG)
//        val downwards = anchorPoint == EnumUtils.AnchorPoint.TOP_RIGHT || anchorPoint == EnumUtils.AnchorPoint.TOP_LEFT
//
//        val lineHeight = 8 + 1 // 1 pixel spacer
//        val height = lineHeight * 3 - 1
//        val width: Int = Minecraft.getMinecraft().fontRendererObj.getStringWidth("+ 1x Forceful Ember Chestplate")
//
//        x = transformXY(x, width, scale)
//        y = transformXY(y, height, scale)
//
//        if (buttonLocation != null) {
//            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale)
//            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
//        }
//
//        main.getUtils().enableStandardGLOptions()
//
//        var i = 0
//        var log: Collection<ItemDiff?> = main.getInventoryUtils().getItemPickupLog()
//        if (buttonLocation != null) {
//            log = DUMMY_PICKUP_LOG
//        }
//        for (itemDiff in log) {
//            val text = String.format(
//                "%s %sx §r%s", if (itemDiff.getAmount() > 0) "§a+" else "§c-",
//                abs(itemDiff.getAmount().toDouble()), itemDiff.getDisplayName()
//            )
//            var stringY = y + (i * lineHeight)
//            if (!downwards) {
//                stringY = y + height - (i * lineHeight) - 8
//            }
//
//            DrawUtils.drawText(text, x, stringY, -0x1)
//            i++
//        }
//
//        main.getUtils().restoreGLOptions()
//    }
//
//    fun drawPowerOrbStatus(mc: Minecraft, scale: Float, buttonLocation: ButtonLocation?) {
//        var activePowerOrb: PowerOrbManager.PowerOrbEntry = PowerOrbManager.getInstance().getActivePowerOrb()
//        if (buttonLocation != null) {
//            activePowerOrb = PowerOrbManager.DUMMY_POWER_ORB_ENTRY
//        }
//        if (activePowerOrb != null) {
//            val powerOrb: PowerOrb = activePowerOrb.getPowerOrb()
//            val seconds: Int = activePowerOrb.getSeconds()
//
//            val displayStyle: PowerOrbDisplayStyle = main.configValues!!.getPowerOrbDisplayStyle()
//            if (displayStyle == EnumUtils.PowerOrbDisplayStyle.DETAILED) {
//                drawDetailedPowerOrbStatus(mc, scale, buttonLocation, powerOrb, seconds)
//            } else {
//                drawCompactPowerOrbStatus(mc, scale, buttonLocation, powerOrb, seconds)
//            }
//        }
//    }
//
//    /**
//     * Displays the power orb display in a compact way with only the amount of seconds to the right of the icon.
//     *
//     *
//     * --
//     * |  | XXs
//     * --
//     */
//    private fun drawCompactPowerOrbStatus(
//        mc: Minecraft,
//        scale: Float,
//        buttonLocation: ButtonLocation?,
//        powerOrb: PowerOrb,
//        seconds: Int
//    ) {
//        var x: Float = main.configValues!!.getActualX(Feature.POWER_ORB_STATUS_DISPLAY)
//        var y: Float = main.configValues!!.getActualY(Feature.POWER_ORB_STATUS_DISPLAY)
//
//        val secondsString = String.format("§e%ss", seconds)
//        val spacing = 1
//        val iconSize: Int = mc.fontRendererObj.FONT_HEIGHT * 3 // 3 because it looked the best
//        val width: Int = iconSize + spacing + mc.fontRendererObj.getStringWidth(secondsString)
//
//        x = transformXY(x, width, scale)
//        y = transformXY(y, iconSize, scale)
//
//        if (buttonLocation != null) {
//            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + iconSize, scale)
//            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
//        }
//
//        var entity: Entity? = null
//        if (PowerOrbManager.getInstance().getActivePowerOrb() != null && PowerOrbManager.getInstance()
//                .getActivePowerOrb().getUuid() != null
//        ) {
//            entity = Utils.getEntityByUUID(PowerOrbManager.getInstance().getActivePowerOrb().getUuid())
//        }
//
//        if (entity == null && buttonLocation != null) {
//            entity = radiantDummyArmorStand
//        }
//
//        main.getUtils().enableStandardGLOptions()
//
//        if (entity is EntityArmorStand) {
//            drawPowerOrbArmorStand(entity as EntityArmorStand, x + 1, y + 4)
//        } else {
//            mc.getTextureManager().bindTexture(powerOrb.getResourceLocation())
//            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, iconSize, iconSize, iconSize, iconSize)
//        }
//
//        DrawUtils.drawText(
//            secondsString,
//            x + spacing + iconSize,
//            y + (iconSize / 2f) - (8 / 2f),
//            ColorCode.WHITE.getColor(255)
//        )
//
//        main.getUtils().restoreGLOptions()
//    }
//
//    /**
//     * Displays the power orb with detailed stats about the boost you're receiving.
//     *
//     *
//     * --  +X ❤/s
//     * |  | +X ✎/s
//     * --  +X ❁
//     * XXs
//     */
//    private fun drawDetailedPowerOrbStatus(
//        mc: Minecraft,
//        scale: Float,
//        buttonLocation: ButtonLocation?,
//        powerOrb: PowerOrb,
//        seconds: Int
//    ) {
//        var x: Float = main.configValues!!.getActualX(Feature.POWER_ORB_STATUS_DISPLAY)
//        var y: Float = main.configValues!!.getActualY(Feature.POWER_ORB_STATUS_DISPLAY)
//
//        val maxHealth: Float = main.getUtils().getAttributes().get(Attribute.MAX_HEALTH).getValue()
//        var healthRegen = (maxHealth * powerOrb.getHealthRegen()) as Float
//        if (main.getUtils().getSlayerQuest() === EnumUtils.SlayerQuest.TARANTULA_BROODFATHER && main.getUtils()
//                .getSlayerQuestLevel() >= 2
//        ) {
//            healthRegen *= 0.5.toFloat() // Tarantula boss 2+ reduces healing by 50%.
//        }
//        val healIncrease: Double = powerOrb.getHealIncrease() * 100
//
//        val display: MutableList<String> = LinkedList()
//        display.add(java.lang.String.format("§c+%s ❤/s", TextUtils.formatDouble(healthRegen.toDouble())))
//        if (powerOrb.getManaRegen() > 0) {
//            val maxMana: Float = main.getUtils().getAttributes().get(Attribute.MAX_MANA).getValue()
//            var manaRegen = floor((maxMana / 50).toDouble()) as Float
//            manaRegen = (manaRegen + manaRegen * powerOrb.getManaRegen()) as Float
//            display.add(java.lang.String.format("§b+%s ✎/s", TextUtils.formatDouble(manaRegen.toDouble())))
//        }
//        if (powerOrb.getStrength() > 0) {
//            display.add(java.lang.String.format("§4+%d ❁", powerOrb.getStrength()))
//        }
//        if (healIncrease > 0) {
//            display.add(java.lang.String.format("§2+%s%% Healing", TextUtils.formatDouble(healIncrease)))
//        }
//
//        val longestLine = display.stream().max(Comparator.comparingInt { obj: String -> obj.length })
//
//        val spacingBetweenLines = 1
//        val iconSize: Int = mc.fontRendererObj.FONT_HEIGHT * 3 // 3 because it looked the best
//        val iconAndSecondsHeight: Int = iconSize + mc.fontRendererObj.FONT_HEIGHT
//
//        val effectsHeight: Int = (mc.fontRendererObj.FONT_HEIGHT + spacingBetweenLines) * display.size
//        val width = iconSize + 2 + longestLine.map(Function<String, Int> { text: String? ->
//            mc.fontRendererObj.getStringWidth(text)
//        })
//            .orElseGet { mc.fontRendererObj.getStringWidth(display[0]) }
//        val height = max(effectsHeight.toDouble(), iconAndSecondsHeight.toDouble()).toInt()
//
//        x = transformXY(x, width, scale)
//        y = transformXY(y, height, scale)
//
//        if (buttonLocation != null) {
//            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale)
//            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
//        }
//
//        var entity: Entity? = null
//        if (PowerOrbManager.getInstance().getActivePowerOrb() != null && PowerOrbManager.getInstance()
//                .getActivePowerOrb().getUuid() != null
//        ) {
//            entity = Utils.getEntityByUUID(PowerOrbManager.getInstance().getActivePowerOrb().getUuid())
//        }
//
//        if (entity == null && buttonLocation != null) {
//            entity = radiantDummyArmorStand
//        }
//
//        main.getUtils().enableStandardGLOptions()
//
//        if (entity is EntityArmorStand) {
//            drawPowerOrbArmorStand(entity as EntityArmorStand, x + 1, y + 4)
//        } else {
//            mc.getTextureManager().bindTexture(powerOrb.getResourceLocation())
//            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, iconSize, iconSize, iconSize, iconSize)
//        }
//
//        val secondsString = String.format("§e%ss", seconds)
//        DrawUtils.drawText(
//            secondsString,
//            Math.round(x + (iconSize / 2f) - (mc.fontRendererObj.getStringWidth(secondsString) / 2f)),
//            y + iconSize,
//            ColorCode.WHITE.getColor(255)
//        )
//
//        val startY = Math.round(y + (iconAndSecondsHeight / 2f) - (effectsHeight / 2f)).toFloat()
//        for (i in display.indices) {
//            DrawUtils.drawText(
//                display[i],
//                x + iconSize + 2,
//                startY + (i * (mc.fontRendererObj.FONT_HEIGHT + spacingBetweenLines)),
//                ColorCode.WHITE.getColor(255)
//            )
//        }
//
//        main.getUtils().restoreGLOptions()
//    }
//
    /**
     * Easily grab an attribute from utils.
     */
    fun getAttribute(attribute: Attribute): Float {
        return main.utils!!.getAttributes().get(attribute)!!.getValue()
    }
//
//    @SubscribeEvent
//    fun onRenderRemoveBars(e: RenderGameOverlayEvent.Pre) {
//        if (main.getUtils().isOnSkyblock() && main.configValues!!.isEnabled(Feature.COMPACT_TAB_LIST)) {
//            if (e.type == RenderGameOverlayEvent.ElementType.PLAYER_LIST) {
//                if (TabListParser.getRenderColumns() != null) {
//                    e.setCanceled(true)
//                    TabListRenderer.render()
//                }
//            }
//        }
//
//        if (e.type == RenderGameOverlayEvent.ElementType.ALL) {
//            if (main.getUtils().isOnSkyblock()) {
//                if (main.configValues!!.isEnabled(Feature.HIDE_FOOD_ARMOR_BAR)) {
//                    GuiIngameForge.renderFood = false
//                    GuiIngameForge.renderArmor = false
//                }
//                if (main.configValues!!.isEnabled(Feature.HIDE_HEALTH_BAR)) {
//                    GuiIngameForge.renderHealth = false
//                }
//                if (main.configValues!!.isEnabled(Feature.HIDE_PET_HEALTH_BAR)) {
//                    GuiIngameForge.renderHealthMount = false
//                }
//            } else {
//                if (main.configValues!!.isEnabled(Feature.HIDE_HEALTH_BAR)) {
//                    GuiIngameForge.renderHealth = true
//                }
//                if (main.configValues!!.isEnabled(Feature.HIDE_FOOD_ARMOR_BAR)) {
//                    GuiIngameForge.renderArmor = true
//                }
//            }
//        }
//    }
//
//    @SubscribeEvent
//    fun onRender(e: RenderTickEvent?) {
//        if (guiToOpen == EnumUtils.GUIType.MAIN) {
//            Minecraft.getMinecraft().displayGuiScreen(SkyblockAddonsGui(guiPageToOpen, guiTabToOpen))
//        } else if (guiToOpen == EnumUtils.GUIType.EDIT_LOCATIONS) {
//            Minecraft.getMinecraft().displayGuiScreen(LocationEditGui(guiPageToOpen, guiTabToOpen))
//        } else if (guiToOpen == EnumUtils.GUIType.SETTINGS) {
//            if (guiFeatureToOpen == Feature.ENCHANTMENT_LORE_PARSING) {
//                Minecraft.getMinecraft().displayGuiScreen(
//                    EnchantmentSettingsGui(
//                        guiFeatureToOpen,
//                        1,
//                        guiPageToOpen,
//                        guiTabToOpen,
//                        guiFeatureToOpen!!.getSettings()
//                    )
//                )
//            } else {
//                Minecraft.getMinecraft().displayGuiScreen(
//                    SettingsGui(
//                        guiFeatureToOpen,
//                        1,
//                        guiPageToOpen,
//                        guiTabToOpen,
//                        guiFeatureToOpen!!.getSettings()
//                    )
//                )
//            }
//        } else if (guiToOpen == EnumUtils.GUIType.WARP) {
//            Minecraft.getMinecraft().displayGuiScreen(IslandWarpGui())
//        }
//        guiToOpen = null
//    }
//
//
    fun setGuiToOpen(guiToOpen: EnumUtils.GUIType?) {
        this.guiToOpen = guiToOpen
    }
//
    fun setGuiToOpen(guiToOpen: EnumUtils.GUIType?, page: Int, tab: EnumUtils.GuiTab) {
        this.guiToOpen = guiToOpen
        guiPageToOpen = page
        guiTabToOpen = tab
    }

    fun setGuiToOpen(guiToOpen: EnumUtils.GUIType?, page: Int, tab: EnumUtils.GuiTab, feature: Feature?) {
        setGuiToOpen(guiToOpen, page, tab)
        guiFeatureToOpen = feature
    }
//
    fun setSubtitleFeature(subtitleFeature: Feature?) {
        this.subtitleFeature = subtitleFeature
    }
//
    fun transformXY(xy: Float, widthHeight: Int, scale: Float): Float {
        var xy = xy
        val minecraftScale: Float = ScaledResolution(Minecraft.getMinecraft()).getScaleFactor().toFloat()
        xy -= widthHeight / 2f * scale
        xy = Math.round(xy * minecraftScale) / minecraftScale
        return xy / scale
    }
//
//    @SubscribeEvent
//    fun onRenderWorld(e: RenderWorldLastEvent) {
//        val mc: Minecraft = Minecraft.getMinecraft()
//        val partialTicks: Float = e.partialTicks
//
//        HealingCircleManager.renderHealingCircleOverlays(partialTicks)
//
//        if (main.getUtils().isOnSkyblock() && main.getUtils().isInDungeon() &&
//            (main.configValues!!.isEnabled(Feature.SHOW_CRITICAL_DUNGEONS_TEAMMATES) || main.configValues!!
//                .isEnabled(
//                    Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY
//                ))
//        ) {
//            val renderViewEntity: Entity = mc.getRenderViewEntity()
//
//            val viewPosition: Vector3d = Utils.getPlayerViewPosition()
//
//            val iconSize = 25
//
//            for (entity in mc.theWorld.playerEntities) {
//                if (renderViewEntity === entity) {
//                    continue
//                }
//
//                if (!main.getDungeonManager().getTeammates().containsKey(entity.getName())) {
//                    continue
//                }
//
//                val dungeonPlayer: DungeonPlayer = main.getDungeonManager().getTeammates().get(entity.getName())
//
//                var x = MathUtils.interpolateX(entity, partialTicks)
//                var y = MathUtils.interpolateY(entity, partialTicks)
//                var z = MathUtils.interpolateZ(entity, partialTicks)
//
//                x -= viewPosition.x
//                y -= viewPosition.y
//                z -= viewPosition.z
//
//                if (main.configValues!!.isEnabled(Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY)) {
//                    y += 0.35
//                }
//
//                if (entity.isSneaking()) {
//                    y -= 0.65
//                }
//
//                val distanceScale =
//                    max(1.0, renderViewEntity.positionVector.distanceTo(entity.getPositionVector()) / 10f)
//
//                if (main.configValues!!.isEnabled(Feature.MAKE_DUNGEON_TEAMMATES_GLOW)) {
//                    y += entity.height + 0.75f + (iconSize * distanceScale) / 40f
//                } else {
//                    y += (entity.height / 2f + 0.25f).toDouble()
//                }
//
//                val f = 1.6f
//                val f1 = 0.016666668f * f
//                GlStateManager.pushMatrix()
//                GlStateManager.translate(x, y, z)
//                GL11.glNormal3f(0.0f, 1.0f, 0.0f)
//                GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f)
//                GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f)
//                GlStateManager.scale(-f1, -f1, f1)
//
//                GlStateManager.scale(distanceScale, distanceScale, distanceScale)
//
//                GlStateManager.disableLighting()
//                GlStateManager.depthMask(false)
//                GlStateManager.disableDepth()
//                GlStateManager.enableBlend()
//                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
//                GlStateManager.enableTexture2D()
//                GlStateManager.color(1f, 1f, 1f, 1f)
//                GlStateManager.enableAlpha()
//
//                if (main.configValues!!.isEnabled(Feature.SHOW_CRITICAL_DUNGEONS_TEAMMATES)
//                    && (!dungeonPlayer.isGhost() && (dungeonPlayer.isCritical() || dungeonPlayer.isLow()))
//                ) {
//                    val tessellator: Tessellator = Tessellator.getInstance()
//                    val worldrenderer: WorldRenderer = tessellator.getWorldRenderer()
//
//                    mc.getTextureManager().bindTexture(CRITICAL)
//                    worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
//                    worldrenderer.pos((-iconSize / 2f).toDouble(), (-iconSize / 2f).toDouble(), 0.0).tex(0.0, 0.0)
//                        .endVertex()
//                    worldrenderer.pos((-iconSize / 2f).toDouble(), (iconSize / 2f).toDouble(), 0.0).tex(0.0, 1.0)
//                        .endVertex()
//                    worldrenderer.pos((iconSize / 2f).toDouble(), (iconSize / 2f).toDouble(), 0.0).tex(1.0, 1.0)
//                        .endVertex()
//                    worldrenderer.pos((iconSize / 2f).toDouble(), (-iconSize / 2f).toDouble(), 0.0).tex(1.0, 0.0)
//                        .endVertex()
//                    tessellator.draw()
//
//                    var text = ""
//                    if (dungeonPlayer.isLow()) {
//                        text = "LOW"
//                    } else if (dungeonPlayer.isCritical()) {
//                        text = "CRITICAL"
//                    }
//
//                    mc.fontRendererObj.drawString(
//                        text,
//                        -mc.fontRendererObj.getStringWidth(text) / 2f,
//                        iconSize / 2f + 2,
//                        -1,
//                        true
//                    )
//                }
//
//                if (!dungeonPlayer.isGhost() && main.configValues!!
//                        .isEnabled(Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY)
//                ) {
//                    val nameOverlay: String = (ColorCode.YELLOW.toString() + "[" + dungeonPlayer.getDungeonClass()
//                        .getFirstLetter()).toString() + "] " + ColorCode.GREEN + entity.getName()
//                    mc.fontRendererObj.drawString(
//                        nameOverlay,
//                        -mc.fontRendererObj.getStringWidth(nameOverlay) / 2f,
//                        iconSize / 2f + 13,
//                        -1,
//                        true
//                    )
//                }
//
//                GlStateManager.enableDepth()
//                GlStateManager.depthMask(true)
//                GlStateManager.enableLighting()
//                GlStateManager.disableBlend()
//                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
//                GlStateManager.popMatrix()
//            }
//        }
//    }
//
//    private fun drawPowerOrbArmorStand(powerOrbArmorStand: EntityArmorStand, x: Float, y: Float) {
//        val prevRenderYawOffset: Float = powerOrbArmorStand.renderYawOffset
//        val prevPrevRenderYawOffset: Float = powerOrbArmorStand.prevRenderYawOffset
//
//        GlStateManager.pushMatrix()
//
//        GlStateManager.enableDepth()
//        GlStateManager.enableColorMaterial()
//
//        GlStateManager.translate(x + 12.5f, y + 50f, 50f)
//        GlStateManager.scale(-25f, 25f, 25f)
//        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f)
//        GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f)
//        RenderHelper.enableStandardItemLighting()
//        GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f)
//        GlStateManager.rotate(22.0f, 1.0f, 0.0f, 0.0f)
//
//        val rendermanager: RenderManager = Minecraft.getMinecraft().getRenderManager()
//        rendermanager.setPlayerViewY(180.0f)
//        val shadowsEnabled = rendermanager.isRenderShadow
//        rendermanager.isRenderShadow = false
//
//        powerOrbArmorStand.setInvisible(true)
//        val yaw = System.currentTimeMillis() % 1750 / 1750f * 360f
//        powerOrbArmorStand.renderYawOffset = yaw
//        powerOrbArmorStand.prevRenderYawOffset = yaw
//
//        rendermanager.renderEntityWithPosYaw(powerOrbArmorStand, 0.0, 0.0, 0.0, 0.0f, 1.0f)
//        rendermanager.isRenderShadow = shadowsEnabled
//
//        RenderHelper.disableStandardItemLighting()
//        GlStateManager.disableRescaleNormal()
//        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit)
//        GlStateManager.disableTexture2D()
//        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit)
//
//        GlStateManager.popMatrix()
//
//        powerOrbArmorStand.renderYawOffset = prevRenderYawOffset
//        powerOrbArmorStand.prevRenderYawOffset = prevPrevRenderYawOffset
//    }
//
//    private fun drawEntity(entity: EntityLivingBase, x: Float, y: Float, yaw: Float) {
//        GlStateManager.pushMatrix()
//
//        GlStateManager.enableDepth()
//        GlStateManager.translate(x, y, 50f)
//        GlStateManager.scale(-25f, 25f, 25f)
//        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f)
//        GlStateManager.rotate(15f, 1f, 0f, 0f)
//        RenderHelper.enableGUIStandardItemLighting()
//
//        entity.renderYawOffset = yaw
//        entity.prevRenderYawOffset = yaw
//        entity.rotationYawHead = yaw
//        entity.prevRotationYawHead = yaw
//
//        val rendermanager: RenderManager = Minecraft.getMinecraft().getRenderManager()
//        rendermanager.setPlayerViewY(180.0f)
//        val shadowsEnabled = rendermanager.isRenderShadow
//        rendermanager.isRenderShadow = false
//        rendermanager.renderEntityWithPosYaw(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f)
//        rendermanager.isRenderShadow = shadowsEnabled
//
//        RenderHelper.disableStandardItemLighting()
//        GlStateManager.disableRescaleNormal()
//        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit)
//        GlStateManager.disableTexture2D()
//        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit)
//        GlStateManager.popMatrix()
//    }
//
//    val radiantDummyArmorStand: EntityArmorStand?
//        get() {
//            if (Companion.radiantDummyArmorStand != null) {
//                return Companion.radiantDummyArmorStand
//            }
//
//            Companion.radiantDummyArmorStand =
//                EntityArmorStand(Utils.getDummyWorld())
//
//            val orbItemStack: ItemStack = ItemUtils.createSkullItemStack(
//                null,
//                null,
//                "3ae3572b-2679-40b4-ba50-14dd58cbbbf7",
//                "7ab4c4d6ee69bc24bba2b8faf67b9f704a06b01aa93f3efa6aef7a9696c4feef"
//            )
//
//            Companion.radiantDummyArmorStand.setCurrentItemOrArmor(4, orbItemStack)
//
//            return Companion.radiantDummyArmorStand
//        }
//
    companion object {
        private val BONE_ITEM: ItemStack = ItemStack(Items.bone)
        private val BARS = ResourceLocation("skyblockaddons", "barsV2.png")
        private val DEFENCE_VANILLA = ResourceLocation("skyblockaddons", "defence.png")
        private val TICKER_SYMBOL = ResourceLocation("skyblockaddons", "ticker.png")

        private val ENDERMAN_ICON = ResourceLocation("skyblockaddons", "icons/enderman.png")
        private val ENDERMAN_GROUP_ICON = ResourceLocation("skyblockaddons", "icons/endermangroup.png")
        private val SIRIUS_ICON = ResourceLocation("skyblockaddons", "icons/sirius.png")
        private val SUMMONING_EYE_ICON = ResourceLocation("skyblockaddons", "icons/summoningeye.png")
        private val ZEALOTS_PER_EYE_ICON = ResourceLocation("skyblockaddons", "icons/zealotspereye.png")
        private val SLASH_ICON = ResourceLocation("skyblockaddons", "icons/slash.png")
        private val IRON_GOLEM_ICON = ResourceLocation("skyblockaddons", "icons/irongolem.png")
        private val FARM_ICON = ResourceLocation("skyblockaddons", "icons/farm.png")

        private val CRITICAL = ResourceLocation("skyblockaddons", "critical.png")

        private val WATER_BUCKET: ItemStack = ItemStack(Items.water_bucket)
        private val IRON_SWORD: ItemStack = ItemStack(Items.iron_sword)
        private val WARP_SKULL: ItemStack = ItemUtils.createSkullItemStack(
            "§bFast Travel",
            null,
            "9ae837fc-19da-3841-af06-7db55d51c815",
            "c9c8881e42915a9d29bb61a16fb26d059913204d265df5b439b3d792acd56"
        )
        private val SKYBLOCK_MENU: ItemStack =
            ItemUtils.createItemStack(Items.nether_star, "§aSkyBlock Menu §7(Right Click)", "SKYBLOCK_MENU", false)
        private val PET_ROCK: ItemStack = ItemUtils.createSkullItemStack(
            "§f§f§7[Lvl 100] §6Rock",
            null,
            "1ed7c993-8190-3055-a48c-f70f71b17284",
            "cb2b5d48e57577563aca31735519cb622219bc058b1f34648b67b8e71bc0fa"
        )
        private val DOLPHIN_PET: ItemStack = ItemUtils.createSkullItemStack(
            "§f§f§7[Lvl 100] §6Dolphin",
            null,
            "48f53ffe-a3f0-3280-aac0-11cc0d6121f4",
            "cefe7d803a45aa2af1993df2544a28df849a762663719bfefc58bf389ab7f5"
        )
        private val CHEST: ItemStack = ItemStack(Item.getItemFromBlock(Blocks.chest))
        private val SKULL: ItemStack = ItemUtils.createSkullItemStack(
            "Skull",
            null,
            "c659cdd4-e436-4977-a6a7-d5518ebecfbb",
            "1ae3855f952cd4a03c148a946e3f812a5955ad35cbcb52627ea4acd47d3081"
        )
        private val HYPERION: ItemStack = ItemUtils.createItemStack(Items.iron_sword, "§6Hyperion", "HYPERION", false)
        private val VALKYRIE: ItemStack = ItemUtils.createItemStack(Items.iron_sword, "§6Valkyrie", "VALKYRIE", false)
        private val ASTRAEA: ItemStack = ItemUtils.createItemStack(Items.iron_sword, "§6Astraea", "ASTRAEA", false)
        private val SCYLLA: ItemStack = ItemUtils.createItemStack(Items.iron_sword, "§6Scylla", "SCYLLA", false)
        private val SCPETRE: ItemStack =
            ItemStack(Blocks.red_flower, 1, 2) //doesnt show sb texture pack cos blocks cant have and idk how

        private val GREEN_CANDY: ItemStack = ItemUtils.createSkullItemStack(
            "Green Candy",
            "GREEN_CANDY",
            "0961dbb3-2167-3f75-92e4-ec8eb4f57e55",
            "ce0622d01cfdae386cc7dd83427674b422f46d0a57e67a20607e6ca4b9af3b01"
        )
        private val PURPLE_CANDY: ItemStack = ItemUtils.createSkullItemStack(
            "Purple Candy",
            "PURPLE_CANDY",
            "5b0e6bf0-6312-3476-b5f8-dbc9a8849a1f",
            "95d7aee4e97ad84095f55405ee1305d1fc8554c309edb12a1db863cde9c1ec80"
        )

        private val DUMMY_PROGRESSES: Array<SlayerArmorProgress?> = arrayOf<SlayerArmorProgress?>(
            SlayerArmorProgress(
                ItemStack(
                    Items.diamond_boots
                )
            ), SlayerArmorProgress(ItemStack(Items.chainmail_leggings)), SlayerArmorProgress(
                ItemStack(
                    Items.diamond_chestplate
                )
            ), SlayerArmorProgress(ItemStack(Items.leather_helmet))
        )

        private var radiantDummyArmorStand: EntityArmorStand? = null
        private var revenant: EntityZombie? = null
        private var tarantula: EntitySpider? = null
        private var caveSpider: EntityCaveSpider? = null
        private var sven: EntityWolf? = null
        private var enderman: EntityEnderman? = null

        private val DUMMY_PICKUP_LOG: List<ItemDiff> = ArrayList(
            Arrays.asList(
                ItemDiff(ColorCode.DARK_PURPLE.toString() + "Forceful Ember Chestplate", 1),
                ItemDiff("Boat", -1), ItemDiff(ColorCode.BLUE.toString() + "Aspect of the End", 1)
            )
        )
    }
}
