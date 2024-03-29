//import com.google.gson.Gson
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

    private const val cachedFileColumnSeparator = "√"
    private const val lemmatizedDelimiter = "∫"

    val isEntireProgramInitialized = MutableStateFlow(false)
    val tableSizes: List<Double> by lazy {
        File(catalogDirectory, "sizes.txt").let {
            if (it.createNewFile()) { //if file not present, use default values and write file
                val list = listOf(20.0, 40.0, 1.3, 50.0, 1.5, 50.0)
                it.writeText(list.joinToString())
                list
            } else it.readText().split(",").map { it.toDouble() }
        }
    }

    fun initialize() {
        log("Initializing catalog.")
    }

    fun lastModificationDate(): String? {
        resetTime()
        return DateTimeFormatter
            .ofPattern("MM/dd/yyyy hh:mm a")
            .format(
                getDateLastModifiedFromFile(originalCatalogTSVFile)
            )
            .also {
                logTime("Time to calculate last modification date:")
            }
    }

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
//        val gson = Gson()
        if (::cachedCatalogFile.isInitialized) {
            log("Cached catalog file was initialized.")
            initCatalogFromCacheOrTSVIfStale()
        } else {
            log("No cache, reading from TSV")
            initCatalogFromTSV()
        }
        //for getting length stats
        //entries.map { it.publisher.length }.let { File("publisher.csv").writeText(it.joinToString(",")) }
        //entries.map { it.category.length }.let { File("category.csv").writeText(it.joinToString(",")) }
        //entries.map { it.author.length }.let { File("author.csv").writeText(it.joinToString(",")) }
        //entries.map { it.seferName.length }.let { File("seferName.csv").writeText(it.joinToString(",")) }
        if (checkCloud) {
            log("Checking cloud catalog for changes")
            scope.launch(Dispatchers.IO) {
                if (isHostAvailable("github.com")) {
                    try {
                        Runtime.getRuntime().exec("git pull", arrayOf(), catalogDirectory).waitFor()
                        log("Updated from cloud")
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                } else log("Internet unavailable")
                refreshObjects(false)
            }
        }
    }

    private fun initCatalogFromTSV() {
        log("Reading catalog TSV")
        val lines = Files.readAllLines(originalCatalogTSVFile.toPath()).toMutableList()
        log("Done reading catalog file.")
        log("Extracting entries from catalog file.")
        lines.removeAt(0) //remove line with column names
        initCatalog(lines, System.nanoTime())
        initLemmatizedCatalogAndWriteCache(lines, cachedFileColumnSeparator)
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
                //                log("Progress: $percentageDone%")
                if (
                    (!printedTwenty1 && percentageDone == 20).also { if (it) printedTwenty1 = true } ||
                    (!printedFourty1 && percentageDone == 40).also { if (it) printedFourty1 = true } ||
                    (!printedSixty1 && percentageDone == 60).also { if (it) printedSixty1 = true } ||
                    (!printedEight1 && percentageDone == 80).also { if (it) printedEight1 = true }
                ) log("Extracting entries $percentageDone% done")
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
            .also { log("Done extracting entries.") }
            //            .filter { it != null }
            //            .collect(Collectors.toList())
            .toMutableList()
            .let {
                logTime("Time to extract entries from file:", startTime)
                it + listOfEnglishSeforim
            }
    }

    private fun initLemmatizedCatalogAndWriteCache(
        lines: MutableList<String>,
        cachedFileColumnSeparator: String,
    ) {
        scope.launch(Dispatchers.Default) {
            log("Extracting shorashim.")
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
                    ) log("Extracting shorashim $percentageDone% done")
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
            log("Done extracting shorashim.")
            logTime("Time to extract shorashim:", lemmaStartTime)
            log("Beginning to draw main screen.")
            writeCacheFiles(cachedFileColumnSeparator)
        }
    }

    private fun writeCacheFiles(cachedFileColumnSeparator: String) {
        scope.launch(Dispatchers.IO) {
            cachedCatalogFile = File(catalogDirectory, cachedCatalogFileName)
            cachedLemmatizedCatalogFile = File(catalogDirectory, cachedLemmatizedCatalogFileName)
            // Sorting the lists before righting might make future searches faster, so why not.
            // Don't want to sort while program is running so as not to impact startup time.
            cachedCatalogFile.writeText(
                entries
                    .sortedBy { it.seferName }
                    .joinToString("\n") {
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
                        entriesLemmatized
                            .sortedBy { it.seferName }
                            .joinToString("\n") {
                            listOf(
                                it._seferName.serializeLemmatizedSet(),
                                it._author.serializeLemmatizedSet(),
                                it._publisher,
                                it._volumeNum,
                                it._category,
                                it._shelfNum
                            ).joinToString(cachedFileColumnSeparator)
                        }
            )
//            testIfSerializedCorrectly()
            val osname = System.getProperty("os.name")
            log("OS name: $osname")
            if (osname.startsWith("win", true)) {
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
    ) {
        log("Reading catalog from cache.")
        val linesOfLemmatizedCatalog = Files.readAllLines(cachedLemmatizedCatalogFile.toPath()).toMutableList()
        val dateModifed = linesOfLemmatizedCatalog.removeFirst()
        if(dateModifed.toLong() != originalCatalogTSVFile.lastModified()) { //if the cache was based on an older version of the catalog than the .tsv present in catalogDirectory, renew the cache
            log("Cache is stale, refreshing cache.")
            initCatalogFromTSV()
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
                    split.next().split(lemmatizedDelimiter).convertToLemmatizedSet(),
                    split.next().split(lemmatizedDelimiter).convertToLemmatizedSet(),
                    split.next(),
                    split.next(),
                    split.next(),
                    split.next(),
                )
            }
            log("Done reading catalog from cache")
        }
    }

    private fun Pair<String, Set<Set<String>>>.serializeLemmatizedSet() =
        "$first$lemmatizedDelimiter${second.joinToString("π") { it.joinToString("Ω") }}"

    private fun List<String>.convertToLemmatizedSet() =
        this[0] to this[1].let {
            if(it.isBlank()) setOf()
            else it
                .splitToSequence("π")
                .mapTo(mutableSetOf()) {
                    it
                        .splitToSequence("Ω")
                        .toSet()
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
    private fun testIfSerializedCorrectly() {
        val entriesCopy = entries.sortedBy { it.seferName }
        val lemmasCopy = entriesLemmatized.sortedBy { it._seferName.first }
        initCatalogFromCacheOrTSVIfStale()
        val entriesFromCache = entries.sortedBy { it.seferName }
        val lemmasFromCache = entriesLemmatized.sortedBy { it._seferName.first }
        var catalogMatches = true
        var lemmaMatches = true
        log("Testing catalog")
        for ((i, j) in entriesCopy.zip(entriesFromCache)) if (i != j) {
            log("Not equal")
            log(i)
            log(j)
            catalogMatches = false
        }
        log("Testing lemmas")
        for ((i, j) in lemmasCopy.zip(lemmasFromCache)) if (i != j) {
            log("Not equal")
            log(i)
            log(j)
            lemmaMatches = false
        }
        log("Serialized correctly: $catalogMatches && $lemmaMatches")
        if (!catalogMatches) {
            File("entries.txt").writeText(entriesCopy.toString())
            File("entries_cache.txt").writeText(entriesFromCache.toString())
            File("lemmas.txt").writeText(lemmasCopy.toString())
            File("lemmas_cache.txt").writeText(lemmasFromCache.toString())
        }
    }
}
