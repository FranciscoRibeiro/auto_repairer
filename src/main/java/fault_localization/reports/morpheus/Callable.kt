package fault_localization.reports.morpheus

class Callable(val fullSignature: String) {
    val name = extractName(fullSignature)
    val parameters = extractParameters(fullSignature)

    private fun extractName(fullSignature: String): String {
        return fullSignature.split("(").first()
    }

    private fun extractParameters(fullSignature: String): List<String> {
        return if (fullSignature.isEmpty()) emptyList()
        else {
            val name = extractName(fullSignature)
            return fullSignature.removePrefix(name) // isolate parameters -> (param1, param2, ...)
                    .drop(1).dropLast(1) // remove parenthesStringis
                    .split(",") // split parameters
        }
    }

    fun simpleParameterTypes(): List<String> {
        return parameters.map { it.split(".").last() }
    }
}