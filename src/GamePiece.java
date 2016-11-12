//Anthony R Whitaker
import java.awt.*;

public interface GamePiece
{
	void draw(Graphics2D g2);
	void move(int deltaY);
	void setX(int x);
	int getX();
}
