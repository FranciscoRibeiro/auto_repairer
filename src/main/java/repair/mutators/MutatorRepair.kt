package repair.mutators

import BuggyProgram
import com.github.javaparser.ast.Node

abstract class MutatorRepair<T: Node> {
    abstract val rank: Int

    fun repair(program: BuggyProgram, node: Node): List<Node> {
        val checkedNode = node as? T ?: return emptyList()
        return checkedRepair(program, checkedNode)
    }

    abstract fun checkedRepair(program: BuggyProgram, checkedNode: T): List<Node>
}
