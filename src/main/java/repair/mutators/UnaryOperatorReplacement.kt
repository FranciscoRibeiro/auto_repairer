package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.UnaryExpr

class UnaryOperatorReplacement: MutatorRepair<UnaryExpr>() {
    override val rank: Int
        get() = 15

    private val exclusions = listOf(UnaryExpr.Operator.LOGICAL_COMPLEMENT,
            UnaryExpr.Operator.BITWISE_COMPLEMENT,
            UnaryExpr.Operator.PLUS,
            UnaryExpr.Operator.MINUS)

    override fun checkedRepair(program: BuggyProgram, unExpr: UnaryExpr): List<UnaryExpr> {
        return if(!exclusions.contains(unExpr.operator)){
            (UnaryExpr.Operator.values().subtract(exclusions))
                    .filter { it != unExpr.operator }
                    .map { UnaryExpr(unExpr.expression.clone(), it) }

        } else { emptyList() }
    }
}
