package repair

import AlternativeProgram
import BuggyProgram
import fault_localization.FaultLocalizationType
import fault_localization.reports.morpheus.MorpheusComponent

class MorpheusRelativeRepair: MorpheusRepair() {

    override fun repair(program: BuggyProgram, basedOn: FaultLocalizationType): Sequence<AlternativeProgram> {
        val patches = program.mostLikelyFaulty(basedOn, 1000)
                .flatten().filterIsInstance<MorpheusComponent>()
                .map { comp -> comp to program.findNodesInRelativeLine(comp) }
                .map { (comp, nodes) -> comp to revertMutation(program, comp, nodes) }

                /*.distinctBy { Pair(it.callable.fullSignature, it.mutOp) } // We only want to focus on the callables (methods and constructors) so we filter out multiple occurrences of the same mutOp in the same callable
                .map { comp -> comp to program.findNodesInCallable(comp) }
                .map { (comp, nodes) -> comp to revertMutation(program, comp, nodes) }*/
        return modifyComponent2(program, patches)
    }
}
