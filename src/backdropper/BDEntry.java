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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class BDEntry {
    public ArrayList<Layer> layers;
    public String entryName;
    
    public BDEntry(ArrayList<Layer> layers, ArrayList<String> layerNames, String entryName) {
        this.layers     = layers;
        this.entryName  = entryName;
    }
    
    public BDEntry(BufferedImage image, String LayerName, String entryName){
        this.layers     = new ArrayList<>();
        this.layers.add(new ImageLayer(entryName, image, 0, 0));
        this.entryName  = entryName;
    }
}

abstract class Layer{
    public int x, y, height, width;
    public abstract void drawLayer(Graphics g);
    public abstract File getLayerFile();
}

class ImageLayer extends Layer{
    private String name;
    private BufferedImage image;
    int x, y;

    public ImageLayer(String name, BufferedImage image, int x, int y) {
        this.name = name;
        this.image = image;
        this.x = x;
        this.y = y;
    }
    
    public Graphics getGraphics(){
        
        return image.getGraphics();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    @Override
    public void drawLayer(Graphics g) {
        g.drawImage(image, x, y, null);
    }    

    @Override
    public File getLayerFile() {
        try {
            File f = File.createTempFile(name, null);
            ImageIO.write(image, "png", f);
            return f;
        } catch (IOException ex) {
            Logger.getLogger(ImageLayer.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}

class TextLayer extends Layer{
    ArrayList<Text> texts;  
    @Override
    public void drawLayer(Graphics g) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public File getLayerFile() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

class Text{

    public Text(String content, Font font, Color color, int x, int y) {
        this.content = content;
        this.font = font;
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
        
    String content;
    Font font;
    Color color;
    int x, y;
    
    protected void draw(Graphics g){
        g.setFont(font);
        g.setColor(color);
        g.drawString(content, x, y);
        
    }
}
