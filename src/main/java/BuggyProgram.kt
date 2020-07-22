import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.CallableDeclaration
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter.setup
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import fault_localization.FaultLocalizationType
import fault_localization.FaultLocalizationType.QSFL
import fault_localization.FaultLocalizationType.SFL
import fault_localization.reports.SFLReport
import fault_localization.reports.qsfl.*
import java.io.File

class BuggyProgram(val path: String, val sourceFile: File) {
    val sflReport = SFLReport(File(path, "site/gzoltar/sfl"))
    val qsflReport = QSFLReport(File(path, "qsfl"))
    val tree = parseAndSolve()
    private val originalTree = ImmutableTree(tree.clone())

    private fun parseAndSolve(): CompilationUnit {
        StaticJavaParser.getConfiguration().setSymbolResolver(JavaSymbolSolver(CombinedTypeSolver(ReflectionTypeSolver())))
        return /*setup(*/StaticJavaParser.parse(sourceFile)/*)*/
    }

    fun mostLikelyFaulty(basedOn: FaultLocalizationType, upTo: Int = 1): Sequence<Sequence<Int>> {
        return when(basedOn){
            SFL -> sflReport.mostLikelyFaulty(upTo)
            QSFL -> qsflReport.mostLikelyFaulty(upTo)
        }
    }

    fun nodesInLine(line: Int): Sequence<Node> {
        return tree.findAll(Node::class.java, { isSameLine(it, line) }).asSequence()
    }

    fun nodesBeginInLine(line: Int): Sequence<Node> {
        return tree.findAll(Node::class.java, { isSameBeginLine(it, line) }).asSequence()
    }

    private fun isSameLine(node: Node?, line: Int): Boolean {
        if(node != null && node.range.isPresent){
            val nodeRange = node.range.get()
            return nodeRange.begin.line == line && nodeRange.end.line == line
        } else{ return false }
    }

    private fun isSameBeginLine(node: Node?, line: Int): Boolean {
        if(node != null && node.range.isPresent){
            val nodeRange = node.range.get()
            return nodeRange.begin.line == line
        } else{ return false }
    }

    fun getOriginalTree(): CompilationUnit {
        return originalTree.getTree()
    }

    fun nodeInfo(nodeId: Int): NodeInfo? {
        return qsflReport.nodeInfo(nodeId)
    }

    fun findNodes(landmark: Landmark): Sequence<Node> {
        var associated: List<Node> = emptyList()
        val paramNode = qsflReport.nodeInfo(landmark.parentId)
        if(paramNode != null && paramNode is Parameter){
            val methodNode = qsflReport.nodeInfo(paramNode.parentId)
            if (methodNode != null && methodNode is Method){
                val decl = tree.findFirst(CallableDeclaration::class.java, { hasNameAndParams(it, methodNode) })
                if(decl.isPresent){
                    val paramNameExpr = NameExpr(paramNode.name)
                    associated = decl.get()
                            .findAll(Node::class.java, { containsVar(it, paramNameExpr) })
                }
            }
        }
        return associated.asSequence()
    }

    fun findNodesIndirectly(landmark: Landmark): Sequence<Node> {
        return findNodes(landmark).map { getLine(it) }
                                .distinct().filter { it != 0 }
                                .flatMap { nodesBeginInLine(it) }
//        var associated: List<Node> = emptyList()
//        val paramNode = qsflReport.nodeInfo(landmark.parentId)
//        if(paramNode != null && paramNode is Parameter){
//            val methodNode = qsflReport.nodeInfo(paramNode.parentId)
//            if (methodNode != null && methodNode is Method){
//                val decl = tree.findFirst(CallableDeclaration::class.java, { hasNameAndParams(it, methodNode) })
//                if(decl.isPresent){
//                    val paramNameExpr = NameExpr(paramNode.name)
//                    associated = decl.get()
//                            .findAll(NameExpr::class.java, { it.nameAsString == paramNode.name })
//                            .map { getLine(it) }
//                            .distinct().filter { it != 0 }
//                            .flatMap { nodesInLine(it).toList() }
//                }
//            }
//        }
//        return associated.asSequence()
    }

    private fun containsVar(node: Node, nameExpr: NameExpr): Boolean {
        return node.findAll(NameExpr::class.java, { it == nameExpr }).isNotEmpty()
    }

    private fun getLine(n: Node): Int {
        return if(n.range.isPresent) n.range.get().begin.line else 0
    }

    private fun hasNameAndParams(decl: CallableDeclaration<*>, methodNode: Method): Boolean {
        return decl.name.asString() == methodNode.name
                && decl.parameters.size == methodNode.params.size
                && methodNode.params.zip(decl.parameters).all { it.first == it.second.typeAsString }
    }
}
