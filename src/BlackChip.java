//Anthony R Whitaker

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class BlackChip implements GamePiece {
	private Ellipse2D chip;
	private Rectangle r;
	private int width, height;

	public BlackChip(Rectangle rect) {
		r = rect;
		chip = new Ellipse2D.Double(r.getX() + r.getWidth() * 1 / 8, r.getY() + r.getWidth() * 1 / 8, r.getWidth() * 3 / 4, r.getHeight() * 3 / 4);
		width = (int)r.getWidth();
		height = (int)r.getHeight();
	}

	public void draw(Graphics2D g2) {
		g2.setColor(Color.BLACK);
		g2.fill(chip);
	}

	public int getMaxX() {
		return (int)chip.getMaxX();
	}

	public int getX() {
		return (int)chip.getX();
	}

	public void setX(int x) {
		chip.setFrame(x, chip.getY(), chip.getWidth(), chip.getHeight());
	}

	public void move(int deltaY) {
		chip.setFrame(chip.getX(), chip.getY() + deltaY, chip.getWidth(), chip.getHeight());
	}

}
