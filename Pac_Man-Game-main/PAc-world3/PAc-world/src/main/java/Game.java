import entities.*;
import utils.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Game implements ActionListener {
    private Board board;
    private Player player;
    private Timer gameLoop;
    private int score = 0;
    private int lives = 3;
    private boolean gameOver = false;
    private boolean paused = false;
    private boolean gameWon = false;
    private SoundManager soundManager;
    private boolean powerPelletActive = false;
    private int powerPelletTimer = 0;
    private static final int POWER_PELLET_DURATION = 625;
    private int totalFoodCount = 0;
    private int ghostMoveCounter = 0;

    public Game() {
        soundManager = new SoundManager();
        soundManager.setSoundEnabled(GameSettings.isSoundEnabled());

        if (GameSettings.isSoundEnabled()) {
            soundManager.playSound("doraemon.wav", true);
        }

        ImageManager imageManager = new ImageManager();

        // Load tilemap based on difficulty
        String[] tileMap = getTileMapForDifficulty();

        board = new Board(tileMap);
        board.loadMap(imageManager);

        // Count total food
        countTotalFood();

        // Load player animations
        Image[] rightFrames = {
                imageManager.loadImage("/images/pacmanRight.png"),
        };
        Image[] upFrames = {
                imageManager.loadImage("/images/pacmanUp.png"),
        };
        Image[] downFrames = {
                imageManager.loadImage("/images/pacmanDown.png"),
        };
        Image[] leftFrames = {
                imageManager.loadImage("/images/pacmanLeft.png"),
        };

        player = new Player(
                rightFrames, upFrames, downFrames, leftFrames,
                board.getPlayerStartX(),
                board.getPlayerStartY(),
                board.getTileSize(),
                board.getTileSize());

        // Initialize ghosts with enhanced AI
        for (Ghost ghost : board.getGhosts()) {
            ghost.reset();
        }

        gameLoop = new Timer(16, this);
    }

    // Sửa các maps cho 3 difficulty levels
    private String[] getTileMapForDifficulty() {
        switch (GameSettings.getDifficulty()) {
            case EASY:
                return new String[] {
                        "XXXXXXXXXXXXXXXXXXX",
                        "X        X        X",
                        "X XX XXX X XXX XX X",
                        "X                 X",
                        "X XX X XXXXX X XX X",
                        "X    X       X    X",
                        "XXXX XXXX XXXX XXXX",
                        "OOO  X       X  OOO",
                        "XXXX X XXrXX X XXXX",
                        "O       bpo       O",
                        "XXXX X XXXXX X XXXX",
                        "OOOX X       X XOOO",
                        "XXXX X XXXXX X XXXX",
                        "X        X        X",
                        "X XX XXX X XXX XX X",
                        "X  X     P     X  X",
                        "XX X X XXXXX X X XX",
                        "X    X   X   X    X",
                        "X XXXXXX X XXXXXX X",
                        "X                 X",
                        "X XXXXXX X XXXXXX X",
                        "X        X        X",
                        "XXXXXXXXXXXXXXXXXXX"
                };

            case NORMAL:
    return new String[] {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X   r   X    X",
        "XXXX XXXX XXXX XXXX",
        "OOO  X       X  OOO",
        "XXXX X XXbXX X XXXX",
        "O         p     o O",
        "XXXX X XXXXX X XXXX",
        "OOO  X       X  OOO",
        "XXXX X XXXXX X XXXX",
        "X                 X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X        X        X",
        "XXXXXXXXXXXXXXXXXXX"
    };

case HARD:
    return new String[] {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     o     X  X",
        "X XX X XXXXX X XX X",
        "X  r X       X b  X",
        "XXXX XXXX XXXX XXXX",
        "OOO  X       X  OOO",
        "XXXX X XX XX X XXXX",
        "O         P       O",
        "XXXX X XXXXX X XXXX",
        "OOO  X       X  OOO",
        "XXXX X XXXXX X XXXX",
        "X                 X",
        "X XX XXX X XXX XX X",
        "X  X           X  X",
        "XX X X XXXXX X X XX",
        "X p  X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX"
    };

            default:
                return getTileMapForDifficulty();
        }
    }

    private void countTotalFood() {
        totalFoodCount = 0;
        for (Entity food : board.getFoods()) {
            if (food instanceof Food) {
                Food f = (Food) food;
                if (f.getImage() == null || f.isPowerPellet()) {
                    totalFoodCount++;
                }
            }
        }
    }

    public void start() {
        gameLoop.start();
    }

    public void render(Graphics g) {
        // Enhanced rendering with better visuals
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw player with glow effect
        player.updateAnimation();

        // Player glow effect
        if (!gameOver) {
            g2d.setColor(new Color(255, 255, 0, 30));
            g2d.fillOval(player.getX() - 3, player.getY() - 3,
                    player.getWidth() + 6, player.getHeight() + 6);
        }

        g2d.drawImage(player.getImage(), player.getX(), player.getY(),
                player.getWidth(), player.getHeight(), null);

        // Draw ghosts with mode indicators
        for (Ghost ghost : board.getGhosts()) {
            // Ghost glow based on mode
            if (!gameOver) {
                Color glowColor = getGhostGlowColor(ghost);
                if (glowColor != null) {
                    g2d.setColor(glowColor);
                    g2d.fillOval(ghost.getX() - 2, ghost.getY() - 2,
                            ghost.getWidth() + 4, ghost.getHeight() + 4);
                }
            }

            g2d.drawImage(ghost.getImage(), ghost.getX(), ghost.getY(),
                    ghost.getWidth(), ghost.getHeight(), null);
        }

        // Draw walls with enhanced visuals
        for (Wall wall : board.getWalls()) {
            g2d.drawImage(wall.getImage(), wall.getX(), wall.getY(),
                    wall.getWidth(), wall.getHeight(), null);
        }

        // Draw food with enhanced effects
        for (Entity food : board.getFoods()) {
            if (food.getImage() == null) { // Regular food pellet
                g2d.setColor(Color.WHITE);
                g2d.fillOval(food.getX(), food.getY(), food.getWidth(), food.getHeight());

                // Small glow for pellets
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.fillOval(food.getX() - 1, food.getY() - 1,
                        food.getWidth() + 2, food.getHeight() + 2);
            } else { // Power pellet or cherry
                // Power pellet glow effect
                if (food instanceof Food && ((Food) food).isPowerPellet()) {
                    g2d.setColor(new Color(255, 255, 0, 60));
                    g2d.fillOval(food.getX() - 4, food.getY() - 4,
                            food.getWidth() + 8, food.getHeight() + 8);
                }

                g2d.drawImage(food.getImage(), food.getX(), food.getY(),
                        food.getWidth(), food.getHeight(), null);
            }
        }

        // Enhanced UI
        drawEnhancedUI(g2d);
    }

    private Color getGhostGlowColor(Ghost ghost) {
        if (ghost.isScared()) {
            return new Color(0, 0, 255, 40); // Blue glow when scared
        }

        switch (ghost.getCurrentMode()) {
            case CHASE:
                return new Color(255, 0, 0, 30); // Red glow when chasing
            case SCATTER:
                return new Color(0, 255, 0, 20); // Green glow when scattering
            case PATROL:
                return new Color(255, 255, 0, 20); // Yellow glow when patrolling
            default:
                return null;
        }
    }

    private void drawEnhancedUI(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (gameOver) {
            // Game over screen with enhanced visuals
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, board.getBoardWidth(), board.getBoardHeight());

            if (gameWon) {
                g2d.setColor(Color.GREEN);
                g2d.setFont(new Font("Arial", Font.BOLD, 36));
                String winText = "VICTORY!";
                FontMetrics fm = g2d.getFontMetrics();
                int x = (board.getBoardWidth() - fm.stringWidth(winText)) / 2;
                g2d.drawString(winText, x, board.getBoardHeight() / 2 - 50);

                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                String scoreText = "Final Score: " + score;
                fm = g2d.getFontMetrics();
                x = (board.getBoardWidth() - fm.stringWidth(scoreText)) / 2;
                g2d.drawString(scoreText, x, board.getBoardHeight() / 2);

                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("Arial", Font.PLAIN, 16));
                String restartText = "Press any key to restart";
                fm = g2d.getFontMetrics();
                x = (board.getBoardWidth() - fm.stringWidth(restartText)) / 2;
                g2d.drawString(restartText, x, board.getBoardHeight() / 2 + 40);
            } else {
                g2d.setColor(Color.RED);
                g2d.setFont(new Font("Arial", Font.BOLD, 36));
                String gameOverText = "GAME OVER";
                FontMetrics fm = g2d.getFontMetrics();
                int x = (board.getBoardWidth() - fm.stringWidth(gameOverText)) / 2;
                g2d.drawString(gameOverText, x, board.getBoardHeight() / 2 - 50);

                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                String scoreText = "Score: " + score;
                fm = g2d.getFontMetrics();
                x = (board.getBoardWidth() - fm.stringWidth(scoreText)) / 2;
                g2d.drawString(scoreText, x, board.getBoardHeight() / 2);

                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("Arial", Font.PLAIN, 16));
                String restartText = "Press any key to restart";
                fm = g2d.getFontMetrics();
                x = (board.getBoardWidth() - fm.stringWidth(restartText)) / 2;
                g2d.drawString(restartText, x, board.getBoardHeight() / 2 + 40);
            }
        } else if (paused) {
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillRect(0, 0, board.getBoardWidth(), board.getBoardHeight());

            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            String pauseText = "PAUSED";
            FontMetrics fm = g2d.getFontMetrics();
            int x = (board.getBoardWidth() - fm.stringWidth(pauseText)) / 2;
            g2d.drawString(pauseText, x, board.getBoardHeight() / 2);
        } else {
            // In-game UI
            g2d.setFont(new Font("Arial", Font.BOLD, 18));

            // Score with shadow effect
            g2d.setColor(Color.BLACK);
            g2d.drawString("Score: " + score, 21, 21);
            g2d.setColor(Color.YELLOW);
            g2d.drawString("Score: " + score, 20, 20);

            // Lives with shadow effect
            String livesText = "Lives: " + lives;
            FontMetrics fm = g2d.getFontMetrics();
            int livesX = board.getBoardWidth() - fm.stringWidth(livesText) - 20;
            g2d.setColor(Color.BLACK);
            g2d.drawString(livesText, livesX + 1, 21);
            g2d.setColor(Color.YELLOW);
            g2d.drawString(livesText, livesX, 20);

            // Difficulty indicator
            g2d.setColor(Color.CYAN);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            String diffText = GameSettings.getDifficulty().toString();
            fm = g2d.getFontMetrics();
            int diffX = (board.getBoardWidth() - fm.stringWidth(diffText)) / 2;
            g2d.drawString(diffText, diffX, 20);

            // Power pellet timer with enhanced visuals
            if (powerPelletActive) {
                int remainingSeconds = (powerPelletTimer * 16) / 1000;
                g2d.setColor(Color.MAGENTA);
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                String powerText = "POWER: " + remainingSeconds + "s";
                fm = g2d.getFontMetrics();
                int powerX = (board.getBoardWidth() - fm.stringWidth(powerText)) / 2;

                // Flashing effect when time is low
                if (remainingSeconds <= 2) {
                    if ((System.currentTimeMillis() / 200) % 2 == 0) {
                        g2d.setColor(Color.RED);
                    }
                }

                g2d.drawString(powerText, powerX, 40);
            }

            // Level complete message
            int currentFoodCount = getCurrentFoodCount();
            if (currentFoodCount <= 5 && currentFoodCount > 0) {
                g2d.setColor(Color.GREEN);
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                String almostText = "Almost there! " + currentFoodCount + " pellets left!";
                FontMetrics almostFm = g2d.getFontMetrics();
                int almostX = (board.getBoardWidth() - almostFm.stringWidth(almostText)) / 2;
                g2d.drawString(almostText, almostX, board.getBoardHeight() - 30);
            }
        }
    }

    private int getCurrentFoodCount() {
        int count = 0;
        for (Entity food : board.getFoods()) {
            if (food instanceof Food) {
                Food f = (Food) food;
                if (f.getImage() == null || f.isPowerPellet()) {
                    count++;
                }
            }
        }
        return count;
    }

    public void update() {
        if (gameOver || paused)
            return;

        // Update power pellet timer with enhanced effects
        if (powerPelletActive) {
            powerPelletTimer--;

            // Flashing effect when time is running out
            if (powerPelletTimer < 125) { // Last 2 seconds
                if (powerPelletTimer % 20 < 10) {
                    for (Ghost ghost : board.getGhosts()) {
                        ghost.setScared(false);
                    }
                } else {
                    for (Ghost ghost : board.getGhosts()) {
                        ghost.setScared(true);
                    }
                }
            }

            if (powerPelletTimer <= 0) {
                powerPelletActive = false;
                resetGhostImages();
            }
        }

        // Player movement
        player.updatePosition();

        // Wall collision for player
        for (Wall wall : board.getWalls()) {
            if (player.collidesWith(wall)) {
                player.setX(player.getX() - player.getVelocityX());
                player.setY(player.getY() - player.getVelocityY());
                break;
            }
        }

        // Screen wrapping (tunnels)
        if (player.getX() < -player.getWidth()) {
            player.setX(board.getBoardWidth());
        } else if (player.getX() > board.getBoardWidth()) {
            player.setX(-player.getWidth());
        }

        // Enhanced ghost movement
        ghostMoveCounter++;
        if (ghostMoveCounter >= 1) {
            ghostMoveCounter = 0;

            for (Ghost ghost : board.getGhosts()) {
                // Ghost collision with player
                if (ghost.collidesWith(player)) {
                    if (powerPelletActive && ghost.isScared()) {
                        if (GameSettings.isSoundEnabled()) {
                            soundManager.playSound("pacman_eatghost.wav", false);
                        }
                        ghost.reset();
                        score += 200;
                    } else if (!powerPelletActive || !ghost.isScared()) {
                        if (GameSettings.isSoundEnabled()) {
                            soundManager.playSound("pacman_death.wav", false);
                        }
                        lives--;
                        if (lives == 0) {
                            gameOver = true;
                            gameWon = false;
                            soundManager.stopBackgroundMusic();
                            return;
                        }
                        resetPositions();
                        return;
                    }
                }

                // Use enhanced AI
                moveGhostWithEnhancedAI(ghost);
            }
        }

        // Food collection with enhanced effects
        Entity foodEaten = null;
        for (Entity food : board.getFoods()) {
            if (player.collidesWith(food)) {
                foodEaten = food;

                if (food instanceof Food) {
                    Food f = (Food) food;
                    if (f.isPowerPellet()) {
                        activatePowerPellet();
                        score += 50;
                        if (GameSettings.isSoundEnabled()) {
                            soundManager.playSound("power_pellet.wav", false);
                        }
                    } else if (f.getImage() != null && f.getImage().toString().contains("cherry")) {
                        score += 100;
                        if (GameSettings.isSoundEnabled()) {
                            soundManager.playSound("fruit.wav", false);
                        }
                    } else {
                        score += 10;
                        // Uncomment for pellet sound: soundManager.playSound("chomp.wav", false);
                    }
                }
                break;
            }
        }
        if (foodEaten != null) {
            board.getFoods().remove(foodEaten);
        }

        // Level completion check
        if (getCurrentFoodCount() == 0 && !gameWon) {
            gameWon = true;
            soundManager.stopBackgroundMusic();
            if (GameSettings.isSoundEnabled()) {
                soundManager.playSound("win.wav", false);
            }

            Timer completeTimer = new Timer(3000, e -> {
                gameOver = true;
                ((Timer) e.getSource()).stop();
            });
            completeTimer.setRepeats(false);
            completeTimer.start();
        }
    }

    // Sửa lỗi trong moveGhostWithEnhancedAI - cải thiện movement
    private void moveGhostWithEnhancedAI(Ghost ghost) {
        int oldX = ghost.getX();
        int oldY = ghost.getY();

        // Update ghost AI
        ghost.updateAI(player, board.getWalls(), board.getTileSize());

        // Đảm bảo ghost có velocity
        ghost.ensureMovement();

        // Move ghost
        ghost.updatePosition();

        // Check collision
        boolean collision = false;
        for (Wall wall : board.getWalls()) {
            if (ghost.collidesWith(wall)) {
                collision = true;
                break;
            }
        }

        // Check boundaries - cho phép đi qua tunnel
        boolean isTunnel = ghost.getY() > 8 * board.getTileSize() &&
                ghost.getY() < 12 * board.getTileSize() &&
                (ghost.getX() < 0 || ghost.getX() > board.getBoardWidth());

        if (!isTunnel && (ghost.getY() <= 0 || ghost.getY() + ghost.getHeight() >= board.getBoardHeight())) {
            collision = true;
        }

        // Handle collision
        if (collision) {
            ghost.setX(oldX);
            ghost.setY(oldY);

            // Force emergency direction
            Direction[] directions = { Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT };
            for (Direction dir : directions) {
                if (isGhostDirectionValid(ghost, dir)) {
                    ghost.setDirection(dir);
                    break;
                }
            }
        }

        // Handle screen wrapping với logic chính xác hơn
        if (ghost.getX() < -ghost.getWidth()) {
            ghost.setX(board.getBoardWidth());
        } else if (ghost.getX() > board.getBoardWidth()) {
            ghost.setX(-ghost.getWidth());
        }
    }

    private void resetGhostImages() {
        for (Ghost ghost : board.getGhosts()) {
            ghost.setScared(false);
        }
    }

    private void resetPositions() {
        // Reset player position and direction
        player.reset();
        player.setVelocityX(0);
        player.setVelocityY(0);

        // Reset all ghosts
        for (Ghost ghost : board.getGhosts()) {
            ghost.reset();
        }

        // Reset power pellet state
        powerPelletActive = false;
        powerPelletTimer = 0;
        resetGhostImages();
    }

    // Sửa lỗi trong isGhostDirectionValid - cải thiện validation
    private boolean isGhostDirectionValid(Ghost ghost, Direction dir) {
        if (dir == null)
            return false;

        int newX = ghost.getX();
        int newY = ghost.getY();
        int speed = ghost.isScared() ? 2 : 3;

        switch (dir) {
            case UP:
                newY -= speed;
                break;
            case DOWN:
                newY += speed;
                break;
            case LEFT:
                newX -= speed;
                break;
            case RIGHT:
                newX += speed;
                break;
        }

        // Check boundaries with improved tunnel logic
        boolean isTunnel = newY > 8 * board.getTileSize() &&
                newY < 12 * board.getTileSize() &&
                (newX < 0 || newX > board.getBoardWidth());

        if (!isTunnel && (newY < 0 || newY + ghost.getHeight() >= board.getBoardHeight())) {
            return false;
        }

        // Check wall collision
        java.awt.Rectangle ghostBounds = new java.awt.Rectangle(newX, newY, ghost.getWidth(), ghost.getHeight());
        for (Wall wall : board.getWalls()) {
            if (ghostBounds.intersects(wall.getBounds())) {
                return false;
            }
        }

        return true;
    }

    // Sửa lỗi trong activatePowerPellet - fix ghost reset
    private void activatePowerPellet() {
        powerPelletActive = true;
        powerPelletTimer = POWER_PELLET_DURATION;

        ImageManager imgManager = new ImageManager();
        Image scaredGhostImage = imgManager.loadImage("/images/scaredGhost.png");

        for (Ghost ghost : board.getGhosts()) {
            if (scaredGhostImage != null) {
                ghost.setScaredImage(scaredGhostImage);
            }

            // Set scared mode
            ghost.setScared(true);

            // Thêm code để tránh ghost bị stuck khi bật power pellet
            Direction[] directions = { Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT };
            Direction currentDir = ghost.getDirection();
            Direction opposite = getOppositeDirection(currentDir);

            // Cố gắng thay đổi hướng khi bật power pellet (tránh đi tiếp vào player)
            for (Direction dir : directions) {
                if (dir != currentDir && dir != opposite && isGhostDirectionValid(ghost, dir)) {
                    ghost.setDirection(dir);
                    break;
                }
            }
        }
    }

    // Helper method - thêm phương thức getOppositeDirection
    private Direction getOppositeDirection(Direction dir) {
        switch (dir) {
            case UP:
                return Direction.DOWN;
            case DOWN:
                return Direction.UP;
            case LEFT:
                return Direction.RIGHT;
            case RIGHT:
                return Direction.LEFT;
            default:
                return Direction.UP;
        }
    }

    public void handleKeyPress(int keyCode) {
        if (gameOver) {
            // Restart game
            score = 0;
            lives = 3;
            gameOver = false;
            gameWon = false;

            String[] tileMap = getTileMapForDifficulty();
            board = new Board(tileMap);
            board.loadMap(new ImageManager());
            countTotalFood();
            resetPositions();

            if (GameSettings.isSoundEnabled()) {
                soundManager.playSound("doraemon.wav", true);
            }

            return;
        }

        if (keyCode == KeyEvent.VK_P) {
            paused = !paused;
            return;
        }

        Direction requestedDirection = null;

        if (keyCode == KeyEvent.VK_UP) {
            requestedDirection = Direction.UP;
        } else if (keyCode == KeyEvent.VK_DOWN) {
            requestedDirection = Direction.DOWN;
        } else if (keyCode == KeyEvent.VK_LEFT) {
            requestedDirection = Direction.LEFT;
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            requestedDirection = Direction.RIGHT;
        }

        if (requestedDirection != null) {
            tryTurn(requestedDirection);
        }
    }

    private void tryTurn(Direction newDirection) {
        int currentX = player.getX();
        int currentY = player.getY();
        int tileSize = board.getTileSize();

        int alignedX = Math.round((float) currentX / tileSize) * tileSize;
        int alignedY = Math.round((float) currentY / tileSize) * tileSize;

        // Tăng tolerance để dễ chuyển hướng hơn
        int tolerance = tileSize / 2;

        boolean closeToAlignedPosition = Math.abs(currentX - alignedX) <= tolerance &&
                Math.abs(currentY - alignedY) <= tolerance;

        if (closeToAlignedPosition) {
            if ((newDirection == Direction.UP || newDirection == Direction.DOWN) &&
                    Math.abs(currentX - alignedX) <= tolerance) {
                player.setX(alignedX);
            }

            if ((newDirection == Direction.LEFT || newDirection == Direction.RIGHT) &&
                    Math.abs(currentY - alignedY) <= tolerance) {
                player.setY(alignedY);
            }

            // Lưu hướng cũ để phục hồi nếu cần
            Direction oldDirection = player.getDirection();

            player.updateDirection(newDirection, tileSize);
            player.updatePosition();

            boolean collision = false;
            for (Wall wall : board.getWalls()) {
                if (player.collidesWith(wall)) {
                    collision = true;
                    break;
                }
            }

            if (collision) {
                player.setX(currentX);
                player.setY(currentY);
                player.updateDirection(oldDirection, tileSize);
            }
        } else {
            // Queued turning - lưu lại input để thực hiện khi có thể
            Direction currentDirection = player.getDirection();

            // Always allow reversing direction immediately
            if (isOppositeDirection(currentDirection, newDirection)) {
                player.updateDirection(newDirection, tileSize);
            }
        }
    }

    // Helper method - thêm phương thức để kiểm tra hướng ngược
    private boolean isOppositeDirection(Direction dir1, Direction dir2) {
        return (dir1 == Direction.UP && dir2 == Direction.DOWN) ||
                (dir1 == Direction.DOWN && dir2 == Direction.UP) ||
                (dir1 == Direction.LEFT && dir2 == Direction.RIGHT) ||
                (dir1 == Direction.RIGHT && dir2 == Direction.LEFT);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
    }

    // Getters
    public Board getBoard() {
        return board;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isPaused() {
        return paused;
    }
}