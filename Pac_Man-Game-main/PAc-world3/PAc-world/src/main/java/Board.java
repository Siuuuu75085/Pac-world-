import entities.*;
import utils.ImageManager;
import java.util.HashSet;
import java.awt.Image;

public class Board {
    private final int rowCount = 21;
    private final int columnCount = 19;
    private final int tileSize = 32;
    private final String[] tileMap;
    private final HashSet<Wall> walls;
    private final HashSet<Food> foods;
    private final HashSet<Ghost> ghosts;
    private int playerStartX, playerStartY;
    
    public Board(String[] tileMap) {
        this.tileMap = tileMap;
        this.walls = new HashSet<>();
        this.foods = new HashSet<>();
        this.ghosts = new HashSet<>();
    }
    
    public void loadMap(ImageManager imageManager) {
        walls.clear();
        foods.clear();
        ghosts.clear();
        
        
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                char tileChar = tileMap[r].charAt(c);
                int x = c * tileSize;
                int y = r * tileSize;
                
                switch (tileChar) {
                    case 'X':
                        walls.add(new Wall(imageManager.loadImage("/images/wall.png"), x, y, tileSize, tileSize));
                        break;
                    case ' ':
                        foods.add(new Food(null, x + 14, y + 14, 4, 4, 10, false));
                        break;
                    case 'O':
                        foods.add(new Food(
                            imageManager.loadImage("/images/powerPellet.png"), 
                            x + 8, y + 8, 16, 16, 50, true));
                        break;
                    case 'C':
                        foods.add(new Food(
                            imageManager.loadImage("/images/cherry.png"), 
                            x + 4, y + 4, 24, 24, 100, false));
                        break;
                    case 'P':
                        playerStartX = x;
                        playerStartY = y;
                        break;
                    case 'b': case 'o': case 'p': case 'r':
                        Image ghostNormal = null;
                        Image ghostScared = null;
                        switch (tileChar) {
                            case 'b':
                                ghostNormal = imageManager.loadImage("/images/blueGhost.png");
                                break;
                            case 'o':
                                ghostNormal = imageManager.loadImage("/images/orangeGhost.png");
                                break;
                            case 'p':
                                ghostNormal = imageManager.loadImage("/images/pinkGhost.png");
                                break;
                            case 'r':
                                ghostNormal = imageManager.loadImage("/images/redGhost.png");
                                break;
                            default:
                                ghostNormal = null;
                                break;
                        }
                        ghostScared = imageManager.loadImage("/images/scaredGhost.png");
                        ghosts.add(new Ghost(ghostNormal, ghostScared, x, y, tileSize, tileSize));
                        break;
                }
            }
        }
    }
    
    // Getters
    public HashSet<Wall> getWalls() { return walls; }
    public HashSet<Food> getFoods() { return foods; }
    public HashSet<Ghost> getGhosts() { return ghosts; }
    public int getTileSize() { return tileSize; }
    public int getBoardWidth() { return columnCount * tileSize; }
    public int getBoardHeight() { return rowCount * tileSize; }
    public int getPlayerStartX() { return playerStartX; }
    public int getPlayerStartY() { return playerStartY; }
}