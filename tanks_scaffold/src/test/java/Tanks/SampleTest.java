package Tanks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class SampleTest extends PApplet {
    private PApplet p;
    private App app;
    private Tanks tank;

    @BeforeEach
    public void setup() {
        app = new App(); // Initialize your PApplet-based App class
        PApplet.runSketch(new String[] { "App" }, app);
        app.noLoop(); // Stop the draw loop to prevent continuous running during tests
    }

    @Test
    public void AppTest() {
        App app = new App();
        PApplet.runSketch(new String[] { "App" }, app);
        app.delay(1000); // Allow time for the sketch to initialize

        // Simulate pressing the right arrow key

        app.noLoop(); // Stop the draw loop if needed
    }

    KeyEvent space = new KeyEvent(app, System.currentTimeMillis(), 1, 0, ' ', ' ');
    KeyEvent up = new KeyEvent(app, System.currentTimeMillis(), 1, 0, ' ', PConstants.UP);
    KeyEvent down = new KeyEvent(app, System.currentTimeMillis(), 1, 0, ' ', PConstants.DOWN);
    KeyEvent left = new KeyEvent(app, System.currentTimeMillis(), 1, 0, ' ', PConstants.LEFT);
    KeyEvent right = new KeyEvent(app, System.currentTimeMillis(), 1, 0, ' ', PConstants.RIGHT);

    KeyEvent F = new KeyEvent(app, System.currentTimeMillis(), 1, 0, 'F', 70);
    KeyEvent H = new KeyEvent(app, System.currentTimeMillis(), 1, 0, 'H', 72);
    KeyEvent L = new KeyEvent(app, System.currentTimeMillis(), 1, 0, 'L', 76);
    KeyEvent R = new KeyEvent(app, System.currentTimeMillis(), 1, 0, 'R', 82);
    KeyEvent S = new KeyEvent(app, System.currentTimeMillis(), 1, 0, 'S', 83);
    KeyEvent T = new KeyEvent(app, System.currentTimeMillis(), 1, 0, 'T', 84);
    KeyEvent W = new KeyEvent(app, System.currentTimeMillis(), 1, 0, 'W', 87);

    MouseEvent press = new MouseEvent(app, System.currentTimeMillis(), 1, 0, 10,
            10, 1, 1);

    @Test
    public void Test() {
        App app = new App();

        PApplet.runSketch(new String[] { "App" }, app);
        app.delay(1000); // Allow time for the sketch to initialize
        app.noLoop();
        app.drawTrees();

        app.keyPressed(space);
        app.drawProjectiles();
        app.keyPressed(up);
        app.keyPressed(down);
        app.keyPressed(left);
        app.keyPressed(right);
        app.keyPressed(H);
        app.keyPressed(W);
        app.keyPressed(S);
        app.keyPressed(F);
        app.keyPressed(R);
        app.keyPressed(T);
        app.mousePressed(press);

        app.adminTest();
        app.allowWindUpdate = true;

        app.keyPressed(H);
        app.drawTanks();
        app.keyPressed(W);
        app.keyPressed(S);
        app.keyPressed(F);
        app.keyPressed(R);
        app.teleportMessage = "";
        app.keyPressed(T);
        app.draw();
        app.mousePressed(press);
        app.draw();

        app.adminTest2();

        app.keyPressed(H);
        app.keyPressed(W);
        app.keyPressed(S);
        app.keyPressed(F);
        app.keyPressed(R);
        app.keyPressed(T);
        app.mousePressed(press);

        app.keyPressed(L);
        delay(1000);
        app.drawTrees();

        for (int i = 0; i < 999; i++) {
            app.keyPressed(space);
            app.draw();
            app.drawTrees();
            app.drawTanks();
            app.drawProjectiles();
            app.displayHealthBar(p, tank);
        }

        for (int j = 0; j < 999; j++) {
            app.keyPressed(space);
            app.draw();
            app.drawTrees();
            app.drawTanks();
            app.drawProjectiles();
            app.displayHealthBar(p, tank);
        }

        app.keyPressed(L);
        delay(1000);
        app.drawTrees();

        app.keyPressed(L);
        delay(1000);
        app.drawTrees();

        app.keyPressed(R);

        app.changeMap(1);
        app.changeMap(2);
        app.changeMap(3);

    }

    @Test
    public void goToNextLevelTest() {
        App app = new App();

        PApplet.runSketch(new String[] { "App" }, app);
        app.delay(1000); // Allow time for the sketch to initialize
        app.noLoop();
        app.totalLevel = 3;
        app.current_level = 1;
        app.gameOver = false;
        app.levelOver = true;
        app.tankId.clear();
        app.keyPressed(L);
        delay(1000);
        app.goToNextLevel();
        app.keyPressed(space);
    }

    @Test
    public void gameOverTest() {
        App app = new App();
        app.gameOver = true;
        PApplet.runSketch(new String[] { "App" }, app);
        app.delay(1000); // Allow time for the sketch to initialize
        app.noLoop();

        app.exit();
    }

}
