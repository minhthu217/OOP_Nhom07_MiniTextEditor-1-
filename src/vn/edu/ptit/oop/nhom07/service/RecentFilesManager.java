
package vn.edu.ptit.oop.nhom07.service;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class RecentFilesManager {

    private static final int MAX = 10;
    private static final String PREFIX = "recent.";

    private final Path storage;
    private final LinkedList<Path> files = new LinkedList<>();

    public RecentFilesManager() {
        this(Path.of(
            System.getProperty("user.home"),
            ".mini_text_editor",
            "app.properties"
        ));
    }

    public RecentFilesManager(Path storage) {
        this.storage = storage;
        load();
    }

    public void add(Path path) {
        if (path == null) {
            return;
        }

        Path p = path.toAbsolutePath().normalize();

        files.remove(p);
        files.addFirst(p);

        while (files.size() > MAX) {
            files.removeLast();
        }

        save();
    }

    public void remove(Path path) {
        if (path == null) {
            return;
        }

        files.remove(path.toAbsolutePath().normalize());
        save();
    }

    public void clear() {
        files.clear();
        save();
    }

    public List<Path> getFiles() {
        return Collections.unmodifiableList(
            new ArrayList<>(files)
        );
    }

    private void load() {
        if (!Files.exists(storage)) {
            return;
        }

        Properties props = new Properties();

        try (Reader reader = Files.newBufferedReader(
                storage, StandardCharsets.UTF_8)) {

            props.load(reader);

            for (int i = 0; i < MAX; i++) {
                String value = props.getProperty(PREFIX + i);

                if (value == null) {
                    break;
                }

                try {
                    Path p = Path.of(value)
                                 .toAbsolutePath()
                                 .normalize();

                    if (!files.contains(p)) {
                        files.add(p);
                    }
                } catch (RuntimeException ex) {
                    System.err.println(
                        "Đường dẫn không hợp lệ: " + value
                    );
                }
            }

        } catch (IOException ex) {
            System.err.println(
                "Không đọc được danh sách file: "
                + ex.getMessage()
            );
        }
    }

    private void save() {
        Properties props = new Properties();

        for (int i = 0; i < files.size(); i++) {
            props.setProperty(
                PREFIX + i,
                files.get(i).toString()
            );
        }

        try {
            Files.createDirectories(storage.getParent());

            try (Writer writer = Files.newBufferedWriter(
                    storage, StandardCharsets.UTF_8)) {

                props.store(
                    writer,
                    "Mini Text Editor - Recent Files"
                );
            }

        } catch (IOException ex) {
            System.err.println(
                "Không lưu được danh sách file: "
                + ex.getMessage()
            );
        }
    }
}
