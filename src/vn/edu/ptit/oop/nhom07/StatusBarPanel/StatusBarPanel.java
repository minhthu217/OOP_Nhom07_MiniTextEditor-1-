package vn.edu.ptit.oop.nhom07.StatusBarPanel;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

public class StatusBarPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final JLabel positionLabel;
    private final JLabel lineCountLabel;
    private final JTextArea editor;

    public StatusBarPanel(JTextArea editor) {
        this.editor = editor;

        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        positionLabel = new JLabel();
        lineCountLabel = new JLabel();

        add(positionLabel, BorderLayout.WEST);
        add(lineCountLabel, BorderLayout.EAST);

        editor.addCaretListener(e -> updatePosition());

        editor.getDocument().addDocumentListener(
            new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateLineCount();
                    updatePosition();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateLineCount();
                    updatePosition();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateLineCount();
                    updatePosition();
                }
            }
        );

        updatePosition();
        updateLineCount();
    }

        private void updatePosition() {
    try {
    int caretPos = editor.getCaretPosition();
    int line = editor.getLineOfOffset(caretPos);
    int lineStart = editor.getLineStartOffset(line);
    int column = caretPos - lineStart;
        positionLabel.setText(
            "Dòng: " + (line + 1) + ", Cột: " + (column + 1)
        );
    } catch (javax.swing.text.BadLocationException e) {
        positionLabel.setText("Dòng: 1, Cột: 1");
    }
}
    

    private void updateLineCount() {
        lineCountLabel.setText(
            "Số dòng: " + editor.getLineCount()
        );
    }
}
