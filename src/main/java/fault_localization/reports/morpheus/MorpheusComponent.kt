package fault_localization.reports.morpheus

import fault_localization.reports.FLComponent

class MorpheusComponent(val mutOp: String,
                        val startEndLines: Pair<Int, Int>,
                        val startEndColumns: Pair<Int, Int>): FLComponent {

}
