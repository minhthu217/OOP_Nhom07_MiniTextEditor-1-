package ptvb;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class text{
    // 1. AnalysisResult
    static class AnalysisResult {
        private final int characterCount;
        private final int characterCountWithoutSpaces;
        private final int wordCount;
        private final int lineCount;

        private final int letterCount;
        private final int digitCount;
        private final int whitespaceCount;
        private final int specialCharacterCount;

        private final Map<String, Integer> wordFrequency;

        public AnalysisResult(
                int characterCount,
                int characterCountWithoutSpaces,
                int wordCount,
                int lineCount,
                int letterCount,
                int digitCount,
                int whitespaceCount,
                int specialCharacterCount,
                Map<String, Integer> wordFrequency) {

            this.characterCount = characterCount;
            this.characterCountWithoutSpaces = characterCountWithoutSpaces;
            this.wordCount = wordCount;
            this.lineCount = lineCount;
            this.letterCount = letterCount;
            this.digitCount = digitCount;
            this.whitespaceCount = whitespaceCount;
            this.specialCharacterCount = specialCharacterCount;
            this.wordFrequency = wordFrequency;
        }

        public int getCharacterCount() {
            return characterCount;
        }

        public int getCharacterCountWithoutSpaces() {
            return characterCountWithoutSpaces;
        }

        public int getWordCount() {
            return wordCount;
        }

        public int getLineCount() {
            return lineCount;
        }

        public int getLetterCount() {
            return letterCount;
        }

        public int getDigitCount() {
            return digitCount;
        }

        public int getWhitespaceCount() {
            return whitespaceCount;
        }

        public int getSpecialCharacterCount() {
            return specialCharacterCount;
        }

        public Map<String, Integer> getWordFrequency() {
            return wordFrequency;
        }
    }

    // 2. WordFrequencyAnalyzer
    static class WordFrequencyAnalyzer {

        private static final Pattern WORD_PATTERN =
                Pattern.compile("[\\p{L}\\p{N}]+");

        public Map<String, Integer> analyze(String text) {
            Map<String, Integer> frequency = new LinkedHashMap<>();

            if (text == null || text.isBlank()) {
                return frequency;
            }

            Matcher matcher = WORD_PATTERN.matcher(text);

            while (matcher.find()) {
                String word = matcher.group()
                        .toLowerCase(Locale.ROOT);

                frequency.put(
                        word,
                        frequency.getOrDefault(word, 0) + 1
                );
            }

            return frequency;
        }

        public int countWords(String text) {
            if (text == null || text.isBlank()) {
                return 0;
            }

            Matcher matcher = WORD_PATTERN.matcher(text);
            int count = 0;

            while (matcher.find()) {
                count++;
            }

            return count;
        }
    }

    // 3. CharCategoryAnalyzer
    static class CharCategoryAnalyzer {

        static class CharCategoryResult {
            private final int letters;
            private final int digits;
            private final int whitespaces;
            private final int specialCharacters;

            public CharCategoryResult(
                    int letters,
                    int digits,
                    int whitespaces,
                    int specialCharacters) {

                this.letters = letters;
                this.digits = digits;
                this.whitespaces = whitespaces;
                this.specialCharacters = specialCharacters;
            }

            public int getLetters() {
                return letters;
            }

            public int getDigits() {
                return digits;
            }

            public int getWhitespaces() {
                return whitespaces;
            }

            public int getSpecialCharacters() {
                return specialCharacters;
            }
        }

        public CharCategoryResult analyze(String text) {
            if (text == null) {
                text = "";
            }

            int letters = 0;
            int digits = 0;
            int whitespaces = 0;
            int specialCharacters = 0;

            for (int i = 0; i < text.length(); ) {

                int codePoint = text.codePointAt(i);

                if (Character.isLetter(codePoint)) {
                    letters++;
                } else if (Character.isDigit(codePoint)) {
                    digits++;
                } else if (Character.isWhitespace(codePoint)) {
                    whitespaces++;
                } else {
                    specialCharacters++;
                }

                i += Character.charCount(codePoint);
            }

            return new CharCategoryResult(
                    letters,
                    digits,
                    whitespaces,
                    specialCharacters
            );
        }
    }

    // 4. TextAnalyzer
    static class TextAnalyzer {

        private final WordFrequencyAnalyzer wordFrequencyAnalyzer;
        private final CharCategoryAnalyzer charCategoryAnalyzer;

        public TextAnalyzer() {
            wordFrequencyAnalyzer = new WordFrequencyAnalyzer();
            charCategoryAnalyzer = new CharCategoryAnalyzer();
        }

        public AnalysisResult analyze(String text) {

            if (text == null) {
                text = "";
            }

            int characterCount =
                    text.codePointCount(0, text.length());

            int characterCountWithoutSpaces =
                    countCharactersWithoutSpaces(text);

            int wordCount =
                    wordFrequencyAnalyzer.countWords(text);

            int lineCount =
                    countLines(text);

            Map<String, Integer> wordFrequency =
                    wordFrequencyAnalyzer.analyze(text);

            CharCategoryAnalyzer.CharCategoryResult charResult =
                    charCategoryAnalyzer.analyze(text);

            return new AnalysisResult(
                    characterCount,
                    characterCountWithoutSpaces,
                    wordCount,
                    lineCount,
                    charResult.getLetters(),
                    charResult.getDigits(),
                    charResult.getWhitespaces(),
                    charResult.getSpecialCharacters(),
                    wordFrequency
            );
        }

        private int countCharactersWithoutSpaces(String text) {

            int count = 0;

            for (int i = 0; i < text.length(); ) {

                int codePoint = text.codePointAt(i);

                if (!Character.isWhitespace(codePoint)) {
                    count++;
                }

                i += Character.charCount(codePoint);
            }

            return count;
        }

        private int countLines(String text) {

            if (text.isEmpty()) {
                return 0;
            }

            return text.split("\\R", -1).length;
        }
    }

    // =========================
    // 5. Main chạy chương trình
    // =========================
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("===== CHƯƠNG TRÌNH PHÂN TÍCH VĂN BẢN =====");
        System.out.println("Nhập văn bản.");
        System.out.println("Nhập END ở một dòng riêng để kết thúc:");
        System.out.println();

        StringBuilder input = new StringBuilder();

        while (true) {
            String line = scanner.nextLine();

            if (line.equals("END")) {
                break;
            }

            if (input.length() > 0) {
                input.append("\n");
            }

            input.append(line);
        }

        String text = input.toString();

        TextAnalyzer analyzer = new TextAnalyzer();
        AnalysisResult result = analyzer.analyze(text);

        System.out.println();
        System.out.println("========== KẾT QUẢ ==========");

        System.out.println(
                "Tổng số ký tự: "
                        + result.getCharacterCount()
        );

        System.out.println(
                "Số ký tự không tính khoảng trắng: "
                        + result.getCharacterCountWithoutSpaces()
        );

        System.out.println(
                "Số từ: "
                        + result.getWordCount()
        );

        System.out.println(
                "Số dòng: "
                        + result.getLineCount()
        );

        System.out.println(
                "Số chữ cái: "
                        + result.getLetterCount()
        );

        System.out.println(
                "Số chữ số: "
                        + result.getDigitCount()
        );

        System.out.println(
                "Số khoảng trắng: "
                        + result.getWhitespaceCount()
        );

        System.out.println(
                "Số ký tự đặc biệt: "
                        + result.getSpecialCharacterCount()
        );

        System.out.println();
        System.out.println("===== TẦN SUẤT TỪ =====");

        if (result.getWordFrequency().isEmpty()) {

            System.out.println("Không có từ nào.");

        } else {

            for (Map.Entry<String, Integer> entry
                    : result.getWordFrequency().entrySet()) {

                System.out.println(
                        entry.getKey()
                                + " : "
                                + entry.getValue()
                );
            }
        }

        scanner.close();
    }
}

