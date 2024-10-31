//package moe.ruruke.skyblock.asm
//
//import moe.ruruke.skyblock.asm.hooks.utils.InjectionHelper
//import moe.ruruke.skyblock.tweaker.transformer.ITransformer
//import org.objectweb.asm.tree.ClassNode
//
//class WorldVertexBufferUploaderTransformer : ITransformer {
//    override fun getClassName(): Array<String> {
//        return className
//    }
//        /**
//         * [net.minecraft.client.renderer.WorldVertexBufferUploader]
//         */
//        get() = arrayOf<String>(TransformerClass.WorldVertexBufferUploader.getTransformerName())
//
//    override fun transform(classNode: ClassNode?, name: String?) {
//        for (methodNode in classNode!!.methods) {
//            if (InjectionHelper.matches(methodNode, TransformerMethod.draw)) {
//                InjectionHelper.start()
//                    .matchMethodHead()
//
//                    .startCode()
//                    .callStaticMethod(
//                        "moe/ruruke/skyblock/asm/hooks/WorldVertexBufferUploaderHook",
//                        "onRenderWorldRendererBuffer",
//                        "()Z"
//                    )
//                    .startIfEqual()
//                    .reeturn()
//                    .endIf()
//                    .endCode()
//
//                    .finish()
//                return
//            }
//        }
//    }
//}
