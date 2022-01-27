import Catalog.containsEnglish
import java.awt.Component
import java.awt.ComponentOrientation
import java.awt.Font
import java.awt.event.HierarchyBoundsListener
import java.awt.event.HierarchyEvent
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
) : JPanel() {
    open val originalCollection: Collection<Any> = emptyList()
    open val listBeingDisplayed: MutableList<Any> = mutableListOf()
    open val displayingCatalogEntry: Boolean =
        false //if false, return listBeingDisplayed[rowNum] in table model, otherwise return value depending on columnNum
    open val columns = emptyList<String>()

    var _constraint: Regex? = null //singleton pattern
    var _searchPhrase: String? = null

    /*only gets regex when the constraint hasn't changed, so that it doesn't create a new regex for every list item*/
    private fun getConstraintRegex(constraint: String): Regex {
        if (constraint == _searchPhrase /*already got regex*/ || constraint.lastOrNull() == '#'/*user has not typed alternate phrase, so don't search for whitespace (i.e. every entry)*/) return _constraint!! /*use "old" regex*/
        /*get new regex*/
        lateinit var regex: Regex
        val replaceHashWithOr = constraint.replace("#", "|")
        regex = try {
            replaceHashWithOr.toRegex(RegexOption.IGNORE_CASE) //if it is a valid regex, go for it
        } catch (t: Throwable) { //probably invalid pattern
            fun String.escapeRegexChars() =
                listOf("\\", "(", ")", "[", "]", "{", "}", "?", "+", "*")
                    .fold(this) { acc: String, b: String -> acc.replace(b, "\\$b") }
            replaceHashWithOr.escapeRegexChars().toRegex(RegexOption.IGNORE_CASE)
        }
        _constraint = regex
        _searchPhrase = constraint
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
        getCriteria(element).contains(constraint)

    open fun matchesConstraintNoRegex(element: String, constraint: String): Boolean = element.contains(constraint)
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
        jLabel1 = JLabel()
        seferNameTextField = JTextField()
        jScrollPane1 = JScrollPane()
        table = JTable()
        jLabel1.text = searchPhrase
        jLabel2 = JLabel()
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
        table.font = Font("Default", 0, 14)
//        table.showHorizontalLines = true
        table.showVerticalLines = true
        jScrollPane1.setViewportView(table)
        seferNameTextField.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {}

            override fun keyPressed(e: KeyEvent?) {}

            override fun keyReleased(e: KeyEvent?) {
                var text = seferNameTextField.text
                seferNameTextField.componentOrientation =
                    if(text
                            .firstOrNull()
                            ?.toString()
                            ?.let { it.containsEnglish() || /*if regex*/ it == "~" } == true
                    ) ComponentOrientation.LEFT_TO_RIGHT
                    else ComponentOrientation.RIGHT_TO_LEFT
                filterList(text)
            }
        }
        )
//        table.autoCreateRowSorter = true
        val shelfNumRegex = "\\d+\\.\\d+".toRegex()
        val rowSorter = TableRowSorter(table.model as AbstractTableModel)
        val comparator = kotlin.Comparator<String> { o1, o2 ->
//            if(/*is shelf number*/o1.firstOrNull()?.isDigit()?.and(o2?.lastOrNull()?.isDigit() == true) == true){ //TODO for correcting sort order on shelf nums
//
//            } else {
                val o1ContainsEnglish = o1.containsEnglish()
                val o2ContainsEnglish = o2.containsEnglish()
                if (o1ContainsEnglish && !o2ContainsEnglish) 1
                else if (!o1ContainsEnglish && o2ContainsEnglish) -1
                else (o1.lowercase()).compareTo(o2.lowercase())
//            }
        }
        val columnIndexToSort =
            if (columns.size - 1 != 0) columns.size - 1 else 0//if only 1 column (e.g. authors), index 0, else "name of sefer" column
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
                                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt())
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
                        .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt())
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
                if (!displayingCatalogEntry) it else {
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
    fun filterList(_constraint: String) {
        var constraint = _constraint
        if (constraint.isBlank()) {
            updateList(originalCollection)
            return
        }
        val newList = Collections.synchronizedList(mutableListOf<Any>())
        val firstElement = originalCollection.firstOrNull()
        val needsRegex = constraint.contains('#') || constraint.startsWith('~').also { if(it) constraint = constraint.removePrefix("~")}
        val regex = if (needsRegex) getConstraintRegex(constraint) else null
        val predicate: (Any) -> Boolean =
            if (firstElement is String)
                if (needsRegex) {
                    { matchesConstraint((it as String), constraint, regex!!) }
                } else {
                    { matchesConstraintNoRegex((it as String), constraint) }
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
        (table.model as AbstractTableModel).fireTableDataChanged()
        jLabel2.text = "Results: ${listBeingDisplayed.size}"
    }
}
