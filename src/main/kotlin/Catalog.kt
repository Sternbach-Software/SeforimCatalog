import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import lemmatizer.hebmorph.tests.Lemmatizer
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.file.Files
import java.nio.file.LinkOption
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern
import kotlin.math.roundToInt


var catalogDirectory = File(System.getProperty("user.dir"))
val lemmatizer = Lemmatizer()
const val cachedCatalogFileName = ".cached_catalog.txt"
const val cachedLemmatizedCatalogFileName = ".cached_lemmatized_catalog.txt"

object Catalog {
    lateinit var originalCatalogTSVFile: File
    lateinit var cachedCatalogFile: File //if already computed
    lateinit var cachedLemmatizedCatalogFile: File //if already computed
    lateinit var entries: List<CatalogEntry>
    lateinit var entriesLemmatized: List<LemmatizedCatalogEntry>

    private const val cachedFileColumnSeparator = "~@~"
    private const val lemmatizedDelimiter = "~!~"

    val isLemmatized = MutableStateFlow(false)
    val tableSizes: List<Double>
        get() = File(catalogDirectory, "sizes.txt").let {
            if (it.createNewFile()) { //if file not present, use default values and write file
                val list = listOf(20.0, 40.0, 1.3, 50.0, 1.5, 50.0)
                it.writeText(list.joinToString())
                list
            } else it.readText().split(",").map { it.toDouble() }
        }

    fun initialize() {
        println("Initializing catalog.")
    }

    fun lastModificationDate() =
        DateTimeFormatter
            .ofPattern("MM/dd/yyyy hh:mm a")
            .format(
                getDateLastModifiedFromFile(originalCatalogTSVFile)
            )

    fun getDateLastModifiedFromFile(file: File) = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(file.lastModified()),
        ZoneId.systemDefault()
    )

    init {
        catalogDirectory.walk().forEach {
            when {
                it.extension == "tsv" -> originalCatalogTSVFile = it
                it.name == cachedCatalogFileName -> cachedCatalogFile = it
                it.name == cachedLemmatizedCatalogFileName -> cachedLemmatizedCatalogFile = it
            }
        }
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
        val gson = Gson()
        if (::cachedCatalogFile.isInitialized) {
            initCatalogFromCacheOrTSVIfStale(gson)
            scope.launch { isLemmatized.emit(true) }
        } else {
            initCatalogFromTSV(gson)
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

    private fun initCatalogFromTSV(gson: Gson) {
        println("Reading catalog TSV")
        val lines = Files.readAllLines(originalCatalogTSVFile.toPath()).toMutableList()
        println("Done reading catalog file.")
        println("Extracting entries from catalog file.")
        lines.removeAt(0) //remove line with column names
        initCatalog(lines, System.nanoTime())
        initLemmatizedCatalogAndWriteCache(lines, cachedFileColumnSeparator, gson)
    }

    private fun initCatalog(
        lines: MutableList<String>,
        startTime: Long
    ) {
        var counter1 = 0
        var printedTwenty1 = false
        var printedFourty1 = false
        var printedSixty1 = false
        var printedEight1 = false
        val listOfEnglishSeforim = mutableListOf<CatalogEntry>()
        val shelfPattern = Pattern.compile("\\w?\\d+\\.\\d+")
        entries = lines
            //            .parallelStream()
            .mapNotNull {
                val percentageDone = (counter1++.toDouble() / lines.size * 100).roundToInt()
                //                println("Progress: $percentageDone%")
                if (
                    (!printedTwenty1 && percentageDone == 20).also { if (it) printedTwenty1 = true } ||
                    (!printedFourty1 && percentageDone == 40).also { if (it) printedFourty1 = true } ||
                    (!printedSixty1 && percentageDone == 60).also { if (it) printedSixty1 = true } ||
                    (!printedEight1 && percentageDone == 80).also { if (it) printedEight1 = true }
                ) println("Extracting entries $percentageDone% done")
                val split = it.split("\t")
                val shelf = split[12]
                val shelfFirstChar = shelf.firstOrNull()
                if (shelfFirstChar == null || shelfFirstChar.isWhitespace() || !shelfPattern.matcher(shelf)
                        .matches()
                ) null //invalid shelf number format
                else CatalogEntry(
                    numberNotSure = split[0],
                    miyunNum = split[1],
                    seferName = split[2].replace('״', '\"'),
                    author = split[3].replace('״', '\"'),
                    publisher = split[4].replace('״', '\"'),
                    volumeNum = split[5],
                    numCopies = split[6],
                    dateAdded = split[7],
                    initials = split[8],
                    needsBinding = split[9],
                    comments = split[10],
                    category = split[11].replace('״', '\"'),
                    shelfNum = shelf
                ).let {
                    if (shelfFirstChar!!.isLetter() && !shelfFirstChar.equals('B', true)) catalogOnlyContainsB =
                        false //if the first char is a letter and does not equal B, the catalog does not only contain B
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
            .also { println("Done extracting entries.") }
            //            .filter { it != null }
            //            .collect(Collectors.toList())
            .toMutableList()
            .let {
                println("Time to extract entries from file: ${(System.nanoTime() - startTime).div(1_000_000_000.00)} seconds")
                it + listOfEnglishSeforim
            }
    }

    private fun initLemmatizedCatalogAndWriteCache(
        lines: MutableList<String>,
        cachedFileColumnSeparator: String,
        gson: Gson
    ) {
        scope.launch(Dispatchers.Default) {
            println("Extracting shorashim.")
            val atomicCounter = AtomicInteger()
            var printedTwenty1 = false
            var printedFourty1 = false
            var printedSixty1 = false
            var printedEight1 = false
            val lemmaStartTime = System.nanoTime()
            val lemmatizedEntries = mutableListOf<LemmatizedCatalogEntry>()
            val synchronizedList = Collections.synchronizedList(lemmatizedEntries)
            entries
                .parallelStream()
                .map {
                    val percentageDone =
                        (atomicCounter.incrementAndGet().toDouble() / lines.size * 100).roundToInt()
                    if (
                        (!printedTwenty1 && percentageDone == 20).also { if (it) printedTwenty1 = true } ||
                        (!printedFourty1 && percentageDone == 40).also { if (it) printedFourty1 = true } ||
                        (!printedSixty1 && percentageDone == 60).also { if (it) printedSixty1 = true } ||
                        (!printedEight1 && percentageDone == 80).also { if (it) printedEight1 = true }
                    ) println("Extracting shorashim $percentageDone% done")
                    LemmatizedCatalogEntry(
                        it.seferName to lemmatizer.getLemmatizedList(it.seferName),
                        it.author to lemmatizer.getLemmatizedList(it.author),
                        it.publisher,
                        it.volumeNum,
                        it.category,
                        it.shelfNum
                    )
                }
                .forEach { synchronizedList.add(it) }
            entriesLemmatized = lemmatizedEntries
            isLemmatized.emit(true)
            println("Done extracting shorashim.")
            println("Time to extract shorashim: ${(System.nanoTime() - lemmaStartTime).div(1_000_000_000.00)} seconds")
            println("Beginning to draw main screen.")
            writeCacheFiles(cachedFileColumnSeparator, gson)
        }
    }

    private fun writeCacheFiles(cachedFileColumnSeparator: String, gson: Gson) {
        scope.launch(Dispatchers.IO) {
            cachedCatalogFile = File(catalogDirectory, cachedCatalogFileName)
            cachedLemmatizedCatalogFile = File(catalogDirectory, cachedLemmatizedCatalogFileName)
            cachedCatalogFile.writeText(
                entries.joinToString("\n") {
                    listOf(
                        it.numberNotSure,
                        it.miyunNum,
                        it.seferName,
                        it.author,
                        it.publisher,
                        it.volumeNum,
                        it.numCopies,
                        it.dateAdded,
                        it.initials,
                        it.needsBinding,
                        it.comments,
                        it.category,
                        it.shelfNum
                    ).joinToString(cachedFileColumnSeparator)
                }
            )
            cachedLemmatizedCatalogFile.writeText(
                "${originalCatalogTSVFile.lastModified()}\n" +
                        entriesLemmatized.joinToString("\n") {
                            listOf(
                                it._seferName.serializeLemmatizedSet(gson),
                                it._author.serializeLemmatizedSet(gson),
                                it._publisher,
                                it._volumeNum,
                                it._category,
                                it._shelfNum
                            ).joinToString(cachedFileColumnSeparator)
                        }
            )
//            testIfSerializedCorrectly(gson)
            if (System.getProperty("os.name").startsWith("win", true)) {
                Files.setAttribute(
                    cachedCatalogFile.toPath(),
                    "dos:hidden",
                    true,
                    LinkOption.NOFOLLOW_LINKS
                );
                Files.setAttribute(cachedLemmatizedCatalogFile.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
            }
        }
    }

    private fun initCatalogFromCacheOrTSVIfStale(
        gson: Gson
    ) {
        println("Reading catalog from cache.")
        val linesOfLemmatizedCatalog = Files.readAllLines(cachedLemmatizedCatalogFile.toPath()).toMutableList()
        val dateModifed = linesOfLemmatizedCatalog.removeFirst()
        if(dateModifed.toLong() != originalCatalogTSVFile.lastModified()) { //if the cache was based on an older version of the catalog than the .tsv present in catalogDirectory, renew the cache
            println("Cache is stale, refreshing cache.")
            initCatalogFromTSV(gson)
        } else {
            entries = Files.readAllLines(cachedCatalogFile.toPath()).mapTo(mutableListOf()) {
                val split = it.split(cachedFileColumnSeparator).iterator()
                CatalogEntry(
                    split.next(),
                    split.next(),
                    split.next(),
                    split.next(),
                    split.next(),
                    split.next(),
                    split.next(),
                    split.next(),
                    split.next(),
                    split.next(),
                    split.next(),
                    split.next(),
                    split.next()
                )
            }
            entriesLemmatized = linesOfLemmatizedCatalog.mapTo(mutableListOf()) {
                val split = it.split(cachedFileColumnSeparator).iterator()
                LemmatizedCatalogEntry(
                    split.next().split(lemmatizedDelimiter).convertToLemmatizedSet(gson),
                    split.next().split(lemmatizedDelimiter).convertToLemmatizedSet(gson),
                    split.next(),
                    split.next(),
                    split.next(),
                    split.next(),
                )
            }
            println("Done reading catalog from cache")
        }
    }

    private fun Pair<String, Set<Set<String>>>.serializeLemmatizedSet(gson: Gson) =
        "$first$lemmatizedDelimiter${gson.toJson(second)}"

    private fun List<String>.convertToLemmatizedSet(gson: Gson) =
        this[0] to gson.fromJson(this[1], Array<Array<String>>::class.java).mapTo(
            mutableSetOf()
        ) { it.toMutableSet() }

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
    /*private fun testIfSerializedCorrectly(gson: Gson) {
        val entriesCopy = entries.sortedBy { it.seferName }
        val lemmasCopy = entriesLemmatized.sortedBy { it._seferName.first }
        initCatalogFromCacheOrTSVIfStale(gson)
        val entriesFromCache = entries.sortedBy { it.seferName }
        val lemmasFromCache = entriesLemmatized.sortedBy { it._seferName.first }
        var catalogMatches = true
        var lemmaMatches = true
        println("Testing catalog")
        for ((i, j) in entriesCopy.zip(entriesFromCache)) if (i != j) {
            println("Not equal")
            println(i)
            println(j)
            catalogMatches = false
        }
        println("Testing lemmas")
        for ((i, j) in lemmasCopy.zip(lemmasFromCache)) if (i != j) {
            println("Not equal")
            println(i)
            println(j)
            lemmaMatches = false
        }
        println("Serialized correctly: $catalogMatches && $lemmaMatches")
        if (!catalogMatches) {
            File("entries.txt").writeText(entriesCopy.toString())
            File("entries_cache.txt").writeText(entriesFromCache.toString())
            File("lemmas.txt").writeText(lemmasCopy.toString())
            File("lemmas_cache.txt").writeText(lemmasFromCache.toString())
        }
    }*/
}
