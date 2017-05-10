/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backdropper;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javafx.util.Pair;

/**
 *
 * @author olavo
 */
public class BDEntry {
    private ArrayList<BufferedImage> layers;
    private ArrayList<String> layerNames;
    private String entryName;
    
    public void renameLayer(int layer, String newName){
        //assert (layer >= 0 && layer < layers.size());
    }

    public BDEntry(ArrayList<BufferedImage> layers, ArrayList<String> layerNames, String entryName) {
        this.layers     = layers;
        this.layerNames = layerNames;
        this.entryName  = entryName;
    }
    
    public BDEntry(BufferedImage image, String LayerName, String entryName){
        this.layers     = new ArrayList<>();
        this.layers.add(image);
        this.layerNames = new ArrayList<>();
        this.layerNames.add(LayerName);
        this.entryName  = entryName;
    }
}

abstract class Layer{
    public abstract void drawLayer(Graphics g);
}

class TextLayer extends Layer{
    ArrayList<Text> texts;  
    @Override
    public void drawLayer(Graphics g) {
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
    
    @Override
    public void drawLayer(Graphics g) {
        g.drawImage(image, x, y, null);
    }
    
    
}