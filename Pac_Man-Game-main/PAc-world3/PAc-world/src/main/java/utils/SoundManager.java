package utils;

import javax.sound.sampled.*;
import java.io.InputStream;
import java.io.BufferedInputStream;

public class SoundManager {
    private Clip backgroundMusic;
    private boolean soundEnabled = true;
    
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled) {
            stopBackgroundMusic();
        }
    }
    
    public void playSound(String filename, boolean loop) {
        if (!soundEnabled) return;
        
        try {
            InputStream audioSrc = getClass().getResourceAsStream("/sounds/" + filename);
            if (audioSrc == null) {
                System.err.println("Could not find sound file: /sounds/" + filename);
                return;
            }
            
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(bufferedIn);
            
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            
            if (loop) {
                if (backgroundMusic != null && backgroundMusic.isRunning()) {
                    backgroundMusic.stop();
                    backgroundMusic.close();
                }
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                backgroundMusic = clip;
            }
            
            clip.start();
            
        } catch (Exception e) {
            System.err.println("Error playing sound '" + filename + "': " + e.getMessage());
        }
    }
    
    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            if (backgroundMusic.isRunning()) {
                backgroundMusic.stop();
            }
            backgroundMusic.close();
            backgroundMusic = null;
        }
    }
    
    public void toggleSound() {
        setSoundEnabled(!soundEnabled);
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public void cleanup() {
        stopBackgroundMusic();
    }
}