package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.*
import com.github.javaparser.resolution.types.ResolvedPrimitiveType
import repair.mutators.utils.calcType
import repair.mutators.utils.getParent
import repair.mutators.utils.isLeftSideAssign

class Negation(): MutatorRepair<Expression>() {
    override val rank: Int
        get() = 34

    override fun checkedRepair(program: BuggyProgram, expr: Expression): List<Expression> {
        if(isLeftSideAssign(expr)) return emptyList()

        val parent = getParent(expr) ?: return emptyList()
        if(parent is BinaryExpr) return emptyList() //Do not negate if it is part of a binary expression
        val type = calcType(expr) ?: return emptyList()
        return if(type is ResolvedPrimitiveType){
            when {
                type.isNumeric -> listOf(UnaryExpr(expr.clone(), UnaryExpr.Operator.MINUS))
                else -> emptyList()
            }
        } else emptyList()
    }
}
