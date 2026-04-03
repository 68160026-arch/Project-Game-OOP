import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.File;

public class CatVsDogGame extends JPanel implements ActionListener {
    private CatPlayer cat;
    private DogCharacter dog;
    private Projectile currentProjectile = new Bone();
    private Image bgMenu, bgMorning, bgEvening, bgNight;
    private int timeOfDay;
    private boolean playerTurn = true;
    private boolean isCharging = false;
    private double currentPower = 0;

    private enum GameState { MENU, CHOOSE_CHAR, PLAYING, GAMEOVER }
    private GameState currentState = GameState.MENU;
    private boolean isPlayerCat = true;
    private boolean isMultiplayer = false;
    private String winnerText = "";
    private String p1Name = "PLAYER 1", p2Name = "PLAYER 2";
    private Clip bgmClip;

    public CatVsDogGame() {
        bgMenu = new ImageIcon("bg_menu.png").getImage();
        bgMorning = new ImageIcon("bg_morning.png").getImage();
        bgEvening = new ImageIcon("bg_evening.png").getImage();
        bgNight = new ImageIcon("bg_night.png").getImage();

        playBGM("bgm.wav");
        new Timer(20, this).start();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePress(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentState == GameState.PLAYING && isCharging) {
                    isCharging = false;
                    if (!currentProjectile.active) {
                        int startX = playerTurn ? cat.x + 101 : dog.x - 10;
                        double angle = playerTurn ? 45 : 135;
                        currentProjectile.launch(startX, cat.y + 20, angle, currentPower);
                        playSound("throw.wav");
                    }
                }
            }
        });
    }

    private void handleMousePress(MouseEvent e) {
        int mx = e.getX(), my = e.getY();
        int w = getWidth(), h = getHeight();

        if (currentState == GameState.MENU) {
            if (mx > w/2 - 125 && mx < w/2 + 125) {
                if (my > h/2 - 80 && my < h/2) currentState = GameState.CHOOSE_CHAR;
                else if (my > h/2 + 20 && my < h/2 + 100) {
                    isMultiplayer = true; askNamesPVP(); initGame(); currentState = GameState.PLAYING;
                }
                else if (my > h/2 + 120 && my < h/2 + 200) System.exit(0);
            }
        } else if (currentState == GameState.CHOOSE_CHAR) {
            if (my > h/2 - 50 && my < h/2 + 150) {
                if (mx > w/2 - 220 && mx < w/2 - 20) {
                    isPlayerCat = true; askNameSingle(); initGame(); currentState = GameState.PLAYING;
                }
                else if (mx > w/2 + 20 && mx < w/2 + 220) {
                    isPlayerCat = false; askNameSingle(); initGame(); currentState = GameState.PLAYING;
                }
            }
        } else if (currentState == GameState.GAMEOVER) {
            if (mx > w/2 - 125 && mx < w/2 + 125) {
                if (my > h/2 && my < h/2 + 60) { initGame(); currentState = GameState.PLAYING; }
                else if (my > h/2 + 80 && my < h/2 + 140) currentState = GameState.MENU;
            }
        } else if (currentState == GameState.PLAYING && !currentProjectile.active) {
            boolean myTurn = isMultiplayer || (isPlayerCat ? playerTurn : !playerTurn);
            if (myTurn) { isCharging = true; currentPower = 0; }
        }
    }

    private void initGame() {
        cat = new CatPlayer(100, 400, p1Name);
        dog = new DogCharacter(800, 400, p2Name);
        timeOfDay = (int)(Math.random() * 3);
        playerTurn = true;
        currentProjectile.active = false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentState == GameState.PLAYING) {
            if (isCharging && currentPower < 100) currentPower += 2.2;

            if (currentProjectile.active) {
                currentProjectile.move();
                if (dog.isHit(currentProjectile.x, currentProjectile.y)) {
                    dog.hp -= 20; playSound("hit.wav"); handleEndTurn();
                } else if (cat.isHit(currentProjectile.x, currentProjectile.y)) {
                    cat.hp -= 20; playSound("hit.wav"); handleEndTurn();
                } else if (!currentProjectile.active) handleEndTurn();
            } else if (!isMultiplayer && (isPlayerCat ? !playerTurn : playerTurn)) {
                // AI Logic
                if (dog.hp > 0 && cat.hp > 0) {
                    if (playerTurn) aiAction(cat, dog, 45); else aiAction(dog, cat, 135);
                }
            }

            if (cat.hp <= 0 || dog.hp <= 0) {
                winnerText = (cat.hp <= 0) ? dog.name + " WIN!" : cat.name + " WIN!";
                currentState = GameState.GAMEOVER;
            }
        }
        repaint();
    }

    private void handleEndTurn() {
        currentProjectile.active = false;
        playerTurn = !playerTurn;
        currentPower = 0;
    }

    private void aiAction(GameCharacter shooter, GameCharacter target, double angle) {
        double dist = Math.abs(shooter.x - target.x);
        double perfectPower = Math.sqrt(dist * 0.5) * 4.472;
        currentProjectile.launch(shooter.x + (shooter instanceof CatPlayer ? 100 : -10), shooter.y, angle, perfectPower + (Math.random()*10 - 5));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentState == GameState.MENU) drawMenu(g);
        else if (currentState == GameState.CHOOSE_CHAR) drawChooseChar(g);
        else {
            drawGame(g);
            if (currentState == GameState.GAMEOVER) drawGameOver(g);
        }
    }

    private void drawGame(Graphics g) {
        int w = getWidth(), h = getHeight();
        Image currentBG = (timeOfDay == 0) ? bgMorning : (timeOfDay == 1) ? bgEvening : bgNight;
        if (currentBG != null) g.drawImage(currentBG, 0, 0, w, h, this);

        // Responsive Position
        cat.y = h - 160;
        dog.y = h - 160;
        dog.x = w - 180;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        String curName = playerTurn ? cat.name : dog.name;
        String turnStatus = curName + " TURN";

        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(w/2 - 150, 25, 300, 45, 15, 15);
        g2.setFont(new Font("Tahoma", Font.BOLD, 22));
        g2.setColor(playerTurn ? Color.CYAN : Color.ORANGE);
        int tw = g2.getFontMetrics().stringWidth(turnStatus);
        g2.drawString(turnStatus, w/2 - tw/2, 56);

        if (isCharging) {
            g2.setColor(Color.ORANGE);
            g2.fillRect(w/2 - 100, 85, (int)(currentPower * 2), 18);
            g2.setColor(Color.WHITE);
            g2.drawRect(w/2 - 100, 85, 200, 18);
        }

        cat.draw(g);
        dog.draw(g);
        currentProjectile.draw(g);
    }

    private void drawMenu(Graphics g) {
        if (bgMenu != null) g.drawImage(bgMenu, 0, 0, getWidth(), getHeight(), this);
        g.setColor(new Color(0,0,0,160)); g.fillRect(0,0,getWidth(),getHeight());
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.YELLOW); g2.setFont(new Font("Arial", Font.BOLD, 70));
        g2.drawString("CAT VS DOG", getWidth()/2 - 210, getHeight()/2 - 120);
        drawBtn(g2, getWidth()/2-125, getHeight()/2-80, 250, 80, "SINGLE PLAYER", new Color(50, 205, 50));
        drawBtn(g2, getWidth()/2-125, getHeight()/2+20, 250, 80, "2 PLAYERS", new Color(30, 144, 255));
        drawBtn(g2, getWidth()/2-125, getHeight()/2+120, 250, 80, "EXIT", new Color(220, 20, 60));
    }

    private void drawBtn(Graphics2D g2, int x, int y, int w, int h, String txt, Color c) {
        g2.setColor(c); g2.fillRoundRect(x, y, w, h, 20, 20);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.drawString(txt, x + w/2 - g2.getFontMetrics().stringWidth(txt)/2, y + h/2 + 8);
    }

    private void drawChooseChar(Graphics g) {
        g.setColor(new Color(30, 30, 30)); g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.YELLOW); g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("SELECT YOUR SIDE", getWidth()/2 - 185, 120);
        g.drawImage(new ImageIcon("cat.png").getImage(), getWidth()/2 - 200, getHeight()/2 - 60, 140, 140, null);
        g.drawImage(new ImageIcon("dog.png").getImage(), getWidth()/2 + 60, getHeight()/2 - 60, 140, 140, null);
    }

    private void drawGameOver(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200)); g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.YELLOW); g.setFont(new Font("Arial", Font.BOLD, 60));
        g.drawString(winnerText, getWidth()/2 - g.getFontMetrics().stringWidth(winnerText)/2, getHeight()/2 - 50);
        drawBtn((Graphics2D)g, getWidth()/2-125, getHeight()/2+30, 250, 60, "TRY AGAIN", new Color(50, 205, 50));
        drawBtn((Graphics2D)g, getWidth()/2-125, getHeight()/2+110, 250, 60, "MAIN MENU", new Color(150, 150, 150));
    }

    private void askNameSingle() {
        String in = JOptionPane.showInputDialog(this, "Enter Your Name:", "Player Name", JOptionPane.PLAIN_MESSAGE);
        if (isPlayerCat) { p1Name = (in != null && !in.trim().isEmpty()) ? in.trim().toUpperCase() : "CAT (YOU)"; p2Name = "AI DOG"; }
        else { p2Name = (in != null && !in.trim().isEmpty()) ? in.trim().toUpperCase() : "DOG (YOU)"; p1Name = "AI CAT"; }
    }

    private void askNamesPVP() {
        String n1 = JOptionPane.showInputDialog(this, "Player 1 (CAT) Name:", "PVP Mode", JOptionPane.PLAIN_MESSAGE);
        p1Name = (n1 != null && !n1.trim().isEmpty()) ? n1.trim().toUpperCase() : "PLAYER 1";
        String n2 = JOptionPane.showInputDialog(this, "Player 2 (DOG) Name:", "PVP Mode", JOptionPane.PLAIN_MESSAGE);
        p2Name = (n2 != null && !n2.trim().isEmpty()) ? n2.trim().toUpperCase() : "PLAYER 2";
    }

    private void playSound(String f) { try { File s = new File(f); if (s.exists()) { Clip c = AudioSystem.getClip(); c.open(AudioSystem.getAudioInputStream(s)); c.start(); } } catch (Exception ex) {} }
    private void playBGM(String f) { try { File s = new File(f); if (s.exists()) { bgmClip = AudioSystem.getClip(); bgmClip.open(AudioSystem.getAudioInputStream(s)); bgmClip.loop(Clip.LOOP_CONTINUOUSLY); bgmClip.start(); } } catch (Exception ex) {} }

    public static void main(String[] args) {
        JFrame f = new JFrame("Cat vs Dog - OOP");
        f.add(new CatVsDogGame());
        f.setSize(1024, 768);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}