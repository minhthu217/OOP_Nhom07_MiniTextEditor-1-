package vn.edu.ptit.oop.nhom07.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import vn.edu.ptit.oop.nhom07.util.ChartHelper;


public class StatisticsWindow extends JFrame {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final DefaultCategoryDataset barData = ChartHelper.newBarDataset();
    private final DefaultPieDataset<String> pieData = ChartHelper.newPieDataset();
    private final JLabel statusLabel = new JLabel(" Đang chờ dữ liệu...");

    public StatisticsWindow(Window owner) {
        super("Thống kê văn bản");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(980, 520);
        setMinimumSize(new Dimension(700, 400));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel charts = new JPanel(new GridLayout(1, 2, 8, 0));
        charts.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        charts.add(ChartHelper.createChartPanel(ChartHelper.createBarChart(barData)));
        charts.add(ChartHelper.createChartPanel(ChartHelper.createPieChart(pieData)));
        add(charts, BorderLayout.CENTER);

        statusLabel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        add(statusLabel, BorderLayout.SOUTH);
    }

    
    public void update(Map<String, Integer> wordFreq, Map<String, Integer> charCategories) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> update(wordFreq, charCategories));
            return;
        }
        ChartHelper.fillBarDataset(barData, wordFreq);
        ChartHelper.fillPieDataset(pieData, charCategories);
        showMessage("Cập nhật lúc " + LocalTime.now().format(TIME));
    }

    public void showMessage(String msg) {
        statusLabel.setText(" " + msg);
    }
}