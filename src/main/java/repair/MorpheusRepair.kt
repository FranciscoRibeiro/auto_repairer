package repair

import BuggyProgram
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.BooleanLiteralExpr
import fault_localization.reports.morpheus.MorpheusComponent
import repair.mutators.*

abstract class MorpheusRepair: RepairStrategy() {
    val oppositeMutOps = mapOf<String, List<MutatorRepair<*>>>(
            "RelationalOperatorReplacement" to listOf(RelationalOperatorReplacement()),
            "ConditionalOperatorReplacement" to listOf(ConditionalOperatorReplacement()),
            "ConditionalOperatorDeletion" to listOf(ConditionalOperatorInsertion(BinaryExpr.Operator.AND, BooleanLiteralExpr(false)),
                    ConditionalOperatorInsertion(BinaryExpr.Operator.AND, BooleanLiteralExpr(true)),
                    ConditionalOperatorInsertion(BinaryExpr.Operator.OR, BooleanLiteralExpr(false)),
                    ConditionalOperatorInsertion(BinaryExpr.Operator.OR, BooleanLiteralExpr(true))),
            "ConditionalOperatorInsertion" to listOf(ConditionalOperatorDeletion()),
            "ArithmeticOperatorReplacement" to listOf(ArithmeticOperatorReplacement()),
            "ArithmeticOperatorDeletion" to listOf(ArithmeticOperatorInsertion()),
            "ArithmeticOperatorInsertion" to listOf(ArithmeticOperatorDeletion()),
            "UnaryOperatorInsertion" to listOf(UnaryOperatorDeletion()),
            "UnaryOperatorReplacement" to listOf(UnaryOperatorReplacement()),
            "BitshiftOperatorReplacement" to listOf(BitshiftOperatorReplacement()),
            "BitwiseOperatorReplacement" to listOf(BitwiseOperatorReplacement()),
            "ArgumentNumberChange" to listOf(ArgumentNumberChange()),
            "ConstantReplacement" to listOf(BooleanConstantModification(), DoubleConstantModification(),
                    IntConstantModification()),
            "VarToVarReplacement" to listOf(VarToVarReplacement(true)),
            "ConsToVarReplacement" to listOf(VarToConsReplacement()),
            "VarToConsReplacement" to listOf(ConsToVarReplacement()),
            "ReferenceReplacementContent" to listOf(ReferenceReplacementContent()),
            "Negation" to listOf(UnaryOperatorDeletion()),
            "ReturnValue" to listOf(ConsToVarReplacement()),
            "TrueReturn" to listOf(ConsToVarReplacement()),
            "FalseReturn" to listOf(ConsToVarReplacement()),
            "MemberVariableAssignmentDeletion" to listOf(MemberVariableAssignmentInsertion()),
            "AccessorMethodChange" to listOf(AccessorMethodChange()),
            "StaticModifierDeletion" to listOf(StaticModifierInsertion()),
            "StaticModifierInsertion" to listOf(StaticModifierDeletion()),
            "AccessorModifierChange" to listOf(AccessorModifierChange()),
            "ArgumentTypeChange" to listOf(ArgumentTypeChange())
    )

    internal fun revertMutation(program: BuggyProgram, comp: MorpheusComponent, nodes: Sequence<Node>): Sequence<Pair<Node, List<Node>>> {
        val repairMutOps = oppositeMutOps[comp.mutOp] ?: return emptySequence()
        return repairMutOps.flatMap { nodes.map { node -> node to mutate(program, it, node) } }.asSequence()
    }

    private fun mutate(program: BuggyProgram, mutOp: MutatorRepair<*>, node: Node): List<Node> {
        val mutOps = mutators[node.javaClass]?.map { it.javaClass } ?: return emptyList()
        return if(mutOp.javaClass in mutOps) { println("exists"); mutOp.repair(program, node) }
        else { emptyList() }
    }
}
