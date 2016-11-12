package whitaker.anthony.connectfour;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Anthony R Whitaker
 */
class Board implements MouseListener, FocusListener, Runnable, MouseMotionListener {
	private final int RHeight;
	private final int RWidth;
	private final Applet applet;
	private final GamePiece[][] board;
	private final int columns;
	private final Font font;
	private final Rectangle rect;
	private final int rows;
	private final BasicStroke thick;
	private final BasicStroke thin;
	private int blackScore;
	private Clip clip;
	private int finalRow;
	private Image splashScreen;
	private boolean isFocused, done;
	private Thread loop;
	private GamePiece piece;
	private int redScore;
	private int turn;

	Board(int row, int column, Dimension d, Applet a, Boolean focus) {
		applet = a;

		RWidth = (int)d.getWidth() / (column + 2);
		RHeight = (int)d.getHeight() / (row + 2);

		done = false;

		thick = new BasicStroke(7);
		thin = new BasicStroke(2);

		font = new Font("Serif", Font.BOLD, 34);

		columns = column;
		rows = row;

		turn = 0;

		rect = new Rectangle(RWidth, 0, RWidth, RHeight);
		createNewPiece();

		board = new GamePiece[rows][columns];
		isFocused = focus;

		try {
			splashScreen = ImageIO.read(Board.class.getResourceAsStream("/C4Bgrd.jpg"));
		} catch(IOException e) {
			e.printStackTrace();
		}

		music();

	}

	private void Winner(int r, int c) {
		if(turn >= 7 && checkForWin(r, c)) {
			done = true;
			incrementScore(board[r][c]);

			if(board[r][c] instanceof RedChip)
				reset("Red chip Wins");
			else if(board[r][c] instanceof BlackChip)
				reset("Black chip Wins");
		}
		else if(turn == columns * rows) {
			done = true;
			reset("Tie Game!");
		}
	}

	private boolean checkForWin(int r, int c) {
		Class p = board[r][c].getClass();

		return checkForWin_Row(r, p)
				|| checkForWin_Column(r, c, p)
				|| checkForWin_DiagonalLeftToRight(r, c, p)
				|| checkForWin_DiagonalRightToLeft(r, c, p);
	}

	private boolean checkForWin_Column(int row, int col, Class pieceClass) {
		int count = 0;

		while(count < 4 && row >= 0)    //stop when four in a row found
		{
			if(checkSpot(row, col, pieceClass))  //check each spot in column
				++count;      //add one to counter when match is found
			else
				count = 0;      //start over if pattern interrupted
			--row;          //go down to next row

			if(count >= 4)
				return true;
		}

		return false;
	}

	private boolean checkForWin_DiagonalLeftToRight(int r, int c, Class pieceClass) {
		int a = Math.min(rows - 1 - r, c);
		int row = r + a;
		int col = c - a;
		int count = 0;

		while(count < 4 && row >= 0 && col < columns) {
			if(checkSpot(row, col, pieceClass))
				++count;
			else
				count = 0;

			if(count >= 4)
				return true;

			--row;
			++col;
		}

		return false;
	}

	private boolean checkForWin_DiagonalRightToLeft(int r, int c, Class pieceClass) {
		int a = Math.min(rows - 1 - r, columns - 1 - c);
		int row = r + a;
		int col = c + a;
		int count = 0;

		while(count < 4 && row >= 0 && col >= 0) {
			if(checkSpot(row, col, pieceClass))
				++count;
			else
				count = 0;

			if(count >= 4)
				return true;

			--row;
			--col;
		}

		return false;
	}

	private boolean checkForWin_Row(int row, Class pieceClass) {
		int col = 0;
		int count = 0;

		while(count < 4 && col < columns)//stop when four in a row found
		{
			if(checkSpot(row, col, pieceClass))  //check each spot in row
				++count;      //add one to counter when match is found
			else
				count = 0;      //start over if pattern interrupted

			++col;          //go to next column

			if(count >= 4)
				return true;
		}

		return false;
	}

	private boolean checkSpot(int row, int col, Class pieceClass) {
		return board[row][col] != null && board[row][col].getClass().equals(pieceClass);
	}

	private void incrementScore(GamePiece c) {
		if(c instanceof RedChip)
			++redScore;
		else if(c instanceof BlackChip)
			++blackScore;
	}


	void draw(Graphics2D g2) {
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
						board[r][c].draw(g2);//draws chips

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

			g2.draw(rect);
		}

		else {
			g2.drawImage(splashScreen, 0, 0, applet.getWidth(), applet.getHeight(), null);
		}
	}


	// FocusListener

	public void focusGained(FocusEvent f) {
		isFocused = true;
		applet.repaint();
	}

	public void focusLost(FocusEvent f) {
		isFocused = false;
		applet.repaint();
	}


	// MouseListener

	public void mouseClicked(MouseEvent event) {}

	public void mouseDragged(MouseEvent event) {}

	public void mouseEntered(MouseEvent event) {}

	public void mouseExited(MouseEvent event) {}

	public void mouseMoved(MouseEvent event) {
		if(loop == null) {
			int col = event.getX() / RWidth;
			piece.setX(col * RWidth + RWidth / 8);
			applet.repaint();
		}
	}

	public void mousePressed(MouseEvent event) {
		if(isFocused) {
			if(loop == null) {
				int col = event.getX() / RWidth - 1;

				if(col <= -1 || col >= columns)
					JOptionPane.showMessageDialog(null, "Click on the board!");
				else {
					int row = findEmptyRow(col);
					finalRow = row;
					if(row < 0)
						JOptionPane.showMessageDialog(null, "That row is full! Choose another.");

					if(row >= 0) {
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

	private int findEmptyRow(int c) {
		int r = 0;
		while(r < rows && board[r][c] != null)
			r++;

		if(r >= rows)
			return -1;
		else
			return r;
	}


	private void music() {
		String tune = null;
		Object[] options = {"Jazz", "Salt", "Silence"};
		JOptionPane pane = new JOptionPane("Choose your ambience", JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options, options[0]);

		JDialog dialog = pane.createDialog(null, "Music");
		dialog.setVisible(true);
		Object selectedValue = pane.getValue();
		if(selectedValue != null && !Objects.equals(selectedValue.toString(), "Silence"))
			for(int counter = 0, maxCounter = options.length - 1; counter < maxCounter; counter++)
				if(options[counter].equals(selectedValue))
					tune = options[counter].toString() + ".wav";


		if(selectedValue != null && !Objects.equals(selectedValue.toString(), "Silence")) {
			try {
				AudioInputStream source = AudioSystem.getAudioInputStream(Board.class.getResourceAsStream("/" + tune));
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

	private void createNewPiece() {
		if(turn % 2 == 0)//Make a red game piece
			piece = new RedChip(rect);
		else //Make a black game piece
			piece = new BlackChip(rect);
	}

	private void reset(String message) {
		JOptionPane.showMessageDialog(null, message);
		turn = 0;
		clearBoard();
		applet.repaint();
		createNewPiece();
		done = false;
	}

	private void clearBoard() {
		for(int r = 0; r < rows; ++r)
			for(int c = 0; c < columns; ++c)
				board[r][c] = null;
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
		createNewPiece();
		applet.repaint();

	}

}