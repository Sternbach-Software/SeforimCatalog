import lemmatizer.hebmorph.tests.LemmatizerTest

fun main() {
//    LemmatizerTest().testGetLemmas()
    LemmatizerTest.getLemmatizedList("חרב פפיות", true, true).also { println(it) }
}