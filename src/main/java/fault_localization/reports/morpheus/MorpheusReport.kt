package fault_localization.reports.morpheus

import BuggyProgram
import fault_localization.reports.FLComponent
import fault_localization.reports.FLReport
import java.io.File

class MorpheusReport: FLReport {
    val diagnosis: MorpheusDiagnosis

    constructor(reportPath: String){
        diagnosis = MorpheusDiagnosis(File(reportPath))
    }

    /* Outer sequence is always size 1 because all components have the same probability */
    override fun mostLikelyFaulty(upTo: Int): Sequence<Sequence<FLComponent>> {
        return sequenceOf(diagnosis.mostLikelyFaulty(upTo))
    }
}

fun main(args: Array<String>) {
    val mr = MorpheusReport(args[1])
    val bp = BuggyProgram(args[0], mr)
    println("end")
}
