package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.BinaryExpr
import repair.mutators.utils.isBitwise

class BitwiseOperatorReplacement: MutatorRepair<BinaryExpr>() {
    override val rank: Int
        get() = 32

    override fun checkedRepair(program: BuggyProgram, binExpr: BinaryExpr): List<BinaryExpr> {
        return if(isBitwise(binExpr.operator)){
            BinaryExpr.Operator.values()
                                    .filter { it != binExpr.operator && isBitwise(it) }
                                    .map { BinaryExpr(binExpr.left.clone(), binExpr.right.clone(), it) }
        } else { emptyList() }
    }
}
