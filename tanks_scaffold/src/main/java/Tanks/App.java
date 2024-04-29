package Tanks;

// import org.checkerframework.checker.units.qual.A;
import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

// import java.awt.*;
// import java.awt.geom.AffineTransform;
// import java.awt.image.BufferedImage;

import java.io.*;
import java.util.*;

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
    private long arrowStartTime;
    private boolean showArrow = false;
    private final int ARROW_DISPLAY_TIME = 2000;
    public PImage arrowImage;

    // Level
    private int numberOfLevel;
    private int[][] playMap;
    private PImage currentBackground;

    // Background
    public PImage basic;
    public PImage desert;
    public PImage forest;

    // Other Images
    public PImage fuel;
    public PImage hills;
    public PImage parachute;
    public PImage snow;
    public PImage tree1;
    public PImage tree2;
    public PImage wind1;
    public PImage wind2;

    public JSONArray bg_img;
    public int current_level = 1;
    public int empty_i = 0;

    public Blocks[][] block;
    public Tree[][] tree;
    public EmptyTile[][] empty;

    public int[][] terrainHeights;

    public JSONArray layout;

    // Tank
    public JSONObject playerColors;
    public ArrayList<Tanks> tanks;
    private int currentTankIndex = 0;
    private boolean moveLeft = false;
    private boolean moveRight = false;
    private Set<Character> initializedTanks;

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
        this.hills = loadImage(
                this.getClass().getResource("hills.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        this.parachute = loadImage(
                this.getClass().getResource("parachute.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        this.tree1 = loadImage(
                this.getClass().getResource("tree1.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        this.tree2 = loadImage(
                this.getClass().getResource("tree2.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        this.wind1 = loadImage(
                this.getClass().getResource("wind1.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        this.wind2 = loadImage(
                this.getClass().getResource("wind2.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        this.arrow = loadImage(
                this.getClass().getResource("arrow.png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
        this.tree1.resize(CELLSIZE, CELLSIZE);
        this.tree2.resize(CELLSIZE, CELLSIZE);

        JSONObject conf = loadJSONObject(new File(this.configPath));
        bg_img = conf.getJSONArray("levels");
        numberOfLevel = bg_img.size();
        playMap = new int[numberOfLevel][3];

        currentBackground = snow;
        for (int i = 0; i < numberOfLevel; i++) {
            JSONObject level = bg_img.getJSONObject(i);
            String[] colorStr = level.getString("foreground-colour").split(",");
            for (int j = 0; j < 3; j++) {
                playMap[i][j] = Integer.parseInt(colorStr[j].trim());
            }
        }

        // tanks
        tanks = new ArrayList<Tanks>();
        playerColors = conf.getJSONObject("player_colours");
        initializedTanks = new HashSet<>();

        int tree_index = 0;
        int empty_index = 0;

        block = new Blocks[bg_img.size()][28 * 20];
        tree = new Tree[bg_img.size()][28 * 20];
        empty = new EmptyTile[bg_img.size()][28 * 20];
        terrainHeights = new int[bg_img.size()][897];
        // terrainPixels = new int[bg_img.size()][WIDTH + 1];

        for (int i = 0; i < bg_img.size(); i++) {
            int tree_i = 0; // Reset tree index for each level
            JSONObject level = bg_img.getJSONObject(i);
            File file = new File(level.getString("layout"));
            try {
                Scanner sc = new Scanner(file);
                int block_i = 0;
                int pos_x = 0;
                int pos_y = 0;

                while (sc.hasNextLine()) {
                    String[] line = sc.nextLine().split("");
                    pos_x = 0;

                    for (int j = 0; j < line.length && j < BOARD_WIDTH; j++) {
                        switch (line[j]) {
                            case "X":
                                terrainHeights[i][pos_x] = 640 - pos_y;
                                // terrainPixels[i][pos_x] = 640 - pos_y;
                                break;
                            case "T":
                                tree[i][tree_i++] = new Tree(pos_x, pos_y);
                                break;
                            default:
                                empty[i][empty_i++] = new EmptyTile(pos_x, pos_y);
                                break;
                        }
                        pos_x += CELLSIZE;
                    }
                    pos_y += CELLSIZE;
                }

                sc.close();

                int p = terrainHeights[i][0];
                // System.out.println(terrainPixels[i][0]);
                for (int size = 0; size <= 896; size++) {
                    if (terrainHeights[i][size] == 0) {
                        terrainHeights[i][size] = p;
                    } else {
                        p = terrainHeights[i][size];
                    }
                }

                // For blocks
                for (int col = 0; col < BOARD_WIDTH; col++) {
                    for (int row = terrainHeights[i][col]; row < BOARD_HEIGHT; row++) {
                        block[i][block_i++] = new Blocks(col * CELLSIZE, row * CELLSIZE);
                    }
                }

                sc.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        applyMovingAverage(terrainHeights, 32);
        applyMovingAverage(terrainHeights, 32);

        // for tree (smoothed)
        for (Tree t : tree[current_level - 1]) {
            if (t != null && !t.hit) {
                int treeX = t.getX() + 16;
                if (treeX < WIDTH) {
                    int newY = HEIGHT - terrainHeights[current_level - 1][treeX] - tree2.height;
                    t.setY(newY);
                }
            }
        }
    }

    private void loadData(int levelIndex) {
        int tree_index = 0;
        int empty_index = 0;
        for (int i = 0; i < bg_img.size(); i++) {
            JSONObject level = bg_img.getJSONObject(i);
            // Arrays.fill(terrainHeights, BOARD_HEIGHT);

            File file = new File(level.getString("layout"));
            try {
                Scanner scan = new Scanner(file);
                // int block_index = 0;
                int position_x = 0;
                int position_y = 0;

                while (scan.hasNextLine()) {

                    String[] line = scan.nextLine().split("");
                    position_x = 0;
                    // HashMap<Character, Tanks> tanks = new HashMap<>(); //for tanks

                    for (int j = 0; j < line.length && j <= BOARD_WIDTH; j++) {
                        if (line[j].equals("X")) {
                            terrainHeights[i][position_x] = 640 - position_y;
                        }

                        else if (line[j].equals("T")) {
                            Tree t = new Tree(position_x, position_y);
                            tree[i][tree_index] = t;
                            tree_index++;
                        }

                        else {
                            EmptyTile emp = new EmptyTile(position_x, position_y);
                            empty[i][empty_index] = emp;
                            empty_index++;
                        }

                        position_x += CELLSIZE;

                    }
                    position_y += CELLSIZE;

                }

                scan.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        // for tree (smoothed)
        // for (Tree t : tree(current_level - 1)) {
        // if (t != null && !t.hit) {
        // int treeX = t.getX() + 16;
        // if (treeX < WIDTH) {
        // int newY = HEIGHT - terrainHeights[current_level - 1][treeX] - tree2.height;
        // t.setY(newY);
        // }
        // }
        // }
    }

    private void initializeLevel(int level) {
        changeMap(level);
        // tanks.clear();
        // initializedTanks.clear();
        loadData(level);
    }

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
                currentBackground = snow; // Default or loop back to first level
                break;
        }

        current_level = level;
    }

    // Smoothing the Terrain
    private void applyMovingAverage(int[][] heights, int range) {
        for (int i = 0; i < heights.length; i++) {
            int[] smoothed_h = new int[heights[i].length];
            int half_r = range / 2;

            for (int j = 0; j < heights[i].length; j++) {
                int sum = 0;
                int count = 0;

                for (int k = -half_r; k <= half_r; k++) {
                    if (j + k >= 0 && j + k < heights[i].length) {
                        sum += heights[i][j + k];
                        count++;
                    }
                }
                smoothed_h[j] = sum / count;
            }
            System.arraycopy(smoothed_h, 0, heights[i], 0, heights[i].length);
        }
    }

    /**
     * Receive key pressed signal from the keyboard.
     */
    @Override
    public void keyPressed(KeyEvent event) {

    }

    /**
     * Receive key released signal from the keyboard.
     */
    @Override
    public void keyReleased() {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO - powerups, like repair and extra fuel and teleport

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    /**
     * Draw all elements in the game by current frame.
     */
    @Override
    public void draw() {
        background(currentBackground);
        image(this.snow, 0, 0, width, height);

        if (current_level <= bg_img.size()) {

        }

        // Draw smoothed terrain
        noStroke();
        fill(255);
        beginShape();
        vertex(0, HEIGHT);
        for (int i = 0; i <= WIDTH; i++) {
            vertex(i, HEIGHT - terrainHeights[current_level - 1][i]);
        }
        vertex(WIDTH, HEIGHT);
        vertex(0, HEIGHT);
        endShape(CLOSE);

        // Draw trees
        for (Tree t : tree[current_level - 1]) {
            if (t != null && !t.hit) {
                PImage treeImg = (current_level == 3) ? tree1 : tree2;
                image(treeImg, t.getX(), t.getY());
            }
        }

        /*
         * // block
         * for (Blocks j : block[current_level - 1]) {
         * if (j != null) {
         * int x = j.getX();
         * int y = j.getY();
         * 
         * fill(255);
         * rect(x, y, CELLSIZE, CELLSIZE);
         * }
         * }
         * 
         * // Tree
         * for (Tree j : tree[current_level - 1]) {
         * if (j != null) {
         * if (j.hit) {
         * empty[current_level - 1][empty_index] = new EmptyTile(j.getX(), j.getY());
         * empty_index++;
         * } else if (!j.hit) {
         * int x = j.getX();
         * int y = j.getY();
         * fill(150);
         * rect(x, y, CELLSIZE, CELLSIZE);
         * }
         * }
         * }
         */

        // ----------------------------------
        // display HUD:
        // ----------------------------------
        // TODO

        // ----------------------------------
        // display scoreboard:
        // ----------------------------------
        // TODO

        // ----------------------------------
        // ----------------------------------

        // TODO: Check user action
    }

    public static void main(String[] args) {
        PApplet.main("Tanks.App");
    }

}