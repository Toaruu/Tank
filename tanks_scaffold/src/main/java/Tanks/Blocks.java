package Tanks;

import processing.core.PImage;

public class Blocks {
    protected int x;
    protected int y;

    public boolean hit;

    /**
     * Constructs blocks with initial settings.
     */
    public Blocks(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Draws the block at its current position using the specified image.
     */
    public void draw(App app, PImage tile) {
        app.image(tile, x, y);
    }

    /**
     * Sets the block's hit status to true and returns true.
     */
    public boolean gotHit() {
        this.hit = true;
        return true;
    }

    /**
     * Setter and Getter methods.
     */
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
