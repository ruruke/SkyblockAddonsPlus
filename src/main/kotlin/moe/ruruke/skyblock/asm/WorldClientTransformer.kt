package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode

class WorldClientTransformer : ITransformer {
    /**
     * [net.minecraft.client.multiplayer.WorldClient]
     */
    override fun getClassName(): Array<String> {
        return  arrayOf(TransformerClass.WorldClient.getTransformerName())
    }

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (TransformerMethod.onEntityRemoved.matches(methodNode)) {
                methodNode.instructions.insertBefore(methodNode.instructions.first, onEntityRemoved())
            } else if (TransformerMethod.invalidateRegionAndSetBlock.matches(methodNode)) {
                methodNode.instructions.insertBefore(methodNode.instructions.first, insertBlockUpdated())
            }
        }
    }

    private fun onEntityRemoved(): InsnList {
        val list = InsnList()

        list.add(VarInsnNode(Opcodes.ALOAD, 1)) // entityIn
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/WorldClientHook", "onEntityRemoved",
                "(" + TransformerClass.Entity.getName() + ")V", false
            )
        ) // WorldClientHook.onEntityRemoved(entityIn);

        return list
    }

    private fun insertBlockUpdated(): InsnList {
        val list = InsnList()

        list.add(VarInsnNode(Opcodes.ALOAD, 1)) // pos
        list.add(VarInsnNode(Opcodes.ALOAD, 2)) // state
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/WorldClientHook", "blockUpdated",
                "(" + TransformerClass.BlockPos.getName() + TransformerClass.IBlockState.getName() + ")V", false
            )
        ) // WorldClientHook.blockUpdated(pos, state);

        return list
    }
}