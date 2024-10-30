package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

class ItemTransformer : ITransformer {
    override var className: Array<String> = arrayOf()
        /**
         * [net.minecraft.item.Item]
         */
        get() = arrayOf(TransformerClass.Item.transformerName)

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) { // Loop through all methods inside of the class.

            val methodName = methodNode.name
            if (nameMatches(methodName, "showDurabilityBar")) { // always deobfuscated

                // Objective:
                // Find: return stack.isItemDamaged();
                // Replace With: return ItemHook.isItemDamaged(stack);

                val iterator: MutableIterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()
                    if (abstractNode is MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        val methodInsnNode = abstractNode
                        if (methodInsnNode.owner == TransformerClass.ItemStack.nameRaw && TransformerMethod.isItemDamaged.matches(
                                methodInsnNode
                            )
                        ) {
                            methodNode.instructions.insertBefore(
                                abstractNode, MethodInsnNode(
                                    Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/ItemHook",
                                    "isItemDamaged", "(" + TransformerClass.ItemStack.getName() + ")Z", false
                                )
                            ) // ItemHook.isItemDamaged(stack);

                            iterator.remove() // Remove the old line.
                            break
                        }
                    }
                }
            }
            if (nameMatches(methodName, "getDurabilityForDisplay")) { // always deobfuscated

                // Objective:
                // Find: Method head.
                // Insert:   ReturnValue returnValue = new ReturnValue();
                //           ItemHook.getDurabilityForDisplay(stack, returnValue);
                //           if (returnValue.isCancelled()) {
                //               return returnValue.getValue();
                //           }

                methodNode.instructions.insertBefore(methodNode.instructions.first, insertDurabilityHook())
            }
        }
    }

    private fun insertDurabilityHook(): InsnList {
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
        list.add(VarInsnNode(Opcodes.ASTORE, 2))

        list.add(VarInsnNode(Opcodes.ALOAD, 1)) // stack
        list.add(VarInsnNode(Opcodes.ALOAD, 2)) // ItemHook.getDurabilityForDisplay(stack, returnValue);
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "moe/ruruke/skyblock/asm/hooks/ItemHook",
                "getDurabilityForDisplay",
                "(" + TransformerClass.ItemStack.getName() + "Lmoe/ruruke/skyblock/asm/utils/ReturnValue;)V",
                false
            )
        )

        list.add(VarInsnNode(Opcodes.ALOAD, 2))
        list.add(
            MethodInsnNode(
                Opcodes.INVOKEVIRTUAL, "moe/ruruke/skyblock/asm/utils/ReturnValue", "isCancelled",
                "()Z", false
            )
        )
        val notCancelled = LabelNode() // if (returnValue.isCancelled())
        list.add(JumpInsnNode(Opcodes.IFEQ, notCancelled))

        list.add(VarInsnNode(Opcodes.ALOAD, 2))
        list.add(
            MethodInsnNode(
                Opcodes.INVOKEVIRTUAL, "moe/ruruke/skyblock/asm/utils/ReturnValue", "getReturnValue",
                "()Ljava/lang/Object;", false
            )
        )
        list.add(TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Double"))
        list.add(
            MethodInsnNode(
                Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue",
                "()D", false
            )
        )
        list.add(InsnNode(Opcodes.DRETURN)) // return returnValue.getValue();
        list.add(notCancelled)

        return list
    }
}
