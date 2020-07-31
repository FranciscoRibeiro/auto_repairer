package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.BooleanLiteralExpr
import com.github.javaparser.ast.stmt.IfStmt
import repair.mutators.utils.getParent

class RemoveConditional: MutatorRepair<BinaryExpr>() {
    override val rank: Int
        get() = 16

    override fun checkedRepair(program: BuggyProgram, binExpr: BinaryExpr): List<BooleanLiteralExpr> {
        val parent = getParent(binExpr) ?: return emptyList()
        return if(parent is IfStmt){
            listOf(BooleanLiteralExpr(true), BooleanLiteralExpr(false))
        } else emptyList()
    }
}
