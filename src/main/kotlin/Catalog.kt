import java.io.File
import java.nio.file.Files
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
                    ).also {
                        println("Instant: $it")
                    },
                    ZoneId.systemDefault()
                )
            )
    init {
        file = File("C:\\Users\\shmue\\Downloads\\Shmuel Sternbach's Copy of Catalog  - Catalog.tsv")
        refreshObjects()
    }

    fun refreshObjects() {
        val lines = Files.readAllLines(file.toPath()).toMutableList()
        println("line removed: ${lines.removeAt(0)}")
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
                if(it.seferName.isBlank() && it.everythingExceptNameIsBlank()) null else
                    if(containsEnglish(it.seferName)) {
                        listOfEnglishSeforim.add(it)
                        null
                    } else it
            }
        }.let {
            it + listOfEnglishSeforim
        }
    }
    var _regex: Regex? = null

    fun containsEnglish(string: String): Boolean {
        return string.contains(_regex ?: "[a-zA-Z]".toRegex().also { _regex = it })
    }
}
