package com.caesar.bean;

import java.awt.*;
import java.util.StringTokenizer;


/**
 * @author caesar
 */
public class Base {

    private int x;
    private int y;
    private Image image;

    protected Base(){

    }

    public Base(int x, int y, Image image) {
        this.x = x;
        this.y = y;
        this.image = image;
    }

    public Rectangle getRectangle(){
        return new Rectangle(x, y, image.getWidth(null), image.getHeight(null));
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void paint(Graphics g){
        g.drawImage(image, x, y, null);
    }

}
