package moe.ruruke.skyblock.core

import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack


enum class SkillType(skillName: String, item: net.minecraft.item.Item, isCosmetic: Boolean) {
    FARMING("Farming", net.minecraft.init.Items.golden_hoe, false),
    MINING("Mining", net.minecraft.init.Items.diamond_pickaxe, false),
    COMBAT("Combat", net.minecraft.init.Items.iron_sword, false),
    FORAGING("Foraging", net.minecraft.item.Item.getItemFromBlock(Blocks.sapling), false),
    FISHING("Fishing", net.minecraft.init.Items.fishing_rod, false),
    ENCHANTING("Enchanting", net.minecraft.item.Item.getItemFromBlock(Blocks.enchanting_table), false),
    ALCHEMY("Alchemy", net.minecraft.init.Items.brewing_stand, false),
    CARPENTRY("Carpentry", net.minecraft.item.Item.getItemFromBlock(Blocks.crafting_table), true),
    RUNECRAFTING("Runecrafting", net.minecraft.init.Items.magma_cream, true),
    TAMING("Taming", net.minecraft.init.Items.spawn_egg, false),
    DUNGEONEERING("Dungeoneering", net.minecraft.item.Item.getItemFromBlock(Blocks.deadbush), false),
    SOCIAL("Social", net.minecraft.init.Items.cake, true);

    private val skillName: String?

    private val item: ItemStack

    private val cosmetic: Boolean

    init {
        this.skillName = skillName
        this.item = ItemStack(item)
        this.cosmetic = isCosmetic
    }

    companion object {
        fun getFromString(text: String): SkillType? {
            for (skillType: SkillType in entries) {
                if (skillType.skillName != null && skillType.skillName == text) {
                    return skillType
                }
            }
            return null
        }
    }
}
