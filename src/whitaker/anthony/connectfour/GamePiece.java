package whitaker.anthony.connectfour;

import java.awt.*;

/**
 * @author Anthony R Whitaker
 */
public interface GamePiece {
	void draw(Graphics2D g2);

	int getX();

	void setX(int x);

	void move(int deltaY);
}
