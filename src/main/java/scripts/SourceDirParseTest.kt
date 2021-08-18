package scripts

import com.github.javaparser.ParserConfiguration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import com.github.javaparser.utils.SourceRoot
import java.io.File
import java.nio.file.Paths


fun main(args: Array<String>) {
    val srcPath = Paths.get(args[0])
    val files = File(args[0]).listFiles()
    val combinedTypeSolver = CombinedTypeSolver(ReflectionTypeSolver(), JavaParserTypeSolver(args[0]))
    val config = ParserConfiguration()
            .setStoreTokens(true)
            .setSymbolResolver(JavaSymbolSolver(combinedTypeSolver))

    val cu = SourceRoot(srcPath, config).tryToParse()/*.filter { it.result.get() is ClassOrInterfaceDeclaration }*/
    println("end")
}
