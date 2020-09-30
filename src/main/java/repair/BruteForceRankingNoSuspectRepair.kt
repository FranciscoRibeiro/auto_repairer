package repair

import AlternativeProgram
import BuggyProgram
import com.github.javaparser.ast.Node
import fault_localization.FaultLocalizationType
import fault_localization.reports.sfl.SFLComponent
import repair.mutators.MutatorRepair
import java.util.*

class BruteForceRankingNoSuspectRepair: RepairStrategy() {
    override fun repair(program: BuggyProgram, basedOn: FaultLocalizationType): Sequence<AlternativeProgram> {
        val nodes = program.mostLikelyFaulty(basedOn, 5) // List of lists: the outer list ranks lines by prob and each inner list has all the line numbers for that spot in the ranking
                .flatten() // This strategy ignores the probability, so we flatten into a list of numbers
                .filterIsInstance<SFLComponent>()
                .map { it.line }
                .flatMap { program.nodesInLine(it) } // List of all nodes
//                        .map { it.map { it to program.nodesInLine(it) } } // List of lists: each inner list now holds pairs of (line_nr, [node]). The snd element are the nodes in that line
//                        .flatten()
//                        .flatMap { it.second }

        val mutantNodes = createMutants(program, nodes)
        return modifyComponent(program, mutantNodes)

//                .map { createMutants(program, it.flatMap { it.second }) } // createMutants receives each spot "flattened", meaning all nodes from all lines in that spot are passed together because they have the same probability of containing the bug. Returns list of lists, each inner list has pairs (node, [node]) associating a node (orig prog) to its produced mutants
//                        .flatMap { modifyComponent(program, it) } // creates all the possible alternatives

//        return alts
    }

    private fun createMutants(program: BuggyProgram, nodes: Sequence<Node>): Sequence<Pair<Node, List<Node>>> {
        return nodes.flatMap { pairWithMutOp(it) }
                    .sortedBy { it.second.rank }
                    .map { it.first to mutate(program, it.second, it.first) }
                    .filter { it.second.isNotEmpty() }
    }

    private fun mutate(program: BuggyProgram, mutOp: MutatorRepair<*>, node: Node): List<Node> {
        return mutOp.repair(program, node)
    }
}

private fun <T> Sequence<T>.mix(): Sequence<T> {
    return this.asIterable().shuffled(Random(1573)).asSequence()
}
