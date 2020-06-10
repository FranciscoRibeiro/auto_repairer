package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.comments.Comment
import com.github.javaparser.ast.comments.LineComment
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.Statement

class StatementDeletion: MutatorRepair<ExpressionStmt>() {
    override val rank: Int
        get() = 6

    override fun checkedRepair(program: BuggyProgram, exprStmt: ExpressionStmt): List<ExpressionStmt> {
        return listOf(ExpressionStmt())
    }
}
