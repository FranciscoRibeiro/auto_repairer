package fault_localization.reports.morpheus

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import java.io.File

class MorpheusDiagnosis {
    var inferences: List<MorpheusComponent>

    constructor(diagnosisFile: File) {
        this.inferences = parseMorpheus(diagnosisFile)
    }

    private fun parseMorpheus(diagnosisFile: File): List<MorpheusComponent> {
        val csvParser = CSVParserBuilder().withSeparator(';').build()
        val csvRecords = CSVReaderBuilder(diagnosisFile.bufferedReader())
//                .withSkipLines(1)
                .withCSVParser(csvParser)
                .build().readAll()
        return csvRecords.flatMap { record -> buildMorpheusComponents(record) }
    }

    private fun buildMorpheusComponents(record: Array<String>): List<MorpheusComponent> {
        val packageName = extractPackageName(record[2])
        val className = extractClassName(record[2])
        val mutOps = fieldToList(record[7])
        val callables = fieldToList(record[8], "null#null,", "),").map { closeSignature(it) }
        val startEndLines = fieldToList(record[11])
        val startEndColumns = fieldToList(record[12])
        val relativeOldStartEndLines = fieldToList(record[13])
        val relativeNewStartEndLines = fieldToList(record[14])
        if (mutOps.size != startEndLines.size || startEndLines.size != startEndColumns.size) {
            return emptyList()
        } else {
            val components = mutableListOf<MorpheusComponent>()
            for (i in mutOps.indices){
                components.add(MorpheusComponent(packageName,
                        className,
                        mutOps[i],
                        Callable(callables[i]),
                        rangeToPair(startEndLines[i]),
                        rangeToPair(startEndColumns[i]),
                        rangeToPair(relativeOldStartEndLines[i]),
                        rangeToPair(relativeNewStartEndLines[i])))
            }
            return components
        }
    }

    private fun closeSignature(signature: String): String {
        return if(signature == "null#null") ""
        else if (signature.isNotEmpty() && signature.last() != ')') "$signature)"
        else signature
    }

    private fun extractPackageName(fullName: String): String {
        return fullName.split("$").first()
    }

    private fun extractClassName(fullName: String): String {
        return fullName.split("$").last()
    }

    private fun rangeToPair(range: String): Pair<Int, Int> {
        val startEnd = range.split("-")
        return if (startEnd.size == 2) Pair(startEnd[0].toInt(), startEnd[1].toInt()) else Pair(0,0)
    }

    private fun fieldToList(field: String, vararg seps: String = arrayOf(",")): List<String> {
        return field.drop(1).dropLast(1) //ignore open/close square brackets
                .split(*seps).map { it.trim() }
    }

    fun mostLikelyFaulty(upTo: Int): Sequence<MorpheusComponent> {
        return inferences.take(upTo).asSequence()
    }
}

fun main(args: Array<String>) {
    val diag = MorpheusDiagnosis(File(args[0]))
    println("end")
}