package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.resolution.types.ResolvedReferenceType
import repair.mutators.utils.calcType

class ReturnValue: MutatorRepair<ReturnStmt>() {
    override val rank: Int
        get() = 8

    override fun checkedRepair(program: BuggyProgram, ret: ReturnStmt): List<ReturnStmt> {
        val retExpr = ret.expression.orElse(null) ?: return emptyList()
        return when(retExpr){
            is NameExpr -> {
                val newExpr = emptyReturn(retExpr) ?: NullLiteralExpr()
                listOf(ReturnStmt(newExpr))
            }
            else -> emptyList()
        }
    }

    private fun emptyReturn(nameExpr: NameExpr): Expression? {
//        val resType = nameExpr.calculateResolvedType() as? ResolvedReferenceType ?: return null
        val resType = calcType(nameExpr) as? ResolvedReferenceType ?: return null
        return when(resType.qualifiedName){
            "java.lang.String" -> StringLiteralExpr("")
            "java.util.Optional" -> MethodCallExpr(NameExpr("Optional"), SimpleName("empty"))
            "java.util.List" -> MethodCallExpr(NameExpr("Collections"), SimpleName("emptyList"))
            "java.util.Collection" -> MethodCallExpr(NameExpr("Collections"), SimpleName("emptyList"))
            "java.util.Set" -> MethodCallExpr(NameExpr("Collections"), SimpleName("emptySet"))
            "java.lang.Integer"  -> IntegerLiteralExpr(0)
            "java.lang.Short" -> IntegerLiteralExpr(0)
            "java.lang.Long" -> LongLiteralExpr(0L)
            "java.lang.Character" -> CharLiteralExpr('\u0000')
            "java.lang.Float" -> DoubleLiteralExpr("0.0F")
            "java.lang.Double" -> DoubleLiteralExpr(0.0)
            else -> null
        }
    }
}