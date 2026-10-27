package vn.edu.ptit.oop.nhom07.ui.dialogs;

import java.awt.Component;

import javax.swing.JOptionPane;

/**
 * Hộp thoại hỏi Lưu / Không lưu / Hủy khi đóng tab hoặc thoát mà còn tài liệu chưa lưu.
 * Đóng hộp bằng nút X được coi là HỦY để không ai vô tình mất dữ liệu.
 */

public class CloseConfirmation {
	public static final int SAVE = 0;
    public static final int DONT_SAVE = 1;
    public static final int CANCEL = 2;

    private CloseConfirmation() { }

    public static int ask(Component parent, String documentName) {
        Object[] options = {"Lưu", "Không lưu", "Hủy"};
        int r = JOptionPane.showOptionDialog(parent,
                "Lưu thay đổi cho '" + documentName + "' trước khi đóng?",
                "Tài liệu chưa lưu",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
        if (r == 0) return SAVE;
        if (r == 1) return DONT_SAVE;
        return CANCEL;   // 2 hoặc CLOSED_OPTION (-1)
    }

}
