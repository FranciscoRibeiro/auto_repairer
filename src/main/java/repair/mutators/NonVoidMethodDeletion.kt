package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.comments.Comment
import com.github.javaparser.ast.comments.LineComment
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.Statement
import com.github.javaparser.resolution.types.ResolvedPrimitiveType
import com.github.javaparser.resolution.types.ResolvedReferenceType
import com.github.javaparser.resolution.types.ResolvedVoidType

class NonVoidMethodDeletion: MutatorRepair<MethodCallExpr>() {
    override val rank: Int
        get() = 8

    override fun checkedRepair(program: BuggyProgram, metCall: MethodCallExpr): List<Expression> {
        val retType = metCall.calculateResolvedType()
        return if(retType !is ResolvedVoidType){
            val target = metCall.scope.orElse(null)
            return if(target == null){
                when (retType) {
                    is ResolvedPrimitiveType -> {
                        when(retType.name){
                            "INT", "BYTE", "SHORT", "LONG" -> listOf(IntegerLiteralExpr(0))
                            "FLOAT", "DOUBLE" -> listOf(DoubleLiteralExpr(0.0))
                            "CHAR" -> listOf(CharLiteralExpr('\u0000'))
                            else -> emptyList()
                        }
                    }
                    is ResolvedReferenceType -> listOf(NullLiteralExpr())
                    else -> emptyList()
                }
            } else listOf(target.clone())
        } else emptyList()
    }
}
