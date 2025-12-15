package entities;
import java.awt.Image;
import java.util.Random;
//import java.util.ArrayList;
//import java.util.List;
import utils.Direction;

public class Ghost extends Entity {
    private static final Random random = new Random();
    private Image normalImage;
    private Image scaredImage;
    private boolean isScared = false;
    
    // **NEW: Grid-based movement variables**
    private int tileSize = 32; // Standard tile size
    private boolean isMoving = false;
    private int targetX, targetY; // Target position for smooth movement
    private int moveProgress = 0; // Progress of current move (0-tileSize)
    private final int MOVE_SPEED = 3; // Pixels per frame (must divide tileSize evenly)
    
    // Ghost behavior modes
    public enum GhostMode {
        SCATTER,    // Di chuyển về góc map
        CHASE,      // Đuổi theo player
        FRIGHTENED, // Chạy trốn khi sợ hãi
        PATROL      // Tuần tra khu vực
    }
    
    private GhostMode currentMode = GhostMode.SCATTER;
    private int modeTimer = 0;
    private int directionTimer = 0;
    private int aiUpdateCounter = 0;
    
    // Scatter targets cho từng ghost (góc map khác nhau)
    private int scatterTargetX = 0;
    private int scatterTargetY = 0;
    private int patrolCenterX = 0;
    private int patrolCenterY = 0;
    
    // AI behavior variables
    private Direction lastPlayerDirection = Direction.RIGHT;
    private int chaseAggressiveness = 50; // 0-100, càng cao càng aggressive
    private int stuckCounter = 0;
    private Direction lastDirection = Direction.UP; // Thêm để tránh oscillation
    private Direction pendingDirection = null; // Direction to apply when current move finishes
    
    public Ghost(Image normalImage, Image scaredImage, int x, int y, int width, int height) {
        super(normalImage, x, y, width, height);
        this.normalImage = normalImage;
        this.scaredImage = scaredImage;
        this.direction = Direction.UP;
        this.lastDirection = Direction.UP;
        
        // **NEW: Snap to grid on spawn**
        snapToGrid();
        
        // Set unique targets cho từng ghost dựa trên vị trí spawn
        setUniqueTargets(x, y);
        
        // Initialize target position
        this.targetX = this.x;
        this.targetY = this.y;
    }
    
    /**
     * **NEW: Snap ghost position to nearest grid position**
     */
    private void snapToGrid() {
        this.x = (this.x / tileSize) * tileSize;
        this.y = (this.y / tileSize) * tileSize;
    }
    
    /**
     * **FIX: Smoother move method to prevent flickering**
     */
    public void move() {
        if (isMoving) {
            // Continue current movement
            moveProgress += MOVE_SPEED;
            
            // **FIX: Clamp moveProgress to prevent overshooting**
            if (moveProgress > tileSize) {
                moveProgress = tileSize;
            }
            
            // Calculate interpolated position with smoother math
            float progress = (float) moveProgress / tileSize;
            
            // **FIX: Use starting position for smoother interpolation**
            int startX = x;
            int startY = y;
            
            switch (direction) {
                case UP:
                    startY = targetY + tileSize;
                    this.y = (int)(startY - (progress * tileSize));
                    break;
                case DOWN:
                    startY = targetY - tileSize;
                    this.y = (int)(startY + (progress * tileSize));
                    break;
                case LEFT:
                    startX = targetX + tileSize;
                    this.x = (int)(startX - (progress * tileSize));
                    break;
                case RIGHT:
                    startX = targetX - tileSize;
                    this.x = (int)(startX + (progress * tileSize));
                    break;
            }
            
            // Check if movement is complete
            if (moveProgress >= tileSize) {
                // Movement complete - snap to target
                this.x = targetX;
                this.y = targetY;
                isMoving = false;
                moveProgress = 0;
                
                // Apply pending direction if any
                if (pendingDirection != null) {
                    startMovement(pendingDirection);
                    pendingDirection = null;
                }
            }
        } else {
            // **FIX: Don't force movement immediately - let AI handle it**
            // Remove the automatic movement forcing to prevent flickering
        }
    }
    
    /**
     * **NEW: Start movement in a direction (grid-based)**
     */
    private boolean startMovement(Direction dir) {
        if (isMoving && dir == direction) {
            return true; // Already moving in this direction
        }
        
        if (dir == null) return false;
        
        // **FIX: Always complete current movement first**
        if (isMoving) {
            this.x = targetX;
            this.y = targetY;
            isMoving = false;
            moveProgress = 0;
            pendingDirection = null;
        }
        
        // Calculate next grid position
        int currentGridX = x / tileSize;
        int currentGridY = y / tileSize;
        int nextGridX = currentGridX;
        int nextGridY = currentGridY;
        
        switch (dir) {
            case UP: nextGridY--; break;
            case DOWN: nextGridY++; break;
            case LEFT: nextGridX--; break;
            case RIGHT: nextGridX++; break;
        }
        
        // Validate next position
        if (isValidGridPositionLenient(nextGridX, nextGridY)) {
            // Start new movement
            this.direction = dir;
            this.targetX = nextGridX * tileSize;
            this.targetY = nextGridY * tileSize;
            this.isMoving = true;
            this.moveProgress = 0;
            
            return true;
        }
        
        return false;
    }
    
    /**
     * **FIX: More lenient grid position validation**
     */
    private boolean isValidGridPositionLenient(int gridX, int gridY) {
        // Check bounds with tunnel support
        if (gridY < 0 || gridY >= rowCount) {
            return false;
        }
        
        // Handle horizontal tunnels - more permissive
        if (gridX < 0 || gridX >= columnCount) {
            // Allow tunnel in middle area
            return gridY >= 8 && gridY <= 12;
        }
        
        // For now, assume position is valid if within bounds
        // Wall collision will be checked in updateAI with actual walls
        return true;
    }
    
    /**
     * **UPDATED: AI update with grid-based movement**
     */
    public void updateAI(Player player, java.util.HashSet<Wall> walls, int tileSize) {
        this.tileSize = tileSize;
        
        modeTimer++;
        directionTimer++;
        aiUpdateCounter++;
        
        // **FIX: Less aggressive recovery system**
        if (aiUpdateCounter % 180 == 0) { // Every 3 seconds instead of 2
            if (!isMoving && moveProgress == 0) {
                snapToGrid();
                this.targetX = this.x;
                this.targetY = this.y;
                
                Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
                for (Direction dir : directions) {
                    if (isValidDirection(dir, walls, tileSize)) {
                        startMovement(dir);
                        break;
                    }
                }
            }
        }
        
        // Update ghost mode based on timer and game state
        updateGhostMode();
        
        // **FIX: Only change direction when not moving or at longer intervals**
        boolean canChangeDirection = (!isMoving && moveProgress == 0) || 
                                   directionTimer >= getCurrentModeUpdateFrequency() * 2; // Double the frequency
        
        if (canChangeDirection) {
            directionTimer = 0;
            Direction newDirection = calculateBestDirection(player, walls, tileSize);
            
            if (newDirection != null) {
                boolean success = startMovement(newDirection);
                if (success) {
                    lastDirection = newDirection;
                    stuckCounter = 0;
                } else {
                    stuckCounter++;
                    if (stuckCounter > 3) { // Increased threshold
                        Direction emergencyDir = getEmergencyDirection(walls, tileSize);
                        if (emergencyDir != null) {
                            startMovement(emergencyDir);
                            stuckCounter = 0;
                        } else {
                            // Reset to spawn only as last resort
                            this.x = this.startX;
                            this.y = this.startY;
                            snapToGrid();
                            this.targetX = this.x;
                            this.targetY = this.y;
                            this.isMoving = false;
                            this.moveProgress = 0;
                            this.pendingDirection = null;
                            stuckCounter = 0;
                        }
                    }
                }
            }
        }
        
        // Store player's direction for prediction
        if (player.getDirection() != null) {
            lastPlayerDirection = player.getDirection();
        }
        
        // Always call move to update position
        move();
        
        // **FIX: Less strict boundary checking**
        if (this.x < -tileSize * 2 || this.x > columnCount * tileSize + tileSize * 2 ||
            this.y < -tileSize * 2 || this.y > rowCount * tileSize + tileSize * 2) {
            this.x = this.startX;
            this.y = this.startY;
            snapToGrid();
            this.targetX = this.x;
            this.targetY = this.y;
            this.isMoving = false;
            this.moveProgress = 0;
            this.pendingDirection = null;
        }
    }
    
    private void setUniqueTargets(int spawnX, int spawnY) {
        // Set scatter target và patrol center khác nhau cho mỗi ghost
        int tileX = spawnX / 32;
        int tileY = spawnY / 32;
        
        if (tileX < 9) { // Left side ghosts
            if (tileY < 10) {
                scatterTargetX = 0; scatterTargetY = 0; // Top-left
                patrolCenterX = 4; patrolCenterY = 5;
                chaseAggressiveness = 70; // Red ghost - most aggressive
            } else {
                scatterTargetX = 0; scatterTargetY = 20; // Bottom-left
                patrolCenterX = 4; patrolCenterY = 15;
                chaseAggressiveness = 40; // Pink ghost - ambush style
            }
        } else { // Right side ghosts
            if (tileY < 10) {
                scatterTargetX = 18; scatterTargetY = 0; // Top-right
                patrolCenterX = 14; patrolCenterY = 5;
                chaseAggressiveness = 60; // Blue ghost - smart chaser
            } else {
                scatterTargetX = 18; scatterTargetY = 20; // Bottom-right
                patrolCenterX = 14; patrolCenterY = 15;
                chaseAggressiveness = 30; // Orange ghost - random/patrol
            }
        }
    }
    
    private void updateGhostMode() {
        if (isScared) {
            currentMode = GhostMode.FRIGHTENED;
            return;
        }
        
        int cycle = (modeTimer % 1800);
        
        if (cycle < 150) {
            currentMode = GhostMode.SCATTER;
        } else if (cycle < 900) {
            currentMode = GhostMode.CHASE;
        } else if (cycle < 1050) {
            currentMode = GhostMode.PATROL;
        } else {
            currentMode = GhostMode.CHASE;
        }
    }
    
    /**
     * **FIX: Slower update frequency to reduce flickering**
     */
    private int getCurrentModeUpdateFrequency() {
        switch (currentMode) {
            case CHASE: return 16; // Doubled from 8
            case FRIGHTENED: return 12; // Doubled from 6
            case SCATTER: return 24; // Doubled from 12
            case PATROL: return 20; // Doubled from 10
            default: return 20;
        }
    }
    
    private Direction calculateBestDirection(Player player, java.util.HashSet<Wall> walls, int tileSize) {
        switch (currentMode) {
            case CHASE:
                return getAdvancedChaseDirection(player, walls, tileSize);
            case SCATTER:
                return getScatterDirection(walls, tileSize);
            case FRIGHTENED:
                return getFrightenedDirection(player, walls, tileSize);
            case PATROL:
                return getPatrolDirection(walls, tileSize);
            default:
                return getScatterDirection(walls, tileSize);
        }
    }
    
    private Direction getAdvancedChaseDirection(Player player, java.util.HashSet<Wall> walls, int tileSize) {
        int currentTileX = x / tileSize;
        int currentTileY = y / tileSize;
        int playerTileX = player.getX() / tileSize;
        int playerTileY = player.getY() / tileSize;
        
        return getDirectionToTarget(playerTileX, playerTileY, walls, tileSize);
    }
    
    private Direction getScatterDirection(java.util.HashSet<Wall> walls, int tileSize) {
        return getDirectionToTarget(scatterTargetX, scatterTargetY, walls, tileSize);
    }
    
    private Direction getFrightenedDirection(Player player, java.util.HashSet<Wall> walls, int tileSize) {
        Direction bestEscape = getSmartEscapeDirection(player, walls, tileSize);
        
        if (random.nextInt(100) < 20) {
            return getRandomValidDirection(walls, tileSize);
        }
        
        return bestEscape != null ? bestEscape : getRandomValidDirection(walls, tileSize);
    }
    
    private Direction getPatrolDirection(java.util.HashSet<Wall> walls, int tileSize) {
        int currentTileX = x / tileSize;
        int currentTileY = y / tileSize;
        
        int distanceFromCenter = Math.abs(currentTileX - patrolCenterX) + Math.abs(currentTileY - patrolCenterY);
        
        if (distanceFromCenter > 3) {
            return getDirectionToTarget(patrolCenterX, patrolCenterY, walls, tileSize);
        } else {
            return getPatrolPattern(walls, tileSize);
        }
    }
    
    private Direction getDirectionToTarget(int targetX, int targetY, java.util.HashSet<Wall> walls, int tileSize) {
        int currentTileX = x / tileSize;
        int currentTileY = y / tileSize;
        
        Direction bestDirection = null;
        double shortestDistance = Double.MAX_VALUE;
        Direction opposite = getOppositeDirection(direction);
        
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        
        for (Direction dir : directions) {
            // In CHASE mode, allow reverse; in other modes, avoid it
            if (currentMode != GhostMode.CHASE && dir == opposite) continue;
            
            if (isValidDirection(dir, walls, tileSize)) {
                int newX = currentTileX;
                int newY = currentTileY;
                
                switch (dir) {
                    case UP: newY--; break;
                    case DOWN: newY++; break;
                    case LEFT: newX--; break;
                    case RIGHT: newX++; break;
                }
                
                double distance = Math.abs(newX - targetX) + Math.abs(newY - targetY);
                
                // Small bonus for not being the last direction (avoid oscillation)
                if (dir != lastDirection) {
                    distance -= 0.1;
                }
                
                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    bestDirection = dir;
                }
            }
        }
        
        // If no forward direction is valid, allow reversing
        if (bestDirection == null && isValidDirection(opposite, walls, tileSize)) {
            bestDirection = opposite;
        }
        
        return bestDirection;
    }
    
    private Direction getPatrolPattern(java.util.HashSet<Wall> walls, int tileSize) {
        Direction[] preferredOrder = {Direction.RIGHT, Direction.DOWN, Direction.LEFT, Direction.UP};
        Direction opposite = getOppositeDirection(direction);
        
        for (Direction dir : preferredOrder) {
            if (dir != opposite && dir != lastDirection && isValidDirection(dir, walls, tileSize)) {
                if (random.nextInt(100) < 80) {
                    return dir;
                }
            }
        }
        
        for (Direction dir : preferredOrder) {
            if (dir != opposite && isValidDirection(dir, walls, tileSize)) {
                return dir;
            }
        }
        
        return getRandomValidDirection(walls, tileSize);
    }
    
    private Direction getSmartEscapeDirection(Player player, java.util.HashSet<Wall> walls, int tileSize) {
        Direction bestEscape = null;
        int maxDistance = 0;
        Direction opposite = getOppositeDirection(direction);
        
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        
        for (Direction dir : directions) {
            if (dir == opposite) continue;
            
            if (isValidDirection(dir, walls, tileSize)) {
                int newX = x;
                int newY = y;
                
                // Look ahead several tiles
                int lookAhead = 4;
                switch (dir) {
                    case UP: newY -= tileSize * lookAhead; break;
                    case DOWN: newY += tileSize * lookAhead; break;
                    case LEFT: newX -= tileSize * lookAhead; break;
                    case RIGHT: newX += tileSize * lookAhead; break;
                }
                
                int distance = Math.abs(newX - player.getX()) + Math.abs(newY - player.getY());
                
                if (distance > maxDistance) {
                    maxDistance = distance;
                    bestEscape = dir;
                }
            }
        }
        
        return bestEscape;
    }
    
    private Direction getRandomValidDirection(java.util.HashSet<Wall> walls, int tileSize) {
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        Direction opposite = getOppositeDirection(direction);
        
        // Shuffle directions
        for (int i = directions.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Direction temp = directions[i];
            directions[i] = directions[j];
            directions[j] = temp;
        }
        
        // Try non-opposite, non-last directions first
        for (Direction dir : directions) {
            if (dir != opposite && dir != lastDirection && isValidDirection(dir, walls, tileSize)) {
                return dir;
            }
        }
        
        // Try non-opposite directions
        for (Direction dir : directions) {
            if (dir != opposite && isValidDirection(dir, walls, tileSize)) {
                return dir;
            }
        }
        
        // Try any valid direction
        for (Direction dir : directions) {
            if (isValidDirection(dir, walls, tileSize)) {
                return dir;
            }
        }
        
        return direction;
    }
    
    private Direction getEmergencyDirection(java.util.HashSet<Wall> walls, int tileSize) {
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        
        for (Direction dir : directions) {
            if (isValidDirection(dir, walls, tileSize)) {
                return dir;
            }
        }
        
        return null;
    }
    
    /**
     * **UPDATED: Grid-based collision detection**
     */
    private boolean isValidDirection(Direction dir, java.util.HashSet<Wall> walls, int tileSize) {
        if (dir == null) return false;
        
        // Calculate next grid position
        int currentGridX = x / tileSize;
        int currentGridY = y / tileSize;
        int nextGridX = currentGridX;
        int nextGridY = currentGridY;
        
        switch (dir) {
            case UP: nextGridY--; break;
            case DOWN: nextGridY++; break;
            case LEFT: nextGridX--; break;
            case RIGHT: nextGridX++; break;
        }
        
        // Check bounds
        if (nextGridY < 0 || nextGridY >= rowCount) {
            return false;
        }
        
        // Handle tunnels
        if (nextGridX < 0 || nextGridX >= columnCount) {
            // Allow tunnel only in middle rows
            return nextGridY >= 9 && nextGridY <= 11;
        }
        
        // Check for wall collision at exact grid position
        int pixelX = nextGridX * tileSize;
        int pixelY = nextGridY * tileSize;
        
        java.awt.Rectangle nextBounds = new java.awt.Rectangle(
            pixelX + 1, pixelY + 1, 
            tileSize - 2, tileSize - 2
        );
        
        for (Wall wall : walls) {
            if (nextBounds.intersects(wall.getBounds())) {
                return false;
            }
        }
        
        return true;
    }
    
    private Direction getOppositeDirection(Direction dir) {
        if (dir == null) return Direction.UP;
        switch (dir) {
            case UP: return Direction.DOWN;
            case DOWN: return Direction.UP;
            case LEFT: return Direction.RIGHT;
            case RIGHT: return Direction.LEFT;
            default: return Direction.UP;
        }
    }
    
    /**
     * **REMOVED: Old velocity-based movement - now using grid-based**
     */
    @Override
    public void setDirection(Direction direction) {
        // For grid-based movement, we use startMovement instead
        startMovement(direction);
    }
    
    public void setScared(boolean scared) {
        this.isScared = scared;
        
        if (scared && scaredImage != null) {
            this.image = scaredImage;
        } else {
            this.image = normalImage;
        }
        
        this.directionTimer = 0;
    }
    
    public boolean isScared() {
        return isScared;
    }
    
    public void setScaredImage(Image img) {
        this.scaredImage = img;
    }
    
    public GhostMode getCurrentMode() {
        return currentMode;
    }
    
    /**
     * **FIX: Enhanced reset method**
     */
    @Override
    public void reset() {
        super.reset();
        setScared(false);
        this.direction = Direction.UP;
        this.lastDirection = Direction.UP;
        this.currentMode = GhostMode.SCATTER;
        this.modeTimer = 0;
        this.directionTimer = 0;
        this.stuckCounter = 0;
        
        // **FIX 8: More thorough reset of movement state**
        this.isMoving = false;
        this.moveProgress = 0;
        this.pendingDirection = null;
        snapToGrid();
        this.targetX = this.x;
        this.targetY = this.y;
        this.aiUpdateCounter = 0;
        
        // Ensure ghost has valid starting position
        if (this.x < 0 || this.y < 0) {
            this.x = this.startX;
            this.y = this.startY;
            snapToGrid();
            this.targetX = this.x;
            this.targetY = this.y;
        }
    }
    
    // Constants cho board size
    private static final int columnCount = 19;
    private static final int rowCount = 21;
    
    // **NEW: Grid movement getters for debugging**
    public boolean isMoving() {
        return isMoving;
    }
    
    public int getMoveProgress() {
        return moveProgress;
    }
    
    public int getGridX() {
        return x / tileSize;
    }
    
    public int getGridY() {
        return y / tileSize;
    }
    
    // Backward compatibility methods
    public void changeDirectionRandomly(int tileSize) {
        Direction newDir = getRandomValidDirection(new java.util.HashSet<>(), tileSize);
        if (newDir != null) {
            startMovement(newDir);
        }
    }
    
    public void updateVelocity(int tileSize) {
        // No longer needed with grid-based movement
    }
    
    public void ensureMovement() {
        // Grid-based movement handles this automatically
    }
}