package vn.edu.ptit.oop.nhom07.undo;

import java.net.URL;

import javax.swing.ImageIcon;

public final class IconLoader {

    private IconLoader() {
    }

    public static ImageIcon load(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }

        URL url = IconLoader.class.getResource(path);
        return url == null ? null : new ImageIcon(url);
    }
}