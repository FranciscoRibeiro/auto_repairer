package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.type.PrimitiveType

class ArgumentTypeChange: MutatorRepair<Parameter>() {
    override val rank: Int
        get() = 24

    override fun checkedRepair(program: BuggyProgram, param: Parameter): List<Parameter> {
        return when(param.type.asString()){
            "long" -> listOf(paramCopyWithType(param, PrimitiveType.Primitive.INT))
            "int" -> listOf(paramCopyWithType(param, PrimitiveType.Primitive.LONG))
            "double" -> listOf(paramCopyWithType(param, PrimitiveType.Primitive.FLOAT))
            "float" -> listOf(paramCopyWithType(param, PrimitiveType.Primitive.DOUBLE))
            else -> emptyList()
        }
    }

    private fun paramCopyWithType(param: Parameter, type: PrimitiveType.Primitive): Parameter {
        return Parameter(param.modifiers, param.annotations, PrimitiveType(type, param.type.annotations), param.isVarArgs, param.varArgsAnnotations, param.name)
    }
}
