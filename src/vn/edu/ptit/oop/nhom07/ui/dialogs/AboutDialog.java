package vn.edu.ptit.oop.nhom07.ui.dialogs;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class AboutDialog extends JDialog {
	private static final long serialVersionUID = 1L;
    public AboutDialog(JFrame owner) {
        super(owner, "About", true);
        add(new JLabel("Nhóm 07 – Mini Text Editor", SwingConstants.CENTER), BorderLayout.CENTER);
        setSize(320, 140);
        setLocationRelativeTo(owner);
    }

}
