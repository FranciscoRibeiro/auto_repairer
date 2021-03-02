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
        val secondPart = fullSignature.split("#")[1]
        return if(secondPart == "null") ""
        else secondPart.split("(")[0]
    }

    private fun extractClassName(fullSignature: String): String {
        val firstPart = fullSignature.split("#")[0]
        return if(firstPart == "null") ""
        else firstPart
    }

    private fun extractParameters(fullSignature: String): List<String> {
        val fullNameAndParams = fullSignature.split("(")
        return if(fullNameAndParams.size == 2) {
            fullNameAndParams[1].dropLast(1) //remove last parenthesis
                    .split(",")
        } else emptyList()
    }

    fun simpleParameterTypes(): List<String> {
        return parameters.map { it.split(".").last() }
    }

    fun simpleCallableName(): String {
        return if(callableName.contains(".")) callableName.split(".").last()
        else callableName
    }
}