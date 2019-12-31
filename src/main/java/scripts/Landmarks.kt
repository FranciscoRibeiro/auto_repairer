package scripts

import fault_localization.reports.QSFLDiagnosis
import fault_localization.reports.Nodes
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
    return Pair(qsflTriple.first.name,
                qsflTriple.second.mostLikelyFaulty()
                        .filter { qsflTriple.third[it]?.type == "LANDMARK" }
                        .map { "$it ${qsflTriple.second[it]} LANDMARK" })
}
