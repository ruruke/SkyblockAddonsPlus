package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

class RenderManagerTransformer : ITransformer {
    /**
     * [net.minecraft.client.renderer.entity.RenderManager]
     */
    override fun getClassName(): Array<String> {
        return  arrayOf(TransformerClass.RenderManager.getTransformerName())
    }

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (TransformerMethod.shouldRender.matches(methodNode)) {
                // Objective:
                // Find: Method head.
                // Insert:   ReturnValue returnValue = new ReturnValue();
                //           RenderManagerHook.shouldRender(entityIn, returnValue);
                //           if (returnValue.isCancelled()) {
                //               return false;
                //           }

                methodNode.instructions.insertBefore(methodNode.instructions.first, insertShouldRender())
            }
        }
    }

    private fun insertShouldRender(): InsnList {
        val list = InsnList()

        list.add(TypeInsnNode(Opcodes.NEW, "moe/ruruke/skyblock/asm/utils/ReturnValue"))
        list.add(InsnNode(Opcodes.DUP)) // ReturnValue returnValue = new ReturnValue();
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                "moe/ruruke/skyblock/asm/utils/ReturnValue",
                "<init>",
                "()V",
                false
            )
        )
        list.add(VarInsnNode(Opcodes.ASTORE, 10))

        list.add(VarInsnNode(Opcodes.ALOAD, 1)) // entityIn
        list.add(VarInsnNode(Opcodes.ALOAD, 10)) //RenderManagerHook.shouldRender(entityIn, returnValue);
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "moe/ruruke/skyblock/asm/hooks/RenderManagerHook",
                "shouldRender",
                "(" + TransformerClass.Entity.getName() + "Lmoe/ruruke/skyblock/asm/utils/ReturnValue;)V",
                false
            )
        )

        list.add(VarInsnNode(Opcodes.ALOAD, 10))
        list.add(
            MethodInsnNode(
                Opcodes.INVOKEVIRTUAL, "moe/ruruke/skyblock/asm/utils/ReturnValue", "isCancelled",
                "()Z", false
            )
        )
        val notCancelled = LabelNode() // if (returnValue.isCancelled())
        list.add(JumpInsnNode(Opcodes.IFEQ, notCancelled))

        list.add(InsnNode(Opcodes.ICONST_0)) // return false;
        list.add(InsnNode(Opcodes.IRETURN))
        list.add(notCancelled)

        return list
    }
}
