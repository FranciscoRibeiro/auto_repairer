//import java.io.File
//import QSFLExecuterChecker.Companion.projPath as projPath
//import QSFLExecuterChecker.Companion.srcPath as srcPath
//import QSFLExecuterChecker.Companion.name
//import fault_localization.reports.qsfl.QSFLReport
//
//fun runCmd(cmd: String,
//           dir: String = System.getProperty("user.dir"),
//           out: File? = null): Int {
//    println(cmd)
//    var pb = ProcessBuilder()
//            .directory(File(dir))
//            .command(cmd.split(" "))
//            .redirectInput(ProcessBuilder.Redirect.INHERIT)
//            .redirectError(ProcessBuilder.Redirect.INHERIT)
//    if(out == null){
//        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
//    } else {
//        out.appendText(cmd + "\n")
//        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(out))
//    }
//
//    return pb.start().waitFor()
//}
//
//fun hasDiagnosisLandmarks(file: File): Boolean{
//    runCmd("cp $file $srcPath")
//    runCmd("mvn clean", "$projPath")
//    runCmd("mvn qsfl:test", "$projPath")
//    runCmd(
//            "python scripts/run_diagnosis_native.py $projPath/target qsfl",
//            "/home/kiko/PhD/q-sfl-experiments")
//    val infoReport = QSFLReport(File(projPath))
//    val faultyNodeIds = infoReport.mostLikelyFaulty(1)
//    val nodeInfos = infoReport.nodeInfo(faultyNodeIds)
//    return nodeInfos
//            .filter { it?.type == "LANDMARK" }
//            .isNotEmpty()
//}
//
//fun putInReport(path: File){
//    val outFile = File("landmark_diagnosis_presence/${name}_diffs.txt")
//    runCmd("diff -b $path originals/$name.java",
//            "/home/kiko/PhD/CodeDefenders_projs",
//            outFile)
//    outFile.appendText("\n-----------------------------------------\n")
//    File("landmark_diagnosis_presence/${name}_muts.txt").appendText("$path".split("CodeDefenders_projs/", "/$name.java")[1]+"\n")
//}
//
//fun setPaths(projName: String){
//    name = projName
//    projPath = "/home/kiko/PhD/CodeDefenders_projs/$projName"
//    srcPath = "$projPath/src/fault_localization.reports.main/java"
//}
//
//fun main(args: Array<String>) {
//    setPaths(args[0])
//    File("/home/kiko/PhD/CodeDefenders_projs/${args[1]}")
//            .walk()
//            .toList()
//            .filter { it.toString().endsWith(".java") }.log()
//            .filter { hasDiagnosisLandmarks(it) }.log()
//            .forEach { putInReport(it) }
//}
//
//class QSFLExecuterChecker {
//    companion object {
//        lateinit var name: String
//        lateinit var projPath: String
//        lateinit var srcPath: String
//    }
//}