package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.*
import repair.mutators.utils.*

class VarToConsReplacement: MutatorRepair<NameExpr>() {
    override val rank: Int
        get() = 9

    override fun checkedRepair(program: BuggyProgram, nameExpr: NameExpr): List<LiteralStringValueExpr> {
        if(isLeftSideAssign(nameExpr)) return emptyList()

        val methodDecl = getEnclosing(nameExpr) ?: return emptyList()
        val type = nameExpr.calculateResolvedType()
        if(type.isArray) return emptyList()
//        val constants = mutableSetOf<LiteralStringValueExpr>()

        return if(isTypeNumber(type)){
            val constants = methodDecl.findAll(LiteralStringValueExpr::class.java, { isNumeric(it) }).distinctBy { it.value }

            val operation = nameExpr.findAncestor(BinaryExpr::class.java).orElse(null)
            return if(operation == null || !isArithmetic(operation.operator)) {
                constants.map { it.clone() }
            } else {
                constants.filter { !invalidOperation(operation, nameExpr, it) }
                        .map { it.clone() }
            }
        } else {
            emptyList()
        }
    }

    private fun invalidOperation(expr: BinaryExpr, variable: NameExpr, cons: LiteralStringValueExpr): Boolean {
        val op = expr.operator
        return when(op){
            BinaryExpr.Operator.PLUS -> isZero(cons)
            BinaryExpr.Operator.MINUS -> isRHS(expr, variable) && isZero(cons)
            BinaryExpr.Operator.MULTIPLY -> isOne(cons)
            BinaryExpr.Operator.DIVIDE -> isRHS(expr, variable) && isOne(cons)
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
