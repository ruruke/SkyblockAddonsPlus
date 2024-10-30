package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

class ModelEndermanTransformer : ITransformer {
    override var className: Array<String> = arrayOf()
        /**
         * [net.minecraft.client.model.ModelEnderman]
         */
        get() = arrayOf(TransformerClass.ModelEnderman.transformerName)

    override fun transform(classNode: ClassNode?, name: String?) {
        // Objective: Add:
        //
        // @Override
        // public render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale) {
        //     ModelEndermanHook.setEndermanColor();
        //     super.render(entityIn, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale);
        // }

        val updateScreen = TransformerMethod.render.createMethodNode()
        updateScreen.instructions.add(render())
        classNode!!.methods.add(updateScreen)
    }

    private fun render(): InsnList {
        val list = InsnList()

        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/ModelEndermanHook", "setEndermanColor",
                "()V", false
            )
        ) // ModelEndermanHook.setEndermanColor();

        list.add(VarInsnNode(Opcodes.ALOAD, 0))
        list.add(VarInsnNode(Opcodes.ALOAD, 1))
        for (`var` in 2..7) { // Load all 6 float parameters
            list.add(VarInsnNode(Opcodes.FLOAD, `var`))
        }
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESPECIAL, TransformerClass.ModelBiped.nameRaw, TransformerMethod.render.getName(),
                TransformerMethod.render.description, false
            )
        ) // super.render(entityIn, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale);

        list.add(InsnNode(Opcodes.RETURN))
        return list
    }
}
