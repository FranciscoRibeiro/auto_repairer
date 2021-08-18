import com.github.javaparser.JavaToken
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.TokenRange
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.*
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

private fun sameLine(token: JavaToken, lineNr: Int): Boolean {
    val tokenRange = token.range.orElse(null) ?: return false
    return tokenRange.begin.line == lineNr && tokenRange.end.line == lineNr
}

private fun intendedCategory(token: JavaToken): Boolean {
    return isCategory(token.category, JavaToken.Category.OPERATOR, JavaToken.Category.IDENTIFIER, JavaToken.Category.LITERAL)
}

private fun getTokens(ast: CompilationUnit, lineNr: Int): List<JavaToken> {
    val tokens = ast.tokenRange.orElse(null) ?: return emptyList()
    val tokensInLine = mutableListOf<JavaToken>()
    for(t in tokens){
        if(sameLine(t, lineNr) && intendedCategory(t) && !isMethodToken(t)){
            tokensInLine.add(t)
        }
    }
    return tokensInLine
}

private fun getNodesAndTokens(ast: CompilationUnit, lineNr: Int): List<Pair<Node, List<JavaToken>>> {
    return ast.findAll(Node::class.java, { it.begin.get().line == lineNr && it.end.get().line == lineNr })
            .filter { isVar(it) || it is LiteralExpr || it is BinaryExpr }
            .map { Pair(it, it.tokenRange.get().toList()) }
}

fun isVar(node: Node): Boolean {
    return when(node){
        is FieldAccessExpr -> true
        is SimpleName -> !isMethodName(node)
        else -> false
    }
}

fun isMethodName(node: SimpleName): Boolean {
    val parent = node.parentNode.orElse(null) ?: return false
    return parent is MethodCallExpr
}

private fun generateMasks(filePath: File, lineNr: Int, outputDir: File) {
    val ast = StaticJavaParser.parse(filePath)
    val tokens = getTokens(ast, lineNr)
    writeMasks(ast, tokens, outputDir)
}

private fun generateMasksLine(filePath: File, lineNr: Int, outputDir: File) {
    var maskNr = 0
    var strContent = ""
    val lines = filePath.readLines()
    for(i in lines.indices){
        if(i == lineNr-1) strContent += "<mask>;\n"
        else strContent += lines[i] + "\n"
    }
    if(!outputDir.exists()) outputDir.mkdirs()
    File(outputDir, "${maskNr++}.java").writeText(strContent)
}

private fun generateMasksNodes(filePath: File, lineNr: Int, outputDir: File) {
    val ast = StaticJavaParser.parse(filePath)
    val nodesAndTokens = getNodesAndTokens(ast, lineNr)
    writeMasksNodes(ast, nodesAndTokens, outputDir)
}

private fun writeMasks(ast: CompilationUnit, tokens: List<JavaToken>, outputDir: File) {
    val tokenRange = ast.tokenRange.orElse(null) ?: return
    var strContent: String
    var maskNr = 0
    for(t in tokens){
        strContent = ""
        for(origToken in tokenRange){
            if(t === origToken) strContent += "<mask>"
            else strContent += origToken.text
        }
        if(!outputDir.exists()) outputDir.mkdirs()
        File(outputDir, "${maskNr++}.java").writeText(strContent)
    }
}

private fun exactlyIn(tok: JavaToken, tokens: List<JavaToken>): Boolean {
    for(token in tokens){
        if(tok === token) return true
    }
    return false
}

fun maskFieldAccess(tokenRange: TokenRange, tokens: List<JavaToken>): String {
    var strContent = ""
    var masked = false
    for(origToken in tokenRange){
        if(exactlyIn(origToken, tokens)) {
            if (!masked) {
                masked = true
                strContent += "<mask>"
            }
        } else strContent += origToken.text
    }
    return strContent
}

fun maskSimple(tokenRange: TokenRange, tokens: List<JavaToken>): String {
    if(tokens.size > 1) return ""
    var strContent = ""
    for(origToken in tokenRange){
        if(exactlyIn(origToken, tokens)) strContent += "<mask>"
        else strContent += origToken.text
    }
    return strContent
}

fun maskBinExpr(binExpr: BinaryExpr, tokenRange: TokenRange, tokens: List<JavaToken>): String {
    var strContent = ""
    var skipOps = false
    for(origToken in tokenRange){
        if(!skipOps && exactlyIn(origToken, tokens) && origToken.category == JavaToken.Category.OPERATOR
                && !opIn(origToken, binExpr.left) && !opIn(origToken, binExpr.right)) {
                    if(origToken.text == ">" && origToken.nextToken.isPresent
                            && origToken.nextToken.get().text == ">") skipOps = true
                    strContent += "<mask>"
        } else if(skipOps && origToken.nextToken.isPresent) {
            skipOps = origToken.nextToken.get().text == ">"
        }
        else strContent += origToken.text
    }
    return strContent
}

fun opIn(tok: JavaToken, expression: Expression): Boolean {
    val tokens = expression.tokenRange.orElse(null).toList() ?: return false
    for(t in tokens){
        if(exactlyIn(tok, tokens)) return true
    }
    return false
}

fun maskNode(node: Node, tokenRange: TokenRange, tokens: List<JavaToken>): String {
    return when(node){
        is FieldAccessExpr -> maskFieldAccess(tokenRange, tokens)
        is SimpleName -> maskSimple(tokenRange, tokens)
        is LiteralExpr -> maskSimple(tokenRange, tokens)
        is BinaryExpr -> maskBinExpr(node, tokenRange, tokens)
        else -> ""
    }
}

fun writeMasksNodes(ast: CompilationUnit, nodesAndTokens: List<Pair<Node, List<JavaToken>>>, outputDir: File) {
    println("writing masks nodes")
    if(nodesAndTokens.isEmpty()) println("empty")
    val tokenRange = ast.tokenRange.orElse(null) ?: return
    var strContent: String
    var maskNr = 0
    for(nt in nodesAndTokens){
        strContent = maskNode(nt.first, tokenRange, nt.second)
        if(!outputDir.exists()) outputDir.mkdirs()
        println("saving mask")
        File(outputDir, "${maskNr++}.java").writeText(strContent)
        println("mask saved")
    }
}

private fun oneShot(filePath: String, lineNr: Int, outputDir: String) {
//    generateMasks(File(filePath), lineNr, File(outputDir))
    println("oneshot")
    generateMasksNodes(File(filePath), lineNr, File(outputDir))
}

private fun bulk(linesDir: String, buggyDir: String, outputDir: String) {
    val idSuffix = if(linesDir.contains("bugs_dot_jar")) "" else "b"
    val projs = File(linesDir).listFiles() ?: return
    for (p in projs) {
        val buggyLinesFiles = p.listFiles() ?: return
        for (file in buggyLinesFiles) {
            val id = file.name.split(".")[0]
            val lines = file.readLines()
            for (line in lines) {
                val fields = line.split("#")
                val (location, lineNr) = Pair(fields[0], fields[1].toInt())
                val baseFile = location.split("/").last()
                val buggyFile = File(buggyDir, "${p.name}_${id}${idSuffix}/$baseFile")
                if(buggyFile.exists()) { //bug may be deprecated and buggy file does not exist
                    val maskSubDir = File(outputDir, "${p.name}_${id}${idSuffix}/$lineNr")
//                    generateMasks(buggyFile, lineNr, maskSubDir)
//                    generateMasksLine(buggyFile, lineNr, maskSubDir)
                    generateMasksNodes(buggyFile, lineNr, maskSubDir)
                } else println("Buggy file $buggyFile does not exist")
            }
        }
    }
}

fun main(args: Array<String>) {
    when (args[0]) {
        "-bulk" -> bulk(args[1], args[2], args[3])
        "-single" -> oneShot(args[1], args[2].toInt(), args[3])
        else -> errorOut("Incorrect Usage")
    }

    println("end")
}
