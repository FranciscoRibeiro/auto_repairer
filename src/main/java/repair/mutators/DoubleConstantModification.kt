package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.DoubleLiteralExpr

class DoubleConstantModification: MutatorRepair<DoubleLiteralExpr>() {
    override val rank: Int
        get() = 2

    override fun checkedRepair(program: BuggyProgram, doubleLitExpr: DoubleLiteralExpr): List<DoubleLiteralExpr> {
        val value = doubleLitExpr.asDouble()
        val newValue = if(value == 1.0) 0.0 else 1.0
        return listOf(DoubleLiteralExpr(newValue))
    }
}
