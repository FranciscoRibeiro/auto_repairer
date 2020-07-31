package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.BinaryExpr
import repair.mutators.utils.isConditional

class ConditionalOperatorReplacement: MutatorRepair<BinaryExpr>() {
    override val rank: Int
        get() = 10

    override fun checkedRepair(program: BuggyProgram, binExpr: BinaryExpr): List<BinaryExpr> {
        return if(isConditional(binExpr.operator)){
            BinaryExpr.Operator.values()
                    .filter { it != binExpr.operator && isConditional(it) }
                    .map { BinaryExpr(binExpr.left.clone(), binExpr.right.clone(), it) }

        } else { emptyList() }
    }
}
