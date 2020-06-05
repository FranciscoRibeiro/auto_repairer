package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.Expression
import repair.mutators.utils.isArithmetic

class ArithmeticOperatorDeletion: MutatorRepair<BinaryExpr>() {
    override val rank: Int
        get() = 7

    override fun checkedRepair(program: BuggyProgram, binExpr: BinaryExpr): List<Expression> {
        return if(isArithmetic(binExpr.operator)){
            listOf(binExpr.clone().left, binExpr.clone().right)
        } else { emptyList() }
    }
}
