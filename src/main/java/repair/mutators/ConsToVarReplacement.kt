package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.LiteralExpr
import com.github.javaparser.ast.expr.NameExpr
import repair.mutators.utils.*

class ConsToVarReplacement: MutatorRepair<LiteralExpr>() {
    override val rank: Int
        get() = 21

    override fun checkedRepair(program: BuggyProgram, litExpr: LiteralExpr): List<NameExpr> {
        val enclosing = getEnclosing(litExpr) ?: return emptyList()
        val varNames = mutableSetOf<String>()

        if(isNumeric(litExpr)){
            varNames.addAll(enclosing.findAll(Parameter::class.java, { isTypeNumber(it.type) })
                    .map { it.nameAsString }
            )

//            varNames.addAll(enclosing.findAll(NameExpr::class.java, { isInScope(it, litExpr) && isTypeNumber(it.calculateResolvedType()) })
            varNames.addAll(enclosing.findAll(NameExpr::class.java, { isInScope(it, litExpr) && isTypeNumber(it) })
                    .map { it.nameAsString }
            )
        }

        val targetAssign = getTargetOfAssign(litExpr)
        val res = varNames.filter { it != targetAssign }
                            .map { NameExpr().setName(it) }
        return res
    }
}
