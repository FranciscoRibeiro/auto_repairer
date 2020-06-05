package fault_localization.reports

import java.io.File

class SFLReport(path: File) {
    val diagnosis = SFLDiagnosis(File(path, "txt/ochiai.ranking.csv"))

    fun mostLikelyFaulty(upTo: Int): Sequence<Sequence<Int>> {
        return diagnosis.mostLikelyFaulty(upTo)
    }
}
