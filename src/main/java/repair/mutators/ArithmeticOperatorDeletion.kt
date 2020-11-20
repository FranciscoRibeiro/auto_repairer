package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.Expression
import repair.mutators.utils.isArithmetic
import repair.mutators.utils.isString

class ArithmeticOperatorDeletion: MutatorRepair<BinaryExpr>() {
    override val rank: Int
        get() = 5

    override fun checkedRepair(program: BuggyProgram, binExpr: BinaryExpr): List<Expression> {
        return if(isArithmetic(binExpr.operator)){
            return if(isString(binExpr.left) || isString(binExpr.right)) emptyList()
            else if (binExpr.right == binExpr.left) listOf(binExpr.left.clone()) // Both sides are the same, produce only one
            else listOf(binExpr.left.clone(), binExpr.right.clone())
        } else { emptyList() }
    }
}
