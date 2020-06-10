package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.BinaryExpr
import repair.mutators.utils.isRelational

class RelationalOperatorReplacement(val op: BinaryExpr.Operator? = null): MutatorRepair<BinaryExpr>() {
    override val rank: Int
        get() = 4

    override fun checkedRepair(program: BuggyProgram, binExpr: BinaryExpr): List<BinaryExpr> {
        return if(isRelational(binExpr.operator)){
            if(op == null){
                BinaryExpr.Operator.values()
                        .filter { it != binExpr.operator && isRelational(it) }
                        .map { BinaryExpr(binExpr.left.clone(), binExpr.right.clone(), it) }
            } else {
                listOf(BinaryExpr(binExpr.left.clone(), binExpr.right.clone(), op))
            }
        } else { emptyList() }
    }
}
