package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.*
import com.github.javaparser.resolution.types.ResolvedPrimitiveType
import repair.mutators.utils.calcType
import repair.mutators.utils.getParent
import repair.mutators.utils.isConditional
import repair.mutators.utils.isLeftSideAssign

class UnaryOperatorInsertion(): MutatorRepair<Expression>() {
    override val rank: Int
        get() = 23

    private val exclusions = listOf(UnaryExpr.Operator.LOGICAL_COMPLEMENT,
            UnaryExpr.Operator.BITWISE_COMPLEMENT,
            UnaryExpr.Operator.PLUS,
            UnaryExpr.Operator.MINUS) // The Negation mutation operator covers MINUS

    override fun checkedRepair(program: BuggyProgram, expr: Expression): List<Expression> {
        if(isLeftSideAssign(expr)) return emptyList()

        val parent = getParent(expr) ?: return emptyList()
        if(parent is UnaryExpr) return emptyList()

        return if(expr is NameExpr){
            val type = calcType(expr) ?: return emptyList()
            if(type is ResolvedPrimitiveType){
                when {
                    type.isNumeric -> {
                        UnaryExpr.Operator.values()
                                .subtract(exclusions)
                                .map { UnaryExpr(EnclosedExpr(expr.clone()), it) }
                    }
                    type.name == "BOOLEAN" -> listOf(UnaryExpr(EnclosedExpr(expr.clone()), UnaryExpr.Operator.LOGICAL_COMPLEMENT))
                    else -> emptyList()
                }
            } else emptyList()

        } else if(expr is BinaryExpr && isConditional(expr.operator)){
            listOf(UnaryExpr(EnclosedExpr(expr.clone()), UnaryExpr.Operator.LOGICAL_COMPLEMENT))
        } else if(parent is BinaryExpr && isConditional(parent.operator)){
            listOf(UnaryExpr(EnclosedExpr(expr.clone()), UnaryExpr.Operator.LOGICAL_COMPLEMENT))
        } else emptyList()
    }
}
