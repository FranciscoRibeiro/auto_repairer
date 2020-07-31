package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.resolution.types.ResolvedType
import com.github.javaparser.resolution.types.ResolvedVoidType
import repair.mutators.utils.calcType
import java.lang.Exception

class VoidMethodDeletion: MutatorRepair<MethodCallExpr>() {
    override val rank: Int
        get() = 11

    override fun checkedRepair(program: BuggyProgram, metCall: MethodCallExpr): List<Expression> {
        /*val retType: ResolvedType
        try {
            retType = metCall.calculateResolvedType()
        } catch (e: Exception){
            return emptyList()
        }*/
        val retType = calcType(metCall)
        return if(retType is ResolvedVoidType){
            val target = metCall.scope.orElse(null) ?: return emptyList() //StatementDeletion handles this case
            return listOf(target.clone())
        } else emptyList()
    }
}
