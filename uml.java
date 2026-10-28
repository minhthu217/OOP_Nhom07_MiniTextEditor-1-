package ptvbuml;
class AnalysisResult {
    - int wordCount
    - int charCount
    - int lineCount
    - Map<String, Integer> wordFrequencies
    - Map<String, Integer> charCategories
    + AnalysisResult()
    + getWordCount(): int
    + setWordCount(int): void
    + getCharCount(): int
    + setCharCount(int): void
    + getLineCount(): int
    + setLineCount(int): void
    + getWordFrequencies(): Map<String, Integer>
    + setWordFrequencies(Map<String, Integer>): void
    + getCharCategories(): Map<String, Integer>
    + setCharCategories(Map<String, Integer>): void
}

class WordFrequencyAnalyzer {
    + analyze(String text): Map<String, Integer>
}

class CharCategoryAnalyzer {
    + analyze(String text): Map<String, Integer>
}

class TextAnalyzer {
    - WordFrequencyAnalyzer wordFrequencyAnalyzer
    - CharCategoryAnalyzer charCategoryAnalyzer
    + TextAnalyzer()
    + analyze(String text): AnalysisResult
}