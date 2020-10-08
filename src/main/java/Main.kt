import fault_localization.FaultLocalizationType
import fault_localization.reports.FLReport
import fault_localization.reports.qsfl.QSFLReport
import fault_localization.reports.sfl.SFLReport
import repair.*
import java.io.File
import java.time.LocalDateTime
import kotlin.system.exitProcess

//import com.github.javaparser.ast.CompilationUnit
//import fault_localization.reports.qsfl.Landmark
//import fault_localization.reports.qsfl.Parameter
//import fault_localization.reports.qsfl.QSFLReport
//import org.json.JSONObject
//import java.io.File
//
//fun <T> T.log(): T {
//    println(this); return this
//}
//
//fun setConfig(configFileName: String = "config.json"): Configuration{
//    val json = JSONObject(File(configFileName).readText())
//    return Configuration(
//            projPath = json.getString("projPath"),
//            mutantBugsFileName = json.getString("mutantBugsFileName"),
//            fileName = json.getString("fileName"),
//            fullClassName = json.getString("fullClassName"),
//            testName = json.getString("testName")
//    )
//}
//
//fun setupBug(config: Configuration, bug: String){
//    runCmd("cp $bug/${config.fileName}.java ${config.srcPath}", "/home/kiko/PhD/CodeDefenders_projs")
//    runCmd("mvn clean", config.projPath)
//    runCmd("mvn qsfl:test", config.projPath)
//    runCmd(
//            "python scripts/run_diagnosis_native.py ${config.projPath}/target qsfl",
//            "/home/kiko/PhD/q-sfl-experiments")
//}
//
//fun main(args: Array<String>) {
//    val config = if(args.isNotEmpty()) setConfig(args[0]) else setConfig()
//
//    //val mutantBugs = File(config.mutantBugsFileName).readLines()
//    //for(bug in mutantBugs) {
//        //setupBug(config, bug)
//
//        val infoReport = QSFLReport(File(config.projPath))
//        val mutator = Mutator(config.srcPath, config.fullClassName)
//        var testChecker = TestChecker(config.fileName, config.classPath, config.testClassPath, config.testName, config.fullClassName)
//
//        val faultyNodeIds = infoReport.mostLikelyFaulty(1)
//        val nodeInfos = infoReport.nodeInfo(faultyNodeIds)
//
//        val mutantGeneration =
//                nodeInfos.filterIsInstance<Landmark>().log()
//                        .map { infoReport.nodeInfo(it.parentId) }.log()
//                        .filterIsInstance<Parameter>().log()
//                        .map { Pair(infoReport.nodeInfo(it.parentId)?.name, it.name) }.log()
//                        .flatMap { mutator.createMutants(it.first, it.second) }.log()
//                        .map { saveMutant(it, config.mutantsPath, config.fileName + "_" + config.mutantID++) }.log()
//
//        if(mutantGeneration.isEmpty()) {
//            println("No mutants were generated")
//        }
//        else {
//            val (repairers, invalid) = testChecker.runTests(config.mutantsPath)
//
//            if (repairers.isEmpty()) {
//                println("No mutant repairs the bug")
//            }
//            else {
//                for (name in repairers) {
//                    println("Mutant $name repairs the bug")
//                }
//            }
//
//            if (invalid.isNotEmpty()) {
//                for (name in invalid) {
//                    println("Mutant $name is invalid")
//                }
//            }
//
//            //makeRepairReport(bug, repairers, invalid, config)
//            //clearMutants(config)
//        }
//    }
////}
//
//fun makeRepairReport(bug: String, repairers: MutableList<String>, invalid: MutableList<String>, config: Configuration) {
//    val repairDir = File("repair_reports/$bug")
//    if(!repairDir.exists()){
//        repairDir.mkdirs()
//    }
//
//    val repairReport = File(repairDir, "repair.txt")
//    if (repairers.isEmpty()) {
//        repairReport.appendText("No mutant repairs the bug\n")
//    }
//    else {
//        for (name in repairers) {
//            repairReport.appendText("Mutant $name repairs the bug\n")
//        }
//        val mutsFile = File(config.mutantBugsFileName)
//        val newContent = mutsFile
//                .readLines()
//                .map { if(it == bug) "$it --- FIXED" else it}
//                .reduce { a,b -> a+"\n"+b }
//
//        mutsFile.writeText(newContent)
//    }
//
//    if (invalid.isNotEmpty()) {
//        for (name in invalid) {
//            repairReport.appendText("Mutant $name is invalid\n")
//        }
//    }
//}
//
//fun clearMutants(config: Configuration) {
//    File(config.mutantsPath).deleteRecursively()
//}
//
//fun saveMutant(mut: CompilationUnit, mutPath: String, mutFileName: String) {
//    val mutDir = File(mutPath)
//    if(!mutDir.exists()){
//        mutDir.mkdirs()
//    }
//    File(mutPath, "$mutFileName.java").writeText(mut.toString())
//}
//
//class Main {
//    /*companion object {
//        *//*val projPath = "/home/kiko/PhD/qsfl_tester"
//        val classPath = "$projPath/target/classes"
//        val testClassPath = "$projPath/target/test-classes"
//        val mutantsPath = "mutants/waterstate"
//        val path = "$projPath/src/fault_localization.reports.main/java/qsfl/tester/waterstate"
//        val fileName = "WaterState"
//        val fullClassName = "qsfl.tester.waterstate.WaterState"
//        val testName = "qsfl.tester.WaterState_Test"*//*
//        *//*var mutantID = 1
//
//        val projPath = "/home/kiko/PhD/CodeDefenders_projs/ByteArrayHashMap"
//        val classPath = "$projPath/target/classes"
//        val testClassPath = "$projPath/target/test-classes"
//        val mutantsPath = "mutants/ByteArrayHashMap"
//        val srcPath = "$projPath/src/fault_localization.reports.main/java"
//        val fileName = "ByteArrayHashMap"
//        val fullClassName = "ByteArrayHashMap"
//        val testName = "TestByteArrayHashMap"*//*
//    }*/
//}

fun errorOut(msg: String): Nothing {
    println(msg)
    exitProcess(1)
}

fun getRepairStrategy(strategy: String): RepairStrategy? {
    return when(strategy){
        "-br" -> BruteForceRankingRepair()
        "-ba" -> BruteForceAdHocRepair()
        "-bn" -> BruteForceRankingNoSuspectRepair()
        "-l" -> LandmarkRepair()
        "-lr" -> LandmarkRankingRepair()
        "-lsr" -> LandmarkStrictRankingRepair()
        "-la" -> LandmarkAdHocRepair()
        "-lsa" -> LandmarkStrictAdHocRepair()
        "-ll" -> LandmarkLinesRepair()
        else -> null
    }
}

fun getFLType(strategy: String): FaultLocalizationType? {
    return when(strategy){
        "-br", "-ba", "-bn" -> FaultLocalizationType.SFL
        "-l", "-lr", "-lsr", "-la", "-lsa", "-ll" -> FaultLocalizationType.QSFL
        else -> null
    }
}

private fun loadReport(flType: FaultLocalizationType, reportPath: String): FLReport {
    return when (flType) {
        FaultLocalizationType.SFL -> SFLReport(reportPath)
        FaultLocalizationType.QSFL -> QSFLReport(reportPath)
    }
}

private fun setupPatch(srcPath: String, alternative: AlternativeProgram): AlternativeProgram {
    File(srcPath, alternative.fullName.replace(".", "/") + ".java").writeText(alternative.toString())
    return alternative
}

private fun savePatch(alternative: AlternativeProgram, counter: Int) {
    val patchDir = File("generated_patches", LocalDateTime.now().toString())
    patchDir.mkdirs()
    File(patchDir, "$counter.java").writeText(alternative.toString())
}

fun run(cmd: String,
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

private fun test(projDir: String): Boolean {
    println("Testing...")
    return run("mvn test", projDir) == 0
}

private fun resetFiles(program: BuggyProgram) {
    program.resetFiles()
}

fun main(args: Array<String>) {
    if(args.size != 3) errorOut("Incorrect Usage")

    val (srcPath, strategy, reportPath) = Triple(args[0], args[1], args[2])
    val repairStrategy = getRepairStrategy(strategy) ?: errorOut("Invalid strategy: $strategy")
    val flType = getFLType(strategy) ?: errorOut("Invalid FL type")
    val report = loadReport(flType, reportPath)
    val program = BuggyProgram(srcPath, report)
    var counter = 0
    repairStrategy.repair(program, flType)
            .map { resetFiles(program); it }
            .map { setupPatch(srcPath, it) }
            .map { savePatch(it, ++counter) }
            .find { test(srcPath.removeSuffix("/src")) }

    println("end")
}
