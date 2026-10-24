package vn.edu.ptit.oop.nhom07.util;

public final class SearchOptions {
    private final boolean matchCase;
    private final boolean wholeWord;
    private final boolean useRegex;
    private final boolean wrapAround;

    public SearchOptions(boolean matchCase, boolean wholeWord,
                         boolean useRegex, boolean wrapAround) {
        this.matchCase = matchCase;
        this.wholeWord = wholeWord;
        this.useRegex = useRegex;
        this.wrapAround = wrapAround;
    }

    public boolean isMatchCase()  { return matchCase; }
    public boolean isWholeWord()  { return wholeWord; }
    public boolean isUseRegex()   { return useRegex; }
    public boolean isWrapAround() { return wrapAround; }
}