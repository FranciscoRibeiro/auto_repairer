package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.type.ReferenceType
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration
import com.github.javaparser.resolution.types.ResolvedReferenceType
import com.github.javaparser.resolution.types.ResolvedType
import printError
import repair.mutators.utils.*

class VarToVarReplacement(val ignoreTypes: Boolean = false): MutatorRepair<NameExpr>() {
    override val rank: Int
        get() = 3

    override fun checkedRepair(program: BuggyProgram, nameExpr: NameExpr): List<NameExpr> {
        val methodDecl = getEnclosing(nameExpr) ?: return emptyList()
//        val type = nameExpr.calculateResolvedType()
        val type = calcType(nameExpr) ?: return emptyList()
        if(type.isArray) return emptyList()
        val varNames = mutableSetOf<String>()

        if(isTypeNumber(type)) {
            varNames.addAll(
                    methodDecl.findAll(Parameter::class.java, { isTypeNumber(it.type) })
                            .map { it.nameAsString }
            )
            varNames.addAll(
//                    methodDecl.findAll(NameExpr::class.java, { isTypeNumber(it.calculateResolvedType()) })
                    methodDecl.findAll(NameExpr::class.java, { isTypeNumber(it) })
                            .map { it.nameAsString }
            )
        }
        else if(isTypeReference(type)){
            varNames.addAll(
                    methodDecl.findAll(Parameter::class.java, { ignoreTypes || matchesType(it, type as ResolvedReferenceType) })
                            .map { it.nameAsString }
            )
            varNames.addAll(
                    methodDecl.findAll(NameExpr::class.java, { ignoreTypes || matchesType(it, type as ResolvedReferenceType) })
                            .map { it.nameAsString }
            )
        }

        val targetAssign = getTargetOfAssign(nameExpr)
        val res = varNames.filter { it != nameExpr.nameAsString && it != targetAssign}
                            .map { NameExpr(it) }
        return res
    }

    private fun matchesType(nameExpr: NameExpr, type: ResolvedReferenceType): Boolean {
//        val exprType = nameExpr.calculateResolvedType()
        val exprType = calcType(nameExpr) ?: return false
        return if(exprType is ResolvedReferenceType){
            exprType == type
        } else false
    }

    private fun matchesType(param: Parameter, type: ResolvedReferenceType): Boolean {
        return if(param.type is ReferenceType){
            type.typeDeclaration.get().name == param.type.toString().substringBefore("<")
        } else false
    }
}
