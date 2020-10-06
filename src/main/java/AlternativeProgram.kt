import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter.print
import fault_localization.reports.FLComponent
import fault_localization.reports.sfl.SFLComponent

class AlternativeProgram(val insertedMutant: Node, val modifiedProgram: CompilationUnit) {
    private var component: FLComponent? = null

    constructor(insertedMutant: Node, modifiedProgram: CompilationUnit, component: FLComponent): this(insertedMutant, modifiedProgram){
        this.component = component
    }

    fun setComponent(component: FLComponent): AlternativeProgram {
        this.component = component
        return this
    }

    fun fullName(): String {
        return when(component){
            is SFLComponent -> (component as SFLComponent).packageName + "." + (component as SFLComponent).simpleClassName
            else -> ""
        }
    }

    override fun toString(): String {
        return print(modifiedProgram)
    }
}
