package Tanks;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;

public class Projectiles {
    private PVector position;
    private PVector velocity;
    private PVector gravity = new PVector(0, 0.12f); // Gravity applied each frame
    private float windEffect;
    private App app;
    private Tanks shooter;

    // explosion variables
    private int[][] terrainHeights;
    public boolean hit = false;
    private int hitX = 0;
    private int hitY = 0;
    private int explosion = 0;
    private int animationDuration = 6;
    private boolean animationDone;
    private ArrayList<Tanks> activeTanks;
    private int currentLevel;

    /**
     * Constructs a projectile with initial settings.
     */
    public Projectiles(PVector position, float windEffect, Tanks shooter, int[][] terrainHeights,
            ArrayList<Tanks> tanksInCurrentLevel, int currentLevel,
            App app) {
        this.app = app;
        this.windEffect = windEffect;
        this.position = new PVector(position.x, position.y);
        this.terrainHeights = terrainHeights;
        this.activeTanks = tanksInCurrentLevel;
        this.currentLevel = currentLevel;
        this.shooter = shooter;
        initialPosition(shooter);
    }

    /**
     * Initializes the projectile's position and velocity.
     */
    public void initialPosition(Tanks tank) {
        int power = shooter.getPower();
        float velocityMagnitude = PApplet.map(power, 0, 100, 1, 9);
        float angle = shooter.getCannon().getAngle();
        angle = 3 * PApplet.HALF_PI + angle;
        velocity = PVector.fromAngle(angle).mult(velocityMagnitude);
        gravity = new PVector(0, 0.12f);
    }

    /**
     * Checks if the projectile has hit the terrain.
     */
    public void checkHitTerrain() {

        int posX = (int) position.x;
        int posY = (int) position.y;

        if (currentLevel < 0 || currentLevel >= terrainHeights.length || isOutOfBounds()) {
            return;
        }

        if (640 - terrainHeights[currentLevel][posX] <= posY) {
            hit = true;
            hitX = posX;
            hitY = posY;
        }

    }

    /**
     * Destroys the terrain within a specified radius around the hit location.
     */
    public void destroyTerrain(int radius) {
        int n;
        boolean fill;
        for (int i = hitX - radius; i <= hitX + radius; i++) {
            try {
                n = 0;
                fill = false;
                int originalHeights = 640 - terrainHeights[currentLevel][i];
                if (originalHeights < 0) {
                    originalHeights = 0;
                }
                int distance = (int) PApplet.dist(hitX, hitY, i, originalHeights);
                if (i >= 0 && i < terrainHeights[currentLevel].length) {
                    for (int j = hitY - radius; j <= hitY + radius; j++) {
                        if (PApplet.dist(hitX, hitY, i, j) <= radius) {
                            if (distance <= radius) {
                                terrainHeights[currentLevel][i] = 640 - j;
                            } else {
                                if (j <= originalHeights) {
                                    terrainHeights[currentLevel][i] = 640 - originalHeights;
                                } else {
                                    fill = true;
                                    n++;
                                }
                            }
                        }
                    }
                }
                if (fill) {
                    terrainHeights[currentLevel][i] = 640 - originalHeights - n;
                }
            } catch (Exception e) {
                continue;
            }
        }
    }

    /**
     * Checks for collisions between the projectile and any tank within the
     * explosion radius.
     */
    public void checkHitTank() {
        for (Tanks tank : activeTanks) {
            int distance = (int) PApplet.dist(hitX, hitY, tank.getX() + tank.getWidth() / 2,
                    tank.getY() + tank.getHeight() / 2);
            if (distance <= 30) { // Check if within explosion range
                int damage = calculateDamage(distance);
                tank.decreaseHealth(damage);
                if (!tank.equals(shooter)) { // Ensure the shooter doesn't gain points for hitting itself
                    shooter.increaseScore(damage);
                }
                if (damage > 0) {
                    // If the tank is within the damage range, we assume it's affected by the
                    // explosion
                    tank.triggerFallDueToExplosion();
                }
            }
        }
    }

    /**
     * Calculates the damage based on the distance from the hit point.
     */
    public int calculateDamage(int distance) {
        int maxDamage = 60;
        int explosionRadius = 30;
        return (int) PApplet.map(distance, 0, explosionRadius, maxDamage, 0);
    }

    /**
     * Updates the state of the projectile, checking for hits and managing the
     * explosion animation.
     */
    public void update() {
        checkHitTerrain();

        if (hit) {
            explosion++;
            if (explosion >= animationDuration) {
                animationDone = true;
                destroyTerrain(30);
                checkHitTank();
            }
        } else {
            velocity.add(gravity);
            position.x += windEffect;
            position.add(velocity);
        }
    }

    /**
     * Draws the projectile on the canvas.
     */
    public void draw(PApplet app) {
        update();

        if (hit) {
            if (explosion < animationDuration) {
                // Calculate and draw explosion circles
                float explosionRadius = PApplet.map(explosion, 0, animationDuration, 0, 30); // Red circle
                float orangeRadius = explosionRadius * 0.5f; // Half the size of the red circle
                float yellowRadius = explosionRadius * 0.2f; // One fifth the size of the red circle

                app.noFill();
                app.fill(255, 0, 0); // Red
                app.ellipse(hitX, hitY, explosionRadius * 2, explosionRadius * 2);
                app.fill(255, 165, 0); // Orange
                app.ellipse(hitX, hitY, orangeRadius * 2, orangeRadius * 2);
                app.fill(255, 255, 0); // Yellow
                app.ellipse(hitX, hitY, yellowRadius * 2, yellowRadius * 2);
                explosion++;
            } else {
                animationDone = true;
            }
        } else {
            // Draw the projectile if it hasn't hit yet
            app.stroke(255, 0, 0);
            app.strokeWeight(1);
            app.ellipse(position.x, position.y, 6, 6);
        }
    }

    /**
     * Checks if the projectile is out of the app's bounds.
     */
    public boolean isOutOfBounds() {
        return position.x < 0 || position.x > app.width || position.y > app.height;
    }

    /**
     * Setter and Getter methods.
     */
    public PVector getPosition() {
        return position;
    }

    public Tanks getShooter() {
        return shooter;
    }

    public int getPositionX() {
        return (int) position.x;
    }

    public int getPositionY() {
        return (int) position.y;
    }

    public boolean getAnimationDone() {
        return animationDone;
    }

    public boolean getHit() {
        return hit;
    }

    public float getWind() {
        return windEffect;
    }

    public void setWind(float effect) {
        this.windEffect = effect;
    }

    public int getHitX() {
        return hitX;
    }

    public int getHitY() {
        return hitY;
    }

    public int[][] getLandPixel() {
        return terrainHeights;
    }

}