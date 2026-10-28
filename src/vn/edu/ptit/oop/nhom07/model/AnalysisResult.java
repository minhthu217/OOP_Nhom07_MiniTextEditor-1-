package vn.edu.ptit.oop.nhom07.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import vn.edu.ptit.oop.nhom07.service.CharCategoryAnalyzer;

public class AnalysisResult {
	private int wordCount;
    private int charCount;
    private int lineCount;
    private Map<String, Integer> wordFrequencies;
    private Map<String, Integer> charCategories;

    public AnalysisResult() {
        this(0, 0, 0, Collections.emptyMap(), defaultCategories());
    }

    public AnalysisResult(int wordCount, int charCount, int lineCount,
                          Map<String, Integer> wordFrequencies,
                          Map<String, Integer> charCategories) {
        this.wordCount = Math.max(0, wordCount);
        this.charCount = Math.max(0, charCount);
        this.lineCount = Math.max(0, lineCount);
        setWordFrequencies(wordFrequencies);
        setCharCategories(charCategories);
    }

    private static Map<String, Integer> defaultCategories() {
        Map<String, Integer> categories = new LinkedHashMap<>();
        categories.put(CharCategoryAnalyzer.LETTERS, 0);
        categories.put(CharCategoryAnalyzer.DIGITS, 0);
        categories.put(CharCategoryAnalyzer.WHITESPACE, 0);
        categories.put(CharCategoryAnalyzer.OTHER, 0);
        return categories;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = Math.max(0, wordCount);
    }

    public int getCharCount() {
        return charCount;
    }

    public void setCharCount(int charCount) {
        this.charCount = Math.max(0, charCount);
    }

    public int getLineCount() {
        return lineCount;
    }

    public void setLineCount(int lineCount) {
        this.lineCount = Math.max(0, lineCount);
    }

    public Map<String, Integer> getWordFrequencies() {
        return Collections.unmodifiableMap(wordFrequencies);
    }

    public void setWordFrequencies(Map<String, Integer> wordFrequencies) {
        this.wordFrequencies = wordFrequencies == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(wordFrequencies);
    }

    public Map<String, Integer> getCharCategories() {
        return Collections.unmodifiableMap(charCategories);
    }

    public void setCharCategories(Map<String, Integer> charCategories) {
        this.charCategories = charCategories == null
                ? defaultCategories()
                : new LinkedHashMap<>(charCategories);
    }

}
