import SearchableTableJPanel.Companion.matchesAllOrdered
import SearchableTableJPanel.Companion.matchesAllUnordered
import SearchableTableJPanel.Companion.matchesAny
import lemmatizer.hebmorph.tests.LemmatizerTest

fun main() {
    val queryShorashim = LemmatizerTest.getLemmatizedList("ספר עקר", true, false).toList()
    val entryShorashim = LemmatizerTest.getLemmatizedList("הגאון הספר העיקרים הגדול", true, false).toList()
    val listOfChecks = mutableListOf<String>()
    println("Query shorashim: $queryShorashim")
    println("Entry shorashim: $entryShorashim")
}/*{
//    LemmatizerTest().testGetLemmas()
    val result = LemmatizerTest.getLemmatizedList(
        "למד" +
                " לימד" +
                " ילמד" +
                " נלמד" +
                " נלמדה" +
                " ילמדו" +
                " תלמד" +
                " תלמדי" +
                " מלמד" +
                " שלימד" +
                " שלמד" +
                " שלמדו" +
                "למדו" +
                "לומדים" +
                "לומדות" +
                "ילמדנו" +
                "תלמדו" +
                "תלמדנה" +
                "תלמוד" +
                "התלמד" +
                "נתלמד" +
                "נתלמדו" +
                "התלמדו" +
                "למדני" +
                "למדנו" +
                "נתלמדנו" +
                "נתלמדו" +
                "התלמדו",
        true,
        true
    )
    println(result)
    println()
    println()
    println()
    println()

    val result1 = LemmatizerTest.getLemmatizedList(
        "אמר" +
                " אימר" +
                " נאמר" +
                " נאמרה" +
                " יאמר" +
                " אמרה" +
                " יאמרו" +
                " תאמר" +
                " תאמרי" +
                " מאמר" +
                " שאימר" +
                " אימרה" +
                " אמירה" +
                " שאמר" +
                " שאמרו" +
                "אמרו" +
                "אומרו" +
                "אומרות" +
                "אומרים" +
                "תאמרו" +
                "תאמרנה" +
                "תאמור" +
                "התאמר" +
                "נתאמר" +
                "נתאמרו" +
                "התאמרו" +
                "אמרני" +
                "אמרנו" +
                "נתאמרנו" +
                "נתאמרו" +
                "התאמרו",
        true,
        true
    )
    println()
    println(result1)
    println()
    println()
    println()
    println()

    val result2 = LemmatizerTest.getLemmatizedList(
        "עקר" +
                " עיקר" +
                " נעקר" +
                " נעקרה" +
                " יעקר" +
                " יעקרו" +
                " עוקרו" +
                " עוקרים" +
                " עוקרות" +
                " עקרה" +
                " תעקר" +
                " תעקרי" +
                " מעקר" +
                " שעיקר" +
                " שעיקרו" +
                " שעקר" +
                " שעקרו" +
                "עקרו" +
                "תעקרו" +
                "תעקרנה" +
                "תעקור" +
                "התעקר" +
                "נתעקר" +
                "נתעקרו" +
                "התעקרנו" +
                "עקרני" +
                "עקרנו" +
                "עיקרנו" +
                "נתעקרנו" +
                "נתעקרו" +
                "התעקרו",
        true,
        true
    )
    println()
    println(result2)
    println("Madad: " + LemmatizerTest.getLemmatizedList(" מודדים מודדו מודדות מדד מודד מדדו למדוד למודד מדדו ימודדו ימודד", false, true,))
    println("mamar: " + LemmatizerTest.getLemmatizedList("מאמר וממאמר וממאמרי וממאמרים ומאמרותיהם וממאמריהם וממאמרותהם", true, true,))
    println("lamad: " + LemmatizerTest.getLemmatizedList(" ילמדינו ילמדנו", true, true,))//TODO fails
    println("atar: " + LemmatizerTest.getLemmatizedList(" עיטור", true, true,))
    println("hatar: " + LemmatizerTest.getLemmatizedList(" העטרה ", true, true,))
    println("batar: " + LemmatizerTest.getLemmatizedList(" בעטר", true, true,))
    println("batar: " + LemmatizerTest.getLemmatizedList(" מעטר", true, true,))
    println("batar: " + LemmatizerTest.getLemmatizedList("א. מחברת מנחם / ב. תשובות דונש / ג. תשובות תלמידי מנחם / ד. תשובות דונש על רס״ג", true, true,))
    println("---------------------")
    println("batar: " + LemmatizerTest.getLemmatizedList("בגבורה", true, true,))
    println("---------------------")
    println("batar: " + LemmatizerTest.getLemmatizedList("בגבורתו", true, true,))
//    println("adjectives: " + LemmatizerTest.getLemmatizedList(" יפיפיה מפואר נהדר מדהים נאזר עטוף אדום כחול אדיר אמור", true, true,))//TODO fails
}*/
//after sanitization: (?!(למד|אמר|עקר))