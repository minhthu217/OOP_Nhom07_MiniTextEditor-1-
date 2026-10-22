package vn.edu.ptit.oop.nhom07.StatusBarPanel;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

public class CaretPosition {
    private final int line;
    private final int column;

    public CaretPosition(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public static CaretPosition from(JTextArea textArea) {
        int offset = textArea.getCaretPosition();

        try {
            int line = textArea.getLineOfOffset(offset);
            int column = offset
                    - textArea.getLineStartOffset(line);

            return new CaretPosition(line + 1, column + 1);
        } catch (BadLocationException e) {
            return new CaretPosition(1, 1);
        }
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return "Dòng: " + line + " | Cột: " + column;
    }
}