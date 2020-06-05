package repair

import AlternativeProgram
import Alternatives
import BuggyProgram
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import fault_localization.FaultLocalizationType
import fault_localization.FaultLocalizationType.SFL
import repair.mutators.MutatorRepair

class BruteForceRepair: RepairStrategy() {
    override fun repair(program: BuggyProgram, basedOn: FaultLocalizationType): Sequence<AlternativeProgram> {
        val x = program.mostLikelyFaulty(basedOn, 3)
                        .map { it.map { it to program.nodesInLine(it) } }
//                      .map { createMutants(program, it.second) }
                        .map { createMutants(program, it.flatMap { it.second }) }
                        .flatMap { modifyComponent(program, it) }

//        val y = x.toList()
        return x
//        return y.asSequence()

//        return when(basedOn){
//            SFL -> Alternatives(program,
//                                faultyComponents.asSequence()
//                                        .map<List<Int>, List<Pair<Int, List<Node>>>> { it.map { it to program.nodesInLine(it) } }
////                                        .map { createMutants(program, it.second) }
//                                        .map { createMutants(program, it.flatMap { it.second }) }
//                                        .flatMap { modifyComponent(program, it) }.toList())
//            else -> null
//        }
    }

//    private fun modifyComponent(program: BuggyProgram, modifications: Sequence<Pair<Node, List<Node>>>): Sequence<AlternativeProgram> {
//        return modifications.flatMap { buildAlternatives(program, it.first, it.second) }
//    }

//    private fun buildAlternatives(buggyProgram: BuggyProgram, originalNode: Node, mutantNodes: List<Node>): Sequence<AlternativeProgram> {
//        val alternatives = mutableListOf<AlternativeProgram>()
//        val tree = buggyProgram.getOriginalTree()
//        val nodeToReplace = findEqualNode(tree, originalNode) ?: return emptyList()
//        var temporaryNode = nodeToReplace
//        for(mutant in mutantNodes){
//            temporaryNode.replace(mutant)
//            alternatives.add(AlternativeProgram(mutant, tree.clone()))
//            temporaryNode = mutant
//        }
//
//        return alternatives
//    }
//
//    private fun findEqualNode(tree: CompilationUnit, nodeToFind: Node): Node? {
//        val maybeNode = tree.findFirst(nodeToFind::class.java, { isSameNode(it, nodeToFind) })
//        return if(maybeNode.isPresent) maybeNode.get() else null
//    }

//    private fun isSameNode(someNode: Node, nodeToFind: Node): Boolean {
//        if(someNode == nodeToFind){
//            val someNodeRange = someNode.range.orElse(null) ?: return false
//            val nodeToFindRange = nodeToFind.range.orElse(null) ?: return false
//            return someNodeRange == nodeToFindRange
//        } else return false
//    }

//    private fun createMutants(program: BuggyProgram, nodes: List<Node>): List<Pair<Node, List<Node>>> {
//        val mutOps = nodes.flatMap { mutators[it.javaClass] ?: emptyList() }
//                .sortedBy { it.rank }
//        return nodes.map { it to mutate(program, it) }
//    }

    private fun createMutants(program: BuggyProgram, nodes: Sequence<Node>): Sequence<Pair<Node, List<Node>>> {
//        val mutOps = nodes.map { it to (mutators[it.javaClass] ?: emptyList()) }
        return nodes.flatMap { pairWithMutOp(it) }//.asSequence()
                    .sortedBy { it.second.rank }
                    .map { it.first to mutate(program, it.second, it.first) }
                    .filter { it.second.isNotEmpty() }//.toList()
//                    .groupBy { it.first }
//                    .map { it.key to it.value.flatMap { it.second } }.toList()
//        return nodes.map { it to mutate(program, it) }
    }

    private fun pairWithMutOp(node: Node): Sequence<Pair<Node, MutatorRepair<*>>> {
        val mutOps = mutators[node.javaClass] ?: return emptySequence()
        return mutOps.asSequence().map { node to it }
//        return (mutators[node.javaClass] ?: emptySequence()).map { node to it }
    }

    private fun mutate(program: BuggyProgram, mutOp: MutatorRepair<*>, node: Node): List<Node> {
        return mutOp.repair(program, node)
    }

//    private fun mutate(program: BuggyProgram, node: Node): List<Node> {
//        return mutators[node.javaClass]?.flatMap { it.repair(program, node) } ?: emptyList()
//    }
}
