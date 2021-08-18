package repair

import AlternativeProgram
import BuggyProgram
import fault_localization.FaultLocalizationType
import fault_localization.reports.morpheus.MorpheusComponent

class MorpheusLineRepair: MorpheusRepair() {

    override fun repair(program: BuggyProgram, basedOn: FaultLocalizationType): Sequence<AlternativeProgram> {
        val patches = program.mostLikelyFaulty(basedOn, 100)
                .flatten().filterIsInstance<MorpheusComponent>()
                .map { comp -> comp to program.findNodesInLine(comp) }
                .map { (comp, nodes) -> comp to revertMutation(program, comp, nodes) }

        /*val x = program.mostLikelyFaulty(basedOn, 15)
                .flatten().filterIsInstance<MorpheusComponent>().toList()*/

//        val y = x.map { comp -> comp to program.findNodesInLine(comp).toList() }.toList()

//        val patches = y.map { (comp, nodes) -> comp to nodes.flatMap { revertMutation(program, comp, nodes.asSequence()).toList() }.toList() }.toList()
//        val patches = y.map { (comp, nodes) -> comp to revertMutation(program, comp, nodes.asSequence()).toList() }.toList()
//        return emptySequence()
        return modifyComponent2(program, patches)
    }
}
