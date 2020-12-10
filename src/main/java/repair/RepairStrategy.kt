package repair

import AlternativeProgram
import BuggyProgram
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.ReturnStmt
import fault_localization.FaultLocalizationType
import fault_localization.reports.FLComponent
import fault_localization.reports.qsfl.Landmark
import printError
import repair.mutators.*
import java.lang.IllegalArgumentException
import java.util.*

abstract class RepairStrategy {
    val mutators = mapOf<Class<out Node>, List<MutatorRepair<*>>>(
            BinaryExpr::class.java to listOf(RelationalOperatorReplacement(),
                    ConditionalOperatorReplacement(), ConditionalOperatorDeletion(),
                    ConditionalOperatorInsertion(BinaryExpr.Operator.AND, BooleanLiteralExpr(false)),
                    ConditionalOperatorInsertion(BinaryExpr.Operator.AND, BooleanLiteralExpr(true)),
                    ConditionalOperatorInsertion(BinaryExpr.Operator.OR, BooleanLiteralExpr(false)),
                    ConditionalOperatorInsertion(BinaryExpr.Operator.OR, BooleanLiteralExpr(true)),
                    ArithmeticOperatorReplacement(), ArithmeticOperatorDeletion(),
                    ArithmeticOperatorInsertion(),
                    UnaryOperatorInsertion(), RemoveConditional(),
                    BitshiftOperatorReplacement(), BitshiftOperatorDeletion(),
                    BitwiseOperatorReplacement()),
            BooleanLiteralExpr::class.java to listOf(BooleanConstantModification(), ConsToVarReplacement()),
            IntegerLiteralExpr::class.java to listOf(IntConstantModification(), ConsToVarReplacement()),
            LongLiteralExpr::class.java to listOf(ConsToVarReplacement()),
            StringLiteralExpr::class.java to listOf(ConsToVarReplacement()),
            DoubleLiteralExpr::class.java to listOf(DoubleConstantModification(), ConsToVarReplacement()),
            CharLiteralExpr::class.java to listOf(ConsToVarReplacement()),
            NameExpr::class.java to listOf(VarToVarReplacement(), VarToConsReplacement(),
                    UnaryOperatorInsertion(), ReferenceReplacementContent(), Negation(),
                    ArithmeticOperatorInsertion()),
            ExpressionStmt::class.java to listOf(StatementDeletion()),
            ReturnStmt::class.java to listOf(ReturnValue(), TrueReturn(), FalseReturn()),
            UnaryExpr::class.java to listOf(UnaryOperatorDeletion(), UnaryOperatorReplacement(),
                    ArithmeticOperatorInsertion()),
            ObjectCreationExpr::class.java to listOf(ArgumentNumberChange()),
            MethodCallExpr::class.java to listOf(NonVoidMethodDeletion(), VoidMethodDeletion(),
                    AccessorMethodChange(), ArgumentNumberChange(), ArithmeticOperatorInsertion()),
            FieldDeclaration::class.java to listOf(MemberVariableAssignmentDeletion(), StaticModifierDeletion(),
                    StaticModifierInsertion()),
            Modifier::class.java to listOf(AccessorModifierChange()),
            ObjectCreationExpr::class.java to listOf(ConstructorCallReplacementNull()),
            FieldAccessExpr::class.java to listOf(ReferenceReplacementContent(), Negation(),
                    ArithmeticOperatorInsertion()),
            ArrayAccessExpr::class.java to listOf(Negation(), ArithmeticOperatorInsertion()),
            Parameter::class.java to listOf(ArgumentTypeChange()),
            VariableDeclarator::class.java to listOf(MemberVariableAssignmentInsertion())
    )

    abstract fun repair(program: BuggyProgram, basedOn: FaultLocalizationType): Sequence<AlternativeProgram>

    fun modifyComponent(program: BuggyProgram, modifications: Sequence<Pair<Node, List<Node>>>): Sequence<AlternativeProgram> {
        return modifications.flatMap { buildAlternatives(program, it.first, it.second) }
    }

    fun modifyComponent2(program: BuggyProgram, modifications: Sequence<Pair<FLComponent, Sequence<Pair<Node, List<Node>>>>>): Sequence<AlternativeProgram> {
        return modifications.flatMap {
            (comp, nodesAndMutants) ->
                program.setAST(comp)
//                val fullName = program.buildFullName(comp)
                val fullPath = program.currentTreeFullPath()
//                modifyComponent(program, nodesAndMutants).map { it.setFullName(fullName) }
                modifyComponent(program, nodesAndMutants).map { it.setFullPath(fullPath) }
        }
    }

    private fun buildAlternatives(buggyProgram: BuggyProgram, originalNode: Node, mutantNodes: List<Node>): Sequence<AlternativeProgram> {
        val alternatives = mutableListOf<AlternativeProgram>()

        for(mutant in mutantNodes){
            val tree = /*setup(*/buggyProgram.getOriginalTree()/*)*/
            val nodeToReplace = findEqualNode(tree, originalNode) ?: return emptySequence()
            try {
                nodeToReplace.replace(mutant)
                alternatives.add(AlternativeProgram(mutant, tree))
            } catch (e: UnsupportedOperationException){
                printError("UnsupportedOperationException: Failed to replace \"$nodeToReplace\" with \"$mutant\"")
                printError("parent: ${originalNode.parentNode.get()}")
            } catch (e: IllegalArgumentException){
                printError("IllegalArgumentException: Failed to replace \"$nodeToReplace\" with \"$mutant\"")
                printError("parent: ${originalNode.parentNode.get()}")
            }
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

    internal fun pairWithMutOp(node: Node): Sequence<Pair<Node, MutatorRepair<*>>> {
        val mutOps = mutators[node.javaClass] ?: return emptySequence()
        return mutOps.asSequence().map { node to it }
    }

    internal fun <T> Sequence<T>.mix(): Sequence<T> {
        return this.asIterable().shuffled(Random(1573)).asSequence()
    }

    internal fun <A, B> apart(pair: Pair<A,Sequence<B>> ): Sequence<Pair<A,B>> {
        return pair.second.map { pair.first to it }
    }

    internal fun <A, B> Sequence<Pair<A,B>>.groupPairs(): Sequence<Pair<A,Sequence<B>>> {
        return this.groupBy({ it.first }, { it.second }).asSequence().map { it.key to it.value.asSequence() }
    }

    internal fun Sequence<Sequence<Pair<Landmark,Sequence<Node>>>>.removeDups(): Sequence<Sequence<Pair<Landmark,Sequence<Node>>>> {
        val nodesNoDups = mutableListOf<Node>()
        val noDups = mutableListOf<MutableList<Pair<Landmark,MutableList<Node>>>>()
        for (rankSpot in this){
            val rankSpotNoDups = mutableListOf<Pair<Landmark,MutableList<Node>>>()
            noDups.add(rankSpotNoDups)
            for(compAndNodes in rankSpot){
                val compAndNodesNoDups = compAndNodes.first to mutableListOf<Node>()
                rankSpotNoDups.add(compAndNodesNoDups)
                for(node in compAndNodes.second){
                    if(!nodesNoDups.hasRef(node)){
                        compAndNodesNoDups.second.add(node)
                        nodesNoDups.add(node)
                    }
                }
            }
        }
        return noDups.map { it.map { it.first to it.second.asSequence() }.asSequence() }.asSequence()
    }

    internal fun <T> List<T>.hasRef(e: T): Boolean {
        for(elem in this){
            if(e === elem) return true
        }
        return false
    }
}

internal fun <E, T> List<List<E>>.has(e: T): Boolean {
    return this
            .flatMap { it }
            .any { it === e }
}
