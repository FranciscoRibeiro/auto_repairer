package repair

import AlternativeProgram
import BuggyProgram
import com.github.javaparser.ast.Node
import fault_localization.FaultLocalizationType
import fault_localization.reports.qsfl.Landmark
import repair.mutators.MutatorRepair

class LandmarkRankingRepair: RepairStrategy() {
    override fun repair(program: BuggyProgram, basedOn: FaultLocalizationType): Sequence<AlternativeProgram> {
        val alts = program.mostLikelyFaulty(basedOn, 5)
//                        .map { it.map { program.nodeInfo(it) } }
                        .map { it.filterIsInstance<Landmark>() }
//                        .map { it.map { program.findNodesIndirectly(it) } } //Select all the nodes in lines where landmark variables are present, even if they are not directly associated
                        .map { landmarks -> landmarks.map { it to program.findNodesIndirectly(it) } }
//                        .map { it.flatten() }
//                        .removeDups() //Different landmarks may cause the same node to be collected multiple times; remove duplicates to avoid producing the same mutations
                        .map { createMutants(program, it) }
//                        .filter { it.any() }
                        .flatMap { modifyComponent2(program, it) }

        return alts
    }

    /*private fun createMutants(program: BuggyProgram, nodes: Sequence<Node>): Sequence<Pair<Node, List<Node>>> {
        return nodes.flatMap { pairWithMutOp(it) }
                .sortedBy { it.second.rank }
                .map { it.first to mutate(program, it.second, it.first) }
                .filter { it.second.isNotEmpty() }
    }*/

    private fun createMutants(program: BuggyProgram,
                              compsAndNodes: Sequence<Pair<Landmark, Sequence<Node>>>)
            : Sequence<Pair<Landmark, Sequence<Pair<Node, List<Node>>>>> {
        return compsAndNodes
                .map { (line, nodes) -> line to nodes.flatMap { pairWithMutOp(it) } }
                .flatMap { apart(it) }
                .sortedBy { (_, nodeMutOp) -> nodeMutOp.second.rank }
//                .mix()
//                .map { (line, nodesAndMutOps) -> line to nodesAndMutOps.mix() }
                /*.map {
                    (line, nodesAndMutOps) ->
                    line to nodesAndMutOps.map { (node, mutOp) -> node to mutate(program, mutOp, node) }
                }*/
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
