import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.CallableDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import visitors.MutatorVisitor
import java.io.File

class Mutator(path: String, fullClassName: String) {
    val compUnit = buildCompUnit(path, fullClassName)

    private fun buildCompUnit(path: String, fullClassName: String): CompilationUnit {
        return StaticJavaParser.parse(File(path,"${fullClassName.replace(".", "/")}.java"))
    }

    private fun <T : CallableDeclaration<*>> getName(name: String?, metOrConsClass: Class<T>): Node?{
        val metOrCons = compUnit.findFirst(
                metOrConsClass,
                {cd -> cd.name.toString() == name?.slice(0..name.indexOf("(")-1)})
        return if (metOrCons.isPresent) metOrCons.get() else null
    }

    fun createMutants(methodName: String?, faultyParamName: String?): List<CompilationUnit> {
        if(faultyParamName == null) return emptyList()

        val mutants = mutableListOf<CompilationUnit>()

        val methodOrConstructor =
                getName(methodName, MethodDeclaration::class.java)
                        ?: getName(methodName, ConstructorDeclaration::class.java)

        val binExprs = methodOrConstructor?.findAll(
                BinaryExpr::class.java,
                { be -> isAboutParam(be, faultyParamName)})

        if (binExprs != null) {
            for(be in binExprs){
                var binExprMutator = MutatorVisitor()
                var compUnitCopy = compUnit.clone()
                binExprMutator.visit(compUnitCopy, Triple(be, compUnitCopy, mutants))
            }
        }
        return mutants
    }

    fun isAboutParam(be: BinaryExpr, paramName: String): Boolean{
        val (lhs, rhs) = Pair(be.left, be.right)
        return when {
            lhs is NameExpr && lhs.toString().contains(paramName) -> true
            rhs is NameExpr && rhs.toString().contains(paramName) -> true
            rhs is MethodCallExpr && rhs.arguments.isNonEmpty && argsAboutParam(rhs.arguments, paramName) -> true
            else -> false
        }
    }

    fun isAboutParam(nameExpr: NameExpr, paramName: String): Boolean{
        return nameExpr.toString().contains(paramName)
    }

    private fun argsAboutParam(arguments: NodeList<Expression>, paramName: String): Boolean {
        return arguments
                .filterIsInstance<NameExpr>()
                .any { isAboutParam(it, paramName) }
    }
}
