package vn.edu.ptit.oop.nhom07.undo;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

public final class ShortcutRegistry {

    private ShortcutRegistry() {
    }

    public static void register(
            JComponent component, EditorActions actions) {

        Objects.requireNonNull(component);
        Objects.requireNonNull(actions);

        InputMap inputMap = component.getInputMap(
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        ActionMap actionMap = component.getActionMap();

        KeyStroke undoKey = KeyStroke.getKeyStroke(
                KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK);

        KeyStroke redoKey = KeyStroke.getKeyStroke(
                KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK);

        KeyStroke alternateRedoKey = KeyStroke.getKeyStroke(
                KeyEvent.VK_Z,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);

        inputMap.put(undoKey, "editor.undo");
        inputMap.put(redoKey, "editor.redo");
        inputMap.put(alternateRedoKey, "editor.redo");

        actionMap.put("editor.undo", actions.getUndoAction());
        actionMap.put("editor.redo", actions.getRedoAction());

        actions.getUndoAction().putValue(
                Action.ACCELERATOR_KEY, undoKey);

        actions.getRedoAction().putValue(
                Action.ACCELERATOR_KEY, redoKey);
    }
}