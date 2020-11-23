package fault_localization.reports.morpheus

import java.io.File

class MorpheusReport {
    val diagnosis: MorpheusDiagnosis

    constructor(reportPath: String){
        diagnosis = MorpheusDiagnosis(File(reportPath))
    }
}