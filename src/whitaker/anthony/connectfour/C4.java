package whitaker.anthony.connectfour;
import java.applet.Applet;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Anthony R Whitaker
 */
public class C4 extends Applet 
{

	private static final int ROWS=6;
	private static final int COLUMNS=7;
	private BufferedImage backBuffer;
	private Board board;
	
	public void init() 
	{
		Boolean focused=isFocusOwner();
		board=new Board(ROWS, COLUMNS, getSize(), this,focused);
		backBuffer=new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);		
		addMouseListener(board);
		addFocusListener(board);
		addMouseMotionListener(board);
		addKeyListener(board);
	}

	public void paint(Graphics g) 
	{
		Graphics2D g2=(Graphics2D)backBuffer.getGraphics();
		
		//fill background
		g2.setColor(Color.BLUE);
		g2.fillRect(0,0,getWidth(),getHeight());
		
		board.draw(g2);
		g.drawImage(backBuffer,0,0,getWidth(),getHeight(),null);
		g2.dispose();

	}
	public void update(Graphics g)
	{
		paint(g);
	}

}
