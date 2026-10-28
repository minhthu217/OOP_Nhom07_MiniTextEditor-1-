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
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JOptionPane;
import javax.swing.JMenu;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.text.JTextComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
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
import vn.edu.ptit.oop.nhom07.ui.dialogs.FindReplaceDialog;
import vn.edu.ptit.oop.nhom07.ui.panels.EditorTab;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import javax.swing.JCheckBoxMenuItem;

import vn.edu.ptit.oop.nhom07.service.RecentFilesManager;
public class MainWindow extends BaseWindow {
	
	private JTabbedPane tabs;            // KHÔNG viết "= new ..." ở đây
    private FileService fileService;
    private EditorTabFactory tabFactory;
    private JFileChooser chooser;
    private StatisticsWindow statsWindow; //Kiều Giang
    private JMenu editMenu;
    private FindReplaceDialog findReplaceDialog;
    private boolean darkTheme = false;
    private boolean wordWrap = false;
    private JComboBox<String> fontCombo;
    private JComboBox<Integer> fontSizeCombo;
    private RecentFilesManager recent; // My
    private JMenu recentMenu;
    
    public MainWindow() {
        super("Nhóm 07 – Mini Text Editor", 1100, 700);
    }

    @Override
    protected void initUI() {
        fileService = new FileService();
        tabFactory = new EditorTabFactory(this::closeTab);

        chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Văn bản thuần (*.txt)", "txt"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Văn bản có định dạng (*.rtf)", "rtf"));
        chooser.setFileFilter(chooser.getChoosableFileFilters()[0]);

        styleNotepadChrome();
        tabs = new JTabbedPane();
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.addChangeListener(e -> onTabChanged());
        add(tabs, BorderLayout.CENTER);

        recent = new RecentFilesManager();
        buildFileMenu();
        buildEditMenu();
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
        
        JCheckBoxMenuItem wrapItem = new JCheckBoxMenuItem("Word Wrap");
        wrapItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.ALT_DOWN_MASK));
        wrapItem.addActionListener(e -> { wordWrap = wrapItem.isSelected(); applyEditorSettings(); setStatus(wordWrap ? "Tự động xuống dòng: Bật" : "Tự động xuống dòng: Tắt"); });
        viewMenu.add(wrapItem);
        JCheckBoxMenuItem darkItem = new JCheckBoxMenuItem("Dark theme");
        darkItem.addActionListener(e -> { darkTheme = darkItem.isSelected(); applyEditorSettings(); });
        viewMenu.add(darkItem);
        viewMenu.addSeparator();
        viewMenu.add(menuItem("Phóng to chữ", KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK, e -> changeFontSize(1)));
        viewMenu.add(menuItem("Thu nhỏ chữ", KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK, e -> changeFontSize(-1)));
        viewMenu.add(menuItem("Cỡ chữ mặc định", KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK, e -> resetFontSize()));
        viewMenu.addSeparator();
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
        recentMenu = new JMenu("Recent Files");
        fileMenu.insert(recentMenu, pos++);
        refreshRecentMenu();
        fileMenu.insertSeparator(pos);
    }

    private JMenuItem menuItem(String text, int key, int modifiers, ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        item.setAccelerator(KeyStroke.getKeyStroke(key, modifiers));
        item.addActionListener(action);
        return item;
    }

    private void styleNotepadChrome() {
        // Tông xanh lá Nhật (matcha/sage), dịu mắt và tối giản.
        java.awt.Color chrome = new java.awt.Color(232, 240, 226);
        java.awt.Color border = new java.awt.Color(198, 215, 188);
        java.awt.Color text = new java.awt.Color(47, 68, 43);
        menuBar.setBackground(chrome);
        menuBar.setForeground(text);
        menuBar.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, border));
        toolBar.setBackground(chrome);
        toolBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 8, 5, 8));
        toolBar.setFloatable(false);
        statusBar.setBackground(chrome);
        statusBar.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, border));
        statusLabel.setForeground(text);
        statusLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
    }

    private void buildEditMenu() {
        editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        editMenu.add(menuItem("Undo", KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK, e -> { if (getCurrentTab() != null) getCurrentTab().getUndoManager().undo(); }));
        editMenu.add(menuItem("Redo", KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK, e -> { if (getCurrentTab() != null) getCurrentTab().getUndoManager().redo(); }));
        editMenu.addSeparator();
        editMenu.add(menuItem("Cut", KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK, e -> editorAction("cut")));
        editMenu.add(menuItem("Copy", KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK, e -> editorAction("copy")));
        editMenu.add(menuItem("Paste", KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK, e -> editorAction("paste")));
        editMenu.add(menuItem("Delete", KeyEvent.VK_DELETE, 0, e -> { if (getCurrentTab() != null) getCurrentTab().getTextArea().replaceSelection(""); }));
        editMenu.addSeparator();
        editMenu.add(menuItem("Select All", KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK, e -> editorAction("selectAll")));
        editMenu.addSeparator();
        editMenu.add(menuItem("Find / Replace...", KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK, e -> showFindReplace()));
        editMenu.add(menuItem("Find Next", KeyEvent.VK_F3, 0, e -> { showFindReplace(); if (findReplaceDialog != null) findReplaceDialog.findNext(); }));
        JMenu formatMenu = new JMenu("Format");
        formatMenu.add(menuItem("Font...", KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, e -> chooseFont()));
        formatMenu.add(menuItem("Bold", KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK, e -> toggleBold()));
        formatMenu.add(menuItem("Italic", KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK, e -> toggleItalic()));
        formatMenu.add(menuItem("Insert table...", KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, e -> insertTable()));
        formatMenu.addSeparator();
        formatMenu.add(menuItem("Print...", KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK, e -> printDocument()));
        menuBar.add(editMenu, 1);
        menuBar.add(formatMenu, 2);
    }

    private void editorAction(String action) {
        if (getCurrentTab() == null) return;
        javax.swing.JTextPane area = getCurrentTab().getTextArea();
        switch (action) {
            case "cut": area.cut(); break;
            case "copy": area.copy(); break;
            case "paste": area.paste(); break;
            case "selectAll": area.selectAll(); break;
            default: break;
        }
        area.requestFocusInWindow();
    }

    private void showFindReplace() {
        if (getCurrentTab() == null) return;
        if (findReplaceDialog == null || !findReplaceDialog.isDisplayable())
            findReplaceDialog = new FindReplaceDialog(this, () -> (JTextComponent) getCurrentTab().getTextArea());
        findReplaceDialog.showDialog();
    }

    private void applyEditorSettings() {
        for (int i = 0; i < tabs.getTabCount(); i++) {
            EditorTab tab = (EditorTab) tabs.getComponentAt(i);
            tab.setWordWrap(wordWrap);
            tab.setDarkTheme(darkTheme);
        }
    }

    private void changeFontSize(int delta) {
        if (getCurrentTab() != null) getCurrentTab().changeFontSize(delta);
    }

    private void resetFontSize() {
        if (getCurrentTab() != null) {
            int current = getCurrentTab().getEditorFontSize();
            getCurrentTab().changeFontSize(14 - current);
        }
    }

    private void buildToolBar() {
        // Nút chữ gọn, đồng bộ với thanh công cụ Notepad hiện đại.
        addCompactToolButton("New", "Tài liệu mới (Ctrl+N)", e -> newDocument());
        addCompactToolButton("Open", "Mở tệp (Ctrl+O)", e -> openDocument());
        addCompactToolButton("Save", "Lưu (Ctrl+S)", e -> saveCurrent());
        toolBar.addSeparator();
        addCompactToolButton("Undo", "Hoàn tác (Ctrl+Z)", e -> { if (getCurrentTab() != null) getCurrentTab().getUndoManager().undo(); });
        addCompactToolButton("Redo", "Làm lại (Ctrl+Y)", e -> { if (getCurrentTab() != null) getCurrentTab().getUndoManager().redo(); });
        toolBar.addSeparator();
        addCompactToolButton("Find", "Tìm kiếm / thay thế (Ctrl+H)", e -> showFindReplace());
        toolBar.addSeparator();
        fontCombo = new JComboBox<>(new String[] {"Monospaced", "Segoe UI", "Arial", "Tahoma", "Times New Roman", "Yu Gothic", "Meiryo"});
        fontCombo.setSelectedItem("Monospaced");
        fontCombo.setToolTipText("Chọn font chữ cho toàn bộ tài liệu");
        fontCombo.addActionListener(e -> { if (fontCombo.getSelectedItem() != null) setFontFamily(fontCombo.getSelectedItem().toString()); });
        toolBar.add(fontCombo);
        fontSizeCombo = new JComboBox<>(new Integer[] {10, 11, 12, 14, 16, 18, 20, 24, 28, 32, 36});
        fontSizeCombo.setSelectedItem(14);
        fontSizeCombo.setToolTipText("Cỡ chữ");
        fontSizeCombo.addActionListener(e -> { if (fontSizeCombo.getSelectedItem() != null) setFontSize((Integer) fontSizeCombo.getSelectedItem()); });
        toolBar.add(fontSizeCombo);
        toolBar.addSeparator();
        addCompactToolButton("B", "In đậm phần chữ được chọn (Ctrl+B)", e -> toggleBold());
        addCompactToolButton("I", "In nghiêng phần chữ được chọn (Ctrl+I)", e -> toggleItalic());
        addCompactToolButton("U", "Gạch chân phần chữ được chọn", e -> { if (getCurrentTab() != null) getCurrentTab().toggleUnderline(); });
        addCompactToolButton("A▾", "Đổi màu chữ", e -> chooseTextColor());
        addCompactToolButton("≡", "Căn trái", e -> setAlignment(javax.swing.text.StyleConstants.ALIGN_LEFT));
        addCompactToolButton("≣", "Căn giữa", e -> setAlignment(javax.swing.text.StyleConstants.ALIGN_CENTER));
        addCompactToolButton("Table", "Chèn bảng dạng văn bản", e -> insertTable());
        addCompactToolButton("Print", "In tài liệu (Ctrl+P)", e -> printDocument());
    }

    private void addCompactToolButton(String label, String tooltip, ActionListener action) {
        javax.swing.JButton button = new javax.swing.JButton(label);
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        button.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10));
        button.setBackground(new java.awt.Color(232, 240, 226));
        button.setForeground(new java.awt.Color(47, 68, 43));
        button.setFocusPainted(false);
        button.addActionListener(action);
        toolBar.add(button);
    }

    private void chooseFont() {
        if (getCurrentTab() == null) return;
        String[] fonts = {"Monospaced", "Segoe UI", "Arial", "Tahoma", "Times New Roman", "Yu Gothic", "Meiryo"};
        String selected = (String) JOptionPane.showInputDialog(this, "Chọn font chữ cho toàn bộ tài liệu:", "Font chữ", JOptionPane.PLAIN_MESSAGE, null, fonts, fontCombo == null ? fonts[0] : fontCombo.getSelectedItem());
        if (selected != null) { if (fontCombo != null) fontCombo.setSelectedItem(selected); else setFontFamily(selected); }
    }

    private void setFontFamily(String family) {
        if (getCurrentTab() != null) getCurrentTab().setFontFamily(family);
    }

    private void setFontSize(int size) {
        if (getCurrentTab() != null) getCurrentTab().setEditorFontSize(size);
    }

    private void toggleBold() { if (getCurrentTab() != null) getCurrentTab().toggleBold(); }
    private void toggleItalic() { if (getCurrentTab() != null) getCurrentTab().toggleItalic(); }
    private void chooseTextColor() {
        if (getCurrentTab() == null) return;
        java.awt.Color color = javax.swing.JColorChooser.showDialog(this, "Chọn màu chữ", new java.awt.Color(47, 68, 43));
        if (color != null) getCurrentTab().setTextColor(color);
    }
    private void setAlignment(int alignment) { if (getCurrentTab() != null) getCurrentTab().setAlignment(alignment); }

    private void insertTable() {
        if (getCurrentTab() == null) return;
        JSpinner rows = new JSpinner(new SpinnerNumberModel(3, 1, 20, 1));
        JSpinner cols = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.GridLayout(2, 2, 8, 8));
        panel.add(new javax.swing.JLabel("Số hàng:")); panel.add(rows);
        panel.add(new javax.swing.JLabel("Số cột:")); panel.add(cols);
        if (JOptionPane.showConfirmDialog(this, panel, "Chèn bảng dạng văn bản", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        int r = (Integer) rows.getValue(), c = (Integer) cols.getValue();
        getCurrentTab().insertTextTable(r, c);
        getCurrentTab().getTextArea().requestFocusInWindow();
    }

    private void printDocument() {
        if (getCurrentTab() == null) return;
        try { getCurrentTab().getTextArea().print(); }
        catch (java.awt.print.PrinterException ex) { JOptionPane.showMessageDialog(this, "Không thể in tài liệu: " + ex.getMessage(), "Lỗi in", JOptionPane.ERROR_MESSAGE); }
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
                    recent.add(path);
                    refreshRecentMenu();
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
            path = path.resolveSibling(name + ".txt");
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
    		if (path.getFileName().toString()
    				.toLowerCase(java.util.Locale.ROOT)
    				.endsWith(".rtf")) {
    			tab.saveRtf(path);
    		} else {
    			tab.getDocumentModel()
    			.setContent(tab.getTextArea().getText());

    			fileService.write(tab.getDocumentModel(), path);
    		}

    		recent.add(path);
    		refreshRecentMenu();

    		refreshTitle(tab);
    		setStatus("Đã lưu " + path.getFileName());
    		return true;

    	} catch (DocumentIOException ex) {
    		showError(ex);
    		return false;
    	} catch (java.io.IOException ex) {
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
        tab.setWordWrap(wordWrap);
        tab.setDarkTheme(darkTheme);
        tab.getTextArea().addCaretListener(e -> {
            int pos = e.getDot();
            try {
                String beforeCaret = tab.getTextArea().getDocument().getText(0, pos);
                int line = 1, lastBreak = -1;
                for (int j = 0; j < beforeCaret.length(); j++) if (beforeCaret.charAt(j) == '\n') { line++; lastBreak = j; }
                int col = pos - lastBreak;
                setStatus("Ln " + line + ", Col " + col + "     " + tab.getDocumentModel().getDisplayName());
            } catch (javax.swing.text.BadLocationException ignored) { }
        });
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
    
    private void refreshRecentMenu() {
        recentMenu.removeAll();
        List<Path> list = recent.getFiles();               // import java.util.List
        if (list.isEmpty()) {
            JMenuItem empty = new JMenuItem("(Trống)");
            empty.setEnabled(false);
            recentMenu.add(empty);
            return;
        }
        int n = 1;
        for (Path p : list) {
            JMenuItem item = new JMenuItem((n++) + ". " + p.getFileName());
            item.setToolTipText(p.toString());
            item.addActionListener(e -> openRecent(p));
            recentMenu.add(item);
        }
        recentMenu.addSeparator();
        JMenuItem clear = new JMenuItem("Xóa danh sách");
        clear.addActionListener(e -> { recent.clear(); refreshRecentMenu(); });
        recentMenu.add(clear);
    }

    private void openRecent(Path p) {
        if (!Files.exists(p)) {                            // tệp đã bị xóa/di chuyển
            JOptionPane.showMessageDialog(this, "Tệp không còn tồn tại:\n" + p,
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            recent.remove(p);
            refreshRecentMenu();
            return;
        }
        openPath(p);
    }
    
}


    
