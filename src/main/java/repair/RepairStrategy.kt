package repair

import Alternatives
import BuggyProgram
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.BooleanLiteralExpr
import com.github.javaparser.ast.expr.DoubleLiteralExpr
import com.github.javaparser.ast.expr.IntegerLiteralExpr
import fault_localization.FaultLocalizationType
import repair.mutators.*

abstract class RepairStrategy {
    val mutators = mapOf<Class<out Node>, List<MutatorRepair<*>>>(
            BinaryExpr::class.java to listOf(RelationalOperatorReplacement(), ArithmeticOperatorDeletion()),
            IntegerLiteralExpr::class.java to listOf(IntConstantModification()),
            BooleanLiteralExpr::class.java to listOf(BooleanConstantModification()),
            DoubleLiteralExpr::class.java to listOf(DoubleConstantModification())
    )

    abstract fun repair(program: BuggyProgram, basedOn: FaultLocalizationType): Alternatives?
}
