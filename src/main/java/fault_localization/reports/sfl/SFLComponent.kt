package fault_localization.reports.sfl

import fault_localization.reports.FLComponent

class SFLComponent(val packageName: String, val simpleClassName: String, val methodSignature: String, val line: Int): FLComponent {
    override fun toString(): String {
        return "SFLComponent(packageName='$packageName', simpleClassName='$simpleClassName', methodSignature='$methodSignature', line=$line)"
    }
}
