package repair.mutators.utils

import com.github.javaparser.ast.body.CallableDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.type.PrimitiveType
import com.github.javaparser.ast.type.Type
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration
import com.github.javaparser.resolution.types.ResolvedPrimitiveType
import com.github.javaparser.resolution.types.ResolvedReferenceType
import com.github.javaparser.resolution.types.ResolvedType
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserSymbolDeclaration

fun getEnclosing(nameExpr: NameExpr): MethodDeclaration? {
    return nameExpr.findAncestor(MethodDeclaration::class.java).orElse(null)
}

fun getEnclosing(litExpr: LiteralExpr): CallableDeclaration<*>? {
    return getEnclosingMethod(litExpr) ?: getEnclosingConstructor(litExpr)
}

fun getEnclosingMethod(litExpr: LiteralExpr): MethodDeclaration? {
    return litExpr.findAncestor(MethodDeclaration::class.java).orElse(null)
}

fun getEnclosingConstructor(litExpr: LiteralExpr): ConstructorDeclaration? {
    return litExpr.findAncestor(ConstructorDeclaration::class.java).orElse(null)
}

fun isTypeNumber(type: ResolvedType): Boolean {
    return if(type is ResolvedPrimitiveType) type.isNumeric else false
}

fun isTypeNumber(type: Type): Boolean {
    return if(type is PrimitiveType) type.toString() != "boolean" else false
}

fun isTypeReference(type: ResolvedType): Boolean {
    return type is ResolvedReferenceType
}

fun isTypeReference(type: Type): Boolean {
    return type.isReferenceType
}

fun isNumeric(litExpr: LiteralExpr): Boolean {
    return litExpr.isIntegerLiteralExpr || litExpr.isLongLiteralExpr || litExpr.isDoubleLiteralExpr
}

fun isInScope(decl: NameExpr, litExpr: LiteralExpr): Boolean {
    val varDecl = try { decl.resolve() } catch (e: UnsolvedSymbolException) { return false }
    return line(varDecl) < litExpr.range.get().begin.line
}

fun line(varDecl: ResolvedValueDeclaration): Int {
    //Workaround: wrappedNode does not seem to be in a common supertype
    val node = when(varDecl){
        is JavaParserFieldDeclaration -> varDecl.wrappedNode
        is JavaParserParameterDeclaration -> varDecl.wrappedNode
        is JavaParserSymbolDeclaration -> varDecl.wrappedNode
        else -> null
    } ?: return 0
    val nodeRange = node.range.orElse(null) ?: return 0
    return nodeRange.begin.line
}

fun isRelational(op: BinaryExpr.Operator): Boolean {
    return op == BinaryExpr.Operator.EQUALS || op == BinaryExpr.Operator.NOT_EQUALS
            || op == BinaryExpr.Operator.LESS || op == BinaryExpr.Operator.LESS_EQUALS
            || op == BinaryExpr.Operator.GREATER || op == BinaryExpr.Operator.GREATER_EQUALS
}

fun isConditional(op: BinaryExpr.Operator): Boolean {
    return op == BinaryExpr.Operator.AND || op == BinaryExpr.Operator.OR
}

fun isArithmetic(op: BinaryExpr.Operator): Boolean {
    return op == BinaryExpr.Operator.PLUS || op == BinaryExpr.Operator.MINUS
            || op == BinaryExpr.Operator.MULTIPLY || op == BinaryExpr.Operator.DIVIDE
            || op == BinaryExpr.Operator.REMAINDER
}

fun isLeftSideAssign(nameExpr: NameExpr): Boolean {
    return nameExpr.findAncestor(AssignExpr::class.java, { it.target === nameExpr }).isPresent
}

fun isRHS(expr: BinaryExpr, variable: NameExpr): Boolean {
    return expr.right === variable
}

fun getTargetOfAssign(expr: Expression): String? {
    val parent = expr.parentNode.orElse(null)
    return if(parent != null) {
        when (parent) {
            is AssignExpr -> parent.target.toString()
            is VariableDeclarator -> parent.nameAsString
            else -> null
        }
    } else null
}
