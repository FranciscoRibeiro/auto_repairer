package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.Expression
import repair.mutators.utils.isBitshift

class BitshiftOperatorDeletion: MutatorRepair<BinaryExpr>() {
    override val rank: Int
        get() = 33

    override fun checkedRepair(program: BuggyProgram, binExpr: BinaryExpr): List<Expression> {
        return if(isBitshift(binExpr.operator)){
            listOf(binExpr.left.clone(), binExpr.right.clone())
        } else { emptyList() }
    }
}
