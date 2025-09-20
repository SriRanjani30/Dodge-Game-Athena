import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.*;

public class DodgeGameWithStars extends JPanel implements ActionListener, KeyListener {

    private boolean inMenu = true;
    private boolean inGame = false;
    private boolean gameOver = false;
    private String difficulty = "Normal";
    private int blockSpeed = 4;
    private int spawnRate = 40;
    private int playerX, playerY;
    private int playerWidth = 150, playerHeight = 150;
    private int playerSpeed = 10;
    private java.util.List<Rectangle> blocks = new ArrayList<>();
    private int blockWidth = 100, blockHeight = 100;
    private int blockTimer = 0;
    private java.util.List<Rectangle> powerUps = new ArrayList<>();
    private int powerUpSize = 100;
    private int powerUpTimer = 0;

    private int score = 0;
    private int lives = 3;

    private Timer timer;

    private Image playerImage;
    private Image blockImage;
    private Image powerUpImage;

    private final java.util.List<Star> stars = new ArrayList<>();
    private final Random rnd = new Random();
    private int starSpawnTimer = 0;
    private int maxStars = 120;

    private boolean movingLeft = false;
    private boolean movingRight = false;

    public DodgeGameWithStars()
    {
        setFocusable(true);
        addKeyListener(this);

        playerImage = new ImageIcon(DodgeGameWithStars.class.getResource("/Images/player.png")).getImage();
        blockImage = new ImageIcon(DodgeGameWithStars.class.getResource("/Images/block.png")).getImage();
        powerUpImage = new ImageIcon(DodgeGameWithStars.class.getResource("/Images/powerup.png")).getImage();

        timer = new Timer(16, this);

        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                int W = getWidth();
                int H = getHeight();
                playerX = Math.max(0, W / 2 - playerWidth / 2);
                playerY = Math.max(0, H - playerHeight - 30);
            }
        });
    }

    public void startGame()
    {
        inMenu = false;
        inGame = true;
        gameOver = false;
        score = 0;
        lives = 3;
        blocks.clear();
        powerUps.clear();
        stars.clear();
        timer.start();
    }

    public void setDifficulty(String level)
    {
        difficulty = level;
        switch (level) {
            case "Easy":
                blockSpeed = 3;
                spawnRate = 60;
                break;
            case "Normal":
                blockSpeed = 5;
                spawnRate = 40;
                break;
            case "Hard":
                blockSpeed = 8;
                spawnRate = 25;
                break;
        }
    }

    @Override
    protected void paintComponent(Graphics g0)
    {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        int WIDTH = getWidth();
        int HEIGHT = getHeight();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        synchronized (stars) {
            for (Star s : stars) {
                s.draw(g);
            }
        }

        GradientPaint haze = new GradientPaint(0, HEIGHT - 120, new Color(0, 0, 0, 0),
                0, HEIGHT, new Color(0, 0, 0, 120));
        g.setPaint(haze);
        g.fillRect(0, HEIGHT - 180, WIDTH, 180);

        if (inMenu)
        {
            drawMenu(g, WIDTH, HEIGHT);
        } else if (inGame) 
        {
            drawGame(g, WIDTH, HEIGHT);
        } else if (gameOver)
        {
            drawGameOver(g, WIDTH, HEIGHT);
        }
        g.dispose();
    }

    private void drawMenu(Graphics2D g, int WIDTH, int HEIGHT)
    {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 42));
        drawCenteredString(g, "DODGE GAME", WIDTH, HEIGHT / 3);
        g.setFont(new Font("Arial", Font.PLAIN, 22));
        drawCenteredString(g, "Press 1 for Easy   |   2 for Normal   |   3 for Hard", WIDTH, HEIGHT / 2);
        drawCenteredString(g, "Use Left/Right arrows to move. Collect yellow power-ups to gain a life.", WIDTH, HEIGHT / 2 + 40);
    }

    private void drawGame(Graphics2D g, int WIDTH, int HEIGHT)
    {
        if (playerImage != null) 
        {
            g.drawImage(playerImage, playerX, playerY, playerWidth, playerHeight, null);
        }
        else 
        {
            g.setColor(Color.GREEN);
            g.fillRect(playerX, playerY, playerWidth, playerHeight);
        }

        for (Rectangle block : blocks)
        {
            if (blockImage != null) 
            {
                g.drawImage(blockImage, block.x, block.y, block.width, block.height, null);
            } 
            else 
            {
                g.setColor(Color.RED);
                g.fillRect(block.x, block.y, block.width, block.height);
            }
        }

        for (Rectangle p : powerUps)
        {
            if (powerUpImage != null)
            {
                g.drawImage(powerUpImage, p.x, p.y, p.width, p.height, null);
            } else
            {
                g.setColor(Color.YELLOW);
                g.fillOval(p.x, p.y, p.width, p.height);
            }
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 20, 30);
        g.drawString("Lives: " + lives, 20, 60);
        g.drawString("Mode: " + difficulty, WIDTH - 150, 30);
    }

    private void drawGameOver(Graphics2D g, int WIDTH, int HEIGHT)
    {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 42));
        drawCenteredString(g, "GAME OVER", WIDTH, HEIGHT / 3);

        g.setFont(new Font("Arial", Font.PLAIN, 22));
        drawCenteredString(g, "Final Score: " + score, WIDTH, HEIGHT / 2 - 10);
        drawCenteredString(g, "Press ENTER to return to Menu", WIDTH, HEIGHT / 2 + 30);
    }

    private void drawCenteredString(Graphics2D g, String text, int panelWidth, int baselineY)
    {
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g.drawString(text, (panelWidth - textWidth) / 2, baselineY);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (movingLeft)
        {
            playerX -= playerSpeed;
        }
        if (movingRight)
        {
            playerX += playerSpeed;
        }

        updateStars();

        if (!inGame)
        {
            repaint();
            return;
        }

        int WIDTH = getWidth();
        int HEIGHT = getHeight();

        if (playerX < 0) playerX = 0;
        if (playerX > WIDTH - playerWidth) playerX = WIDTH - playerWidth;
        blockTimer++;

        if (blockTimer >= spawnRate)
        {
            int blockX = (int) (Math.random() * Math.max(1, WIDTH - blockWidth));
            blocks.add(new Rectangle(blockX, 0, blockWidth, blockHeight));
            blockTimer = 0;
        }

        powerUpTimer++;

        if (powerUpTimer >= 300)
        {
            int pX = (int) (Math.random() * Math.max(1, WIDTH - powerUpSize));
            powerUps.add(new Rectangle(pX, 0, powerUpSize, powerUpSize));
            powerUpTimer = 0;
        }

        Iterator<Rectangle> it = blocks.iterator();
        while (it.hasNext()) 
        {
            Rectangle block = it.next();
            block.y += blockSpeed;
            if (block.y > HEIGHT)
            {
                it.remove();
                score++;
            } 
            else if (block.intersects(new Rectangle(playerX, playerY, playerWidth, playerHeight))) 
            {
                it.remove();
                lives--;
                if (lives <= 0)
                {
                    inGame = false;
                    gameOver = true;
                    timer.stop();
                }
            }
        }

        Iterator<Rectangle> puIt = powerUps.iterator();
        while (puIt.hasNext())
        {
            Rectangle p = puIt.next();
            p.y += Math.max(1, blockSpeed - 2);
            if (p.y > HEIGHT) 
            {
                puIt.remove();
            }
            else if (p.intersects(new Rectangle(playerX, playerY, playerWidth, playerHeight)))
            {
                puIt.remove();
                lives++;
            }
        }

        playerY = HEIGHT - playerHeight - 30;

        repaint();
    }

    private void updateStars()
    {
        starSpawnTimer++;
        int spawnThreshold = Math.max(2, 12 - (maxStars - stars.size()) / 12);
        if (starSpawnTimer >= spawnThreshold && stars.size() < maxStars) 
        {
            starSpawnTimer = 0;
            int W = Math.max(100, getWidth());
            int H = Math.max(100, getHeight());
            float x = rnd.nextInt(W);
            float y = rnd.nextInt(H);
            float size = 1f + rnd.nextFloat() * 3f;
            int life = 40 + rnd.nextInt(60);
            float vy = 0.1f + rnd.nextFloat() * 0.6f;
            synchronized (stars) {
                stars.add(new Star(x, y, size, life, vy));
            }
        }

        synchronized (stars)
        {
            Iterator<Star> sit = stars.iterator();
            while (sit.hasNext()) {
                Star s = sit.next();
                s.update();
                if (s.dead()) sit.remove();
            }
        }
    }

    private static class Star
    {
        float x, y;
        float size;
        int life;
        int maxLife;
        float vy;

        public Star(float x, float y, float size, int life, float vy) {
            this.x = x; this.y = y; this.size = size; this.life = life; this.maxLife = life; this.vy = vy;
        }

        public void update()
        {
            y += vy;
            life--;
        }

        public boolean dead() 
        { 
            return life <= 0; 
        }

        public void draw(Graphics2D g)
        {
            float alpha = Math.max(0f, (float) life / (float) maxLife);
            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, alpha)));
            g.setColor(Color.WHITE);
            float r = Math.max(1f, size);
            g.fillOval(Math.round(x - r/2), Math.round(y - r/2), Math.round(r), Math.round(r));
            g.setComposite(old);
        }
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        int key = e.getKeyCode();

        if (inMenu) 
        {
            if (key == KeyEvent.VK_1)
            {
                setDifficulty("Easy");
                startGame();
            }
            else if (key == KeyEvent.VK_2)
            {
                setDifficulty("Normal");
                startGame();
            }
            else if (key == KeyEvent.VK_3)
            {
                setDifficulty("Hard");
                startGame();
            }
        } 
        else if (inGame) 
        {
            if (key == KeyEvent.VK_LEFT)
            {
                movingLeft = true;
            }
            else if (key == KeyEvent.VK_RIGHT)
            {
                movingRight = true;
            }
            else if (key == KeyEvent.VK_ESCAPE)
            {
                timer.stop();
                inGame = false;
                inMenu = true;
            }
        } 
        else if (gameOver)
        {
            if (key == KeyEvent.VK_ENTER)
            {
                inMenu = true;
                gameOver = false;
                repaint();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT)
        {
            movingLeft = false;
        } 
        else if (key == KeyEvent.VK_RIGHT)
        {
            movingRight = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e)
    {

    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Dodge Game (Stars Background)");
            DodgeGameWithStars game = new DodgeGameWithStars();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.add(game);
            frame.setVisible(true);
            game.playerX = frame.getWidth() / 2 - game.playerWidth / 2;
            game.playerY = frame.getHeight() - game.playerHeight - 30;
        });
    }
}
