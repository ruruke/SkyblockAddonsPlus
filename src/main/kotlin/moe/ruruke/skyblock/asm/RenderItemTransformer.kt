package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode

class RenderItemTransformer : ITransformer {
    override var className: Array<String?>? = arrayOf()
        /**
         * [net.minecraft.client.renderer.entity.RenderItem]
         */
        get() = arrayOf(TransformerClass.RenderItem.transformerName)

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            // Objective:
            //
            // Find:
            //   this.renderItem(stack, model);
            //
            // Add after:
            //   RenderItemHook.renderToxicArrowPoisonEffect(model, stack);


            if (TransformerMethod.renderItem.matches(methodNode)) {
                val iterator: Iterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()

                    if (abstractNode is MethodInsnNode && (abstractNode.getOpcode() == Opcodes.INVOKESPECIAL || abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL)) {
                        if (TransformerMethod.renderModel_IBakedModel_ItemStack.matches(abstractNode)) {
                            methodNode.instructions.insert(
                                abstractNode, MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "moe/ruruke/skyblock/asm/hooks/RenderItemHook",
                                    "renderToxicArrowPoisonEffect",
                                    "(" + TransformerClass.IBakedModel.getName() + TransformerClass.ItemStack.getName() + ")V",
                                    false
                                )
                            )
                            methodNode.instructions.insert(abstractNode, VarInsnNode(Opcodes.ALOAD, 1))
                            methodNode.instructions.insert(abstractNode, VarInsnNode(Opcodes.ALOAD, 2))
                            break
                        }
                    }
                }
            }
        }
    }
}
