import Catalog.containsEnglish
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.apache.commons.text.similarity.LevenshteinDistance
import java.awt.*
import java.awt.event.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.*
import javax.swing.border.TitledBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableRowSorter


private const val FILTER_EXACT = 0
private const val FILTER_ROOT = 1
private const val FILTER_INSENSITIVE = 2
private const val FILTER_ALTERNATE_PHRASE = 3
private const val FILTER_SIMILARITY = 4
val letterBoundariesRegex = "(?<=\\p{InHebrew})(?=\\p{InHebrew})".toRegex()
val yudOrVavInWordRegex = "(?<=\\p{InHebrew})[וי](?=\\p{InHebrew})".toRegex()
var catalogOnlyContainsB = true
const val defaultMatchWordBoundarySetting = false
var _previousMatchWordBoundary = mutableMapOf(
    FILTER_EXACT to defaultMatchWordBoundarySetting,
    FILTER_ROOT to defaultMatchWordBoundarySetting,
    FILTER_INSENSITIVE to defaultMatchWordBoundarySetting,
    FILTER_ALTERNATE_PHRASE to defaultMatchWordBoundarySetting,
    FILTER_SIMILARITY to defaultMatchWordBoundarySetting,
) //used to know whether to update list if constraint hasn't changed
val shelfNumComparator = kotlin.Comparator<String> { o1, o2 ->
    fun compareShelf(o1: String, o2: String, hasLetter: Boolean): Int {

        val indexOfDot1 = o1.indexOf(".")
        val firstNum1 = o1.substring(if (hasLetter) 1/*exclude letter*/ else 0, indexOfDot1)
        val secondNum1 = o1.substring(indexOfDot1 + 1)

        val indexOfDot2 = o2.indexOf(".")
        val firstNum2 = o2.substring(if (hasLetter) 1 else 0, indexOfDot2)
        val secondNum2 = o2.substring(indexOfDot2 + 1)
        //val firstNumsCompared = firstNum1.toInt().compareTo(firstNum2.toInt())
//        return if(firstNumsCompared == 0) secondNum1.toInt().compareTo(secondNum2.toInt()) else firstNumsCompared //this seems to be slower in my quick and dirty benchmarks on the yeshiva laptop (couldn't install JMH)

        // First check size of strings: if one number has more digits than the other,
        // then it is certainly greater. Otherwise, check which is greater.
        // If they are the same number, do the previous operations/repeat for the second number.
        return if (firstNum1.length > firstNum2.length) 1
        else if (firstNum1.length < firstNum2.length) -1
        else { //they are the same length, now check if same num; if yes, check second num
            val firstNumComparison = firstNum1.compareTo(firstNum2)
            if (firstNumComparison != 0) firstNumComparison //if not the same num, we know what to sort by. Otherwise, check the second num
            else {
                if (secondNum1.length > secondNum2.length) 1
                else if (secondNum1.length < secondNum2.length) -1
                else {
                    secondNum1.compareTo(secondNum2)
                }
            }
        }
    }
    try {
        if (catalogOnlyContainsB) {
            val firstIsBelenofsky = o1.first().equals('B', true)
            val secondIsBelenofsky = o2.first().equals('B', true)
            if (firstIsBelenofsky && !secondIsBelenofsky) 1
            else if (!firstIsBelenofsky && secondIsBelenofsky) -1
            else compareShelf(o1, o2, firstIsBelenofsky && secondIsBelenofsky/*they are either both or neither*/)
        } else {
            val firstChar = o1.first()
            val secondChar = o2.first()
            val firstIsLetter =
                firstChar.isLetter()//being a letter means it is from a satelite beis medrash, like the belenefosky
            val secondIsLetter = secondChar.isLetter()

            if (firstIsLetter && !secondIsLetter) 1
            else if (!firstIsLetter && secondIsLetter) -1
            else if (firstIsLetter && secondIsLetter) //they are either both from a satelite
                if (firstChar != secondChar) firstChar.compareTo(secondChar) //if they are not from the same beis medrash, sort the shelves by beis, then by shelf sort order (i.e. group seforim from belenefosky with belenofsky, beis X seforim with beis X seforim, etc.)
                else compareShelf(o1, o2, true) //they are either both from the same beis medrash
            else compareShelf(o1, o2, false) //they are both from the main beis
        }
    } catch (t: Throwable) {
        log("Sorting encountered an error on \"$o1\" and \"$o2\": ${t.stackTraceToString()}")
        1 //just dump them at the end
    }
}
val catalogEntriesAsMutableList: MutableCollection<Any> = Catalog.entries.toMutableList()

val sortedCounter = AtomicInteger(0)

var timeSincePreviousLog = System.nanoTime()
fun resetTime() {
    timeSincePreviousLog = System.nanoTime()
}
var logging = true
fun <T> log(message: T) {
    if(logging) println(message)
}
fun log() {
    if(logging) println()
}
fun logTime(message: String, startTime: Long = timeSincePreviousLog) {
//    incrementStartupProgress()
    if(logging) {
        log("$message${" ".repeat(100 - message.length)}${"%f".format((System.nanoTime() - startTime).div(1_000_000_000.00))}")
        resetTime()
    }
}

const val exactSearchName = "Exact search"
const val rootSearchName = "Root word (שרש) search"
const val similaritySearchName = "Similarity search"
const val alternatePhraseSearchName = "Alternate phrase search"
const val maleiChaseirSearchName = "Malei/Chaseir Insensitive search"

abstract class SearchableTableJPanel(
    private val searchPhrase: String,
    private val getLemmatizedCriteriaLambda: ((LemmatizedCatalogEntry) -> Set<Set<String>>)? = null
) : JPanel() {
    //    fun <T> MutableList<T>.toSynchronizedList(): MutableList<T> = this//Collections.synchronizedList(this)
    open val originalCollection: MutableCollection<Any> by lazy {
        resetTime()
        getOriginalList().also {
            logTime("Time to call getOriginalList():")
        }
    }
    open val originalCollectionLemmatized: List<LemmatizedCatalogEntry> = emptyList()
    open val listBeingDisplayed: MutableList<Any> by lazy {
        resetTime()
        originalCollection.toMutableList()/*.toSynchronizedList()*/.also {
            logTime("Time to call originalCollection.toMutableList():")

        }
    }
    open val displayingCatalogEntry: Boolean =
        false //if false, return listBeingDisplayed[rowNum] in table model, otherwise return value depending on columnNum
    open val columns = emptyList<String>()
    open val getElementToSearchBy: (Pair<*, *>) -> String = { "" }

    var _constraint: Regex? = null //singleton pattern
    var _searchPhrase: String? = null

    open fun getOriginalList(): MutableCollection<Any> = catalogEntriesAsMutableList

    private fun String.addHebrewWordBoundaryIfAllowed(matchWordBoundary: Boolean): String {
        if (!matchWordBoundary) return this
        val startBoundary = "(?:(?<=\\p{InHebrew})(?=\\P{InHebrew})|(?<=\\P{InHebrew})(?=\\p{InHebrew})|^)"
        val endBoundary = "(?:(?<=\\p{InHebrew})(?=\\P{InHebrew})|(?<=\\P{InHebrew})(?=\\p{InHebrew})|$)"
        return "$startBoundary($this)$endBoundary"
    }//"(?!<\\p{InHebrew})($this)(?!\\p{InHebrew})"

    /*only gets regex when the constraint hasn't changed, so that it doesn't create a new regex for every list item*/
    private fun getConstraintRegex(constraint: String, matchWordBoundary: Boolean): Regex {
        //log("getConstraintRegex(constraint=$constraint), _searchPhrase=$_searchPhrase, _constraint=$_constraint")
        if ((/*constraint == _constraint?.toString()*/ /*already got regex*/ /*||*/ constraint.lastOrNull() == '#'/*user has not typed alternate phrase, so don't search for whitespace (i.e. every entry)*/) && _constraint != null) return _constraint!! /*use "old" regex*/
        /*get new regex*/
        lateinit var regex: Regex
        val lemmatizerRegex = "!([^!]+)!".toRegex()
        val mConstraint =
            if (!constraint.contains(lemmatizerRegex)) constraint else constraint.replace(lemmatizerRegex) {
                lemmatizer.getLemmatizedList(it.groupValues.first())
                    .also { if (it.size > 1) log("Error: $constraint") }.first()
                    .joinToString("|", "(", ")")
            }
        val replaceHashWithOr = mConstraint.replace("#", "|")
        regex = replaceHashWithOr.tryRegexOrEscape().pattern.addHebrewWordBoundaryIfAllowed(matchWordBoundary)
            .toRegex(RegexOption.IGNORE_CASE)
        _constraint = regex
        //log("Pattern of constraint: $regex")
        //log("Searching for: $regex")
        return regex
    }

    private fun String.tryRegexOrEscape() = try {
        toRegex(RegexOption.IGNORE_CASE) //if it is a valid regex, go for it
    } catch (t: Throwable) { //probably invalid pattern
        fun String.escapeRegexChars() =
            listOf("\\", "(", ")", "[", "]", "{", "}", "?", "+", "*")
                .fold(this) { acc: String, b: String -> acc.replace(b, "\\$b") }
        escapeRegexChars().toRegex(RegexOption.IGNORE_CASE)
    }

    val specialChars = "\\s.,-·;:'\"\\[\\]()!?<>&#\\d"
    open fun getCriteria(entry: CatalogEntry): String =
        ""//has default implementation so that JPanels which don't contain CatalogEntrys (e.g. list of authors) don't need to implement it

    open fun matchesConstraint(element: String, constraint: String, regex: Regex) = element.contains(regex)
    open fun matchesConstraint(element: CatalogEntry, constraint: String, regex: Regex): Boolean =
        getCriteria(element).contains(regex)

    open fun matchesConstraintNoRegex(element: CatalogEntry, constraint: String): Boolean =
        getCriteria(element).contains(constraint, ignoreCase = true)

    open fun matchesConstraintNoRegex(element: String, constraint: String): Boolean =
        element.contains(constraint, ignoreCase = true)

    open fun JTable.setJTableColumnsWidth(
        percentages: List<Double>
    ) {
        val tablePreferredWidth = this.preferredSize.width
        var total = 0.0
        for (i in 0 until columnModel.columnCount) {
            total += percentages[i]
        }
        for (i in 0 until columnModel.columnCount) {
            val column = columnModel.getColumn(i)
            column.preferredWidth = (tablePreferredWidth * (percentages[i] / total)).toInt()
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="expanded" desc="Generated Code">
    fun initComponents(): SearchableTableJPanel {
        resetTime()
        buttonGroup1 = ButtonGroup()
        jLabel1 = JLabel(searchPhrase)
        seferNameTextField = JTextField()
        jScrollPane1 = JScrollPane()
        val hideExplanationString = "Hide search mode explanation"
        jToggleButton1 = JToggleButton(hideExplanationString)
        table = object : JTable(catalogModel()) {
            /*override fun getToolTipText(e: MouseEvent): String? {
                return getValueAt(rowAtPoint(e.point), columnAtPoint(e.point))?.toString()
            }*/
        }
        jLabel2 = JLabel("Results: ${listBeingDisplayed.size}")

        resetTime()
        val explanationPanelTitledBorder = BorderFactory.createTitledBorder(exactSearchName/*start with exact*/)
        logTime("Time to create border object:")
        searchModeExplanation = JPanel().apply {
//            isVisible = true
            add(exactMatchJPanel)
            resetTime()
            border = explanationPanelTitledBorder
            logTime("Time to set border:")
            layout = GridLayout()
            logTime("Time to set layout:")
        }

        jLabel6 = JLabel("Search mode:")/*.also { it.isVisible = false }*/
        exactSearchRadioButton = JRadioButton(exactSearchName).also { it.isSelected = true }
        rootWordSearchRadioButton = JRadioButton(rootSearchName).also { it.isVisible = getLemmatizedCriteriaLambda != null }
        maleiChaseirSearchRadioButton = JRadioButton(maleiChaseirSearchName)/*.also { it.isVisible = false }*/
        patternSearchRadioButton = JRadioButton(alternatePhraseSearchName).also { it.isVisible = false }
        seferNameTextField.locale = Locale("he")
        seferNameTextField.componentOrientation = ComponentOrientation.RIGHT_TO_LEFT
        table.autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN
        logTime("Time to init properties:")
        val rightToLeftAlignmentRenderer = DefaultTableCellRenderer()
        rightToLeftAlignmentRenderer.horizontalAlignment = JLabel.RIGHT
        table.columnModel.columns.asIterator().forEach { it.cellRenderer = rightToLeftAlignmentRenderer }
        logTime("Time to set right to left:")
        resetTime()
        table.tableHeader.reorderingAllowed = false
        logTime("Time to allow reordering:")
        /*
        Category: 40 chars
        Author: 50 chars
        Name: 50 chars
        Publisher: 20 chars
        */
//        repeat(table.columnModel.columnCount) { table.columnModel.getColumn(it).preferredWidth = listOfAverageLengths[it].roundToInt() }
        val sizes = Catalog.tableSizes
        table.addHierarchyBoundsListener(object : HierarchyBoundsListener {
            override fun ancestorMoved(evt: HierarchyEvent) {}
            override fun ancestorResized(evt: HierarchyEvent) {
                table.setJTableColumnsWidth(sizes)
            }
        })
        logTime("Time to add table bounds listener:")
        table.tableHeader.defaultRenderer = object : TableCellRenderer {
            var renderer: DefaultTableCellRenderer = table.tableHeader.defaultRenderer as DefaultTableCellRenderer
            override fun getTableCellRendererComponent(
                table: JTable, value: Any, isSelected: Boolean,
                hasFocus: Boolean, row: Int, col: Int
            ): Component {
                return renderer.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, col
                )
            }

            init {
                renderer.horizontalAlignment = JLabel.RIGHT
            }
        }
        logTime("Time to set default renderer:")
        val (globalTableFont, globalTableFontSize) = fontFileContents
        table.font = Font(globalTableFont, 0, globalTableFontSize.toInt())
//        table.showHorizontalLines = true
        table.showVerticalLines = true
        jScrollPane1.setViewportView(table)
        logTime("Time to set font, vertical lines, and viewport:")
        seferNameTextField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = updateTextFieldAndFilter()
            override fun removeUpdate(e: DocumentEvent?) = updateTextFieldAndFilter()
            override fun changedUpdate(e: DocumentEvent?) = updateTextFieldAndFilter()

            private fun updateTextFieldAndFilter() {
                EventQueue.invokeLater {
                    seferNameTextField.componentOrientation =
                        if (seferNameTextField.text.trim()
                                .also {
                                    scope.launch(Dispatchers.IO) {
                                        kotlin.runCatching { logFile.appendText(it + "\n") }
                                    }
                                }
                                .firstOrNull()
                                ?.toString()
                                ?.let { it.containsEnglish() || /*if regex*/ it == "~" } == true
                        ) ComponentOrientation.LEFT_TO_RIGHT
                        else ComponentOrientation.RIGHT_TO_LEFT
                }
                //                מדד מודד מדדו למדוד למודד מדדו ימודדו ימודד
                //                the above list of words generates the lemmas דד and מדד. When using שרש search, consider adding hei to end of
                //                two letter shoresh.
                //                Related Shoresh transformations: TODO
                //                1. related letters get switched (אהחע, בומפ, זשסרצ, דנטלת, גיכק)
                //                2. double letters turn into letter+hei
                //                3. words which end in hei get replaced with first letter + vav + second letter (רמה into רום)
                //                4. words which end in hei get second letter doubles (רמה into רמם)

                //                Explanation of shoresh search:
                //                שרש search
                //                (looks for matches by comparing the root words contained in the search query with the root words in each catalog entry,
                //                preserving nouns in their full form (e.g. וממאמריהם -> מאמר) and reducing verbs and adjectives* to their root word)
                //                *Except for participles in בנין נפעל (e.g. נאזר) - see tip #7
                filterList()
            }
        })
        seferNameTextField.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {}

            override fun keyPressed(e: KeyEvent?) {}

            override fun keyReleased(e: KeyEvent?) {

            }
        }
        )
        exactSearchRadioButton.addActionListener {
            setExplanationJPanel(exactMatchJPanel, explanationPanelTitledBorder, exactSearchName, FILTER_EXACT)
        }
        rootWordSearchRadioButton.addActionListener {
            setExplanationJPanel(rootWordSearchJPanel, explanationPanelTitledBorder, rootSearchName, FILTER_ROOT)
        }
        maleiChaseirSearchRadioButton.addActionListener {
            setExplanationJPanel(
                maleiChaseirSearchJPanel,
                explanationPanelTitledBorder,
                maleiChaseirSearchName,
                FILTER_INSENSITIVE
            )
        }
        patternSearchRadioButton.addActionListener {
            setExplanationJPanel(
                alternatePhrasesJPanel,
                explanationPanelTitledBorder,
                alternatePhraseSearchName,
                FILTER_EXACT
            )
        }
        jToggleButton1.addItemListener {
            if (it.stateChange == ItemEvent.SELECTED) {
                jToggleButton1.text = "Show search mode explanation"
                searchModeExplanation.isVisible = false
            } else {
                searchModeExplanation.isVisible = true
                jToggleButton1.text = hideExplanationString
            }
        }
        logTime("Time to add listeners:")

        buttonGroup1.add(exactSearchRadioButton)
        buttonGroup1.add(rootWordSearchRadioButton)
        buttonGroup1.add(patternSearchRadioButton)
        buttonGroup1.add(maleiChaseirSearchRadioButton)

        /*similaritySearchJPanel = SimilaritySearchJPanel()*/
        similaritySearchJPanel?.filterCallback = object : Function1<LevenshteinDistance, Unit> {
            override fun invoke(p1: LevenshteinDistance) {
                log("Edit distance updated: ${p1.threshold}")
                filterList(FILTER_SIMILARITY, true, p1)
            }
        }
        val layout = GroupLayout(this)
        setLayout(layout)
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(
                                    layout.createSequentialGroup()
                                        .addComponent(
                                            searchModeExplanation,
                                            GroupLayout.DEFAULT_SIZE,
                                            GroupLayout.DEFAULT_SIZE,
                                            Short.MAX_VALUE.toInt()
                                        )
                                        .addContainerGap()
                                )
                                .addGroup(
                                    layout.createSequentialGroup()
                                        .addComponent(jScrollPane1)
                                        .addContainerGap()
                                )
                                .addGroup(
                                    layout.createSequentialGroup()
                                        .addGroup(
                                            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addGroup(
                                                    layout.createSequentialGroup()
                                                        .addComponent(jLabel1)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(seferNameTextField)
                                                )
                                                .addGroup(
                                                    layout.createSequentialGroup()
                                                        .addComponent(jLabel2)
                                                        .addGap(0, 0, Short.MAX_VALUE.toInt())
                                                )
                                        )
                                        .addGap(6, 6, 6)
                                )
                                .addGroup(
                                    layout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(exactSearchRadioButton)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(rootWordSearchRadioButton)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(maleiChaseirSearchRadioButton)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(patternSearchRadioButton)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jToggleButton1)
                                        .addGap(0, 0, Short.MAX_VALUE.toInt())
                                )
                        )
                )
        )
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel6)
                                .addComponent(exactSearchRadioButton)
                                .addComponent(rootWordSearchRadioButton)
                                .addComponent(maleiChaseirSearchRadioButton)
                                .addComponent(patternSearchRadioButton)
                                .addComponent(jToggleButton1)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(
                            searchModeExplanation,
                            GroupLayout.PREFERRED_SIZE,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel1)
                                .addComponent(
                                    seferNameTextField,
                                    GroupLayout.PREFERRED_SIZE,
                                    GroupLayout.DEFAULT_SIZE,
                                    GroupLayout.PREFERRED_SIZE
                                )
                        )
                        .addGap(1, 1, 1)
                        .addComponent(jLabel2)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1)
                        .addContainerGap()
                )
        )
        logTime("Time to layout:")
        scope.launch(Dispatchers.Default) {
            Catalog.isEntireProgramInitialized.collect {
                if (it) {
                    launch(Dispatchers.Default) {
//                        delay(2_000) //don't need to sort right away
                        val sortStartTime = System.nanoTime()
//        table.autoCreateRowSorter = true
                        val rowSorter = TableRowSorter(table.model as AbstractTableModel)
                        val hebrewEntriesFirstComparator = kotlin.Comparator<String> { o1, o2 ->
                            val o1ContainsEnglish = o1.containsEnglish()
                            val o2ContainsEnglish = o2.containsEnglish()
                            if (o1ContainsEnglish && !o2ContainsEnglish) 1
                            else if (!o1ContainsEnglish && o2ContainsEnglish) -1
                            else (o1.lowercase()).compareTo(o2.lowercase())
                        }
                        if (this@SearchableTableJPanel is FindSeferByCriteriaJPanel) {
                            val indexOfSeferNameColumn = columns.indexOf(seferNameColumnString)
                            rowSorter.setComparator(indexOfSeferNameColumn, hebrewEntriesFirstComparator)
                            rowSorter.setComparator(columns.indexOf(shelfNumColumnString), shelfNumComparator)
                            rowSorter.sortKeys = listOf(RowSorter.SortKey(indexOfSeferNameColumn, SortOrder.ASCENDING))
                        } else if (this@SearchableTableJPanel is ListOfShelvesJPanel) {
                            rowSorter.setComparator(
                                columns.indices.last,
                                shelfNumComparator
                            ) //TODO make this index dynamic
                        }
                        logTime("Time to sort \"$searchPhrase\":", sortStartTime)
                        EventQueue.invokeLater {
                            val timeBeforeSettingRowSorter = System.nanoTime()
                            table.rowSorter = rowSorter
                            logTime("Time to set rowSorter for \"$searchPhrase\":", timeBeforeSettingRowSorter)
                            val incrementAndGet = sortedCounter.incrementAndGet()
                            scope.launch {
                                if (incrementAndGet == 9) {
                                    Catalog.isEntireProgramInitialized.emit(false)
                                }
                            }
                        }
                    }
                }
            }
        }
        return this
    } // </editor-fold>

    private fun setExplanationJPanel(
        panel: JPanel,
        explanationPanelTitledBorder: TitledBorder,
        borderSearchExplanation: String,
        searchMode: Int
    ) {

        searchModeExplanation.removeAll()
        searchModeExplanation.add(panel)
        searchModeExplanation.revalidate()
        searchModeExplanation.repaint()

        explanationPanelTitledBorder.title = borderSearchExplanation
        filterList(
            searchMode,
            if (panel is PlainTextExplanationJPanel) panel.boundaryCheckBox.isSelected
            else true
        ) //update list to reflect new search mode

    }

    private fun catalogModel() = object : AbstractTableModel() {

        override fun getColumnName(col: Int): String = columns[col] //same as { return columns[col] }

        override fun getRowCount(): Int = listBeingDisplayed.size

        override fun getColumnCount(): Int = columns.size

        override fun getValueAt(row: Int, col: Int): Any {
            val it = listBeingDisplayed[row]
            return when (it) {
                is String -> it
                is Pair<*, *> -> if (col == 0) it.first else it.second
                else -> {
                    it as CatalogEntry
                    when (col) {
                        /*Publisher (הוצאה)",
        Category (קטיגורי)",
        Volume (כרך)",
        Author (שם המחבר)",
        Shelf (מס' מדף)",
        Name (שם הספר)"*/
                        0 -> it.publisher
                        1 -> it.category
                        2 -> it.volumeNum
                        3 -> it.author
                        4 -> it.shelfNum
                        5 -> it.seferName
                        else -> TODO("This should not have happened: getValueAt($row:, $col)")
                    }
                }
            } ?: "".also { System.err.println("This should never have happened") }
        }

        override fun isCellEditable(row: Int, col: Int): Boolean = false

    }.also { tableModel = it }

    private lateinit var jLabel1: JLabel
    private lateinit var jLabel2: JLabel
    private lateinit var jToggleButton1: JToggleButton
    private lateinit var jScrollPane1: JScrollPane
    lateinit var seferNameTextField: JTextField
    lateinit var table: JTable
    lateinit var tableModel: AbstractTableModel
    lateinit var rootWordSearchRadioButton: JRadioButton
    lateinit var searchModeExplanation: JPanel
    lateinit var patternSearchRadioButton: JRadioButton
    lateinit var maleiChaseirSearchRadioButton: JRadioButton
    lateinit var buttonGroup1: ButtonGroup
    lateinit var exactSearchRadioButton: JRadioButton
    lateinit var jLabel6: JLabel
    var rootWordSearchInitialized = false
    val rootWordSearchJPanel: RootWordSearchExplanationJPanel by lazy {
        rootWordSearchInitialized = true
        RootWordSearchExplanationJPanel(this)
    }
    val similaritySearchJPanel: SimilaritySearchJPanel? = null
    private val exactMatchJPanel: PlainTextExplanationJPanel by lazy {
        PlainTextExplanationJPanel(
            "Exact search determines matching entries based on whether the entry contains the exact search phrase entered."
        ) {
            _previousMatchWordBoundary[FILTER_EXACT] = !it
            filterList()
        }
    }
    private val alternatePhrasesJPanel: PlainTextExplanationJPanel by lazy {
        PlainTextExplanationJPanel(
            "Alternate phrase search determines matches based on whether the entry contains any of the phrases separated by \"#\", e.g. \"fire#אור#אש\" or \"עשר(ת#ה) (מאמר#הדיבר)ות\" (i.e. show results for either עשרת הדיברות or עשרה מאמרות). Surround words with an exclamation mark to have the program turn the word into its shoresh/shorashim. See tip #5 for more information."
        ) {
            _previousMatchWordBoundary[FILTER_ALTERNATE_PHRASE] = !it
            filterList()
        }
    }
    private val maleiChaseirSearchJPanel: PlainTextExplanationJPanel by lazy {
        PlainTextExplanationJPanel(
            "Malei/Chaseir Insensitive search determines matches based on whether the entry contains the exact search phrase entered, while allowing a vav or yud to appear anywhere in middle of any of the words in the search phrase or entry text. This means that searching for חמש will show results for חומש, and vice versa."
        ) {
            _previousMatchWordBoundary[FILTER_INSENSITIVE] = !it
            filterList()
        }
    }

    fun filterList() {
        val (mode, matchWordBoundary) = if (exactSearchRadioButton.isSelected) FILTER_EXACT to exactMatchJPanel.boundaryCheckBox.isSelected
        else if (rootWordSearchRadioButton.isSelected) FILTER_ROOT to true
        else if (maleiChaseirSearchRadioButton.isSelected) FILTER_INSENSITIVE to maleiChaseirSearchJPanel.boundaryCheckBox.isSelected
        else FILTER_ALTERNATE_PHRASE to alternatePhrasesJPanel.boundaryCheckBox.isSelected
        filterList(
            mode,
            matchWordBoundary,
            null//if (mode != FILTER_SIMILARITY) null else similaritySearchJPanel.ld
        )
    }

    var mMode = FILTER_EXACT
    fun filterList(mode: Int, matchWordBoundary: Boolean, ld: LevenshteinDistance? = null) {
        if (!entireProgramIsInitialized) return
        val _constraint1 = seferNameTextField.text?.trim() //TODO consider making this a computed field
        if (_constraint1 == null || (_searchPhrase == _constraint1 && (_constraint1.isBlank() || mode == mMode) && matchWordBoundary == _previousMatchWordBoundary[mode])) {
            log("Returning early from filterList() - nothing changed")
            return
        } //don't do anything if the constraint is blank and the user is clicking different modes, or the constraint hasn't changed, or the "Match word boundary" setting hasn't changed
        _searchPhrase = _constraint1
        mMode = mode
        if (_constraint1.isBlank()) {
            updateList(originalCollection)
            if (rootWordSearchInitialized) rootWordSearchJPanel.setShorashim(listOf())
            return
        }
        when (mode) {
            FILTER_EXACT -> filterListExactMatch(_constraint1, matchWordBoundary)
            FILTER_ROOT -> filterListRootSearch(_constraint1)
            FILTER_INSENSITIVE -> filterListMaleiChaseirInsensitive(_constraint1, matchWordBoundary)
            FILTER_ALTERNATE_PHRASE -> filterListExactMatch(_constraint1, matchWordBoundary)
            FILTER_SIMILARITY -> filterListSimilaritySearch(_constraint1, ld!!)
        }
    }

    fun filterListMaleiChaseirInsensitive(constraint: String, matchWordBoundary: Boolean) =
        filterListExactMatch(constraint, matchWordBoundary, useMaleiChaseirInsensitive = true)

    fun filterListRootSearch(constraint: String) {
        val queryShorashim = lemmatizer.getLemmatizedList/*Debug*/(constraint)
//            .reversed()//the text field puts the words backwards
            .toList()
        if (rootWordSearchInitialized) rootWordSearchJPanel.setShorashim(queryShorashim.flatten())
//        log("Query shorashim: $queryShorashim")
        val listOfChecks = mutableListOf<Pair<String, String>>()
        filterWithPredicate(true, {
            false //root search not available on columns whose lemmas weren't indexed
        }) { entry ->
            val entryShorashim = getLemmatizedCriteriaLambda!!(entry as LemmatizedCatalogEntry).toList()
            //return:
            /* if (entryShorashim.isEmpty()) false
             else {*/
            (
                    if (rootSearchShouldMatchAll)
                        if (rootSearchShouldMatchSequential)
                            matchesAllOrdered(queryShorashim, entryShorashim)
                        else matchesAllUnordered(queryShorashim, entryShorashim, listOfChecks)
                    else matchesAny(queryShorashim, entryShorashim, listOfChecks)
                    )
//                .also {
            //if(it) log("Sefer: ${entry.seferName}, Entry shorashim: $entryShorashim, checks: $listOfChecks")
//                }
//            }
        }
    }

    fun filterListSimilaritySearch(constraint: String, levenshteinDistance: LevenshteinDistance) {
        filterWithPredicate(false, {
            val distance = levenshteinDistance.apply(constraint, it)
            log("Distance between $constraint and $it: $distance")
            distance != -1 && distance <= levenshteinDistance.threshold
        }) {
            val criteria = getCriteria(it)
            val distance = levenshteinDistance.apply(constraint, criteria)
            log("Distance between $constraint and $criteria: $distance")
            distance != -1 && distance <= levenshteinDistance.threshold
        }
    }

    private fun filterWithPredicate(
        useLemmatizedList: Boolean,
        predicateIfString: (String) -> Boolean,
        predicateIfCatalogEntry: (CatalogEntry) -> Boolean
    ) /*= scope.launch(Dispatchers.Default)*/ {
        if (useLemmatizedList) {
            val list = Collections.synchronizedList(mutableListOf<CatalogEntry>())
            (originalCollectionLemmatized as Collection<LemmatizedCatalogEntry>)
                .parallelStream()
                .filter {
                    try {
                        predicateIfCatalogEntry(it)
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        log("filterWithPredicate threw error, entry=\"$it\"")
                        false
                    }
                }
                .forEach { list.add(it) }
            updateList(list)
        } else {
            val firstOrNull = originalCollection.firstOrNull()
            when (firstOrNull) { //Pretty sure this can be DRYed up with filter exact match function
                is String -> {
                    val list = Collections.synchronizedList(mutableListOf<String>())
                    (originalCollection as Collection<String>)
                        .parallelStream()
                        .filter { predicateIfString(it) }
                        .forEach { list.add(it) }
                    updateList(list)
                }

                is Pair<*, *> -> {
                    val list = Collections.synchronizedList(mutableListOf<String>())
                    (originalCollection as Collection<Pair<String, String>>)
                        .parallelStream()
                        .filter { predicateIfString(getElementToSearchBy(it)) }
                        .forEach { list.add(getElementToSearchBy(it)) }
                    updateList(list)
                }

                else -> {
                    val list = Collections.synchronizedList(mutableListOf<CatalogEntry>())
                    (originalCollection as Collection<CatalogEntry>)
                        .parallelStream()
                        .filter {
                            try {
                                predicateIfCatalogEntry(it)
                            } catch (t: Throwable) {
                                t.printStackTrace()
                                log("filterWithPredicate threw error, entry=\"$it\"")
                                false
                            }
                        }
                        .forEach { list.add(it) }
                    updateList(list)
                }
            }
        }
    }

    fun filterListExactMatch(
        _constraint: String,
        matchWordBoundary: Boolean,
        useAlternatePhraseSearch: Boolean = false,
        useMaleiChaseirInsensitive: Boolean = false,
    ) {
        log("filterListExactMatch($_constraint, $useAlternatePhraseSearch, $useMaleiChaseirInsensitive)")
        var constraint = _constraint
        if (constraint.isBlank()) {
            updateList(originalCollection)
            return
        }
        val newList = Collections.synchronizedList(mutableListOf<Any>())
        val firstElement = originalCollection.firstOrNull()
        val needsRegex =
            matchWordBoundary || useAlternatePhraseSearch || constraint.contains("[#!]".toRegex()) || constraint.startsWith(
                '~'
            )
                .also { if (it) constraint = constraint.removePrefix("~") }
        val regex =
            if (useMaleiChaseirInsensitive) {
                log("Using malei chaseir insensitivity")
                constraint = constraint.replace(yudOrVavInWordRegex, "")
                val placesToInsert = letterBoundariesRegex
                    .findAll(constraint)
                    .map { it.range.first }
                    .toList()
                log("Places to insert: $placesToInsert")
                val maleiInsensitive = StringBuilder(constraint)
                for (index in placesToInsert.asReversed()) maleiInsensitive.insert(index, "[יו]{0,3}")
                maleiInsensitive
                    .toString()
                    .tryRegexOrEscape()//don't want to escape the hebrew word boundary, only the constraint
                    .pattern
                    .addHebrewWordBoundaryIfAllowed(matchWordBoundary)
                    .tryRegexOrEscape()
                    .also { log("Searching for: $it") }
            } else if (needsRegex) getConstraintRegex(constraint, matchWordBoundary)
            else null
        val predicate: (Any) -> Boolean =
            if (firstElement is String)
                if (needsRegex || useMaleiChaseirInsensitive) {
                    { matchesConstraint((it as String), constraint, regex!!) }
                } else {
                    { matchesConstraintNoRegex((it as String), constraint) }
                }
            else if (firstElement is Pair<*, *>)
                if (needsRegex || useMaleiChaseirInsensitive) {
                    { matchesConstraint(getElementToSearchBy((it as Pair<*, *>)), constraint, regex!!) }
                } else {
                    { matchesConstraintNoRegex(getElementToSearchBy((it as Pair<*, *>)), constraint) }
                }
            else
                if (needsRegex || useMaleiChaseirInsensitive) {
                    { matchesConstraint((it as CatalogEntry), constraint, regex!!) }
                } else {
                    { matchesConstraintNoRegex((it as CatalogEntry), constraint) }
                }
        originalCollection
            .parallelStream()
            .filter {
                try {
                    predicate(it)
                } catch (t: Throwable) {
                    t.printStackTrace()
                    log("Threw error, entry=\"$it\", constraint=\"$constraint\"")
                    false
                }
            }
            .forEach {
//                log("Item being added: $it")
                newList.add(it)
            }
        updateList(newList)
    }

    private fun updateList(newList: Collection<Any>) {
        listBeingDisplayed.clear()
        listBeingDisplayed.addAll(newList)
        //EventQueue.invokeLater {
        (table.model as AbstractTableModel).fireTableDataChanged()
        jLabel2.text = "Results: ${listBeingDisplayed.size}"
        //}
    }

    companion object {
        fun matchesAllUnordered(
            queryShorashim: List<Set<String>>,
            entryShorashim: List<Set<String>>,
            listOfChecks: MutableList<Pair<String, String>>,
            logChecks: Boolean = false
        ): Boolean {
            return queryShorashim.all { shorashim: Set<String> ->
                shorashim.any { shoresh: String ->
                    entryShorashim.any {
                        it.any {
                            if (logChecks) listOfChecks.add(shoresh to it)
                            it == shoresh
                        }
                    }
                }
            }
        }

        fun matchesAny(
            queryShorashim: List<Set<String>>,
            entryShorashim: List<Set<String>>,
            listOfChecks: MutableList<Pair<String, String>>,
            logChecks: Boolean = false
        ) = queryShorashim.any {
            it.any { queryShoresh ->
                entryShorashim.any {
                    it.any { entryShoresh ->
                        if (logChecks) listOfChecks.add(queryShoresh to entryShoresh)
                        entryShoresh == queryShoresh
                    }
                }
            }
        }

        /**
         * Determines whether [this] list contains a sublist such that at least one element in each list of said sublist is contained in a parallel sublist of [other].
         * This is a direct adaptation of [CharSequence.contains(CharSequence)].
         * For example the following returns true:
        val x = listOf(                       listOf('a', 'b', 'c'), listOf('d', 'e', 'f')             )
        val y = listOf(listOf('x' ,'y' ,'z'), listOf('1', '2', 'a'), listOf('3', 'f', '4'), listOf('9'))
        val z = listOf(                       listOf('1', '2', 'a'), listOf('3', 'f', '4')             )
        println(x in y) //prints true
        println(x in z) //prints true
         */
        operator fun <T> List<Iterable<T>>.contains(other: List<Iterable<T>>): Boolean =
            contains(other) { thisList, otherList -> thisList.any { it in otherList } }

        fun <T> List<T>.contains(other: List<T>, predicate: (thisElement: T, otherElement: T) -> Boolean): Boolean =
            indexOf(other, 0, this.size, predicate) >= 0

        private fun <T> List<T>.indexOf(
            other: List<T>,
            startIndex: Int,
            endIndex: Int,
            predicate: (thisElement: T, otherElement: T) -> Boolean
        ): Int {
            fun <T> List<T>.regionMatches(
                thisOffset: Int,
                other: List<T>,
                otherOffset: Int,
                size: Int,
                predicate: (thisElement: T, otherElement: T) -> Boolean
            ): Boolean {
                if ((otherOffset < 0) || (thisOffset < 0) || (thisOffset > this.size - size) || (otherOffset > other.size - size)) {
                    return false
                }

                for (index in 0 until size) {
                    if (!predicate(this[thisOffset + index], other[otherOffset + index]))
                        return false
                }
                return true
            }

            val indices = startIndex.coerceAtLeast(0)..endIndex.coerceAtMost(this.size)

            for (index in indices) {
                if (other.regionMatches(0, this, index, other.size, predicate))
                    return index
            }
            return -1
        }

        fun matchesAllOrdered(
            queryShorashim: List<Iterable<String>>,
            entryShorashim: List<Iterable<String>>,
        ): Boolean {
            if (entryShorashim.isEmpty()) return false
            return queryShorashim in entryShorashim
        }
    }
}
