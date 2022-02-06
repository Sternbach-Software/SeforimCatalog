/***************************************************************************
 *   Copyright (C) 2010-2015 by                                            *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License           *
 *   version 3, as published by the Free Software Foundation.              *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU Affero General Public License for more details.                   *
 *                                                                         *
 *   You should have received a copy of the GNU Affero General Public      *
 *   License along with this program; if not, see                          *
 *   <http://www.gnu.org/licenses/>.                                       *
 **************************************************************************/
package lemmatizer.hebmorph.tests;

import com.code972.hebmorph.HebrewToken;
import com.code972.hebmorph.Reference;
import com.code972.hebmorph.StreamLemmatizer;
import com.code972.hebmorph.Token;

import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LemmatizerTest extends TestBase {
    private static StreamLemmatizer m_lemmatizer;

    public void testLemmatizer() throws IOException {
        String text = "רפאל ולדן הוא פרופסור לרפואה ישראלי, מלמד באוניברסיטת תל אביב, סגן מנהל בית החולים שיבא ופעיל חברתי. מתמחה בכירוגיה כללית ובכלי דם." +
                "ולדן נולד בצרפת ועלה לישראל בגיל 9. הוא שימש בבית החולים שיבא כמנהל האגף לכירורגיה ומנהל היחידה לכלי דם." +
                "ולדן פעיל וחבר בהנהלה בעמותת רופאים לזכויות אדם וכמו כן חבר בהנהלת ארגון לתת. ולדן זכה באות לגיון הכבוד הצרפתי (Légion d'Honneur) של ממשלת צרפת בזכות על פעילותו במסגרת רופאים לזכויות אדם לקידום שיתוף הפעולה בין פלסטינים לישראלים. האות הוענק לו על ידי שר החוץ של צרפת, ברנאר קושנר, בטקס בשגרירות צרפת בתל אביב." +
                "נשוי לבלשנית צביה ולדן, בתו של שמעון פרס והוא משמש כרופאו האישי של פרס.";
        //StringReader reader = new StringReader("להישרדות בהישרדות ההישרדות מהישרדות ניסיון הניסיון הביטוח  בביטוח לביטוח שביטוח מביטוחים");
        int expectedNumberOfNonHebrewWords = 0;
        StringReader reader = new StringReader(text);
        m_lemmatizer = new StreamLemmatizer(reader, getDictionary());

        String word = "";
        List<Token> tokens = new ArrayList<Token>();
        while (m_lemmatizer.getLemmatizeNextToken(new Reference<String>(word), tokens) > 0) {
            if (tokens.size() == 0) {
                System.out.println(word + " Unrecognized word");
                continue;
            }

            if ((tokens.size() == 1) && !(tokens.get(0) instanceof HebrewToken)) {
                System.out.printf("%s Not a Hebrew word; detected as %s%n", word, tokens.get(0).isNumeric() ? "Numeric" : "NonHebrew");

                if (!tokens.get(0).isNumeric() && !word.isEmpty()) {
                    expectedNumberOfNonHebrewWords--;
                }

                continue;
            }

            int curPrefix = -1;
            String curWord = "";
            for (Token r : tokens) {
                if (!(r instanceof HebrewToken))
                    continue;
                HebrewToken ht = (HebrewToken) r;

                if ((curPrefix != ht.getPrefixLength()) || !curWord.equals(ht.getText())) {
                    curPrefix = ht.getPrefixLength();
                    curWord = ht.getText();
                    if (curPrefix == 0)
                        System.out.println(String.format("Legal word: %s (score: %f)", ht.getText(), ht.getScore()));
                    else {
                        System.out.println(String.format("Legal combination: %s+%s (score: %f)", ht.getText().substring(0, curPrefix), ht.getText().substring(curPrefix), ht.getScore()));
                    }
                }
                System.out.println("token: " + ht);
            }
        }


    }
    public void testGetLemmas() throws IOException {
        String message = "Artificial Intelligence search (Malei/Chaseir insensitivity, Shoresh (root word) approximation)";
        String text = "רפאל ולדן הוא פרופסור לרפואה ישראלי, מלמד באוניברסיטת תל אביב, סגן מנהל בית החולים שיבא ופעיל חברתי. מתמחה בכירוגיה כללית ובכלי דם." +
                "ולדן נולד בצרפת ועלה לישראל בגיל 9. הוא שימש בבית החולים שיבא כמנהל האגף לכירורגיה ומנהל היחידה לכלי דם." +
                "ולדן פעיל וחבר בהנהלה בעמותת רופאים לזכויות אדם וכמו כן חבר בהנהלת ארגון לתת. ולדן זכה באות לגיון הכבוד הצרפתי (Légion d'Honneur) של ממשלת צרפת בזכות על פעילותו במסגרת רופאים לזכויות אדם לקידום שיתוף הפעולה בין פלסטינים לישראלים. האות הוענק לו על ידי שר החוץ של צרפת, ברנאר קושנר, בטקס בשגרירות צרפת בתל אביב." +
                "נשוי לבלשנית צביה ולדן, בתו של שמעון פרס והוא משמש כרופאו האישי של פרס and then he did.";

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
//            Catalog.initialize()
//            TabbedJFrame().apply {
//                title = "Seforim Finder"
//                isVisible = true
//            }
            }
        });
        Set<String> lemmatizedList = getLemmatizedList(text, true, true);
        System.out.println(lemmatizedList);
        for(String entry: lemmatizedList) System.out.println("\""+entry+"\"");
        System.out.println();
        System.out.println("List: " + getLemmatizedList("hello my name is asam", true, true));
    }
    /**
     * @param ignorePrefixes if true, for inputs like:
     *           לתת
     *                   it will return
     *                   תת
     * This is in addition to returning the regular lemmas (if any) and the exact word, which will be returned either way.
     * @return a set of both lemmas and the exact words
     * */
    public static Set<String> getLemmatizedList(String text, boolean ignorePrefixes, boolean printLogs) throws IOException {
        //StringReader reader = new StringReader("להישרדות בהישרדות ההישרדות מהישרדות ניסיון הניסיון הביטוח  בביטוח לביטוח שביטוח מביטוחים");
        Set<String> lemmatizedList = new HashSet<>();
//        if(IsBlankChecker.containsLatin(text)) {
//            lemmatizedList.addAll(Arrays.asList(text.split("[\\s,\"?!&.;:()]")));//split by words
//            return lemmatizedList;
//        }
        StringReader reader = new StringReader(text);
        m_lemmatizer = new StreamLemmatizer(reader, getDictionary());
        String word = "";
        List<Token> tokens = new ArrayList<>();
        while (m_lemmatizer.getLemmatizeNextToken(new Reference<>(word), tokens) > 0) {
            /*if(*//*IsBlankChecker.*//*isBlank(word)) {
                System.out.println("was blank");
                continue;
            }*/
            if (tokens.size() == 0 || ((tokens.size() == 1) && !(tokens.get(0) instanceof HebrewToken))) {
                String wordWhichFailed = tokens.size() > 0 ? tokens.get(0).getText() : word;
                if(printLogs) System.out.printf("%s Not a Hebrew word", wordWhichFailed);
                if(!wordWhichFailed.isBlank()) lemmatizedList.add(wordWhichFailed);
                continue;
            }

            int curPrefix = -1;
            String curWord = "";
            int counter = 0;
            for (Token r : tokens) {
                if(printLogs) System.out.println("Counter is " + counter++);
                if (!(r instanceof HebrewToken)){
                    if(printLogs) System.out.println("Token text: " + r.getText());
                    continue;
                }
                HebrewToken ht = (HebrewToken) r;

                if ((curPrefix != ht.getPrefixLength()) || !curWord.equals(ht.getText())) {
                    curPrefix = ht.getPrefixLength();
                    curWord = ht.getText();
                    if (curPrefix == 0) {
                        if(printLogs) System.out.println(String.format("Legal word: %s (score: %f)", ht.getText(), ht.getScore()));
                    }
                    else {
                        String prefix = ht.getText().substring(0, curPrefix);
                        String wordAfterPrefix = ht.getText().substring(curPrefix);
                        if(printLogs) {
                            System.out.println("Prefix: " + prefix);
                            System.out.println("Word after prefix: " + wordAfterPrefix);
                            System.out.println(String.format("Legal combination: %s+%s (score: %f)", prefix, wordAfterPrefix, ht.getScore()));
                        }
                        if(ignorePrefixes && !wordAfterPrefix.isBlank()) lemmatizedList.add(wordAfterPrefix);
                    }
                    String lemma = ht.getLemma();
                    if(lemma != null && !lemma.isBlank()) lemmatizedList.add(lemma);
                    if(!ht.getText().isBlank()) lemmatizedList.add(ht.getText()/*add actual word for exact search*/);
                    if(printLogs) System.out.println("lemma: " + lemma + "\nword: " + ht.getText());
                }
            }
        }
        return lemmatizedList;
    }
    public void testLemmatizerBasic() throws IOException {
        String text = "רפאל ולדן הוא פרופסור לרפואה ישראלי, מלמד באוניברסיטת תל אביב, סגן מנהל בית החולים שיבא ופעיל חברתי. מתמחה בכירוגיה כללית ובכלי דם." +
                "ולדן נולד בצרפת ועלה לישראל בגיל 9. הוא שימש בבית החולים שיבא כמנהל האגף לכירורגיה ומנהל היחידה לכלי דם." +
                "ולדן פעיל וחבר בהנהלה בעמותת רופאים לזכויות אדם וכמו כן חבר בהנהלת ארגון לתת. ולדן זכה באות לגיון הכבוד הצרפתי (Légion d'Honneur) של ממשלת צרפת בזכות על פעילותו במסגרת רופאים לזכויות אדם לקידום שיתוף הפעולה בין פלסטינים לישראלים. האות הוענק לו על ידי שר החוץ של צרפת, ברנאר קושנר, בטקס בשגרירות צרפת בתל אביב." +
                "נשוי לבלשנית צביה ולדן, בתו של שמעון פרס והוא משמש כרופאו האישי של פרס.";
        //StringReader reader = new StringReader("להישרדות בהישרדות ההישרדות מהישרדות ניסיון הניסיון הביטוח  בביטוח לביטוח שביטוח מביטוחים");
        int expectedNumberOfNonHebrewWords = 0;
        StringReader reader = new StringReader(text);
        m_lemmatizer = new StreamLemmatizer(reader, getDictionary());

        String word = "";
        List<Token> tokens = new ArrayList<Token>();
        while (m_lemmatizer.getLemmatizeNextToken(new Reference<String>(word), tokens) > 0) {
            if (tokens.size() == 0 || ((tokens.size() == 1) && !(tokens.get(0) instanceof HebrewToken))) {
                //use english matchesConstraint()
                System.out.printf("%s Not a Hebrew word; detected as %s%n", word, tokens.get(0).isNumeric() ? "Numeric" : "NonHebrew");

                if (!tokens.get(0).isNumeric() && !word.isEmpty()) {
                    expectedNumberOfNonHebrewWords--;
                }

                continue;
            }

            int curPrefix = -1;
            String curWord = "";
            for (Token r : tokens) {
                if (!(r instanceof HebrewToken))
                    continue;
                HebrewToken ht = (HebrewToken) r;

                if ((curPrefix != ht.getPrefixLength()) || !curWord.equals(ht.getText())) {
                    curPrefix = ht.getPrefixLength();
                    curWord = ht.getText();
                    if (curPrefix == 0)
                        System.out.println(String.format("Legal word: %s (score: %f)", ht.getText(), ht.getScore()));
                    else {
                        System.out.println(String.format("Legal combination: %s+%s (score: %f)", ht.getText().substring(0, curPrefix), ht.getText().substring(curPrefix), ht.getScore()));
                    }
                }
                System.out.println("token: " + ht.getText());
            }
        }


    }
}
