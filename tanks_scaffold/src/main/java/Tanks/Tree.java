package Tanks;

import processing.core.PImage;

public class Tree {
    protected int x;
    protected int y;
    protected PImage treeImage;
    protected boolean outOfMap;
    protected boolean hit;

    /**
     * Constructs tree with initial settings.
     */
    public Tree(int x, int y, PImage treeImage) {
        this.x = x;
        this.y = y;
        this.treeImage = treeImage;
        this.hit = false;
        this.outOfMap = false;
    }

    /**
     * Draws the tree at its current position using the specified image, if it is
     * not out of map bounds.
     */
    public void draw(App app, PImage treeImage) {
        if (!outOfMap) {
            app.image(treeImage, x, y);
        }
    }

    /**
     * Sets the tree's hit status to true and returns true.
     */
    public boolean gotHit() {
        this.hit = true;
        return true;
    }

    /**
     * Setter and Getter methods.
     */

    public boolean isOutOfMap() {
        return this.outOfMap;
    }

    public void setTreeOutOfMap(boolean status) {
        this.outOfMap = status;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
