import javax.swing.*;
import java.awt.*;

public abstract class GameCharacter {
    protected int x, y, hp = 100;
    protected Image img;
    protected String name = "";

    public GameCharacter(int x, int y, String imgPath, String name) {
        this.x = x; this.y = y;
        this.img = new ImageIcon(imgPath).getImage();
        this.name = name;
    }

    public abstract void draw(Graphics g);

    public boolean isHit(double px, double py) {
        return px > x && px < x + 100 && py > y && py < y + 100;
    }
}