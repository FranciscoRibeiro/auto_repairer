package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.LiteralExpr
import com.github.javaparser.ast.expr.NameExpr
import repair.mutators.utils.getEnclosing
import repair.mutators.utils.isInScope
import repair.mutators.utils.isNumeric
import repair.mutators.utils.isTypeNumber

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

            varNames.addAll(enclosing.findAll(NameExpr::class.java, { isInScope(it, litExpr) && isTypeNumber(it.calculateResolvedType()) })
                    .map { it.nameAsString }
            )
        }
        val res = varNames.map { NameExpr().setName(it) }
        return res
    }
}
