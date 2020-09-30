package fault_localization.reports.sfl

import java.io.File

//fun main() {
//    val sflDiag = SFLDiagnosis(File("/home/kiko/PhD/CodeDefenders_projs/HSLColor/fl_reports/1007_19_00000012/site/gzoltar/sfl/txt/ochiai.ranking.csv"))
//    sflDiag.faultyLines.forEach { i, componentInfo ->  println("$i, $componentInfo")}
//}

class SFLDiagnosis {
//    var faultyLines: List<Map<Int, SFLComponent>>
    var faultyLines: List<Map<SFLComponent, Double>>
    var similarityCoefficient: String

    constructor(diagnosisFile: File, similarityCoefficient: String = "ochiai") {
        this.faultyLines = diagnosisFile.useLines { parseLines(it) }
        this.similarityCoefficient = similarityCoefficient
    }

    private fun parseLines(csvLines: Sequence<String>): List<Map<SFLComponent, Double>> {
        return csvLines.drop(1) //ignore CSV header
                .map { getLineAndComponentInfo(it) }
                .groupBy { it.second }
                .map { it.value.toMap() }
//               .forEach { this[it.first] = it.second }
    }

    private fun getLineAndComponentInfo(line: String): Pair<SFLComponent, Double> {
        val (compInfo, prob) = line.split(";")
        val (classAndMethod, line) = compInfo.split(":")
        val (fullClassName, methodSignature) = classAndMethod.split("#")
        val (packageName, simpleClassName) = fullClassName.split("$")
        return Pair(SFLComponent(packageName, simpleClassName, methodSignature, line.toInt()), prob.toDouble())
    }

    fun mostLikelyFaulty(upTo: Int): Sequence<Sequence<SFLComponent>> {
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