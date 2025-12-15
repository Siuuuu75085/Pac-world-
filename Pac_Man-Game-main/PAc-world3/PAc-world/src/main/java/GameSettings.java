public class GameSettings {
    public enum Difficulty {
        EASY("Easy"),
        NORMAL("Normal"), 
        HARD("Hard");
        
        private final String displayName;
        
        Difficulty(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    private static Difficulty currentDifficulty = Difficulty.EASY;
    private static boolean soundEnabled = true;
    
    public static Difficulty getDifficulty() {
        return currentDifficulty;
    }
    
    public static void setDifficulty(Difficulty difficulty) {
        currentDifficulty = difficulty;
    }
    
    public static boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }
    
    public static void toggleSound() {
        soundEnabled = !soundEnabled;
    }
    
    public static void cycleDifficulty() {
        switch (currentDifficulty) {
            case EASY:
                currentDifficulty = Difficulty.NORMAL;
                break;
            case NORMAL:
                currentDifficulty = Difficulty.HARD;
                break;
            case HARD:
                currentDifficulty = Difficulty.EASY;
                break;
        }
    }
}
