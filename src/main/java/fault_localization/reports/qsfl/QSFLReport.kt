package fault_localization.reports.qsfl

import fault_localization.reports.FLReport
import fault_localization.reports.sfl.SFLDiagnosis
import java.io.File

class QSFLReport: FLReport {
//    val diagnosis = QSFLDiagnosis(File(path, "diagnosis.txt"))
    val diagnosis: QSFLDiagnosis
//    val nodes = Nodes(File(path, "nodes.txt"))
    val nodes: Nodes

    constructor(reportPath: String){
        diagnosis = QSFLDiagnosis(File(reportPath, "diagnosis.txt"))
        nodes = Nodes(File(reportPath, "nodes.txt"))
    }

    /* This constructor is a workaround to prevent previous experiments from breaking. */
    constructor(reportPath: File){
        diagnosis = QSFLDiagnosis(File(reportPath, "diagnosis.txt"))
        nodes = Nodes(File(reportPath, "nodes.txt"))
    }

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
