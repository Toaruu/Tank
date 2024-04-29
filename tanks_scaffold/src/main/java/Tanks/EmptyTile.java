package Tanks;

import processing.core.PImage;

public class EmptyTile {
    protected int x;
    protected int y;

    public boolean hit;

    public EmptyTile(int x, int y) {
        this.x = x;
        this.y = y;
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

    public void draw(App app, PImage tile) {
        app.image(tile, x, y);
    }

    public boolean gotHit() {
        this.hit = true;
        return true;
    }
}
