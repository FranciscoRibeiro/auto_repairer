package repair.mutators

import com.github.javaparser.ast.Node

abstract class MutatorRepair<T: Node> {
    fun repair(node: Node): List<Node> {
        val checkedNode = node as? T ?: return emptyList()
        return checkedRepair(checkedNode)
    }

    abstract fun checkedRepair(checkedNode: T): List<Node>
}
