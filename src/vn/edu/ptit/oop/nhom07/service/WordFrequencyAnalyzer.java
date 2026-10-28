package vn.edu.ptit.oop.nhom07.service;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WordFrequencyAnalyzer implements TextAnalyzer {
	private static final Pattern WORD_PATTERN =
            Pattern.compile("[\\p{L}\\p{N}]+");

    private final int topN;

    public WordFrequencyAnalyzer() {
        this(10);
    }

    public WordFrequencyAnalyzer(int topN) {
        if (topN < 1) {
            throw new IllegalArgumentException("topN phải lớn hơn 0");
        }
        this.topN = topN;
    }

    @Override
    public Map<String, Integer> analyze(String text) {
        Map<String, Integer> frequencies = new LinkedHashMap<>();

        if (text == null || text.isEmpty()) {
            return frequencies;
        }

        String normalized = Normalizer.normalize(text, Normalizer.Form.NFC);
        Matcher matcher = WORD_PATTERN.matcher(normalized);

        while (matcher.find()) {
            String word = matcher.group().toLowerCase(Locale.ROOT);
            frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
        }

        return frequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue()
                        .reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(topN)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
    }

}
