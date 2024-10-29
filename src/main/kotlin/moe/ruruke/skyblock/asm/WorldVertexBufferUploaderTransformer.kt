//package moe.ruruke.skyblock.asm
//
//import moe.ruruke.skyblock.asm.utils.InjectionHelper
//import moe.ruruke.skyblock.tweaker.transformer.ITransformer
//import org.objectweb.asm.tree.ClassNode
//
//class WorldVertexBufferUploaderTransformer : ITransformer {
//    override var className: Array<String?>? = arrayOf()
//        /**
//         * [net.minecraft.client.renderer.WorldVertexBufferUploader]
//         */
//        get() = arrayOf<String>(TransformerClass.WorldVertexBufferUploader.transformerName)
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
