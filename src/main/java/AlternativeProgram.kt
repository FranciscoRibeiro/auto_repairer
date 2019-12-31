import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node

class AlternativeProgram(val insertedMutant: Node, val modifiedProgram: CompilationUnit) {
    override fun toString(): String {
        return modifiedProgram.toString()
    }
}
