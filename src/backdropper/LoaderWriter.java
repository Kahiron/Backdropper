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

public class LoaderWriter {
    
    public BufferedImage original;

    public LoaderWriter() throws IOException {
        try {
            this.original = ImageIO.read(getClass().getClassLoader().getResource("res/image.jpg"));//temp. file solution TODO: fix
        } catch (IllegalArgumentException ex) {
            System.out.println("No file available");
            //System.exit(0);
        }
            
        //allow transparency editing irregardles of source image opacity.
        BufferedImage temp = new BufferedImage(original.getWidth(), original.getHeight(), original.TYPE_4BYTE_ABGR);
        temp.getGraphics().drawImage(original, 0, 0, null);
        original = temp;
    }
    
    public void ImageWriter(String s){
        File file = new File("src/res/Transp_Logo_" + s + ".png");//temp. file solution TODO: fix
        try {
            ImageIO.write(original, "png", file);
        } catch (IOException ex) {
            Logger.getLogger(LoaderWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
