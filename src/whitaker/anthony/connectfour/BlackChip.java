package whitaker.anthony.connectfour;

	import java.awt.*;
	import java.awt.geom.*;

/**
 * @author Anthony R Whitaker
 */
public class BlackChip implements GamePiece
{
	private Ellipse2D chip;

	public BlackChip(Rectangle rect)
	{
		chip=new Ellipse2D.Double(rect.getX()+ rect.getWidth()*1/8, rect.getY()+ rect.getWidth()*1/8, rect.getWidth()*3/4, rect.getHeight()*3/4);
	}	
	
	public void draw(Graphics2D g2)
	{
		g2.setColor(Color.BLACK);
		g2.fill(chip);
	}
	public void move(int deltaY)
	{
		chip.setFrame(chip.getX(),chip.getY()+deltaY,chip.getWidth(),chip.getHeight());
	}
	public void setX(int x)
	{
		chip.setFrame(x,chip.getY(),chip.getWidth(),chip.getHeight());
	}

	public int getX()
	{
		return (int)chip.getX();
	}

}
