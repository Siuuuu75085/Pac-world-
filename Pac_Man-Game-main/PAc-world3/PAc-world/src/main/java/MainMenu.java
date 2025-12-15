import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import utils.ImageManager;
import utils.SoundManager;

public class MainMenu extends JPanel {
    private Image pacmanLogo;
    private Image redGhost;
    private Image blueGhost;
    private Image pinkGhost;
    private Image orangeGhost;
    private JButton playButton;
    private JButton helpButton;
    private JButton settingsButton;
    private JButton exitButton;
    private Game game;
    private JFrame parentFrame;
    private GamePanel gamePanel;
    private SoundManager soundManager;

    // UI Constants
    private final Color BUTTON_COLOR = new Color(16, 28, 140);
    private final Color HOVER_COLOR = new Color(40, 60, 220);
    private final Color PRESSED_COLOR = new Color(10, 20, 100);
    private final Font TITLE_FONT = new Font("Arial", Font.BOLD, 70);
    private final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 28);

    // Animation properties
    private Timer animationTimer;
    private int animationTick = 0;
    private float glowIntensity = 0;
    private boolean glowIncreasing = true;
    private int ghostX = -50;

    public MainMenu(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.setLayout(new BorderLayout());
        this.soundManager = new SoundManager();
        soundManager.playSound("begin.wav", false);

        // Start background animation
        animationTimer = new Timer(40, e -> {
            animationTick++;

            // Control the glow effect
            if (glowIncreasing) {
                glowIntensity += 0.02f;
                if (glowIntensity >= 1.0f) {
                    glowIntensity = 1.0f;
                    glowIncreasing = false;
                }
            } else {
                glowIntensity -= 0.02f;
                if (glowIntensity <= 0.3f) {
                    glowIntensity = 0.3f;
                    glowIncreasing = true;
                }
            }

            // Move ghost across screen
            ghostX += 2;
            if (ghostX > getWidth() + 50) {
                ghostX = -50;
            }

            repaint();
        });
        animationTimer.start();

        loadResources();
        setupUI();
    }

    private void loadResources() {
        ImageManager imageManager = new ImageManager();
        pacmanLogo = imageManager.loadImage("/images/pacmanRight.png");
        redGhost = imageManager.loadImage("/images/redGhost.png");
        blueGhost = imageManager.loadImage("/images/blueGhost.png");
        pinkGhost = imageManager.loadImage("/images/pinkGhost.png");
        orangeGhost = imageManager.loadImage("/images/orangeGhost.png");
    }

    private void setupUI() {
        // Use the entire panel for custom painting
        setLayout(null);

        // Create buttons
        playButton = createStyledButton("PLAY GAME");
        helpButton = createStyledButton("HELP");
        settingsButton = createStyledButton("SETTINGS");
        exitButton = createStyledButton("EXIT");

        // Position buttons
        int buttonWidth = 250;
        int buttonHeight = 60;
        int centerX = 608 / 2 - buttonWidth / 2;

        playButton.setBounds(centerX, 300, buttonWidth, buttonHeight);
        helpButton.setBounds(centerX, 380, buttonWidth, buttonHeight);
        settingsButton.setBounds(centerX, 460, buttonWidth, buttonHeight);
        exitButton.setBounds(centerX, 540, buttonWidth, buttonHeight);

        // Add action listeners
        playButton.addActionListener(e -> {
            soundManager.playSound("click.wav", false);
            startGame();
        });

        helpButton.addActionListener(e -> {
            soundManager.playSound("click.wav", false);
            showHelp();
        });

        settingsButton.addActionListener(e -> {
            soundManager.playSound("click.wav", false);
            showSettings();
        });

        exitButton.addActionListener(e -> {
            soundManager.playSound("death.wav", false);
            // Small delay to allow sound to play before exit
            Timer exitTimer = new Timer(500, event -> System.exit(0));
            exitTimer.setRepeats(false);
            exitTimer.start();
        });

        // Add buttons to panel
        add(playButton);
        add(helpButton);
        add(settingsButton);
        add(exitButton);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Button background with gradient
                GradientPaint gp;
                if (getModel().isPressed()) {
                    gp = new GradientPaint(0, 0, PRESSED_COLOR, 0, getHeight(), PRESSED_COLOR.darker());
                } else if (getModel().isRollover()) {
                    gp = new GradientPaint(0, 0, HOVER_COLOR, 0, getHeight(), HOVER_COLOR.darker());
                } else {
                    gp = new GradientPaint(0, 0, BUTTON_COLOR, 0, getHeight(), BUTTON_COLOR.darker());
                }
                g2.setPaint(gp);

                // Draw rounded button with beveled edge effect
                RoundRectangle2D.Double rect = new RoundRectangle2D.Double(3, 3, getWidth() - 6, getHeight() - 6, 20,
                        20);
                g2.fill(rect);

                // Draw outer glow
                if (getModel().isRollover() && !getModel().isPressed()) {
                    g2.setColor(new Color(100, 100, 255, 100));
                    g2.setStroke(new BasicStroke(3));
                    g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 22, 22));
                }

                // Draw button highlight (top part lighter)
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fill(new RoundRectangle2D.Double(5, 5, getWidth() - 10, (getHeight() - 10) / 2, 18, 18));

                // Draw button text with shadow
                g2.setColor(new Color(0, 0, 0, 120));
                g2.setFont(BUTTON_FONT);
                FontMetrics metrics = g2.getFontMetrics();
                int x = (getWidth() - metrics.stringWidth(getText())) / 2 + 2;
                int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent() + 2;
                g2.drawString(getText(), x, y);

                // Draw button text
                g2.setColor(Color.YELLOW);
                g2.drawString(getText(), x - 2, y - 2);

                g2.dispose();
            }
        };

        // Button styling
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                soundManager.playSound("click.wav", false);
            }
        });

        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background gradient
        GradientPaint bgGradient = new GradientPaint(
                0, 0, new Color(0, 0, 30),
                0, getHeight(), new Color(0, 0, 80));
        g2d.setPaint(bgGradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw maze-like background
        drawMazeBackground(g2d);

        // Draw title
        drawTitle(g2d);

        // Draw animated ghosts
        drawAnimatedGhosts(g2d);

        // Draw version info
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.setColor(new Color(150, 150, 150));
        g2d.drawString("v1.0", getWidth() - 30, getHeight() - 10);
    }

    private void drawMazeBackground(Graphics2D g2d) {
        // Draw maze-like lines
        g2d.setColor(new Color(0, 0, 150, 30));
        g2d.setStroke(new BasicStroke(2));

        // Horizontal lines
        for (int y = 50; y < getHeight(); y += 50) {
            g2d.drawLine(20, y, getWidth() - 20, y);
        }

        // Vertical lines
        for (int x = 50; x < getWidth(); x += 50) {
            g2d.drawLine(x, 20, x, getHeight() - 20);
        }

        // Draw dots (pellets)
        g2d.setColor(new Color(255, 255, 255, 50));
        for (int y = 50; y < getHeight(); y += 50) {
            for (int x = 50; x < getWidth(); x += 50) {
                // Skip some dots randomly for variety
                if ((x + y) % 100 != 0) {
                    g2d.fillOval(x - 3, y - 3, 6, 6);
                }
            }
        }

        // Draw some power pellets
        g2d.setColor(new Color(255, 255, 255, 40 + (int) (40 * glowIntensity)));
        g2d.fillOval(100 - 6, 200 - 6, 12, 12);
        g2d.fillOval(500 - 6, 150 - 6, 12, 12);
        g2d.fillOval(300 - 6, 500 - 6, 12, 12);
    }

    private void drawTitle(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Calculate center position
        FontMetrics metrics = g2d.getFontMetrics(TITLE_FONT);
        String titleText = "PAC-MAN";
        int titleX = (getWidth() - metrics.stringWidth(titleText)) / 2;

        // Draw shadow with blur effect
        for (int i = 0; i < 5; i++) {
            g2d.setColor(new Color(0, 0, 100, 20));
            g2d.setFont(TITLE_FONT);
            g2d.drawString(titleText, titleX + i, 120 + i);
        }

        // Draw title glow
        float alpha = 0.3f + (0.7f * glowIntensity);
        g2d.setColor(new Color(1.0f, 1.0f, 0.0f, alpha));
        g2d.setFont(TITLE_FONT);
        g2d.drawString(titleText, titleX - 1, 119);

        // Draw main title
        g2d.setColor(Color.YELLOW);
        g2d.setFont(TITLE_FONT);
        g2d.drawString(titleText, titleX, 120);

        // Draw Pac-Man logo with slight bouncing
        if (pacmanLogo != null) {
            int bounceOffset = (int) (Math.sin(animationTick * 0.1) * 3);
            g2d.drawImage(pacmanLogo, titleX - 85, 62 + bounceOffset, 70, 70, null);
        }

        // Draw ghosts under title
        int ghostSize = 40;
        int ghostY = 170;
        int spacing = 60;
        int startX = (getWidth() / 2) - (spacing * 2);

        // Animate ghost bobbing
        int[] bobOffsets = new int[4];
        for (int i = 0; i < 4; i++) {
            bobOffsets[i] = (int) (Math.sin((animationTick + i * 15) * 0.1) * 4);
        }

        if (redGhost != null)
            g2d.drawImage(redGhost, startX, ghostY + bobOffsets[0], ghostSize, ghostSize, null);
        if (blueGhost != null)
            g2d.drawImage(blueGhost, startX + spacing, ghostY + bobOffsets[1], ghostSize, ghostSize, null);
        if (pinkGhost != null)
            g2d.drawImage(pinkGhost, startX + spacing * 2, ghostY + bobOffsets[2], ghostSize, ghostSize, null);
        if (orangeGhost != null)
            g2d.drawImage(orangeGhost, startX + spacing * 3, ghostY + bobOffsets[3], ghostSize, ghostSize, null);
    }

    private void drawAnimatedGhosts(Graphics2D g2d) {
        // Draw a ghost moving across the bottom of the screen
        Image ghostToShow;
        switch ((animationTick / 30) % 4) {
            case 0:
                ghostToShow = redGhost;
                break;
            case 1:
                ghostToShow = blueGhost;
                break;
            case 2:
                ghostToShow = pinkGhost;
                break;
            default:
                ghostToShow = orangeGhost;
        }

        if (ghostToShow != null) {
            g2d.drawImage(ghostToShow, ghostX, getHeight() - 70, 40, 40, null);
        }

        // Also draw pacman chasing if ghost is far enough along
        if (ghostX > 100) {
            int pacmanX = ghostX - 80;
            int bobOffset = (int) (Math.sin(animationTick * 0.3) * 3);
            g2d.drawImage(pacmanLogo, pacmanX, getHeight() - 70 + bobOffset, 40, 40, null);
        }
    }

    private void startGame() {
        // Stop animation timer first
        if (animationTimer != null) {
            animationTimer.stop();
        }

        // Show loading message
        JLabel loadingLabel = new JLabel("Loading game...", JLabel.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.BOLD, 24));
        loadingLabel.setForeground(Color.YELLOW);

        // Temporarily show loading
        removeAll();
        add(loadingLabel, BorderLayout.CENTER);
        revalidate();
        repaint();

        // Use SwingWorker to load game in background
        SwingWorker<Void, Void> gameLoader = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Create game in background thread
                game = new Game();
                gamePanel = new GamePanel(game);
                return null;
            }

            @Override
            protected void done() {
                try {
                    // Switch to game panel
                    parentFrame.getContentPane().removeAll();
                    parentFrame.add(gamePanel);
                    parentFrame.pack();
                    parentFrame.revalidate();
                    parentFrame.repaint();

                    gamePanel.requestFocusInWindow();
                    game.start();

                } catch (Exception e) {
                    e.printStackTrace();
                    // If error, show error message and stay on menu
                    JOptionPane.showMessageDialog(
                            parentFrame,
                            "Error starting game: " + e.getMessage(),
                            "Game Error",
                            JOptionPane.ERROR_MESSAGE);

                    // Restore menu
                    setupUI();
                    revalidate();
                    repaint();
                }
            }
        };

        gameLoader.execute();
    }

    private void showHelp() {
        JPanel helpPanel = new JPanel();
        helpPanel.setLayout(new BorderLayout(10, 10));
        helpPanel.setBackground(new Color(0, 0, 50));
        helpPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("How to Play", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.YELLOW);

        JTextArea instructionsArea = new JTextArea(
                "• Use ARROW KEYS to move Pac-Man\n" +
                        "• Eat all dots to complete the level\n" +
                        "• Power pellets let you eat ghosts\n" +
                        "• Press P to pause the game\n" +
                        "• Avoid ghosts or lose a life\n" +
                        "• You have 3 lives - good luck!");
        instructionsArea.setFont(new Font("Arial", Font.PLAIN, 14));
        instructionsArea.setForeground(Color.WHITE);
        instructionsArea.setBackground(new Color(0, 0, 70));
        instructionsArea.setEditable(false);
        instructionsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        helpPanel.add(titleLabel, BorderLayout.NORTH);
        helpPanel.add(instructionsArea, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(
                this,
                helpPanel,
                "Game Instructions",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void showSettings() {
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBackground(new Color(0, 0, 50));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Game Settings", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.YELLOW);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        settingsPanel.add(titleLabel, gbc);

        // Sound Setting
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        JLabel soundLabel = new JLabel("Sound:");
        soundLabel.setFont(new Font("Arial", Font.BOLD, 16));
        soundLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        settingsPanel.add(soundLabel, gbc);

        JButton soundToggleButton = createSettingsButton(
                GameSettings.isSoundEnabled() ? "ON" : "OFF");
        soundToggleButton.addActionListener(e -> {
            GameSettings.toggleSound();
            soundToggleButton.setText(GameSettings.isSoundEnabled() ? "ON" : "OFF");
            soundManager.setSoundEnabled(GameSettings.isSoundEnabled());
            if (GameSettings.isSoundEnabled()) {
                soundManager.playSound("pellet1.wav", false);
            }
        });
        gbc.gridx = 1;
        settingsPanel.add(soundToggleButton, gbc);

        // Difficulty Setting
        gbc.gridy = 2;
        JLabel difficultyLabel = new JLabel("Difficulty:");
        difficultyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        difficultyLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        settingsPanel.add(difficultyLabel, gbc);

        JButton difficultyButton = createSettingsButton(GameSettings.getDifficulty().toString());
        difficultyButton.addActionListener(e -> {
            GameSettings.cycleDifficulty();
            difficultyButton.setText(GameSettings.getDifficulty().toString());
            if (GameSettings.isSoundEnabled()) {
                soundManager.playSound("click.wav", false);
            }
        });
        gbc.gridx = 1;
        settingsPanel.add(difficultyButton, gbc);

        // Difficulty Description
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JTextArea descArea = new JTextArea(getDifficultyDescription());
        descArea.setFont(new Font("Arial", Font.PLAIN, 12));
        descArea.setForeground(Color.LIGHT_GRAY);
        descArea.setBackground(new Color(0, 0, 70));
        descArea.setEditable(false);
        descArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        settingsPanel.add(descArea, gbc);

        JOptionPane.showOptionDialog(
                this,
                settingsPanel,
                "Game Settings",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[] { "Apply & Close" },
                "Apply & Close");
    }

    private JButton createSettingsButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(16, 28, 140));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private String getDifficultyDescription() {
        switch (GameSettings.getDifficulty()) {
            case EASY:
                return "Easy: Simple maze, ghosts move randomly.\nGood for beginners!";
            case NORMAL:
                return "Normal: More complex maze, ghosts chase you\nwhen nearby. Balanced challenge!";
            case HARD:
                return "Hard: Complex maze, aggressive ghosts with\npathfinding AI. Prepare for a challenge!";
            default:
                return "";
        }
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(608, 672); // Same size as game board
    }
}