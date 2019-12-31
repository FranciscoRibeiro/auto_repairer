package repair.mutators

import com.github.javaparser.ast.expr.BooleanLiteralExpr

class BooleanConstantModification: MutatorRepair<BooleanLiteralExpr>() {
    override fun checkedRepair(boolLitExpr: BooleanLiteralExpr): List<BooleanLiteralExpr> {
        val value = boolLitExpr.value
        return listOf(boolLitExpr.clone().setValue(!value))
    }
}
