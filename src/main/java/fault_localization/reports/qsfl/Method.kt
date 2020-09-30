package fault_localization.reports.qsfl

class Method(signature: String, val parentId: Int): QSFLNode() {
    val name: String
    val params: List<String>

    init {
        val tokens = signature.split("(")
        if (tokens.size == 2){
            name = tokens[0]
            params = tokens[1].removeSuffix(")").split(",")
        } else {
            name = ""
            params = emptyList()
        }
    }
}