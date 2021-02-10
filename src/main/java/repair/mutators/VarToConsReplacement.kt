package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.*
import repair.mutators.utils.*

class VarToConsReplacement: MutatorRepair<NameExpr>() {
    override val rank: Int
        get() = 19

    val defaultValues = listOf("1", "0", "-1").map { IntegerLiteralExpr(it) }

    override fun checkedRepair(program: BuggyProgram, nameExpr: NameExpr): List<LiteralStringValueExpr> {
        val parent = getParent(nameExpr)
        if(parent != null) {
            when(parent) {
                is AssignExpr -> if(parent.target === nameExpr) return emptyList()
                is UnaryExpr -> if(!forNumbers(parent.operator)) return emptyList()
            }
        }

        val enclosingDecl = getEnclosingCallable(nameExpr) ?: return emptyList()
        val type = calcType(nameExpr)
        //if it is an array or NOT a number
        if(type != null && (type.isArray || !isTypeNumber(type))) return emptyList()

        val constants = (enclosingDecl.findAll(LiteralStringValueExpr::class.java, { isNumeric(it) }) + defaultValues)
                                .distinctBy { it.value }
                                .map { it.clone() }

        return if(parent is BinaryExpr && isArithmetic(parent.operator)){
            constants.filter { !invalidOperation(parent, nameExpr, it) }
        } else constants
    }

    private fun invalidOperation(expr: BinaryExpr, variable: NameExpr, cons: LiteralStringValueExpr): Boolean {
        val op = expr.operator
        return when(op){
            BinaryExpr.Operator.PLUS -> isZero(cons)
            BinaryExpr.Operator.MINUS -> isRHS(expr, variable) && isZero(cons)
            BinaryExpr.Operator.MULTIPLY -> isOne(cons)
            BinaryExpr.Operator.DIVIDE -> isRHS(expr, variable) && (isOne(cons) || isZero(cons))
            else -> false
        }
    }

    private fun isOne(cons: LiteralStringValueExpr): Boolean {
        return when(cons){
            is IntegerLiteralExpr -> cons.asInt() == 1
            is DoubleLiteralExpr -> cons.asDouble() == 1.0
            is LongLiteralExpr -> cons.asLong() == 1L
            else -> false
        }
    }

    private fun isZero(cons: LiteralStringValueExpr): Boolean {
        return when(cons){
            is IntegerLiteralExpr -> cons.asInt() == 0
            is DoubleLiteralExpr -> cons.asDouble() == 0.0
            is LongLiteralExpr -> cons.asLong() == 0L
            else -> false
        }
    }


}
