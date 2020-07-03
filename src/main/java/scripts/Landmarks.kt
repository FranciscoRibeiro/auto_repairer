package scripts

import fault_localization.reports.qsfl.Landmark
import fault_localization.reports.qsfl.QSFLDiagnosis
import fault_localization.reports.qsfl.Nodes
import java.io.File

fun main(args: Array<String>) {
    File("/home/kiko/PhD/CodeDefenders_projs/").walk().maxDepth(1)
            .filter { it.name in args }
            .flatMap { File(it, "fl_reports/").walk().maxDepth(1) }
            .filter { it.name != "fl_reports" }
            .filter { File(it, "qsfl/diagnosis.txt").length() > 0 }
            .map { Triple(it, QSFLDiagnosis(File(it, "qsfl/diagnosis.txt")), Nodes(File(it, "qsfl/nodes.txt"))) }
            .map { extractMostLikelyLandmarks(it) }
            .filter { it.second.isNotEmpty() }
            .forEach { println(it) }

}

fun extractMostLikelyLandmarks(qsflTriple: Triple<File, QSFLDiagnosis, Nodes>): Pair<String, List<String>> {
    val y = Pair(qsflTriple.first.name.replace("_", "/"),
                qsflTriple.second.mostLikelyFaulty(1).toList().map { it.toList() }.flatten()
                        .filter { qsflTriple.third[it] is Landmark }
                        //.map { "$it ${qsflTriple.second.faultyNodes[it]} LANDMARK" })
                        .map { lid -> "$lid ${qsflTriple.second.faultyNodes.mapNotNull { it[lid] }}" })
    return y
}
