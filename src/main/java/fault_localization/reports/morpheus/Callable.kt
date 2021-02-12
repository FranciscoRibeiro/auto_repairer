package fault_localization.reports.morpheus

class Callable(val fullSignature: String) {
    val className = extractClassName(fullSignature)
    val callableName = extractCallableName(fullSignature)
    val parameters = extractParameters(fullSignature)

    private fun splitNameAndParams(fullSignature: String): Pair<String, String> {
        val split = fullSignature.split("(")
        return Pair(split[0], "(" + split[1])
    }

    private fun extractCallableName(fullSignature: String): String {
        return if(fullSignature.isEmpty()) ""
        else splitNameAndParams(fullSignature).first.split("#")[1]
    }

    private fun extractClassName(fullSignature: String): String {
        return if(fullSignature.isEmpty()) ""
        else splitNameAndParams(fullSignature).first.split("#")[0]
    }

    private fun extractParameters(fullSignature: String): List<String> {
        return if (fullSignature.isEmpty()) emptyList()
        else {
            val params = splitNameAndParams(fullSignature).second
            return params.drop(1).dropLast(1) // remove parenthesis
                    .split(",") // split parameters
        }
    }

    fun simpleParameterTypes(): List<String> {
        return parameters.map { it.split(".").last() }
    }
}