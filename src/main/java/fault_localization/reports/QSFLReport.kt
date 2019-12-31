package fault_localization.reports

import java.io.File

class QSFLReport(path: File) {
    val diagnosis = QSFLDiagnosis(File(path, "diagnosis.txt"))
    val nodes = Nodes(File(path, "nodes.txt"))

    fun mostLikelyFaulty(): List<Int> {
        return diagnosis.mostLikelyFaulty()
    }

    fun nodeInfo(faultyNodeIds: List<Int>): List<NodeInfo?> {
        //return nodes[faultyNodeId]
        return faultyNodeIds.map { nodes[it] }
    }

    fun nodeInfo(faultyNodeId: Int): NodeInfo? {
        return nodes[faultyNodeId]
    }
}
