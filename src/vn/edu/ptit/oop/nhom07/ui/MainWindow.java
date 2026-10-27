package vn.edu.ptit.oop.nhom07.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import vn.edu.ptit.oop.nhom07.common.BaseWindow;
import vn.edu.ptit.oop.nhom07.exception.DocumentIOException;
import vn.edu.ptit.oop.nhom07.model.DocumentModel;
import vn.edu.ptit.oop.nhom07.service.FileService;
import vn.edu.ptit.oop.nhom07.ui.dialogs.AboutDialog;
import vn.edu.ptit.oop.nhom07.ui.dialogs.CloseConfirmation;
import vn.edu.ptit.oop.nhom07.ui.panels.EditorTab;

public class MainWindow extends BaseWindow {
	
	private JTabbedPane tabs;            // KHÔNG viết "= new ..." ở đây
    private FileService fileService;
    private EditorTabFactory tabFactory;
    private JFileChooser chooser;
    private StatisticsWindow statsWindow; //Kiều Giang
    
    public MainWindow() {
        super("Nhóm 07 – Mini Text Editor", 1100, 700);
    }

    @Override
    protected void initUI() {
        fileService = new FileService();
        tabFactory = new EditorTabFactory(this::closeTab);

        chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Tệp văn bản (*.txt)", "txt"));

        tabs = new JTabbedPane();
        tabs.addChangeListener(e -> onTabChanged());
        add(tabs, BorderLayout.CENTER);

        buildFileMenu();
        buildToolBar();

        // Tự quyết định có đóng hay không (hỏi Lưu / Không lưu / Hủy)
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (confirmCloseAll()) {
                    dispose();
                    System.exit(0);
                }
            }
        });
        
        viewMenu.add(menuItem("Thống kê & biểu đồ", KeyEvent.VK_T,  //Kiều Giang
                InputEvent.CTRL_DOWN_MASK, e -> openStatistics())); 
        newDocument();
        setStatus("Sẵn sàng");
    }

    // ------------------------------------------------------------------ menu & toolbar

    private void buildFileMenu() {
        // BaseWindow đã thêm sẵn "Exit" ở vị trí 0 -> chèn các mục mới phía trước nó
        int pos = 0;
        fileMenu.insert(menuItem("New", KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK, e -> newDocument()), pos++);
        fileMenu.insert(menuItem("Open...", KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK, e -> openDocument()), pos++);
        fileMenu.insert(menuItem("Save", KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK, e -> saveCurrent()), pos++);
        fileMenu.insert(menuItem("Save As...", KeyEvent.VK_S,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, e -> saveCurrentAs()), pos++);
        fileMenu.insert(menuItem("Close Tab", KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK, e -> closeCurrentTab()), pos++);
        fileMenu.insertSeparator(pos);
    }

    private JMenuItem menuItem(String text, int key, int modifiers, ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        item.setAccelerator(KeyStroke.getKeyStroke(key, modifiers));
        item.addActionListener(action);
        return item;
    }

    private void buildToolBar() {
        // Icon do TV6 chuẩn bị; thiếu icon thì BaseWindow tự dùng nút chữ
        addToolButton("/resources/icons/new.png", "Tài liệu mới (Ctrl+N)", e -> newDocument());
        addToolButton("/resources/icons/open.png", "Mở tệp (Ctrl+O)", e -> openDocument());
        addToolButton("/resources/icons/save.png", "Lưu (Ctrl+S)", e -> saveCurrent());
    }

    // ------------------------------------------------------------------ API công khai cho TV3/4/5/6/8

    /** Tab đang chọn; luôn khác null vì cửa sổ luôn có ít nhất một tab. */
    public EditorTab getCurrentTab() {
        return (EditorTab) tabs.getSelectedComponent();
    }

    public JTabbedPane getTabs() {
        return tabs;
    }

    // ------------------------------------------------------------------ New / Open

    public void newDocument() {
        addTab(tabFactory.createNew());
        setStatus("Đã tạo tài liệu mới");
    }

    public void openDocument() {
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        openPath(chooser.getSelectedFile().toPath());
    }

    /** Mở một tệp (dùng cho menu Open và cho Recent Files của TV9). Đọc nền bằng SwingWorker. */
    public void openPath(Path path) {
        for (int i = 0; i < tabs.getTabCount(); i++) {       // đã mở rồi -> chuyển sang tab đó
            EditorTab t = (EditorTab) tabs.getComponentAt(i);
            if (path.equals(t.getDocumentModel().getFilePath())) {
                tabs.setSelectedIndex(i);
                return;
            }
        }
        setStatus("Đang mở " + path.getFileName() + "...");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws DocumentIOException {
                return fileService.read(path);               // chạy ở luồng nền
            }

            @Override
            protected void done() {                          // chạy trên EDT
                try {
                    addTab(tabFactory.createFromFile(path, get()));
                    setStatus("Đã mở " + path.getFileName());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    setStatus("Mở tệp thất bại");
                    showError(ex.getCause());
                }
            }
        }.execute();
    }

    // ------------------------------------------------------------------ Save / Save As

    public boolean saveCurrent() {
        return saveTab(getCurrentTab());
    }

    public boolean saveCurrentAs() {
        return saveTabAs(getCurrentTab());
    }

    public boolean saveTab(EditorTab tab) {
        Path path = tab.getDocumentModel().getFilePath();
        return (path == null) ? saveTabAs(tab) : writeTo(tab, path);
    }

    private boolean saveTabAs(EditorTab tab) {
        tabs.setSelectedComponent(tab);
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return false;
        Path path = chooser.getSelectedFile().toPath();
        String name = path.getFileName().toString();
        if (!name.contains(".")) {
            path = path.resolveSibling(name + ".txt");       // tự thêm đuôi .txt
        }
        if (Files.exists(path)) {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Tệp '" + path.getFileName() + "' đã tồn tại. Ghi đè?",
                    "Xác nhận ghi đè", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok != JOptionPane.YES_OPTION) return false;
        }
        return writeTo(tab, path);
    }

    private boolean writeTo(EditorTab tab, Path path) {
        try {
            fileService.write(tab.getDocumentModel(), path);  // markSaved() chỉ gọi khi ghi thành công
            refreshTitle(tab);
            setStatus("Đã lưu " + path.getFileName());
            return true;
        } catch (DocumentIOException ex) {
            showError(ex);
            return false;
        }
    }

    // ------------------------------------------------------------------ Close tab / thoát

    public void closeCurrentTab() {
        closeTab(getCurrentTab());
    }

    public boolean closeTab(EditorTab tab) {
        if (!confirmSaveOrDiscard(tab)) return false;
        tabs.remove(tab);
        if (tabs.getTabCount() == 0) {
            newDocument();                                   // luôn giữ ít nhất một tab
        }
        return true;
    }

    /** Duyệt mọi tab chưa lưu; trả về false nếu người dùng chọn Hủy (hoặc lưu thất bại). */
    private boolean confirmCloseAll() {
        for (int i = 0; i < tabs.getTabCount(); i++) {
            if (!confirmSaveOrDiscard((EditorTab) tabs.getComponentAt(i))) return false;
        }
        return true;
    }

    /** true = được phép đóng tab này (đã lưu, hoặc chọn Không lưu, hoặc tab không có thay đổi). */
    private boolean confirmSaveOrDiscard(EditorTab tab) {
        DocumentModel model = tab.getDocumentModel();
        if (!model.isModified()) return true;
        tabs.setSelectedComponent(tab);
        int choice = CloseConfirmation.ask(this, model.getDisplayName());
        if (choice == CloseConfirmation.CANCEL) return false;
        return choice != CloseConfirmation.SAVE || saveTab(tab);
    }

    // ------------------------------------------------------------------ tiện ích nội bộ

    private void addTab(EditorTab tab) {
        tabs.addTab(tabTitle(tab), tab);
        tabs.setSelectedComponent(tab);
        // EditorTab (TV3) phát thuộc tính "modified" khi người dùng gõ -> cập nhật dấu *
        tab.addPropertyChangeListener("modified", e -> refreshTitle(tab));
        // TODO(TV3): nếu dùng TabHeader (setTabComponentAt) thì gắn tại đây
        tab.getTextArea().requestFocusInWindow();
    }

    private String tabTitle(EditorTab tab) {
        DocumentModel m = tab.getDocumentModel();
        return m.getDisplayName() + (m.isModified() ? " *" : "");
    }

    private void refreshTitle(EditorTab tab) {
        int i = tabs.indexOfComponent(tab);
        if (i >= 0) tabs.setTitleAt(i, tabTitle(tab));
    }

    private void onTabChanged() {
        EditorTab tab = getCurrentTab();
        if (tab != null) setStatus(tab.getDocumentModel().getDisplayName());
    }

    private void showError(Throwable cause) {
        JOptionPane.showMessageDialog(this, cause.getMessage(), "Lỗi tệp", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    protected void showAbout() {
        new AboutDialog(this).setVisible(true);              // gọi đa hình từ menu Help ▸ About
    }
    
    public void openStatistics() {
        if (statsWindow == null || !statsWindow.isDisplayable()) {
            statsWindow = new StatisticsWindow(this);
            new StatisticsRefresher(this, statsWindow,
                    text -> /* TV7: trả về Map từ -> số lần */ null,
                    text -> /* TV7: trả về Map nhóm ký tự -> số ký tự */ null).start();
        }
        statsWindow.setVisible(true);
        statsWindow.toFront();
    }
}
