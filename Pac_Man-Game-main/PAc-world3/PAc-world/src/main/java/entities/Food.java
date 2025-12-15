package entities;

import java.awt.Image;

public class Food extends Entity {
    private int points;
    private boolean isPowerPellet;
    
    public Food(Image image, int x, int y, int width, int height, int points, boolean isPowerPellet) {
        super(image, x, y, width, height);
        this.points = points;
        this.isPowerPellet = isPowerPellet;
    }
    
    public int getPoints() {
        return points;
    }
    
    public boolean isPowerPellet() {
        return isPowerPellet;
    }
    
    public boolean isRegularFood() {
        return !isPowerPellet && (image == null || !image.toString().contains("cherry"));
    }
}
