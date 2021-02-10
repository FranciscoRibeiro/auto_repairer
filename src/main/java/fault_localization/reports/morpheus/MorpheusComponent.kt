package fault_localization.reports.morpheus

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.CallableDeclaration
import com.github.javaparser.ast.type.Type
import fault_localization.reports.FLComponent
import repair.mutators.utils.getEnclosingCallable

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
        val enclosingSignature = getEnclosingCallable(node)?.signature ?: return false
        return signatureMatchCallable(enclosingSignature, callable)
    }

    private fun signatureMatchCallable(signature: CallableDeclaration.Signature, callable: Callable): Boolean {
        return nameMatchCallableName(signature, callable)
                && typesMatchCallableTypes(signature, callable)
    }

    private fun nameMatchCallableName(signature: CallableDeclaration.Signature, callable: Callable): Boolean {
        return signature.name == callable.name
    }

    private fun typesMatchCallableTypes(signature: CallableDeclaration.Signature, callable: Callable): Boolean {
        return signature.parameterTypes.zip(callable.simpleParameterTypes())
                .all { (t1, t2) -> typeMatchString(t1, t2) }
    }

    private fun typeMatchString(type: Type, typeAsString: String): Boolean {
        return type.asString() == typeAsString
    }
}
