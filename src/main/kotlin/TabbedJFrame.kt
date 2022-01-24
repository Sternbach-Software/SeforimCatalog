import java.awt.EventQueue
import java.awt.Font
import java.awt.event.HierarchyBoundsListener
import java.awt.event.HierarchyEvent
import kotlin.jvm.JvmStatic
import java.lang.ClassNotFoundException
import java.lang.InstantiationException
import java.lang.IllegalAccessException
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.*
val TIPS =
    "Tips: \n" +
            "        1. Clicking on column headers once will sort the list by that criterion in ascending order, clicking again will sort in descending order. \n" +
            "        2. Clicking on a cell (or dragging to select multiple cells) and clicking Ctrl+C will copy the cell's content to the clipboard (pressing Ctrl+V will paste the content). \n" +
            "        3. Separating alternate search phrases or spellings with \"#\" (e.g. \"phrase1#phrase2\") will return results matching any of the phrases. \n" +
            "        4. Dragging the right edge of a column header will resize the column. \n" +
            "        5. English seforim come after hebrew in the sort order, so sort the \"name\" column by descending to see the English seforim displayed before the hebrew seforim.\n" +
            "        6. A quick way to find seforim by a specific author or category when you know the name of the author or category you are looking for is to search the list of authors to find the author's exact spelling in the catalog, click on the author's name to select the cell, press Ctrl+C to copy the name, then press Ctrl+V in the search bar in the \"Seforim by author\" tab to filter the list for seforim by that author."
val alternatePhrases = " (combine searches using \"#\", e.g. \"fire#אור#אש\" or \"(מפרש#מפורש) (סידור#סדור)\")"

class TabbedJFrame : JFrame() {
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private fun initComponents() {
        //TODO add tab called "Tips"
        //TODO when results are empty, add result CatalogEntry("No results","No results",...) and just string
        defaultCloseOperation = EXIT_ON_CLOSE
        jTabbedPane1 = JTabbedPane()
        refreshDatabaseButton = JButton()
        jLabel1 = JLabel()
        jLabel2 = JLabel()
        jScrollPane2 = JScrollPane()
        jTextArea1 = JTextArea()
        jTextArea1.isEditable = false
        jTextArea1.background = UIManager.getDefaults().getColor("Label.background")
        jTextArea1.columns = 20
        jTextArea1.font = Font("Tahoma", 0, 14) // NOI18N
        jTextArea1.lineWrap = true
        jTextArea1.rows = 3
        jTextArea1.text =
            TIPS
        jTextArea1.wrapStyleWord = true
        jTextArea1.border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
        jTextArea1.highlighter = null
        jTextArea1.maximumSize = null
        jTextArea1.isFocusable = false
        jTextArea1.preferredSize = jTextArea1.preferredSize
        jTextArea1.addHierarchyBoundsListener(object : HierarchyBoundsListener {
            override fun ancestorMoved(evt: HierarchyEvent) {}
            override fun ancestorResized(evt: HierarchyEvent) {
                jTextArea1AncestorResized(evt)
            }
        })
        jScrollPane2.setViewportView(jTextArea1)
        jTabbedPane1.addTab(
            "Seforim by name",
            (FindSeferByCriteriaJPanel("Enter name of sefer:") { it.seferName }).initComponents()
        )
        jTabbedPane1.addTab(
            "Seforim by author",
            (FindSeferByCriteriaJPanel("Enter author of sefer:") { it.author }).initComponents()
        )
        jTabbedPane1.addTab(
            "Seforim by category",
            (FindSeferByCriteriaJPanel("Enter category of sefer:") { it.category }).initComponents()
        )
        jTabbedPane1.addTab(
            "Seforim by publisher",
            (FindSeferByCriteriaJPanel("Enter publisher of sefer:") { it.publisher }).initComponents()
        )
        jTabbedPane1.addTab(
            "Seforim by shelf",
            (FindSeferByCriteriaJPanel("Enter shelf of sefer:") { it.shelfNum }).initComponents()
        )
        jTabbedPane1.addTab(
            "Authors",
            ListOfAuthorsJPanel().initComponents()
        )
        jTabbedPane1.addTab(
            "Categories",
            ListOfCategoriesJPanel().initComponents()
        )
        jTabbedPane1.addTab(
            "Publishers",
            ListOfPublishersJPanel().initComponents()
        )
        jTabbedPane1.addTab(
            "Shelves",
            ListOfShelvesJPanel().initComponents()
        )
        refreshDatabaseButton.text = "Refresh Catalog"
        val programVersion = "1.0.0"
        val getLastUpdateString = { "Database last updated: ${Catalog.lastModificationDate()}"}
        jLabel2.text = getLastUpdateString()
        jLabel1.text = "Program version: $programVersion"
//        jLabel3.text = "For tech support, please contact ssternbach@torahdownloads.com; for catalog support, please contact Asher Lewis"

        refreshDatabaseButton.addActionListener {
            Catalog.refreshObjects()
            jLabel2.text = getLastUpdateString()
        }

        val layout = GroupLayout(contentPane)
        contentPane.layout = layout
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane2)
                                .addComponent(jTabbedPane1)
                                .addGroup(
                                    layout.createSequentialGroup()
                                        .addComponent(refreshDatabaseButton)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel2)
                                        .addPreferredGap(
                                            LayoutStyle.ComponentPlacement.RELATED,
                                            27,
                                            Short.MAX_VALUE.toInt()
                                        )
                                        .addComponent(jLabel1)
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
                        .addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, 137, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTabbedPane1, GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE.toInt())
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(refreshDatabaseButton)
                                )
                                .addComponent(jLabel1, GroupLayout.Alignment.TRAILING)
                        )
                        .addContainerGap()
                )
        )
        pack()
    } // </editor-fold>                        
    private fun jTextArea1AncestorResized(evt: HierarchyEvent) {
        jTextArea1.preferredSize = null
    }
    private lateinit var refreshDatabaseButton: JButton
    private lateinit var jLabel1: JLabel
    private lateinit var jLabel2: JLabel
    private lateinit var jLabel3: JLabel
    private lateinit var jTabbedPane1: JTabbedPane
    private lateinit var jScrollPane2: JScrollPane
    private lateinit var jTextArea1: JTextArea
    companion object {
        /**
         * @param args the command line arguments
         */
        @JvmStatic
        fun main(args: Array<String>) {
            /* Set the Nimbus look and feel */
            //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
            /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
//            Arrays.toString(UIManager.getInstalledLookAndFeels()).also { println(it) }
            try {
                for (info in UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus" == info.name) {
                        UIManager.setLookAndFeel(info.className)
                        break
                    }
                }
            } catch (ex: ClassNotFoundException) {
                Logger.getLogger(TabbedJFrame::class.java.name).log(Level.SEVERE, null, ex)
            } catch (ex: InstantiationException) {
                Logger.getLogger(TabbedJFrame::class.java.name).log(Level.SEVERE, null, ex)
            } catch (ex: IllegalAccessException) {
                Logger.getLogger(TabbedJFrame::class.java.name).log(Level.SEVERE, null, ex)
            } catch (ex: UnsupportedLookAndFeelException) {
                Logger.getLogger(TabbedJFrame::class.java.name).log(Level.SEVERE, null, ex)
            }
            //</editor-fold>

            /* Create and display the form */EventQueue.invokeLater {

                Catalog.initialize()
                TabbedJFrame().apply {
                    title = "Seforim Finder"
                    isVisible = true
                }
            }
        }
    }

    /**
     * Creates new form TabbedJFrame
     */
    init {
        initComponents()
    }
}
