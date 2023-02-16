import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.ocpsoft.prettytime.PrettyTime
import java.awt.EventQueue
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.jvm.JvmStatic
import javax.swing.*

/**
 *
 * @author shmuel
 */
lateinit var logFile: File
lateinit var fontFile: File
val fontFileContents by lazy { fontFile.readText().split(",") }
val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
var rootSearchShouldMatchAll = true
var rootSearchShouldMatchSequential = true
var entireProgramIsInitialized = false //so that filterList() isn't called when all of the components are being initialized and auto-clicked
val TIPS =
    """
    
    Tips: 
        1. Clicking on column headers once will sort the list by that criterion in ascending order, clicking again will sort in descending order. 
        2. Clicking on a cell (or dragging to select multiple cells) and clicking Ctrl+C will copy the cell's content to the clipboard (pressing Ctrl+V will paste the content). 
        3. Dragging the right edge of a column header will resize the column. 
        4. English seforim come after hebrew in the sort order, so sort the "name" column by descending to see the English seforim displayed before the hebrew seforim.
        
        5. Separating alternate search phrases or spellings with "#" will return results for any of those phrases; for example, "(מפרש#מפורש) (סידור#סדור)" will search for all 4 possible variations of that phrase (viz. when the first is malei and the second isn't, vice versa, or when both or neither are malei). Make sure to group spellings in parentheses, because without parentheses "סידור#סדור מפרש#מפורש" will search for 3 phrases instead of 4: סידור, סדור מפרש, or מפורש.
        
        6. A quick way to find seforim by a specific author or category when you know the name of the author or category you are looking for is to search the list of authors to find the author's exact spelling in the catalog, click on the author's name to select the cell, press Ctrl+C to copy the name, then press Ctrl+V in the search bar in the "Seforim by author" tab to filter the list for seforim by that author.
        
        7. You can now learn the layout of the library easily by looking in the "Criteria > Categories" and "Criteria > Shelves" tabs to learn which categories are stored on a particular shelf, and on which shelves you can find a category (if it is spread across multiple shelves)."""
        .replaceIndent("    ")
val alternatePhrases = ""

val startupProgressObservable = Observable(0)
fun incrementStartupProgress(by: Int = 1) {
    println("Startup progress: ${startupProgressObservable.value}, by: $by")
    startupProgressObservable.value = startupProgressObservable.value + by
}

class MainJFrame : JFrame() {
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="expanded" desc="Generated Code">
    private fun initComponents() {
        log("Initializing program.")
        val startTime = System.nanoTime()
        extendedState = JFrame.MAXIMIZED_BOTH;
        isUndecorated = false;
        jTabbedPane1 = JTabbedPane()

        log()
        var now = System.nanoTime()
        val seforimByName = (FindSeferByCriteriaJPanel(
            "Enter name of sefer$alternatePhrases:",
            { it.seferName },
            { it._seferName.second })).also { log("Drawing \"Seforim by name\"") }
        logTime("Time to construct seforim by name:", now)
        log()
        now = System.nanoTime()
        seforimByName.initComponents()
        logTime("Time to init seforim by name:", now)

        log()
        now = System.nanoTime()
        val seforimByAuthor = (FindSeferByCriteriaJPanel(
            "Enter author of sefer$alternatePhrases:",
            { it.author },
            { it._author.second })).also { log("Drawing \"Seforim by author\"") }
        logTime("Time to construct seforim by author:", now)
        log()
        now = System.nanoTime()
        seforimByAuthor.initComponents()
        logTime("Time to init seforim by author:", now)

        log()
        now = System.nanoTime()
        val seforimByCategory = (FindSeferByCriteriaJPanel(
            "Enter category of sefer$alternatePhrases:",
            { it.category })).also { log("Drawing \"Seforim by category\".") }
        logTime("Time to construct seforim by category:", now)

        log()
        now = System.nanoTime()
        seforimByCategory.initComponents()
        logTime("Time to init seforim by category:", now)

        log()
        now = System.nanoTime()
        val seforimByPublisher = (FindSeferByCriteriaJPanel(
            "Enter publisher of sefer$alternatePhrases:",
            { it.publisher })).also { log("Drawing \"Seforim by publisher\".") }
        logTime("Time to construct seforim by publisher:", now)

        log()
        now = System.nanoTime()
        seforimByPublisher.initComponents()
        logTime("Time to init seforim by publisher:", now)

        log()
        now = System.nanoTime()
        val seforimByShelf = (FindSeferByCriteriaJPanel(
            "Enter shelf of sefer$alternatePhrases:",
            { it.shelfNum })).also { log("Drawing \"Seforim by shelf\".") }
        logTime("Time to construct seforim by shelf:", now)

        log()
        now = System.nanoTime()
        seforimByShelf.initComponents()
        logTime("Time to init seforim by shelf:", now)

        findSeferByNameJPanel1 = seforimByName

        seforimByCriteriaTabJPanel = TabJPanel(
            listOf(
                "Seforim by author" to seforimByAuthor,
                "Seforim by category" to seforimByCategory,
                "Seforim by publisher" to seforimByPublisher,
                "Seforim by shelf" to seforimByShelf,
            )
        )
        log()
        now = System.nanoTime()
        val listOfAuthorsJPanel = ListOfAuthorsJPanel()
        logTime("Time to construct listOfAuthors:", now)

        log()
        now = System.nanoTime()
        listOfAuthorsJPanel.initComponents()
        logTime("Time to init listOfAuthors:", now)

        log()
        now = System.nanoTime()
        val listOfCategoriesJPanel = ListOfCategoriesJPanel()
        logTime("Time to construct listOfCategoriesJPanel:", now)

        log()
        now = System.nanoTime()
        listOfCategoriesJPanel.initComponents()
        logTime("Time to init listOfCategoriesJPanel:", now)

        log()
        now = System.nanoTime()
        val listOfPublishersJPanel = ListOfPublishersJPanel()
        logTime("Time to construct listOfPublishersJPanel:", now)

        log()
        now = System.nanoTime()
        listOfCategoriesJPanel.initComponents()
        logTime("Time to init listOfPublishersJPanel:", now)

        log()
        now = System.nanoTime()
        val listOfShelvesJPanel = ListOfShelvesJPanel()
        logTime("Time to construct listOfShelvesJPanel:", now)

        log()
        now = System.nanoTime()
        listOfCategoriesJPanel.initComponents()
        logTime("Time to init listOfShelvesJPanel:", now)

        criteriaTabJPanel = TabJPanel(
            listOf(
                "Authors" to listOfAuthorsJPanel,
                "Categories" to listOfCategoriesJPanel,
                "Publishers" to listOfPublishersJPanel,
                "Shelves" to listOfShelvesJPanel
            )
        )
        resetTime()
//        textJPanel1 = TextJPanel(TIPS)
//        log("Time to create tips tab:")
        val (month, day, year) = listOf(2, 1, 2023)
        val lastUpdatedDate = "$month/$day/$year"
        val lastUpdate = LocalDateTime.of(year, month, day, 0, 0, 0)/*Catalog.getDateLastModifiedFromFile(
            catalogDirectory
            .walk()
            .find { it.extension == "jar" }!!
        )*/
        logTime("Time to create last updated object:")
        val tipsPanel = TextJPanel(TIPS)
        updatesPanel = TextJPanel(
            """
                
                6.0.0 - $lastUpdatedDate
                    > Drastically increase startup time (~15 seconds to ~3 seconds)
                    > Update tips to include how to find seforim easier
                    > Add button to show/hide search mode explanation
                    > Improve shoresh finding algorithm
                    > Clarify rules of root word search
                5.0.0 - 10/2/2022
                    > Add Malei/Chaseir insensitive search
                    > Add "Match whole word" option
                    > Add "Updates" tab
                    > Add column for displaying which categories are stored on a shelf and which shelves a category is stored on (in case it is spread across multiple shelves)
                    > Remove alternate phrase search
                    > Fix exact search for seforim with abbreviations (would sometimes be excluded from results)
                    > Display startup progress in console
                4.0.0 - 02/17/2022
                    > Implement Shoresh/root word search
                """.replaceIndent("    ")
        )
        logTime("Time to create updates panel:")
        val getLastUpdateString = { "Catalog last updated: ${Catalog.lastModificationDate()}" }
        refreshDatabaseButton = JButton("Refresh Catalog")
        jLabel1 = JLabel(getLastUpdateString())
        jLabel2 = JLabel("Program Version: 6.0.0")
        defaultCloseOperation = EXIT_ON_CLOSE
        resetTime()
        val updatedString = "Updates (updated ${
            DateTimeFormatter.ofPattern("MM/dd/yyyy").format(lastUpdate)
        } - ${
            PrettyTime(LocalDateTime.now()).format(lastUpdate)
        })"
        logTime("Time to calculate updated string:")
        val startTimeTabs = System.nanoTime()
        jTabbedPane1!!.addTab("Seforim by name", findSeferByNameJPanel1)
        logTime("Time to add seforim by name:")
        jTabbedPane1!!.addTab("Seforim by criteria", seforimByCriteriaTabJPanel)
        logTime("Time to add seforim by criteria:")
        jTabbedPane1!!.addTab("Criteria", criteriaTabJPanel)
        logTime("Time to add Criteria:")
        jTabbedPane1!!.addTab("Tips (7)", tipsPanel)
        logTime("Time to add Tips:")
        jTabbedPane1!!.addTab(updatedString, updatesPanel)
        logTime("Time to add Updates tab:")
        jTabbedPane1!!.addTab("Help", HelpJPanel())
        logTime("Time to add Help tab:")
        logTime("Time to add all tabs:", startTimeTabs)
        refreshDatabaseButton!!.addActionListener {
            Catalog.refreshObjects()
            jLabel1!!.text = getLastUpdateString()
            //LevenshteinDistance()
            (0 until jTabbedPane1!!.tabCount).map { jTabbedPane1!!.getTabComponentAt(it) }.forEach {
                if (it is SearchableTableJPanel) {
                    try {
                        it.originalCollection.clear()
                        it.originalCollection.addAll(it.getOriginalList())
                        it.filterList()
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
            }
        }
        logTime("Total startup time before requesting display:", startTime)
        val layout = GroupLayout(contentPane)
        contentPane.layout = layout
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(jTabbedPane1)
                                .addGroup(
                                    layout.createSequentialGroup()
                                        .addComponent(refreshDatabaseButton)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel1)
                                        .addPreferredGap(
                                            LayoutStyle.ComponentPlacement.RELATED,
                                            GroupLayout.DEFAULT_SIZE,
                                            Short.MAX_VALUE.toInt()
                                        )
                                        .addComponent(jLabel2)
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
                        .addComponent(jTabbedPane1, GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE.toInt())
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(refreshDatabaseButton)
                                .addComponent(jLabel1)
                                .addComponent(jLabel2)
                        )
                        .addContainerGap()
                )
        )
        logTime("Time to layout:")
        log("Displaying main screen.")
        pack()
        logTime("Time to pack:")
        EventQueue.invokeLater {
            logTime("Total startup time before sorting:", startTime)
            scope.launch {
                val timeBeforeSorting = System.nanoTime()
                Catalog.isEntireProgramInitialized.emit(true)
                Catalog.isEntireProgramInitialized.collect {
                    if(!it) {
                        logTime("Total startup time after sorting:", startTime)
                        logTime("Total time to sort:", timeBeforeSorting)
                    }
                }
            }
            entireProgramIsInitialized = true
        }
    } // </editor-fold>

    // Variables declaration - do not modify                     
    private var findSeferByNameJPanel1: FindSeferByCriteriaJPanel? = null
    private var refreshDatabaseButton: JButton? = null
    private var jLabel1: JLabel? = null
    private var jLabel2: JLabel? = null
    private var jTabbedPane1: JTabbedPane? = null
    private var seforimByCriteriaTabJPanel: TabJPanel? = null
    private var criteriaTabJPanel: TabJPanel? = null
    private var updatesPanel: TextJPanel? = null // End of variables declaration

    /**
     * Creates new form MainJFrame
     */
    init {
        initComponents()
    }

    companion object {
        /**
         * @param args the command line arguments
         */
        @JvmStatic
        fun main(args: Array<String>) {
            // Set the Nimbus look and feel
            // For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
            UIManager.setLookAndFeel(UIManager.getInstalledLookAndFeels().find { it.name == "Nimbus" }?.className)
            /*ProgressDialogJFrame(
                "a",
                "Startup progress",
                100,
                startupProgressObservable
            ).isVisible = true*/
            val firstArg = args.getOrNull(0)
            if (firstArg != null) {
                catalogDirectory = File(firstArg)
                fontFile = File(catalogDirectory, "table_font.txt")
                logFile = File(catalogDirectory, "logs.txt")
            } else fontFile = File(catalogDirectory, "table_font.txt")
            /* Create and display the form */EventQueue.invokeLater {
                MainJFrame().apply {
                    title = "Seforim Finder"
                    args.getOrNull(1)?.let { iconImage = ImageIcon(it).image }
                    args.getOrNull(2)?.toBooleanStrictOrNull()?.let { logging = it }
                    isVisible = true
                }
            }
        }
    }
}

