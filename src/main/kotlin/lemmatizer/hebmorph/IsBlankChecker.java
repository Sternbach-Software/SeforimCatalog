package lemmatizer.hebmorph;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class IsBlankChecker {

    public static boolean isBlank(String text) {
        return indexOfNonWhitespace(text) == text.length();
    }

    public static int indexOfNonWhitespaceLatin1(byte[] value) {
        int length = value.length;
        int left = 0;
        while (left < length) {
            char ch = (char)(value[left] & 0xff);
            if (ch != ' ' && ch != '\t' && !Character.isWhitespace(ch)) {
                break;
            }
            left++;
        }
        return left;
    }
    public static int length(byte[] value) {
        return value.length >> 1;
    }
    // intrinsic performs no bounds checks
    static char getChar(byte[] val, int index) {
        assert index >= 0 && index < length(val) : "Trusted caller missed bounds check";
        index <<= 1;
        int HI_BYTE_SHIFT;
        int LO_BYTE_SHIFT;
        boolean isBigEndian = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);
        if (isBigEndian) {
            HI_BYTE_SHIFT = 8;
            LO_BYTE_SHIFT = 0;
        } else {
            HI_BYTE_SHIFT = 0;
            LO_BYTE_SHIFT = 8;
        }
        return (char)(((val[index++] & 0xff) << HI_BYTE_SHIFT) |
                ((val[index]   & 0xff) << LO_BYTE_SHIFT));
    }
    private static int codePointAt(byte[] value, int index, int end) {
        assert index < end;
        char c1 = getChar(value, index);
        if (Character.isHighSurrogate(c1) && ++index < end) {
            char c2 = getChar(value, index);
            if (Character.isLowSurrogate(c2)) {
                return Character.toCodePoint(c1, c2);
            }
        }
        return c1;
    }
    public static int indexOfNonWhitespace2(byte[] value) {
        int length = value.length >> 1;
        int left = 0;
        while (left < length) {
            int codepoint = codePointAt(value, left, length);
            if (codepoint != ' ' && codepoint != '\t' && !Character.isWhitespace(codepoint)) {
                break;
            }
            left += Character.charCount(codepoint);
        }
        return left;
    }
    static Pattern latin1 = Pattern.compile("\\p{InLATIN_1_SUPPLEMENT}");
    public static boolean containsLatin(String text) {
        return latin1.matcher(text).find();
    }
    private static int indexOfNonWhitespace(String text) {
        if (containsLatin(text)) {
            return indexOfNonWhitespaceLatin1(text.getBytes(StandardCharsets.UTF_8));
        } else {
            return indexOfNonWhitespace2(text.getBytes(StandardCharsets.UTF_16));
        }
    }
}
