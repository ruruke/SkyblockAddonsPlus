package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.hooks.utils.TransformerClass
import moe.ruruke.skyblock.asm.hooks.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodInsnNode

class TileEntityEnderChestRendererTransformer : ITransformer {
    override var className: Array<String> = arrayOf()
        /**
         * [net.minecraft.client.renderer.tileentity.TileEntityEnderChestRenderer]
         */
        get() = arrayOf(TransformerClass.TileEntityEnderChestRenderer.transformerName)

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (TransformerMethod.renderTileEntityAt.matches(methodNode)) {
                // Objective:
                // Find: this.bindTexture(ENDER_CHEST_TEXTURE);
                // Replacement: TileEntityEnderChestRendererHook.bindTexture(this, (ResourceLocation)ENDER_CHEST_TEXTURE);

                // Objective:
                // Find: this.field_147521_c.renderAll();
                // Insert 2 lines before: TileEntityEnderChestRendererHook.setEnderchestColor();

                var bindTextureCount = 0

                val iterator: MutableIterator<AbstractInsnNode> = methodNode.instructions.iterator()

                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()

                    if (abstractNode is MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        val methodInsnNode = abstractNode
                        if (methodInsnNode.owner == TransformerClass.TileEntityEnderChestRenderer.nameRaw
                            && TransformerMethod.bindTexture.matches(methodInsnNode)
                        ) { // TileEntityEnderChestRendererHook.bindTexture(ENDER_CHEST_TEXTURE);
                            if (bindTextureCount == 1) { // Find the second statement, not the first one.
                                methodNode.instructions.insertBefore(
                                    abstractNode, MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "moe/ruruke/skyblock/asm/hooks/TileEntityEnderChestRendererHook",  // Add TileEntityEnderChestRendererHook.bindTexture(this, (ResourceLocation)ENDER_CHEST_TEXTURE);
                                        "bindTexture",
                                        "(" + TransformerClass.TileEntityEnderChestRenderer.getName() + TransformerClass.ResourceLocation.getName() + ")V",
                                        false
                                    )
                                )
                                iterator.remove() // Remove the old method call.
                            }
                            bindTextureCount++
                        } else if (methodInsnNode.owner == TransformerClass.ModelChest.nameRaw
                            && TransformerMethod.renderAll.matches(methodInsnNode)
                        ) { // The two lines are to make sure its before the "this" & the "field_147521_c".
                            methodNode.instructions.insertBefore(
                                methodNode.instructions[methodNode.instructions.indexOf(
                                    abstractNode
                                ) - 2], insertChangeEnderchestColor()
                            )
                        }
                    }
                }
                break
            }
        }
    }

    private fun insertChangeEnderchestColor(): InsnList {
        val list = InsnList()

        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "moe/ruruke/skyblock/asm/hooks/TileEntityEnderChestRendererHook",
                "setEnderchestColor",
                "()V",
                false
            )
        ) // TileEntityEnderChestRendererHook.setEnderchestColor();

        return list
    }
}
