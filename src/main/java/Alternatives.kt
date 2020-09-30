import java.io.File

class Alternatives(val baseProgram: BuggyProgram, val alternatives: List<AlternativeProgram>) {
    /*fun save(){
        val repairDir = File(baseProgram.path, "repairs")
        if(!repairDir.exists()){ repairDir.mkdir() }
        val originalFileName = baseProgram.sourceFile.nameWithoutExtension
        alternatives.forEach { File(repairDir, "${originalFileName}_${alternatives.indexOf(it)}.java").writeText(it.toString()) }
    }*/
}
