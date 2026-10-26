package vn.edu.ptit.oop.nhom07.undo;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

public final class EditorActions {

    private UndoRedoManager currentManager;

    private final Runnable stateListener = this::refreshState;

    private final Action undoAction;
    private final Action redoAction;

    public EditorActions() {
        undoAction = new AbstractAction(
                "Undo",
                IconLoader.load("/resources/icons/undo.png")) {

            @Override
            public void actionPerformed(ActionEvent event) {
                if (currentManager != null) {
                    currentManager.undo();
                }
            }
        };

        redoAction = new AbstractAction(
                "Redo",
                IconLoader.load("/resources/icons/redo.png")) {

            @Override
            public void actionPerformed(ActionEvent event) {
                if (currentManager != null) {
                    currentManager.redo();
                }
            }
        };

        undoAction.putValue(
                Action.SHORT_DESCRIPTION, "Hoàn tác (Ctrl+Z)");

        redoAction.putValue(
                Action.SHORT_DESCRIPTION, "Làm lại (Ctrl+Y)");

        refreshState();
    }

    public Action getUndoAction() {
        return undoAction;
    }

    public Action getRedoAction() {
        return redoAction;
    }

    public void setCurrentManager(UndoRedoManager manager) {
        if (currentManager != null) {
            currentManager.removeChangeListener(stateListener);
        }

        currentManager = manager;

        if (currentManager != null) {
            currentManager.addChangeListener(stateListener);
        }

        refreshState();
    }

    public void refreshState() {
        undoAction.setEnabled(
                currentManager != null && currentManager.canUndo());

        redoAction.setEnabled(
                currentManager != null && currentManager.canRedo());
    }

    public void dispose() {
        setCurrentManager(null);
    }
}