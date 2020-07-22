package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.*
import com.github.javaparser.resolution.UnsolvedSymbolException
import printError
import repair.mutators.utils.*

class VarToConsReplacement: MutatorRepair<NameExpr>() {
    override val rank: Int
        get() = 9

    override fun checkedRepair(program: BuggyProgram, nameExpr: NameExpr): List<LiteralStringValueExpr> {
        val parent = getParent(nameExpr)
        if(parent != null) {
            when(parent) {
                is AssignExpr -> if(parent.target === nameExpr) return emptyList()
                is UnaryExpr -> if(!forNumbers(parent.operator)) return emptyList()
            }
        }

        val methodDecl = getEnclosing(nameExpr) ?: return emptyList()
//        val type = nameExpr.calculateResolvedType()
        val type = calcType(nameExpr) ?: return emptyList()
        if(type.isArray) return emptyList()

        return if(isTypeNumber(type)){
            val constants = methodDecl.findAll(LiteralStringValueExpr::class.java, { isNumeric(it) })
                                    .distinctBy { it.value }
                                    .map { it.clone() }

//            val operation = nameExpr.findAncestor(BinaryExpr::class.java).orElse(null)
            return if(parent is BinaryExpr && isArithmetic(parent.operator)){
                constants.filter { !invalidOperation(parent, nameExpr, it) }
            } else constants
//            return if(operation == null || !isArithmetic(operation.operator)) {
//                constants
//            } else {
//                constants.filter { !invalidOperation(operation, nameExpr, it) }
//            }
        } else emptyList()
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
