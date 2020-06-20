package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.*
import com.github.javaparser.resolution.types.ResolvedReferenceType
import repair.mutators.utils.getParent

class ReferenceReplacementContent: MutatorRepair<Expression>() {
    override val rank: Int
        get() = 26

    override fun checkedRepair(program: BuggyProgram, expr: Expression): List<MethodCallExpr> {
        if(!(expr is NameExpr || expr is FieldAccessExpr)) return emptyList()

        val parent = getParent(expr) ?: return emptyList()
        return if((parent is AssignExpr || parent is VariableDeclarator) && isRHSAssignment(parent, expr)){
            val type = expr.calculateResolvedType()
            if(type is ResolvedReferenceType){
                listOf(MethodCallExpr(expr, "clone"))
            } else emptyList()
        } else emptyList()
    }

    private fun isRHSAssignment(node: Node, expr: Expression): Boolean {
        return when(node){
            is AssignExpr -> node.value === expr
            is VariableDeclarator -> node.initializer.orElse(null) === expr
            else -> false
        }
    }
}
