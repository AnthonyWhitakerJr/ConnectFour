package whitaker.anthony.connectfour;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public abstract class Chip implements GamePiece {

	private Ellipse2D chip;
	private Color color;

	public Chip(Rectangle rect, Color color) {
		this.color = color;
		chip = new Ellipse2D.Double(rect.getX() + rect.getWidth() * 1 / 8, rect.getY() + rect.getWidth() * 1 / 8, rect.getWidth() * 3 / 4, rect.getHeight() * 3 / 4);
	}

	public void draw(Graphics2D g2) {
		g2.setColor(color);
		g2.fill(chip);
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
