package moe.ruruke.skyblock.shader.chroma

/**
 * This shader shows a chroma color on a pixel depending on its position on the screen.
 *
 * This shader does:
 * - Work with textures (see [ChromaScreenShader] for a non-textured version).
 * - Preserve the brightness and saturation of the original color (for text shadows)
 *
 * This shader does not:
 * - Take in account world position, scale, etc.
 */
class ChromaScreenTexturedShader : ChromaShader("chroma_screen_textured")
