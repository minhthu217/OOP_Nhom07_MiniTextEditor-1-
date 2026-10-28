package vn.edu.ptit.oop.nhom07.service;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;

import vn.edu.ptit.oop.nhom07.model.AnalysisResult;

public class CharCategoryAnalyzer implements TextAnalyzer {
	public static final String LETTERS = "Chữ cái";
    public static final String DIGITS = "Chữ số";
    public static final String WHITESPACE = "Khoảng trắng";
    public static final String OTHER = "Ký tự khác";

    @Override
    public Map<String, Integer> analyze(String text) {
        Map<String, Integer> result = new LinkedHashMap<>();
        result.put(LETTERS, 0);
        result.put(DIGITS, 0);
        result.put(WHITESPACE, 0);
        result.put(OTHER, 0);

        if (text == null || text.isEmpty()) {
            return result;
        }

        String normalized = Normalizer.normalize(text, Normalizer.Form.NFC);

        for (int i = 0; i < normalized.length();) {
            int codePoint = normalized.codePointAt(i);
            i += Character.charCount(codePoint);

            if (Character.isLetter(codePoint)) {
                result.put(LETTERS, result.get(LETTERS) + 1);
            } else if (Character.isDigit(codePoint)) {
                result.put(DIGITS, result.get(DIGITS) + 1);
            } else if (Character.isWhitespace(codePoint)
                    || Character.isSpaceChar(codePoint)) {
                result.put(WHITESPACE, result.get(WHITESPACE) + 1);
            } else {
                result.put(OTHER, result.get(OTHER) + 1);
            }
        }

        return result;
    }

}
