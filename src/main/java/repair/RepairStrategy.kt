package repair

import AlternativeProgram
import Alternatives
import BuggyProgram
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.ExpressionStmt
import fault_localization.FaultLocalizationType
import repair.mutators.*

abstract class RepairStrategy {
    val mutators = mapOf<Class<out Node>, List<MutatorRepair<*>>>(
            /*BinaryExpr::class.java to listOf(RelationalOperatorReplacement(), ArithmeticOperatorDeletion()),
            BooleanLiteralExpr::class.java to listOf(BooleanConstantModification()),
            IntegerLiteralExpr::class.java to listOf(IntConstantModification(), ConsToVarReplacement()),
            DoubleLiteralExpr::class.java to listOf(DoubleConstantModification()),*/
            NameExpr::class.java to listOf(/*VarToVarReplacement(),*/ VarToConsReplacement())/*,*/
            /*ExpressionStmt::class.java to listOf(StatementDeletion())*/
    )

    abstract fun repair(program: BuggyProgram, basedOn: FaultLocalizationType): Sequence<AlternativeProgram>

    fun modifyComponent(program: BuggyProgram, modifications: Sequence<Pair<Node, List<Node>>>): Sequence<AlternativeProgram> {
        return modifications.flatMap { buildAlternatives(program, it.first, it.second) }
    }

    private fun buildAlternatives(buggyProgram: BuggyProgram, originalNode: Node, mutantNodes: List<Node>): Sequence<AlternativeProgram> {
        val alternatives = mutableListOf<AlternativeProgram>()
        val tree = buggyProgram.getOriginalTree()
        val nodeToReplace = findEqualNode(tree, originalNode) ?: return emptySequence()
        var temporaryNode = nodeToReplace
        for(mutant in mutantNodes){
            temporaryNode.replace(mutant)
            alternatives.add(AlternativeProgram(mutant, tree.clone()))
            temporaryNode = mutant
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
