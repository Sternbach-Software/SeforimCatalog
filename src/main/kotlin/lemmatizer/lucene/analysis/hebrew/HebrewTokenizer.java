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
package lemmatizer.lucene.analysis.hebrew;

import lemmatizer.hebmorph.Reference;
import lemmatizer.hebmorph.datastructures.DictRadix;
import org.apache.lucene.analysis.Tokenizer;
import lemmatizer.lucene.analysis.hebrew.HebrewTokenTypeAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;
import java.util.HashMap;

/**
 * Tokenizes a given stream using HebMorph's Tokenizer, removes prefixes where possible, and tags Tokens
 * with appropriate types where possible
 */
public final class HebrewTokenizer extends Tokenizer {

    private final lemmatizer.hebmorph.Tokenizer hebMorphTokenizer;
    private final HashMap<String, Integer> prefixesTree;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final HebrewTokenTypeAttribute hebTypeAtt = addAttribute(HebrewTokenTypeAttribute.class);

    public HebrewTokenizer(HashMap<String, Integer> prefixes) {
        this(prefixes, null);
    }

    public HebrewTokenizer(final HashMap<String, Integer> _prefixesTree, final DictRadix<Byte> specialCases) {
        super();
        hebMorphTokenizer = new lemmatizer.hebmorph.Tokenizer(input, _prefixesTree, specialCases);
        prefixesTree = _prefixesTree;
    }

    public void setSuffixForExactMatch(final Character suffixForExactMatch) {
        this.hebMorphTokenizer.setSuffixForExactMatch(suffixForExactMatch);
    }

    public interface TOKEN_TYPES {
        int Hebrew = 0;
        int NonHebrew = 1;
        int Numeric = 2;
        int Construct = 3;
        int Acronym = 4;
        int Mixed = 5;
    }

    public static final String[] TOKEN_TYPE_SIGNATURES = new String[]{
            "<HEBREW>",
            "<NON_HEBREW>",
            "<NUM>",
            "<CONSTRUCT>",
            "<ACRONYM>",
            "<MIXED>",
            null
    };

    public static String tokenTypeSignature(final int tokenType) {
        return TOKEN_TYPE_SIGNATURES[tokenType];
    }

    @Override
    public boolean incrementToken() throws IOException {
        clearAttributes();

        final Reference<String> nextToken = new Reference<String>(null);
        String nextTokenVal = null;
        int tokenType;

        // Used to loop over certain noise cases
        while (true) {
            tokenType = hebMorphTokenizer.nextToken(nextToken);
            nextTokenVal = nextToken.ref;

            if (tokenType == 0)
                return false; // EOS

            if ((tokenType & lemmatizer.hebmorph.Tokenizer.TokenType.Hebrew) > 0 && prefixesTree != null) {
                // Ignore "words" which are actually only prefixes in a single word.
                // This first case is easy to spot, since the prefix and the following word will be
                // separated by a dash marked as a construct (סמיכות) by the Tokenizer
                if ((tokenType & lemmatizer.hebmorph.Tokenizer.TokenType.Construct) > 0) {
                    if (isLegalPrefix(nextToken.ref))
                        continue;
                }

                // This second case is a bit more complex. We take a risk of splitting a valid acronym or
                // abbrevated word into two, so we send it to an external function to analyze the word, and
                // get a possibly corrected word. Examples for words we expect to simplify by this operation
                // are ה"שטיח", ש"המידע.
                if ((tokenType & lemmatizer.hebmorph.Tokenizer.TokenType.Acronym) > 0) {
                    nextTokenVal = nextToken.ref = tryStrippingPrefix(nextToken.ref);

                    // Re-detect acronym, in case it was a false positive
                    if (nextTokenVal.indexOf('"') == -1) {
                        tokenType &= ~lemmatizer.hebmorph.Tokenizer.TokenType.Acronym;
                    }
                }
            }

            break;
        }

        // Record the term string
        termAtt.copyBuffer(nextTokenVal.toCharArray(), 0, nextTokenVal.length());
        offsetAtt.setOffset(correctOffset(hebMorphTokenizer.getOffset()), correctOffset(hebMorphTokenizer.getOffset() + hebMorphTokenizer.getLengthInSource()));
        if ((tokenType & lemmatizer.hebmorph.Tokenizer.TokenType.Exact) > 0)
            hebTypeAtt.setExact(true);

        if ((tokenType & lemmatizer.hebmorph.Tokenizer.TokenType.Hebrew) > 0) {
            if ((tokenType & lemmatizer.hebmorph.Tokenizer.TokenType.Acronym) > 0) {
                hebTypeAtt.setType(HebrewTokenTypeAttribute.HebrewType.Acronym);
            } else if ((tokenType & lemmatizer.hebmorph.Tokenizer.TokenType.Construct) > 0) {
                hebTypeAtt.setType(HebrewTokenTypeAttribute.HebrewType.Construct);
            } else {
                hebTypeAtt.setType(HebrewTokenTypeAttribute.HebrewType.Hebrew);
            }
        } else if ((tokenType & lemmatizer.hebmorph.Tokenizer.TokenType.Numeric) > 0) {
            hebTypeAtt.setType(HebrewTokenTypeAttribute.HebrewType.Numeric);
        } else {
            hebTypeAtt.setType(HebrewTokenTypeAttribute.HebrewType.NonHebrew);
        }

        return true;
    }

    @Override
    public void end() throws IOException {
        super.end();
        // set final offset
        int finalOffset = correctOffset(hebMorphTokenizer.getOffset());
        offsetAtt.setOffset(finalOffset, finalOffset);
    }

    @Override
    public void close() throws IOException {
        super.close();
        hebMorphTokenizer.reset(input);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        hebMorphTokenizer.reset(input);
    }

    public boolean isLegalPrefix(final String str) {
        return prefixesTree.containsKey(str);
    }

    // See the Academy's punctuation rules (see לשוננו לעם, טבת, תשס"ב) for an explanation of this rule
    public String tryStrippingPrefix(String word) {
        // TODO: Make sure we conform to the academy rules as closely as possible

        int firstQuote = word.indexOf('"');

        if (firstQuote > -1 && firstQuote < word.length() - 2) {
            if (isLegalPrefix(word.substring(0, firstQuote))) {
                return word.substring(firstQuote + 1, firstQuote + 1 + word.length() - firstQuote - 1);
            }
        }

        int firstSingleQuote = word.indexOf('\'');
        if (firstSingleQuote == -1) {
            return word;
        }

        if ((firstQuote > -1) && (firstSingleQuote > firstQuote)) {
            return word;
        }

        if (isLegalPrefix(word.substring(0, firstSingleQuote))) {
            return word.substring(firstSingleQuote + 1, firstSingleQuote + 1 + word.length() - firstSingleQuote - 1);
        }

        return word;
    }
}
