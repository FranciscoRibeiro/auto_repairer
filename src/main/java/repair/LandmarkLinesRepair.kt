package repair

import AlternativeProgram
import BuggyProgram
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.NameExpr
import fault_localization.FaultLocalizationType
import fault_localization.reports.qsfl.Landmark
import fault_localization.reports.qsfl.Line
import fault_localization.reports.qsfl.Parameter
import repair.mutators.MutatorRepair

class LandmarkLinesRepair: RepairStrategy() {
    override fun repair(program: BuggyProgram, basedOn: FaultLocalizationType): Sequence<AlternativeProgram> {
        val landmarks = program.mostLikelyFaulty(basedOn, 5)
                                .map { it.map { program.nodeInfo(it) } }
                                .map { it.filterIsInstance<Landmark>() }
                                //.flatten()

        val lines = program.mostLikelyFaulty(basedOn, 5)
                            .map { it.map { program.nodeInfo(it) } }
                            .map { it.filterIsInstance<Line>() }
                            .map { it.map { it.line } }

        val alts = lines.map { nodesInLines(program, it) }
                                .map { pairNodesWithMutOps(it) }
                                .map { sortByMutOpRanking(it) }
                                .map { bubbleUp(program, it, landmarks) }
                                .map { createMutants(program, it) }
                                .flatMap { modifyComponent(program, it) }

        return alts


//        val alts = program.mostLikelyFaulty(basedOn, 2)
//                        .map { it.map { program.nodeInfo(it) } }
//                        .map { it.filterIsInstance<Landmark>() }
//                        .map { it.map { program.findNodesIndirectly(it) } }
//                        .map { it.flatten() }
//                        .map { createMutants(program, it) }
//                        .filter { it.any() }
//                        .flatMap { modifyComponent(program, it) }
//
//        return alts
    }

    private fun containsVars(node: Node, nameExprs: Sequence<NameExpr>): Boolean {
        return nameExprs.any { ne -> node.findAll(NameExpr::class.java, { it == ne }).isNotEmpty() }
    }

    private fun bubbleUp(program: BuggyProgram, nodesAndMutOps: Sequence<Pair<Node, MutatorRepair<*>>>, landmarks: Sequence<Sequence<Landmark>>): Sequence<Pair<Node, MutatorRepair<*>>> {
        val varNames = landmarks.map { it.mapNotNull { program.nodeInfo(it.parentId) } }
                                .map { it.filterIsInstance<Parameter>() }
                                .map { it.map { NameExpr(it.name) } }

//        val involveVars = nodesAndMutOps.filter { containsVars(it.first, varNames) }
        var totalContains = mutableListOf<Pair<Node, MutatorRepair<*>>>() //iteratively add nodes containing landmark vars to this list
        var auxNoContains = nodesAndMutOps.toList() //change this list in every iteration so that it keeps shrinking; each iteration removes some nodes which contain landmark vars
        for (vn in varNames){
            val (contains, noContains) = auxNoContains.partition { containsVars(it.first, vn) }
//        val (contains, noContains) = nodesAndMutOps.partition { containsVars(it.first, varNames) }
            totalContains.addAll(contains) //add nodes with vars
            auxNoContains = noContains //update list to most recent "non-containing" list
        }

//        val noInvolveVars = nodesAndMutOps.asIterable().subtract(involveVars.asIterable())
//        val noInvolveVars = nodesAndMutOps.toList().subtract(involveVars.toList())
//        return (involveVars + noInvolveVars)
        return (totalContains + auxNoContains).asSequence()
//        return (contains + noContains).asSequence()
    }

    private fun sortByMutOpRanking(nodesAndMutOps: Sequence<Pair<Node, MutatorRepair<*>>>): Sequence<Pair<Node, MutatorRepair<*>>> {
        return nodesAndMutOps.sortedBy { it.second.rank }
    }

    private fun pairNodesWithMutOps(nodes: Sequence<Node>): Sequence<Pair<Node, MutatorRepair<*>>> {
        return nodes.flatMap { pairWithMutOp(it) }
    }

    private fun nodesInLines(program: BuggyProgram, lineNrs: Sequence<Int>): Sequence<Node> {
        return lineNrs.flatMap { program.nodesInLine(it) }
    }

    private fun createMutants(program: BuggyProgram, nodesAndMutOps: Sequence<Pair<Node, MutatorRepair<*>>>): Sequence<Pair<Node, List<Node>>> {
        return nodesAndMutOps.map { it.first to mutate(program, it.second, it.first) }
                                .filter { it.second.isNotEmpty() }
    }

    private fun mutate(program: BuggyProgram, mutOp: MutatorRepair<*>, node: Node): List<Node> {
        return mutOp.repair(program, node)
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

    private fun <A, B> apart(pair: Pair<A,Sequence<B>> ): Sequence<Pair<A,B>> {
        return pair.second.map { pair.first to it }
    }
}
