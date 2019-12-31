package repair.mutators

import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.Expression

class ArithmeticOperatorDeletion: MutatorRepair<BinaryExpr>() {
    override fun checkedRepair(binExpr: BinaryExpr): List<Expression> {
        return if(isArithmetic(binExpr.operator)){
            listOf(binExpr.clone().left, binExpr.clone().right)
        } else { emptyList() }
    }

    private fun isArithmetic(op: BinaryExpr.Operator): Boolean {
        return op == BinaryExpr.Operator.PLUS || op == BinaryExpr.Operator.MINUS
                || op == BinaryExpr.Operator.MULTIPLY || op == BinaryExpr.Operator.DIVIDE
                || op == BinaryExpr.Operator.REMAINDER
    }
}
