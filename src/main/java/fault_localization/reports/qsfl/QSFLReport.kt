package fault_localization.reports.qsfl

import fault_localization.reports.FLReport
import java.io.File

class QSFLReport(path: File): FLReport {
    val diagnosis = QSFLDiagnosis(File(path, "diagnosis.txt"))
    val nodes = Nodes(File(path, "nodes.txt"))

    override fun mostLikelyFaulty(upTo: Int): Sequence<Sequence<QSFLNode>> {
        return diagnosis.mostLikelyFaulty(upTo)
                .map { ids-> ids.mapNotNull { id -> nodes[id] } }
    }

    fun nodeInfo(faultyNodeIds: List<Int>): List<QSFLNode?> {
        return faultyNodeIds.map { nodes[it] }
    }

    fun nodeInfo(faultyNodeId: Int): QSFLNode? {
        return nodes[faultyNodeId]
    }
}
