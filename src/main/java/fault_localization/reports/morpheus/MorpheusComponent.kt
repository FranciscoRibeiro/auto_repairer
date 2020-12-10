package fault_localization.reports.morpheus

import com.github.javaparser.ast.Node
import fault_localization.reports.FLComponent

class MorpheusComponent(val packageName: String,
                        val simpleClassName: String,
                        val mutOp: String,
                        val startEndLines: Pair<Int, Int>,
                        val startEndColumns: Pair<Int, Int>): FLComponent {

    fun hasSamePosition(node: Node): Boolean {
        val nodeRange = node.range.orElseGet { null } ?: return false
        val (startLine, endLine) = Pair(nodeRange.begin.line, nodeRange.end.line)
        val (startColumn, endColumn) = Pair(nodeRange.begin.column, nodeRange.end.column)
        return startLine == startEndLines.first && endLine == startEndLines.second
                && startColumn == startEndColumns.first && endColumn == startEndColumns.second
    }

    fun hasSameLine(node: Node): Boolean {
        val nodeRange = node.range.orElseGet { null } ?: return false
        val (startLine, endLine) = Pair(nodeRange.begin.line, nodeRange.end.line)
        return startLine == startEndLines.first && endLine == startEndLines.second
    }
}
