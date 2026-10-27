package vn.edu.ptit.oop.nhom07.ui.panels;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import vn.edu.ptit.oop.nhom07.model.DocumentModel;

/** Tiêu đề tab: tên tệp, dấu * khi chưa lưu, nút đóng. */
public class TabHeader extends JPanel {

    private final EditorTab tab;
    private final JLabel titleLabel = new JLabel();

    public TabHeader(EditorTab tab, Consumer<EditorTab> onCloseRequest) {
        super(new FlowLayout(FlowLayout.LEFT, 4, 0));
        this.tab = tab;
        setOpaque(false);

        JButton closeButton = new JButton("×");
        closeButton.setToolTipText("Đóng tab");
        closeButton.setFocusable(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        closeButton.setContentAreaFilled(false);
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Không tự đóng: chỉ báo cho MainWindow, vì còn phải hỏi Lưu/Không lưu/Hủy
        closeButton.addActionListener(e -> onCloseRequest.accept(tab));

        add(titleLabel);
        add(closeButton);

        tab.addPropertyChangeListener("modified", e -> updateTitle());
        updateTitle();
    }

    private void updateTitle() {
        DocumentModel m = tab.getDocumentModel();
        titleLabel.setText(m.getDisplayName() + (m.isModified() ? " *" : ""));
    }
}