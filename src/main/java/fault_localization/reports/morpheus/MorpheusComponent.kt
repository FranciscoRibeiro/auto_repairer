package fault_localization.reports.morpheus

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.CallableDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.type.Type
import fault_localization.reports.FLComponent
import repair.mutators.utils.getEnclosingCallable
import repair.mutators.utils.getEnclosingClass

class MorpheusComponent(val packageName: String,
                        val simpleClassName: String,
                        val mutOp: String,
                        val callable: Callable,
                        val startEndLines: Pair<Int, Int>,
                        val startEndColumns: Pair<Int, Int>): FLComponent {

    fun hasSamePosition(node: Node): Boolean {
        val nodeRange = node.range.orElseGet { null } ?: return false
        val (startLine, endLine) = Pair(nodeRange.begin.line, nodeRange.end.line)
        val (startColumn, endColumn) = Pair(nodeRange.begin.column, nodeRange.end.column)
        return startLine == startEndLines.first && endLine == startEndLines.second
                && startColumn >= startEndColumns.first && endColumn <= startEndColumns.second
    }

    fun hasSameLine(node: Node): Boolean {
        val nodeRange = node.range.orElseGet { null } ?: return false
        val (startLine, endLine) = Pair(nodeRange.begin.line, nodeRange.end.line)
        return startLine == startEndLines.first && endLine == startEndLines.second
    }

    fun hasSameCallable(node: Node): Boolean {
        val enclosingCallable = getEnclosingCallable(node) ?: return false
        val enclosingClass = getEnclosingClass(enclosingCallable) ?: return false
        return signatureMatchCallable(enclosingClass, enclosingCallable, callable)
    }

    private fun signatureMatchCallable(classDecl: ClassOrInterfaceDeclaration, callableDecl: CallableDeclaration<*>, callable: Callable): Boolean {
        return classNameMatch(classDecl, callable) && callableNameMatch(callableDecl, callable)
                && typesMatch(callableDecl, callable)
    }

    private fun classNameMatch(classDecl: ClassOrInterfaceDeclaration, callable: Callable): Boolean {
        return classDecl.nameAsString == callable.className
    }

    private fun callableNameMatch(callableDecl: CallableDeclaration<*>, callable: Callable): Boolean {
        return callableDecl.signature.name == callable.callableName
    }

    private fun typesMatch(callableDecl: CallableDeclaration<*>, callable: Callable): Boolean {
        return callableDecl.signature.parameterTypes.zip(callable.simpleParameterTypes())
                .all { (t1, t2) -> typeMatchString(t1, t2) }
    }

    private fun typeMatchString(type: Type, typeAsString: String): Boolean {
        return type.asString() == typeAsString
    }
}
