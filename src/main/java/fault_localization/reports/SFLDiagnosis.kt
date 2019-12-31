package fault_localization.reports

import java.io.File

fun main() {
    val sflDiag = SFLDiagnosis(File("/home/kiko/PhD/CodeDefenders_projs/HSLColor/fl_reports/1007_19_00000012/site/gzoltar/sfl/txt/ochiai.ranking.csv"))
    sflDiag.faultyLines.forEach { i, d -> println("$i, $d") }
}

class SFLDiagnosis {
    val faultyLines = LinkedHashMap<Int, Double>()
    var similarityCoefficient: String

    constructor(diagnosisFile: File, similarityCoefficient: String = "ochiai") {
        diagnosisFile.useLines { parseLines(it) }
        this.similarityCoefficient = similarityCoefficient
    }

    private fun parseLines(csvLines: Sequence<String>) {
        csvLines.drop(1) //ignore CSV header
                .map { getLineAndProbability(it) }
                .forEach { this[it.first] = it.second }
    }

    private fun getLineAndProbability(line: String): Pair<Int, Double> {
        val fields = line.split(";")
        return Pair(fields[0].split(":")[1].toInt(), fields[1].toDouble())
    }

    operator fun get(i: Int): Double? {
        return faultyLines.get(i)
    }

    operator fun set(i: Int, value: Double) {
        faultyLines.put(i, value)
    }

    fun mostLikelyFaulty(): List<Int> {
        val highestProb = faultyLines.values.first()
        return faultyLines.keys.takeWhile { key -> faultyLines[key] == highestProb }
    }
}