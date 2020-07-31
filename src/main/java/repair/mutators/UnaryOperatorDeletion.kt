package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.UnaryExpr

class UnaryOperatorDeletion: MutatorRepair<UnaryExpr>() {
    override val rank: Int
        get() = 9

    override fun checkedRepair(program: BuggyProgram, unExpr: UnaryExpr): List<Expression> {
        return listOf(unExpr.expression.clone())
    }
}