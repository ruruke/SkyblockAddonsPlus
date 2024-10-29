package codes.biscuit.skyblockaddons.asm.utils

import codes.biscuit.skyblockaddons.asm.utils.InjectionHelper.InstructionMatcher.InstructionMatcherFunction
import com.google.common.collect.Sets
import lombok.AccessLevel
import lombok.Getter
import lombok.Setter
import lombok.experimental.Accessors
import moe.ruruke.skyblock.asm.utils.InstructionBuilder
import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerField
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import org.objectweb.asm.tree.*
import java.util.*
import java.util.function.Consumer


@Accessors(chain = true)
@Setter(value = AccessLevel.PRIVATE)
class InjectionHelper {
    private var method: TransformerMethod? = null
    private var methodNode: MethodNode? = null
    private val condition = InjectionPoint()
    private val anchorConditions: MutableMap<Int, InjectionPoint> =
        TreeMap() // Instruction Offset -> Condition Injection Point
    private var injectionPosition: InjectionPosition? = null


    @Setter
    private var injectionOffset = 0

    @Setter(value = AccessLevel.PUBLIC)
    private var instructions: InsnList? = null
    private var instructionConsumer: Consumer<AbstractInsnNode>? = null

    fun setMethodNode(_methodNode: MethodNode): InjectionHelper {
        methodNode = _methodNode
        return this
    }
    fun setMethod(_method: TransformerMethod): InjectionHelper {
        method = _method
        return this
    }
    fun addAnchorCondition(offset: Int): InjectionPoint {
        if (offset == 0) {
            return condition
        } else {
            val condition = InjectionPoint()
            anchorConditions[offset] = condition
            return condition
        }
    }
    fun setInstructionConsumer(consumer: Consumer<AbstractInsnNode>) {
        instructionConsumer = consumer
    }
    fun setInjectionPosition(_injectionPosition: InjectionPosition?) {
        injectionPosition = _injectionPosition
    }
    fun setInstructions(inst: InsnList?): InjectionHelper {
        instructions = inst
        return this
    }

    fun consumeForEach(instructionConsumer: Consumer<AbstractInsnNode>): InjectionHelper {
        setInstructionConsumer(instructionConsumer)
        return this
    }

    fun startCode(): InstructionBuilder {
        return injectCodeBefore()
    }

    fun injectCodeBefore(): InstructionBuilder {
        return startCode(InjectionPosition.BEFORE)
    }

    fun injectCodeAfter(): InstructionBuilder {
        return startCode(InjectionPosition.AFTER)
    }
    fun setInjectionPosition(point: InjectionPosition){
        injectionPosition = point
    }

    private fun startCode(injectionPosition: InjectionPosition): InstructionBuilder {
        setInjectionPosition(injectionPosition)
        return InstructionBuilder.start(methodNode)
    }

    fun finish(): Boolean {
//        if (!matches()) {
//            return false;
//        }

        if (instructionConsumer != null) {
            val iterator: Iterator<AbstractInsnNode> = methodNode!!.instructions.iterator()
            while (iterator.hasNext()) {
                val abstractNode = iterator.next()
                instructionConsumer!!.accept(abstractNode)
            }
            return true
        } else if (instructions != null) {
            val injectionPointNode = findInjectionPoint() ?: return false

            if (injectionPosition == InjectionPosition.BEFORE) {
                methodNode!!.instructions.insertBefore(injectionPointNode, instructions)
            } else if (injectionPosition == InjectionPosition.AFTER) {
                methodNode!!.instructions.insert(injectionPointNode, instructions)
            }
            return true
        }

        return false
    }

    fun matches(): Boolean {
        if (methodNode == null) {
            return false
        }

        return method!!.matches(methodNode!!)
    }

    fun getCondition(): InjectionPoint {
        return condition
    }

    private fun findInjectionPoint(): AbstractInsnNode? {
        if (condition == null) {
            return null
        }

        if (condition.getType() == MatchType.HEAD) {
            return methodNode!!.instructions.first
        } else if (condition.getType() == MatchType.REGULAR) {
            val iterator: Iterator<AbstractInsnNode> = methodNode!!.instructions.iterator()
            while (iterator.hasNext()) {
                var instruction: AbstractInsnNode? = iterator.next()
                if (!matchesCondition(condition, instruction) || !matchesAnchorConditions(instruction)) {
                    continue
                }

                instruction = getOffsetInstruction(instruction, this.injectionOffset)
                if (instruction == null) {
                    continue
                }

                return instruction
            }
        }

        return null
    }

    private fun matchesCondition(injectionPoint: InjectionPoint, instruction: AbstractInsnNode?): Boolean {
        return injectionPoint.matches(instruction)
    }

    private fun matchesAnchorConditions(originalInstruction: AbstractInsnNode?): Boolean {
        for ((conditionOffset, condition1) in anchorConditions) {
            val instruction = getOffsetInstruction(originalInstruction, conditionOffset) ?: return false

            if (!matchesCondition(condition, instruction)) {
                return false
            }
        }

        return true
    }

    private fun getOffsetInstruction(instruction: AbstractInsnNode?, offset: Int): AbstractInsnNode? {
        var offset = offset
        if (offset == 0) {
            return instruction
        }

        var conditionInstruction = instruction
        while (offset < 0) {
            conditionInstruction = conditionInstruction!!.previous
            if (conditionInstruction == null) {
                return null
            }
            offset++
        }
        while (offset > 0) {
            conditionInstruction = conditionInstruction!!.next
            if (conditionInstruction == null) {
                return null
            }
            offset--
        }

        return conditionInstruction
    }

    fun clear(): InjectionHelper {
        this.method = null
        this.methodNode = null
        condition.clear()
        anchorConditions.clear()
        this.injectionPosition = null
        this.injectionOffset = 0
        return this
    }

    class InjectionPoint {
        private var type: MatchType? = null
        fun getType(): MatchType {
            return type!!
        }
        private val opcodeMatcher = InstructionMatcher<Int> { instruction: AbstractInsnNode, matchAgainst: Int -> matchAgainst == instruction.opcode }
        private val fieldMatcher =
            InstructionMatcher { instruction: AbstractInsnNode, matchAgainst: TransformerField ->
                instruction is FieldInsnNode && matchAgainst.matches(
                    instruction
                )
            }
        private val methodMatcher =
            InstructionMatcher { instruction: AbstractInsnNode?, matchAgainst: TransformerMethod ->
                instruction is MethodInsnNode && matchAgainst.matches(
                    instruction
                )
            }
        private val localVarMatcher =
            InstructionMatcher { instruction: AbstractInsnNode?, matchAgainst: Int -> instruction is VarInsnNode && matchAgainst === instruction.`var` }
        private val ownerMatcher = InstructionMatcher(
            InstructionMatcherFunction { instruction: AbstractInsnNode?, matchAgainst: TransformerClass ->
                if (instruction is FieldInsnNode) {
                    return@InstructionMatcherFunction matchAgainst.nameRaw == instruction.owner
                } else if (instruction is MethodInsnNode) {
                    return@InstructionMatcherFunction matchAgainst.nameRaw == instruction.owner
                }
                false
            })

        @Getter
        private val matchers: Set<InstructionMatcher<*>> = Sets.newHashSet<InstructionMatcher<*>>(
            ownerMatcher,
            opcodeMatcher,
            fieldMatcher,
            methodMatcher,
            localVarMatcher
        )

        fun clear() {
            this.type = MatchType.REGULAR
            matchers.forEach(Consumer { obj: InstructionMatcher<*> -> obj.reset() })
        }

        fun matches(instruction: AbstractInsnNode?): Boolean {
            for (instructionMatcher in matchers) {
                if (instructionMatcher.isEnabled() && !instructionMatcher.matches(instruction!!)) {
                    return false
                }
            }

            return true
        }

        fun matchMethodHead(): InjectionHelper {
            type = MatchType.HEAD
            return endCondition()
        }

        fun matchingOwner(clazz: TransformerClass): InjectionPoint {
            ownerMatcher.setValue(clazz)
            return this
        }

        fun matchingMethod(method: TransformerMethod): InjectionPoint {
            methodMatcher.setValue(method)
            return this
        }

        fun matchingField(field: TransformerField): InjectionPoint {
            fieldMatcher.setValue(field)
            return this
        }

        fun matchingOpcode(opcode: Int): InjectionPoint {
            opcodeMatcher.setValue(opcode)
            return this
        }

        fun matchingLocalVarNumber(localVarNumber: Int): InjectionPoint {
            localVarMatcher.setValue(localVarNumber)
            return this
        }

        fun endCondition(): InjectionHelper {
            return INSTANCE
        }
    }

    class InstructionMatcher<T>(private val matchesFunction: InstructionMatcherFunction<T>) {
        private var enabled = false
        private var value: T? = null

        fun isEnabled(): Boolean {
            return enabled
        }

        fun matches(instruction: AbstractInsnNode): Boolean {
            return matchesFunction.matches(instruction, value!!)
        }

        fun setValue(value: T) {
            this.value = value
            this.enabled = true
        }

        fun reset() {
            enabled = false
        }


        fun interface InstructionMatcherFunction<T> {
            fun matches(instruction: AbstractInsnNode, matchAgainst: T): Boolean
        }
    }


    enum class MatchType {
        HEAD,
        REGULAR
    }

    enum class InjectionPosition {
        BEFORE,
        AFTER
    }

    companion object {
        private val INSTANCE = InjectionHelper()

        fun matches(methodNode: MethodNode?, method: TransformerMethod?): Boolean {
            return INSTANCE.clear().setMethodNode(methodNode!!).setMethod(method!!).matches()
        }

        fun start(): InjectionPoint {
            return INSTANCE.condition
        }

        fun resume(): InjectionHelper {
            return INSTANCE
        }
    }
}