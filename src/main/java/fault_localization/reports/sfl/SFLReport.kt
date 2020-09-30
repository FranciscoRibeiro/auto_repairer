package fault_localization.reports.sfl

import fault_localization.reports.FLReport
import java.io.File

class SFLReport: FLReport {
//    val diagnosis = SFLDiagnosis(File(path, "txt/ochiai.ranking.csv"))
    val diagnosis: SFLDiagnosis

    constructor(reportPath: String){
        diagnosis = SFLDiagnosis(File(reportPath))
    }

    /* This constructor is a workaround to prevent previous experiments from breaking. */
    constructor(reportPath: File){
        diagnosis = SFLDiagnosis(File(reportPath, "txt/ochiai.ranking.csv"))
    }

    override fun mostLikelyFaulty(upTo: Int): Sequence<Sequence<SFLComponent>> {
        return diagnosis.mostLikelyFaulty(upTo)
    }
}
