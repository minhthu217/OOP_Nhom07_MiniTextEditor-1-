package ptvb;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class text {
    static class AnalysisResult {
        int characters;
        int charactersNoSpace;
        int words;
        int lines;
        int letters;
        int digits;
        int spaces;
        int specialChars;

        Map<String, Integer> frequency;

        public AnalysisResult(int characters,
                              int charactersNoSpace,
                              int words,
                              int lines,
                              int letters,
                              int digits,
                              int spaces,
                              int specialChars,
                              Map<String, Integer> frequency) {

            this.characters = characters;
            this.charactersNoSpace = charactersNoSpace;
            this.words = words;
            this.lines = lines;
            this.letters = letters;
            this.digits = digits;
            this.spaces = spaces;
            this.specialChars = specialChars;
            this.frequency = frequency;
        }

        public int getCharacters() {
            return characters;
        }

        public int getWords() {
            return words;
        }

        public int getLines() {
            return lines;
        }

        public Map<String, Integer> getFrequency() {
            return frequency;
        }
    }
    static class WordFrequencyAnalyzer {

        private final Pattern pattern =
                Pattern.compile("[\\p{L}\\p{N}]+");

        public Map<String, Integer> analyze(String text) {

            Map<String, Integer> result =
                    new LinkedHashMap<>();

            Matcher matcher = pattern.matcher(text);

            while (matcher.find()) {

                String word = matcher.group()
                        .toLowerCase(Locale.ROOT);

                result.put(
                        word,
                        result.getOrDefault(word, 0) + 1
                );
            }

            return result;
        }

        public int countWords(String text) {

            Matcher matcher = pattern.matcher(text);
            int count = 0;

            while (matcher.find()) {
                count++;
            }

            return count;
        }
    }
    static class CharCategoryAnalyzer {

        int[] analyze(String text) {

            int letters = 0;
            int digits = 0;
            int spaces = 0;
            int special = 0;

            for (char c : text.toCharArray()) {

                if (Character.isLetter(c)) {
                    letters++;
                }
                else if (Character.isDigit(c)) {
                    digits++;
                }
                else if (Character.isWhitespace(c)) {
                    spaces++;
                }
                else {
                    special++;
                }
            }

            return new int[]{
                    letters,
                    digits,
                    spaces,
                    special
            };
        }
    }
    static class TextAnalyzer {

        private final WordFrequencyAnalyzer wordAnalyzer =
                new WordFrequencyAnalyzer();

        private final CharCategoryAnalyzer charAnalyzer =
                new CharCategoryAnalyzer();

        public AnalysisResult analyze(String text) {

            if (text == null) {
                text = "";
            }

            int characters = text.length();

            int charactersNoSpace =
                    text.replaceAll("\\s", "").length();

            int words =
                    wordAnalyzer.countWords(text);

            int lines =
                    text.isEmpty() ? 0
                            : text.split("\\R", -1).length;

            int[] chars =
                    charAnalyzer.analyze(text);

            Map<String, Integer> frequency =
                    wordAnalyzer.analyze(text);

            return new AnalysisResult(
                    characters,
                    charactersNoSpace,
                    words,
                    lines,
                    chars[0],
                    chars[1],
                    chars[2],
                    chars[3],
                    frequency
            );
        }
    }
}
