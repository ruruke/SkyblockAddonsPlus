//package moe.ruruke.skyblock.asm
//
//import moe.ruruke.skyblock.tweaker.transformer.ITransformer
//import org.objectweb.asm.Opcodes
//import org.objectweb.asm.tree.*
//
//class FontRendererTransformer : ITransformer {
//    override fun getClassName(): Array<String> {
//        return className
//    }
//        /**
//         * [net.minecraft.client.gui.FontRenderer]
//         */
//        get() = arrayOf<String>(
//            TransformerClass.FontRenderer.getTransformerName(),
//            "club.sk1er.patcher.hooks.FontRendererHook"
//        )
//
//    override fun transform(classNode: ClassNode?, name: String?) {
//        for (methodNode in classNode!!.methods) {
//            if (classNode.name == "club/sk1er/patcher/hooks/FontRendererHook") {
//                if (methodNode.name == "renderStringAtPos" && methodNode.desc == "(Ljava/lang/String;Z)Z") {
//                    // Objective:
//                    // Find Method Head: Add:
//                    //   if (FontRendererHook.shouldOverridePatcher(text)) return false;
//                    methodNode.instructions.insertBefore(methodNode.instructions.first, patcherOverride())
//                }
//                continue
//            }
//
//            // Objective:
//            // Find Method Head: Add:
//            //   FontRendererHook.changeTextColor(); <- insert the call right before the return
//            if (TransformerMethod.renderChar.matches(methodNode) || methodNode.name == "renderChar") {
//                methodNode.instructions.insertBefore(methodNode.instructions.first, insertChangeTextColor())
//            } else if (TransformerMethod.renderStringAtPos.matches(methodNode)) {
//                var fieldInsnNode: FieldInsnNode
//                var nextNode: AbstractInsnNode
//
//                val elseIf22Start = LabelNode()
//                var nextElseIf: LabelNode? = null
//
//                // In vanilla, this is always the last on the style list
//                var italicStyleCount = 0
//                var findString = false
//                var insertedChroma = false
//                var findIfEq20 = false
//
//                val iterator: Iterator<AbstractInsnNode> = methodNode.instructions.iterator()
//                while (iterator.hasNext()) {
//                    val abstractNode = iterator.next()
//
//                    // Add the §z color code in the 22nd index
//                    if (!findString &&
//                        abstractNode is LdcInsnNode && abstractNode.getOpcode() == Opcodes.LDC &&
//                        abstractNode.cst is String && abstractNode.cst == "0123456789abcdefklmnor"
//                    ) {
//                        abstractNode.cst = "0123456789abcdefklmnorz"
//                        findString = true
//                    } else if (findString &&
//                        abstractNode is FieldInsnNode && (abstractNode.also {
//                            fieldInsnNode = it
//                        }).opcode == Opcodes.PUTFIELD &&
//                        fieldInsnNode.owner == TransformerClass.FontRenderer.getNameRaw() && fieldInsnNode.name == TransformerField.italicStyle.getName()
//                    ) {
//                        italicStyleCount++
//                        // Insert a chroma reset, as the new format code is between 0 and 15 (regular colors)
//                        if (italicStyleCount == 1 || italicStyleCount == 3) {
//                            methodNode.instructions.insert(abstractNode, insertRestoreChromaState())
//                        }
//                    } else if (findString && !findIfEq20 &&
//                        abstractNode is VarInsnNode && abstractNode.getOpcode() == Opcodes.ILOAD && abstractNode.`var` == 5 &&
//                        (abstractNode.getNext().also {
//                            nextNode = it
//                        }) is IntInsnNode && nextNode.opcode == Opcodes.BIPUSH && (nextNode as IntInsnNode).operand == 20 &&
//                        (nextNode.next.also { nextNode = it }) is JumpInsnNode && nextNode.opcode == Opcodes.IF_ICMPNE
//                    ) {
//                        nextElseIf = (nextNode as JumpInsnNode).label
//                        (nextNode as JumpInsnNode).label = elseIf22Start
//                        findIfEq20 = true
//                    } else if (findIfEq20 && !insertedChroma &&
//                        abstractNode is JumpInsnNode && abstractNode.getOpcode() == Opcodes.GOTO
//                    ) {
//                        methodNode.instructions.insert(
//                            abstractNode,
//                            checkChromaToggleOn(elseIf22Start, nextElseIf, abstractNode.label)
//                        )
//                        insertedChroma = true
//                    } else if (insertedChroma && abstractNode is InsnNode && abstractNode.getOpcode() == Opcodes.RETURN) {
//                        methodNode.instructions.insertBefore(abstractNode, insertEndOfString())
//                        //methodNode.instructions.insertBefore(abstractNode, saveStringChroma());
//                    }
//                }
//                // Only do this if the initial injection was successful
//                if (insertedChroma) {
//                    // Insert a call to FontRendererHook.beginRenderString(shadow) as the first instruction
//                    methodNode.instructions.insertBefore(methodNode.instructions.first, insertBeginRenderString())
//                }
//            }
//        }
//    }
//
//    /**
//     * Inserts a call to [FontRendererHook.changeTextColor]
//     */
//    private fun insertChangeTextColor(): InsnList {
//        val list = InsnList()
//
//        list.add(
//            MethodInsnNode(
//                Opcodes.INVOKESTATIC,
//                "moe/ruruke/skyblock/asm/hooks/FontRendererHook",
//                "changeTextColor",
//                "()V",
//                false
//            )
//        )
//
//        return list
//    }
//
//    /**
//     * Inserts a call to [FontRendererHook.beginRenderString]
//     */
//    private fun insertBeginRenderString(): InsnList {
//        val list = InsnList()
//
//        // FontRendererHook.beginRenderString(shadow);
//        list.add(VarInsnNode(Opcodes.ILOAD, 2)) // shadow
//        list.add(
//            MethodInsnNode(
//                Opcodes.INVOKESTATIC,
//                "moe/ruruke/skyblock/asm/hooks/FontRendererHook",
//                "beginRenderString",
//                "(Z)V",
//                false
//            )
//        )
//
//        return list
//    }
//
//    /**
//     * Skips patcher's optimized font renderer if the call to [FontRendererHook.shouldOverridePatcher] returns true
//     */
//    private fun patcherOverride(): InsnList {
//        val list = InsnList()
//
//        // if (FontRendererHook.shouldOverridePatcher(text)) return false;
//        list.add(VarInsnNode(Opcodes.ALOAD, 1))
//        list.add(
//            MethodInsnNode(
//                Opcodes.INVOKESTATIC,
//                "moe/ruruke/skyblock/asm/hooks/FontRendererHook",
//                "shouldOverridePatcher",
//                "(Ljava/lang/String;)Z",
//                false
//            )
//        )
//        val endIf = LabelNode()
//        list.add(JumpInsnNode(Opcodes.IFEQ, endIf))
//        list.add(InsnNode(Opcodes.ICONST_0))
//        list.add(InsnNode(Opcodes.IRETURN))
//        list.add(endIf)
//
//        return list
//    }
//
//    /**
//     * Inserts a call to [FontRendererHook.restoreChromaState]
//     */
//    private fun insertRestoreChromaState(): InsnList {
//        val list = InsnList()
//
//        list.add(
//            MethodInsnNode(
//                Opcodes.INVOKESTATIC,
//                "moe/ruruke/skyblock/asm/hooks/FontRendererHook",
//                "restoreChromaState",
//                "()V",
//                false
//            )
//        )
//
//        return list
//    }
//
//    /**
//     * Inserts a call to [FontRendererHook.endRenderString]
//     */
//    private fun insertEndOfString(): InsnList {
//        val list = InsnList()
//
//        list.add(
//            MethodInsnNode(
//                Opcodes.INVOKESTATIC,
//                "moe/ruruke/skyblock/asm/hooks/FontRendererHook",
//                "endRenderString",
//                "()V",
//                false
//            )
//        )
//
//        return list
//    }
//
//
//    /**
//     * Insert instructions on line 419:
//     * else if (i1 == 22) {
//     * this.resetStyles();
//     * [FontRendererHook.toggleChromaOn]
//     * }
//     */
//    private fun checkChromaToggleOn(startIf: LabelNode, elseIf: LabelNode?, endIf: LabelNode): InsnList {
//        val list = InsnList()
//
//        list.add(startIf)
//        list.add(FrameNode(Opcodes.F_SAME, 0, null, 0, null))
//        // else if (i1 == 22) {}
//        list.add(VarInsnNode(Opcodes.ILOAD, 5))
//        list.add(IntInsnNode(Opcodes.BIPUSH, 22))
//        list.add(JumpInsnNode(Opcodes.IF_ICMPNE, elseIf))
//
//        // this.resetStyles()
//        list.add(VarInsnNode(Opcodes.ALOAD, 0)) // this
//        list.add(
//            MethodInsnNode(
//                Opcodes.INVOKESPECIAL,
//                TransformerClass.FontRenderer.getNameRaw(),
//                TransformerMethod.resetStyles.getName(),
//                TransformerMethod.resetStyles.description,
//                false
//            )
//        )
//
//        // Call shader manager
//        list.add(
//            MethodInsnNode(
//                Opcodes.INVOKESTATIC,
//                "moe/ruruke/skyblock/asm/hooks/FontRendererHook",
//                "toggleChromaOn",
//                "()V",
//                false
//            )
//        )
//
//        // Go to end of else if chain
//        list.add(JumpInsnNode(Opcodes.GOTO, endIf))
//
//        return list
//    }
//}