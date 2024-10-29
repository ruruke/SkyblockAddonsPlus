package moe.ruruke.skyblock.asm.utils

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldInsnNode

object ASMUtils {
    fun getField(field: TransformerField, targetClass: TransformerClass): FieldInsnNode {
        return FieldInsnNode(Opcodes.GETFIELD, targetClass.nameRaw, field.getName(), field.getType())
    }
}
