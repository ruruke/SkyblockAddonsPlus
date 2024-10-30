package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.ASMUtils.getField
import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerField
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.VarInsnNode

class EntityPlayerTransformer : ITransformer {
    override var className: Array<String> = arrayOf()
        /**
         * [net.minecraft.entity.player.EntityPlayer]
         */
        get() = arrayOf(TransformerClass.EntityPlayer.transformerName)

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            // Objective:
            // Replace the entire method to fix a forge bug...

            if (TransformerMethod.setCurrentItemOrArmor.matches(methodNode)) {
                methodNode.instructions = setCurrentItemOrArmor()
            }
        }
    }

    private fun setCurrentItemOrArmor(): InsnList {
        val list = InsnList()

        // this.inventory.armorInventory[slotIn] = stack;
        list.add(VarInsnNode(Opcodes.ALOAD, 0)) // this
        list.add(getField(TransformerField.inventory, TransformerClass.EntityPlayer)) // this.inventory
        list.add(
            getField(
                TransformerField.armorInventory,
                TransformerClass.InventoryPlayer
            )
        ) // inventory.armorInventory
        list.add(VarInsnNode(Opcodes.ILOAD, 1)) // slotIn
        list.add(VarInsnNode(Opcodes.ALOAD, 2)) // stack
        list.add(InsnNode(Opcodes.AASTORE))
        list.add(InsnNode(Opcodes.RETURN))

        return list
    }
}
