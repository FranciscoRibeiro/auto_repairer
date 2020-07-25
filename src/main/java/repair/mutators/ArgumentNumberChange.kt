package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.resolution.types.ResolvedType
import repair.has
import repair.mutators.utils.*

class ArgumentNumberChange: MutatorRepair<Expression>() {
    override val rank: Int
        get() = 24

    override fun checkedRepair(program: BuggyProgram, callExpr: Expression): List<Expression> {
        when (callExpr) {
            is MethodCallExpr -> {
                val methodDecl = resolveDecl(callExpr) ?: return emptyList()
                val overloading = overloadingMethods(methodDecl)
                        ?.filter { it.numberOfParams != callExpr.arguments.size } ?: return emptyList()
                val paramsAndTypes = callExpr.arguments.mapNotNull { pairWithType(it) }
                val x = overloading.map { paramTypes(it) }
                        .map { fitParams(paramsAndTypes, it) }
                        .flatMap { overloadingMethodFits ->
                            overloadingMethodFits.map {
                                MethodCallExpr(callExpr.scope.orElse(null)?.clone(),
                                        callExpr.typeArguments.orElse(null),
                                        callExpr.nameAsString,
                                        NodeList(it))
                            }
                        }
                return x
            }
            is ObjectCreationExpr -> {
                val constructorDecl = resolveDecl(callExpr) ?: return emptyList()
                val overloading = overloadingConstructors(constructorDecl)
                        ?.filter { it.numberOfParams != callExpr.arguments.size } ?: return emptyList()
                val paramsAndTypes = callExpr.arguments.mapNotNull { pairWithType(it) }
                val x = overloading.map { paramTypes(it) }
                        .map { fitParams(paramsAndTypes, it) }
                        .flatMap { overloadingConstructorFits ->
                            overloadingConstructorFits.map {
                                ObjectCreationExpr(null, callExpr.type, NodeList(it))
                            }
                        }
                return x
            }
            else -> return emptyList()
        }
    }

    private fun fitParams(paramsAndTypes: List<Pair<Expression, ResolvedType>>, types: List<ResolvedType>): List<List<Expression>> {
        val paramFits = types.map { paramTypeMatches(paramsAndTypes, it) }
                            .replaceDupsWithDefault()
                            .map { lExpr -> lExpr.map { exprAndType -> exprAndType.first } }
//        if (paramFits.any { it.isEmpty() }) return emptyList()
        return paramFits.fold(
                listOf(listOf()),
                { acc, lExpr -> acc.flatMap { subList -> lExpr.map { expr -> subList + expr } } }
        )
    }

    private fun List<List<Pair<Expression, ResolvedType>>>.replaceDupsWithDefault(): List<List<Pair<Expression, ResolvedType>>> {
        val noDups = mutableListOf<MutableList<Pair<Expression, ResolvedType>>>()
        for (elemList in this){
            val subNoDups = mutableListOf<Pair<Expression, ResolvedType>>()
            noDups.add(subNoDups)
            for(elem in elemList){
                if(!noDups.has(elem)) subNoDups.add(elem)
                else subNoDups.add(defaultValue(elem.second) to elem.second)
            }
        }
        return noDups
    }

    private fun paramTypeMatches(paramsAndTypes: List<Pair<Expression, ResolvedType>>, type: ResolvedType): List<Pair<Expression, ResolvedType>> {
        val matches = paramsAndTypes.filter { it.second == type }
        return if(matches.isEmpty()) listOf(defaultValue(type) to type)
        else matches
    }
}
