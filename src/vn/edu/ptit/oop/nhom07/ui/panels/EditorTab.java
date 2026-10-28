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
	        textArea.setSelectionColor(enabled ? new Color(70,100,72) : new Color(198,222,184));
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
	    public void insertTextTable(int rows, int cols) {
	        StringBuilder out = new StringBuilder();
	        StringBuilder rule = new StringBuilder("+");
	        for (int c=0;c<cols;c++) rule.append("----------------+");
	        out.append(rule).append('\n');
	        for (int r=0;r<rows;r++) {
	            out.append('|');
	            for (int c=0;c<cols;c++) out.append("                |");
	            out.append('\n').append(rule).append('\n');
	        }
	        textArea.replaceSelection(out.toString());
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
