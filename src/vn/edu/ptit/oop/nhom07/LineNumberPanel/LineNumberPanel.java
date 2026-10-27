package vn.edu.ptit.oop.nhom07.LineNumberPanel;

import java.awt.BorderLayout; 
import java.awt.Color;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

public class LineNumberPanel extends JPanel {
	private final JTextComponent editor;
    private final JTextArea numbers;
    public LineNumberPanel(JTextComponent editor) {
        this.editor = editor;
        setLayout(new BorderLayout());
        numbers = new JTextArea("1");
        numbers.setEditable(false); numbers.setFocusable(false);
        numbers.setBackground(new Color(232, 240, 226));
        numbers.setForeground(new Color(100, 125, 91));
        numbers.setFont(editor.getFont());
        numbers.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        add(numbers, BorderLayout.CENTER);
        editor.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateNumbers(); }
            public void removeUpdate(DocumentEvent e) { updateNumbers(); }
            public void changedUpdate(DocumentEvent e) { updateNumbers(); }
        });
        editor.addPropertyChangeListener("font", e -> numbers.setFont(editor.getFont()));
        updateNumbers();
    }
    private void updateNumbers() {
        int total = Math.max(1, editor.getText().split("\\n", -1).length);
        StringBuilder result = new StringBuilder();
        for (int i=1;i<=total;i++) result.append(i).append('\n');
        numbers.setText(result.toString()); numbers.setCaretPosition(0);
    }
    public JTextArea getNumberArea() { return numbers; }
}