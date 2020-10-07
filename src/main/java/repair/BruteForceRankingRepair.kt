package repair

import AlternativeProgram
import BuggyProgram
import com.github.javaparser.ast.Node
import fault_localization.FaultLocalizationType
import fault_localization.reports.sfl.SFLComponent
import repair.mutators.MutatorRepair

class BruteForceRankingRepair: RepairStrategy() {
    override fun repair(program: BuggyProgram, basedOn: FaultLocalizationType): Sequence<AlternativeProgram> {
        val alts = program.mostLikelyFaulty(basedOn, 5)
                .map { comps -> comps.filterIsInstance<SFLComponent>() }
                .map { lines -> lines.map { it to program.nodesInLine(it) } }
                .map { createMutants(program, it) }
                .flatMap { modifyComponent2(program, it) }

        return alts
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
