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
	private final int cellHeight;
	private final int cellWidth;
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
	private boolean isFocused, isGameOver;
	private Thread dropPieceThread;
	private GamePiece activePiece;
	private int redScore;
	private int turn;

	Board(int row, int column, Dimension d, Applet a, Boolean focus) {
		applet = a;

		cellWidth = (int)d.getWidth() / (column + 2);
		cellHeight = (int)d.getHeight() / (row + 2);

		isGameOver = false;

		thick = new BasicStroke(7);
		thin = new BasicStroke(2);

		font = new Font("Serif", Font.BOLD, 34);

		columns = column;
		rows = row;

		turn = 0;

		rect = new Rectangle(cellWidth, 0, cellWidth, cellHeight);
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
			activePiece = new RedChip(rect);
		else //Make a black game piece
			activePiece = new BlackChip(rect);
	}

	private void reset(String message) {
		JOptionPane.showMessageDialog(null, message);
		turn = 0;
		clearBoard();
		applet.repaint();
		createNewPiece();
		isGameOver = false;
	}

	private void clearBoard() {
		for(int r = 0; r < rows; ++r)
			for(int c = 0; c < columns; ++c)
				board[r][c] = null;
	}

	public void run() {
		dropPiece();
		createNewPiece();
		applet.repaint();

	}

	private void dropPiece() {
		int row = rows;
		while(row > finalRow) {
			activePiece.move(cellHeight);
			--row;
			applet.repaint();
			try {
				Thread.sleep(100);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		dropPieceThread = null;
	}


	// Determine winner

	private void Winner(int r, int c) {
		if(turn >= 7 && checkForWin(r, c)) {
			isGameOver = true;
			incrementScore(board[r][c]);

			if(board[r][c] instanceof RedChip)
				reset("Red player Wins");
			else if(board[r][c] instanceof BlackChip)
				reset("Black player Wins");
		}
		else if(turn == columns * rows) {
			isGameOver = true;
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

	// Draw Board
	void draw(Graphics2D g2) {
		if(isFocused || isGameOver) {
			//fill yellow rectangle
			g2.setColor(Color.YELLOW);
			g2.fillRect(cellWidth, cellHeight, columns * cellWidth, rows * cellHeight);

			//draw black border lines
			g2.setStroke(thick);
			g2.setColor(Color.BLACK);
			g2.drawRect(cellWidth, cellHeight, columns * cellWidth, rows * cellHeight);
			g2.setStroke(thin);

			//draw circles
			//circles = 3/4 size of square

			for(int y = 0; y < rows; y++) {
				for(int x = 0; x < columns; x++) {
					g2.setColor(Color.BLUE);
					g2.fillOval(x * cellWidth + cellWidth * 9 / 8, y * cellHeight + cellHeight * 9 / 8, cellWidth * 3 / 4, cellHeight * 3 / 4);
				}
			}

			//draw horizontal lines
			g2.setColor(Color.BLACK);
			for(int r = 2; r < (columns + 1); r++) {
				g2.drawLine(cellWidth, cellHeight * r, (columns + 1) * cellWidth, cellHeight * r);
			}

			//draw vertical lines
			g2.setColor(Color.BLACK);
			for(int r = 2; r < (columns + 1); r++) {
				g2.drawLine(cellWidth * r, cellHeight, cellWidth * r, (rows + 1) * cellHeight);
			}
			//draw chips
			for(int r = 0; r < rows; r++)//rows on matrix
				for(int c = 0; c < columns; c++)//columns on matrix
					if(board[r][c] != null)//doesn't draw on startup
						board[r][c].draw(g2);//draws chips

			//draw movable chip
			activePiece.draw(g2);


			//Score
			g2.setFont(font);
			g2.setColor(Color.RED);
			g2.drawString("Red", cellWidth / 5, cellHeight - cellHeight / 2);
			g2.drawString(String.valueOf(redScore), cellWidth / 3, cellHeight);
			g2.setColor(Color.BLACK);
			g2.drawString("Black", applet.getWidth() - cellWidth, cellHeight - cellHeight / 2);
			g2.drawString(String.valueOf(blackScore), applet.getWidth() - cellWidth + cellWidth / 3, cellHeight);
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
		if(dropPieceThread == null) {
			int col = event.getX() / cellWidth;
			activePiece.setX(col * cellWidth + cellWidth / 8);
			applet.repaint();
		}
	}

	public void mousePressed(MouseEvent event) {
		if(isFocused) {
			if(dropPieceThread == null) {
				int col = event.getX() / cellWidth - 1;

				if(col <= -1 || col >= columns)
					JOptionPane.showMessageDialog(null, "Click on the board!");
				else {
					int row = findEmptyRow(col);
					finalRow = row;
					if(row < 0)
						JOptionPane.showMessageDialog(null, "That row is full! Choose another.");

					if(row >= 0) {
						board[row][col] = activePiece;
						++turn;
						applet.repaint();
						dropPieceThread = new Thread(this);
						dropPieceThread.start();
						Winner(row, col);
						rect.setLocation((col + 1) * cellWidth, 0);//where chip is placed
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

}