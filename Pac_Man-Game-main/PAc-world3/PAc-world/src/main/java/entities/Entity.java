package entities;

import java.awt.Image;
import java.awt.Rectangle;
import utils.Direction;

public abstract class Entity {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected Image image;
    protected int startX;
    protected int startY;
    protected Direction direction;
    protected int velocityX;
    protected int velocityY;
    
    public Entity(Image image, int x, int y, int width, int height) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.startX = x;
        this.startY = y;
    }
    
    public void reset() {
        this.x = this.startX;
        this.y = this.startY;
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
    public boolean collidesWith(Entity other) {
        return getBounds().intersects(other.getBounds());
    }
    
    public void updatePosition() {
        x += velocityX;
        y += velocityY;
    }
    
    // Getters and setters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Image getImage() { return image; }
    public Direction getDirection() { return direction; }
    public int getVelocityX() { return velocityX; }
    public int getVelocityY() { return velocityY; }
    
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setImage(Image image) { this.image = image; }
    public void setDirection(Direction direction) { this.direction = direction; }
    public void setVelocityX(int velocityX) { this.velocityX = velocityX; }
    public void setVelocityY(int velocityY) { this.velocityY = velocityY; }
}