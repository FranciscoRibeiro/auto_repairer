package repair

import AlternativeProgram
import Alternatives
import BuggyProgram
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import fault_localization.FaultLocalizationType
import fault_localization.FaultLocalizationType.SFL

class BruteForceRepair: RepairStrategy() {
    override fun repair(program: BuggyProgram, basedOn: FaultLocalizationType): Alternatives? {
        val faultyComponents: List<Int> = program.mostLikelyFaulty(basedOn)
        return when(basedOn){
            SFL -> Alternatives(program,
                                faultyComponents.asSequence()
                                        .map { it to program.nodesInLine(it) }
                                        .map { createMutants(it.second) }
                                        .flatMap { modifyComponent(program, it) }.toList())
            else -> null
        }
    }

    private fun modifyComponent(program: BuggyProgram, modifications: Map<Node, List<Node>>): Sequence<AlternativeProgram> {
        return modifications.flatMap { buildAlternatives(program, it.key, it.value) }.asSequence()
    }

    private fun buildAlternatives(buggyProgram: BuggyProgram, originalNode: Node, mutantNodes: List<Node>): List<AlternativeProgram> {
        val alternatives = mutableListOf<AlternativeProgram>()
        //val tree = buggyProgram.tree
        val tree = buggyProgram.getOriginalTree()
        val nodeToReplace = findEqualNode(tree, originalNode) ?: return emptyList()
        var temporaryNode = nodeToReplace
        for(mutant in mutantNodes){
            temporaryNode.replace(mutant)
            alternatives.add(AlternativeProgram(mutant, tree.clone()))
            temporaryNode = mutant
        }

        /*var temporaryNode = originalNode
        for(mutant in mutantNodes){
            temporaryNode.replace(mutant)
            alternatives.add(AlternativeProgram(mutant, tree.clone()))
            temporaryNode = mutant
        }*/

        return alternatives
    }

    private fun findEqualNode(tree: CompilationUnit, nodeToFind: Node): Node? {
        val maybeNode = tree.findFirst(nodeToFind::class.java, { it.equals(nodeToFind) })
        return if(maybeNode.isPresent) maybeNode.get() else null
    }

    private fun createMutants(nodes: List<Node>): Map<Node, List<Node>> {
        return nodes.map { it to mutate(it) }.toMap()
    }

    private fun mutate(node: Node): List<Node> {
        return mutators[node.javaClass]?.flatMap { it.repair(node) } ?: emptyList()
    }
}