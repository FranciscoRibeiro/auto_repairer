package fault_localization.reports.qsfl

import org.json.JSONObject
import java.io.File

class QSFLDiagnosis {
    lateinit var faultyNodes: List<Map<Int, Double>>

    constructor(projPath: String) {
        QSFLDiagnosis(File("$projPath/target/qsfl/diagnosis.txt"))
    }

    constructor(diagnosisFile: File){
        this.faultyNodes = if(diagnosisFile.exists()) diagnosisFile.useLines { parseLines(it) } else emptyList()
    }

    private fun parseLines(lines: Sequence<String>): List<Map<Int, Double>> {
        return lines.map { getIdAndProb(it) }
                .groupBy { it.second }
                .map { it.value.toMap() }
    }

    private fun getIdAndProb(line: String): Pair<Int, Double> {
        var jsonObj = JSONObject(line)
        var nodeId = jsonObj.getInt("nodeId")
        var score = jsonObj.getDouble("score")
        return Pair(nodeId, score)
    }

    fun mostLikelyFaulty(upTo: Int): Sequence<Sequence<Int>> {
        return this.faultyNodes.asSequence()
                                .take(upTo)
                                .filter { it.values.first() != 0.0 }
                                .map { it.asSequence().map { it.key } }
    }
}
