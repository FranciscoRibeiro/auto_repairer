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