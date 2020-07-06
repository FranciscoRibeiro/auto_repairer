import fault_localization.FaultLocalizationType.SFL
import fault_localization.FaultLocalizationType.QSFL
import repair.*
import java.io.File
import kotlin.system.exitProcess

val strategyOptions = mapOf("-a"    to "all",
                            "-l"    to "landmark",
                            "-lr"   to "landmark_ranking",
                            "-lsr"  to "landmark_strict_ranking",
                            "-ll"   to "landmark_lines",
                            "-la"   to "landmark_adhoc",
                            "-lsa"  to "landmark_strict_adhoc",
                            "-br"   to "brute_force_ranking",
                            "-ba"   to "brute_force_adhoc",
                            "-bn"   to "brute_force_ranking_no_suspects")

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

private fun setupFix(projDir: String, fileName: String, fix: AlternativeProgram): AlternativeProgram{
    println("Fix: ${fix.insertedMutant}")
    File("$projDir/src/main/java/$fileName.java").writeText(fix.toString())
    return fix
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
    /*val mutantIdentifier = "1011/29/00000019" //bug: UnaryOperatorInsertion
    val fileName = "IntHashMap"*/
    /*val mutantIdentifier = "1017/10/00000027" //bug: ConditionalOperatorReplacement
    val fileName = "VCardBean"*/
    /*val mutantIdentifier = "1016/22/00000006" //bug: ArithmeticOperatorReplacement
    val fileName = "TimeStamp"*/
    /*val mutantIdentifier = "1012/69/00000006" //bug: UnaryOperatorReplacement
    val fileName = "ParameterParser"*/
    /*val mutantIdentifier = "1017/10/00000067" //bug: AccessorMethodChange
    val fileName = "VCardBean"*/
    /*val mutantIdentifier = "1006/22/00000003"
    val fileName = "HierarchyPropertyParser"*/
    /*val mutantIdentifier = "rrc"
    val fileName = "TestFile"*/
    val mutantIdentifier = args[2]
    val fileName = args[3]
    val strategy = validateStrategyOption(args.getOrElse(4, { "-a" }))
    val strategyDir = setStrategyDir(strategy)
    val countOnly = validateCountOption(args.getOrNull(5))

    val mutantFile = File("${args[0]}/${args[1]}/$mutantIdentifier/$fileName.java")

//    val cu = StaticJavaParser.parse(File("src/main/java/TestFile.java"))

    /* parse program */
    val buggyProgram = BuggyProgram(
            "${args[0]}/$fileName/fl_reports/${mutantIdentifier.replace("/", "_")}",
            mutantFile)

    /* lazy creation of potential fixes based on landmarks */
    val landmarkAlternatives =
            if(strategy == "-l" || strategy == "-a") LandmarkRepair().repair(buggyProgram, QSFL)
            else if(strategy == "-lr") LandmarkRankingRepair().repair(buggyProgram, QSFL)
            else if(strategy == "-lsr") LandmarkStrictRankingRepair().repair(buggyProgram, QSFL)
            else if(strategy == "-la") LandmarkAdHocRepair().repair(buggyProgram, QSFL)
            else if(strategy == "-lsa") LandmarkStrictAdHocRepair().repair(buggyProgram, QSFL)
            else if(strategy == "-ll") LandmarkLinesRepair().repair(buggyProgram, QSFL)
            else emptySequence()

    /* lazy creation of potential fixes based on the mut ops ranking */
    val bruteForceAlternatives =
            if(strategy == "-br" || strategy == "-a") BruteForceRankingRepair().repair(buggyProgram, SFL)
            else if(strategy == "-ba") BruteForceAdHocRepair().repair(buggyProgram, SFL)
            else if(strategy == "-bn") BruteForceRankingNoSuspectRepair().repair(buggyProgram, SFL)
            else emptySequence()

    var counter = 0
    val alternatives = (landmarkAlternatives + bruteForceAlternatives)
    if(countOnly) { /* total number of potential patches */
        println(alternatives.toList().size)
    } else { /* stop when a mutant fixes the program */
        val fix = alternatives
//                .forEach { File("tmp_lsr/${++counter}.java").writeText(it.toString()) }
                .map { setupFix("${args[0]}/$fileName", fileName, it) }
                .map { saveFix("${args[0]}/$fileName/patches/$strategyDir/${mutantIdentifier.replace("/","_")}", ++counter, it) }
                .find { passTests("${args[0]}/$fileName") }

        if(fix == null) exitProcess(1)
    }
}

fun validateCountOption(count: String?): Boolean {
    return !(count == null || count != "--count")
}

fun setStrategyDir(strategy: String): String {
    return strategyOptions[strategy] ?: run {
        printError("invalid option - executing as \"-a\"")
        "all"
    }
}

fun printError(msg: String) {
    System.err.println(msg)
}

fun validateStrategyOption(strategy: String): String {
    return if(strategyOptions.contains(strategy))
        strategy
    else {
        printError("invalid option - executing as \"-a\"")
        "-a"
    }
}

fun saveFix(patchDir: String, n: Int, fix: AlternativeProgram) {
    File("$patchDir/$n.java").writeText(fix.toString())
}
