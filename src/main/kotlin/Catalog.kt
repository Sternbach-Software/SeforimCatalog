import java.io.File
import java.net.InetSocketAddress
import java.net.Socket
import java.net.UnknownHostException
import java.nio.file.Files
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern


var catalogDirectory = File(System.getProperty("user.dir"))
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
        file = catalogDirectory.walk().find { it.extension == "tsv" }!!
        refreshObjects()
    }


    private fun isHostAvailable(hostName: String): Boolean {
        try {
            Socket().use { socket ->
                val port = 80
                val socketAddress = InetSocketAddress(hostName, port)
                socket.connect(socketAddress, 3000)
                return true
            }
        } catch (unknownHost: UnknownHostException) {
            return false
        }
    }
    fun refreshObjects() {
        if(isHostAvailable("github.com")) {
            Runtime.getRuntime().exec("cd $catalogDirectory && git pull").waitFor()
        }
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
