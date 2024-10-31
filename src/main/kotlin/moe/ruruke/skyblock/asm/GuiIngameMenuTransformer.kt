package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerField
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

class GuiIngameMenuTransformer : ITransformer {
    /**
     * [net.minecraft.client.gui.GuiIngameMenu]
     */
    override fun getClassName(): Array<String> {
        return arrayOf(TransformerClass.GuiIngameMenu.getTransformerName())
    }

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (TransformerMethod.actionPerformed.matches(methodNode)) {
                // Objective 1:
                // Find: boolean flag = this.mc.isIntegratedServerRunning();
                // Insert Before: GuiDisconnectedHook.onDisconnect();

                // Objective 2:
                // Find: Head of actionPerformed.
                // Insert:
                // if (button.id == 53) {
                //     GuiIngameMenuHook.onButtonClick();
                // }

                methodNode.instructions.insertBefore(methodNode.instructions.first, insertOnButtonClick())

                val iterator: Iterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()
                    if (abstractNode is MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        val methodInsnNode = abstractNode
                        if (methodInsnNode.owner == TransformerClass.Minecraft.getNameRaw() &&
                            TransformerMethod.isIntegratedServerRunning.matches(methodInsnNode)
                        ) {
                            // Go two backwards because of this & this.mc.

                            methodNode.instructions.insertBefore(
                                abstractNode.getPrevious().previous, MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "moe/ruruke/skyblock/asm/hooks/GuiDisconnectedHook",
                                    "onDisconnect",
                                    "()V",
                                    false
                                )
                            ) // GuiDisconnectedHook.onDisconnect();
                        }
                    }
                }
            } else if (TransformerMethod.initGui.matches(methodNode)) {
                // Objective:
                // Find: initGui() return.
                // Insert: GuiIngameMenuHook.addMenuButtons(this.buttonsList, this.width, this.height);

                val iterator: Iterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()
                    if (abstractNode is InsnNode && abstractNode.getOpcode() == Opcodes.RETURN) {
                        methodNode.instructions.insertBefore(abstractNode, insertAddMenuButtons())
                    }
                }
            }
        }
    }

    private fun insertAddMenuButtons(): InsnList {
        val list = InsnList()

        list.add(VarInsnNode(Opcodes.ALOAD, 0)) // this.buttonsList
        list.add(TransformerField.buttonList.getField(TransformerClass.GuiIngameMenu))

        list.add(VarInsnNode(Opcodes.ALOAD, 0)) // this.width
        list.add(TransformerField.width.getField(TransformerClass.GuiIngameMenu))

        list.add(VarInsnNode(Opcodes.ALOAD, 0)) // this.height
        list.add(TransformerField.height.getField(TransformerClass.GuiIngameMenu))

        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,  // GuiIngameMenuHook.addMenuButtons(this.buttonsList, this.width, this.height);
                "moe/ruruke/skyblock/asm/hooks/GuiIngameMenuHook",
                "addMenuButtons",
                "(Ljava/util/List;II)V",
                false
            )
        )
        return list
    }

    private fun insertOnButtonClick(): InsnList {
        val list = InsnList()

        list.add(VarInsnNode(Opcodes.ALOAD, 1)) // button

        list.add(TransformerField.id.getField(TransformerClass.GuiButton)) // button.id

        list.add(IntInsnNode(Opcodes.BIPUSH, 53))
        val labelNode = LabelNode()
        list.add(
            JumpInsnNode(
                Opcodes.IF_ICMPNE,
                labelNode
            )
        ) // Jump to the label after the statement if they are not equal.

        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/GuiIngameMenuHook",
                "onButtonClick", "()V", false
            )
        ) // GuiIngameMenuHook.onButtonClick();
        list.add(labelNode)

        return list
    }
}
