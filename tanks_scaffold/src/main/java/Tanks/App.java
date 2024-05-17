package Tanks;

// import java.awt.*;
// import java.awt.geom.AffineTransform;
// import java.awt.image.BufferedImage;
import java.io.File;
// import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
// import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

// import org.checkerframework.checker.units.qual.A;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class App extends PApplet {

    public static final int CELLSIZE = 32; // 8;
    public static final int CELLHEIGHT = 32;

    public static final int CELLAVG = 32;
    public static final int TOPBAR = 0;
    public static int WIDTH = 864; // CELLSIZE*BOARD_WIDTH;
    public static int HEIGHT = 640; // BOARD_HEIGHT*CELLSIZE+TOPBAR;
    public static final int BOARD_WIDTH = WIDTH / CELLSIZE;
    public static final int BOARD_HEIGHT = 20;

    public static final int INITIAL_PARACHUTES = 1;

    public static final int FPS = 30;

    public String configPath;

    public static Random random = new Random();

    // Arrow
    private long arrowDisplayStart;
    private final int arrowDisplayTime = 2000;

    // Level
    private int numberOfLevel;
    private int[][] displayMap;
    private PImage currentBackground;
    public int totalLevel;

    // Background
    public PImage basic;
    public PImage desert;
    public PImage forest;

    // Other Images
    public PImage fuel;
    public PImage hills;
    public PImage parachute1;
    public PImage parachute2;
    public PImage snow;
    public PImage tree1;
    public PImage tree2;
    public PImage wind1;
    public PImage wind2;

    public JSONArray backgroundImage;
    public int current_level = 1;
    public int empty_i = 0;

    public Blocks[][] block;
    public Tree[][] tree;
    public EmptyTile[][] empty;

    // Terrain
    public int[][] initialTerrain;
    public int[][] terrainColor;
    private PImage[] background_image;
    private PImage[] tree_image;
    public ArrayList<ArrayList<Tree>> trees;
    private ArrayList<ArrayList<Blocks>> blocks;
    public ArrayList<Map<String, int[]>> tankIdPosition;
    public Map<String, Tanks> allTanks;
    private Map<String, int[]> tankColor;

    public int[][] terrainHeights;
    public JSONArray layout;

    // Tank
    public JSONObject playerColors;
    public Tanks tanks;
    public ArrayList<String> tankId;
    public ArrayList<Tanks> tankIdPositionCL;
    public int current_tank = 0;

    HashMap<Integer, Boolean> keys = new HashMap<>();

    // Projectiles
    public ArrayList<Projectiles> projectiles;

    // Wind
    public boolean allowWindUpdate = true;
    private int wind; // Initial wind value, can be between -35 to 35

    // Powerups
    private int repairKit = 0;
    private int additionalFuel = 0;

    // Gameplay
    public boolean switchLevel = false;
    private boolean switchLevelNow = false;
    public int switchFrame;
    public boolean levelOver;
    public boolean gameOver;
    public int playerCounter = 0;
    private int ArrowDuration = 60;
    public ArrayList<Tanks> List;
    public ArrayList<Tanks> Display;
    private int endScore = 0;
    private boolean isTeleportModeActive = false;
    public String teleportMessage = "";
    public boolean displayFinalScoresActive = false; // Flag to control scoreboard display

    // Feel free to add any additional methods or attributes you want. Please put
    // classes in different files.

    public App() {
        this.configPath = "config.json";
    }

    /**
     * Initialise the setting of the window size.
     */
    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Load all resources such as images. Initialise the elements such as the player
     * and map elements.
     */
    @Override
    public void setup() {
        frameRate(FPS);
        // See PApplet javadoc:
        // loadJSONObject(configPath)
        // loadImage(this.getClass().getResource(filename).getPath().toLowerCase(Locale.ROOT).replace("%20",
        // " "));
        tankColor = Color(this, configPath);

        this.snow = loadImage(
                this.getClass().getResource("snow.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        this.desert = loadImage(
                this.getClass().getResource("desert.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        this.basic = loadImage(
                this.getClass().getResource("basic.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        this.forest = loadImage(
                this.getClass().getResource("forest.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        this.fuel = loadImage(
                this.getClass().getResource("fuel.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        this.parachute1 = loadImage(
                this.getClass().getResource("parachute1.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        this.parachute2 = loadImage(
                this.getClass().getResource("parachute2.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        this.tree1 = loadImage(
                this.getClass().getResource("tree1.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        this.tree2 = loadImage(
                this.getClass().getResource("tree2.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        this.wind1 = loadImage(
                this.getClass().getResource("wind1.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        this.wind2 = loadImage(
                this.getClass().getResource("wind2.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        this.tree1.resize(CELLSIZE, CELLSIZE);
        this.tree2.resize(CELLSIZE, CELLSIZE);
        this.wind1.resize(48, 48);
        this.wind2.resize(48, 48);
        this.fuel.resize(22, 22);
        this.parachute2.resize(22, 22);

        parsingSetup();

        initialTerrain = new int[totalLevel][terrainHeights[current_level - 1].length];
        for (int i = 0; i < totalLevel; i++) {
            for (int j = 0; j < initialTerrain[0].length; j++) {
                initialTerrain[i][j] = terrainHeights[i][j];
            }
        }

        JSONObject conf = loadJSONObject(new File(this.configPath));
        backgroundImage = conf.getJSONArray("levels");
        numberOfLevel = backgroundImage.size();
        totalLevel = 3;
        displayMap = new int[numberOfLevel][3];
        projectiles = new ArrayList<>();

        currentBackground = snow;
        for (int i = 0; i < numberOfLevel; i++) {
            JSONObject level = backgroundImage.getJSONObject(i);
            String[] colorStr = level.getString("foreground-colour").split(",");
            for (int j = 0; j < 3; j++) {
                displayMap[i][j] = Integer.parseInt(colorStr[j].trim());
            }
        }

        block = new Blocks[backgroundImage.size()][28 * 20];
        tree = new Tree[backgroundImage.size()][28 * 20];
        empty = new EmptyTile[backgroundImage.size()][28 * 20];
        if (allowWindUpdate) {
            wind = (int) random(-35, 36);
        }

        tankId = new ArrayList<>();
        tankId.addAll(tankIdPosition.get(current_level - 1).keySet());
        Collections.sort(tankId);

        tanks = allTanks.get(tankId.get(0));
        tankIdPositionCL = new ArrayList<>();

        for (String tankid : tankIdPosition.get(current_level - 1).keySet()) {
            Tanks tanks = allTanks.get(tankid);
            if (tanks != null) {
                tankIdPositionCL.add(tanks);
            }
        }

        List = new ArrayList<>(allTanks.values());
        Display = new ArrayList<>();
    }

    /**
     * Parses the level configurations from the JSON file to set up the game levels
     * (creates terrain, tanks, and trees according to the layout specified in the
     * configuration file).
     */
    public void parsingSetup() {
        JSONObject config = loadJSONObject(configPath);
        JSONArray levels = config.getJSONArray("levels");

        totalLevel = levels.size();
        int position_x = 0;
        int position_y = 0;

        blocks = new ArrayList<>(totalLevel);
        trees = new ArrayList<>(totalLevel);
        terrainHeights = new int[totalLevel][WIDTH + 33];
        terrainColor = new int[totalLevel][3];
        background_image = new PImage[totalLevel];
        tree_image = new PImage[totalLevel];
        tankIdPosition = new ArrayList<>(totalLevel);
        allTanks = new HashMap<>();

        for (int i = 0; i < levels.size(); i++) {
            blocks.add(new ArrayList<Blocks>(850));
            trees.add(new ArrayList<Tree>(30));
            tankIdPosition.add(new HashMap<>());

            JSONObject levelConfig = levels.getJSONObject(i);
            File file = new File(levelConfig.getString("layout"));

            position_y = 0;

            String[] colors = levelConfig.getString("foreground-colour").split(",");
            for (int j = 0; j < 3; j++) {
                terrainColor[i][j] = Integer.parseInt(colors[j]);
            }

            // Importing and storing the background image
            String background = levelConfig.getString("background");
            background_image[i] = loadImage(
                    this.getClass().getResource(background).getPath().toLowerCase(Locale.ROOT).replace("%20", " "));

            // Importing and storing the tree image
            if (levelConfig.hasKey("trees")) {
                String tree_picture = levelConfig.getString("trees");
                tree_image[i] = loadImage(this.getClass().getResource(tree_picture).getPath().toLowerCase(Locale.ROOT)
                        .replace("%20", " "));
            }

            try {
                Scanner scan = new Scanner(file);
                while (scan.hasNextLine()) {
                    String[] alphabet = scan.nextLine().split("");
                    position_x = 0;

                    for (int k = 0; k < alphabet.length && k <= BOARD_WIDTH; k++) {
                        if (alphabet[k].equals("X")) {
                            terrainHeights[i][position_x] = 640 - position_y;
                            for (int l = 1; l < 32; l++) {
                                if (position_x + l < WIDTH + 33) {
                                    terrainHeights[i][position_x + l] = terrainHeights[i][position_x];
                                }
                            }

                            Blocks terrain = new Blocks(position_x, position_y);
                            blocks.get(i).add(terrain);
                            for (int below = position_y + CELLSIZE; below < BOARD_HEIGHT
                                    * CELLSIZE; below += CELLSIZE) {
                                terrain = new Blocks(position_x, below);
                                blocks.get(i).add(terrain);
                            }
                        } else if (alphabet[k].equals("T")) {
                            int treeX = (int) random(-15, 16);
                            int newTreeX = treeX + position_x;
                            if (newTreeX < 0 || newTreeX > WIDTH) {
                                newTreeX = position_x;
                            }
                            Tree t = new Tree(newTreeX, position_y, tree_image[i]);
                            trees.get(i).add(t);
                        } else if (alphabet[k].length() > 0 && Character.isLetter(alphabet[k].charAt(0))) {
                            String tankID = alphabet[k];
                            if (i == 0) {
                                Tanks tanks = new Tanks(this, tankID, position_x, position_y,
                                        tankColor.get(alphabet[k]));
                                allTanks.put(tankID, tanks);
                            }
                            int[] position = { position_x, position_y };
                            tankIdPosition.get(i).put(tankID, position);
                        }
                        position_x += CELLSIZE;
                    }
                    position_y += CELLSIZE;
                }
                applyMovingAverage(i, terrainHeights);
                applyMovingAverage(i, terrainHeights);
                updateTreePosition(i, trees, terrainHeights);
                updateTankPosition(current_level - 1, tankIdPosition, allTanks, terrainHeights);
                scan.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles the logic to transition from one level to the next (changing maps,
     * reinitializing tanks, and resetting game variables).
     */
    public void goToNextLevel() {
        if (!switchLevel) {
            switchLevel = true;
            switchFrame = millis();
        }

        if (millis() - switchFrame >= 1000 || switchLevelNow) {
            switchLevelNow = false;
            switchLevel = false;

            if (current_level < totalLevel) {
                current_level += 1;
                changeMap(current_level);
                reinitializeTank(tankIdPosition, current_level, allTanks);
                updateTankPosition(current_level - 1, tankIdPosition, allTanks, terrainHeights);
                tankIdPositionCL.clear();
                tankId.clear();

                tankId.addAll(tankIdPosition.get(current_level - 1).keySet());
                Collections.sort(tankId);

                for (String tankID : tankIdPosition.get(current_level - 1).keySet()) {
                    Tanks tanks = allTanks.get(tankID);
                    if (tanks != null) {
                        tanks.setIsFalling(false);
                        tankIdPositionCL.add(tanks);
                    }
                }

                current_tank = 0;
                projectiles.clear();
                if (allowWindUpdate) {
                    updateWind();
                    wind = (int) random(-35, 36);
                }
                levelOver = false;
                tanks = allTanks.get(tankId.get(current_tank));
            } else {
                gameOver = true;
            }
        }
    }

    /**
     * Resets the game to its initial state.
     * Clears all game entities like projectiles and resets all tanks and game
     * variables.
     */
    public void restartGame() {
        projectiles.clear();
        current_level = 1;
        tankId = new ArrayList<>();
        tankId.addAll(tankIdPosition.get(current_level - 1).keySet());
        Collections.sort(tankId);

        tanks = allTanks.get(tankId.get(0));
        tankIdPositionCL = new ArrayList<>(); // Initialize the tanks list

        for (String tankID : tankIdPosition.get(current_level - 1).keySet()) {
            Tanks tanks = allTanks.get(tankID);
            if (tanks != null) {
                tankIdPositionCL.add(tanks);
                tanks.setScore(0);
                tanks.setIsFalling(false);
            }
        }

        terrainHeights = new int[totalLevel][initialTerrain[0].length];
        for (int i = 0; i < totalLevel; i++) {
            for (int j = 0; j < terrainHeights[i].length; j++) {
                terrainHeights[i][j] = initialTerrain[i][j];
            }
        }

        reinitializeTank(tankIdPosition, current_level, allTanks);
        updateTankPosition(current_level - 1, tankIdPosition, allTanks, terrainHeights);
        updateTreePosition(current_level - 1, trees, terrainHeights);
        gameOver = false;
        playerCounter = 0;
        levelOver = false;
        current_tank = 0;
        if (allowWindUpdate) {
            updateWind();
            wind = (int) random(-35, 36);
        }
        displayFinalScoresActive = false;
    }

    /**
     * Reinitializes tanks' positions based on the current level config.
     */
    public static void reinitializeTank(ArrayList<Map<String, int[]>> tankIdPosition, int current_level,
            Map<String, Tanks> allTanks) {
        Map<String, int[]> tankLocations = tankIdPosition.get(current_level - 1);
        for (Map.Entry<String, int[]> data : tankLocations.entrySet()) {
            String tankId = data.getKey();
            int[] location = data.getValue();
            int x = location[0];
            int y = location[1];

            Tanks tanks = allTanks.get(tankId);
            if (tanks != null) {
                tanks.resetTank(x, y);
            }
        }
    }

    /**
     * Parses and returns a mapping of tank colors from a configuration path.
     */
    public static Map<String, int[]> Color(PApplet app, String configPath) {
        JSONObject config = app.loadJSONObject(configPath);
        JSONObject tankColors = config.getJSONObject("player_colours");

        Map<String, int[]> tankColor = new HashMap<>();
        @SuppressWarnings("unchecked")
        Set<String> keys = tankColors.keys();

        for (String key : keys) {
            String colorsInTank = tankColors.getString(key);
            int[] extractedColor = parseColor(colorsInTank);
            tankColor.put(key, extractedColor);
        }

        return tankColor;
    }

    /**
     * Helper method to parse RGB color values from a string.
     */
    public static int[] parseColor(String key) {
        int[] rgb = new int[3];
        if (key.equals("random")) {
            rgb[0] = (int) Math.random() * 256;
            rgb[1] = (int) Math.random() * 256;
            rgb[2] = (int) Math.random() * 256;
            return rgb;
        } else {
            String[] color = key.split(",");
            for (int i = 0; i < 3; i++) {
                rgb[i] = Integer.parseInt(color[i]);
            }
            return rgb;
        }
    }

    /**
     * Changes the map to the next level and update background.
     */
    public void changeMap(int level) {
        switch (current_level) {
            case 1:
                currentBackground = snow;
                break;
            case 2:
                currentBackground = desert;
                break;
            case 3:
                currentBackground = basic;
                break;
            default:
                currentBackground = snow;
                break;
        }

        current_level = level;
    }

    /**
     * Smoothing the Terrain.
     */
    private void applyMovingAverage(int current_level, int[][] heights) {
        int range = 32;
        for (int i = 0; i < terrainHeights[current_level].length - range; i++) {
            int total = 0;
            for (int j = i; j < i + range; j++) {
                total += terrainHeights[current_level][j];
            }
            int calculatedAverage = total / range;
            terrainHeights[current_level][i] = calculatedAverage;
        }
    }

    /**
     * Receive key pressed signal from the keyboard.
     */
    @Override
    public void keyPressed(KeyEvent event) {
        float deltaTime = 1.0f / frameRate;
        switch (event.getKeyCode()) {
            case LEFT:
                tanks.setMoveLeft(true);
                tanks.moveLeft(terrainHeights, current_level, tanks, this);
                break;
            case RIGHT:
                tanks.setMoveRight(true);
                tanks.moveRight(terrainHeights, current_level, WIDTH, tanks, this);
                break;
            case UP:
                tanks.aimUp(deltaTime);
                break;
            case DOWN:
                tanks.aimDown(deltaTime);
                break;
            case 'W':
                int increment = (int) (36 * deltaTime);
                int newPower = Math.min(tanks.getPower() + increment, 100);
                tanks.setPower(newPower);
                break;
            case 'S':
                int decrement = (int) (36 * deltaTime);
                newPower = Math.max(tanks.getPower() - decrement, 0);
                tanks.setPower(newPower);
                break;
            case ' ':
                if (!gameOver) {
                    if (tankId.size() != 0) {
                        fireProjectile(tanks);
                        nextTank();
                        arrowDisplayStart = millis();
                        updateWind();
                    } else {
                        levelOver = true;
                    }
                    frameCount = 0;
                    if (switchLevel) {
                        switchLevelNow = true;
                    }
                }
                break;
            case 'R':
                if (gameOver) {
                    restartGame();
                } else if (!gameOver) {
                    if (tanks.getScore() >= 20) {
                        repairKit += 1;
                        tanks.setScore(tanks.getScore() - 20);
                    }
                    if (repairKit > 0) {
                        if (tanks.getHealth() < 80) {
                            tanks.setHealth(tanks.getHealth() + 20);
                        } else {
                            tanks.setHealth(100);
                        }
                        repairKit--;
                    }
                }

            case 'F':
                if (tanks.getScore() >= 10) {
                    additionalFuel += 1;
                    tanks.setScore(tanks.getScore() - 20);
                }
                if (additionalFuel > 0) {
                    if (tanks.getFuel() < 50) {
                        tanks.setFuel(tanks.getFuel() + 200);
                    } else {
                        tanks.setFuel(250);
                    }
                    additionalFuel--;
                }
            case 'H':
                if (tanks.getScore() >= 20 && !tanks.isShieldActive()) {
                    tanks.activateShield();
                    tanks.setScore(tanks.getScore() - 20);
                }
                break;
            case 'T':
                if (tanks.getScore() >= 15 && !isTeleportModeActive) {
                    isTeleportModeActive = true;
                    teleportMessage = "Press anywhere to teleport";
                }
                break;
            case 'L':
                // Trigger switch to next level or end game if it's the last level
                if (current_level < totalLevel) {
                    current_level++;
                    changeMap(current_level);
                    goToNextLevel();
                    reinitializeTank(tankIdPosition, current_level, allTanks);
                    updateTankPosition(current_level - 1, tankIdPosition, allTanks, terrainHeights);
                } else {
                    gameOver = true; // End game if no more levels are available
                }
                break;

        }

    }

    /**
     * Fire projectile from current tank's position with the wind effect.
     */
    public void fireProjectile(Tanks tanks) {
        PVector startPosition = tanks.getCannonEndPosition();
        float windEffect = this.wind * 0.03f;
        Projectiles projectile = new Projectiles(startPosition, windEffect, tanks, terrainHeights, tankIdPositionCL,
                current_level - 1, this);
        projectiles.add(projectile);
    }

    /**
     * Change turn into next player.
     */
    public void nextTank() {
        // Find the current tank index
        int currentIndex = tankId.indexOf(tanks.getId());
        int nextIndex = (currentIndex + 1) % tankId.size();
        while (!allTanks.get(tankId.get(nextIndex)).isActive()) {
            nextIndex = (nextIndex + 1) % tankId.size();
            if (nextIndex == currentIndex) { // Prevent infinite loop if all tanks are inactive
                return;
            }
        }
        tanks = allTanks.get(tankId.get(nextIndex));
    }

    /**
     * Updates the tanks' positions based on the terrain heights to ensure they are
     * on the ground.
     */
    public static void updateTankPosition(int current_level, ArrayList<Map<String, int[]>> tankIdPosition,
            Map<String, Tanks> allTanks, int[][] terrainHeights) {
        int positionX;
        int new_positionY;
        for (String tankid : tankIdPosition.get(current_level).keySet()) {
            Tanks tank = allTanks.get(tankid);
            if (tank != null) {
                positionX = tank.getX() + 12;
                new_positionY = 640 - terrainHeights[current_level][positionX] - 8;
                tank.setY(new_positionY);
            }
        }
    }

    /**
     * Updates the position of trees based on the terrain heights to ensure they are
     * correctly placed on the ground.
     */
    public static void updateTreePosition(int current_level, ArrayList<ArrayList<Tree>> tree, int[][] terrainHeights) {
        int positionX;
        int new_positionY;
        for (Tree t : tree.get(current_level)) {
            if (t != null) {
                positionX = t.getX() + 16;
                new_positionY = 640 - terrainHeights[current_level][positionX] - 32;
                t.setY(new_positionY);
                if (new_positionY + 32 >= 640) {
                    t.setTreeOutOfMap(true);
                }
            }
        }
    }

    /**
     * Updates the wind effect, randomly changing its strength and direction.
     */
    public void updateWind() {
        if (allowWindUpdate) {
            wind += (int) random(-5, 6);
        }
    }

    /**
     * Receive key released signal from the keyboard.
     */
    @Override
    public void keyReleased(KeyEvent event) {
    }

    /**
     * Handles mouse pressed input.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        // TODO - powerups, like repair and extra fuel and teleport
        if (isTeleportModeActive) {
            int mouseX = e.getX();
            int newTankX = Math.min(Math.max(mouseX, 0), WIDTH - tanks.getWidth()); // Ensure within bounds
            tanks.teleportation(newTankX, terrainHeights, current_level - 1); // Check array index, ensure it's valid
            tanks.setScore(tanks.getScore() - 15); // Deduct the cost
            isTeleportModeActive = false;
            teleportMessage = "";
        }
    }

    /**
     * Handles mouse released input.
     */
    @Override
    public void mouseReleased(MouseEvent e) {

    }

    /**
     * Draw all elements in the game by current frame.
     */
    @Override
    public void draw() {
        background(currentBackground);

        displayMap(this, background_image, current_level, terrainColor, terrainHeights);

        drawTerrain();
        drawTrees();
        drawTanks();
        drawProjectiles();

        // ----------------------------------
        // display HUD:
        // ----------------------------------
        // TODO

        if (millis() - arrowDisplayStart < arrowDisplayTime) {
            drawArrow(tanks);
        }

        // draw Current Player's Turn Info

        fill(0);
        textAlign(LEFT, TOP);
        text("Player " + tanks.getId() + "'s turn", 10, 15);

        fill(0);
        textAlign(LEFT, TOP);

        fill(0);
        textAlign(CENTER, TOP);
        text("Health: ", 430, 15);
        text("Power: " + tanks.getPower(), 438, 50);
        text(tanks.getHealth(), 630, 15);

        // Draw health bar for the current active tank
        displayHealthBar(this, tanks);

        // draw Fuel
        image(fuel, 175, 10);
        fill(0);
        textAlign(RIGHT, TOP);
        text(tanks.getFuel(), 225, 15);

        // draw Parachutes
        image(parachute2, 175, 40);
        fill(0);
        textAlign(RIGHT, TOP);
        text(tanks.getParachutes(), 210, 45);

        // draw Wind
        if (wind < 0) {
            image(wind1, width - 100, 0);
        } else {
            image(wind2, width - 100, 0);
        }

        fill(0);
        textAlign(RIGHT, TOP);
        text(wind, width - 20, 15);

        // ----------------------------------
        // display scoreboard:
        // ----------------------------------
        // TODO

        if (gameOver) {
            displayFinalScoresActive = true;
        }

        if (!displayFinalScoresActive) {
            displayScoreboard();
        } else {
            playerCounter = displayFinalScoreboard(this, List, 35, endScore, playerCounter, Display);
            fill(255, 0, 0); // Font color for the restart message
            text("Game Over! Press 'R' to Restart", (width / 2) + 120, height / 2 + 80);
        }

        // ----------------------------------
        // ----------------------------------

        // TODO: Check user action

        if (!gameOver) {
            if (frameCount < ArrowDuration) {
                drawArrow(tanks);
            }

            if (levelOver) {
                goToNextLevel();
                if (frameCount < ArrowDuration) {
                    drawArrow(tanks);
                }
            }
        }

        if (!teleportMessage.isEmpty()) {
            fill(255, 0, 0); // White color for the text
            textAlign(CENTER, CENTER);
            text(teleportMessage, WIDTH / 2, HEIGHT / 2); // Display at center of the screen
        }

        endScore++;
        frameCount++;
    }

    /**
     * Displays the game map, setting the background image and color for the
     * terrain.
     */
    public static void displayMap(PApplet p, PImage[] bgImg, int current_level, int[][] terrainColor,
            int[][] terrainHeights) {
        p.image(bgImg[current_level - 1], 0, 0);
        p.fill(terrainColor[current_level - 1][0], terrainColor[current_level - 1][1],
                terrainColor[current_level - 1][2]);

    }

    /**
     * Displays the game's scoreboard, showing each player's score.
     */
    public void displayScoreboard() {
        int scoreboardX1 = width - 75;
        int scoreboardY1 = 75;
        int scoreboardX2 = width - 25;
        int scoreboardY2 = 75;
        int scoreBoardX = width - 87;
        int scoreBoardY = 50;

        stroke(0); // Black border
        strokeWeight(2);
        noFill();
        line(width - 150, 50, width - 10, 50); // Top
        line(width - 150, 160, width - 10, 160); // Bottom
        line(width - 150, 72, width - 10, 72); // Middle
        line(width - 150, 50, width - 150, 160); // Left
        line(width - 10, 50, width - 10, 160); // Right

        textAlign(RIGHT, TOP);
        textSize(16);
        text("Scores", scoreBoardX, scoreBoardY);

        for (String tankID : tankId) {
            Tanks tank = allTanks.get(tankID);
            if (tank != null) {
                int[] color = tank.getColor();
                fill(color[0], color[1], color[2]); // Set text color to the tank's color

                textAlign(RIGHT, TOP);
                text("Player " + tankID, scoreboardX1, scoreboardY1);
                fill(0); // Reset text color for score to black
                text(tank.getScore(), scoreboardX2, scoreboardY2);

                scoreboardY1 += 20; // Move down for the next entry
                scoreboardY2 += 20;
            }
        }
    }

    /**
     * Draws the game's terrain based on the current level's heights.
     */
    public void drawTerrain() {
        // Draw smoothed terrain
        noStroke();
        beginShape();
        vertex(0, HEIGHT);
        for (int i = 0; i < WIDTH; i++) {
            vertex(i, HEIGHT - terrainHeights[current_level - 1][i]);
        }
        vertex(WIDTH, HEIGHT);
        endShape(CLOSE);
    }

    /**
     * Draws trees on the map.
     */
    public void drawTrees() {
        // Draw trees
        Iterator<Tree> treeIterator = trees.get(current_level - 1).iterator();
        while (treeIterator.hasNext()) {
            Tree tree = treeIterator.next();
            if (tree.isOutOfMap()) {
                treeIterator.remove();
            }
            if (current_level == 1) {
                PImage treeImg = tree2;
                image(treeImg, tree.getX(), tree.getY());
            } else if (current_level == 3) {
                PImage treeImg = tree1;
                image(treeImg, tree.getX(), tree.getY());
            }
        }
    }

    /**
     * Draws all active tanks on the map.
     */
    public void drawTanks() {
        Iterator<Tanks> iteratingTanks = tankIdPositionCL.iterator();
        while (iteratingTanks.hasNext()) {
            Tanks tank = iteratingTanks.next();
            // Remove the tank if it is not active
            tank.checkOutOfBounds();

            if (!tank.isActive()) {
                iteratingTanks.remove();
                continue;
            }

            if (tankIdPositionCL.size() <= 1) {
                levelOver = true;
            }

            // Check for and update falling state
            tank.checkIfShouldFall(terrainHeights, current_level - 1);

            // If the tank is falling, handle the fall mechanics
            if (tank.isFalling()) {
                tank.fall(terrainHeights, current_level - 1, this);
            }

            tank.draw(this); // Draw the tank
        }

        imageMode(CORNER); // Reset the image mode for other uses
    }

    /**
     * Draws all active projectiles, handling their motion and impact.
     */
    public void drawProjectiles() {
        Iterator<Projectiles> projectile = projectiles.iterator();
        while (projectile.hasNext()) {
            Projectiles shooter = projectile.next();
            noStroke();
            shooter.draw(this);
            if (shooter.getHit()) {
                terrainHeights = shooter.getLandPixel();
                updateTreePosition(current_level - 1, trees, terrainHeights);
            }
            if (shooter.getAnimationDone()) {
                projectile.remove();
            }
            if (shooter.getPositionX() > 865 || shooter.getPositionX() < 0 || shooter.getPositionY() > 640
                    || shooter.getHitY() < 0) {
                projectile.remove();
            }
        }
    }

    /**
     * Draws arrow above the tank of the current player's turn.
     */
    private void drawArrow(Tanks tank) {
        int arrowX = tank.getX() + tank.getWidth() / 2; // Center X above the tank
        int arrowY = tank.getY() - tank.getHeight() - 20; // Arrow starts 30 pixels above the tank

        stroke(0); // Black color for the arrow line
        fill(0); // Black color for the arrowhead
        strokeWeight(3); // Set the thickness of the arrow shaft

        // Draw the arrow shaft
        line(arrowX, arrowY - 20, arrowX, arrowY - 60); // Line going downwards from the arrowhead base

        // Draw the arrowhead pointing downwards
        beginShape();
        vertex(arrowX, arrowY); // Top point of the arrowhead
        vertex(arrowX - 10, arrowY - 20); // Left point of the arrowhead
        vertex(arrowX + 10, arrowY - 20); // Right point of the arrowhead
        endShape(CLOSE);

        strokeWeight(1);
    }

    /**
     * Displays the healthbar for the current player's tank.
     */
    public void displayHealthBar(PApplet p, Tanks tanks) {
        if (tanks == null) {
            return;
        }

        int[] color = tanks.getColor();
        if (color == null || color.length < 3) {
            return;
        }

        p.rectMode(PApplet.CORNER);
        p.noStroke();
        p.fill(255);
        p.rect(460, 15, 150, 20);
        p.fill(color[0], color[1], color[2]); // Use validated color data
        p.rect(460, 15, (float) 1.5 * tanks.getHealth(), 20);
        p.noFill();
        p.stroke(0);
        p.strokeWeight(5);
        p.rect(460, 15, 150, 20);
        p.stroke(150);
        p.strokeWeight(4);
        p.rect(460, 15, (float) 1.5 * tanks.getPower(), 20);
        p.stroke(255, 165, 0);
        p.line(460 + (float) 1.5 * tanks.getPower(), 10, 460 + (float) 1.5 * tanks.getPower(), 38);
    }

    /**
     * Displays the final scoreboard at the end of the game, ranking players by
     * their scores.
     */
    public static int displayFinalScoreboard(PApplet p, ArrayList<Tanks> tankList, int inc2, int endScoreFrame,
            int endScore, ArrayList<Tanks> tanksDisplayed) {

        Comparator<Tanks> comparison = (Tanks i, Tanks j) -> {
            if (i.getScore() != j.getScore()) {
                return j.getScore() - i.getScore(); // Descending score
            } else {
                return i.getId().compareTo(j.getId()); // Ascending ID
            }
        };
        Collections.sort(tankList, comparison);

        int yPosition = 190;
        p.rectMode(PApplet.CORNER);
        p.fill(Math.min(tankList.get(0).getColor()[0] + 100, 255), Math.min(tankList.get(0).getColor()[1] + 100, 255),
                Math.min(tankList.get(0).getColor()[2] + 100, 255), 99);
        p.stroke(255);
        p.strokeWeight(5);

        // Displaying each tank in sorted order
        for (int i = 0; i < tankList.size(); i++) {
            if (tankList.size() - i == 1) {
                p.rect(210, 140, 450, 80 + i * inc2);
            }
        }

        if (endScoreFrame % 30 == 0) {
            if (tanksDisplayed.size() < tankList.size()) {
                tanksDisplayed.add(tankList.get(endScore));
            }
            endScore++;
        }

        for (Tanks tanks : tanksDisplayed) {
            String tankName = tanks.getId();
            int score = tanks.getScore();
            p.fill(tanks.getColor()[0], tanks.getColor()[1], tanks.getColor()[2]);
            p.text("Player " + tankName, 310, yPosition);
            p.fill(255);
            p.text(score, 600, yPosition);
            yPosition += 35;
        }

        p.fill(tankList.get(0).getColor()[0], tankList.get(0).getColor()[1], tankList.get(0).getColor()[2]);
        p.text("Player " + tankList.get(0).getId() + " Wins!", 320, 100);
        p.strokeWeight(5);
        p.fill(255);
        p.text("Final Scores", 310, 150);
        p.stroke(255);
        p.line(210, 180, 660, 180);

        return endScore;
    }

    /**
     * Retrieves the currently active tank.
     */
    public Tanks getCurrentTank() {
        return tanks;
    }

    /**
     * Sets all tanks' scores to a high value and the wind to 0 for testing
     * purposes.
     */
    public void adminTest() {
        // Set all tanks' scores to 2000
        for (Tanks tank : allTanks.values()) {
            tank.setScore(2000);
        }

        // Set wind to 0
        wind = 0;
        allowWindUpdate = false;
        for (Projectiles projectile : projectiles) {
            projectile.setWind(0); // Set wind effect to 0 for each projectile
        }
    }

    /**
     * Sets all tanks' health and fuel to specific values for testing purposes.
     */
    public void adminTest2() {
        // Set all tanks' Health to 50
        // Set all tanks' Fuel to 40
        for (Tanks tank : allTanks.values()) {
            tank.setHealth(50);
            tank.setFuel(40);
        }
    }

    /**
     * Main method to start the Processing sketch.
     */
    public static void main(String[] args) {
        PApplet.main("Tanks.App");
    }

}