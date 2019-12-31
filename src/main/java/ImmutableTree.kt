import com.github.javaparser.ast.CompilationUnit

class ImmutableTree(private val programTree: CompilationUnit) {
    fun getTree(): CompilationUnit {
        return programTree.clone()
    }
}
