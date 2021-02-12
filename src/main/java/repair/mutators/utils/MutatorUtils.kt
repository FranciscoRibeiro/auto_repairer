package repair.mutators.utils

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.type.PrimitiveType
import com.github.javaparser.ast.type.Type
import com.github.javaparser.resolution.Resolvable
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration
import com.github.javaparser.resolution.declarations.ResolvedMethodLikeDeclaration
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration
import com.github.javaparser.resolution.types.ResolvedPrimitiveType
import com.github.javaparser.resolution.types.ResolvedReferenceType
import com.github.javaparser.resolution.types.ResolvedType
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserSymbolDeclaration
import printError
import java.lang.IllegalStateException

fun getEnclosingClass(node: Node): ClassOrInterfaceDeclaration? {
    return node.findAncestor(ClassOrInterfaceDeclaration::class.java).orElse(null)
}

fun getEnclosingCallable(node: Node): CallableDeclaration<*>? {
    return getEnclosingMethod(node) ?: getEnclosingConstructor(node)
}

fun getEnclosingMethod(node: Node): MethodDeclaration? {
    return node.findAncestor(MethodDeclaration::class.java).orElse(null)
}

fun getEnclosingConstructor(node: Node): ConstructorDeclaration? {
    return node.findAncestor(ConstructorDeclaration::class.java).orElse(null)
}

/*fun getEnclosing(nameExpr: NameExpr): MethodDeclaration? {
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
}*/

fun isTypeNumber(type: ResolvedType): Boolean {
    return if(type is ResolvedPrimitiveType) type.isNumeric else false
}

fun isTypeNumber(type: Type): Boolean {
    return if(type is PrimitiveType) type.toString() != "boolean" else false
}

fun isTypeNumber(expr: Expression): Boolean {
    val type = calcType(expr) ?: return false
    return isTypeNumber(type)
}

fun isTypeReference(type: ResolvedType): Boolean {
    return type is ResolvedReferenceType
}

fun isNumeric(litExpr: LiteralExpr): Boolean {
    return litExpr.isIntegerLiteralExpr || litExpr.isLongLiteralExpr || litExpr.isDoubleLiteralExpr
}

fun isInScope(decl: NameExpr, litExpr: LiteralExpr): Boolean {
    /*val varDecl = try {
        decl.resolve()
    } catch (e: UnsolvedSymbolException) {
        e.printStackTrace()
        return false
    }*/
    val varDecl = resolveDecl(decl) ?: return false
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

fun isBitshift(op: BinaryExpr.Operator): Boolean {
    return op == BinaryExpr.Operator.LEFT_SHIFT || op == BinaryExpr.Operator.SIGNED_RIGHT_SHIFT
            || op == BinaryExpr.Operator.UNSIGNED_RIGHT_SHIFT
}

fun isBitwise(op: BinaryExpr.Operator): Boolean {
    return op == BinaryExpr.Operator.BINARY_AND || op == BinaryExpr.Operator.BINARY_OR
            || op == BinaryExpr.Operator.XOR
}

fun isLeftSideAssign(expr: Expression): Boolean {
    return expr.findAncestor(AssignExpr::class.java, { it.target === expr }).isPresent
}

fun isRHS(expr: BinaryExpr, variable: NameExpr): Boolean {
    return expr.right === variable
}

fun getParent(node: Node): Node? {
    return node.parentNode.orElse(null)
}

fun getTargetOfAssign(expr: Expression): String? {
    val parent = getParent(expr) ?: return null
    return when (parent) {
        is AssignExpr -> parent.target.toString()
        is VariableDeclarator -> parent.nameAsString
        else -> null
    }
}

fun forNumbers(op: UnaryExpr.Operator): Boolean {
    return op == UnaryExpr.Operator.BITWISE_COMPLEMENT
}

fun isString(expr: Expression): Boolean {
    /*val type = try {
        expr.calculateResolvedType()
    } catch (e: RuntimeException){
        printError("Could not calculate type of \"$expr\"")
        return false
    }*/
    val type = calcType(expr)
    return if(type is ResolvedReferenceType) type.qualifiedName == "java.lang.String"
    else false
}

fun calcType(expr: Expression): ResolvedType? {
    return try {
        expr.calculateResolvedType()
    } catch(e: UnsolvedSymbolException){
        printError("Could not calculate type of \"$expr\"")
        null
    } catch (e: RuntimeException){
        printError("Could not calculate type of \"$expr\"")
        null
    }
}

fun <T> resolveDecl(res: Resolvable<T>): T? {
    return try {
        res.resolve()
    } catch(e: UnsolvedSymbolException){
        printError("Could not resolve named expression \"$res\"")
        null
    } catch (e: IllegalStateException){
        printError("Illegal state. Node potentially not inserted: \"$res\"")
        null
    }
}

fun overloadingMethods(methodDecl: ResolvedMethodDeclaration): List<ResolvedMethodDeclaration>? {
    val overloading = methodDecl.declaringType().declaredMethods
            .filter { it.name == methodDecl.name && it.signature != methodDecl.signature }
    return if(overloading.isEmpty()) null
    else overloading
}

fun overloadingConstructors(methodDecl: ResolvedConstructorDeclaration): List<ResolvedConstructorDeclaration>? {
    val overloading = methodDecl.declaringType().constructors
            .filter { it.name == methodDecl.name && it.signature != methodDecl.signature }
    return if(overloading.isEmpty()) null
    else overloading
}

fun paramTypes(methodDecl: ResolvedMethodLikeDeclaration): List<ResolvedType> {
    val nParams = methodDecl.numberOfParams
    return (0..nParams-1).map { methodDecl.getParam(it).type }
}

fun pairWithType(expr: Expression): Pair<Expression, ResolvedType>? {
    val type = calcType(expr) ?: return null
    return expr to type
}

fun defaultValue(type: ResolvedType): Expression {
    return if(type.isReferenceType) return StaticJavaParser.parseExpression<ObjectCreationExpr>("new ${type.describe()}()")
    else if(type.isPrimitive){
        when(type.asPrimitive().name){
            "INT", "BYTE", "SHORT", "LONG" -> IntegerLiteralExpr()
            "FLOAT", "DOUBLE" -> DoubleLiteralExpr()
            "CHAR" -> CharLiteralExpr()
            "BOOLEAN" -> BooleanLiteralExpr()
            else -> NullLiteralExpr()
        }
    } else NullLiteralExpr()
}

fun simpleName(type: ResolvedType): String {
    return type.describe().split(".").last()
}

fun isSameType(type: Type, resType: ResolvedType): Boolean {
    return type.toString() == simpleName(resType)
}

fun isSameType(expr: Expression, resType: ResolvedType): Boolean {
    val type = calcType(expr) ?: return false
    return type == resType
}

fun getOtherSide(expr: Expression): String? {
    val parent = getParent(expr) ?: return null
    return if(parent is BinaryExpr){
        return if(parent.left === expr) parent.right.toString()
        else parent.left.toString()
    } else null
}
