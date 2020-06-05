package fault_localization.reports.qsfl

import fault_localization.reports.ComponentInfo
import org.json.JSONObject
import java.io.File

class QSFLDiagnosis {
    lateinit var faultyNodes: List<Map<Int, Double>>

    constructor(projPath: String) {
        QSFLDiagnosis(File("$projPath/target/qsfl/diagnosis.txt"))
    }

    constructor(diagnosisFile: File){
        this.faultyNodes = diagnosisFile.useLines { parseLines(it) }
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
        //return faultyNodes.keys.first()
//        val highestProb = faultyNodes.values.first()
//        return faultyNodes.keys.takeWhile { key -> faultyNodes[key] == highestProb}

        return this.faultyNodes.asSequence()
                                .take(upTo)
                                .map { it.asSequence().map { it.key } }

//        var counter = 0
//        var currentProb = faultyNodes.values.first()
//        return faultyNodes.keys.takeWhile { key ->
//            if(faultyNodes[key] != currentProb) {
//                currentProb = faultyNodes[key]!!
//                counter++
//            }
//            counter != upTo
//        }
    }
}