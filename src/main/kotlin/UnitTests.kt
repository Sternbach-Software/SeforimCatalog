import SearchableTableJPanel.Companion.matchesAllOrdered
import SearchableTableJPanel.Companion.matchesAllUnordered
import SearchableTableJPanel.Companion.matchesAny
import lemmatizer.hebmorph.tests.Lemmatizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnitTests {
    val queryShorashim = lemmatizer.getLemmatizedList("ספר עקר", true, false).toList()
    val entryShorashim = lemmatizer.getLemmatizedList("הגאון הספר העיקרים הגדול", true, false).toList()
    val listOfChecks = mutableListOf<Pair<String, String>>()

    @Test
    fun test1() {
        assertEquals(queryShorashim, setOf("ספר","עקר").map { setOf(it) })
    }

    @Test
    fun test2() {
        assertEquals(entryShorashim, setOf("גאן", "ספר", "עקר", "גדל").map { setOf(it) })
    }

    @Test
    fun test3() {
        matchesAllUnordered(queryShorashim, entryShorashim, listOfChecks, true)
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

    @Test
    fun test4() {
        assertTrue(matchesAllOrdered(queryShorashim, entryShorashim))
        //should compare: listOf("ספר" to "ספר","עקר" to "עקר",))
    }

    @Test
    fun test5() {
        matchesAny(queryShorashim, entryShorashim, listOfChecks, true)
        assertEquals(
            listOfChecks, listOf(
                "ספר" to "גאן",
                "ספר" to "ספר",
            )
        )
        listOfChecks.clear()
    }

    @Test
    fun columnSortingWorks() {
        val original = mutableListOf<String>()
        for (i in 0..100) {
            for (j in 0..100) {
                original.add("$i.$j")
            }
        }
        original.addAll(original.map { "B$it" })
        assertEquals(original, original.shuffled().sortedWith(shelfNumComparator))
    }
}
