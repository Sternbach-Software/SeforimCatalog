import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lemmatizer.hebmorph.tests.LemmatizerTest
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.file.Files
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern
import kotlin.math.roundToInt


var catalogDirectory = File(System.getProperty("user.dir"))

object Catalog {
    lateinit var entries: List<CatalogEntry>
    lateinit var entriesLemmatized: List<LemmatizedCatalogEntry>
    val file: File
    val tableSizes: List<Double>
        get() = File(catalogDirectory, "sizes.txt").also { it.createNewFile() }.readText().split(",")
            .map { it.toDouble() }

    fun initialize() {
        println("Initializing catalog.")
    }
    fun lastModificationDate() =
        DateTimeFormatter
            .ofPattern("MM/dd/yyyy hh:mm a")
            .format(
                LocalDateTime.ofInstant(
                    Instant
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
        } catch (t: Throwable) {
            return false
        }
    }

    fun refreshObjects(checkCloud: Boolean = false) {
        println("Reading catalog file.")
        val lines = Files.readAllLines(file.toPath()).toMutableList()
        println("Done.")
        println("Processing catalog file.")
        lines.removeAt(0) //remove line with column names
        val listOfEnglishSeforim = mutableListOf<CatalogEntry>()
        val startTime = System.nanoTime()
        var counter = 0
        var printedTwenty = false
        var printedFourty = false
        var printedSixty = false
        var printedEight = false
        entries = lines
//            .parallelStream()
            .mapNotNull {
                val percentageDone = (counter++.toDouble() / lines.size * 100).roundToInt()
//                println("Progress: $percentageDone%")
                if (
                    (!printedTwenty && percentageDone == 20).also { if (it) printedTwenty = true } ||
                    (!printedFourty && percentageDone == 40).also { if (it) printedFourty = true } ||
                    (!printedSixty && percentageDone == 60).also { if (it) printedSixty = true } ||
                    (!printedEight && percentageDone == 80).also { if (it) printedEight = true }
                ) println("Progress: $percentageDone%")
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
            }
            .also { println("Done.") }
//            .filter { it != null }
//            .collect(Collectors.toList())
            .toMutableList()
            .let {
                println("Time to extract entries from file: ${(System.nanoTime() - startTime).div(1_000_000_000.00)} seconds")
                it + listOfEnglishSeforim
            }
        scope.launch(Dispatchers.Default) {
            println("Extracting shorashim.")
            val atomicCounter = AtomicInteger()
            printedTwenty = false
            printedFourty = false
            printedSixty = false
            printedEight = false
            val lemmaStartTime = System.nanoTime()
            val lemmatizedEntries = mutableListOf<LemmatizedCatalogEntry>()
            val syncrhonizedList = Collections.synchronizedList(lemmatizedEntries)
            entries
                .parallelStream()
                .map {
                    val percentageDone = (atomicCounter.incrementAndGet().toDouble() / lines.size * 100).roundToInt()
                    if (
                        (!printedTwenty && percentageDone == 20).also { if (it) printedTwenty = true } ||
                        (!printedFourty && percentageDone == 40).also { if (it) printedFourty = true } ||
                        (!printedSixty && percentageDone == 60).also { if (it) printedSixty = true } ||
                        (!printedEight && percentageDone == 80).also { if (it) printedEight = true }
                    ) println("Progress: $percentageDone%")
                    LemmatizedCatalogEntry(
                        it.seferName to LemmatizerTest.getLemmatizedList(it.seferName, true, true, true, false, true),
                        it.author to LemmatizerTest.getLemmatizedList(it.author, true, false, true, false, true),
                        it.publisher,
                        it.volumeNum,
                        it.category,
                        it.shelfNum
                    )
                }
                .forEach { syncrhonizedList.add(it) }
            entriesLemmatized = lemmatizedEntries
            println("Done.")
            println("Time to extract shorashim: ${(System.nanoTime() - lemmaStartTime).div(1_000_000_000.00)} seconds")
        }
        //for getting length stats
        //entries.map { it.publisher.length }.let { File("publisher.csv").writeText(it.joinToString(",")) }
        //entries.map { it.category.length }.let { File("category.csv").writeText(it.joinToString(",")) }
        //entries.map { it.author.length }.let { File("author.csv").writeText(it.joinToString(",")) }
        //entries.map { it.seferName.length }.let { File("seferName.csv").writeText(it.joinToString(",")) }
        if (checkCloud) {
            println("Checking cloud catalog for changes")
            scope.launch(Dispatchers.IO) {
                if (isHostAvailable("github.com")) {
                    try {
                        Runtime.getRuntime().exec("git pull", arrayOf(), catalogDirectory).waitFor()
                        println("Updated from cloud")
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                } else println("Internet unavailable")
                refreshObjects(false)
            }
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
