package utils;
import javax.swing.ImageIcon;
import java.awt.Image;

public class ImageManager {
    public Image loadImage(String path) {
        try {
            return new ImageIcon(getClass().getResource(path)).getImage();
        } catch (Exception e) {
            System.err.println("Error loading image: " + path);
            return null;
        }
    }
    
    public Image[] loadAnimationFrames(String basePath, int frameCount) {
        Image[] frames = new Image[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = loadImage(basePath + "_" + (i+1) + ".png");
        }
        return frames;
    }
}