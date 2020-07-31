package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.BooleanLiteralExpr

class BooleanConstantModification: MutatorRepair<BooleanLiteralExpr>() {
    override val rank: Int
        get() = 1

    override fun checkedRepair(program: BuggyProgram, boolLitExpr: BooleanLiteralExpr): List<BooleanLiteralExpr> {
        val value = boolLitExpr.value
        return listOf(BooleanLiteralExpr(!value))
    }
}
