package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.hooks.utils.TransformerClass
import moe.ruruke.skyblock.asm.hooks.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.MethodInsnNode

class RenderEndermanTransformer : ITransformer {
    override var className: Array<String> = arrayOf()
        /**
         * [net.minecraft.client.renderer.entity.RenderEnderman]
         */
        get() = arrayOf(TransformerClass.RenderEnderman.transformerName)

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (TransformerMethod.getEntityTexture_RenderEnderman.matches(methodNode)) {
                // Objective:
                // Find: return endermanTextures;
                // Change to: return RenderEndermanHook.getEndermanTexture(endermanTextures);

                val iterator: Iterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()
                    if (abstractNode is InsnNode && abstractNode.getOpcode() == Opcodes.ARETURN) {
                        methodNode.instructions.insertBefore(
                            abstractNode, MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "moe/ruruke/skyblock/asm/hooks/RenderEndermanHook",
                                "getEndermanTexture",
                                "(" + TransformerClass.ResourceLocation.getName() + ")" + TransformerClass.ResourceLocation.getName(),
                                false
                            )
                        )
                        break
                    }
                }
            }
        }
    }
}