package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.type.ReferenceType
import com.github.javaparser.resolution.types.ResolvedReferenceType
import repair.mutators.utils.getEnclosing
import repair.mutators.utils.getTargetOfAssign
import repair.mutators.utils.isTypeNumber
import repair.mutators.utils.isTypeReference

class VarToVarReplacement: MutatorRepair<NameExpr>() {
    override val rank: Int
        get() = 5

    override fun checkedRepair(program: BuggyProgram, nameExpr: NameExpr): List<NameExpr> {
        val methodDecl = getEnclosing(nameExpr) ?: return emptyList()
        val type = nameExpr.calculateResolvedType()
        if(type.isArray) return emptyList()
        val varNames = mutableSetOf<String>()

        if(isTypeNumber(type)) {
            varNames.addAll(
                    methodDecl.findAll(Parameter::class.java, { isTypeNumber(it.type) })
//                            .filter { it.nameAsString != nameExpr.nameAsString }
                            .map { it.nameAsString }
            )
            varNames.addAll(
                    methodDecl.findAll(NameExpr::class.java, { isTypeNumber(it.calculateResolvedType()) })
//                            .filter { it.nameAsString != nameExpr.nameAsString }
                            .map { it.nameAsString }
            )
        }
        else if(isTypeReference(type)){
            varNames.addAll(
                    methodDecl.findAll(Parameter::class.java, { isTypeReference(it.type) && isSameType(type as ResolvedReferenceType, it.type as ReferenceType) })
//                            .filter { it.nameAsString != nameExpr.nameAsString }
                            .map { it.nameAsString }
            )
            varNames.addAll(
                    methodDecl.findAll(NameExpr::class.java, { isTypeReference(it.calculateResolvedType()) })
//                            .filter { it.nameAsString != nameExpr.nameAsString }
                            .map { it.nameAsString }
            )
        }

        val targetAssign = getTargetOfAssign(nameExpr)
        val res = varNames.filter { it != nameExpr.nameAsString && it != targetAssign}
                            .map { NameExpr(it) }
        return res
    }

    private fun isSameType(mainType: ResolvedReferenceType, otherType: ReferenceType): Boolean {
        return mainType.typeDeclaration.name == otherType.toString().substringBefore("<")
    }
}
