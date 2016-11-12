package whitaker.anthony.connectfour;
import java.awt.*;

/**
 * @author Anthony R Whitaker
 */
public interface GamePiece
{
	void draw(Graphics2D g2);
	void move(int deltaY);
	void setX(int x);
	int getX();
}
