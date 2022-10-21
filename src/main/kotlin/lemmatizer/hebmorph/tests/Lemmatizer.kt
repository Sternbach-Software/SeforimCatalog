/***************************************************************************
 * Copyright (C) 2010-2015 by                                            *
 * Itamar Syn-Hershko <itamar at code972 dot com>                     *
 * *
 * This program is free software; you can redistribute it and/or modify  *
 * it under the terms of the GNU Affero General Public License           *
 * version 3, as published by the Free Software Foundation.              *
 * *
 * This program is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 * GNU Affero General Public License for more details.                   *
 * *
 * You should have received a copy of the GNU Affero General Public      *
 * License along with this program; if not, see                          *
 * <http:></http:>//www.gnu.org/licenses/>.                                       *
</itamar> */
package lemmatizer.hebmorph.tests

import Catalog.containsEnglish
import Catalog.containsHebrew
import catalogDirectory
import lemmatizer.hebmorph.HebrewToken
import lemmatizer.hebmorph.Reference
import lemmatizer.hebmorph.StreamLemmatizer
import lemmatizer.hebmorph.Token
import lemmatizer.hebmorph.datastructures.DictHebMorph
import lemmatizer.hebmorph.hspell.HSpellDictionaryLoader
import kotlin.Throws
import java.io.IOException
import java.awt.EventQueue
import java.io.File
import java.io.StringReader
import java.util.ArrayList

class Lemmatizer {

    val dictionary by lazy { HSpellDictionaryLoader().loadDictionaryFromPath(catalogDirectory.absolutePath + File.separatorChar + "hspell-data-files") }

    @Throws(IOException::class)
    fun testLemmatizer() {
        val text =
            "רפאל ולדן הוא פרופסור לרפואה ישראלי, מלמד באוניברסיטת תל אביב, סגן מנהל בית החולים שיבא ופעיל חברתי. מתמחה בכירוגיה כללית ובכלי דם." +
                    "ולדן נולד בצרפת ועלה לישראל בגיל 9. הוא שימש בבית החולים שיבא כמנהל האגף לכירורגיה ומנהל היחידה לכלי דם." +
                    "ולדן פעיל וחבר בהנהלה בעמותת רופאים לזכויות אדם וכמו כן חבר בהנהלת ארגון לתת. ולדן זכה באות לגיון הכבוד הצרפתי (Légion d'Honneur) של ממשלת צרפת בזכות על פעילותו במסגרת רופאים לזכויות אדם לקידום שיתוף הפעולה בין פלסטינים לישראלים. האות הוענק לו על ידי שר החוץ של צרפת, ברנאר קושנר, בטקס בשגרירות צרפת בתל אביב." +
                    "נשוי לבלשנית צביה ולדן, בתו של שמעון פרס והוא משמש כרופאו האישי של פרס."
        //StringReader reader = new StringReader("להישרדות בהישרדות ההישרדות מהישרדות ניסיון הניסיון הביטוח  בביטוח לביטוח שביטוח מביטוחים");
        var expectedNumberOfNonHebrewWords = 0
        val reader = StringReader(text)
        val m_lemmatizer = StreamLemmatizer(reader, dictionary)
        val word = ""
        val tokens: List<Token> = ArrayList()
        while (m_lemmatizer!!.getLemmatizeNextToken(Reference(word), tokens) > 0) {
            if (tokens.size == 0) {
                println("$word Unrecognized word")
                continue
            }
            if (tokens.size == 1 && tokens[0] !is HebrewToken) {
                System.out.printf(
                    "%s Not a Hebrew word; detected as %s%n",
                    word,
                    if (tokens[0].isNumeric) "Numeric" else "NonHebrew"
                )
                if (!tokens[0].isNumeric && !word.isEmpty()) {
                    expectedNumberOfNonHebrewWords--
                }
                continue
            }
            var curPrefix = -1
            var curWord = ""
            for (r in tokens) {
                if (r !is HebrewToken) continue
                val ht = r
                if (curPrefix != ht.prefixLength.toInt() || curWord != ht.text) {
                    curPrefix = ht.prefixLength.toInt()
                    curWord = ht.text
                    if (curPrefix == 0) println(String.format("Legal word: %s (score: %f)", ht.text, ht.score)) else {
                        println(
                            String.format(
                                "Legal combination: %s+%s (score: %f)",
                                ht.text.substring(0, curPrefix),
                                ht.text.substring(curPrefix),
                                ht.score
                            )
                        )
                    }
                }
                println("token: $ht")
            }
        }
    }

    @Throws(IOException::class)
    fun testGetLemmas() {
        val message = "Artificial Intelligence search (Malei/Chaseir insensitivity, Shoresh (root word) approximation)"
        val text =
            "רפאל ולדן הוא פרופסור לרפואה ישראלי, מלמד באוניברסיטת תל אביב, סגן מנהל בית החולים שיבא ופעיל חברתי. מתמחה בכירוגיה כללית ובכלי דם." +
                    "ולדן נולד בצרפת ועלה לישראל בגיל 9. הוא שימש בבית החולים שיבא כמנהל האגף לכירורגיה ומנהל היחידה לכלי דם." +
                    "ולדן פעיל וחבר בהנהלה בעמותת רופאים לזכויות אדם וכמו כן חבר בהנהלת ארגון לתת. ולדן זכה באות לגיון הכבוד הצרפתי (Légion d'Honneur) של ממשלת צרפת בזכות על פעילותו במסגרת רופאים לזכויות אדם לקידום שיתוף הפעולה בין פלסטינים לישראלים. האות הוענק לו על ידי שר החוץ של צרפת, ברנאר קושנר, בטקס בשגרירות צרפת בתל אביב." +
                    "נשוי לבלשנית צביה ולדן, בתו של שמעון פרס והוא משמש כרופאו האישי של פרס and then he did."
        EventQueue.invokeLater {
            //            Catalog.initialize()
//            TabbedJFrame().apply {
//                title = "Seforim Finder"
//                isVisible = true
//            }
        }
        val lemmatizedList = getLemmatizedList(text)
        println(lemmatizedList)
        for (entry in lemmatizedList) println("\"" + entry + "\"")
        println()
        println("List: " + getLemmatizedList("hello my name is asam"))
    }

    @Throws(IOException::class)
    fun testLemmatizerBasic() {
        val text =
            "רפאל ולדן הוא פרופסור לרפואה ישראלי, מלמד באוניברסיטת תל אביב, סגן מנהל בית החולים שיבא ופעיל חברתי. מתמחה בכירוגיה כללית ובכלי דם." +
                    "ולדן נולד בצרפת ועלה לישראל בגיל 9. הוא שימש בבית החולים שיבא כמנהל האגף לכירורגיה ומנהל היחידה לכלי דם." +
                    "ולדן פעיל וחבר בהנהלה בעמותת רופאים לזכויות אדם וכמו כן חבר בהנהלת ארגון לתת. ולדן זכה באות לגיון הכבוד הצרפתי (Légion d'Honneur) של ממשלת צרפת בזכות על פעילותו במסגרת רופאים לזכויות אדם לקידום שיתוף הפעולה בין פלסטינים לישראלים. האות הוענק לו על ידי שר החוץ של צרפת, ברנאר קושנר, בטקס בשגרירות צרפת בתל אביב." +
                    "נשוי לבלשנית צביה ולדן, בתו של שמעון פרס והוא משמש כרופאו האישי של פרס."
        //StringReader reader = new StringReader("להישרדות בהישרדות ההישרדות מהישרדות ניסיון הניסיון הביטוח  בביטוח לביטוח שביטוח מביטוחים");
        var expectedNumberOfNonHebrewWords = 0
        val reader = StringReader(text)
        val m_lemmatizer = StreamLemmatizer(reader, dictionary)
        val word = ""
        val tokens: List<Token> = ArrayList()
        while (m_lemmatizer!!.getLemmatizeNextToken(Reference(word), tokens) > 0) {
            if (tokens.size == 0 || tokens.size == 1 && tokens[0] !is HebrewToken) {
                //use english matchesConstraint()
                System.out.printf(
                    "%s Not a Hebrew word; detected as %s%n",
                    word,
                    if (tokens[0].isNumeric) "Numeric" else "NonHebrew"
                )
                if (!tokens[0].isNumeric && !word.isEmpty()) {
                    expectedNumberOfNonHebrewWords--
                }
                continue
            }
            var curPrefix = -1
            var curWord = ""
            for (r in tokens) {
                if (r !is HebrewToken) continue
                val ht = r
                if (curPrefix != ht.prefixLength.toInt() || curWord != ht.text) {
                    curPrefix = ht.prefixLength.toInt()
                    curWord = ht.text
                    if (curPrefix == 0) println(String.format("Legal word: %s (score: %f)", ht.text, ht.score)) else {
                        println(
                            String.format(
                                "Legal combination: %s+%s (score: %f)",
                                ht.text.substring(0, curPrefix),
                                ht.text.substring(curPrefix),
                                ht.score
                            )
                        )
                    }
                }
                println("token: " + ht.text)
            }
        }
    }

    fun <T> Iterable<T>.toFrequencyMap(): Map<T, Int> {
        val frequencies: MutableMap<T, Int> = mutableMapOf()
        this.forEach { frequencies[it] = frequencies.getOrDefault(it, 0) + 1 }
        return frequencies
    }

    /**
     * @param ignorePrefixes if true, for inputs like:
     * לתת
     * it will return
     * תת
     * This is in addition to returning the regular lemmas (if any) and the exact word, which will be returned either way.
     * @return a set of both lemmas and the exact words
     */
    fun getLemmatizedList(
        text: String?
    ) = getLemmatizedList(text, true, false, true, false, true)

    fun getLemmatizedListPrintLogs(
        text: String?
    ) = getLemmatizedList(text, true, true, true, true, true)

    @Throws(IOException::class)
    fun getLemmatizedList(
        text: String?,
        ignorePrefixes: Boolean,
        printLogs: Boolean,
        removeVavsAndYudsFromLemma: Boolean = true,
        addExactWords: Boolean = false,
        reduceNifalParticiple: Boolean = true,
        includeEnglishInExactSet: Boolean = false,
    ): Set<Set<String>> {
        //StringReader reader = new StringReader("להישרדות בהישרדות ההישרדות מהישרדות ניסיון הניסיון הביטוח  בביטוח לביטוח שביטוח מביטוחים");
        val lemmatizedSetOfSets: MutableSet<Set<String>> = mutableSetOf()
        val lemmatizedSet: MutableSet<String> = mutableSetOf()
        val exactSet: MutableSet<String> = mutableSetOf()
        val lemmatizedList = mutableListOf<String>()
        //        if(IsBlankChecker.containsLatin(text)) {
//            lemmatizedList.addAll(Arrays.asList(text.split("[\\s,\"?!&.;:()]")));//split by words
//            return lemmatizedList;
//        }
        val m_lemmatizer = StreamLemmatizer(StringReader(text), dictionary)
        val word = ""
        val tokens: List<Token> = ArrayList()
        while (m_lemmatizer.getLemmatizeNextToken(Reference(word), tokens) > 0) {
            /*if(*/ /*IsBlankChecker.*/ /*isBlank(word)) {
                System.out.println("was blank");
                continue;
            }*/
            if (tokens.isEmpty() || (tokens.size == 1 && tokens[0] !is HebrewToken)) {
                val wordWhichFailed = if (tokens.isNotEmpty()) tokens[0].text else word
                if (printLogs) println("$wordWhichFailed Not a Hebrew word")
                if (wordWhichFailed.isNotBlank()) {
                    val containsEnglish = wordWhichFailed.containsEnglish()
                    if (!containsEnglish || includeEnglishInExactSet) {
                        exactSet.add(wordWhichFailed)
                    }
                }
                continue
            }
            var curPrefix = -1
            var curWord = ""
            var curLemma = ""
            var counter = 0
            for (r in tokens) {
                if (printLogs) println("Counter is " + counter++)
                if (r !is HebrewToken) {
                    if (printLogs) println("Token text: " + r.text)
                    continue
                }
                val ht = r
                if (curPrefix != ht.prefixLength.toInt() || curWord != ht.text || curLemma != ht.lemma) {
                    curPrefix = ht.prefixLength.toInt()
                    curWord = ht.text
                    ht.lemma?.let { curLemma = it }
                    if (curPrefix == 0) {
                        if (printLogs) println(String.format("Legal word: %s (score: %f)", ht.text, ht.score))
                    } else {
                        val prefix = ht.text.substring(0, curPrefix)
                        val wordAfterPrefix = ht.text.substring(curPrefix)
                        if (printLogs) {
                            println("Prefix: $prefix")
                            println("Word after prefix: $wordAfterPrefix")
                            println(
                                String.format(
                                    "Legal combination: %s+%s (score: %f)",
                                    prefix,
                                    wordAfterPrefix,
                                    ht.score
                                )
                            )
                        }
                        //don't just add the word after the prefix, add the lemma (e.g. given שעיקרו, add עקר not עיקרו)
                        if (ignorePrefixes && wordAfterPrefix.isNotBlank()) {
                            exactSet.add(wordAfterPrefix)
                        }
                    }
                    var lemma = ht.lemma
                    if (lemma == null) lemmatizedSet.add(ht.text)/*nouns don't have lemmas*/
                    if (removeVavsAndYudsFromLemma) {
                        if (printLogs) println("Lemma before sanitization: $lemma")
                        lemma = ht.lemma?.replace("[וי]".toRegex(), "")
                        if (printLogs) println("Lemma after sanitization: $lemma")
                    }
                    if (printLogs) {
                        println("First: ${lemma?.firstOrNull()}")
                        println("Last: ${lemma?.lastOrNull()}")
                    }
                    if (reduceNifalParticiple) {//e.g. נאזר
                        if (lemma?.length == 4 && lemma.firstOrNull() == 'נ') lemma = lemma.substring(1)
                    }
                    if (lemma?.startsWith("הת") == true) lemma = lemma.removePrefix("הת")
                    if (/*lemma is העטר*/lemma?.length == 4 && lemma.firstOrNull() == 'ה') lemma =
                        lemma.substring(1)
                    if (/*lemma is עטרה*/lemma?.length == 4 && lemma.lastOrNull() == 'ה') lemma =
                        lemma.dropLast(1)

                    if (lemma?.isNotBlank() == true) {
                        lemmatizedSet.add(lemma)
                        lemmatizedList.add(lemma)
                    }
                    if (addExactWords && ht.text.isNotBlank() && !ht.text.containsEnglish()) {
                        exactSet.add(ht.text /*add actual word for exact search*/)
                    }
                    if (printLogs) println(
                        """
                                lemma: $lemma
                                word: ${ht.text}
                            """.trimIndent()
                    )
                }
            }
            //new word:
            if (printLogs) {
                println("Starting new word; adding hashset")
                println("lemmatizedSetOfSets=$lemmatizedSetOfSets, lemmatizedSet=$lemmatizedSet")
            }
            if (lemmatizedSet.isNotEmpty()) lemmatizedSetOfSets.add(lemmatizedSet.toSet())
            if (printLogs) {
                println("lemmatizedSetOfSets=$lemmatizedSetOfSets")
                println("Clearing set")
            }
            lemmatizedSet.clear()
            if (printLogs) println("lemmatizedSetOfSets=$lemmatizedSetOfSets")
        }

        //if list contains אמר and מאמר, remove the latter, but if it contains מד and למד, dont remove the latter
        //Commented out because i think maamar should remain that way, because having results for אמר is too verbose and not what the user wants lemmatized, because it is a noun.
        /*val copy = lemmatizedSet.toSet()
        for(it in copy) {
            inner@for(it1 in copy) {
                if(it == it1) continue@inner
                val bigger: String
                val smaller: String
                if (it1.length == 4 && it.length == 3) {
                    bigger = it1
                    smaller = it
                } else {
                    bigger = it
                    smaller = it1
                }
                if (bigger.contains(smaller)) {
                    lemmatizedSet.remove(bigger)
                    lemmatizedList.remove(bigger)
                }
            }
        }*/
        if (printLogs) println("Frequency map: ${lemmatizedList.toFrequencyMap()}")
        return lemmatizedSetOfSets.also { if (addExactWords && exactSet.isNotEmpty()) it.addAll(listOf(exactSet)) }
    }


}