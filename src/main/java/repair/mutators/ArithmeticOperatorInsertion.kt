package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.expr.BinaryExpr.Operator.*
import repair.mutators.utils.calcType

class ArithmeticOperatorInsertion(val op: BinaryExpr.Operator? = null, val exprToAdd: Expression? = null): MutatorRepair<Expression>() {
    override val rank: Int
        get() = 18

    override fun checkedRepair(program: BuggyProgram, expr: Expression): List<Expression> {
        return if(op == null && exprToAdd == null){
            val exprType = calcType(expr) ?: return emptyList()
            when(exprType.describe()){
                "int" -> return listOf(Pair(1, PLUS), Pair(1, MINUS),
                        Pair(Int.MAX_VALUE, PLUS), Pair(Int.MAX_VALUE, MINUS), Pair(Int.MAX_VALUE, MULTIPLY),
                        Pair(Int.MAX_VALUE, DIVIDE), Pair(Int.MIN_VALUE, PLUS), Pair(Int.MIN_VALUE, MINUS),
                        Pair(Int.MIN_VALUE, MULTIPLY), Pair(Int.MIN_VALUE, DIVIDE)
                ).map { EnclosedExpr(BinaryExpr(EnclosedExpr(expr.clone()), IntegerLiteralExpr(it.first.toString()), it.second)) }
                "long" -> return listOf(Pair(1, PLUS), Pair(1, MINUS),
                        Pair(Long.MAX_VALUE, PLUS), Pair(Long.MAX_VALUE, MINUS), Pair(Long.MAX_VALUE, MULTIPLY),
                        Pair(Long.MAX_VALUE, DIVIDE), Pair(Long.MIN_VALUE, PLUS), Pair(Long.MIN_VALUE, MINUS),
                        Pair(Long.MIN_VALUE, MULTIPLY), Pair(Long.MIN_VALUE, DIVIDE)
                ).map { EnclosedExpr(BinaryExpr(EnclosedExpr(expr.clone()), LongLiteralExpr(it.first.toString()), it.second)) }
                "double" -> return listOf(Pair(1, PLUS), Pair(1, MINUS),
                        Pair(Double.MAX_VALUE, PLUS), Pair(Double.MAX_VALUE, MINUS), Pair(Double.MAX_VALUE, MULTIPLY),
                        Pair(Double.MAX_VALUE, DIVIDE), Pair(Double.MIN_VALUE, PLUS), Pair(Double.MIN_VALUE, MINUS),
                        Pair(Double.MIN_VALUE, MULTIPLY), Pair(Double.MIN_VALUE, DIVIDE)
                ).map { EnclosedExpr(BinaryExpr(EnclosedExpr(expr.clone()), DoubleLiteralExpr(it.first.toString()), it.second)) }
                "float" -> return listOf(Pair(1, PLUS), Pair(1, MINUS),
                        Pair(Double.MAX_VALUE, PLUS), Pair(Double.MAX_VALUE, MINUS), Pair(Double.MAX_VALUE, MULTIPLY),
                        Pair(Double.MAX_VALUE, DIVIDE), Pair(Double.MIN_VALUE, PLUS), Pair(Double.MIN_VALUE, MINUS),
                        Pair(Double.MIN_VALUE, MULTIPLY), Pair(Double.MIN_VALUE, DIVIDE)
                ).map { EnclosedExpr(BinaryExpr(EnclosedExpr(expr.clone()), DoubleLiteralExpr(it.first.toString()), it.second)) }
                else -> emptyList()
            }
        } else emptyList()
    }
}
