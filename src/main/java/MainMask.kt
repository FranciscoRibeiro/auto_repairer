import com.github.javaparser.JavaToken
import com.github.javaparser.ast.Node
import fault_localization.reports.sfl.SFLComponent
import java.io.File

private fun isMethodToken(token: JavaToken): Boolean {
    val nextToken = token.nextToken.orElse(null)
    return if(token.category == JavaToken.Category.IDENTIFIER && nextToken != null){
        nextToken.text == "("
    } else false
}

private fun isCategory(category: JavaToken.Category, vararg categories: JavaToken.Category): Boolean {
    return categories.any { it == category }
}

private fun getTokens(nodes: Sequence<Node>): Sequence<JavaToken> {
    return nodes.flatMap { it.tokenRange.orElse(null) ?: emptyList() }
            .distinct()
            .filter { isCategory(it.category, JavaToken.Category.OPERATOR, JavaToken.Category.IDENTIFIER, JavaToken.Category.LITERAL) }
            .filter { !isMethodToken(it) }
}

fun main(args: Array<String>) {
    if(args.size != 5) errorOut("Incorrect Usage")

    val (basePath, srcPath, outputDir) = Triple(args[0], args[1], args[2])
    val (strategy, reportPath) = Pair(args[3], args[4])
    val repairStrategy = getRepairStrategy(strategy) ?: errorOut("Invalid strategy: $strategy")
    val flType = getFLType(strategy) ?: errorOut("Invalid FL type")
    val report = loadReport(flType, reportPath)
    val program = BuggyProgram(srcPath, report)
    var counter = 0
    var compCounter = 0
    program.mostLikelyFaulty(flType, 500)
            .map { comps -> comps.filterIsInstance<SFLComponent>() } // Cast to SFLComponent
            .map { lines -> lines.map {
                println(compCounter); compCounter++
                it to program.nodesInLine(it) }
            } // List of lists: each inner list now holds pairs of (SFLComponent, [node]). The snd element are the nodes in that line (SFLComponent)
            .map { spot -> spot.map { it.first to getTokens(it.second)} }
            .flatten()
            .forEach { counter = createMaskedFiles(outputDir, program, it, counter) }

    println("end")
}

fun createMaskedFiles(outputDir: String, program: BuggyProgram, compAndTokens: Pair<SFLComponent, Sequence<JavaToken>>, counter: Int): Int {
    var localCounter = counter
    val ast = program.setAST(compAndTokens.first) ?: return localCounter
    for (token in compAndTokens.second){
        var strContent = ""
        val ts = ast.tokenRange.orElse(null) ?: return localCounter
        for (t in ts){
            if(t === token) strContent += "<mask>"
            else strContent += t.text
        }
        File(outputDir, "$localCounter.java").writeText(strContent)
        localCounter++
    }
    return localCounter
}
