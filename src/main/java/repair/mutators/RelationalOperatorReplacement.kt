package repair.mutators

import com.github.javaparser.ast.expr.BinaryExpr

class RelationalOperatorReplacement: MutatorRepair<BinaryExpr>() {
    override fun checkedRepair(binExpr: BinaryExpr): List<BinaryExpr> {
        return if(isRelational(binExpr.operator)){
            BinaryExpr.Operator.values()
                    .filter { it != binExpr.operator && isRelational(it) }
                    .map { binExpr.clone().setOperator(it) }
        } else { emptyList() }
    }

    private fun isRelational(op: BinaryExpr.Operator): Boolean {
        return op == BinaryExpr.Operator.EQUALS || op == BinaryExpr.Operator.NOT_EQUALS
                || op == BinaryExpr.Operator.LESS || op == BinaryExpr.Operator.LESS_EQUALS
                || op == BinaryExpr.Operator.GREATER || op == BinaryExpr.Operator.GREATER_EQUALS
    }
}
