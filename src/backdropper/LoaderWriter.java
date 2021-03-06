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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import java.util.zip.ZipOutputStream;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class LoaderWriter {

    final static ExtensionFilter ef1 = new ExtensionFilter("Any readable", "*.jpg", "*.png", "*.jpeg", "*.bdf", "*.JPG", "*.PNG", "*.JPEG", "*.BDF");
    final static ExtensionFilter ef2 = new ExtensionFilter("BD file (.bdf)", "*.bdf");
    final static ExtensionFilter ef3 = new ExtensionFilter("Edit any image", "*.jpg", "*.png", "*.jpeg", "*.JPG", "*.PNG", "*.JPEG");
    final static ExtensionFilter ef4 = new ExtensionFilter("PNG", "*.png", "*.PNG");
    final static ExtensionFilter ef5 = new ExtensionFilter("JPEG", "*.jpeg", "*.JPEG");
    final static ExtensionFilter ef6 = new ExtensionFilter("JPG", "*.jpg", "*.JPG");

    /**
     * Urges the user to elect a file to open, or where to save a file, through
     * a system call (using the native file manager). Ensures file chosen is of
     * valid format.
     *
     * @param read true if the dialogue and constraints should be for opening a
     * file, false if a (potentially new) file to save to should be produced.
     *
     * @param stage application-stage for drawing the window.
     * @return a file where data can be read/written.
     */
    public static File getUserFileChoice(boolean read, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        File file;
        if (read) {
            fileChooser.getExtensionFilters().addAll(ef1, ef2, ef3, ef4, ef5, ef6);
            fileChooser.setTitle("Choose a file to edit");
            file = fileChooser.showOpenDialog(stage);
        } else {
            fileChooser.getExtensionFilters().addAll(ef2, ef3, ef4, ef5, ef6);
            fileChooser.setTitle("Choose a save file");
            file = fileChooser.showSaveDialog(stage);
        }
        return file;
    }

    /**
     * Takes an image file and, given it is of a viable format, returns a
     * buffered image that of the 4-byte-ARGB-format. Terminates the program if
     * the file was unusable.
     *
     * @param file the File from which an image should be created.
     * @return buffered image from the passed file, in 4-byte-ARGB-format.
     * @throws IOException
     */
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

    /**
     * Produces the substring up to but not including the last '.' of a passed
     * string. Used to glean the name of a file. Returns "default" if format is
     * not as expected.
     *
     * @param fileName String to be truncated
     * @return substring of fileName up to last '.', or "default".
     */
    static String nameFromFile(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0) {
            return "default";
        } else {
            return fileName.substring(0, dotIndex);
        }
    }
    
    @Deprecated
    static BDEntry BDEntryFromBDFile(File file) throws IOException {
        ZipFile zip = new ZipFile(file);
        Enumeration<ZipEntry> entries;
        entries = (Enumeration<ZipEntry>) zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry nextElement = entries.nextElement();
            
            System.out.println("File contains " + nextElement.getName());
            
        }
        
        return null;
    }

    /**
     * Packages and stores passed data to a provided file. Used to save a work
     * in progress.
     *
     * @param entry data to be saved.
     * @param file file to save data to. Should be of the .bdf-type, but this
     * won't affect the behaviour of the method.
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    static void saveBDEntryToFile(BDEntry entry, File file) throws FileNotFoundException, IOException {
        FileOutputStream fOut = new FileOutputStream(file);
        System.out.println("writing to file");
        //BufferedOutputStream bOut = new BufferedOutputStream(fOut);
        CheckedOutputStream cOut = new CheckedOutputStream(fOut, new Adler32());
        BufferedOutputStream bOut = new BufferedOutputStream(cOut);
        ZipOutputStream zOut = new ZipOutputStream(bOut);
        
        final int BUFFER = 2048;
        byte data[] = new byte[BUFFER];
        BufferedInputStream bis;
        FileInputStream fis;

        File metaFile = entry.getMetaTempFile(file.getParent());
        ZipEntry zipEntry = new ZipEntry(metaFile.getAbsolutePath());
        zOut.putNextEntry(zipEntry);
        fis = new FileInputStream(metaFile);
        bis = new BufferedInputStream(fis);

        int i;
        while ((i = bis.read(data, 0, BUFFER)) != -1) {
            zOut.write(data, 0, i);
            System.out.println("wrote meta file: " + i + " bytes");
        }
        metaFile.delete();
        bis.close();
        
        for (Layer layer : entry.layers) {
            File layerFile = layer.getLayerFile(file.getParent());
            zipEntry = new ZipEntry(layerFile.getAbsolutePath());
            zOut.putNextEntry(zipEntry);

            fis = new FileInputStream(layerFile);
            bis = new BufferedInputStream(fis, BUFFER);

            while ((i = bis.read(data, 0, BUFFER)) != -1) {
                System.out.println("wrote layer: " + i + " bytes");
                zOut.write(data, 0, i);
            }
            
            bis.close();
            layerFile.delete();
        }

        zOut.close();
        fOut.close();
        //bOut.close();

        ZipFile zip = new ZipFile(file);
        Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry nextElement = entries.nextElement();
            System.out.println(nextElement.getName());
        }
    }

    /**
     * Produces an image from a provided entry and stores it to a provided image
     * file. File should be of .jpg-, .png- or .jpg-format.
     *
     * @param entry BDEntry to save.
     * @param file
     */
    static void exportBDEntry(BDEntry entry, File file) {
        BufferedImage image = new BufferedImage(entry.getWidth(), entry.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        for (Layer layer : entry.layers) {
            layer.drawLayer(g);
        }

        String name = file.getName();
        String type = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
        if (type.equals("jpg") || type.equals("jpeg")) { //jpg/jpeg can't handle alpha values, ImageIO does not handle this.
            BufferedImage rgbImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            rgbImg.getGraphics().drawImage(image, 0, 0, null);
            image = rgbImg;

        }

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

    public static void saveBDEntry(BDEntry entry, Stage stage) throws IOException {
        File file = getUserFileChoice(false, stage);
        if (file != null) { //true if a file is chosen.
            String lcName = file.getName().toLowerCase();
            if (lcName.endsWith(".png") || lcName.endsWith(".jpg") || lcName.endsWith(".jpeg")) {
                exportBDEntry(entry, file);
            } else if (lcName.endsWith(".bdf")) {
                saveBDEntryToFile(entry, file);
            } else {
                System.out.println("invalid file chosen");
            }
        }
    }

    /**
     * Creates and returns a new BDEntry based on user selected file.
     *
     * @param stage applicationStage where user can be asked for file selection.
     * @return a new BDEntry based on the file chosen by user (image or .bdf).
     * @throws IOException
     */
    static BDEntry openToBDEntry(Stage stage) throws IOException {
        File file = getUserFileChoice(true, stage);
        if (file == null) {
            System.out.println("No file chosen");
            return null;
        }
        String name = file.getName();
        String lcName = name.toLowerCase();
        if (lcName.endsWith(".png") || lcName.endsWith(".jpg") || lcName.endsWith(".jpeg")) {

            return new BDEntry(ImageFromFile(file), "Layer 1", nameFromFile(name));
        } else if (lcName.endsWith(".bdf")) {
            
            return BDEntryFromBDFile(file);
        } else { //Should not enter if correct extension filter has been applied. Hic sunt dracones.
            System.out.println("nonusable file");
            System.exit(1);
            return null;
        }
    }
}
