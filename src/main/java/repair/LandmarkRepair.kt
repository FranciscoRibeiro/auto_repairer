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
import repair.mutators.RelationalOperatorReplacement
import repair.mutators.utils.isRelational
import java.lang.NumberFormatException

class LandmarkRepair: RepairStrategy() {
    override fun repair(program: BuggyProgram, basedOn: FaultLocalizationType): Sequence<AlternativeProgram> {
        return program.mostLikelyFaulty(basedOn, 5)
//                        .map { it.map { program.nodeInfo(it) } }
                        .map { it.filterIsInstance<Landmark>() }
//                        .map { it.map { it.name to program.findNodes(it) } }
                .map { landmarks -> landmarks.map { it to program.findNodes(it) } }
                        .flatMap { it.map { apart(it) } }
                        .map { createMutants(program, it) }
                        .filter { it.any() }
                        .flatMap { modifyComponent2(program, it) }
    }

    /*private fun createMutants(program: BuggyProgram, relAndNodes: Sequence<Pair<String, Node>>): Sequence<Pair<Node, List<Node>>> {
        return relAndNodes.map { (rel, node) -> node to mutate(program, rel, node) }
                        .filter { it.second.isNotEmpty() }
    }*/

    private fun createMutants(program: BuggyProgram,
                              relAndNodes: Sequence<Pair<Landmark, Node>>)
            : Sequence<Pair<Landmark, Sequence<Pair<Node, List<Node>>>>> {
        return relAndNodes.map { (landmark, node) -> landmark to (node to mutate(program, landmark.name, node)) }
                .filter { it.second.second.isNotEmpty() }
                .groupPairs()
    }

    private fun mutate(program: BuggyProgram, relation: String, node: Node): List<Node> {
        val (rel, pivot) = relAndPivot(relation)
        return when(node) {
            is BinaryExpr -> {
                val pivotNumber = pivot.toIntOrNull() ?: return emptyList()
                val rn = node.right
                try {
                    return if(rn is IntegerLiteralExpr && rn.asNumber() != pivotNumber) {
                        IntConstantModification(pivot.toInt()).repair(program, rn).map { BinaryExpr(node.left.clone(), (it as Expression), node.operator) }
                    }
                    else if(isRelational(node.operator)) {
                        val relOp = toRelOp(rel) ?: return emptyList()

                        return if(relOp == node.operator) emptyList()
                        else if(incorporatesRelation(node.operator, relOp)){
                            val newOp = subtractRelation(node.operator, relOp) ?: return emptyList()
                            RelationalOperatorReplacement(newOp).repair(program, node)
                        }
                        else {
                            val landmarkExpr = BinaryExpr(node.left, IntegerLiteralExpr(pivot), relOp)
                            ConditionalOperatorInsertion(BinaryExpr.Operator.OR, landmarkExpr).repair(program, node)
                        }
                    }
                    else emptyList()
                } catch (e: NumberFormatException){
                    emptyList<Node>()
                }
            }
            else -> emptyList()
        }
    }

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
