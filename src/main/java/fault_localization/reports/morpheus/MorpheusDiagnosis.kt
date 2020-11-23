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
                .withSkipLines(1)
                .withCSVParser(csvParser)
                .build().readAll()
        return csvRecords.flatMap { record -> buildMorpheusComponents(record) }
    }

    private fun buildMorpheusComponents(record: Array<String>): List<MorpheusComponent> {
        val mutOps = fieldToList(record[5])
        val startEndLines = fieldToList(record[6])
        val startEndColumns = fieldToList(record[7])
        if (mutOps.size != startEndLines.size || startEndLines.size != startEndColumns.size) {
            return emptyList()
        } else {
            val components = mutableListOf<MorpheusComponent>()
            for (i in mutOps.indices){
                components.add(MorpheusComponent(mutOps[i],
                        rangeToPair(startEndLines[i]),
                        rangeToPair(startEndColumns[i])))
            }
            return components
        }
    }

    private fun rangeToPair(range: String): Pair<Int, Int> {
        val (start, end) = range.split("-")
        return Pair(start.toInt(), end.toInt())
    }

    private fun fieldToList(field: String): List<String> {
        return field.drop(1).dropLast(1) //ignore open/close square brackets
                .split(",").map { it.trim() }
    }
}

fun main(args: Array<String>) {
    val diag = MorpheusDiagnosis(File(args[0]))
    println("end")
}