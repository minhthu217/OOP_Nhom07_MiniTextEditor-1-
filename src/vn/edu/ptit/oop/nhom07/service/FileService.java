package vn.edu.ptit.oop.nhom07.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import vn.edu.ptit.oop.nhom07.exception.DocumentIOException;
import vn.edu.ptit.oop.nhom07.model.DocumentModel;

public class FileService {

	public String read(Path path) throws DocumentIOException {
		try {
			return Files.readString(path, StandardCharsets.UTF_8);
		} catch (IOException ex) {
			throw new DocumentIOException("Không thể mở tệp: " + nameOf(path), path, ex);
		}
	}

	public DocumentModel open(Path path) throws DocumentIOException {
		return new DocumentModel(path, read(path));
	}

	public void write(DocumentModel doc, Path path) throws DocumentIOException {
		try {
			Files.writeString(path, doc.getContent(), StandardCharsets.UTF_8);
			doc.markSaved(path);
		} catch (IOException ex) {
			throw new DocumentIOException("Không thể lưu tệp: " + nameOf(path), path, ex);
		}
	}

	private static String nameOf(Path path) {
		Path name = path.getFileName();
		return (name == null) ? path.toString() : name.toString();
	}
}