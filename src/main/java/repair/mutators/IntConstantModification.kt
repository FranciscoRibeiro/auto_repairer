package repair.mutators

import com.github.javaparser.ast.expr.IntegerLiteralExpr

class IntConstantModification: MutatorRepair<IntegerLiteralExpr>() {
    override fun checkedRepair(intLitExpr: IntegerLiteralExpr): List<IntegerLiteralExpr> {
        val value = intLitExpr.asInt()
        val valueSet = mutableSetOf(1, 0, -1, -value, value+1, value-1)
        valueSet.remove(value)
        return valueSet.map { intLitExpr.clone().setInt(it) }
    }
}
