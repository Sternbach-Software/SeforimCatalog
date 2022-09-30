import Catalog.containsEnglish
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.text.similarity.LevenshteinDistance
import java.awt.*
import java.awt.event.*
import java.util.*
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
private const val FILTER_ALTERNATE_PHRASE = 3
private const val FILTER_SIMILARITY = 4
var catalogOnlyContainsB = true
val shelfNumComparator = kotlin.Comparator<String> { o1, o2 ->
    inline fun compareShelf(o1: String, o2: String, hasLetter: Boolean): Int {
        val indexOfDot1 = o1.indexOf(".")
        val firstNum1 = o1.substring(if (hasLetter) 1/*exclude letter*/ else 0, indexOfDot1)
        val secondNum1 = o1.substring(indexOfDot1 + 1)

        val indexOfDot2 = o2.indexOf(".")
        val firstNum2 = o2.substring(if (hasLetter) 1 else 0, indexOfDot2)
        val secondNum2 = o2.substring(indexOfDot2 + 1)

        // First check size of strings: if one number has more digits than the other,
        // then it is certainly bigger. Otherwise, check which is bigger.
        // If they are the same number, do the previous operations for the second number.
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
        if(catalogOnlyContainsB) {
            val firstIsBelenofsky = o1.first().equals("B", true)
            val secondIsBelenofsky = o2.first().equals("B", true)
            if (firstIsBelenofsky && !secondIsBelenofsky) 1
            else if (!firstIsBelenofsky && secondIsBelenofsky) -1
            else compareShelf(o1, o2, firstIsBelenofsky && secondIsBelenofsky/*they are either both or neither*/)
        } 
        else {
            var firstChar = o1.first()
            var secondChar = o2.first()
            val firstIsLetter = firstChar.isLetter()//being a letter means it is from a satelite beis medrash, like the belenefosky
            val secondIsLetter = secondChar.isLetter() 
            
            if (firstIsLetter && !secondIsLetter) 1
            else if (!firstIsLetter && secondIsLetter) -1
            else if(firstIsLetter && secondIsLetter) //they are either both from a satelite
                if(firstChar != secondChar) firstChar.compareTo(secondChar) //if they are not from the same beis medrash, sort the shelves by beis, then by shelf sort order (i.e. group seforim from belenefosky with belenofsky, beis X seforim with beis X seforim, etc.)
                else compareShelf(o1, o2, true) //they are either both from the same beis medrash
            else compareShelf(o1, o2, false) //they are both from the main beis
        }
    } catch(t: Throwable) {
        println("Sorting encountered an error on \"$o1\" and \"$o2\": ${t.stackTraceToString()}")
        1 //just dump them at the end
    }
}

abstract class SearchableTableJPanel(
    private val searchPhrase: String,
    private val getLemmatizedCriteriaLambda: ((LemmatizedCatalogEntry) -> Set<Set<String>>)? = null
) : JPanel() {
    fun <T> MutableList<T>.toSynchronizedList(): MutableList<T> = this//Collections.synchronizedList(this)
    open val originalCollection: Collection<Any> = emptyList()
    open val originalCollectionLemmatized: List<LemmatizedCatalogEntry> = emptyList()
    open val listBeingDisplayed: MutableList<Any> = mutableListOf()
    open val displayingCatalogEntry: Boolean =
        false //if false, return listBeingDisplayed[rowNum] in table model, otherwise return value depending on columnNum
    open val columns = emptyList<String>()
    open val getElementToSearchBy: (Pair<*, *>) -> String = { "" }

    var _constraint: Regex? = null //singleton pattern
    var _searchPhrase: String? = null

    /*only gets regex when the constraint hasn't changed, so that it doesn't create a new regex for every list item*/
    private fun getConstraintRegex(constraint: String): Regex {
        fun String.addBoundary() = "\\b($this)\\b"
        println("getConstraintRegex(constraint=$constraint), _searchPhrase=$_searchPhrase, _constraint=$_constraint")
        if ((constraint == _constraint?.toString() /*already got regex*/ || constraint.lastOrNull() == '#'/*user has not typed alternate phrase, so don't search for whitespace (i.e. every entry)*/) && _constraint != null) return _constraint!! /*use "old" regex*/
        /*get new regex*/
        lateinit var regex: Regex
        val lemmatizerRegex = "![^!]+!".toRegex()
        val mConstraint =
            if (!constraint.contains(lemmatizerRegex)) constraint else constraint.replace(lemmatizerRegex) {
                lemmatizer.getLemmatizedList(it.value).also { if (it.size > 1) println("Error: $constraint") }.first()
                    .joinToString("|", "(", ")")
            }
        val replaceHashWithOr = mConstraint.replace("#", "|")
        regex = try {
            replaceHashWithOr.addBoundary().toRegex(RegexOption.IGNORE_CASE) //if it is a valid regex, go for it
        } catch (t: Throwable) { //probably invalid pattern
            fun String.escapeRegexChars() =
                listOf("\\", "(", ")", "[", "]", "{", "}", "?", "+", "*")
                    .fold(this) { acc: String, b: String -> acc.replace(b, "\\$b") }
            replaceHashWithOr.escapeRegexChars().addBoundary().toRegex(RegexOption.IGNORE_CASE)
        }
        _constraint = regex
        //println("Pattern of constraint: $regex")
        return regex
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
        buttonGroup1 = ButtonGroup()
        jLabel1 = JLabel()
        seferNameTextField = JTextField()
        jScrollPane1 = JScrollPane()
        table = object: JTable() {
            /*override fun getToolTipText(e: MouseEvent): String? {
                return getValueAt(rowAtPoint(e.point), columnAtPoint(e.point))?.toString()
            }*/
        }
        jLabel1.text = searchPhrase
        jLabel2 = JLabel()
        searchModeExplanation = JPanel().also { it.isVisible = true }
        jLabel6 = JLabel()/*.also { it.isVisible = false }*/
        exactSearchRadioButton = JRadioButton()/*.also { it.isVisible = false }*/
        rootWordSearchRadioButton = JRadioButton().also { it.isVisible = getLemmatizedCriteriaLambda != null }
        patternSearchRadioButton = JRadioButton()/*.also { it.isVisible = false }*/
        seferNameTextField.locale = Locale("he")
        seferNameTextField.componentOrientation = ComponentOrientation.RIGHT_TO_LEFT
        table.model = catalogModel()
        table.autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN
        val rightToLeftAlignmentRenderer = DefaultTableCellRenderer()
        rightToLeftAlignmentRenderer.horizontalAlignment = JLabel.RIGHT
        table.columnModel.columns.asIterator().forEach { it.cellRenderer = rightToLeftAlignmentRenderer }
        table.tableHeader.reorderingAllowed = false
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
        val (font, size) = fontFile.readText().split(",")
        table.font = Font(font, 0, size.toInt())
//        table.showHorizontalLines = true
        table.showVerticalLines = true
        jScrollPane1.setViewportView(table)
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
//        table.autoCreateRowSorter = true
        val rowSorter = TableRowSorter(table.model as AbstractTableModel)
        val hebrewEntriesFirstComparator = kotlin.Comparator<String> { o1, o2 ->
            val o1ContainsEnglish = o1.containsEnglish()
            val o2ContainsEnglish = o2.containsEnglish()
            if (o1ContainsEnglish && !o2ContainsEnglish) 1
            else if (!o1ContainsEnglish && o2ContainsEnglish) -1
            else (o1.lowercase()).compareTo(o2.lowercase())
        }
        if (this is FindSeferByCriteriaJPanel) {
            val indexOfSeferNameColumn = columns.indexOf(seferNameColumnString)
            rowSorter.setComparator(indexOfSeferNameColumn, hebrewEntriesFirstComparator)
            rowSorter.setComparator(columns.indexOf(shelfNumColumnString), shelfNumComparator)
            rowSorter.sortKeys = listOf(RowSorter.SortKey(indexOfSeferNameColumn, SortOrder.ASCENDING))
        } else if (this is ListOfShelvesJPanel) {
            rowSorter.setComparator(columns.indices.last, shelfNumComparator) //TODO make this index dynamic
        }
        table.rowSorter = rowSorter
        jLabel2.text = "Results: ${listBeingDisplayed.size}"

        jLabel6.text = "Search mode:"
        val exactSearchName = "Exact search"
        val rootSearchName = "Root word (שרש) search"
        val similaritySearchName = "Similarity search"
        val alternatePhraseSearchName = "Alternate phrase search"
        rootWordSearchJPanel = RootWordSearchExplanationJPanel(this)
        similaritySearchJPanel = SimilaritySearchJPanel()
        val exactMatchJPanel = PlainTextExplanationJPanel(
            "Exact search determines matching entries based on whether the entry contains the exact search phrase entered."
        )
        val alternatePhrasesJPanel = PlainTextExplanationJPanel(
            "Alternate phrase search determines matches based on whether the entry contains any of the phrases separated by \"#\", e.g. \"fire#אור#אש\" or \"עשר(ת#ה) (מאמר#הדיבר)ות\" (i.e. show results for either עשרת הדיברות or עשרה מאמרות). Surround words with an exclamation mark to have the program turn the word into its shoresh/shorashim. See tip #5 for more information."
        )
        val explanationPanelTitledBorder = BorderFactory.createTitledBorder(exactSearchName/*start with exact*/)
        searchModeExplanation.border = explanationPanelTitledBorder
        searchModeExplanation.layout = GridLayout()
        exactSearchRadioButton.text = exactSearchName
        rootWordSearchRadioButton.text = rootSearchName
        patternSearchRadioButton.text = alternatePhraseSearchName

        patternSearchRadioButton.isVisible = false

        exactSearchRadioButton.addActionListener {
            setExplanationJPanel(exactMatchJPanel, explanationPanelTitledBorder, exactSearchName, FILTER_EXACT)
        }
        rootWordSearchRadioButton.addActionListener {
            setExplanationJPanel(rootWordSearchJPanel, explanationPanelTitledBorder, rootSearchName, FILTER_ROOT)
        }
        patternSearchRadioButton.addActionListener {
            setExplanationJPanel(
                alternatePhrasesJPanel,
                explanationPanelTitledBorder,
                alternatePhraseSearchName,
                FILTER_EXACT
            )
        }
        buttonGroup1.add(exactSearchRadioButton)
        buttonGroup1.add(rootWordSearchRadioButton)
        buttonGroup1.add(patternSearchRadioButton)

        exactSearchRadioButton.doClick()

        similaritySearchJPanel.filterCallback = object : Function1<LevenshteinDistance, Unit> {
            override fun invoke(p1: LevenshteinDistance) {
                println("Edit distance updated: ${p1.threshold}")
                filterList(FILTER_SIMILARITY, p1)
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
                                        .addComponent(patternSearchRadioButton)
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
                                .addComponent(patternSearchRadioButton)
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
        filterList(searchMode)//update list to reflect new search mode
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
    private lateinit var jScrollPane1: JScrollPane
    lateinit var seferNameTextField: JTextField
    lateinit var table: JTable
    lateinit var tableModel: AbstractTableModel
    lateinit var rootWordSearchRadioButton: JRadioButton
    lateinit var searchModeExplanation: JPanel
    lateinit var patternSearchRadioButton: JRadioButton
    lateinit var buttonGroup1: ButtonGroup
    lateinit var exactSearchRadioButton: JRadioButton
    lateinit var jLabel6: JLabel
    lateinit var rootWordSearchJPanel: RootWordSearchExplanationJPanel
    lateinit var similaritySearchJPanel: SimilaritySearchJPanel
    fun filterList() {
        val mode = if (exactSearchRadioButton.isSelected) FILTER_EXACT
        else if (rootWordSearchRadioButton.isSelected) FILTER_ROOT
        else FILTER_ALTERNATE_PHRASE
        filterList(
            mode,
            null//if (mode != FILTER_SIMILARITY) null else similaritySearchJPanel.ld
        )
    }

    var mMode = 0
    fun filterList(mode: Int, ld: LevenshteinDistance? = null) {
        val _constraint1 = seferNameTextField.text?.trim() //TODO consider making this a computed field
        if (_constraint1 == null || (_searchPhrase == _constraint1 && (_constraint1.isBlank() || mode == mMode))) return //don't do anything if the constraint is blank and the user is clicking different modes, or the constraint hasn't changed
        _searchPhrase = _constraint1
        mMode = mode
        if (_constraint1.isBlank()) {
            updateList(originalCollection)
            rootWordSearchJPanel.setShorashim(listOf())
            return
        }
        when (mode) {
            FILTER_EXACT -> filterListExactMatch(_constraint1)
            FILTER_ROOT -> filterListRootSearch(_constraint1)
            FILTER_ALTERNATE_PHRASE -> filterListExactMatch(
                _constraint1,
                true
            )//filterListSimilaritySearch(_constraint1, ld!!)
            FILTER_SIMILARITY -> filterListSimilaritySearch(_constraint1, ld!!)
        }
    }

    fun filterListRootSearch(constraint: String) {
        val queryShorashim = lemmatizer.getLemmatizedList(constraint)
//            .reversed()//the text field puts the words backwards
            .toList()
        rootWordSearchJPanel.setShorashim(queryShorashim.flatten())
//        println("Query shorashim: $queryShorashim")
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
            //if(it) println("Sefer: ${entry.seferName}, Entry shorashim: $entryShorashim, checks: $listOfChecks")
//                }
//            }
        }
    }

    fun filterListSimilaritySearch(constraint: String, levenshteinDistance: LevenshteinDistance) {
        filterWithPredicate(false, {
            val distance = levenshteinDistance.apply(constraint, it)
            println("Distance between $constraint and $it: $distance")
            distance != -1 && distance <= levenshteinDistance.threshold
        }) {
            val criteria = getCriteria(it)
            val distance = levenshteinDistance.apply(constraint, criteria)
            println("Distance between $constraint and $criteria: $distance")
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
                        println("filterWithPredicate threw error, entry=\"$it\"")
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
                                println("filterWithPredicate threw error, entry=\"$it\"")
                                false
                            }
                        }
                        .forEach { list.add(it) }
                    updateList(list)
                }
            }
        }
    }

    fun filterListExactMatch(_constraint: String, useAlternatePhraseSearch: Boolean = false) {
        var constraint = _constraint
        if (constraint.isBlank()) {
            updateList(originalCollection)
            return
        }
        val newList = Collections.synchronizedList(mutableListOf<Any>())
        val firstElement = originalCollection.firstOrNull()
        val needsRegex = useAlternatePhraseSearch || constraint.contains("[#!]".toRegex()) || constraint.startsWith('~')
            .also { if (it) constraint = constraint.removePrefix("~") }
        val regex = if (needsRegex) getConstraintRegex(constraint) else null
        val predicate: (Any) -> Boolean =
            if (firstElement is String)
                if (needsRegex) {
                    { matchesConstraint((it as String), constraint, regex!!) }
                } else {
                    { matchesConstraintNoRegex((it as String), constraint) }
                }
            else if (firstElement is Pair<*, *>)
                if (needsRegex) {
                    { matchesConstraint(getElementToSearchBy((it as Pair<*, *>)), constraint, regex!!) }
                } else {
                    { matchesConstraintNoRegex(getElementToSearchBy((it as Pair<*, *>)), constraint) }
                }
            else
                if (needsRegex) {
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
                    println("Threw error, entry=\"$it\", constraint=\"$constraint\"")
                    false
                }
            }
            .forEach {
//                println("Item being added: $it")
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
