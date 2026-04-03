import java.awt.*;

public abstract class Projectile implements Movable {
    protected double x, y, vx, vy;
    protected boolean active = false;

    public abstract void draw(Graphics g);

    public void launch(int startX, int startY, double angle, double power) {
        this.x = startX; this.y = startY;
        double rad = Math.toRadians(angle);
        this.vx = Math.cos(rad) * (power / 5);
        this.vy = -Math.sin(rad) * (power / 5);
        this.active = true;
    }

    @Override
    public void move() {
        if (active) {
            x += vx; vy += 0.5; y += vy;
            if (y > 1000 || x < -200 || x > 2500) active = false;
        }
    }
}