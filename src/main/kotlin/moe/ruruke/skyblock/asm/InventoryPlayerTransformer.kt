package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode

class InventoryPlayerTransformer : ITransformer {
    /**
     * [net.minecraft.entity.player.InventoryPlayer]
     */
    override fun getClassName(): Array<String> {
        return  arrayOf(TransformerClass.InventoryPlayer.getTransformerName())
    }

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (TransformerMethod.changeCurrentItem.matches(methodNode)) {
                // Objective:
                // Find: Method head.
                // Insert: MinecraftHook.updatedCurrentItem();

                methodNode.instructions.insert(
                    MethodInsnNode(
                        Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/MinecraftHook",
                        "updatedCurrentItem", "()V", false
                    )
                ) // MinecraftHook.updatedCurrentItem();
            }
        }
    }
}
