package repair

import AlternativeProgram
import BuggyProgram
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.IntegerLiteralExpr
import fault_localization.FaultLocalizationType
import fault_localization.reports.qsfl.Landmark
import repair.mutators.ConditionalOperatorInsertion
import repair.mutators.IntConstantModification
import repair.mutators.MutatorRepair
import repair.mutators.RelationalOperatorReplacement
import repair.mutators.utils.isRelational

class LandmarkStrictAdHocRepair: RepairStrategy() {
    override fun repair(program: BuggyProgram, basedOn: FaultLocalizationType): Sequence<AlternativeProgram> {
        val alts = program.mostLikelyFaulty(basedOn, 5)
//                        .map { it.map { program.nodeInfo(it) } }
                        .map { it.filterIsInstance<Landmark>() }
                        .map { landmarks -> landmarks.map { it to program.findNodes(it) } } //Only select the nodes that are "strictly coupled" to landmark variables
//                        .map { it.map { program.findNodesIndirectly(it) } }
//                        .map { it.flatten() }
                        .map { createMutants(program, it) }
//                        .filter { it.any() }
                        .flatMap { modifyComponent2(program, it) }

        return alts

//                        .map { it.map { it.fullRelation to program.findNodes(it) } }
//                        .flatMap { it.map { apart(it) } }
//                        .map { createMutants(program, it) }
//                        .filter { it.any() }
//                        .flatMap { modifyComponent(program, it) }
    }

    /*private fun createMutants(program: BuggyProgram, nodes: Sequence<Node>): Sequence<Pair<Node, List<Node>>> {
        return nodes.flatMap { pairWithMutOp(it) }
                .mix()
//                .sortedBy { it.second.rank }
                .map { it.first to mutate(program, it.second, it.first) }
                .filter { it.second.isNotEmpty() }
    }*/

    private fun createMutants(program: BuggyProgram,
                              compsAndNodes: Sequence<Pair<Landmark, Sequence<Node>>>)
            : Sequence<Pair<Landmark, Sequence<Pair<Node, List<Node>>>>> {
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

//    private fun createMutants(program: BuggyProgram, relAndNodes: Sequence<Pair<String, Node>>): Sequence<Pair<Node, List<Node>>> {
////        return relAndNodes.map { (rel, node) -> node to mutate(program, rel, node) }
//        return relAndNodes.map { (rel, node) -> node to mutate(program, node) }
//                        .filter { it.second.isNotEmpty() }
//    }

//    private fun mutate(program: BuggyProgram, node: Node): List<Node> {
//        val (rel, pivot) = relAndPivot(relation)
//        return when(node) {
//            is BinaryExpr -> {
//                val rn = node.right
//                return if(rn is IntegerLiteralExpr && rn.asInt() != pivot.toInt()) {
//                    IntConstantModification(pivot.toInt()).repair(program, rn).map { BinaryExpr(node.left.clone(), (it as Expression), node.operator) }
//                }
//                else if(isRelational(node.operator)) {
//                    val relOp = toRelOp(rel) ?: return emptyList()
//
//                    return if(relOp == node.operator) emptyList()
//                    else if(incorporatesRelation(node.operator, relOp)){
//                        val newOp = subtractRelation(node.operator, relOp) ?: return emptyList()
//                        RelationalOperatorReplacement(newOp).repair(program, node)
//                    }
//                    else {
//                        val landmarkExpr = BinaryExpr(node.left, IntegerLiteralExpr(pivot), relOp)
//                        ConditionalOperatorInsertion(BinaryExpr.Operator.OR, landmarkExpr).repair(program, node)
//                    }
//                }
//                else emptyList()
//            }
//            else -> emptyList()
//        }
//    }

    private fun toRelOp(rel: String): BinaryExpr.Operator? {
        return when(rel){
            "eq" -> BinaryExpr.Operator.EQUALS
            "lt" -> BinaryExpr.Operator.LESS
            "gt" -> BinaryExpr.Operator.GREATER
            else -> null
        }
    }

    private fun subtractRelation(op: BinaryExpr.Operator, subtract: BinaryExpr.Operator): BinaryExpr.Operator? {
        return when {
            op == BinaryExpr.Operator.LESS_EQUALS && subtract == BinaryExpr.Operator.LESS -> BinaryExpr.Operator.EQUALS
            op == BinaryExpr.Operator.LESS_EQUALS && subtract == BinaryExpr.Operator.EQUALS -> BinaryExpr.Operator.LESS
            op == BinaryExpr.Operator.GREATER_EQUALS && subtract == BinaryExpr.Operator.GREATER -> BinaryExpr.Operator.EQUALS
            op == BinaryExpr.Operator.GREATER_EQUALS && subtract == BinaryExpr.Operator.EQUALS -> BinaryExpr.Operator.GREATER
            else -> null
        }
    }

    private fun incorporatesRelation(op: BinaryExpr.Operator, toCheck: BinaryExpr.Operator): Boolean {
        return when(toCheck){
            BinaryExpr.Operator.EQUALS -> op == BinaryExpr.Operator.LESS_EQUALS || op == BinaryExpr.Operator.GREATER_EQUALS
            BinaryExpr.Operator.LESS -> op == BinaryExpr.Operator.LESS_EQUALS
            BinaryExpr.Operator.GREATER -> op == BinaryExpr.Operator.GREATER_EQUALS
            else -> false
        }
    }

    private fun relAndPivot(relation: String): Pair<String, String> {
        if(relation == "IsNull") return Pair("eq", "null")
        else if(relation == "NotNull") return Pair("ne", "null")
        else if(relation == "false" || relation == "true") return Pair("eq", relation)
        else {
            val tokens = relation.split(" ")
            return Pair(tokens[0], tokens[1])
        }
    }
}
