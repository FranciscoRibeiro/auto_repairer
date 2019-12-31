package fault_localization.reports

import org.json.JSONObject
import java.io.File

class QSFLDiagnosis {
    val faultyNodes = LinkedHashMap<Int, Double>()

    constructor(projPath: String) {
        File("$projPath/target/qsfl/diagnosis.txt").forEachLine {
            line ->
                var jsonObj = JSONObject(line)
                var nodeId = jsonObj.getInt("nodeId")
                var score = jsonObj.getDouble("score")
                this[nodeId] = score
        }
    }

    constructor(diagnosisFile: File){
        diagnosisFile.forEachLine {
            line ->
            var jsonObj = JSONObject(line)
            var nodeId = jsonObj.getInt("nodeId")
            var score = jsonObj.getDouble("score")
            this[nodeId] = score
        }
    }

    operator fun get(i: Int): Double? {
        return faultyNodes.get(i)
    }

    operator fun set(i: Int, value: Double) {
        faultyNodes.put(i, value)
    }

    fun mostLikelyFaulty(): List<Int> {
        //return faultyNodes.keys.first()
        val highestProb = faultyNodes.values.first()
        return faultyNodes.keys.takeWhile { key -> faultyNodes[key] == highestProb}
    }
}