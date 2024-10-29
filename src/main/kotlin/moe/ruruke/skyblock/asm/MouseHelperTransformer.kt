package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode

class MouseHelperTransformer : ITransformer {
    override var className: Array<String?>? = arrayOf()
        /**
         * [net.minecraft.util.MouseHelper]
         */
        get() = arrayOf(TransformerClass.MouseHelper.transformerName)

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (TransformerMethod.ungrabMouseCursor.matches(methodNode)) {
                // Objective:
                // Find: Mouse.setCursorPosition(MouseHelperHook.ungrabMouseCursor() / 2, Display.getHeight() / 2);
                // Replace method with: MouseHelperHook.ungrabMouseCursor(Display.getWidth() / 2, Display.getHeight() / 2);

                val iterator: MutableIterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()
                    if (abstractNode is MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKESTATIC) {
                        val methodInsnNode = abstractNode
                        if (methodInsnNode.owner == "org/lwjgl/input/Mouse" && methodInsnNode.name == "setCursorPosition") { // these are not minecraft methods, not obfuscated

                            methodNode.instructions.insertBefore(
                                abstractNode, MethodInsnNode(
                                    Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/MouseHelperHook",
                                    "ungrabMouseCursor", "(II)V", false
                                )
                            ) // Add the replacement method call.
                            iterator.remove() // Remove the old method call.
                            break
                        }
                    }
                }
                break
            }
        }
    }
}
