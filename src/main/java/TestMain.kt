import com.github.javaparser.JavaParser
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.stmt.ReturnStmt
import fault_localization.FaultLocalizationType.SFL
import fault_localization.FaultLocalizationType.QSFL
import repair.BruteForceRepair
import repair.LandmarkRepair
import java.io.File

fun runCmd(cmd: String,
           dir: String = System.getProperty("user.dir"),
           out: File? = null): Int {
    println(cmd)
    var pb = ProcessBuilder()
            .directory(File(dir))
            .command(cmd.split(" "))
            .redirectInput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
    if(out == null){
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
    } else {
        out.appendText(cmd + "\n")
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(out))
    }

    return pb.start().waitFor()
}

private fun passTests(projDir: String): Boolean {
    println("Testing...")
    return runCmd("mvn test", projDir) == 0
}

private fun setupFix(projDir: String, fileName: String, fix: AlternativeProgram){
    println("Fix: ${fix.insertedMutant}")
    File("$projDir/src/main/java/$fileName.java").writeText(fix.toString())
}

fun main(args: Array<String>) {
    /*val mutantIdentifier = "1007/19/00000012"
    val fileName = "HSLColor"*/
    /*val mutantIdentifier = "1001/10/00000017"
    val fileName = "ByteArrayHashMap"*/
    /*val mutantIdentifier = "1001/10/00000025"
    val fileName = "ByteArrayHashMap"*/
    /*val mutantIdentifier = "1019/10/00000031"
    val fileName = "XmlElement"*/
    /*val mutantIdentifier = "1011/62/00000006"
    val fileName = "IntHashMap"*/
    /*val mutantIdentifier = "1013/29/00000017"
    val fileName = "Range"*/
    /*val mutantIdentifier = "1001/12/00000029"
    val fileName = "ByteArrayHashMap"*/
    /*val mutantIdentifier = "1007/19/00000009"
    val fileName = "HSLColor"*/
    /*val mutantIdentifier = "1007/19/00000013"
    val fileName = "HSLColor"*/
    /*val mutantIdentifier = "1004/22/00000004"
    val fileName = "FontInfo"*/
    /*val mutantIdentifier = "rv_null"
    val fileName = "TestFile"*/
    val mutantIdentifier = "1011/29/00000019"
    val fileName = "IntHashMap"
    val mutantFile = File("${args[0]}/${args[1]}/$mutantIdentifier/$fileName.java")

//    val cu = StaticJavaParser.parse(File("src/main/java/TestFile.java"))

    /* parse program */
    val buggyProgram = BuggyProgram(
            "${args[0]}/$fileName/fl_reports/${mutantIdentifier.replace("/", "_")}",
            mutantFile)

    /* lazy creation of potential fixes based on landmarks */
    val lRepairer = LandmarkRepair()
    val landmarkAlternatives = lRepairer.repair(buggyProgram, QSFL)

    /* lazy creation of potential fixes based on the mut ops ranking */
    val bfRepairer = BruteForceRepair()
    val bruteForceAlternatives = bfRepairer.repair(buggyProgram, SFL)

    /* stop when a mutant fixes the program */
    var counter = 0
    val x = (landmarkAlternatives + bruteForceAlternatives).toList()
            x.forEach { File("tmp/${++counter}.java").writeText(it.toString()) }
//            .map { setupFix("${args[0]}/$fileName", fileName, it) }
//            .find { passTests("${args[0]}/$fileName") }
}
