package repair

import BuggyProgram
import com.github.javaparser.ast.Node
import fault_localization.reports.morpheus.MorpheusComponent
import repair.mutators.*

abstract class MorpheusRepair: RepairStrategy() {
    val oppositeMutOps = mapOf<String, List<MutatorRepair<*>>>(
            "RelationalOperatorReplacement" to listOf(RelationalOperatorReplacement()),
            "ArgumentNumberChange" to listOf(ArgumentNumberChange()),
            "ConstantReplacement" to listOf(BooleanConstantModification(), DoubleConstantModification(),
                    IntConstantModification()),
            "VarToVarReplacement" to listOf(VarToVarReplacement(true)),
            "ConsToVarReplacement" to listOf(VarToConsReplacement()),
            "MemberVariableAssignmentDeletion" to listOf(MemberVariableAssignmentInsertion())
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
