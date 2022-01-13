import Catalog.containsEnglish
import Catalog.containsHebrew
import lemmatizer.hebmorph.tests.LemmatizerTest
import java.awt.Component
import java.awt.Font
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.util.*
import javax.swing.*
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableRowSorter


abstract class SearchableTableJPanel(
    private val searchPhrase: String
    ): JPanel() {
    open val originalCollection: Collection<Any> = emptyList()
    open val listBeingDisplayed: MutableList<Any> = mutableListOf()
    open val displayingCatalogEntry: Boolean = false //if false, return listBeingDisplayed[rowNum] in table model, otherwise return value depending on columnNum
    open val columns = emptyList<String>()

    var _constraint: Regex? = null //singleton pattern
    var _searchPhrase: String? = null
    /*only gets regex when the constraint hasn't changed, so that it doesn't create a new regex for every list item*/
    private fun getConstraintRegex(constraint: String): Regex {
        if(constraint == _searchPhrase /*already got regex*/) return _constraint!! /*use "old" regex*/
        /*get new regex*/
        lateinit var regex: Regex
        val replaceHashWithOr = constraint.replace("#", "|")
        regex = try {
            replaceHashWithOr.toRegex(RegexOption.IGNORE_CASE) //if it is a valid regex, go for it
        } catch (t: Throwable) { //probably invalid pattern
            fun String.escapeRegexChars() =
                listOf("\\","(",")","[","]","{","}","?","+","*")
                    .fold(this){acc: String, b: String -> acc.replace(b, "\\$b") }
            replaceHashWithOr.escapeRegexChars().toRegex(RegexOption.IGNORE_CASE)
        }
        _constraint = regex
        _searchPhrase = constraint
        //println("Pattern of constraint: $regex")
        return regex
    }

    val specialChars = "\\s.,-·;:'\"\\[\\]()!?<>&#\\d"
    open fun getCriteria(entry: CatalogEntry): String = ""//has default implementation so that JPanels which don't contain CatalogEntrys (e.g. list of authors) don't need to implement it
    open fun matchesConstraint(element: String, constraint: String) = element.contains(getConstraintRegex(constraint))
    open fun matchesConstraint(element: CatalogEntry, constraint: String): Boolean {
        println("matchesConstraint(element: CatalogEntry, constraint: String")
        if(constraint.isBlank()) return true
        val criteria = getCriteria(element)
        val criteriaContainsHebrew = criteria.containsHebrew()
        println("Criteria is hebrew: $criteriaContainsHebrew")
        val constraintContainsHebrew = constraint.containsHebrew()
        println("Constraint is hebrew: $constraintContainsHebrew")
        if(criteria.isBlank()) return false
        //23.6	הקטן והלכותיו - חינוך · דינים · מנהגים -- א	רקובסקי, ברוך	הוצאת נתיב הברכה	א	1	הלכה - שונות
        return if (constraintContainsHebrew || criteriaContainsHebrew) { //contains hebrew
            val lemmatizedListConstraint = LemmatizerTest.getLemmatizedList(constraint, true, true)
            val lemmatizedListCriteria = LemmatizerTest.getLemmatizedList(criteria, true, true)
            println("Constraint: \n$constraint\nLematized constraint:${lemmatizedListConstraint}")
            println("Criteria: \n$criteria\nLematized criteria:${lemmatizedListCriteria}")
            lemmatizedListCriteria.any { it1 ->
                lemmatizedListConstraint.any {
                    //TODO make checkboxes:
                    val onlyMatchWholeShoresh = true//matches: "לימד" with "מלמד", criteria.equals(lemma),
                    val fuzzyMatch = true//matches: "יות" with "חרב פיפיות", criteria.startsWith(lemma) || criteria.endsWith(lemma
                    val fuzzierMatch = true//criteria.contains(constraint)

                    //Consider the following cases:
                    //Case #1: "יות" as the constraint and criteria being "חרב פיפיות"
                    //Case #2: "t" as the constraint and the criteria being	"טמוני חול - Chullin Illuminated"
                    // ^ do we want the "t" to find illuminated?
                    //Case #3: "מד" as the constraint and the criteria being	"במדבר chumash "
                    //try to match lemmas, which won't work for either of the above cases
                    var matches =
                        (it.equals(it1)).also { isTrue -> if(isTrue) println("\"$it\".equals(\"$it1\")") } ||
                                it1.equals(it).also { isTrue -> if(isTrue) println("\"$it1\".equlas(\"$it\")") }
                    //if either contain english and it didn't match yet, try to see if it contains
                    if(!matches && (it.containsEnglish() || it1.containsEnglish())) {
                        matches = matches || (it.contains(it1)).also { isTrue -> if(isTrue) println("\"$it\".contains(\"$it1\")") } ||
                                it1.contains(it).also { isTrue -> if(isTrue) println("\"$it1\".contains(\"$it\")") }
                    }
                    matches
                }
            }
        } else {
            println("Contains english english")
            criteria.contains(getConstraintRegex(constraint))
        }/*
        else if((constraintIsHebrew && !criteriaIsHebrew) || (!constraintIsHebrew *//*implied && criteriaIsHebrew*//*)) {
            println("They are different languages; returning false")
            false
        }*/
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="expanded" desc="Generated Code">
    fun initComponents(): SearchableTableJPanel {
        jLabel1 = JLabel()
        seferNameTextField = JTextField()
        jScrollPane1 = JScrollPane()
        table = JTable()
        jLabel1.text = searchPhrase
        jLabel2 = JLabel()
        table.model = catalogModel()
        val rightToLeftAlignmentRenderer = DefaultTableCellRenderer()
        rightToLeftAlignmentRenderer.horizontalAlignment = JLabel.RIGHT
        table.columnModel.columns.asIterator().forEach { it.cellRenderer = rightToLeftAlignmentRenderer }
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
        table.font = Font("Default", 0, 14)
        jScrollPane1.setViewportView(table)
        seferNameTextField.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {}

            override fun keyPressed(e: KeyEvent?) {}

            override fun keyReleased(e: KeyEvent?) {
                val constraint = seferNameTextField.text
                filterList(constraint)
            }
        }
        )
//        table.autoCreateRowSorter = true
        val rowSorter = TableRowSorter(table.model as AbstractTableModel)
        val comparator = kotlin.Comparator<String> { o1, o2 ->
            val o1ContainsEnglish = o1.containsEnglish()
            val o2ContainsEnglish = o2.containsEnglish()
            if (o1ContainsEnglish && !o2ContainsEnglish) 1
            else if(!o1ContainsEnglish && o2ContainsEnglish) -1
            else o1.compareTo(o2)
        }
        val columnIndexToSort = minOf(columns.size - 1, 1)//if only 1 column (e.g. authors), index 0, else "name of sefer" column
        rowSorter.setComparator(columnIndexToSort, comparator)
        table.rowSorter = rowSorter
        rowSorter.sortKeys = listOf(RowSorter.SortKey(columnIndexToSort, SortOrder.ASCENDING))
        jLabel2.text = "Results: ${listBeingDisplayed.size}"

        val layout = GroupLayout(this)
        setLayout(layout)
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE.toInt())
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
                        .addContainerGap()
                )
        )
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    layout.createSequentialGroup()
                        .addContainerGap()
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
                        .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE.toInt())
                        .addContainerGap()
                )
        )
        return this
    } // </editor-fold>

    private fun catalogModel() = object : AbstractTableModel() {

        override fun getColumnName(col: Int): String = columns[col] //same as { return columns[col] }

        override fun getRowCount(): Int = listBeingDisplayed.size

        override fun getColumnCount(): Int = columns.size

        override fun getValueAt(row: Int, col: Int): Any {
            return listBeingDisplayed[row].let {
                if(!displayingCatalogEntry) it else {
                    it as CatalogEntry
                    when(col) {
                        0 ->  it.shelfNum
                        1 ->  it.seferName
                        2 ->  it.author
                        3 ->  it.publisher
                        4 ->  it.volumeNum
                        5 ->  it.numCopies
                        6 ->  it.category
                        else -> TODO("This should not have happened: getValueAt($row:, $col)")
                    }
                }
            }
        }

        override fun isCellEditable(row: Int, col: Int): Boolean = false

    }.also { tableModel = it }

    private lateinit var jLabel1: JLabel
    private lateinit var jLabel2: JLabel
    private lateinit var jScrollPane1: JScrollPane
    lateinit var seferNameTextField: JTextField
    lateinit var table: JTable
    lateinit var tableModel: AbstractTableModel
    fun filterList(constraint: String) {
        if(constraint.isBlank()) {
            updateList(originalCollection)
            return
        }
        val newList = Collections.synchronizedList(mutableListOf<Any>())
        originalCollection
//            .parallelStream()
            .filter {
                try {
                    when (it) {
                        is String -> matchesConstraint(it, constraint) //need to have seperate branches to guarantee the compiler of its type
                        is CatalogEntry -> matchesConstraint(it, constraint)
                        else -> TODO("This should never happen, it=$it, it.javaClass = ${it.javaClass}")
                    }
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
        (table.model as AbstractTableModel).fireTableDataChanged()
        jLabel2.text = "Results: ${listBeingDisplayed.size}"
    }
}