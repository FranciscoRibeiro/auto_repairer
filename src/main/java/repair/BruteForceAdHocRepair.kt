package repair

import AlternativeProgram
import BuggyProgram
import com.github.javaparser.ast.Node
import fault_localization.FaultLocalizationType
import fault_localization.reports.sfl.SFLComponent
import repair.mutators.MutatorRepair

class BruteForceAdHocRepair: RepairStrategy() {
    override fun repair(program: BuggyProgram, basedOn: FaultLocalizationType): Sequence<AlternativeProgram> {
        val alts = program.mostLikelyFaulty(basedOn, 5) // List of lists: the outer list ranks components (lines) by prob and each inner list has all the components for that spot in the ranking
                .map { comps -> comps.filterIsInstance<SFLComponent>() } // Cast to SFLComponent
//                .map { comps -> comps.map { it.line } } // Extract line nrs
//                .map { lines -> lines.map { it to program.nodesInLine(it.line) } } // List of lists: each inner list now holds pairs of (line_nr, [node]). The snd element are the nodes in that line
                .map { lines -> lines.map { it to program.nodesInLine(it) } } // List of lists: each inner list now holds pairs of (SFLComponent, [node]). The snd element are the nodes in that line (SFLComponent)
//                .map { createMutants(program, it.flatMap { it.second }) } // createMutants receives each spot "flattened", meaning all nodes from all lines in that spot are passed together because they have the same probability of containing the bug. Returns list of lists, each inner list has pairs (node, [node]) associating a node (orig prog) to its produced mutants
                .map { createMutants(program, it) } // createMutants receives each spot "flattened", meaning all nodes from all lines in that spot are passed together because they have the same probability of containing the bug. Returns list of lists, each inner list has pairs (node, [node]) associating a node (orig prog) to its produced mutants
                .flatMap { modifyComponent2(program, it) } // creates all the possible alternatives

        return alts
    }

    /*private fun createMutants(program: BuggyProgram, nodes: Sequence<Node>): Sequence<Pair<Node, List<Node>>> {
        return nodes.flatMap { pairWithMutOp(it) }
                    .mix()
                    .map { it.first to mutate(program, it.second, it.first) }
                    .filter { it.second.isNotEmpty() }
    }*/
    
    private fun createMutants(program: BuggyProgram,
                              compsAndNodes: Sequence<Pair<SFLComponent, Sequence<Node>>>)
            : Sequence<Pair<SFLComponent, Sequence<Pair<Node, List<Node>>>>> {
        return compsAndNodes
                .map { (line, nodes) -> line to nodes.flatMap { pairWithMutOp(it) } }
                .mix()
                .map { (line, nodesAndMutOps) -> line to nodesAndMutOps.mix() }
                .map {
                    (line, nodesAndMutOps) ->
                        line to nodesAndMutOps.map { (node, mutOp) -> node to mutate(program, mutOp, node) }
                }
                .map {
                    (line, nodesAndMutants) ->
                        line to nodesAndMutants.filter { (_, mutants) -> mutants.isNotEmpty() }
                }
    }

    private fun mutate(program: BuggyProgram, mutOp: MutatorRepair<*>, node: Node): List<Node> {
        return mutOp.repair(program, node)
    }
}
