package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.BooleanLiteralExpr
import com.github.javaparser.ast.stmt.ReturnStmt
import repair.mutators.utils.calcType

class TrueReturn: MutatorRepair<ReturnStmt>() {
    override val rank: Int
        get() = 26

    override fun checkedRepair(program: BuggyProgram, ret: ReturnStmt): List<ReturnStmt> {
        val retExpr = ret.expression.orElse(null) ?: return emptyList()
        val retExprType = calcType(retExpr) ?: return emptyList()
        return if(retExprType.describe() == "boolean") {
            listOf(ReturnStmt(BooleanLiteralExpr(true)))
        }
        else emptyList()
    }
}
