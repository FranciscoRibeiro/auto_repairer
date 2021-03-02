import com.github.javaparser.ParserConfiguration
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.CallableDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import com.github.javaparser.utils.SourceRoot
import fault_localization.FaultLocalizationType
import fault_localization.reports.FLComponent
import fault_localization.reports.FLReport
import fault_localization.reports.morpheus.MorpheusComponent
import fault_localization.reports.morpheus.MorpheusReport
import fault_localization.reports.qsfl.*
import fault_localization.reports.sfl.SFLComponent
import repair.mutators.utils.resolveDecl
import java.io.File
import java.nio.file.Paths

class BuggyProgram(val srcPath: String) {
    private lateinit var flReport: FLReport
    private val allTrees = parseAndSolve()
    private lateinit var currentTree: CompilationUnit
    private lateinit var originalTree: ImmutableTree
    private lateinit var srcRoot: SourceRoot

    constructor(srcPath: String, flReport: FLReport): this(srcPath){
        this.flReport = flReport
    }

    private fun dependencyJars(): List<String> {
        val dependencyDir = System.getenv("HOME") + "/.m2/repository"
        return System.getProperty("java.class.path")
                .split(File.pathSeparator)
                .filter { it.endsWith(".jar") && it.startsWith(dependencyDir) }
    }

    private fun parseAndSolve(): List<CompilationUnit> {
        val jars = dependencyJars()
        val sources = Paths.get(srcPath)
        val combinedTypeSolver = CombinedTypeSolver(ReflectionTypeSolver(), JavaParserTypeSolver(srcPath))
        jars.forEach { combinedTypeSolver.add(JarTypeSolver(it)) }
        val config = ParserConfiguration()
                .setStoreTokens(true)
                .setSymbolResolver(JavaSymbolSolver(combinedTypeSolver))

        this.srcRoot = SourceRoot(sources, config)

        return this.srcRoot.tryToParse()
                .mapNotNull { it.result.orElse(null) }
        /*.filter { it.result.get() is ClassOrInterfaceDeclaration }*/


//        StaticJavaParser.getConfiguration().setSymbolResolver(JavaSymbolSolver(CombinedTypeSolver(ReflectionTypeSolver())))
//        return /*setup(*/StaticJavaParser.parse(sourceFile)/*)*/
//        return /*setup(*/StaticJavaParser.parse(srcPath)/*)*/
    }

    fun currentTreeFullPath(): String {
        val storage = currentTree.storage.orElse(null) ?: return ""
        return storage.path.toString()
    }

    fun mostLikelyFaulty(basedOn: FaultLocalizationType, upTo: Int = 1): Sequence<Sequence<FLComponent>> {
        return flReport.mostLikelyFaulty(upTo)
        /*return when(basedOn){
            SFL -> sflReport.mostLikelyFaulty(upTo)
            QSFL -> qsflReport.mostLikelyFaulty(upTo)
        }*/
    }

    fun nodesInLine(line: Int): Sequence<Node> {
        return currentTree.findAll(Node::class.java, { isSameLine(it, line) }).asSequence()
    }

    fun nodesInLine(component: FLComponent): Sequence<Node> {
        val (line, fileAST) = when(component){
            is SFLComponent -> Pair(component.line, getFileAST(component.packageName, component.simpleClassName))
            is Line -> {
                val className = getClassName(component)
                val packageName = getPackageName(component)
                if(className == null) Pair(-1, null)
                else Pair(component.line, getFileAST(packageName, className))
            }
            else -> Pair(-1, null)
        }
        return if(fileAST != null) {
            currentTree = fileAST
            nodesInLine(line)
        }
        else emptySequence()
    }

    private fun getPackageName(component: QSFLNode): String {
        //TODO: Complete
        return ""
    }

    private fun getFileAST(packageName: String, simpleClassName: String): CompilationUnit? {
        val fileASTs = allTrees.filter { tree -> hasFullClassName(tree, packageName, simpleClassName)}
        return if(fileASTs.size == 1) return fileASTs[0]
        else null
    }

    private fun hasFullClassName(tree: CompilationUnit, packageName: String, simpleClassName: String): Boolean {
        return hasPackageName(tree, packageName) && hasSimpleClassName(tree, simpleClassName)
    }

    private fun hasPackageName(tree: CompilationUnit, packageName: String): Boolean {
        val treePackageName = tree.packageDeclaration.orElse(null) ?: return false
        return treePackageName.nameAsString == packageName
    }

    private fun hasSimpleClassName(tree: CompilationUnit, simpleClassName: String): Boolean {
        return tree.findFirst(ClassOrInterfaceDeclaration::class.java, { it.nameAsString == simpleClassName }).isPresent
    }

    private fun getClassName(qsflNode: QSFLNode): String? {
        return if(qsflNode is Class) qsflNode.name
        else {
            val parentNode = nodeInfo(qsflNode.parentId) ?: return null
            getClassName(parentNode)
        }
    }

    /* Returns null if it fails to set the new AST.
    *  If successful, it returns the newly set AST.
    *  When using this function, if one is not sure the AST can be correctly set, the output should be checked */
    fun setAST(component: FLComponent): CompilationUnit? {
        val (packageName, className) = when(component) {
//            is SFLComponent -> component.simpleClassName
//            is QSFLNode -> getClassName(component)
            is MorpheusComponent -> Pair(component.packageName, component.simpleClassName)
            else -> return null
        } ?: return null
        currentTree = getFileAST(packageName, className) ?: return null
        return currentTree
    }

    fun nodesBeginInLine(line: Int): Sequence<Node> {
        return currentTree.findAll(Node::class.java, { isSameBeginLine(it, line) }).asSequence()
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
        originalTree = ImmutableTree(currentTree.clone())
        return originalTree.getTree()
    }

    fun nodeInfo(nodeId: Int): QSFLNode? {
        val qsflReport = flReport as? QSFLReport ?: return null
        return qsflReport.nodeInfo(nodeId)
    }

    fun findNodes(landmark: Landmark): Sequence<Node> {
        setAST(landmark) ?: return emptySequence()
        val qsflReport = flReport as? QSFLReport ?: return emptySequence()
        var associated: List<Node> = emptyList()
        val paramNode = qsflReport.nodeInfo(landmark.parentId)
        if(paramNode != null && paramNode is Parameter){
            val methodNode = qsflReport.nodeInfo(paramNode.parentId)
            if (methodNode != null && methodNode is Method){
                val decl = currentTree.findFirst(CallableDeclaration::class.java, { hasNameAndParams(it, methodNode) })
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

    fun findNodes(morpheusComp: MorpheusComponent): Sequence<Node> {
        setAST(morpheusComp)
        return currentTree.findAll(Node::class.java, { morpheusComp.hasSamePosition(it) })
                .asSequence()
    }

    fun findNodesInLine(morpheusComp: MorpheusComponent): Sequence<Node> {
        setAST(morpheusComp)
        return currentTree.findAll(Node::class.java, { morpheusComp.hasSameLine(it) })
                .asSequence()
    }

    fun findNodesInCallable(morpheusComp: MorpheusComponent): Sequence<Node> {
        setAST(morpheusComp)
        return currentTree.findAll(Node::class.java, { morpheusComp.hasSameCallable(it) })
                .asSequence()
    }

    fun findNodesInRelativeLine(morpheusComp: MorpheusComponent): Sequence<Node> {
        setAST(morpheusComp)
        return currentTree.findAll(Node::class.java, { morpheusComp.hasSameRelativeLine(it) })
                .asSequence()
    }

    private fun containsVar(node: Node, nameExpr: NameExpr): Boolean {
        return node.findAll(NameExpr::class.java, { it == nameExpr }).isNotEmpty()
    }

    private fun getLine(n: Node): Int {
        return if(n.range.isPresent) n.range.get().begin.line else 0
    }



    private fun hasNameAndParams(decl: CallableDeclaration<*>, methodNode: Method): Boolean {
        return decl.name.asString() == methodNode.methodName
                && decl.parameters.size == methodNode.params.size
                && methodNode.params.zip(decl.parameters)
                        .all { it.first == it.second.typeAsString
                                || it.first == fullName(it.second)
                        }
    }

    private fun fullName(param: com.github.javaparser.ast.body.Parameter): String {
        val type = resolveDecl(param) ?: return ""
        return try {
            type.describeType()
        } catch (e: UnsolvedSymbolException){
            ""
        }
    }

    fun resetFiles() {
        srcRoot.saveAll()
    }

    fun buildFullName(component: FLComponent): String {
        return when(component){
            is SFLComponent -> component.packageName + "." + component.simpleClassName
            is QSFLNode -> {
                val elements = mutableListOf(component)
                var currentQSFLNode = component as QSFLNode
                while(currentQSFLNode.parentId != 0){
                    currentQSFLNode = (flReport as QSFLReport).nodeInfo(currentQSFLNode.parentId) ?: return ""
                    elements.add(currentQSFLNode)
                }
                elements.reverse()
                elements.take(elements.indexOfFirst { it is Class } + 1)
                        .fold("", { name, qsflNode -> "$name.${qsflNode.name}" })
                        .removePrefix(".")
            }
            else -> ""
        }
    }
}
