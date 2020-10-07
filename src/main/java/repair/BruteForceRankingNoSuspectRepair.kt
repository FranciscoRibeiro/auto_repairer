package repair

import AlternativeProgram
import BuggyProgram
import com.github.javaparser.ast.Node
import fault_localization.FaultLocalizationType
import fault_localization.reports.sfl.SFLComponent
import repair.mutators.MutatorRepair

class BruteForceRankingNoSuspectRepair: RepairStrategy() {
    override fun repair(program: BuggyProgram, basedOn: FaultLocalizationType): Sequence<AlternativeProgram> {
        val compsAndNodes = program.mostLikelyFaulty(basedOn, 5) // List of lists: the outer list ranks lines by prob and each inner list has all the components for that spot in the ranking
                .flatten() // This strategy ignores the probability, so we flatten into a list of components
                .filterIsInstance<SFLComponent>() // Cast to SFLComponent
                .map { line -> line to program.nodesInLine(line) }

        val compsNodesAndMuts = createMutants(program, compsAndNodes)
        return modifyComponent2(program, compsNodesAndMuts)
    }

    private fun createMutants(program: BuggyProgram,
                              compsAndNodes: Sequence<Pair<SFLComponent, Sequence<Node>>>)
            : Sequence<Pair<SFLComponent, Sequence<Pair<Node, List<Node>>>>> {
        return compsAndNodes
                .map { (line, nodes) -> line to nodes.flatMap { pairWithMutOp(it) } }
                .flatMap { apart(it) }
                .sortedBy { (_, nodeMutOp) -> nodeMutOp.second.rank }
                .map {
                    (line, nodeMutOp) ->
                    line to (nodeMutOp.first to mutate(program, nodeMutOp.second, nodeMutOp.first))
                }
                .groupPairs()
                .map {
                    (line, nodesAndMutants) ->
                    line to nodesAndMutants.filter { (_, mutants) -> mutants.isNotEmpty() }
                }
    }

    private fun mutate(program: BuggyProgram, mutOp: MutatorRepair<*>, node: Node): List<Node> {
        return mutOp.repair(program, node)
    }
}
