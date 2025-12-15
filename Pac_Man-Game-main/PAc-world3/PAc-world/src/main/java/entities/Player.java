package entities;
import utils.Animation;
import utils.Direction;
import java.awt.Image;

public class Player extends Entity {
    private Animation[] animations;
    private int currentAnimation = 0;
    
    public Player(Image[] rightFrames, Image[] upFrames, 
                 Image[] downFrames, Image[] leftFrames,
                 int x, int y, int width, int height) {
        super(rightFrames[0], x, y, width, height);
        this.direction = Direction.RIGHT;
        
        animations = new Animation[4];
        animations[Direction.RIGHT.ordinal()] = new Animation(rightFrames, 10);
        animations[Direction.UP.ordinal()] = new Animation(upFrames, 10);
        animations[Direction.DOWN.ordinal()] = new Animation(downFrames, 10);
        animations[Direction.LEFT.ordinal()] = new Animation(leftFrames, 10);
    }
    
    public void updateAnimation() {
        if (animations[currentAnimation] != null) {
            image = animations[currentAnimation].getCurrentFrame();
        }
    }
    
    public void updateDirection(Direction newDirection, int tileSize) {
        this.direction = newDirection;
        updateVelocity(tileSize);
        currentAnimation = newDirection.ordinal();
    }
    
    private void updateVelocity(int tileSize) {
        int speed = tileSize/10; // Điều chỉnh số này để thay đổi tốc độ (số càng lớn, tốc độ càng chậm)
        
        switch (direction) {
            case UP:
                velocityX = 0;
                velocityY = -speed;
                break;
            case DOWN:
                velocityX = 0;
                velocityY = speed;
                break;
            case LEFT:
                velocityX = -speed;
                velocityY = 0;
                break;
            case RIGHT:
                velocityX = speed;
                velocityY = 0;
                break;
        }
    }
}