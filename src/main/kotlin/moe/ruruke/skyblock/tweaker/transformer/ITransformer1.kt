package moe.ruruke.skyblock.tweaker.transformer

import org.objectweb.asm.tree.ClassNode

interface ITransformer {
    val className: Array<String?>?

    fun transform(classNode: ClassNode?, name: String?)

    fun nameMatches(method: String, vararg names: String): Boolean {
        for (name in names) if (method == name) return true

        return false
    }
}
