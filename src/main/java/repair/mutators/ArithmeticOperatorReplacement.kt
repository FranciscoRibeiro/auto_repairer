package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.BinaryExpr
import repair.mutators.utils.isArithmetic
import repair.mutators.utils.isString

class ArithmeticOperatorReplacement: MutatorRepair<BinaryExpr>() {
    override val rank: Int
        get() = 12

    override fun checkedRepair(program: BuggyProgram, binExpr: BinaryExpr): List<BinaryExpr> {
        return if(isArithmetic(binExpr.operator)){
            return if(isString(binExpr.left) || isString(binExpr.right)) emptyList()
            else BinaryExpr.Operator.values()
                                    .filter { it != binExpr.operator && isArithmetic(it) }
                                    .map { BinaryExpr(binExpr.left.clone(), binExpr.right.clone(), it) }
        } else { emptyList() }
    }
}
