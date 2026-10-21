package vn.edu.ptit.oop.nhom07.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;


public final class ChartHelper {

    
    public static final int TOP_N = 10;

    private static final String ROW_KEY = "Số lần";
    private static final String VIETNAMESE_SAMPLE = "ạảãăằẳẵắặâầẩẫấậđêềểễếệôồổỗốộơờởỡớợưừửữứự";
    private static final String[] PREFERRED_FONTS = { "Segoe UI", "Arial", "Tahoma", "Times New Roman", "DejaVu Sans" };
    private static final Color BAR_COLOR = new Color(0x2E86C1);

    private ChartHelper() { }

   
    public static void installTheme() {
        String family = pickVietnameseFont();
        StandardChartTheme theme = new StandardChartTheme("MiniTextEditor");
        theme.setExtraLargeFont(new Font(family, Font.BOLD, 18));
        theme.setLargeFont(new Font(family, Font.BOLD, 14));
        theme.setRegularFont(new Font(family, Font.PLAIN, 12));
        theme.setSmallFont(new Font(family, Font.PLAIN, 11));
        theme.setBarPainter(new StandardBarPainter());   
        theme.setShadowVisible(false);
        theme.setPlotBackgroundPaint(Color.WHITE);
        theme.setDomainGridlinePaint(Color.LIGHT_GRAY);
        theme.setRangeGridlinePaint(Color.LIGHT_GRAY);
        ChartFactory.setChartTheme(theme);
    }

   
    static String pickVietnameseFont() {
        List<String> installed = new ArrayList<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String name : PREFERRED_FONTS) {
            if (installed.contains(name)
                    && new Font(name, Font.PLAIN, 12).canDisplayUpTo(VIETNAMESE_SAMPLE) == -1) {
                return name;
            }
        }
        return Font.DIALOG;
    }

    public static DefaultCategoryDataset newBarDataset() {
        return new DefaultCategoryDataset();
    }

    public static DefaultPieDataset<String> newPieDataset() {
        return new DefaultPieDataset<>();
    }

    
    public static void fillBarDataset(DefaultCategoryDataset data, Map<String, Integer> wordFreq) {
        data.setNotify(false);
        data.clear();
        if (wordFreq != null) {
            wordFreq.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                            .thenComparing(Map.Entry.comparingByKey()))
                    .limit(TOP_N)
                    .forEach(e -> data.addValue(e.getValue(), ROW_KEY, e.getKey()));
        }
        data.setNotify(true);
    }

    
    public static void fillPieDataset(DefaultPieDataset<String> data, Map<String, Integer> charCategories) {
        data.setNotify(false);
        data.clear();
        if (charCategories != null) {
            charCategories.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> data.setValue(e.getKey(), e.getValue()));
        }
        data.setNotify(true);
    }

    public static JFreeChart createBarChart(DefaultCategoryDataset data) {
        JFreeChart chart = ChartFactory.createBarChart(
                "Top " + TOP_N + " từ xuất hiện nhiều nhất", "Từ", "Số lần",
                data, PlotOrientation.VERTICAL, false, true, false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setNoDataMessage("Chưa có dữ liệu");
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setStandardTickUnits(NumberAxis.createIntegerTickUnits());   
        range.setUpperMargin(0.12);                                       

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, BAR_COLOR);
        renderer.setMaximumBarWidth(0.12);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        return chart;
    }

    @SuppressWarnings("unchecked")
    public static JFreeChart createPieChart(DefaultPieDataset<String> data) {
        JFreeChart chart = ChartFactory.createPieChart("Tỷ lệ các nhóm ký tự", data, true, true, false);

        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setNoDataMessage("Chưa có dữ liệu");
        plot.setIgnoreZeroValues(true);          
        plot.setShadowPaint(null);
        plot.setOutlineVisible(false);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}: {2}", NumberFormat.getIntegerInstance(), NumberFormat.getPercentInstance()));
        plot.setToolTipGenerator(new StandardPieToolTipGenerator(
                "{0}: {1} ký tự ({2})", NumberFormat.getIntegerInstance(), NumberFormat.getPercentInstance()));
        return chart;
    }

    public static ChartPanel createChartPanel(JFreeChart chart) {
        ChartPanel panel = new ChartPanel(chart);
        panel.setMouseWheelEnabled(false);
        panel.setDomainZoomable(false);
        panel.setRangeZoomable(false);
        return panel;
    }
}
