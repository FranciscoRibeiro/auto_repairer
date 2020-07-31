package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.NullLiteralExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr

class ConstructorCallReplacementNull: MutatorRepair<ObjectCreationExpr>() {
    override val rank: Int
        get() = 21

    override fun checkedRepair(program: BuggyProgram, checkedNode: ObjectCreationExpr): List<NullLiteralExpr> {
        return listOf(NullLiteralExpr())
    }
}
