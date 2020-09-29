package scripts

import com.github.javaparser.ParseProblemException
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import java.io.File

fun save(dir: File, relPath: File, tree: CompilationUnit){
    val newDir = File("${dir.parent}/new_${dir.name}/${relPath.parent ?: ""}")
    if(!newDir.exists()) newDir.mkdirs()
    File(newDir, relPath.name).writeText(tree.toString())
}

fun parse(file: File): CompilationUnit? {
    return try {
        StaticJavaParser.parse(file)
    } catch (e: ParseProblemException){
        null
    }
}

fun main(args: Array<String>) {
    val dir = File(args[0])
//    println(dir.name)
    dir.walk().filter { it.isFile }
            .map { it.relativeTo(dir) to parse(it) }
            .filter { it.second != null }
            .forEach { save(dir, it.first, it.second!!) }
//            .forEach { println(it.first) }
}
