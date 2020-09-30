import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.CallableDeclaration
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import fault_localization.FaultLocalizationType
import fault_localization.reports.FLComponent
import fault_localization.reports.FLReport
import fault_localization.reports.qsfl.*

class BuggyProgram(val srcPath: String) {
    lateinit var flReport: FLReport
    val tree = parseAndSolve()
    private val originalTree = ImmutableTree(tree.clone())

    constructor(srcPath: String, flReport: FLReport): this(srcPath){
        this.flReport = flReport
    }

    private fun parseAndSolve(): CompilationUnit {
        StaticJavaParser.getConfiguration().setSymbolResolver(JavaSymbolSolver(CombinedTypeSolver(ReflectionTypeSolver())))
//        return /*setup(*/StaticJavaParser.parse(sourceFile)/*)*/
        return /*setup(*/StaticJavaParser.parse(srcPath)/*)*/
    }

    fun mostLikelyFaulty(basedOn: FaultLocalizationType, upTo: Int = 1): Sequence<Sequence<FLComponent>> {
        return flReport.mostLikelyFaulty(upTo)
        /*return when(basedOn){
            SFL -> sflReport.mostLikelyFaulty(upTo)
            QSFL -> qsflReport.mostLikelyFaulty(upTo)
        }*/
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

    fun nodeInfo(nodeId: Int): QSFLNode? {
        val qsflReport = flReport as? QSFLReport ?: return null
        return qsflReport.nodeInfo(nodeId)
    }

    fun findNodes(landmark: Landmark): Sequence<Node> {
        val qsflReport = flReport as? QSFLReport ?: return emptySequence()
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
