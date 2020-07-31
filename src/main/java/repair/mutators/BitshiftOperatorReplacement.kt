package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.BinaryExpr
import repair.mutators.utils.isBitshift

class BitshiftOperatorReplacement: MutatorRepair<BinaryExpr>() {
    override val rank: Int
        get() = 27

    override fun checkedRepair(program: BuggyProgram, binExpr: BinaryExpr): List<BinaryExpr> {
        return if(isBitshift(binExpr.operator)){
            BinaryExpr.Operator.values()
                                    .filter { it != binExpr.operator && isBitshift(it) }
                                    .map { BinaryExpr(binExpr.left.clone(), binExpr.right.clone(), it) }
        } else { emptyList() }
    }
}
