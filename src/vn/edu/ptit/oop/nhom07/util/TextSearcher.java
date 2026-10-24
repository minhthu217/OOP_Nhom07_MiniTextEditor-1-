package vn.edu.ptit.oop.nhom07.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class TextSearcher {

    public static final class Range {
        public final int start;
        public final int end;
        public Range(int start, int end) { this.start = start; this.end = end; }
    }

    private TextSearcher() { }

    public static Pattern compile(String query, SearchOptions o) throws PatternSyntaxException {
        String p = o.isUseRegex() ? query : Pattern.quote(query);
        if (o.isWholeWord()) {
            p = "(?<![\\p{L}\\p{N}_])(?:" + p + ")(?![\\p{L}\\p{N}_])";
        }
        int flags = o.isMatchCase() ? 0 : (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        return Pattern.compile(p, flags);
    }

    public static Range findNext(String text, String query, int from, SearchOptions o) {
        Matcher m = compile(query, o).matcher(text);
        from = Math.max(0, Math.min(from, text.length()));
        Range r = firstNonEmpty(m, from);
        if (r == null && o.isWrapAround() && from > 0) {
            r = firstNonEmpty(m, 0);
        }
        return r;
    }

    public static Range findPrevious(String text, String query, int from, SearchOptions o) {
        Matcher m = compile(query, o).matcher(text);
        Range before = null, last = null;
        while (m.find()) {
            if (m.end() == m.start()) continue;      
            last = new Range(m.start(), m.end());
            if (m.start() < from) before = last;
            else break;
        }
        if (before != null) return before;
        return o.isWrapAround() ? lastMatch(m, text) : null;
    }

    public static int countMatches(String text, String query, SearchOptions o) {
        Matcher m = compile(query, o).matcher(text);
        int n = 0;
        while (m.find()) if (m.end() > m.start()) n++;
        return n;
    }

    public static String replacementAt(String text, String query, int start, int end,
                                       String replacement, SearchOptions o) {
        Matcher m = compile(query, o).matcher(text);
        m.region(start, end);
        m.useTransparentBounds(true);   
        m.useAnchoringBounds(false);
        if (!m.matches()) return null;
        if (!o.isUseRegex()) return replacement;
        StringBuilder sb = new StringBuilder();
        m.appendReplacement(sb, replacement);
        return sb.substring(start);
    }

    public static String replaceAll(String text, String query, String replacement, SearchOptions o) {
        Matcher m = compile(query, o).matcher(text);
        String rep = o.isUseRegex() ? replacement : Matcher.quoteReplacement(replacement);
        return m.replaceAll(rep);
    }

    private static Range firstNonEmpty(Matcher m, int from) {
        boolean ok = m.find(from);
        while (ok && m.end() == m.start()) ok = m.find();
        return ok ? new Range(m.start(), m.end()) : null;
    }

    private static Range lastMatch(Matcher m, String text) {
        m.reset(text);
        Range last = null;
        while (m.find()) if (m.end() > m.start()) last = new Range(m.start(), m.end());
        return last;
    }
}