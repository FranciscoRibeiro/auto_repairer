package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.EnclosedExpr
import com.github.javaparser.ast.expr.Expression
import repair.mutators.utils.isConditional

class ConditionalOperatorInsertion(val op: BinaryExpr.Operator, val exprToAdd: Expression): MutatorRepair<BinaryExpr>() {
    override val rank: Int
        get() = 17

    override fun checkedRepair(program: BuggyProgram, binExpr: BinaryExpr): List<Expression> {
        return if (isConditional(op)){
            listOf(EnclosedExpr(BinaryExpr(EnclosedExpr(binExpr.clone()), exprToAdd.clone(), op)))
        } else emptyList()
    }
}
