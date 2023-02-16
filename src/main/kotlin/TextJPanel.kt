import java.awt.Color
import java.awt.GridLayout
import java.awt.Insets
import java.awt.event.HierarchyEvent
import javax.swing.*

/**
 *
 * @author shmuel
 */
class TextJPanel(val text: String) : JPanel() {
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private fun initComponents() {
        val jTextArea = JTextArea(text).apply {
            isEditable = false
            lineWrap = true
            background = Color(215, 217, 223)
            border = null
            wrapStyleWord = true
            highlighter = null
        }
        this.layout = GridLayout(1, 1)
        add(jTextArea)
        /*val layout = GroupLayout(this)
        this.layout = layout
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(
                            jTextArea,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE,
                            Short.MAX_VALUE.toInt()
                        )
                        .addContainerGap()
                )
        )
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(
                            jTextArea,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE,
                            Short.MAX_VALUE.toInt()
                        )
                        .addContainerGap()
                )
        )*/
    } // </editor-fold>                        

    private fun jTextArea1AncestorResized(evt: HierarchyEvent) {
        jTextArea!!.preferredSize = null
    }

    // Variables declaration - do not modify                     
    private var jScrollPane2: JScrollPane? = null
    private var jTextArea: JTextArea? = null // End of variables declaration

    /**
     * Creates new form TextJPanel
     */
    init {
        initComponents()
    }
}
