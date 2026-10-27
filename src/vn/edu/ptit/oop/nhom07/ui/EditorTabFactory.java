package vn.edu.ptit.oop.nhom07.ui;

import java.nio.file.Path; 
import java.util.function.Consumer;

import javax.swing.JTabbedPane;

import vn.edu.ptit.oop.nhom07.model.DocumentModel;
import vn.edu.ptit.oop.nhom07.ui.panels.EditorTab;
import vn.edu.ptit.oop.nhom07.ui.panels.TabHeader;

/** Tạo tab mới / tab từ tệp và gắn vào JTabbedPane. */
public class EditorTabFactory {

	private final Consumer<EditorTab> onCloseRequest;

    /** @param onCloseRequest MainWindow xử lý đóng tab (hỏi lưu nếu cần) */
    public EditorTabFactory(Consumer<EditorTab> onCloseRequest) {
        this.onCloseRequest = onCloseRequest;
    }

    public EditorTab createNew() {
        return new EditorTab(new DocumentModel(), "");
    }

    public EditorTab createFromFile(Path path, String text) {
        DocumentModel model = new DocumentModel();
        model.setContent(text);
        model.markSaved(path);           // mở file xong thì chưa "sửa"
        EditorTab tab = new EditorTab(model, text);
        if (path != null && path.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".rtf")) {
            try { tab.loadRtf(path); }
            catch (java.io.IOException ex) { throw new IllegalArgumentException("Không thể mở tệp RTF: " + ex.getMessage(), ex); }
        }
        return tab;
    }

    /** Thêm tab vào JTabbedPane, gắn TabHeader và chọn tab đó. */
    public void addTo(JTabbedPane tabs, EditorTab tab) {
        tabs.addTab("", tab);
        int i = tabs.indexOfComponent(tab);
        tabs.setTabComponentAt(i, new TabHeader(tab, onCloseRequest));
        tabs.setSelectedIndex(i);
    }
}