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
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;

class PixelEntry implements Comparable<PixelEntry> {

    public Point coord;
    public int weight;
    public boolean read;
    public boolean changed;

    public PixelEntry(int x, int y, int weight) {
        this.coord = new Point(x, y);
        this.weight = weight;
        this.read = false;
        this.changed = false;
    }

    @Override
    public int compareTo(PixelEntry other) {
        return this.weight - other.weight;
    }
}

class UQItem<E extends Object> {

    E elem;
    UQItem<E> next;

    public UQItem(E elem /*, UQItem<E> next*/) {
        this.elem = elem;
        //this.next = next;
        this.next = null;
    }

    public E getElem() {
        return elem;
    }

    public UQItem<E> getNext() {
        return next;
    }

    public void setNext(UQItem<E> next) {
        this.next = next;
    }
}

/**
 * Hue Swap Model, HSM
 * @author Olav
 */
class HSM{
    int r2cAlpha;
    int g2cAlpha;
    int b2cAlpha;
    
    public HSM(int transform){
        this.r2cAlpha = ((transform >> 16) & 0xff);
        this.g2cAlpha = ((transform >> 8 ) & 0xff);
        this.b2cAlpha = ( transform        & 0xff);
        
        while (this.r2cAlpha + this.g2cAlpha + this.b2cAlpha > 0xff) {
            System.out.println("fugged up");
            this.r2cAlpha --;
            this.g2cAlpha --;
            this.b2cAlpha --;
        }
    }
    
    protected int hueSwap(Color col){
        return (int)((col.getRed() + col.getGreen() + col.getBlue()) /3);
        /*
        return Math.min(0xff,
                (col.getRed()   * r2cAlpha/0xff)  +
                (col.getGreen() * g2cAlpha/0xff)  + 
                (col.getBlue()  * b2cAlpha/0xff)  );
        */
    }
}

class CSM{
    HSM RSM;
    HSM GSM;
    HSM BSM;

    public CSM(int rTrnsf, int gTrnsf, int bTrnsf) {
        this.RSM = new HSM(rTrnsf);
        this.GSM = new HSM(gTrnsf);
        this.BSM = new HSM(bTrnsf);
    }
    
    public Color transformColor(Color col){
        return new Color((col.getRGB() & 0xff000000) + (RSM.hueSwap(col) << 16) + (GSM.hueSwap(col) << 8) + GSM.hueSwap(col));
    }
}

class UpdateQueue<E extends Object> {

    UQItem<E> first;

    public UpdateQueue() {
        first = null;
    }

    public UpdateQueue(Collection<E> col) {
        UQItem tmp = new UQItem<>(null);
        UQItem<E> current = tmp;
        for (E elem : col) {
            current.setNext(new UQItem<>(elem));
            current = current.next;
        }
        first = tmp.next;
    }

    public E pop() {
        if (first == null) {
            return null;
        }
        E ret = first.getElem();
        first = first.getNext();
        return ret;
    }

    public boolean isEmpty() {
        return first == null;
    }

    public boolean contains(E item) {
        UQItem<E> current = first;
        while (current != null) {
            if (current.elem == item) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    public boolean enqueMissing(E item) {
        if (first == null) {
            first = new UQItem<>(item);
        } else {
            UQItem<E> current = first;
            int counter = 1;
            while (current.next != null) {
                counter++;
                if (current.elem == item) {
                    return false;
                }
                current = current.next;
            }
            //System.out.println("remaining " + counter);
            //item not contained, and end of queue reached
            current.setNext(new UQItem<>(item));
        }
        return true;
    }
}

/**
 *
 * @author Olav
 */
public class Editor {
    
    public Editor(BufferedImage target) {
        this.target = target;
        int len, width;
        len = target.getHeight()*target.getWidth();
        width = target.getWidth();
        this.pixelList = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            pixelList.add(new PixelEntry(i % width, i / width, (int) colDist(TARG_COL, target.getRGB(i % width, i / width))));
        }
    }

    
    static ArrayList<PixelEntry> pixelList;
    static BufferedImage target;
    final static int TARG_COL = 0x000000;
    final static double CONTRAST_WEIGHT = 0.2;
    final static double MAX_COL_DIST = colDist(0, 0xffffff);

    /**
     * Returns distance in the three-dimensional colour space between two
     * colours. Accepts any integers representing colour types where the last
     * three bytes each give one of red, green and blue. Order must match
     * between passed colour values for return value to be correct.
     *
     * @param c1 first colour.
     * @param c2 second colour
     * @return distance between input colours.
     */
    private static double colDist(int c1, int c2) {
        int r1, r2, g1, g2, b1, b2; //presume rgb-format for readability's sake.

        r1 = (c1 >> 16) & 0xff;
        g1 = (c1 >> 8) & 0xff;
        b1 = c1 & 0xff;

        r2 = (c2 >> 16) & 0xff;
        g2 = (c2 >> 8) & 0xff;
        b2 = c2 & 0xff;

        return Math.sqrt(
                Math.pow(r1 - r2, 2)
                + Math.pow(g1 - g2, 2)
                + Math.pow(b1 - b2, 2));
    }

    /**
     * Returns indexes for neighbours of coordinate for listed elements in any
     * 2D-array. Always returns as 4-length index array; embeds index -1 for any
     * neighbour that does not exist (due to coord being on the border)
     *
     * @param coord item whose neighbours are to be identified
     * @param limit max x- and y-value for presumed array.
     * @return int[4] of indexes for neighbours, non-existing neighbours indexed
     * as -1.
     */
    static int[] getNeighbours(Point coord, Point limit) {
        int[] neighbours = new int[4];
        int current = (coord.y * limit.x) + coord.x;
        if (coord.x != 0) {
            neighbours[0] = current - 1;
        } else {
            neighbours[0] = -1;
        }

        if (coord.x != limit.x - 1) {
            neighbours[1] = current + 1;
        } else {
            neighbours[1] = -1;
        }

        if (coord.y != 0) {
            neighbours[2] = current - limit.x;
        } else {
            neighbours[2] = -1;
        }

        if (coord.y != limit.y - 1) {
            neighbours[3] = current + limit.x;
        } else {
            neighbours[3] = -1;
        }

        return neighbours;
    }

    static void colorSwap(int index){
        assert (index <= 27 && index >= 0);
        
        CSM csm = new CSM(0xff0000, 0xff00, 0xff);
        
        for (int x = 0; x < target.getWidth(); x++) {
            for (int y = 0; y < target.getHeight(); y++) {
                Color col = new Color(target.getRGB(x, y));
                int r, g, b;
                if (index < 9)
                    r = col.getRed();
                else if (index < 18)
                    r = col.getGreen();
                else r = col.getBlue();
                
                if (index % 9 < 3)
                    g = col.getRed();
                else if (index % 9 < 6)
                    g = col.getGreen();
                else g = col.getBlue();
                
                switch (index % 3) {
                    case 0:
                        b = col.getRed();
                        break;
                    case 1:
                        b = col.getGreen();
                        break;
                    default:
                        b = col.getBlue();
                        break;
                }
                
                target.setRGB(x, y, 0xff000000 + (r<<16) + (g<<8) + b);
                
                //target.setRGB(x, y, csm.transformColor(new Color(target.getRGB(x, y))).getRGB());
            }
        }
    }

    static boolean rebalanceWeight(PixelEntry pixel, ArrayList<PixelEntry> list, BufferedImage target) {
        int rowLen = target.getWidth();
        boolean res = false; //true if weight is altered

        int[] neighbors = getNeighbours(pixel.coord, new Point(target.getWidth(), target.getHeight()));

        for (int neighbor : neighbors) {
            if (neighbor != -1) {
                PixelEntry otherPixel = list.get(neighbor);
                int contrastWeight = otherPixel.weight + (int) (CONTRAST_WEIGHT * colDist(
                        target.getRGB(otherPixel.coord.x, otherPixel.coord.y),
                        target.getRGB(pixel.coord.x, pixel.coord.y)));
                res = res || pixel.weight > contrastWeight;
                pixel.weight = Integer.min(pixel.weight, contrastWeight);
            }
        }

        return res;
    }

    /**
     * Flood fill function for returning all borders of a presumed background
     * pixel. Borders being the first neighbouring non-background pixels.
     *
     * @param original
     * @param limit
     * @param pixel
     * @return
     */
    static ArrayList<PixelEntry> bordering(ArrayList<PixelEntry> original, Point limit, PixelEntry pixel) {
        ArrayList<PixelEntry> res = new ArrayList<>();

        if (pixel.read) {
            return res;
        }
        pixel.read = true;
        if (pixel.weight == 0) {
            for (int i : getNeighbours(pixel.coord, limit)) {
                if (i != -1) {
                    res.addAll(bordering(original, limit, original.get(i)));
                }
            }
        } else {
            res.add(pixel);
        }
        return res;
    }

    /**
     * Returns all borders
     *
     * @param originalList
     * @param limit
     * @return
     */
    @Deprecated
    static ArrayList<PixelEntry> getBGBorderOld(ArrayList<PixelEntry> originalList, Point limit) {
        ArrayList<PixelEntry> res = new ArrayList<>();
        originalList.stream().filter((pixel) -> (!pixel.read && pixel.weight == 0)).forEach((pixel) -> {
            res.addAll(bordering(originalList, limit, pixel));
        });
        originalList.stream().forEach((pixel) -> {
            pixel.read = false; //reset aid-bool
        });
        return res;
    }

    /**
     * Returns all borders
     *
     * @param originalList
     * @param limit
     * @return
     */
    static ArrayList<PixelEntry> getBGBorder(ArrayList<PixelEntry> originalList, Point limit) {
        ArrayList<PixelEntry> res = new ArrayList<>();
        for (PixelEntry pixel : originalList) {
            if (pixel.weight == 0) {
                for (int index : getNeighbours(pixel.coord, limit)) {
                    if (index != -1) {
                        PixelEntry neighbour = originalList.get(index);

                        if (neighbour.weight != 0 && !neighbour.read) {
                            neighbour.read = true;
                            res.add(neighbour);
                        }
                    }
                }
            }
        }

        for (PixelEntry pixel : originalList) {
            pixel.read = false;
        }

        System.out.println("finnished with border generation. border: " + res.size());
        return res;
    }
    
    /**
     * Given the current colour of a pixel and intended
     *
     * @param alpha
     * @param currCol
     * @return
     */
    static int rinsedRGB(int alpha, int currCol) {
        if (alpha == 0) {
            return TARG_COL; //full transparency lets through background, ensures no div. by zero.
        }
        
        alpha = alpha & 0xff; //Should allready be the case, but catch errors by stupid users
        
        int r = (currCol >> 16) & 0xff;
        int g = (currCol >> 8 ) & 0xff;
        int b = currCol & 0xff;
        
        
        int bgr = (TARG_COL >> 16) & 0xff;
        int bgg = (TARG_COL >> 8 ) & 0xff;
        int bgb = TARG_COL & 0xff;
        
        /*
        b > n (mörkt på ljust)
        
        kända: min(c) [c] = 0, 0xff [f], bg [b], newCol [n]
        sökes: min(a) [a]

        ca/f + b(f-a)/f = n
        b(f-a)/f = n
        b -ab/f = n
        b-n = ab/f
        
        a = f(b-n)/b
        
        b < n: (ljut på mörkt)
        
        kända: max(c) [c] = 0xff, 0xff [f], bg [b], newCol [n]
        sökes: min(a) [a]

        ca/f + b(f-a)/f = n
        a + b(f-a)/f = n
        a + b -ba/f = n
        a  -ba/f = n - b
        (1 - b/f)a = n-b
        
        a = (n-b)/(1 - b/f)
         */
        
        //n > b
        int minRAlpha, minGAlpha, minBAlpha;
        
        if (r == bgr)
            minRAlpha = 0;
        else if (r > bgr)
            minRAlpha = (r - bgr)/(1 - bgr/0xff);
        else
            minRAlpha = (0xff * (r - bgr)/bgr);
        
        if (g == bgg)
            minGAlpha = 0;
        else if (g > bgg)
            minGAlpha = (g - bgg)/(1 - bgg/0xff);
        else
            minGAlpha = (0xff * (g - bgg)/bgg);
        
        if (b == bgb)
            minBAlpha = 0;
        else if (b > bgb)
            minBAlpha = (b - bgb)/(1 - bgb/0xff);
        else
            minBAlpha = (0xff * (b - bgb)/bgb);
        
        
        
        if (minBAlpha > 0xff || minGAlpha > 0xff || minBAlpha > 0xff) {
            System.out.println("hic sunt dracones");
            
        }
        alpha = Math.min(0xff, Math.max(Math.max(alpha, minRAlpha), Math.max(minGAlpha, minBAlpha))); //alpha set to absolute minimum
        
        if (minBAlpha == alpha || minGAlpha == alpha || minBAlpha == alpha) {
            //System.out.println("alpha changed to "+ alpha);
        }
        
        /*
        alpha/0xff * col + (0xff - alpha)/ff * bgCol = newCol (formula for painting with nonopaque brush)
        ca/f + b(f-a)/f = n
        n, b, f, a kända (a antagen)
    
        ca/f = n - b(f-a)/f
        ca = nf - b(f-a)
        c = nf/a - b(f-a)/a = nf/a - bf/a + b
         */
        
        int newR = Math.max(0, Math.min(0xff, (int) (0xff * r / alpha) + TARG_COL - (TARG_COL * 0xff / alpha)));
        int newG = Math.max(0, Math.min(0xff, (int) (0xff * g / alpha) + TARG_COL - (TARG_COL * 0xff / alpha)));
        int newB = Math.max(0, Math.min(0xff, (int) (0xff * b / alpha) + TARG_COL - (TARG_COL * 0xff / alpha)));
        
        return (alpha << 24) +  (newR << 16) +  (newG << 8) +  newB;
    }

    public static void colorTest(){
        /*
        Graphics g = target.getGraphics();
        g.setColor(Color.black);
        g.fillRect(0, 0, target.getWidth(), target.getHeight());
        BufferedImage overlay = new BufferedImage(target.getWidth()/2, target.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        for (int i = 0; i < overlay.getWidth(); i++) {
            for (int j = 0; j < overlay.getHeight(); j++) {
                overlay.setRGB(i, j, 0xc00000ff);
            }
        }
        g.drawImage(overlay, 0, 0, null);
        
        g.setColor(new Color(0x0000c0));
        g.fillRect(overlay.getWidth(), 0, overlay.getWidth(), overlay.getHeight());
         */
        
        BufferedImage overlay = new BufferedImage(target.getWidth(), target.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        
        for (int i = 0; i < overlay.getWidth(); i++) {
            for (int j = 0; j < overlay.getHeight(); j++) {
                overlay.setRGB(i, j, rinsedRGB(0x01, target.getRGB(i, j)));
            }
        }
        Graphics g = target.getGraphics();
        g.setColor(Color.black);
        g.fillRect(0, 0, target.getWidth(), target.getHeight());
        
        //g.drawImage(overlay, 0, 0, null);
        //target = overlay;
        
        for (int i = 0; i < target.getWidth(); i++) {
            for (int j = 0; j < target.getHeight(); j++) {
                target.setRGB(i, j, overlay.getRGB(i, j));
            }
        }
    }
        
    static void recGradientReduce(PixelEntry pixel, Point limit, ArrayList<PixelEntry> list, int lastCol, BufferedImage target) {
        if (!pixel.read && pixel.weight != 0) {

            pixel.read = true;

            int col = target.getRGB(pixel.coord.x, pixel.coord.y);

            int preferedAlpha = (int) (0xff * colDist(col, TARG_COL) / MAX_COL_DIST);
            int newCol = rinsedRGB(preferedAlpha, col);
            

            target.setRGB(pixel.coord.x, pixel.coord.y, newCol);

            if (colDist(col, lastCol) < 0xc) {
                //System.out.println("gradient found");
                for (int index : getNeighbours(pixel.coord, limit)) {
                    if (index != -1) {
                        recGradientReduce(list.get(index), limit, list, col, target);
                    }
                }
            }
        }
        
        
    }

    static int coordToIndex(Point point){
        int maxLen = target.getWidth();
        return point.y * maxLen + point.x;
    }
    
    static void recTest(Point current, Point limit){
        
        if (pixelList.get(coordToIndex(current)).read){
            //System.out.println("found a read pixel");
            return;
        }
        pixelList.get(coordToIndex(current)).read = true;
                
        for (int index : getNeighbours(pixelList.get(coordToIndex(current)).coord, limit)) {
            if (index != -1)
            recTest(pixelList.get(index).coord, limit);
        }
    }

    public static void gradientAlpha(BufferedImage target) {
        //int pixelList[][] = new int[target.getWidth()][target.getHeight()];
        int len = target.getWidth() * target.getHeight();
        int width = target.getWidth();

        ArrayList<PixelEntry> pixelList = new ArrayList<>(len);
        System.out.println("size " + pixelList.size());
        for (int i = 0; i < len; i++) {
            pixelList.add(new PixelEntry(i % width, i / width, (int) colDist(TARG_COL, target.getRGB(i % width, i / width))));
        }
        
        //recTest(pixelList.get(0).coord, new Point(target.getWidth(), target.getHeight()));

        //pixelList.sort(null);
        //System.out.println(pixelList.get(0).weight + " " + pixelList.get(len - 1).weight);
        //System.out.println("" + MAX_COL_DIST);
        final int breakoff = 10;

        for (PixelEntry pixel : pixelList) {
            if (pixel.weight < breakoff) {
                pixel.weight = 0;
                //target.setRGB(pixel.coord.x, pixel.coord.y, 0xffff0000);
            } else {
                //    break;
            }
        }
        //all sufficiently close pixels are weighted as 0 for background.

        ArrayList<PixelEntry> bgBorder = getBGBorder(pixelList, new Point(target.getWidth(), target.getHeight()));
        //borders to background contained in bgBorder
        for (PixelEntry pixel : bgBorder) {
            //target.setRGB(pixel.coord.x, pixel.coord.y, 0xffffff00);
        }
        
        
        for (PixelEntry pixel : bgBorder) {
            recGradientReduce(pixel, new Point(target.getWidth(), target.getHeight()), pixelList, TARG_COL, target);    
        }
        
        for (PixelEntry pixel : pixelList) {
            if (pixel.weight == 0) {
                target.setRGB(pixel.coord.x, pixel.coord.y, 0x00ffffff);
            }
        }
        
        
        
        /*
        UpdateQueue<PixelEntry> toCheck = new UpdateQueue<>(bgBorder);
        //int remainingInQueue = bgBorder.size();
        while (!toCheck.isEmpty()) {
            PixelEntry pixel = toCheck.pop();
            //System.out.println(""+ (pixel == null));
            //int weight = pixel.weight;
            //remainingInQueue = remainingInQueue - 1;
            if (rebalanceWeight(pixel, pixelList, target)) {
                //target.setRGB(pixel.coord.x, pixel.coord.y, 0xffffff00);
                //System.out.println("reduced weigght");
                //System.out.println("reduced weight by " + (weight - pixel.weight) + " to " + pixel.weight);
                for (int index : getNeighbours(pixel.coord, new Point(target.getWidth(), target.getHeight()))) {
                    if (index != -1) {
                        //if (toCheck.enqueMissing(pixelList.get(index)))
                        toCheck.enqueMissing(pixelList.get(index));
                            //System.out.println("enqued missing");
                            //remainingInQueue = remainingInQueue + 1;
                    }
                }
            }
            //System.out.println("in loop");
            //System.out.println("remaining " + remainingInQueue);
        }
         */
 /*
        boolean unchanged = true;
        while (unchanged) {
            unchanged = !fillNeighbors(0, target, new PixelEntry(0, 0, (int) MAX_COL_DIST), pixelList.get(0), pixelList);
        }
         */

    }

    public static void alphaScaleByMatch(BufferedImage target) {

        int argb;

        for (int i = 0; i < target.getWidth(); i++) {
            for (int j = 0; j < target.getHeight(); j++) {
                argb = target.getRGB(i, j);
                double alphaFactor = colDist(argb, TARG_COL) / MAX_COL_DIST;
                //if (alphaFactor < 0.3)
                //    alphaFactor = Math.pow(alphaFactor, 2);
                //else
                //    alphaFactor= Math.sqrt(MAX_COL_DIST);
                int r, g, b;
                r = (argb >> 16) & 0xff;
                g = (argb >> 8) & 0xff;
                b = argb & 0xff;

                if (b < g || b < r || r + g + b < 0x80) {

                    int newAlpha = (int) (0xff * alphaFactor);

                    argb = (argb & 0xffffff) | newAlpha << 24;
                    target.setRGB(i, j, argb);
                }
            }
        }
    }
    
    /*
    static boolean fillNeighbors(int newRel, BufferedImage targ, PixelEntry oldPixel, PixelEntry pixel, ArrayList<PixelEntry> list) {
        if (pixel.read > newRel) {
            return false;
        }
        boolean core = (pixel.read == 0);
        pixel.read = newRel + 1;

        boolean altered = false;

        int altWeight = oldPixel.weight + (int) colDist(
                targ.getRGB(oldPixel.coord.x, oldPixel.coord.y),
                targ.getRGB(pixel.coord.x, pixel.coord.y));
        if (altWeight < pixel.weight) {
            pixel.weight = altWeight;
            altered = true;
            pixel.changed = true;
        }

        if (core || altered) {
            int rowLen = targ.getWidth();
            if (pixel.coord.x > 0 && pixel.coord.x - 1 != oldPixel.coord.x) //check pixel x-1
            {
                altered = altered || fillNeighbors(pixel.read, targ, pixel, list.get((pixel.coord.x - 1) + (pixel.coord.y * rowLen)), list);
            }
            if (pixel.coord.x < rowLen && pixel.coord.x + 1 != oldPixel.coord.x) //check pixel x+1
            {
                altered = altered || fillNeighbors(pixel.read, targ, pixel, list.get((pixel.coord.x + 1) + (pixel.coord.y * rowLen)), list);
            }
            if (pixel.coord.y > 0 && pixel.coord.y - 1 != oldPixel.coord.y) //check pixel y-1
            {
                altered = altered || fillNeighbors(pixel.read, targ, pixel, list.get((pixel.coord.x) + (pixel.coord.y * rowLen) - rowLen), list);
            }
            if (pixel.coord.y < targ.getHeight() && pixel.coord.y + 1 != oldPixel.coord.y) //check pixel y+1
            {
                altered = altered || fillNeighbors(pixel.read, targ, pixel, list.get((pixel.coord.x) + (pixel.coord.y * rowLen) + rowLen), list);
            }
        }
        return altered;
    }
     */
}
