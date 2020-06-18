package repair

import AlternativeProgram
import BuggyProgram
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter.setup
import fault_localization.FaultLocalizationType
import repair.mutators.*

abstract class RepairStrategy {
    val mutators = mapOf<Class<out Node>, List<MutatorRepair<*>>>(
            BinaryExpr::class.java to listOf(RelationalOperatorReplacement(),
                    ConditionalOperatorReplacement(), ConditionalOperatorDeletion(),
                    ArithmeticOperatorReplacement(), ArithmeticOperatorDeletion(),
                    UnaryOperatorInsertion(), RemoveConditional()),
            BooleanLiteralExpr::class.java to listOf(BooleanConstantModification()),
            IntegerLiteralExpr::class.java to listOf(IntConstantModification(), ConsToVarReplacement()),
            DoubleLiteralExpr::class.java to listOf(DoubleConstantModification()),
            NameExpr::class.java to listOf(VarToVarReplacement(), VarToConsReplacement(), UnaryOperatorInsertion()),
            ExpressionStmt::class.java to listOf(StatementDeletion()),
            ReturnStmt::class.java to listOf(ReturnValue()),
            UnaryExpr::class.java to listOf(UnaryOperatorDeletion(), UnaryOperatorReplacement()),
            MethodCallExpr::class.java to listOf(NonVoidMethodDeletion(), VoidMethodDeletion()),
            FieldDeclaration::class.java to listOf(MemberVariableAssignmentDeletion()),
            Modifier::class.java to listOf(AccessorModifierChange())
    )

    abstract fun repair(program: BuggyProgram, basedOn: FaultLocalizationType): Sequence<AlternativeProgram>

    fun modifyComponent(program: BuggyProgram, modifications: Sequence<Pair<Node, List<Node>>>): Sequence<AlternativeProgram> {
        return modifications.flatMap { buildAlternatives(program, it.first, it.second) }
    }

    private fun buildAlternatives(buggyProgram: BuggyProgram, originalNode: Node, mutantNodes: List<Node>): Sequence<AlternativeProgram> {
        val alternatives = mutableListOf<AlternativeProgram>()

        for(mutant in mutantNodes){
            val tree = setup(buggyProgram.getOriginalTree())
            val nodeToReplace = findEqualNode(tree, originalNode) ?: return emptySequence()
            try {
                nodeToReplace.replace(mutant)
            } catch (e: UnsupportedOperationException){
                println("UnsupportedOperationException: Failed to replace \"$nodeToReplace\" with \"$mutant\"")
            }
            alternatives.add(AlternativeProgram(mutant, tree))
        }

        return alternatives.asSequence()
    }

    private fun findEqualNode(tree: CompilationUnit, nodeToFind: Node): Node? {
        val maybeNode = tree.findFirst(nodeToFind::class.java, { isSameNode(it, nodeToFind) })
        return if(maybeNode.isPresent) maybeNode.get() else null
    }

    private fun isSameNode(someNode: Node, nodeToFind: Node): Boolean {
        if(someNode == nodeToFind){
            val someNodeRange = someNode.range.orElse(null) ?: return false
            val nodeToFindRange = nodeToFind.range.orElse(null) ?: return false
            return someNodeRange == nodeToFindRange
        } else return false
    }
}
