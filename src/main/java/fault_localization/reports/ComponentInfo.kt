package fault_localization.reports

class ComponentInfo(val className: String, val methodSignature: String, val probability: Double){
    override fun toString(): String {
        return "ComponentInfo(className='$className', methodSignature='$methodSignature', probability=$probability)"
    }
}
