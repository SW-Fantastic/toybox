package org.swdc.toybox.extension.fsmapper;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class FileViewUtil {

    private static ConcurrentHashMap<String, Image> imageIcons = new ConcurrentHashMap<>();

    private static int size = 64;

    public static Image getImageIcon(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        int dot = file.getName().indexOf(".");
        String extension = file.getName().substring(dot + 1);
        if (imageIcons.containsKey(extension)) {
            return imageIcons.get(extension);
        }

        FileSystemView view = FileSystemView.getFileSystemView();
        Icon icon = null;
        try {
            icon = view.getSystemIcon(file, size, size);
            if (icon == null) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        int width = icon.getIconWidth() > 0 ? icon.getIconWidth() : size;
        int height = icon.getIconHeight() > 0 ? icon.getIconHeight() : size;

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        icon.paintIcon(null, g2d, 0, 0);
        g2d.dispose();

        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
        imageIcons.put(extension, image);
        return image;
    }

}
