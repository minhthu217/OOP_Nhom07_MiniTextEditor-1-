package vn.edu.ptit.oop.nhom07.LineNumberPanel;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class LineNumberPanel extends JPanel {
    private final JTextArea editor;
    private final JTextArea numbers;

    public LineNumberPanel(JTextArea editor) {
        this.editor = editor;

        setLayout(new BorderLayout());
        numbers = new JTextArea("1");
        numbers.setEditable(false);
        numbers.setFocusable(false);
        numbers.setBackground(new Color(240, 240, 240));
        numbers.setForeground(Color.GRAY);
        numbers.setFont(editor.getFont());
        numbers.setBorder(
                BorderFactory.createEmptyBorder(0, 5, 0, 5)
        );

        add(numbers, BorderLayout.CENTER);

        editor.getDocument().addDocumentListener(
            new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    updateNumbers();
                }

                public void removeUpdate(DocumentEvent e) {
                    updateNumbers();
                }

                public void changedUpdate(DocumentEvent e) {
                    updateNumbers();
                }
            }
        );

        editor.addPropertyChangeListener("font",
            e -> numbers.setFont(editor.getFont()));

        updateNumbers();
    }

    private void updateNumbers() {
        int totalLines = editor.getLineCount();
        StringBuilder result = new StringBuilder();

        for (int i = 1; i <= totalLines; i++) {
            result.append(i).append("\n");
        }

        numbers.setText(result.toString());
        numbers.setCaretPosition(0);
    }

    public JTextArea getNumberArea() {
        return numbers;
    }
}