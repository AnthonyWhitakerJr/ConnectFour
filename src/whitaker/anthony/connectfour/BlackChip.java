package whitaker.anthony.connectfour;

	import java.awt.*;
	import java.awt.geom.*;

/**
 * @author Anthony R Whitaker
 */
public class BlackChip extends Chip
{
	private Ellipse2D chip;

	public BlackChip(Rectangle rect)
	{
		super(rect, Color.BLACK);
	}

}
