package exception;

import java.nio.file.Path;

public class DocumentIOException extends Exception {

	private static final long serialVersionUID = 1L;

	private final transient Path path;

	public DocumentIOException(String message, Path path, Throwable cause) {
		super(message, cause);
		this.path = path;
	}

	public Path getPath() {
		return path;
	}
}
