package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.SimpleName
import repair.mutators.utils.resolveDecl

class AccessorMethodChange: MutatorRepair<MethodCallExpr>() {
    override val rank: Int
        get() = 22

    override fun checkedRepair(program: BuggyProgram, metCall: MethodCallExpr): List<MethodCallExpr> {
        return if(hasAccessorPrefix(metCall.name)){
            val accessorDecl = getAccessorDeclaration(metCall) ?: return emptyList()
            val enclosingClass = accessorDecl.findAncestor(ClassOrInterfaceDeclaration::class.java)
                                            .orElse(null) ?: return emptyList()

            enclosingClass
                    .findAll(MethodDeclaration::class.java,
                            { hasAccessorPrefix(it.name) && matchesSignature(it, accessorDecl) && it.name != accessorDecl.name})
                    .map { MethodCallExpr(metCall.scope.orElse(null), it.nameAsString, metCall.arguments) }
        } else emptyList()
    }

    private fun matchesSignature(metDecl: MethodDeclaration, accessorDecl: MethodDeclaration): Boolean {
        return when {
            metDecl.type != accessorDecl.type -> false
            metDecl.parameters.size != accessorDecl.parameters.size -> false
            else -> metDecl.parameters.zip(accessorDecl.parameters).all { (p1, p2) -> sameType(p1,p2) }
        }
    }

    private fun sameType(p1: Parameter, p2: Parameter): Boolean {
        return p1.type == p2.type
    }

    private fun hasAccessorPrefix(metName: SimpleName): Boolean {
        return metName.asString().startsWith("get")
    }

    private fun getAccessorDeclaration(metCall: MethodCallExpr): MethodDeclaration? {
//        val metDecl = metCall.resolve().toAst().orElse(null) ?: return null
        val metDecl = resolveDecl(metCall)?.toAst()?.orElse(null) ?: return null
//        val metAST = metDecl.toAst().orElse(null) ?: return null
        return if(metDecl.isStatic) null else metDecl
    }
}
