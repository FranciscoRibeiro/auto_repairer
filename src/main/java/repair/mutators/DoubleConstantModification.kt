package repair.mutators

import com.github.javaparser.ast.expr.DoubleLiteralExpr

class DoubleConstantModification: MutatorRepair<DoubleLiteralExpr>() {
    override fun checkedRepair(doubleLitExpr: DoubleLiteralExpr): List<DoubleLiteralExpr> {
        val value = doubleLitExpr.asDouble()
        val newValue = if(value == 1.0) 0.0 else 1.0
        return listOf(doubleLitExpr.clone().setDouble(newValue))
    }
}
