import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.BinaryExpr
import fault_localization.FaultLocalizationType.SFL
import repair.BruteForceRepair
import java.io.File

fun main() {
    val mutantIdentifier = "1007/19/00000012"
    val fileName = "HSLColor"
    val mutantFile = File("/home/kiko/PhD/CodeDefenders_projs/generated_mutants_2016/$mutantIdentifier/$fileName.java")
    //val cu = StaticJavaParser.parse(File("/home/kiko/PhD/CodeDefenders_projs/generated_mutants_2016/1001/10/00000004/ByteArrayHashMap.java"))

    val buggyProgram = BuggyProgram("/home/kiko/PhD/CodeDefenders_projs/HSLColor/fl_reports/1007_19_00000012", mutantFile)
    val bfRepairer = BruteForceRepair()
    val alternatives = bfRepairer.repair(buggyProgram, SFL)
    alternatives?.save() ?: println("No mutants generated")


    /*val sflDiag = SFLDiagnosis(File("/home/kiko/PhD/CodeDefenders_projs/$fileName/fl_reports/${mutantIdentifier.replace("/", "_")}/site/gzoltar/sfl/txt/ochiai.ranking.csv"))
    val compUnit = StaticJavaParser.parse(mutantFile)
    val nodesInLine = compUnit
            .findAll(Node::class.java,
                    { isSameLine(it) })
    val firstBE = compUnit
            .findFirst(BinaryExpr::class.java,
                    { isSameLine(it) })
    val cloneBE = firstBE.get().clone()
    cloneBE.setOperator(BinaryExpr.Operator.DIVIDE)
    firstBE.get().replace(cloneBE)
    println("hey ho")*/
}

fun isSameLine(node: Node?): Boolean {
    if(node != null && node.range.isPresent){
        val nodeRange = node.range.get()
        return nodeRange.begin.line == 239 && nodeRange.end.line == 239
    } else{ return false }
}
