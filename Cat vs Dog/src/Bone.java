import java.awt.*;

public class Bone extends Projectile {
    @Override
    public void draw(Graphics g) {
        if (active) {
            g.setColor(Color.WHITE); g.fillOval((int)x, (int)y, 16, 16);
            g.setColor(Color.BLACK); g.drawOval((int)x, (int)y, 16, 16);
        }
    }
}