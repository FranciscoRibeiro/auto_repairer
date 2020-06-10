package repair

import AlternativeProgram
import BuggyProgram
import com.github.javaparser.ast.Node
import fault_localization.FaultLocalizationType
import repair.mutators.MutatorRepair

class BruteForceRepair: RepairStrategy() {
    override fun repair(program: BuggyProgram, basedOn: FaultLocalizationType): Sequence<AlternativeProgram> {
        val alts = program.mostLikelyFaulty(basedOn, 3)
                        .map { it.map { it to program.nodesInLine(it) } }
//                      .map { createMutants(program, it.second) }
                        .map { createMutants(program, it.flatMap { it.second }) }
                        .flatMap { modifyComponent(program, it) }

        return alts
    }

    private fun createMutants(program: BuggyProgram, nodes: Sequence<Node>): Sequence<Pair<Node, List<Node>>> {
        return nodes.flatMap { pairWithMutOp(it) }//.asSequence()
                    .sortedBy { it.second.rank }
                    .map { it.first to mutate(program, it.second, it.first) }
                    .filter { it.second.isNotEmpty() }
    }

    private fun pairWithMutOp(node: Node): Sequence<Pair<Node, MutatorRepair<*>>> {
        val mutOps = mutators[node.javaClass] ?: return emptySequence()
        return mutOps.asSequence().map { node to it }
    }

    private fun mutate(program: BuggyProgram, mutOp: MutatorRepair<*>, node: Node): List<Node> {
        return mutOp.repair(program, node)
    }
}
