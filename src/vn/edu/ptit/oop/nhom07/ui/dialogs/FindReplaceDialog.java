package vn.edu.ptit.oop.nhom07.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.function.Supplier;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import vn.edu.ptit.oop.nhom07.util.SearchOptions;
import vn.edu.ptit.oop.nhom07.util.TextSearcher;
import vn.edu.ptit.oop.nhom07.util.TextSearcher.Range;

public class FindReplaceDialog extends JDialog {

    private final Supplier<JTextComponent> editorSupplier;

    private final JTextField findField = new JTextField(24);
    private final JTextField replaceField = new JTextField(24);
    private final JCheckBox cbMatchCase = new JCheckBox("Phân biệt hoa/thường");
    private final JCheckBox cbWholeWord = new JCheckBox("Khớp từ nguyên vẹn");
    private final JCheckBox cbRegex = new JCheckBox("Biểu thức chính quy (regex)");
    private final JCheckBox cbWrap = new JCheckBox("Quay vòng", true);
    private final JLabel statusLabel = new JLabel(" ");

    public FindReplaceDialog(Frame owner, Supplier<JTextComponent> editorSupplier) {
        super(owner, "Tìm và thay thế", false);
        this.editorSupplier = editorSupplier;
        buildUi();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public void showDialog() {
        JTextComponent ed = editor();
        if (ed != null && ed.getSelectedText() != null && !ed.getSelectedText().contains("\n")) {
            findField.setText(ed.getSelectedText());
        }
        setVisible(true);
        toFront();
        findField.requestFocusInWindow();
        findField.selectAll();
    }

    public void findNext()     { doFind(true); }
    public void findPrevious() { doFind(false); }

    private void buildUi() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Tìm:"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; form.add(findField, c);
        c.gridx = 0; c.gridy = 1; c.fill = GridBagConstraints.NONE; c.weightx = 0; form.add(new JLabel("Thay bằng:"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; form.add(replaceField, c);

        JPanel opts = new JPanel(new GridLayout(2, 2, 6, 2));
        opts.setBorder(BorderFactory.createTitledBorder("Tùy chọn"));
        opts.add(cbMatchCase); opts.add(cbWholeWord);
        opts.add(cbRegex);     opts.add(cbWrap);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; form.add(opts, c);

        JButton btnNext = new JButton("Tìm tiếp");
        JButton btnPrev = new JButton("Tìm trước");
        JButton btnReplace = new JButton("Thay thế");
        JButton btnReplaceAll = new JButton("Thay thế tất cả");
        JButton btnCount = new JButton("Đếm");
        JButton btnClose = new JButton("Đóng");

        JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 5));
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 0, 4, 10));
        for (JButton b : new JButton[]{btnNext, btnPrev, btnReplace, btnReplaceAll, btnCount, btnClose}) {
            buttons.add(b);
        }

        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 10));

        btnNext.addActionListener(e -> doFind(true));
        btnPrev.addActionListener(e -> doFind(false));
        btnReplace.addActionListener(e -> doReplace());
        btnReplaceAll.addActionListener(e -> doReplaceAll());
        btnCount.addActionListener(e -> doCount());
        btnClose.addActionListener(e -> setVisible(false));
        findField.addActionListener(e -> doFind(true));  
        getRootPane().setDefaultButton(btnNext);

        getRootPane().registerKeyboardAction(e -> setVisible(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.EAST);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private SearchOptions options() {
        return new SearchOptions(cbMatchCase.isSelected(), cbWholeWord.isSelected(),
                cbRegex.isSelected(), cbWrap.isSelected());
    }

    private JTextComponent editor() { return editorSupplier.get(); }

    private boolean ready(JTextComponent ed) {
        if (ed == null) { setStatus("Chưa có tài liệu nào đang mở.", true); return false; }
        if (findField.getText().isEmpty()) { setStatus("Hãy nhập nội dung cần tìm.", true); return false; }
        return true;
    }

    private void doFind(boolean forward) {
        JTextComponent ed = editor();
        if (!ready(ed)) return;
        try {
            String text = ed.getText();
            String q = findField.getText();
            Range r = forward
                    ? TextSearcher.findNext(text, q, ed.getSelectionEnd(), options())
                    : TextSearcher.findPrevious(text, q, ed.getSelectionStart(), options());
            if (r == null) {
                setStatus("Không tìm thấy \"" + q + "\".", true);
                Toolkit.getDefaultToolkit().beep();
            } else {
                ed.getCaret().setSelectionVisible(true); 
                ed.select(r.start, r.end);
                setStatus(" ", false);
            }
        } catch (PatternSyntaxException ex) {
            setStatus("Regex không hợp lệ: " + ex.getDescription(), true);
        }
    }

    private void doReplace() {
        JTextComponent ed = editor();
        if (!ready(ed)) return;
        try {
            int s = ed.getSelectionStart(), e = ed.getSelectionEnd();
            if (s != e) {
                String rep = TextSearcher.replacementAt(ed.getText(), findField.getText(),
                        s, e, replaceField.getText(), options());
                if (rep != null) ed.replaceSelection(rep);
            }
            doFind(true);   
        } catch (IllegalArgumentException | IndexOutOfBoundsException ex) {
            setStatus("Lỗi: " + ex.getMessage(), true);
        }
    }

    private void doReplaceAll() {
        JTextComponent ed = editor();
        if (!ready(ed)) return;
        try {
            String text = ed.getText();
            int n = TextSearcher.countMatches(text, findField.getText(), options());
            if (n == 0) { setStatus("Không có gì để thay thế.", true); return; }
            String result = TextSearcher.replaceAll(text, findField.getText(),
                    replaceField.getText(), options());
            int caret = ed.getCaretPosition();
            replaceWholeText(ed, result);
            ed.setCaretPosition(Math.min(caret, result.length()));
            setStatus("Đã thay thế " + n + " vị trí.", false);
        } catch (IllegalArgumentException | IndexOutOfBoundsException ex) {
            setStatus("Lỗi: " + ex.getMessage(), true);
        }
    }

    private void doCount() {
        JTextComponent ed = editor();
        if (!ready(ed)) return;
        try {
            int n = TextSearcher.countMatches(ed.getText(), findField.getText(), options());
            setStatus("Tìm thấy " + n + " kết quả.", n == 0);
        } catch (PatternSyntaxException ex) {
            setStatus("Regex không hợp lệ: " + ex.getDescription(), true);
        }
    }

    private void replaceWholeText(JTextComponent ed, String newText) {
        try {
            if (ed.getDocument() instanceof AbstractDocument) {
                ((AbstractDocument) ed.getDocument())
                        .replace(0, ed.getDocument().getLength(), newText, null);
            } else {
                ed.setText(newText);
            }
        } catch (BadLocationException ex) {
            ed.setText(newText);
        }
    }

    private void setStatus(String msg, boolean error) {
        statusLabel.setForeground(error ? new Color(0xC0, 0x1C, 0x1C) : UIManager.getColor("Label.foreground"));
        statusLabel.setText(msg);
    }
}