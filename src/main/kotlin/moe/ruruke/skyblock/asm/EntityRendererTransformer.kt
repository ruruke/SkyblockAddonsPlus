package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.hooks.utils.InjectionHelper
import moe.ruruke.skyblock.asm.hooks.utils.TransformerClass
import moe.ruruke.skyblock.asm.hooks.utils.TransformerField
import moe.ruruke.skyblock.asm.hooks.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.tree.ClassNode

class EntityRendererTransformer : ITransformer {
    override var className: Array<String> = arrayOf()
        /**
         * [net.minecraft.client.renderer.EntityRenderer]
         */
        get() = arrayOf(TransformerClass.EntityRenderer.transformerName)

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (InjectionHelper.matches(methodNode, TransformerMethod.getNightVisionBrightness)) {
                InjectionHelper.start()
                    .matchMethodHead()

                    .startCode() // ReturnValue returnValue = new ReturnValue();
                    .newInstance("moe/ruruke/skyblock/asm/utils/ReturnValue")
                    .storeAuto(0) // TODO Reference local variable by name maybe? "returnValue"?
                    // EntityRendererHook.onGetNightVisionBrightness(returnValue);

                    .loadAuto(0)
                    .callStaticMethod(
                        "moe/ruruke/skyblock/asm/hooks/EntityRendererHook", "onGetNightVisionBrightness",
                        "(Lmoe/ruruke/skyblock/asm/utils/ReturnValue;)V"
                    ) // if (returnValue.isCancelled())

                    .loadAuto(0)
                    .invokeInstanceMethod("moe/ruruke/skyblock/asm/utils/ReturnValue", "isCancelled", "()Z")
                    .startIfEqual() // return 1.0F;
                    .constantValue(1.0f)
                    .reeturn() // }
                    .endIf()
                    .endCode()
                    .finish()
            } else if (InjectionHelper.matches(methodNode, TransformerMethod.updateCameraAndRender)) {
                InjectionHelper.start() // Match at: if (this.mc.currentScreen != null)
                    .matchingOwner(TransformerClass.Minecraft).matchingField(TransformerField.currentScreen)
                    .endCondition() // Inject before the if statement (2 instructions above)
                    .setInjectionOffset(-2) // 6 lines backwards should be: this.renderEndNanoTime = System.nanoTime();
                    .addAnchorCondition(-6).matchingOwner(TransformerClass.EntityRenderer)
                    .matchingField(TransformerField.renderEndNanoTime).endCondition()

                    .injectCodeBefore() // EntityRendererHook.onRenderScreenPre();
                    .callStaticMethod(
                        "moe/ruruke/skyblock/asm/hooks/EntityRendererHook",
                        "onRenderScreenPre",
                        "()V"
                    )
                    .endCode()
                    .finish()
            }
        }
    }
}
