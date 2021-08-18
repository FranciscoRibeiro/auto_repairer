package repair

import AlternativeProgram
import BuggyProgram
import MaskedProgram
import com.github.javaparser.JavaToken
import com.github.javaparser.JavaToken.*
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import fault_localization.FaultLocalizationType
import fault_localization.reports.FLComponent
import fault_localization.reports.sfl.SFLComponent
import java.io.File

class MaskGenerator: RepairStrategy() {
    override fun repair(program: BuggyProgram, basedOn: FaultLocalizationType): Sequence<AlternativeProgram> {
        val alts = program.mostLikelyFaulty(basedOn, 500) // List of lists: the outer list ranks components (lines) by prob and each inner list has all the components for that spot in the ranking
                .map { comps -> comps.filterIsInstance<SFLComponent>() } // Cast to SFLComponent
                .map { lines -> lines.map { it to program.nodesInLine(it) } } // List of lists: each inner list now holds pairs of (SFLComponent, [node]). The snd element are the nodes in that line (SFLComponent)
                .map { spot -> spot.map { it.first to getTokens(it.second)} }
                .flatMap { createMasks(program, it) }

        return alts
                /*.map { createMutants(program, it) } // createMutants receives each spot "flattened", meaning all nodes from all lines in that spot are passed together because they have the same probability of containing the bug. Returns list of lists, each inner list has pairs (node, [node]) associating a node (orig prog) to its produced mutants
                .flatMap { modifyComponent2(program, it) } // creates all the possible alternatives*/
    }

    private fun createMasks(program: BuggyProgram,
                            compsAndTokens: Sequence<Pair<SFLComponent, Sequence<JavaToken>>>)
            : Sequence<MaskedProgram> {
        return compsAndTokens.flatMap {
            (comp, tokens) -> tokens.mapNotNull { createMaskedProgram(program, comp, it) } }
    }

    private fun createMaskedProgram(program: BuggyProgram, comp: FLComponent, token: JavaToken): MaskedProgram? {
        val ast = program.setAST(comp) ?: return null
//        return MaskedProgram(token, ast.clone())
        return MaskedProgram(token, ast)
    }

    /*private fun getTokens(program: BuggyProgram, comp: FLComponent, nodes: Sequence<Node>): Sequence<JavaToken> {
        val ast = program.setAST(comp) ?: return emptySequence()

    }*/

    private fun getTokens(nodes: Sequence<Node>): Sequence<JavaToken> {
        return nodes.flatMap { it.tokenRange.orElse(null) ?: emptyList() }
                .distinct()
                .filter { isCategory(it.category, Category.OPERATOR, Category.IDENTIFIER, Category.LITERAL) }
                .filter { !isMethodToken(it) }
    }

    private fun isMethodToken(token: JavaToken): Boolean {
        val nextToken = token.nextToken.orElse(null)
        return if(token.category == Category.IDENTIFIER && nextToken != null){
            nextToken.text == "("
        } else false
    }

    private fun isCategory(category: Category, vararg categories: Category): Boolean {
        return categories.any { it == category }
    }
}

fun inLine(ast: CompilationUnit, lineNr: Int): List<Node> {
    return ast.findAll(Node::class.java, { it.begin.get().line == lineNr && it.end.get().line == lineNr })
}

fun main(args: Array<String>) {
    val filename = args[0]
    val lineNr = args[1].toInt()
    val ast = StaticJavaParser.parse(File(filename))
    val nodes = inLine(ast, lineNr)
    val tokens = nodes.flatMap { it.tokenRange.get() }.distinct().filter { it.category == Category.OPERATOR}

    for (token in tokens){
        for (origToken in ast.tokenRange.get()){
            if(origToken === token) { print("<mask>") }
            else { print(origToken.text) }
        }
    }

//    println(tokens)
    println("end")
}