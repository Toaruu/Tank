package Tanks;

import processing.core.PApplet;
import processing.core.PVector;

public class Cannon {
    private float angle;
    private int height;
    private int thickness;
    private static final float ROTATION_SPEED = 3;
    private static final float MAX_ANGLE = PApplet.PI / 2;

    /**
     * Constructs a Cannon with specified height and thickness.
     */
    public Cannon(int height, int thickness) {
        this.height = height;
        this.angle = 0;
        this.thickness = thickness;
        // this.app = app;
    }

    /**
     * Draws the cannon on the application's canvas.
     */
    public void draw(App app, int x, int y) {
        app.pushMatrix();
        app.translate(x, y);
        // app.rotate(app.radians(angle));
        app.rotate(angle);
        app.fill(60); // Dark gray color for the cannon
        // app.rect(0, -thickness / 2, height, thickness);
        app.rect(-thickness / 2, -height, thickness, height);
        app.popMatrix();
    }

    /**
     * Resets the angle of the cannon to its default position.
     */
    public void reset(int x, int y) {
        this.angle = 0;
    }

    /**
     * Decreases the angle of the cannon, simulating rotation upwards.
     */
    public void decreaseAngle(float deltaTime) {
        if (angle < MAX_ANGLE) {
            angle += ROTATION_SPEED * deltaTime;
            angle = PApplet.min(angle, MAX_ANGLE);
            angle = PApplet.max(angle, -MAX_ANGLE); // Ensure angle does not go beyond 90 degrees
        }
    }

    /**
     * Increases the angle of the cannon, simulating rotation downwards.
     */
    public void increaseAngle(float deltaTime) {
        if (angle > -MAX_ANGLE) {
            angle -= ROTATION_SPEED * deltaTime;
            angle = PApplet.max(angle, -MAX_ANGLE);
            angle = PApplet.min(angle, MAX_ANGLE); // Ensure angle does not go beyond -90 degrees
        }
    }

    /**
     * Setter and Getter methods.
     */

    public PVector getCannonEndPosition() {
        float xEnd = this.height * PApplet.cos(angle);
        float yEnd = this.height * PApplet.sin(angle);
        return new PVector(xEnd, yEnd);
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getAngle() {
        return this.angle;
    }

    public int getHeight() {
        return this.height;
    }

}