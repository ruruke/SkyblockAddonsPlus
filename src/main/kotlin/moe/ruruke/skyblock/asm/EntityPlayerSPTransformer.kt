package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

class EntityPlayerSPTransformer : ITransformer {
    override var className: Array<String> = arrayOf()
        /**
         * [net.minecraft.client.entity.EntityPlayerSP]
         */
        get() = arrayOf(TransformerClass.EntityPlayerSP.transformerName)

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            // Objective:
            // Find: Method head.
            // Insert:   ReturnValue returnValue = new ReturnValue();
            //           EntityPlayerSPHook.dropOneItemConfirmation(returnValue);
            //           if (returnValue.isCancelled()) {
            //               return null;
            //           }

            if (TransformerMethod.dropOneItem.matches(methodNode)) {
                methodNode.instructions.insertBefore(methodNode.instructions.first, insertConfirmation())
            }
        }
    }

    private fun insertConfirmation(): InsnList {
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
        list.add(VarInsnNode(Opcodes.ASTORE, 3))

        list.add(VarInsnNode(Opcodes.ALOAD, 3)) // EntityPlayerSPHook.dropOneItemConfirmation(returnValue);
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "moe/ruruke/skyblock/asm/hooks/EntityPlayerSPHook",
                "dropOneItemConfirmation",
                "(Lmoe/ruruke/skyblock/asm/utils/ReturnValue;)" + TransformerClass.EntityItem.getName(),
                false
            )
        )

        list.add(VarInsnNode(Opcodes.ALOAD, 3))
        list.add(
            MethodInsnNode(
                Opcodes.INVOKEVIRTUAL, "moe/ruruke/skyblock/asm/utils/ReturnValue", "isCancelled",
                "()Z", false
            )
        )
        val notCancelled = LabelNode() // if (returnValue.isCancelled())
        list.add(JumpInsnNode(Opcodes.IFEQ, notCancelled))

        list.add(InsnNode(Opcodes.ACONST_NULL)) // return null;
        list.add(InsnNode(Opcodes.ARETURN))
        list.add(notCancelled)

        return list
    }
}
