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

class MainJFrame : JFrame() {
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="expanded" desc="Generated Code">
    private fun initComponents() {
        println("Initializing program.")
        val startTime = System.nanoTime()
        extendedState = JFrame.MAXIMIZED_BOTH;
        isUndecorated = false;
        jTabbedPane1 = JTabbedPane()
        findSeferByNameJPanel1 = (FindSeferByCriteriaJPanel("Enter name of sefer$alternatePhrases:", { it.seferName }, { it._seferName.second })).also { println("Drawing \"Seforim by name\"") }.initComponents() as FindSeferByCriteriaJPanel
        seforimByCriteriaTabJPanel = TabJPanel(
            listOf(
                "Seforim by author" to (FindSeferByCriteriaJPanel("Enter author of sefer$alternatePhrases:", { it.author }, { it._author.second })).also { println("Drawing \"Seforim by author\"") }.initComponents(),
                "Seforim by category" to (FindSeferByCriteriaJPanel("Enter category of sefer$alternatePhrases:", { it.category })).also { println("Drawing \"Seforim by category\".") }.initComponents(),
                "Seforim by publisher" to (FindSeferByCriteriaJPanel("Enter publisher of sefer$alternatePhrases:", { it.publisher })).also { println("Drawing \"Seforim by publisher\".") }.initComponents(),
                "Seforim by shelf" to (FindSeferByCriteriaJPanel("Enter shelf of sefer$alternatePhrases:", { it.shelfNum })).also { println("Drawing \"Seforim by shelf\".") }.initComponents(),
            )
        )
        criteriaTabJPanel = TabJPanel(
            listOf(
                "Authors" to ListOfAuthorsJPanel().also { println("Drawing list of authors.") }.initComponents(),
                "Categories" to ListOfCategoriesJPanel().also { println("Drawing list of categories.") }.initComponents(),
                "Publishers" to ListOfPublishersJPanel().also { println("Drawing list of publishers.") }.initComponents(),
                "Shelves" to ListOfShelvesJPanel().also { println("Drawing list of shelves.") }.initComponents()
            )
        )
        textJPanel1 = TextJPanel(TIPS)
        val lastUpdatedDate = "02/1/2023"
        textJPanel3 = TextJPanel(
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
                """.trimIndent()
        )
        textJPanel2 = TextJPanel("For tech support, please contact ssternbach@torahdownloads.com; for catalog support, please contact Asher Lewis.")
        refreshDatabaseButton = JButton()
        jLabel1 = JLabel()
        jLabel2 = JLabel()
        defaultCloseOperation = EXIT_ON_CLOSE
        jTabbedPane1!!.addTab("Seforim by name", findSeferByNameJPanel1)
        jTabbedPane1!!.addTab("Seforim by criteria", seforimByCriteriaTabJPanel)
        jTabbedPane1!!.addTab("Criteria", criteriaTabJPanel)
        jTabbedPane1!!.addTab("Tips (7)", textJPanel1)
        val (month, day, year) = lastUpdatedDate.split("/")
        val lastUpdate = LocalDateTime.of(year.toInt(), month.toInt(), day.toInt(), 0, 0, 0)/*Catalog.getDateLastModifiedFromFile(
            catalogDirectory
            .walk()
            .find { it.extension == "jar" }!!
        )*/
        jTabbedPane1!!.addTab(
            "Updates (updated ${
                DateTimeFormatter.ofPattern("MM/dd/yyyy").format(lastUpdate)
            } - ${
                PrettyTime(LocalDateTime.now()).format(lastUpdate)
            })", textJPanel3
        )
        jTabbedPane1!!.addTab("Help", HelpJPanel())
        refreshDatabaseButton!!.text = "Refresh Catalog"

        val getLastUpdateString = { "Catalog last updated: ${Catalog.lastModificationDate()}" }
        jLabel1!!.text = getLastUpdateString()
        jLabel2!!.text = "Program Version: 6.0.0"

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
        println("Total startup time before requesting display: ${(System.nanoTime() - startTime).div(1_000_000_000.00)} seconds")
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
        println("Displaying main screen.")
        pack()
        EventQueue.invokeLater {
            println("Total startup time before sorting: ${(System.nanoTime() - startTime).div(1_000_000_000.00)} seconds")
            scope.launch {
                val timeBeforeSorting = System.nanoTime()
                Catalog.isEntireProgramInitialized.emit(true)
                Catalog.isEntireProgramInitialized.collect {
                    if(!it) {
                        println("Total startup time after sorting: ${(System.nanoTime() - startTime).div(1_000_000_000.00)} seconds")
                        println("Total time to sort: ${(System.nanoTime() - timeBeforeSorting).div(1_000_000_000.00)} seconds")
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
    private var textJPanel1: TextJPanel? = null
    private var textJPanel2: TextJPanel? = null
    private var textJPanel3: TextJPanel? = null // End of variables declaration

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
                    isVisible = true
                }
            }
        }
    }
}

