/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.SwingDispatcher
import java.awt.EventQueue
import java.awt.Font
import java.awt.Toolkit
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.*

/**
 * Multi-line scrolling message box with "don't show again" button that edits [shouldShowCompleteBox] and has an "OK" button
 * @author shmuel
 */
class ProgressDialogJFrame(
    private val message: String? = null,
    private val mtitle: String,
    private val numTasks: Int? = null,
    private val currentProgress: Observable<Int>? = null,
    private val printToScreenObservable: Observable<String>? = null,
) : JFrame() {
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private fun initComponents() {
        title = mtitle
        jScrollPane1 = JScrollPane().apply { isVisible = false }
        jTextArea1 = JTextArea()
        jPanel1 = JPanel()
        jLabel1 = JLabel("Startup progress:")
        progressBar = JProgressBar(0, 100)
        totalNumCompleteLabel = JLabel()
        printToScreenObservable?.observe(onPrintToScreenObserver)
        currentProgress?.observe(onProgressReceivedObserver)
        defaultCloseOperation = EXIT_ON_CLOSE
        jTextArea1!!.text = message
        jTextArea1!!.lineWrap = true
        jTextArea1!!.wrapStyleWord = true
        jTextArea1!!.isEditable = false
        jTextArea1!!.font = Font("Calibri", Font.BOLD, 18)
        jScrollPane1!!.setViewportView(jTextArea1)
//        dismissButton!!.addActionListener {
//            isVisible = false
//            dispose()
//        }
        totalNumCompleteLabel!!.text = getProgressString(0, 0, numTasks ?: 0)
        val jPanel1Layout = GroupLayout(jPanel1)
        jPanel1!!.layout = jPanel1Layout
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(
                                    jPanel1Layout.createSequentialGroup()
                                        .addGroup(
                                            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                                                .addComponent(jLabel2)
                                                .addComponent(jLabel1)
                                        )
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(
                                            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addComponent(
                                                    progressBar,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    371,
                                                    Short.MAX_VALUE.toInt()
                                                )
//                                                .addComponent(
//                                                    currentShiurUploadProgressBar,
//                                                    GroupLayout.DEFAULT_SIZE,
//                                                    GroupLayout.DEFAULT_SIZE,
//                                                    Short.MAX_VALUE.toInt()
//                                                )
                                        )
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(
                                            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                .addComponent(
                                                    totalNumCompleteLabel,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE.toInt()
                                                )
//                                                .addComponent(
//                                                    currentUploadProgressLabel,
//                                                    GroupLayout.PREFERRED_SIZE,
//                                                    190,
//                                                    GroupLayout.PREFERRED_SIZE
//                                                )
                                        )
                                )
//                                .addGroup(
//                                    jPanel1Layout.createSequentialGroup()
//                                        .addComponent(jLabel3)
//                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                        .addComponent(currentShiurBeingUploadedLabel)
//                                        .addGap(0, 0, Short.MAX_VALUE.toInt())
//                                )
                        )
                        .addContainerGap()
                )
        )
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel1)
                                .addComponent(
                                    progressBar,
                                    GroupLayout.PREFERRED_SIZE,
                                    GroupLayout.DEFAULT_SIZE,
                                    GroupLayout.PREFERRED_SIZE
                                )
                                .addComponent(totalNumCompleteLabel)
                        )
//                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                        .addGroup(
//                            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                                .addComponent(jLabel2)
//                                .addComponent(
//                                    currentShiurUploadProgressBar,
//                                    GroupLayout.PREFERRED_SIZE,
//                                    GroupLayout.DEFAULT_SIZE,
//                                    GroupLayout.PREFERRED_SIZE
//                                )
//                                .addComponent(currentUploadProgressLabel)
//                        )
//                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                        .addGroup(
//                            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                .addComponent(jLabel3)
//                                .addComponent(currentShiurBeingUploadedLabel)
//                        )
                        .addContainerGap()
                )
        )
        val layout = GroupLayout(contentPane)
        contentPane.layout = layout
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                                .addGroup(
//                                    GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
//                                        .addGap(0, 0, Short.MAX_VALUE.toInt())
//                                        .addComponent(dismissButton)
//                                )
                                .addComponent(jScrollPane1)
                                .addComponent(
                                    jPanel1,
                                    GroupLayout.DEFAULT_SIZE,
                                    GroupLayout.DEFAULT_SIZE,
                                    Short.MAX_VALUE.toInt()
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
                        .addComponent(
                            jPanel1,
                            GroupLayout.PREFERRED_SIZE,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE.toInt())
                        .addGap(10, 10, 10)
//                        .addGroup(
//                            layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                .addComponent(dismissButton)
//                        )
                        .addContainerGap()
                )
        )
        pack()
    } // </editor-fold>

    private fun Int.asPercent(total: Int): Int =
        if (this == 0) 0 else (this / total.toDouble()).times(100).toInt().coerceAtMost(100)

    private fun Long.asPercent(total: Long): Int =
        if (this == 0L) 0 else (this / total.toDouble()).times(100).toInt().coerceAtMost(100)

    private fun getProgressString(currentNumPercent: Int, currentNum: Int, totalNum: Int) =
        "$currentNumPercent%"

    fun showDialog() {
        EventQueue.invokeLater {
            isVisible = true
        }
    }

    override fun dispose() {
        printToScreenObservable?.removeObserver(onPrintToScreenObserver)
        currentProgress?.removeObserver(onProgressReceivedObserver)
        super.dispose()
    }

    // Variables declaration - do not modify
    private var jLabel1: JLabel? = null
    private var jPanel1: JPanel? = null
    private lateinit var progressBar: JProgressBar
    private var currentShiurUploadProgressBar: JProgressBar? = null
    private var jScrollPane1: JScrollPane? = null
    private var jTextArea1: JTextArea? = null // End of variables declaration                   
    private var totalNumCompleteLabel: JLabel? = null // End of variables declaration

    val onProgressReceivedObserver: (Int) -> Unit = {
        kotlin.runCatching {
            val currentNumPercent = it.asPercent(numTasks!!)
                println("on progress received: $it, percent: $currentNumPercent")
                totalNumCompleteLabel!!.text = "${it}%" // getProgressString(currentNumPercent, it, numTasks)
//                jTextArea1?.text = jTextArea1?.text?.plus("\n$it")
                progressBar.value = currentNumPercent
                if(it == numTasks) dispose()
        }.exceptionOrNull()?.printStackTrace()
    }
    val onPrintToScreenObserver: (String) -> Unit = {
        kotlin.runCatching {
            jTextArea1?.text = jTextArea1?.text?.plus("\n$it")
        }
    }

    /**
     * Creates new form MessageBoxJFrame
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
            /* Set the Nimbus look and feel */
            //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
            /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
            try {
                for (info in UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus" == info.name) {
                        UIManager.setLookAndFeel(info.className)
                        break
                    }
                }
            } catch (ex: ClassNotFoundException) {
                Logger.getLogger(ProgressDialogJFrame::class.java.name).log(Level.SEVERE, null, ex)
            } catch (ex: InstantiationException) {
                Logger.getLogger(ProgressDialogJFrame::class.java.name).log(Level.SEVERE, null, ex)
            } catch (ex: IllegalAccessException) {
                Logger.getLogger(ProgressDialogJFrame::class.java.name).log(Level.SEVERE, null, ex)
            } catch (ex: UnsupportedLookAndFeelException) {
                Logger.getLogger(ProgressDialogJFrame::class.java.name).log(Level.SEVERE, null, ex)
            }
            //</editor-fold>

            val observable = Observable<Int>()
            /* Create and display the form */EventQueue.invokeLater {
                ProgressDialogJFrame(
                    "fnoewfnewoifnoweifnoweifnoiwenfoinewoifnweofnoewn fweo voew voeiw voiew vowe vowe v weo vowe v weo vwoviw weoi voiwe oweoiv oiwevoiwe ov ewov eoiwv oew ovwe voweiowe owev oiwe voiwe oivw oiv woew oe evwoiv ewoi vowe vw owe iov wowioviowiofoiefew eowe owe jojo w ofj ewj f owejowejof oew fjo efwoij weoi efwoj fowj fweo jfew iowe jie iow jeijo fwoej f weio fio wjiefjio ewio fjiwe oiew jij iowef jio jiow jioew jioew io oe lwe owe jiofwe jiofkl",
                    "Title",
                    100,
                    observable,
                ).isVisible = true

                scope.launch {
                    (0.. 100).asFlow().collect {
                        delay(1_0)
                        observable.value = it
                    }
                }
            }
        }
    }
}