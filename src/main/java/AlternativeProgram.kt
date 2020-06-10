import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter.print
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter.setup

class AlternativeProgram(val insertedMutant: Node, val modifiedProgram: CompilationUnit) {
    override fun toString(): String {
        return print(modifiedProgram)
    }
}
