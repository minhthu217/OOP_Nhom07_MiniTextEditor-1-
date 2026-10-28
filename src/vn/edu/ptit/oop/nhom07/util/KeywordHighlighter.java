package vn.edu.ptit.oop.nhom07.util;

import java.awt.Color;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import vn.edu.ptit.oop.nhom07.service.UndoRedoManager;

/**
 * Tô màu cú pháp Java (từ khóa, chuỗi, chú thích, số) trên một JTextPane
 * bằng StyledDocument.
 *
 * Thiết kế:
 * - Chỉ đổi thuộc tính màu chữ, không đổi chữ => nội dung văn bản không đổi.
 * - Khi đang tô màu, tạm dừng ghi lịch sử Undo (để Ctrl+Z không "hoàn tác màu")
 *   và báo cho EditorTab biết (isApplying) để không bật cờ "đã sửa" (dấu *).
 * - Gom nhiều lần gõ phím thành một lần tô (javax.swing.Timer, trễ 150 ms).
 */
public class KeywordHighlighter {

    /** Loại token và màu tương ứng cho nền sáng / nền tối (enum có hành vi). */
    public enum TokenType {
        KEYWORD(new Color(0, 0, 180),  new Color(86, 156, 214)),
        STRING (new Color(163, 21, 21), new Color(206, 145, 120)),
        COMMENT(new Color(0, 128, 0),  new Color(106, 153, 85)),
        NUMBER (new Color(9, 134, 88), new Color(181, 206, 168));

        private final Color light;
        private final Color dark;

        TokenType(Color light, Color dark) {
            this.light = light;
            this.dark = dark;
        }

        public Color colorFor(boolean darkMode) {
            return darkMode ? dark : light;
        }
    }

    private static final String[] KEYWORDS = {
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
        "class", "const", "continue", "default", "do", "double", "else", "enum",
        "extends", "final", "finally", "float", "for", "goto", "if", "implements",
        "import", "instanceof", "int", "interface", "long", "native", "new",
        "package", "private", "protected", "public", "return", "short", "static",
        "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
        "transient", "try", "void", "volatile", "while", "var", "record",
        "true", "false", "null"
    };

    /** 4 nhóm; thứ tự quan trọng: chú thích/chuỗi phải đứng trước từ khóa. */
    private static final Pattern TOKEN = Pattern.compile(
            "(?<comment>//[^\\n]*|(?s:/\\*.*?(?:\\*/|\\z)))"
          + "|(?<string>\"(?:\\\\.|[^\"\\\\\\n])*\"|'(?:\\\\.|[^'\\\\\\n])*')"
          + "|(?<keyword>\\b(?:" + String.join("|", KEYWORDS) + ")\\b)"
          + "|(?<number>\\b\\d+(?:\\.\\d+)?\\b)");

    private final JTextPane pane;
    private final StyledDocument doc;
    private final UndoRedoManager undo;
    private final Timer timer;

    private boolean enabled;
    private boolean darkMode;
    private boolean applying;

    public KeywordHighlighter(JTextPane pane, UndoRedoManager undo) {
        this.pane = Objects.requireNonNull(pane, "pane");
        this.undo = Objects.requireNonNull(undo, "undo");
        this.doc = pane.getStyledDocument();

        this.timer = new Timer(150, e -> highlightNow());
        this.timer.setRepeats(false);

        // Chỉ nghe insert/remove. KHÔNG xử lý changedUpdate, vì chính việc đổi màu
        // sẽ phát changedUpdate => nếu xử lý sẽ lặp vô hạn.
        doc.addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { schedule(); }
            @Override public void removeUpdate(DocumentEvent e) { schedule(); }
            @Override public void changedUpdate(DocumentEvent e) { /* bỏ qua có chủ ý */ }
        });
    }

    /** Bật/tắt tô màu. Tắt thì trả chữ về màu mặc định của vùng soạn thảo. */
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) {
            highlightNow();
        } else {
            timer.stop();
            runQuietly(this::clearForeground);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** Đổi bảng màu sáng/tối (gọi sau khi EditorTab đã đổi màu nền). */
    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        if (enabled) highlightNow();
    }

    /** True khi đang đổi thuộc tính (EditorTab dùng để bỏ qua changedUpdate). */
    public boolean isApplying() {
        return applying;
    }

    private void schedule() {
        if (enabled) timer.restart();
    }

    private void highlightNow() {
        if (!enabled) return;
        timer.stop();
        runQuietly(this::paintTokens);
    }

    /** Chạy một thao tác sửa thuộc tính mà không làm bẩn Undo và cờ modified. */
    private void runQuietly(Runnable action) {
        applying = true;
        undo.setRecording(false);
        try {
            action.run();
        } finally {
            undo.setRecording(true);
            applying = false;
        }
    }

    private void paintTokens() {
        try {
            String text = doc.getText(0, doc.getLength());
            clearForeground();
            Matcher m = TOKEN.matcher(text);
            while (m.find()) {
                TokenType type = typeOf(m);
                SimpleAttributeSet attrs = new SimpleAttributeSet();
                StyleConstants.setForeground(attrs, type.colorFor(darkMode));
                doc.setCharacterAttributes(m.start(), m.end() - m.start(), attrs, false);
            }
        } catch (BadLocationException ex) {
            System.err.println("Không tô màu được: " + ex.getMessage());
        }
    }

    private static TokenType typeOf(Matcher m) {
        if (m.group("comment") != null) return TokenType.COMMENT;
        if (m.group("string") != null) return TokenType.STRING;
        if (m.group("keyword") != null) return TokenType.KEYWORD;
        return TokenType.NUMBER;
    }

    /**
     * Gỡ thuộc tính màu chữ của từng đoạn (run) để chữ theo màu mặc định của theme.
     * Các thuộc tính khác (đậm, nghiêng, cỡ chữ, font) được giữ nguyên.
     */
    private void clearForeground() {
        int length = doc.getLength();
        int pos = 0;
        while (pos < length) {
            Element run = doc.getCharacterElement(pos);
            int end = Math.min(run.getEndOffset(), length);
            AttributeSet attrs = run.getAttributes();
            if (attrs.isDefined(StyleConstants.Foreground)) {
                SimpleAttributeSet copy = new SimpleAttributeSet(attrs.copyAttributes());
                copy.removeAttribute(StyleConstants.Foreground);
                doc.setCharacterAttributes(pos, end - pos, copy, true);
            }
            pos = Math.max(end, pos + 1);
        }
    }
}
