package vn.edu.ptit.oop.nhom07.model;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class DocumentModel {

	private Path filePath;
	private String content = "";
	private boolean modified;
	private final Charset charset = StandardCharsets.UTF_8;

	public DocumentModel() {
	}

	public DocumentModel(Path filePath, String content) {
		if (content == null) {
			throw new IllegalArgumentException("Nội dung không được null");
		}
		this.filePath = filePath;
		this.content = content;
	}

	public void setContent(String newContent) {
		if (newContent == null) {
			throw new IllegalArgumentException("Nội dung không được null");
		}
		if (!newContent.equals(content)) {
			content = newContent;
			modified = true;
		}
	}

	public void markSaved(Path path) {
		if (path == null) {
			throw new IllegalArgumentException("Đường dẫn không được null");
		}
		this.filePath = path;
		this.modified = false;
	}

	public String getContent() {
		return content;
	}

	public boolean isModified() {
		return modified;
	}

	public Path getFilePath() {
		return filePath;
	}

	public boolean isNew() {
		return filePath == null;
	}

	public Charset getCharset() {
		return charset;
	}

	public String getDisplayName() {
		if (filePath == null) {
			return "Untitled";
		}
		Path name = filePath.getFileName();
		return (name == null) ? filePath.toString() : name.toString();
	}
}
