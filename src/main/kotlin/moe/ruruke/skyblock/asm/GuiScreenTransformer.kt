package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

class GuiScreenTransformer : ITransformer {
    override var className: Array<String?>? = arrayOf()
        /**
         * [net.minecraft.client.gui.GuiScreen]
         */
        get() = arrayOf(TransformerClass.GuiScreen.transformerName)

    override fun transform(classNode: ClassNode?, name: String?) {
        try {
            for (methodNode in classNode!!.methods) {
                if (TransformerMethod.renderToolTip.matches(methodNode)) {
                    // Objective:
                    // Find: Method head.
                    // Insert:   if (GuiScreenHook.onRenderTooltip(stack, x, y)) {
                    //               return;
                    //           }

                    methodNode.instructions.insertBefore(methodNode.instructions.first, onRenderTooltip())
                }
                if (TransformerMethod.handleComponentClick.matches(methodNode)) {
                    // Objective:
                    // Find: Method head.
                    // Insert: GuiScreenHook.handleComponentClick(component);

                    methodNode.instructions.insertBefore(methodNode.instructions.first, insertComponentClick())
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun onRenderTooltip(): InsnList {
        val list = InsnList()

        list.add(VarInsnNode(Opcodes.ALOAD, 1)) // stack
        list.add(VarInsnNode(Opcodes.ILOAD, 2)) // x
        list.add(VarInsnNode(Opcodes.ILOAD, 3)) // y
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/GuiScreenHook", "onRenderTooltip",
                "(" + TransformerClass.ItemStack.getName() + "II)Z", false
            )
        )
        val notCancelled = LabelNode()
        list.add(JumpInsnNode(Opcodes.IFEQ, notCancelled)) // if (GuiScreenHook.onRenderTooltip(stack, x, y)) {
        list.add(InsnNode(Opcodes.RETURN)) // return;
        list.add(notCancelled) // }

        return list
    }

    private fun insertComponentClick(): InsnList {
        val list = InsnList()

        list.add(VarInsnNode(Opcodes.ALOAD, 1)) // component
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/GuiScreenHook", "handleComponentClick",
                "(" + TransformerClass.IChatComponent.getName() + ")V", false
            )
        ) // GuiScreenHook.handleComponentClick(component);

        return list
    }
}
