package lemmatizer.lucene.analysis.hebrew;

import lemmatizer.hebmorph.HebrewToken;
import org.apache.lucene.util.Attribute;

/**
 * Reflects the Hebrew Part-of-Speech as detected by HebMorph
 */
public interface HebrewPosAttribute extends Attribute{
    void setHebrewToken(HebrewToken hebToken);

    enum PosTag {
        Unknown,
        Verb,
        Noun,
        ProperNoun,
        Adjective,
    }

    PosTag getPosTag();
}
