package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.IntegerLiteralExpr

class IntConstantModification(val newValue: Int? = null): MutatorRepair<IntegerLiteralExpr>() {
    override val rank: Int
        get() = 1

    override fun checkedRepair(program: BuggyProgram, intLitExpr: IntegerLiteralExpr): List<IntegerLiteralExpr> {
        if(newValue == null) {
            val value = intLitExpr.asInt()
            val valueSet = mutableSetOf(1, 0, -1, -value, value + 1, value - 1)
            valueSet.remove(value)
            return valueSet.map { IntegerLiteralExpr(it) }
        } else {
            return listOf(IntegerLiteralExpr(newValue))
        }
    }
}
