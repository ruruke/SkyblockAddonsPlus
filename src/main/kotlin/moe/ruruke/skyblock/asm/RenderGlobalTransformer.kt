package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.hooks.RenderGlobalHook
import moe.ruruke.skyblock.asm.hooks.utils.TransformerClass
import moe.ruruke.skyblock.asm.hooks.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

class RenderGlobalTransformer : ITransformer {
    private var existingLabel: LabelNode? = null
    private val newLabel = LabelNode()

    override var className: Array<String> = arrayOf()
        /**
         * [net.minecraft.client.renderer.RenderGlobal]
         */
        get() = arrayOf<String>(TransformerClass.RenderGlobal.transformerName)

    /**
     * See [RenderGlobalHook.blockRenderingSkyblockItemOutlines],
     * [RenderGlobalHook.afterFramebufferDraw], [RenderGlobalHook.onAddBlockBreakParticle], and
     * [RenderGlobalHook.shouldRenderSkyblockItemOutlines])
     */
    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (TransformerMethod.renderEntities.matches(methodNode)) {
                val iterator: Iterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()
                    if (abstractNode is MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        if (TransformerMethod.isRenderEntityOutlines.matches(abstractNode)) {
                            if (abstractNode.getNext() is JumpInsnNode && abstractNode.getNext().opcode == Opcodes.IFEQ) {
                                val jumpInsnNode = abstractNode.getNext() as JumpInsnNode

                                existingLabel = jumpInsnNode.label
                                methodNode.instructions.insertBefore(
                                    abstractNode.getPrevious(),
                                    shouldRenderEntityOutlinesExtraCondition(newLabel)
                                )
                            }
                        }
                    }

                    if (newLabel != null && abstractNode is LabelNode) {
                        if (abstractNode === existingLabel) {
                            methodNode.instructions.insertBefore(abstractNode, newLabel)
                        }
                    }
                }
            } else if (TransformerMethod.isRenderEntityOutlines.matches(methodNode)) {
                methodNode.instructions.insertBefore(methodNode.instructions.first, loadInFrameBuffers())
            } else if (TransformerMethod.renderEntityOutlineFramebuffer.matches(methodNode)) {
                val iterator: Iterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()
                    if (abstractNode is InsnNode && abstractNode.getOpcode() == Opcodes.RETURN) {
                        methodNode.instructions.insertBefore(
                            abstractNode, MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "moe/ruruke/skyblock/asm/hooks/RenderGlobalHook",
                                "afterFramebufferDraw",
                                "()V",
                                false
                            )
                        )
                        break
                    }
                }
            } else if (TransformerMethod.sendBlockBreakProgress.matches(methodNode)) {
                methodNode.instructions.insertBefore(methodNode.instructions.first, insertOnAddBlockBreakParticle())
            }
        }
    }

    private fun insertOnAddBlockBreakParticle(): InsnList {
        val list = InsnList()

        list.add(VarInsnNode(Opcodes.ILOAD, 1))
        list.add(VarInsnNode(Opcodes.ALOAD, 2))
        list.add(VarInsnNode(Opcodes.ILOAD, 3))
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "moe/ruruke/skyblock/asm/hooks/RenderGlobalHook",
                "onAddBlockBreakParticle",
                "(I" + TransformerClass.BlockPos.getName() + "I)V",
                false
            )
        )
        return list
    }

    private fun shouldRenderEntityOutlinesExtraCondition(labelNode: LabelNode): InsnList {
        val list = InsnList()

        list.add(VarInsnNode(Opcodes.ALOAD, 2)) // camera
        list.add(VarInsnNode(Opcodes.FLOAD, 3)) // partialTicks
        list.add(VarInsnNode(Opcodes.DLOAD, 5)) // x
        list.add(VarInsnNode(Opcodes.DLOAD, 7)) // y
        list.add(VarInsnNode(Opcodes.DLOAD, 9)) // z
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "moe/ruruke/skyblock/asm/hooks/RenderGlobalHook",
                "blockRenderingSkyblockItemOutlines",
                "(" + TransformerClass.ICamera.getName() + "FDDD)Z",
                false
            )
        )
        list.add(JumpInsnNode(Opcodes.IFEQ, labelNode))

        return list
    }

    private fun loadInFrameBuffers(): InsnList {
        val list = InsnList()

        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "moe/ruruke/skyblock/asm/hooks/RenderGlobalHook",
                "shouldRenderSkyblockItemOutlines",
                "()Z",
                false
            )
        )
        val notCancelled = LabelNode() // if (RenderGlobalHook.shouldRenderSkyblockItemOutlines())
        list.add(JumpInsnNode(Opcodes.IFEQ, notCancelled))

        list.add(InsnNode(Opcodes.ICONST_1)) // return true;
        list.add(InsnNode(Opcodes.IRETURN))
        list.add(notCancelled)

        return list
    }
}