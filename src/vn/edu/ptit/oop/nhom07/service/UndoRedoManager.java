package vn.edu.ptit.oop.nhom07.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

public final class UndoRedoManager implements UndoableEditListener {

	 private final Document document;
	    private final UndoManager history = new UndoManager();
	    private final List<Runnable> listeners = new ArrayList<>();
	    private boolean recording = true;

	    public UndoRedoManager(Document document) {
	        this.document = Objects.requireNonNull(document);
	        this.document.addUndoableEditListener(this);
	    }

	    @Override
	    public void undoableEditHappened(UndoableEditEvent event) {
	        if (!recording) {
	            return;
	        }
	        history.addEdit(event.getEdit());
	        notifyListeners();
	    }

	    /** Bật/tắt ghi nhận thay đổi vào lịch sử Undo/Redo.
	     * Dùng khi tô màu cú pháp hoặc thay đổi thuộc tính hiển thị tự động.
	     */
	    public void setRecording(boolean recording) {
	        this.recording = recording;
	    }

	    public boolean isRecording() {
	        return recording;
	    }

	    public boolean canUndo() {
	        return history.canUndo();
	    }

	    public boolean canRedo() {
	        return history.canRedo();
	    }

	    public void undo() {
	        if (canUndo()) {
	            history.undo();
	            notifyListeners();
	        }
	    }

	    public void redo() {
	        if (canRedo()) {
	            history.redo();
	            notifyListeners();
	        }
	    }

	    public void discardAllEdits() {
	        history.discardAllEdits();
	        notifyListeners();
	    }

	    public void addChangeListener(Runnable listener) {
	        listeners.add(Objects.requireNonNull(listener));
	    }

	    public void removeChangeListener(Runnable listener) {
	        listeners.remove(listener);
	    }

	    private void notifyListeners() {
	        for (Runnable listener : new ArrayList<>(listeners)) {
	            listener.run();
	        }
	    }

	    public void dispose() {
	        document.removeUndoableEditListener(this);
	        history.discardAllEdits();
	        listeners.clear();
	    }
}