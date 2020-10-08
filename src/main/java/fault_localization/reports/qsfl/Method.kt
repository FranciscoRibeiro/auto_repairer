package fault_localization.reports.qsfl

class Method(override val name: String, override val parentId: Int): QSFLNode() {
    val methodName: String
    val params: List<String>

    init {
        val tokens = name.split("(")
        if (tokens.size == 2){
            methodName = tokens[0]
            params = tokens[1].removeSuffix(")").split(",")
        } else {
            methodName = ""
            params = emptyList()
        }
    }
}