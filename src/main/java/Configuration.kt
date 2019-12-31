data class Configuration(
        val projPath: String,
        val classPath: String = "$projPath/target/classes",
        val testClassPath: String = "$projPath/target/test-classes",
        val mutantsPath: String = "mutants/${projPath.split("/").last()}",
        val mutantBugsFileName: String,
        val srcPath: String = "$projPath/src/fault_localization.reports.main/java",
        val fileName: String,
        val fullClassName: String,
        val testName: String,
        var mutantID: Int = 1
)