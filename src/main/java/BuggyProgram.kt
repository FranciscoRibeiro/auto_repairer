import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import fault_localization.FaultLocalizationType
import fault_localization.FaultLocalizationType.QSFL
import fault_localization.FaultLocalizationType.SFL
import fault_localization.reports.QSFLReport
import fault_localization.reports.SFLReport
import java.io.File

class BuggyProgram(val path: String, val sourceFile: File) {
    val sflReport = SFLReport(File(path, "site/gzoltar/sfl"))
    val qsflReport = QSFLReport(File(path, "qsfl"))
    //val programTree = ProgramTree(StaticJavaParser.parse(sourceFile))
    val tree = StaticJavaParser.parse(sourceFile)
    //private val originalTree = ImmutableTree(programTree.tree.clone())
    private val originalTree = ImmutableTree(tree.clone())

    fun mostLikelyFaulty(basedOn: FaultLocalizationType): List<Int> {
        return when(basedOn){
            SFL -> sflReport.mostLikelyFaulty()
            QSFL -> qsflReport.mostLikelyFaulty()
        }
    }

    /*fun nodesInLine(line: Int): List<Node> {
        return programTree.nodesInLine(line)
    }*/

    fun nodesInLine(line: Int): List<Node> {
        return tree.findAll(Node::class.java, { isSameLine(it, line) }) ?: emptyList()
    }

    private fun isSameLine(node: Node?, line: Int): Boolean {
        if(node != null && node.range.isPresent){
            val nodeRange = node.range.get()
            return nodeRange.begin.line == line && nodeRange.end.line == line
        } else{ return false }
    }

    fun getOriginalTree(): CompilationUnit {
        return originalTree.getTree()
    }
}
