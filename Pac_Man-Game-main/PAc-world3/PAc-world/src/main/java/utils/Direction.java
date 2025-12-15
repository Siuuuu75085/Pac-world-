package utils;

import java.awt.event.KeyEvent;

public enum Direction {
    UP('U'), DOWN('D'), LEFT('L'), RIGHT('R');
    
    private final char symbol;
    
    Direction(char symbol) {
        this.symbol = symbol;
    }
    
    public char getSymbol() {
        return symbol;
    }
    
    public static Direction fromKeyCode(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP: return UP;
            case KeyEvent.VK_DOWN: return DOWN;
            case KeyEvent.VK_LEFT: return LEFT;
            case KeyEvent.VK_RIGHT: return RIGHT;
            default: return RIGHT;
        }
    }
}