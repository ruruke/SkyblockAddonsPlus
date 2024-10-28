package moe.ruruke.skyblock.tweaker

import net.minecraftforge.common.ForgeVersion
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion

@MCVersion(ForgeVersion.mcVersion)
class SkyblockAddonsLoadingPlugin : IFMLLoadingPlugin {
    override fun getASMTransformerClass(): Array<String> {
        return arrayOf(SkyblockAddonsTransformer::class.java.name)
    }

    override fun getModContainerClass(): String? {
        return null
    }

    override fun getSetupClass(): String {
        return SkyblockAddonsDuplicateChecker::class.java.name
    }

    override fun injectData(data: Map<String, Any>) {
        coremodList = data["coremodList"] as List<Any>?
    }

    override fun getAccessTransformerClass(): String? {
        return null
    }

    companion object {
        var coremodList: List<Any>? = null
    }
}