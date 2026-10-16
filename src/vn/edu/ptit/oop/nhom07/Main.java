package vn.edu.ptit.oop.nhom07;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import vn.edu.ptit.oop.nhom07.ui.MainWindow;
import vn.edu.ptit.oop.nhom07.util.ChartHelper;

/** Điểm vào của ứng dụng Mini Text Editor. */

public class Main {
	public static void main(String[] args) {
        // Gọi MỘT lần, trước khi tạo biểu đồ đầu tiên (font tiếng Việt cho JFreeChart)
        ChartHelper.installTheme();

        // Mọi thao tác Swing phải chạy trên Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException
                     | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                System.err.println("Không đặt được Look & Feel hệ thống: " + ex.getMessage());
            }
            new MainWindow().setVisible(true);
        });
    }
	
}
