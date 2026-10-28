package vn.edu.ptit.oop.nhom07.ui;

import java.awt.event.WindowAdapter; 
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import javax.swing.text.JTextComponent;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import vn.edu.ptit.oop.nhom07.ui.panels.EditorTab;


public class StatisticsRefresher {

	private static final int DEBOUNCE_MS = 300;

    private record Result(Map<String, Integer> words, Map<String, Integer> chars) { }

    private final MainWindow main;
    private final StatisticsWindow window;
    private final Function<String, Map<String, Integer>> wordCounter;
    private final Function<String, Map<String, Integer>> charCounter;

    private final Timer debounce = new Timer(DEBOUNCE_MS, e -> refreshNow());
    private final DocumentListener docListener = new DocumentListener() {
        @Override public void insertUpdate(DocumentEvent e)  { debounce.restart(); }
        @Override public void removeUpdate(DocumentEvent e)  { debounce.restart(); }
        @Override public void changedUpdate(DocumentEvent e) { debounce.restart(); }
    };
    private final ChangeListener tabListener = e -> { rebind(); refreshNow(); };

    private JTextComponent watched;     
    private int generation;        
    private boolean stopped;

    public StatisticsRefresher(MainWindow main, StatisticsWindow window,
                               Function<String, Map<String, Integer>> wordCounter,
                               Function<String, Map<String, Integer>> charCounter) {
        this.main = main;
        this.window = window;
        this.wordCounter = wordCounter;
        this.charCounter = charCounter;
        debounce.setRepeats(false);
    }

    public void start() {
        main.getTabs().addChangeListener(tabListener);
        window.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) { stop(); }
        });
        rebind();
        refreshNow();
    }

    public void stop() {
        stopped = true;
        debounce.stop();
        main.getTabs().removeChangeListener(tabListener);
        if (watched != null) {
            watched.getDocument().removeDocumentListener(docListener);
            watched = null;
        }
    }

    private void rebind() {
        if (watched != null) watched.getDocument().removeDocumentListener(docListener);
        EditorTab tab = main.getCurrentTab();
        watched = (tab == null) ? null : tab.getTextArea();
        if (watched != null) watched.getDocument().addDocumentListener(docListener);
    }

    private void refreshNow() {
        if (stopped || watched == null) return;
        final String text = watched.getText();
        final int myGeneration = ++generation;

        new SwingWorker<Result, Void>() {
            @Override
            protected Result doInBackground() {
                return new Result(wordCounter.apply(text), charCounter.apply(text));
            }

            @Override
            protected void done() {
                if (stopped || myGeneration != generation) return;   
                try {
                    Result r = get();
                    window.update(r.words(), r.chars());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    window.showMessage("Lỗi phân tích: " + ex.getCause().getMessage());
                }
            }
        }.execute();
    }
}
