package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*


class EffectRendererTransformer : ITransformer {

    /**
     * [net.minecraft.client.particle.EffectRenderer]
     */
    override fun getClassName(): Array<String> {
        return arrayOf(TransformerClass.EffectRenderer.getTransformerName())
    }

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (TransformerMethod.addEffect.matches(methodNode)) {
                methodNode.instructions.insertBefore(methodNode.instructions.first, insertOnAddParticle())
            } else if (TransformerMethod.renderParticles.matches(methodNode)) {
                var last_depthFunc: AbstractInsnNode? = null

                val iterator: Iterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()

                    if (abstractNode is MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKESTATIC &&
                        abstractNode.owner == TransformerClass.GlStateManager.getNameRaw() &&
                        abstractNode.name == TransformerMethod.depthMask.name
                    ) {
                        last_depthFunc = abstractNode
                    } else if (last_depthFunc != null &&
                        abstractNode is InsnNode && abstractNode.getOpcode() == Opcodes.RETURN
                    ) {
                        methodNode.instructions.insertBefore(last_depthFunc.previous, insertAfterRenderParticles())
                    }
                }
            }
        }
    }

    private fun insertOnAddParticle(): InsnList {
        val list = InsnList()

        list.add(VarInsnNode(Opcodes.ALOAD, 1)) // effect

        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,  // EffectRendererHook.onAddParticle(effect);
                "moe/ruruke/skyblock/asm/hooks/EffectRendererHook",
                "onAddParticle",
                "(" + TransformerClass.EntityFX.getName() + ")V",
                false
            )
        )
        return list
    }

    private fun insertAfterRenderParticles(): InsnList {
        val list = InsnList()

        list.add(VarInsnNode(Opcodes.FLOAD, 2)) // partialTicks
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,  // EffectRendererHook.renderParticleOverlays(partialTicks);
                "moe/ruruke/skyblock/asm/hooks/EffectRendererHook", "renderParticleOverlays", "(F)V", false
            )
        )

        return list
    }

    override fun nameMatches(method: String, vararg names: String): Boolean {
        return super.nameMatches(method, *names)
    }
}