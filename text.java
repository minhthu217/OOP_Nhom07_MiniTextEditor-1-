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

    // =========================
    // 4. Bộ phân tích văn bản
    // =========================
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
