import com.github.yohanki.egovconstant.data.ExcelDictionaryLoader
import com.github.yohanki.egovconstant.data.StdEntry
import com.github.yohanki.egovconstant.data.EntryType
import com.google.gson.GsonBuilder
import java.io.File

val xlsxFile = File("temp/공공데이터 공통표준용어(2023.11월)_수정.xlsx")
if (!xlsxFile.exists()) {
    println("XLSX file not found: ${xlsxFile.absolutePath}")
} else {
    val result = ExcelDictionaryLoader.load(xlsxFile)
    if (result.error != null) {
        println("Error loading XLSX: ${result.error}")
    } else {
        val entries = result.entries
        val terms = entries.filter { it.type == EntryType.TERM }
        val words = entries.filter { it.type == EntryType.WORD }
        val domains = entries.filter { it.type == EntryType.DOMAIN }

        fun saveToJson(entries: List<StdEntry>, path: String) {
            val file = File(path)
            file.parentFile.mkdirs()
            val gson = GsonBuilder().setPrettyPrinting().create()
            file.writeText(gson.toJson(entries))
        }

        saveToJson(terms, "src/main/resources/egovconstant/default/terms.json")
        saveToJson(words, "src/main/resources/egovconstant/default/words.json")
        saveToJson(domains, "src/main/resources/egovconstant/default/domains.json")

        println("Successfully converted XLSX to JSON.")
    }
}
