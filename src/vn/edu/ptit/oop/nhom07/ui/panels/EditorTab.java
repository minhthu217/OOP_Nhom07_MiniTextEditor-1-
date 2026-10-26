package vn.edu.ptit.oop.nhom07.ui.panels;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import vn.edu.ptit.oop.nhom07.model.DocumentModel;
import vn.edu.ptit.oop.nhom07.service.UndoRedoManager;
// TODO: sửa import cho khớp package thật của TV4 (xem mục 5)
import vn.edu.ptit.oop.nhom07.LineNumberPanel.LineNumberPanel;

/** Một tab soạn thảo: chứa (composition) vùng soạn thảo, số dòng, model và bộ undo. */
public class EditorTab extends JPanel {

    private final JTextArea textArea = new JTextArea();
    private final DocumentModel model;
    private final UndoRedoManager undo = new UndoRedoManager();
    private boolean lastModified;   // nhớ trạng thái cũ để chỉ báo khi thật sự đổi

    public EditorTab(DocumentModel model, String initialText) {
        super(new BorderLayout());
        if (model == null) {
            throw new IllegalArgumentException("DocumentModel không được null");
        }
        this.model = model;

        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));
        textArea.setTabSize(4);
        textArea.setLineWrap(false);                 // tắt wrap để số dòng khớp
        textArea.setText(initialText == null ? "" : initialText);   // TRƯỚC khi gắn listener

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setRowHeaderView(new LineNumberPanel(textArea));
        add(scroll, BorderLayout.CENTER);

        undo.attachTo(textArea.getDocument());       // sau setText => không undo được việc nạp file
        lastModified = model.isModified();

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { onEdited(); }
            @Override public void removeUpdate(DocumentEvent e)  { onEdited(); }
            @Override public void changedUpdate(DocumentEvent e) { onEdited(); }
        });
    }

    private void onEdited() {
        model.setContent(textArea.getText());        // model tự bật cờ modified
        boolean now = model.isModified();
        if (now != lastModified) {                   // chỉ báo khi trạng thái đổi
            boolean old = lastModified;
            lastModified = now;
            firePropertyChange("modified", old, now);   // TabHeader lắng nghe
        }
    }

    /**
     * MainWindow gọi sau khi Save / Save As thành công để TabHeader cập nhật
     * tên tệp và bỏ dấu *. Luôn phát sự kiện (old != new).
     */
    public void refreshState() {
        lastModified = model.isModified();
        firePropertyChange("modified", !lastModified, lastModified);
    }

    public JTextArea getTextArea()          { return textArea; }
    public DocumentModel getDocumentModel() { return model; }
    public UndoRedoManager getUndoManager() { return undo; }
}