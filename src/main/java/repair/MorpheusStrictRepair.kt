package repair

import AlternativeProgram
import BuggyProgram
import fault_localization.FaultLocalizationType
import fault_localization.reports.morpheus.MorpheusComponent

class MorpheusStrictRepair: MorpheusRepair() {

    override fun repair(program: BuggyProgram, basedOn: FaultLocalizationType): Sequence<AlternativeProgram> {
        val patches = program.mostLikelyFaulty(basedOn, 100)
                .flatten().filterIsInstance<MorpheusComponent>()
                .map { comp -> comp to program.findNodes(comp) }
                .map { (comp, nodes) -> comp to revertMutation(program, comp, nodes) }

        return modifyComponent2(program, patches)
    }
}
