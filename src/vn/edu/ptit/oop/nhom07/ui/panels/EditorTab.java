package vn.edu.ptit.oop.nhom07.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import vn.edu.ptit.oop.nhom07.LineNumberPanel.LineNumberPanel;
import vn.edu.ptit.oop.nhom07.model.DocumentModel;
import vn.edu.ptit.oop.nhom07.service.UndoRedoManager;
import vn.edu.ptit.oop.nhom07.util.KeywordHighlighter;

/** Một tab soạn thảo: chứa (composition) vùng soạn thảo, số dòng, model và bộ undo. */
public class EditorTab extends JPanel {

	private final JTextPane textArea = new JTextPane();
    private final DocumentModel model;
    private final LineNumberPanel lineNumbers;
    private final KeywordHighlighter highlighter;
	    private final UndoRedoManager undo;
	    private boolean lastModified;
	    private boolean darkTheme = false;
	    private int editorFontSize = 14;
	    private String editorFontFamily = "Segoe UI";

	    public EditorTab(DocumentModel model, String initialText) {
	        super(new BorderLayout());

	        if (model == null) {
	            throw new IllegalArgumentException(
	                "DocumentModel không được null"
	            );
	        }

	        this.model = model;

	        textArea.setFont(
	            new Font(editorFontFamily, Font.PLAIN, editorFontSize)
	        );
	        textArea.setMargin(new java.awt.Insets(10, 12, 10, 12));
	        // Màu vùng chọn rõ hơn, kể cả khi hộp thoại Tìm và thay thế đang mở.
	        textArea.setSelectionColor(new Color(25, 35, 23));
	        textArea.setSelectedTextColor(Color.WHITE);
	        textArea.setText(initialText == null ? "" : initialText);

	        JScrollPane scroll = new JScrollPane(textArea);

	        lineNumbers = new LineNumberPanel(textArea);
	        scroll.setRowHeaderView(lineNumbers);

	        add(scroll, BorderLayout.CENTER);

	        // Khởi tạo bộ Undo/Redo
	        undo = new UndoRedoManager(textArea.getDocument());
	        highlighter = new KeywordHighlighter(textArea, undo);

	        lastModified = model.isModified();

	        textArea.getDocument().addDocumentListener(new DocumentListener() {
	            @Override
	            public void insertUpdate(DocumentEvent e) {
	                onEdited();
	            }

	            @Override
	            public void removeUpdate(DocumentEvent e) {
	                onEdited();
	            }

	            @Override
	            public void changedUpdate(DocumentEvent e) {
	            	if (highlighter.isApplying()) return;
	                model.markModified();
	                onEdited();
	            }
	        });
	    }

	    private void onEdited() {
	        model.setContent(textArea.getText());
	        boolean now = model.isModified();
	        if (now != lastModified) {
	            boolean old = lastModified; lastModified = now;
	            firePropertyChange("modified", old, now);
	        }
	    }
	    public void refreshState() { lastModified = model.isModified(); firePropertyChange("modified", !lastModified, lastModified); }
	    public JTextPane getTextArea() { return textArea; }
	    public DocumentModel getDocumentModel() { return model; }
	    public UndoRedoManager getUndoManager() { return undo; }
	    public LineNumberPanel getLineNumbers() {
	        return lineNumbers;
	    }
	    public void setWordWrap(boolean enabled) { /* JTextPane tự wrap theo chiều rộng */ }
	    
	    public void setSyntaxHighlight(boolean enabled) { highlighter.setEnabled(enabled); }
	    public boolean isSyntaxHighlight() { return highlighter.isEnabled(); }
	    
	    public void setDarkTheme(boolean enabled) {
	        darkTheme = enabled;
	        Color bg = enabled ? new Color(36,42,36) : new Color(250,252,247);
	        Color fg = enabled ? new Color(230,236,226) : new Color(43,57,40);
	        textArea.setBackground(bg); textArea.setForeground(fg); textArea.setCaretColor(fg);
	        // Xanh matcha đậm + chữ trắng để vùng được chọn dễ nhận biết.
	        textArea.setSelectionColor(enabled ? new Color(25, 35, 23) : new Color(25, 35, 23));
	        textArea.setSelectedTextColor(Color.WHITE);
	        if (getComponentCount() > 0 && getComponent(0) instanceof JScrollPane) {
	            JScrollPane pane = (JScrollPane)getComponent(0);
	            pane.getViewport().setBackground(bg); pane.setBackground(bg);
	            if (pane.getRowHeader() != null) pane.getRowHeader().setBackground(enabled ? new Color(42,48,42) : new Color(232,240,226));
	        }
	        highlighter.setDarkMode(enabled);
	    }

	    public void changeFontSize(int delta) { setEditorFontSize(Math.max(8, Math.min(40, editorFontSize + delta))); }
	    public void setEditorFontSize(int size) {
	        editorFontSize = Math.max(8, Math.min(72, size));
	        applyCharacterAttribute(StyleConstants.Size, Integer.valueOf(editorFontSize));
	        textArea.setFont(new Font(editorFontFamily, Font.PLAIN, editorFontSize));
	    }
	    public int getEditorFontSize() { return editorFontSize; }
	    public void setFontFamily(String family) {
	        if (family == null || family.trim().isEmpty()) return;
	        editorFontFamily = family;
	        applyCharacterAttribute(StyleConstants.FontFamily, family);
	        textArea.setFont(new Font(editorFontFamily, Font.PLAIN, editorFontSize));
	    }
	    public void toggleBold() { toggleStyle(StyleConstants.Bold); }
	    public void toggleItalic() { toggleStyle(StyleConstants.Italic); }
	    public void toggleUnderline() { toggleStyle(StyleConstants.Underline); }
	    public void setTextColor(Color color) { if (color != null) applyCharacterAttribute(StyleConstants.Foreground, color); }
	    public void setAlignment(int alignment) {
	        StyledDocument doc = textArea.getStyledDocument();
	        SimpleAttributeSet attrs = new SimpleAttributeSet();
	        StyleConstants.setAlignment(attrs, alignment);
	        int start = textArea.getSelectionStart(), end = textArea.getSelectionEnd();
	        int pos = start == end ? textArea.getCaretPosition() : start;
	        try {
	            int paragraphStart = doc.getDefaultRootElement().getElementIndex(pos);
	            int paragraphEnd = end > start ? doc.getDefaultRootElement().getElementIndex(end) : paragraphStart;
	            for (int i = paragraphStart; i <= paragraphEnd; i++) {
	                javax.swing.text.Element el = doc.getDefaultRootElement().getElement(i);
	                doc.setParagraphAttributes(el.getStartOffset(), el.getEndOffset()-el.getStartOffset(), attrs, false);
	            }
	        } catch (Exception ignored) { }
	    }
	    private void toggleStyle(Object key) {
	        int start = textArea.getSelectionStart(), end = textArea.getSelectionEnd();
	        AttributeSet current = textArea.getCharacterAttributes();
	        boolean enabled = key == StyleConstants.Bold ? StyleConstants.isBold(current)
	                : key == StyleConstants.Italic ? StyleConstants.isItalic(current) : StyleConstants.isUnderline(current);
	        SimpleAttributeSet attrs = new SimpleAttributeSet();
	        if (key == StyleConstants.Bold) StyleConstants.setBold(attrs, !enabled);
	        else if (key == StyleConstants.Italic) StyleConstants.setItalic(attrs, !enabled);
	        else StyleConstants.setUnderline(attrs, !enabled);
	        if (start == end) textArea.setCharacterAttributes(attrs, false);
	        else textArea.getStyledDocument().setCharacterAttributes(start, end-start, attrs, false);
	        textArea.requestFocusInWindow();
	    }
	    private void applyCharacterAttribute(Object key, Object value) {
	        SimpleAttributeSet attrs = new SimpleAttributeSet();
	        if (key == StyleConstants.Size) StyleConstants.setFontSize(attrs, (Integer)value);
	        else if (key == StyleConstants.FontFamily) StyleConstants.setFontFamily(attrs, (String)value);
	        else if (key == StyleConstants.Foreground) StyleConstants.setForeground(attrs, (Color)value);
	        int start = textArea.getSelectionStart(), end = textArea.getSelectionEnd();
	        if (start == end) textArea.setCharacterAttributes(attrs, false);
	        else textArea.getStyledDocument().setCharacterAttributes(start, end-start, attrs, false);
	    }
	    // ===== Chèn bảng dạng văn bản =====

	    /** Các ký tự kẻ khung dùng để vẽ bảng. */
	    private static final String BOX_CHARS = "┌┬┐├┼┤└┴┘─│";

	    /** Font đơn cách (monospace) có sẵn ký tự kẻ khung, thử theo thứ tự ưu tiên. */
	    private static final String[] TABLE_FONT_CANDIDATES = {
	        "Consolas", "Cascadia Mono", "Cascadia Code", "Lucida Console",
	        "DejaVu Sans Mono", "Menlo", "Liberation Mono", "Noto Sans Mono",
	        "Ubuntu Mono", "Courier New"
	    };

	    private static String cachedTableFont;
	    private static boolean tableFontResolved;

	    /**
	     * Tìm font để vẽ bảng. Font hợp lệ phải có đủ ký tự kẻ khung VÀ các ký tự đó
	     * phải rộng đúng bằng dấu cách; nếu không, cột sẽ bị lệch.
	     * (Font logic "Monospaced" bị loại vì Java lấy ký tự kẻ khung từ font khác,
	     * độ rộng không khớp dấu cách => bảng lệch như trong ảnh lỗi.)
	     * @return tên font, hoặc null nếu máy không có font phù hợp (dùng bảng ASCII).
	     */
	    private String resolveTableFont() {
	        if (tableFontResolved) return cachedTableFont;
	        java.util.Set<String> installed = new java.util.HashSet<>();
	        for (String f : java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
	                .getAvailableFontFamilyNames()) {
	            installed.add(f.toLowerCase());
	        }
	        for (String name : TABLE_FONT_CANDIDATES) {
	            if (!installed.contains(name.toLowerCase())) continue;
	            Font font = new Font(name, Font.PLAIN, 16);
	            if (font.canDisplayUpTo(BOX_CHARS) != -1) continue;
	            java.awt.FontMetrics fm = textArea.getFontMetrics(font);
	            int w = fm.charWidth(' ');
	            boolean sameWidth = w > 0;
	            for (int i = 0; i < BOX_CHARS.length() && sameWidth; i++) {
	                sameWidth = fm.charWidth(BOX_CHARS.charAt(i)) == w;
	            }
	            if (sameWidth) {
	                cachedTableFont = name;
	                break;
	            }
	        }
	        tableFontResolved = true;
	        return cachedTableFont;
	    }

	    /** Dựng chuỗi bảng. unicode=true: nét liền ┌─┬─┐; false: bảng ASCII +---+. */
	    private static String buildTable(int rows, int cols, int cellWidth, boolean unicode) {
	        String h = unicode ? "─" : "-";
	        String v = unicode ? "│" : "|";
	        String[] top    = unicode ? new String[] {"┌", "┬", "┐"} : new String[] {"+", "+", "+"};
	        String[] middle = unicode ? new String[] {"├", "┼", "┤"} : new String[] {"+", "+", "+"};
	        String[] bottom = unicode ? new String[] {"└", "┴", "┘"} : new String[] {"+", "+", "+"};

	        StringBuilder out = new StringBuilder();
	        out.append(border(top, h, cols, cellWidth)).append('\n');
	        for (int r = 0; r < rows; r++) {
	            out.append(v);
	            for (int c = 0; c < cols; c++) {
	                out.append(" ".repeat(cellWidth)).append(v);
	            }
	            out.append('\n');
	            out.append(border(r == rows - 1 ? bottom : middle, h, cols, cellWidth)).append('\n');
	        }
	        return out.toString();
	    }

	    private static String border(String[] corners, String h, int cols, int cellWidth) {
	        StringBuilder sb = new StringBuilder(corners[0]);
	        for (int c = 0; c < cols; c++) {
	            sb.append(h.repeat(cellWidth)).append(c == cols - 1 ? corners[2] : corners[1]);
	        }
	        return sb.toString();
	    }

	    public void insertTextTable(int rows, int cols) {
	        if (rows < 1 || cols < 1) {
	            return;
	        }

	        final int cellWidth = 12;
	        String fontName = resolveTableFont();
	        String table = buildTable(rows, cols, cellWidth, fontName != null);

	        int insertStart = Math.min(textArea.getSelectionStart(), textArea.getSelectionEnd());
	        StyledDocument doc = textArea.getStyledDocument();

	        // Bảng phải bắt đầu ở đầu dòng, nếu không dòng đầu sẽ bị lệch so với các dòng sau.
	        String prefix = "";
	        try {
	            if (insertStart > 0 && doc.getText(insertStart - 1, 1).charAt(0) != '\n') {
	                prefix = "\n";
	            }
	        } catch (BadLocationException ignored) { }

	        // Tạm dừng tô cú pháp khi chèn bảng để không ghi đè thuộc tính font.
	        boolean wasHighlighting = highlighter.isEnabled();
	        if (wasHighlighting) highlighter.setEnabled(false);
	        try {
	            textArea.replaceSelection(prefix + table);

	            // Toàn bộ bảng (kể cả dấu cách và ký tự xuống dòng) dùng CÙNG một font/cỡ chữ
	            // và không đậm/nghiêng để các nét kẻ nối liền nhau. Không đặt màu chữ cố định
	            // để bảng vẫn đúng màu khi đổi giao diện sáng/tối.
	            SimpleAttributeSet tableAttrs = new SimpleAttributeSet();
	            StyleConstants.setFontFamily(tableAttrs, fontName != null ? fontName : "Monospaced");
	            StyleConstants.setFontSize(tableAttrs, editorFontSize);
	            doc.setCharacterAttributes(insertStart + prefix.length(), table.length(), tableAttrs, true);
	        } finally {
	            if (wasHighlighting) highlighter.setEnabled(true);
	        }
	    }

	    public void loadRtf(Path path) throws IOException {
	        try {
	            RTFEditorKit kit = new RTFEditorKit();
	            textArea.setText("");
	            try (java.io.InputStream in = Files.newInputStream(path)) { kit.read(in, textArea.getStyledDocument(), 0); }
	            model.setContent(textArea.getText());
	            model.markSaved(path); lastModified = false;
	        } catch (BadLocationException ex) { throw new IOException("Không đọc được nội dung RTF", ex); }
	    }
	    public void saveRtf(Path path) throws IOException {
	        RTFEditorKit kit = new RTFEditorKit();
	        try (java.io.OutputStream out = Files.newOutputStream(path)) {
	            kit.write(out, textArea.getStyledDocument(), 0, textArea.getDocument().getLength());
	        } catch (BadLocationException ex) { throw new IOException("Không ghi được nội dung RTF", ex); }
	        model.setContent(textArea.getText()); model.markSaved(path); refreshState();
	    }
}
