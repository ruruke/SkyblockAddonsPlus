package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode

class SoundManagerTransformer : ITransformer {
    override var className: Array<String?>? = arrayOf()
        /**
         * [net.minecraft.client.audio.SoundManager]
         */
        get() = arrayOf(TransformerClass.SoundManager.transformerName)

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (TransformerMethod.playSound.matches(methodNode)) {
                // Objective:
                // Find: this.getNormalizedVolume(p_sound, soundpoolentry, soundcategory);
                // Replace method with: SoundManagerHook.getNormalizedVolume(this, p_sound, soundpoolentry, soundcategory);

                val iterator: MutableIterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()
                    if (abstractNode is MethodInsnNode) {
                        val methodInsnNode = abstractNode
                        if (nameMatches(
                                methodInsnNode.owner,
                                TransformerClass.SoundManager.nameRaw!!
                            ) && TransformerMethod.getNormalizedVolume.matches(methodInsnNode)
                        ) {
                            methodNode.instructions.insertBefore(
                                abstractNode, MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "moe/ruruke/skyblock/asm/hooks/SoundManagerHook",
                                    "getNormalizedVolume",
                                    "(" + TransformerClass.SoundManager.getName() + TransformerClass.ISound.getName() + TransformerClass.SoundPoolEntry.getName() + TransformerClass.SoundCategory.getName() + ")F",
                                    false
                                )
                            ) // Add SoundManagerHook.getNormalizedVolume(this, p_sound, soundpoolentry, soundcategory);

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
