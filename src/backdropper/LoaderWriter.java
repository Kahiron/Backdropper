/*
 * The MIT License
 *
 * Copyright 2017 Olav Övrebö.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package backdropper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import java.util.zip.ZipFile; //to be used
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class LoaderWriter {

    public static File getUserFileChoice(boolean read, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        File file;
        if (read) {
            ExtensionFilter ef1 = new ExtensionFilter("Any readable", "*.jpg", "*.png", "*.jpeg", "*.bdf", "*.JPG", "*.PNG", "*.JPEG", "*.BDF");
            ExtensionFilter ef2 = new ExtensionFilter("BD file (.bdf)", "*.bdf");
            ExtensionFilter ef3 = new ExtensionFilter("Edit any image", "*.jpg", "*.png", "*.jpeg", "*.JPG", "*.PNG", "*.JPEG");
            ExtensionFilter ef4 = new ExtensionFilter("PNG", "*.png", "*.PNG");
            ExtensionFilter ef5 = new ExtensionFilter("JPEG", "*.jpeg", "*.JPEG");
            ExtensionFilter ef6 = new ExtensionFilter("JPG", "*.jpg", "*.JPG");
            fileChooser.getExtensionFilters().addAll(ef1, ef2, ef3, ef4, ef5);
            fileChooser.setTitle("Choose a file to edit");
            file = fileChooser.showOpenDialog(stage);
        } else {
            ExtensionFilter ef2 = new ExtensionFilter("BD file (.bdf)", "*.bdf");
            ExtensionFilter ef3 = new ExtensionFilter("Store as image", "*.jpg", "*.png", "*.jpeg", "*.JPG", "*.PNG", "*.JPEG");
            ExtensionFilter ef4 = new ExtensionFilter("PNG", "*.png", "*.PNG");
            ExtensionFilter ef5 = new ExtensionFilter("JPEG", "*.jpeg", "*.JPEG");
            ExtensionFilter ef6 = new ExtensionFilter("JPG", "*.jpg", "*.JPG");
            fileChooser.getExtensionFilters().addAll(ef2, ef3, ef4, ef5);
            fileChooser.setTitle("Choose a save file");
            file = fileChooser.showSaveDialog(stage);
        }
        return file;
    }

    public static BufferedImage ImageFromFile(File file) throws IOException {

        BufferedImage image;
        try {
            image = ImageIO.read(file);
            BufferedImage temp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            temp.getGraphics().drawImage(image, 0, 0, null); //ensures consistent argb-format regardless of loaded image opacity
            return temp;

        } catch (IllegalArgumentException ex) {
            System.out.println("File not available or usable.");
            System.exit(1);
            return null;    //hic sunt dracones
        }
    }

    static String nameFromFile(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == 0) {
            return "default";
        } else {
            return fileName.substring(0, dotIndex - 1);
        }
    }

    static void saveToBDEntry(BDEntry entry, Stage stage) {
        //to-be-implemented, to save as .bdf using utils.zip lib.
    }

    static void exportBDEntry(BufferedImage image, String name, Stage stage) {
        File file = getUserFileChoice(false, stage);
        if (file == null) {
            return; //user canceled save
        }
        name = file.getName();
        String type = name.substring(name.lastIndexOf('.') + 1);
        if (type.equals("jpg") || type.equals("png") || type.equals("jpeg")) {
            try {
                ImageIO.write(image, type, file);
            } catch (IOException ex) {
                Logger.getLogger(LoaderWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("Invalid file format");
        }
    }

    static BDEntry openToBDEntry(Stage stage) throws IOException {
        File file = getUserFileChoice(true, stage);
        if (file == null) {
            System.out.println("No file chosen");
            return null;
        }
        String name = file.getName();
        if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {

            return new BDEntry(ImageFromFile(file), "Layer 1", nameFromFile(name));
        } else if (name.endsWith(".bdf")) {
            //todo: implement using utils.zip
            return null;
        } else {
            System.out.println("nonusable file");
            System.exit(1);
            return null;
        }
    }
}
