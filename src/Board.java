//Anthony R Whitaker

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class Board implements MouseListener, FocusListener, Runnable, MouseMotionListener, KeyListener {
	private int RWidth, RHeight, columns, rows, turn, redScore, blackScore, finalRow, mouseX;
	private Applet applet;
	private GamePiece[][] board;
	private Clip clip;
	private Font font, smallFont, largeFont;
	private Image image;
	private boolean isFocused, done;
	private KeyTracker keyTracker;
	private Thread loop;
	private GamePiece piece, nextPiece;
	private Rectangle rect;
	private BasicStroke thick, thin;

	public Board(int row, int column, Dimension d, Applet a, Boolean focus) {
		applet = a;

		RWidth = (int)d.getWidth() / (column + 2);
		RHeight = (int)d.getHeight() / (row + 2);

		done = false;

		thick = new BasicStroke(7);
		thin = new BasicStroke(2);

		smallFont = new Font("Serif", Font.BOLD, 28);
		font = new Font("Serif", Font.BOLD, 34);
		largeFont = new Font("Serif", Font.BOLD, 68);

		columns = column;
		rows = row;

		turn = 0;

		rect = new Rectangle(RWidth, 0, RWidth, RHeight);
		newPiece();

		board = new GamePiece[rows][columns];
		isFocused = focus;

		keyTracker = new KeyTracker();


		try {
			image = ImageIO.read(new File("C4Bgrd.jpg"));
		} catch(IOException e) {
			e.printStackTrace();

		}

		music();

	}		
/*	public void check()
	{
		Dimension d=applet.getSize();
		RWidth=(int)d.getWidth()/(columns+2);
		RHeight=(int)d.getHeight()/(rows+2);
		rect=new Rectangle(RWidth,0,RWidth,RHeight);

	}
*/

	public void Winner(int r, int c) {
		if(turn >= 7 && checkForWin(r, c)) {
			done = true;
			score(board[r][c].getClass().getName());
//			reset(board[r][c].getClass().getName()+" Wins");
			if(board[r][c].getClass().getName().equals("RedChip")) { reset("Red chip Wins"); }
			else if(board[r][c].getClass().getName().equals("BlackChip")) { reset("Black chip Wins"); }
		}
		else if(turn == columns * rows) {
			done = true;
			reset("Tie Game!");
		}
	}

	public boolean checkForWin(int r, int c) {
		Class p = board[r][c].getClass();
		int row = r;
		int col = 0;
		int count = 0;

		//Row
		while(count < 4 && col < columns)//stop when four in a row found
		{
			if(checkSpot(r, col, p))  //check each spot in column
			{
				++count;      //add one to counter when match is found
			}
			else {
				count = 0;      //start over if pattern interrupted
			}
			++col;          //go to next column

			if(count >= 4) { return true; }
		}
		row = r;
		col = c;
		count = 0;

		//Column
		while(count < 4 && row >= 0)    //stop when four in a row found
		{
			if(checkSpot(row, c, p))  //check each spot in column
			{
				++count;      //add one to counter when match is found
			}
			else {
				count = 0;      //start over if pattern interrupted
			}
			--row;          //go down to next row


			if(count >= 4) { return true; }
		}

		//Diag left-->right
		int a = Math.min(rows - 1 - r, c);
		row = r + a;
		col = c - a;
		count = 0;
		while(count < 4 && row >= 0 && col < columns) {
			if(checkSpot(row, col, p)) { ++count; }
			else { count = 0; }
			if(count >= 4) { return true; }
			--row;
			++col;
		}

		//Diag right-->left
		a = Math.min(rows - 1 - r, columns - 1 - c);
		row = r + a;
		col = c + a;
		count = 0;
		while(count < 4 && row >= 0 && col >= 0) {
			if(checkSpot(row, col, p)) { ++count; }
			else { count = 0; }
			if(count >= 4) { return true; }
			--row;
			--col;
		}

		return false;
	}

	public boolean checkSpot(int r, int c, Class p) {
		return board[r][c] != null && board[r][c].getClass().equals(p);
	}

	public void draw(Graphics2D g2) {
		if(isFocused || done) {
			//fill yellow rectangle
			g2.setColor(Color.YELLOW);
			g2.fillRect(RWidth, RHeight, columns * RWidth, rows * RHeight);

			//draw black border lines
			g2.setStroke(thick);
			g2.setColor(Color.BLACK);
			g2.drawRect(RWidth, RHeight, columns * RWidth, rows * RHeight);
			g2.setStroke(thin);

			//draw circles
			//circles = 3/4 size of square

			for(int y = 0; y < rows; y++) {
				for(int x = 0; x < columns; x++) {
					g2.setColor(Color.BLUE);
					g2.fillOval(x * RWidth + RWidth * 9 / 8, y * RHeight + RHeight * 9 / 8, RWidth * 3 / 4, RHeight * 3 / 4);
				}
			}

			//draw horizontal lines
			g2.setColor(Color.BLACK);
			for(int r = 2; r < (columns + 1); r++) {
				g2.drawLine(RWidth, RHeight * r, (columns + 1) * RWidth, RHeight * r);
			}

			//draw vertical lines
			g2.setColor(Color.BLACK);
			for(int r = 2; r < (columns + 1); r++) {
				g2.drawLine(RWidth * r, RHeight, RWidth * r, (rows + 1) * RHeight);
			}
			//draw chips
			for(int r = 0; r < rows; r++)//rows on matrix
				for(int c = 0; c < columns; c++)//columns on matrix
					if(board[r][c] != null)//doesn't draw on startup
					{
						board[r][c].draw(g2);//draws chips
					}

			//draw movable chip
			piece.draw(g2);


			//Score
			g2.setFont(font);
			g2.setColor(Color.RED);
			g2.drawString("Red", RWidth / 5, RHeight - RHeight / 2);
			g2.drawString(String.valueOf(redScore), RWidth / 3, RHeight);
			g2.setColor(Color.BLACK);
			g2.drawString("Black", applet.getWidth() - RWidth, RHeight - RHeight / 2);
			g2.drawString(String.valueOf(blackScore), applet.getWidth() - RWidth + RWidth / 3, RHeight);

			//check dimensions
//			check();

			g2.draw(rect);
		}

		else {
			g2.drawImage(image, 0, 0, applet.getWidth(), applet.getHeight(), null);
/*			g2.setFont(largeFont);
			g2.setColor(Color.GREEN);
//			g2.fillRect(0,0,applet.getWidth(),applet.getHeight());
//			g2.setColor(Color.BLUE);
			g2.drawString("CLICK ME!!!!",100,100);
*/
		}
	}

	public int findEmptyRow(int c) {
		int r = 0;
		while(r < rows && board[r][c] != null)
			r++;
		if(r >= rows) { return -1; }
		else { return r; }
	}

	public void focusGained(FocusEvent f) {
		isFocused = true;
		applet.repaint();
	}

	public void focusLost(FocusEvent f) {
		isFocused = false;
		applet.repaint();
	}

	public void handleEvent(AWTEvent e) {
		if(e.getID() == KeyEvent.KEY_PRESSED) { keyTracker.handleKeyPressed((KeyEvent)e); }
		if(e.getID() == KeyEvent.KEY_RELEASED) { keyTracker.handleKeyReleased((KeyEvent)e); }

	}

	public void keyPressed(KeyEvent event) {
		keyTracker.handleKeyPressed((KeyEvent)event);
		movePiece();
	}

	public void keyReleased(KeyEvent event) {
		keyTracker.handleKeyReleased((KeyEvent)event);
	}

	public void keyTyped(KeyEvent event) {}

	public void mouseClicked(MouseEvent event) {}

	public void mouseDragged(MouseEvent event) {}

	public void mouseEntered(MouseEvent event) {}

	public void mouseExited(MouseEvent event) {}

	public void mouseMoved(MouseEvent event) {
		if(loop == null) {
			int col = event.getX() / RWidth;
			piece.setX(col * RWidth + RWidth / 8);
//			rect.setLocation((col+1)*RWidth,0);//where chip is placed
			applet.repaint();
		}
/*		else
		{	int col=event.getX()/RWidth;
			nextpiece.setX(col*RWidth+RWidth/8);
		}
*/
	}

	public void mousePressed(MouseEvent event) {
		if(isFocused) {
			if(loop == null) {
				int col = event.getX() / RWidth - 1;

				if(col <= -1 || col >= columns) { JOptionPane.showMessageDialog(null, "Click on the board!"); }
				else {
					int row = findEmptyRow(col);
					finalRow = row;
					if(row < 0) { JOptionPane.showMessageDialog(null, "That row is full! Choose another."); }

					if(row >= 0) {
//						rect.setLocation((col+1)*RWidth,0);//where chip is placed
/*						if (turn%2==0)//Make a black game piece
							piece=new BlackChip(rect);
						if (turn%2==1)//Make a red game piece
							piece=new RedChip(rect);
*/
						board[row][col] = piece;
						++turn;
						applet.repaint();
						loop = new Thread(this);
						loop.start();
						Winner(row, col);
						rect.setLocation((col + 1) * RWidth, 0);//where chip is placed
					}

				}
			}
		}
	}

	public void mouseReleased(MouseEvent event) {}

	public void movePiece() {
		if(loop == null) {
			if(keyTracker.isPressed(KeyEvent.VK_LEFT)) {
				piece.setX(piece.getX() - RWidth);
				//			applet.repaint();
				System.out.println("Pressed");
			}

			else if(keyTracker.isPressed(KeyEvent.VK_RIGHT)) {
				piece.setX(piece.getX() + RWidth);
				//			applet.repaint();
			}
		}
	}

	public void music() {
		String tune = null;
		Object[] options = {"Jazz", "Salt", "Silence"};
		JOptionPane pane = new JOptionPane("Choose your ambience", JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options, options[0]);

     	/*pane.set.showOptionDialog(null, "Choose your ambience","Music",
		JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
		null, options, options[0]); // Configure*/

		JDialog dialog = pane.createDialog(null, "Music");
		dialog.show();
		Object selectedValue = pane.getValue();
		if(selectedValue != null && selectedValue.toString() != "Silence") {
			for(int counter = 0, maxCounter = options.length - 1; counter < maxCounter; counter++)
				if(options[counter].equals(selectedValue)) { tune = options[counter].toString() + ".wav"; }
		}


		if(selectedValue != null && selectedValue.toString() != "Silence") {
			File soundFile = new File(tune);
			try {
				AudioInputStream source = AudioSystem.getAudioInputStream(soundFile);
				DataLine.Info clipInfo = new DataLine.Info(Clip.class, source.getFormat());

				if(AudioSystem.isLineSupported(clipInfo)) {
					clip = (Clip)AudioSystem.getLine(clipInfo);
					clip.open(source);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}

			clip.setFramePosition(0);
			clip.loop(-1);
		}
	}

	public void newPiece() {
		if(turn % 2 == 0)//Make a black game piece
		{ piece = new RedChip(rect); }
		if(turn % 2 == 1)//Make a red game piece
		{ piece = new BlackChip(rect); }
	}

	public void reset(String message) {
		JOptionPane.showMessageDialog(null, message);
		turn = 0;
		for(int r = 0; r < rows; ++r)//Clear board
			for(int c = 0; c < columns; ++c)
				board[r][c] = null;
		applet.repaint();
		newPiece();
		done = false;
	}

	public void run() {
		int row = rows;
		while(row > finalRow) {
			piece.move(RHeight);
			--row;
			applet.repaint();
			try {
				Thread.sleep(100);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		loop = null;
//		rect.setLocation((int)rect.getX(),0);
		newPiece();
		applet.repaint();

	}

	public void score(String c) {
		if(c.equals("RedChip")) { ++redScore; }
		if(c.equals("BlackChip")) { ++blackScore; }
	}

}