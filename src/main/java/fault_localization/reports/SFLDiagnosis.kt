package fault_localization.reports

import java.io.File

//fun main() {
//    val sflDiag = SFLDiagnosis(File("/home/kiko/PhD/CodeDefenders_projs/HSLColor/fl_reports/1007_19_00000012/site/gzoltar/sfl/txt/ochiai.ranking.csv"))
//    sflDiag.faultyLines.forEach { i, componentInfo ->  println("$i, $componentInfo")}
//}

class SFLDiagnosis {
    var faultyLines: List<Map<Int, ComponentInfo>>
    var similarityCoefficient: String

    constructor(diagnosisFile: File, similarityCoefficient: String = "ochiai") {
        this.faultyLines = diagnosisFile.useLines { parseLines(it) }
        this.similarityCoefficient = similarityCoefficient
    }

    private fun parseLines(csvLines: Sequence<String>): List<Map<Int, ComponentInfo>> {
        return csvLines.drop(1) //ignore CSV header
                .map { getLineAndComponentInfo(it) }
                .groupBy { it.second.probability }
                .map { it.value.toMap() }
//               .forEach { this[it.first] = it.second }
    }

    private fun getLineAndComponentInfo(line: String): Pair<Int, ComponentInfo> {
        val (compInfo, prob) = line.split(";")
        val (classAndMethod, line) = compInfo.split(":")
        val (className, methodSignature) = classAndMethod.split("#")
        return Pair(line.toInt(), ComponentInfo(className.drop(1), methodSignature, prob.toDouble()))
    }

    fun mostLikelyFaulty(upTo: Int): Sequence<Sequence<Int>> {
        return this.faultyLines.asSequence()
                                .take(upTo)
                                .map { it.asSequence().map { it.key } }
//        var counter = 0
//        var currentProb = faultyLines.values.first().probability
//        return faultyLines.keys.takeWhile { key ->
//            if(faultyLines[key]?.probability != currentProb) {
//                currentProb = faultyLines[key]?.probability!!
//                counter++
//            }
//            counter != upTo
//            }
    }
}