import java.io.File
import java.nio.file.Files
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

object Catalog {
    lateinit var entries: List<CatalogEntry>
    val file: File
    fun initialize(){}
    fun lastModificationDate() =
        DateTimeFormatter
            .ofPattern("MM/dd/yyyy hh:mm a")
            .format(
                LocalDateTime.ofInstant(Instant
                    .ofEpochMilli(
                        file
                            .lastModified()
                    ),
                    ZoneId.systemDefault()
                )
            )
    init {
        file = File(System.getProperty("user.dir")).walk().find { it.extension == "tsv" }!!
        refreshObjects()
    }

    fun refreshObjects() {
        val lines = Files.readAllLines(file.toPath()).toMutableList()
        lines.removeAt(0) //remove line with column names
        val listOfEnglishSeforim = mutableListOf<CatalogEntry>()
        entries = lines.mapNotNull {
            val split = it.split("\t")
            CatalogEntry(
                split[0],
                split[1],
                split[2],
                split[3],
                split[4],
                split[5],
                split[6],
                split[7],
                split[8],
                split[9],
                split[10],
                split[11],
                split[12]
            ).let {
                when {
                    it.everythingIsBlank() -> null
                    it.seferName.containsEnglish() -> {
                        listOfEnglishSeforim.add(it)
                        null
                    }
                    else -> it
                }
            }
        }.let {
            println("Num english with hebrew: ${(it.filter { containsHebrewAndEnglish(it) } + listOfEnglishSeforim.filter { containsHebrewAndEnglish(it) }).map { it.seferName }}")
            println()
            println()
            println()
            it + listOfEnglishSeforim
        }
    }

    private fun containsHebrewAndEnglish(it: CatalogEntry) =
        it.seferName.containsEnglish() && it.seferName.containsHebrew()

    var _pattern: Pattern? = null

    var hebrewPattern: Regex? = null
    var englishPattern: Regex? = null
    fun String.containsHebrew(): Boolean {
      return contains(hebrewPattern ?: "\\p{InHebrew}".toRegex().also { hebrewPattern = it })
    }
    fun String.containsEnglish(): Boolean {
      return contains(englishPattern ?: "\\p{Alpha}".toRegex().also { englishPattern = it })
    }
//    fun containsEnglish(string: String): Boolean {
//        return (_pattern ?: Pattern.compile("[a-zA-Z]").also { _pattern = it }).matcher(string).find()
//    }
}
