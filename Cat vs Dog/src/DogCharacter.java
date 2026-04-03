import java.awt.*;

public class DogCharacter extends GameCharacter {
    public DogCharacter(int x, int y, String name) {
        super(x, y, "dog.png", name);
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(img, x, y, 100, 100, null);
        g.setColor(Color.RED); g.fillRect(x, y-15, 100, 6);
        g.setColor(Color.GREEN); g.fillRect(x, y-15, hp, 6);
        g.setColor(Color.WHITE); g.setFont(new Font("Tahoma", Font.BOLD, 14));
        g.drawString(name, x + 50 - g.getFontMetrics().stringWidth(name)/2, y - 25);
    }
}