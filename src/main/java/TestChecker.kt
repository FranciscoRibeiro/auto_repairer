import org.junit.internal.TextListener
import org.junit.runner.JUnitCore
import java.io.File

/*fun runCmd(cmd: String) {
    println(cmd)
    val exitCode = ProcessBuilder()
            .command(cmd.split(" "))
            .inheritIO()
            .start()
            .waitFor()
    println("Exit code: $exitCode")
}

fun fault_localization.reports.main() {
    val junitClassPath = "/home/kiko/.m2/repository/junit/junit/4.11/junit-4.11.jar"
    val hamcrestClassPath = "/home/kiko/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"
    val projPath = "/home/kiko/PhD/CodeDefenders_projs/ByteArrayHashMap"
    val classPath = "$projPath/target/classes"
    val testClassPath = "$projPath/target/test-classes"
    val testName = "TestByteArrayHashMap"
    runCmd("java -cp $junitClassPath:$hamcrestClassPath:$classPath:$testClassPath org.junit.runner.JUnitCore $testName")
}*/

class TestChecker(val fileName: String, val classPath: String, val testClassPath: String, val testName: String, val fullClassName: String) {
    val junitClassPath = "/home/kiko/.m2/repository/junit/junit/4.11/junit-4.11.jar"
    val hamcrestClassPath = "/home/kiko/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"
    val junit = JUnitCore()

    init {
        junit.addListener(TextListener(System.out))
    }

    fun runCmd(cmd: String): Int {
        println(cmd)
        return ProcessBuilder()
                .command(cmd.split(" "))
                .inheritIO()
                .start()
                .waitFor()
    }

    fun compileMutant(mutFile: File): Int{
        runCmd("cp $mutFile ${mutFile.parent}/$fileName.java")
        return runCmd("javac -cp $classPath:$testClassPath -d $classPath ${mutFile.parent}/$fileName.java")
    }

    fun testResult(): Int {
        return runCmd("java " +
                "-cp $junitClassPath:$hamcrestClassPath:$classPath:$testClassPath " +
                "org.junit.runner.JUnitCore $testName")
    }

    fun runTests(mutantsPath: String): Pair<MutableList<String>, MutableList<String>> {
        val repairers = mutableListOf<String>()
        val invalid = mutableListOf<String>()

        for(mutFile in File(mutantsPath).listFiles()){
            var compilationExitCode = compileMutant(mutFile)

            if(compilationExitCode == 0){
                var testExitCode = testResult()
                println("-------------------------------------")
                if(testExitCode == 0){
                    repairers.add(mutFile.name)
                }
            }
            else{
                invalid.add(mutFile.name)
            }
        }

        runCmd("rm $mutantsPath/$fileName.java")
        return Pair(repairers, invalid)
    }
}