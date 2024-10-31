package moe.ruruke.skyblock.asm.utils

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import java.util.*
import kotlin.math.max

class InstructionBuilder {
    private var methodNode: MethodNode? = null
    private var firstUnusedLocalVariableIndex = 0
    private var instructions: InsnList? = null
    private var currentType: VariableType? = null
    private var localVariableTypes: MutableMap<Int, VariableType>? = null
    private var branchStack: LinkedList<LabelNode>? = null

    // New Instances
    fun newInstance(clazz: String?): InstructionBuilder {
        return newInstance(clazz, "()V")
    }

    fun newInstance(clazz: String?, constructor: String?): InstructionBuilder {
        instructions!!.add(TypeInsnNode(Opcodes.NEW, clazz))
        instructions!!.add(InsnNode(Opcodes.DUP))
        instructions!!.add(MethodInsnNode(Opcodes.INVOKESPECIAL, clazz, "<init>", constructor, false))

        currentType = VariableType.OBJECT
        return this
    }

    // Local Variables
    fun storeAuto(relativeVariableNumber: Int): InstructionBuilder {
        return store(this.firstUnusedLocalVariableIndex + relativeVariableNumber)
    }

    fun loadAuto(relativeVariableNumber: Int): InstructionBuilder {
        return load(this.firstUnusedLocalVariableIndex + relativeVariableNumber)
    }

    fun storeAuto(variableType: VariableType, relativeVariableNumber: Int): InstructionBuilder {
        return store(variableType, this.firstUnusedLocalVariableIndex + relativeVariableNumber)
    }

    fun loadAuto(variableType: VariableType, relativeVariableNumber: Int): InstructionBuilder {
        return load(variableType, this.firstUnusedLocalVariableIndex + relativeVariableNumber)
    }

    fun store(variableNumber: Int): InstructionBuilder {
        val variableType = this.currentType
        require(!(variableType == null || variableType == VariableType.VOID)) { "There is no variable to store!" }

        return store(variableType, variableNumber)
    }

    fun load(variableNumber: Int): InstructionBuilder {
        val variableType = localVariableTypes!![variableNumber]
        require(!(variableType == null || variableType == VariableType.VOID)) { "The variable type is not yet known for this variable. Please use load(VariableType, int) instead!" }

        return load(variableType, variableNumber)
    }

    fun load(variableType: VariableType, variableNumber: Int): InstructionBuilder {
        instructions!!.add(VarInsnNode(variableType.getOpcode(Opcodes.ILOAD), variableNumber))
        localVariableTypes!![variableNumber] = variableType

        this.currentType = variableType
        return this
    }

    private fun store(variableType: VariableType, variableNumber: Int): InstructionBuilder {
        instructions!!.add(VarInsnNode(variableType.getOpcode(Opcodes.ISTORE), variableNumber))
        localVariableTypes!![variableNumber] = variableType

        this.currentType = VariableType.VOID
        return this
    }

    // Methods
    fun callStaticMethod(owner: String?, method: String?, descriptor: String): InstructionBuilder {
        return callStaticMethod(owner, method, descriptor, false)
    }

    fun callStaticMethod(
        owner: String?,
        method: String?,
        descriptor: String,
        isInterface: Boolean
    ): InstructionBuilder {
        return invoke(Opcodes.INVOKESTATIC, owner, method, descriptor, isInterface)
    }

    fun invokeInstanceMethod(owner: String?, method: String?, descriptor: String): InstructionBuilder {
        return invokeInstanceMethod(owner, method, descriptor, false)
    }

    fun invokeInstanceMethod(
        owner: String?,
        method: String?,
        descriptor: String,
        isInterface: Boolean
    ): InstructionBuilder {
        return invoke(Opcodes.INVOKEVIRTUAL, owner, method, descriptor, isInterface)
    }

    private fun invoke(
        opcode: Int,
        owner: String?,
        method: String?,
        descriptor: String,
        isInterface: Boolean
    ): InstructionBuilder {
        instructions!!.add(MethodInsnNode(opcode, owner, method, descriptor, isInterface))
        this.currentType = VariableType.getTypeFromDescriptor(descriptor)
        return this
    }

    // If Statments
    fun startIfEqual(): InstructionBuilder {
        require(currentType == VariableType.BOOLEAN) { "You must supply a boolean before starting an if statement!" }

        val notCancelled = LabelNode()
        instructions!!.add(JumpInsnNode(Opcodes.IFEQ, notCancelled))
        branchStack!!.add(notCancelled)

        this.currentType = VariableType.VOID
        return this
    }

    fun startIfNotEqual(): InstructionBuilder {
        require(currentType == VariableType.BOOLEAN) { "You must supply a boolean before starting an if statement!" }

        val notCancelled = LabelNode()
        instructions!!.add(JumpInsnNode(Opcodes.IFNE, notCancelled))
        branchStack!!.add(notCancelled)

        this.currentType = VariableType.VOID
        return this
    }

    fun endIf(): InstructionBuilder {
        instructions!!.add(branchStack!!.removeLast())

        this.currentType = VariableType.VOID
        return this
    }

    // Constants
    fun constantValue(value: Float): InstructionBuilder {
        if (value == 0.0f) {
            instructions!!.add(InsnNode(Opcodes.FCONST_0))
        }
        if (value == 1.0f) {
            instructions!!.add(InsnNode(Opcodes.FCONST_1))
        }
        if (value == 2.0f) {
            instructions!!.add(InsnNode(Opcodes.FCONST_2))
        } else {
            instructions!!.add(LdcInsnNode(value))
        }
        currentType = VariableType.FLOAT
        return this
    }

    // Return
    @Suppress("SpellCheckingInspection")
    fun reeturn(): InstructionBuilder { // TODO Does returning in a void method work?
        // TODO Do we need to pop the variable for void or?
        require(
            !(this.currentType != VariableType.VOID && this.currentType != VariableType.getTypeFromDescriptor(
                methodNode!!.desc
            ))
        ) { "This type of variable cannot be returned for this method!" }

        instructions!!.add(InsnNode(currentType!!.getOpcode(Opcodes.IRETURN)))

        this.currentType = VariableType.VOID
        return this
    }

    // Finishers
    fun finishList(): InsnList? {
        return instructions
    }
//TODO:
    fun endCode(): InjectionHelper {
        return InjectionHelper.resume().setInstructions(this.instructions)
    }

    private val firstUnusedVariableIndex: Int
        get() {
            var maxIndex = -1
            for (localVariable in methodNode!!.localVariables) {
                maxIndex = max(maxIndex.toDouble(), localVariable.index.toDouble()).toInt()
            }

            return maxIndex + 1
        }

    enum class VariableType(private val asmType: Type) {
        VOID(Type.VOID_TYPE),
        BOOLEAN(Type.BOOLEAN_TYPE),
        CHAR(Type.CHAR_TYPE),
        BYTE(Type.BYTE_TYPE),
        SHORT(Type.SHORT_TYPE),
        INT(Type.INT_TYPE),
        FLOAT(Type.FLOAT_TYPE),
        LONG(Type.LONG_TYPE),
        DOUBLE(Type.DOUBLE_TYPE),

        OBJECT(Type.getObjectType("Dummy"));

        fun getOpcode(opcode: Int): Int {
            return asmType.getOpcode(opcode)
        }

        companion object {
            fun getTypeFromDescriptor(descriptor: String): VariableType? {
                val type = Type.getReturnType(descriptor)

                val sort = type.sort
                return if (sort == Type.OBJECT || sort == Type.ARRAY) {
                    OBJECT
                } else {
                    fromASMType(type)
                }
            }

            fun fromASMType(type: Type): VariableType? {
                for (variableType in entries) {
                    if (variableType.asmType === type) {
                        return variableType
                    }
                }

                return null
            }
        }
    }

    companion object {
        private val INSTANCE = InstructionBuilder()

        // Start
        fun start(methodNode: MethodNode?): InstructionBuilder {
            INSTANCE.instructions = InsnList()
            INSTANCE.methodNode = methodNode
            INSTANCE.firstUnusedLocalVariableIndex =
                INSTANCE.firstUnusedVariableIndex
            INSTANCE.currentType = VariableType.VOID
            INSTANCE.localVariableTypes = HashMap()
            INSTANCE.branchStack = LinkedList()
            return INSTANCE
        }
    }
}
