package utils;

import java.awt.Image;

public class Animation {
    private Image[] frames;
    private int currentFrame = 0;
    private int frameDelay;
    private int delayCounter = 0;
    
    public Animation(Image[] frames, int frameDelay) {
        this.frames = frames;
        this.frameDelay = frameDelay;
    }
    
    public Image getCurrentFrame() {
        delayCounter++;
        if (delayCounter >= frameDelay) {
            delayCounter = 0;
            currentFrame = (currentFrame + 1) % frames.length;
        }
        return frames[currentFrame];
    }
}