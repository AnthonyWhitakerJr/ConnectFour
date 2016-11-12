//Anthony R Whitaker

import java.awt.*;

public interface GamePiece {
	void draw(Graphics2D g2);

	int getX();

	void setX(int x);

	void move(int deltaY);
}
