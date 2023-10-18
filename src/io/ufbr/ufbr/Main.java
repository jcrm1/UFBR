package io.ufbr.ufbr;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main {
	public static final String TITLE = "UFBR";
	private static final int acceptableWidth;
	private static final int acceptableHeight;
	static {
		System.setProperty("apple.awt.application.name", TITLE);
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		acceptableWidth = (int) (screenSize.getWidth() / 3);
		acceptableHeight = (int) (screenSize.getWidth() / 2);
	}
	public static final String DIR_TITLE = "Choose a directory";
	public static final String IMAGE_TITLE = "Choose an image or directory";
	private static File directory;
	private static JFileChooser fc = new JFileChooser();
	private static int imageCount = 0;
	private static final WindowListener windowListener = new WindowListener() {
		@Override
		public void windowClosed(WindowEvent e) {
			imageCount--;
			if (imageCount <= 0) System.exit(0);
		}

		@Override
		public void windowOpened(WindowEvent e) {
			
		}

		@Override
		public void windowClosing(WindowEvent e) {
			e.getWindow().dispose();
		}

		@Override
		public void windowIconified(WindowEvent e) {
			
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
			
		}

		@Override
		public void windowActivated(WindowEvent e) {
			
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
			
		}
	};
	public static void main(String[] args) {
		chooseDirectory();
		fc.setCurrentDirectory(directory);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setMultiSelectionEnabled(true);
		fc.setDialogTitle(IMAGE_TITLE);
		fc.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpeg", "jpg"));
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					openImages();
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	public static void chooseDirectory() {
		fc.setCurrentDirectory(null);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);
		fc.setDialogTitle(DIR_TITLE);
		int res = fc.showOpenDialog(null);
		if (res == JFileChooser.APPROVE_OPTION) {
			directory = fc.getSelectedFile();
		} else {
			chooseDirectory();
		}
	}
	public static void openImages() {
		ArrayList<File> images = new ArrayList<File>();
		while (true) {
			if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				File[] files = fc.getSelectedFiles();
				images.clear();
				for (File file : files) {
					if (file.isFile()) images.add(file);
					if (file.isDirectory()) {
						images.addAll(getFiles(file));
					}
				}
			} else {
				images.clear();
				break;
			}
			for (File file : images) {
				if (file.getName().startsWith(".")) continue;
				byte[] img = null;
				try {
					img = Base64.getDecoder().decode(Files.readString(file.toPath()).replaceAll("\n", ""));
				} catch (IllegalArgumentException | MalformedInputException e) {
					JOptionPane.showMessageDialog(null, file.getName() + " is not a Base64 encoded image", "Error", JOptionPane.ERROR_MESSAGE);
					continue;
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				if (img == null) continue;
				JFrame frame = new JFrame(file.getName());
				ImageIcon icon = new ImageIcon(img);
				frame.setSize(icon.getIconWidth() > acceptableWidth ? acceptableWidth : icon.getIconWidth(), icon.getIconHeight() > acceptableHeight ? acceptableHeight : icon.getIconHeight());
				frame.setLocationByPlatform(true);
				frame.add(new JScrollPane(new JLabel(icon)));
				imageCount++;
				frame.addWindowListener(windowListener);
				
				JMenuBar menuBar = new JMenuBar();
				JMenu menu = new JMenu("File");
				JMenuItem imageMenuItem = new JMenuItem("Open Image(s)");
				imageMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						openImages();
					}
				});
				imageMenuItem.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
				JMenuItem quitMenuItem = new JMenuItem("Quit");
				quitMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						System.exit(0);
					}
				});
				quitMenuItem.setAccelerator(KeyStroke.getKeyStroke('Q', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
				menu.add(imageMenuItem);
				menu.add(quitMenuItem);
				menuBar.add(menu);
				frame.setJMenuBar(menuBar);
				
				frame.setVisible(true);
			}
			images.clear();
		}
		if (images.size() == 0 && imageCount == 0) {
			System.exit(0);
		}
	}
	private static ArrayList<File> getFiles(File file) {
		ArrayList<File> files = new ArrayList<File>();
		if (file.isFile()) {
			files.add(file);
		} else if (file.isDirectory()) {
			File[] children = file.listFiles();
			if (children != null) {
				for (File child : children) {
					files.addAll(getFiles(child));
				}
			}
		}
		return files;
	}

}
