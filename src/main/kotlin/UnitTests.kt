import SearchableTableJPanel.Companion.matchesAllOrdered
import SearchableTableJPanel.Companion.matchesAllUnordered
import SearchableTableJPanel.Companion.matchesAny
import lemmatizer.hebmorph.tests.LemmatizerTest
import kotlin.test.assertEquals

class UnitTests {
    val queryShorashim = LemmatizerTest.getLemmatizedList("ספר עקר", true, false).toList()
    val entryShorashim = LemmatizerTest.getLemmatizedList("הגאון הספר העיקרים הגדול", true, false).toList()
    val listOfChecks = mutableListOf<Pair<String, String>>()

    fun test1() {
        assertEquals(queryShorashim, setOf("ספר","עקר").map { setOf(it) })
    }

    fun test2() {
        assertEquals(entryShorashim, setOf("גאן", "ספר", "עקר", "גדל").map { setOf(it) })
    }

    fun test3() {
        matchesAllUnordered(queryShorashim, entryShorashim, listOfChecks)
        assertEquals(
            listOfChecks, listOf(
                "ספר" to "גאן",
                "ספר" to "ספר",
                "עקר" to "גאן",
                "עקר" to "ספר",
                "עקר" to "עקר",
            )
        )
        listOfChecks.clear()
    }

    fun test4() {
        matchesAllOrdered(queryShorashim, entryShorashim, listOfChecks)
        assertEquals(
            listOfChecks, listOf(
                "ספר" to "ספר",
                "עקר" to "עקר",
            )
        )
        listOfChecks.clear()
    }

    fun test5() {
        matchesAny(queryShorashim, entryShorashim, listOfChecks)
        assertEquals(
            listOfChecks, listOf(
                "ספר" to "גאן",
                "ספר" to "ספר",
            )
        )
        listOfChecks.clear()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val x = UnitTests()
            x.test1()
            x.test2()
            x.test3()
            x.test4()
            x.test5()
            println("All tests passing!")
        }
    }
}
