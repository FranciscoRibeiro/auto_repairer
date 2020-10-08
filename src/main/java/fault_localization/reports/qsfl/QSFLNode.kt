package fault_localization.reports.qsfl

import fault_localization.reports.FLComponent

abstract class QSFLNode: FLComponent {
    abstract val parentId: Int
    abstract val name: String
}
