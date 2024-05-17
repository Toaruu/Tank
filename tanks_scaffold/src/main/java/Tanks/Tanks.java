package Tanks;

import java.util.List;

import processing.core.PApplet;
import processing.core.PVector;

public class Tanks {
    private App app;

    // Tank properties
    private int x;
    private int y;
    private Position position;
    private Cannon cannon;
    private int fuel;
    private int health;
    private int power;
    private int[] color;
    private int parachutes;
    private boolean isFalling = false;
    private boolean parachuteDeployed = false;
    private boolean moveLeft = false;
    private boolean moveRight = false;
    private boolean isActive = true;
    private String id;
    private int score = 0;
    private boolean fallDueToExplosion = false; // Flag to indicate if the fall is due to an explosion
    public boolean shield = false;

    // Tank dimensions
    private int width = 20;
    private int height = 12;

    /**
     * Constructor for the Tanks class.
     * Initializes a tank with its basic attributes like position, color, and
     * initial stats.
     */
    public Tanks(App app, String id, int x, int y, int[] color) {
        this.x = x;
        this.y = y;
        this.app = app;
        this.id = id;
        this.position = new Position(x, y);
        this.color = color;
        this.cannon = new Cannon(8, 2);
        this.power = 50;
        this.health = 100;
        this.fuel = 250;
        this.parachutes = 3;
    }

    /**
     * Draws the tank on the game canvas.
     */
    public void draw(App app) {

        app.stroke(0);
        app.strokeWeight(1);

        // Draw the base of the tank
        app.fill(color[0], color[1], color[2]);
        float baseWidth = width;
        float baseHeight = (float) (height * 0.6);
        app.ellipse(position.getX() + width / 2, position.getY() + baseHeight / 2, baseWidth, baseHeight);

        // Draw the top of the tank
        float topWidth = (float) (width * 0.7);
        float topHeight = (float) (height * 0.4);
        app.ellipse(position.getX() + width / 2, position.getY(), topWidth, topHeight);

        // Draw the cannon
        cannon.draw(app, (int) (position.getX() + width / 2), (int) (position.getY() - topHeight / 2));

        // Draw the shield
        if (shield) {
            app.stroke(173, 216, 230);
            app.strokeWeight(2);
            app.fill(173, 216, 230, 100); // Light blue fill
            float shieldDiameter = Math.max(width, height) * 1.8f;
            app.ellipse(position.getX() + width / 2, position.getY() + height / 2 - 10, shieldDiameter, shieldDiameter);
        }
    }

    /**
     * Decreases the health of the tank by a specified amount. If the tank has a
     * shield,
     * the shield will absorb the damage instead. If health reaches zero, the tank
     * explodes.
     */
    public void decreaseHealth(int damage) {
        if (shield) {
            shield = false; // Disable shield if active
        } else {
            this.health -= damage;
            if (this.health < 0) {
                this.health = 0; // Ensure health does not go negative
            }
            if (this.power > this.health) {
                this.power = this.health; // Adjust power to not exceed current health
            }
            if (this.health <= 0) {
                explode(15); // Call explode if health drops to zero or below
            }
        }
    }

    /**
     * Activates the tank's shield.
     */
    public void activateShield() {
        this.shield = true;
    }

    /**
     * Checks if the tank's shield is currently active.
     */
    public boolean isShieldActive() {
        return shield;
    }

    /**
     * Handles the teleportation of the tank to a new position.
     * This is typically triggered by a game event or player action.
     */
    public void teleportation(int x, int[][] terrainHeights, int current_level) {
        int tankCenterX = x + 24 / 2; // Center of the tank
        if (tankCenterX < 0 || tankCenterX >= terrainHeights[current_level].length) {
            return;
        }
        this.position.setX(x); // Update the position object
        this.position.setY(640 - terrainHeights[current_level][tankCenterX] - height); // Adjust for height of the tank
    }

    /**
     * Checks if the tank is out of the game bounds and triggers an explosion if it
     * is.
     */
    public void checkOutOfBounds() {
        if (position.getY() >= 640) {
            explode(30);
        }
    }

    /**
     * Causes the tank to explode, rendering an explosion animation and deactivating
     * the tank.
     */
    private void explode(int explosionRadius) {
        app.fill(255, 0, 0);
        app.ellipse(position.getX(), position.getY(), explosionRadius * 2, explosionRadius * 2);
        isActive = false; // Set the tank as inactive after explosion

        if (this == app.getCurrentTank()) {
            app.nextTank();
        }
    }

    /**
     * Resets the tank's attributes to their initial values on new level.
     */
    public void resetTank(int x, int y) {
        position.x = x;
        position.y = y;
        fuel = 250;
        power = 50;
        health = 100;
        this.cannon.reset(x, y);
        this.parachutes = 3;
        this.isActive = true; // Reactivate the tank if previously deactivated
    }

    /**
     * Moves the tank to the left.
     */
    public void moveLeft(int[][] terrainHeights, int currentLevel, Tanks tanks, App app) {
        if (moveLeft && fuel > 0 && position.x > 0) {
            int potentialX = position.getX() - 2;
            position.setX(potentialX);
            updateVerticalPosition(currentLevel, terrainHeights, app);
            preventCollision(app.tankIdPositionCL);
            fuel--;
        }
    }

    /**
     * Moves the tank to the right.
     */
    public void moveRight(int[][] terrainHeights, int currentLevel, int screenWidth,
            Tanks tanks, App app) {
        if (moveRight && fuel > 0 && position.x < 864) {
            int potentialX = position.getX() + 2;
            position.setX(potentialX);
            updateVerticalPosition(currentLevel, terrainHeights, app);
            preventCollision(app.tankIdPositionCL);
            fuel--;
        }
    }

    /**
     * Prevents the tank from colliding with other tanks.
     */
    public void preventCollision(List<Tanks> allTanks) {
        for (Tanks other : allTanks) {
            if (other != this && Math.abs(this.getX() - other.getX()) < width) {
                if (this.getX() > other.getX()) {
                    this.setX(other.getX() + width);
                } else {
                    this.setX(other.getX() - width);
                }
            }
        }
    }

    /**
     * Updates the vertical position of the tank to align with the terrain.
     */
    public void updateVerticalPosition(int currentLevel, int[][] terrainHeights, App app) {
        int tankCenterX = position.getX() + 24 / 2;
        position.setY(640 - terrainHeights[currentLevel - 1][tankCenterX] - 8);

    }

    /**
     * Handles the falling mechanics for the tank if it is airborne.
     * This method will check if parachutes are available and use them if
     * applicable.
     */
    public void fall(int[][] terrainHeights, int currentLevel, App app) {
        int tankCenterX = position.getX() + width / 2;
        int terrainBelow = 640 - terrainHeights[currentLevel][tankCenterX] - 8;

        if (getY() < terrainBelow) {
            isFalling = true;
            if (fallDueToExplosion && parachutes > 0 && !parachuteDeployed) {
                // When parachutes are available and falling due to an explosion
                parachuteDeployed = true;
                parachutes--;
            } else if (fallDueToExplosion && parachutes <= 0 && !parachuteDeployed) {
                // When no parachutes are available and falling due to an explosion
                int fallRate = 120; // Increased fall rate when no parachutes are available
                int newY = position.getY() + fallRate;
                int damage = Math.min(newY, terrainBelow) - position.getY();
                decreaseHealth(damage); // Apply damage based on fall distance
                increaseScore(damage);
                position.setY(Math.min(newY, terrainBelow));
            }
        } else {
            stopFalling();
        }

        if (isFalling) {
            int newY;
            if (parachutes > 0 && parachuteDeployed) {
                // Fall with parachute
                newY = Math.min(position.getY() + 2, terrainBelow);
            } else {
                // Fall without parachute
                newY = Math.min(position.getY() + 4, terrainBelow);
            }

            if (fallDueToExplosion && !parachuteDeployed) {
                int damage = newY - position.getY(); // Damage is 1hp per pixel fallen
                decreaseHealth(damage);
            }

            position.setY(newY);

            if (position.getY() == terrainBelow) {
                stopFalling();
            }

            if (parachuteDeployed) {
                app.imageMode(PApplet.CENTER);
                app.image(app.parachute1, getX() + getWidth() / 2, getY() - app.parachute1.height / 2);
            }
        }
    }

    /**
     * Checks if the tank should start falling.
     */
    public void checkIfShouldFall(int[][] terrainHeights, int currentLevel) {
        int tankCenterX = this.getX() + this.getWidth() / 2;
        int terrainBelow = 640
                - terrainHeights[currentLevel][Math.min(tankCenterX, terrainHeights[currentLevel].length - 1)];
        if (this.getY() < terrainBelow) {
            this.isFalling = true;
        } else {
            this.isFalling = false;
            this.parachuteDeployed = false; // Reset parachute status when not falling.
        }
    }

    /**
     * Stops the tank from falling.
     */
    public void stopFalling() {
        isFalling = false;
        parachuteDeployed = false; // Ensure parachute is reset
        fallDueToExplosion = false; // Reset explosion falling flag
    }

    /**
     * Checks if the tank is currently active in the game.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Increases the tank's score by a specified amount.
     */
    public void increaseScore(int points) {
        this.score += points;
    }

    /**
     * Gets the current position of the cannon's end based on the cannon angle and
     * length.
     */
    public PVector getCannonEndPosition() {
        float x = position.getX() + width / 2 + PApplet.cos(cannon.getAngle()) *
                cannon.getHeight(); // Calculate end X based on angle and cannon length
        float y = position.getY() + PApplet.sin(cannon.getAngle()) *
                cannon.getHeight(); // Calculate end Y
        return new PVector(x, y);
    }

    /**
     * Setter and Getter methods.
     */
    public Cannon getCannon() {
        return this.cannon;
    }

    public int getHealth() {
        return this.health;
    }

    public void setHealth(int newHealth) {
        this.health = newHealth;
    }

    public int getFuel() {
        return this.fuel;
    }

    public void setFuel(int newFuel) {
        this.fuel = newFuel;
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int newScore) {
        this.score = newScore;
    }

    public int getPower() {
        return this.power;
    }

    public void setPower(int newPower) {
        int maxPower = Math.min(100, this.health);
        newPower = Math.max(0, Math.min(newPower, maxPower));
        this.power = newPower;
    }

    public void aimUp(float deltaTime) {
        cannon.increaseAngle(deltaTime);
    }

    public void aimDown(float deltaTime) {
        cannon.decreaseAngle(deltaTime);
    }

    public String getId() {
        return id;
    }

    public void setMoveLeft(boolean moveLeft) {
        this.moveLeft = moveLeft;
    }

    public void setMoveRight(boolean moveRight) {
        this.moveRight = moveRight;
    }

    public int getX() {
        return position.getX();
    }

    public int getY() {
        return position.getY();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setX(int x) {
        position.setX(x);
    }

    public void setY(int y) {
        position.setY(y);
    }

    public class Position {
        private int x;
        private int y;

        public Position(int x, int y) {
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
    }

    public int getParachutes() {
        return this.parachutes;
    }

    public void setParachutes(int parachutes) {
        this.parachutes = parachutes;
    }

    public boolean isParachuteDeployed() {
        return parachuteDeployed && parachutes > 0; // Only show parachute if available
    }

    public void setIsParachuteDeployed(boolean parachuteDeployed) {
        this.parachuteDeployed = parachuteDeployed;
    }

    // Method to trigger falling due to explosion.
    public void triggerFallDueToExplosion() {
        this.fallDueToExplosion = true;
    }

    // Reset falling due to explosion.
    public void resetFallDueToExplosion() {
        this.fallDueToExplosion = false;
    }

    // Reset parachute deployed.
    public void resetParachuteDeployed() {
        this.parachuteDeployed = false; // Reset when no longer falling
    }

    public boolean isFalling() {
        return this.isFalling;
    }

    public void setIsFalling(boolean isFalling) {
        this.isFalling = isFalling;
    }

    public int[] getColor() {
        return this.color;
    }

}
