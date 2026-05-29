

import java.awt.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;

public class Pente extends JFrame implements ActionListener, WindowListener {

	// MATRIX STATES
	final int EMPTY = 0;
	final int BLUE = 1;
	final int RED_TAN = -1;
	final int ERASED = 2;
	final int AIPOSSIBLEMOVE = 9;

	protected Image[] images;
	boolean gameOver = false;

	// Width of the board
	int WIDTH = 570;

	// Directions
	private final int UPPER = 0;
	private final int LOWER = 1;
	private final int RIGHT = 2;
	private final int LEFT = 3;
	private final int UPPERLEFT = 4;
	private final int UPPERRIGHT = 5;
	private final int LOWERRIGHT = 6;
	private final int LOWERLEFT = 7;
	int directions[] = { 0, 1, 2, 3, 4, 5, 6, 7 };
	int targetstone[] = new int[2];

	// Player or Computer Turn.
	public int turn;

	// count of captures
	public int paircount_blue;
	public int paircount_red_tan;

	// The board. 19 x 19 matrix
	int stone[][] = new int[19][19];
	Stack<AIMove> movesequence = new Stack<>();
	String move;
	int randomNum, x;
	TextArea outputarea; // text area
	final int fieldsize = 35; // text area width in chars
	boolean you_go_first;
	pentepanel mypentepanel;
	Button quitbutton;
	Button resetbutton;
	JButton debugbutton;
	Button backbutton;
	ImageIcon yesbug;
	ImageIcon nodebug;
	Panel ButtonPanel = new Panel();
	Random rand = new Random();
	AllAIMoves aimoves;
	boolean debug;

	// ----------------------------------------------------
	// COP3538 Data Structures for IT FSW AI OOP PENTE GAME
	// ----------------------------------------------------
	public Pente() {
		super("Dr. Webster's COP3538 FSW AI OOP J A V A   P E N T E  V 1.0 ");

		this.you_go_first = true;

		// IMAGE STUFF - RWW

		images = new Image[11];
		createImage("1.gif", 0);
		createImage("2.gif", 1);
		createImage("4.gif", 2);
		createImage("5.gif", 3);
		createImage("6.png", 4);
		createImage("bluelarge.png", 5);
		createImage("redtanlarge.png", 6);
		createImage("nodebug.png", 7);
		createImage("yesbug.png", 8);
		createImage("bluehd.png", 9);
		createImage("tanhd.png", 10);
		images[9] = images[9].getScaledInstance(28, 28, Image.SCALE_SMOOTH);
		images[10] = images[10].getScaledInstance(28, 28, Image.SCALE_SMOOTH);

		gameOver = false;
		paircount_blue = 0;
		paircount_red_tan = 0;

		for (int row = 0; row < 19; row++)
			for (int col = 0; col < 19; col++) {
				stone[row][col] = EMPTY;
			}

		aimoves = new AllAIMoves(stone, paircount_red_tan, paircount_blue);

		Image scaledyes = images[8].getScaledInstance(35, 35, Image.SCALE_SMOOTH);
		Image scaledno = images[7].getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		yesbug = new ImageIcon(scaledyes);
		nodebug = new ImageIcon(scaledno);
		debugbutton = new JButton(nodebug);
		debugbutton.setPreferredSize(new Dimension(35, 35));
		debugbutton.addActionListener(this);

		layoutStuff();
		mypentepanel.setVisible(true);
		mypentepanel.repaint();
	}

	public void layoutStuff() {
		int x, y = 1;

		outputarea = new TextArea(5, fieldsize);
		outputarea.setEditable(false);

		outputarea.setBackground(new Color(200, 200, 200));
		outputarea.setForeground(Color.BLACK);

		quitbutton = new Button("Quit");
		quitbutton.addActionListener(this);

		resetbutton = new Button("Reset");
		resetbutton.addActionListener(this);

		backbutton = new Button("Back");
		backbutton.addActionListener(this);

		ButtonPanel.add(quitbutton);
		ButtonPanel.add(resetbutton);
		ButtonPanel.add(backbutton);
		ButtonPanel.add(debugbutton);
		ButtonPanel.setBackground(Color.LIGHT_GRAY);
		mypentepanel = new pentepanel();
		this.add("North", outputarea);
		this.add("South", ButtonPanel);
		this.add("Center", mypentepanel);
		this.pack();
		this.setVisible(true);
		this.setSize(800, 800);
		this.addWindowListener(this);

		outputarea.append("Welcome to COP3538 FSW AI OOP Pente version 1.0 \n ------------------------------ \n ");
		outputarea.append("You are BLUE, your opponent the Computer is RED-TAN.\n");

		if (you_go_first) {
			turn = BLUE;
			outputarea.append("You move first. He who casts the first stone...!\n");
		} else {
			turn = RED_TAN;
			outputarea.append("The Computer has the first move.\n");
		}

	}

	public void Reset() {

		gameOver = false;
		paircount_blue = 0;
		paircount_red_tan = 0;
		turn = BLUE;
		for (int row = 0; row < 19; row++)
			for (int col = 0; col < 19; col++) {
				stone[row][col] = EMPTY;
			}
		aimoves.allaimoves.clear();
		movesequence.clear();
		mypentepanel.repaint();
		outputarea.setText("Welcome to FSW Pente version 1.0 \n ------------------------------ \n ");
		outputarea.append("You are BLUE, your opponent the Computer is RED-TAN.\n");
		outputarea.append("You move first. He who casts the first stone...!\n");
	}

	public void createImage(String filename, int index) {
		try {
			File mypic = new File(filename);
			if (!mypic.exists()) {
				JOptionPane.showMessageDialog(null, filename + " File Not Found");
				return;
			}
			// images[index] = ImageIO.read(getClass().getResource(filename));
			images[index] = ImageIO.read(mypic);
		} catch (IOException ex) {
			String msg = "The error is\n" + ex.toString();
			JOptionPane.showMessageDialog(null, msg);
		}
	}

	public void actionPerformed(ActionEvent event) {
		Component source = (Component) event.getSource();
		if (source == quitbutton) {
			this.dispose();
			System.exit(0);
		}
		if (source == resetbutton) {
			Reset();
		}
		if (source == debugbutton) {
			if (debug == true) {
				debug = false;
				debugbutton.setIcon(nodebug);

			} else {
				debug = true;
				debugbutton.setIcon(yesbug);
			}
			mypentepanel.repaint();
		}
		if (source == backbutton && !movesequence.isEmpty()) {

			stone[movesequence.peek().row][movesequence.peek().col] = EMPTY;
			if (!movesequence.peek().getCaptures().isEmpty()) {
				movesequence.peek().getCaptures()
						.forEach(capture -> stone[capture.row][capture.col] = movesequence.peek().turn);
				if (movesequence.peek().turn == RED_TAN) {
					paircount_blue--;
				} else {
					paircount_red_tan--;
				}
			}
			if (gameOver) {
				gameOver = false;
			} else {
				turn = -turn;
			}
			movesequence.pop();
			updateAIMoves();
			mypentepanel.repaint();
		}
	}

	public void windowActivated(WindowEvent e) {

	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		this.dispose();
		System.exit(0);
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
		mypentepanel.repaint();
	}

	// ----------------------------------------------------
	// checkSlot
	// ----------------------------------------------------
	public boolean checkSlot(int row, int column, int turn) {
		if (stone[row][column] == EMPTY || stone[row][column] == AIPOSSIBLEMOVE || stone[row][column] == ERASED) {
			stone[row][column] = turn;
			AIMove lastmove = new AIMove(row, column, 0);
			movesequence.add(lastmove);
			return true;
		}
		outputarea.append("You have made an illegal move.\n");
		outputarea.append("Please choose another move.\n");
		return false;
	}

	public boolean checkWin(int turn) {

		if (turn == BLUE && paircount_blue >= 5) {
			return true;
		}
		if (turn == RED_TAN && paircount_red_tan >= 5) {
			return true;
		}

		for (int r = 0; r < 18; r++) {
			for (int c = 0; c < 18; c++) {
				if (stone[r][c] == turn) {
					// Cardinals
					if (isInBounds(r - 4, c) && stone[r - 1][c] == turn && stone[r - 2][c] == turn
							&& stone[r - 3][c] == turn && stone[r - 4][c] == turn) {
						return true;
					}
					if (isInBounds(r + 4, c) && stone[r + 1][c] == turn && stone[r + 2][c] == turn
							&& stone[r + 3][c] == turn && stone[r + 4][c] == turn) {
						return true;
					}
					if (isInBounds(r, c - 4) && stone[r][c - 1] == turn && stone[r][c - 2] == turn
							&& stone[r][c - 3] == turn && stone[r][c - 4] == turn) {
						return true;
					}
					if (isInBounds(r, c + 4) && stone[r][c + 1] == turn && stone[r][c + 2] == turn
							&& stone[r][c + 3] == turn && stone[r][c + 4] == turn) {
						return true;
					}

					// Diagonals
					if (isInBounds(r - 4, c - 4) && stone[r - 1][c - 1] == turn && stone[r - 2][c - 2] == turn
							&& stone[r - 3][c - 3] == turn && stone[r - 4][c - 4] == turn) {
						return true;
					}
					if (isInBounds(r - 4, c + 4) && stone[r - 1][c + 1] == turn && stone[r - 2][c + 2] == turn
							&& stone[r - 3][c + 3] == turn && stone[r - 4][c + 4] == turn) {
						return true;
					}
					if (isInBounds(r + 4, c - 4) && stone[r + 1][c - 1] == turn && stone[r + 2][c - 2] == turn
							&& stone[r + 3][c - 3] == turn && stone[r + 4][c - 4] == turn) {
						return true;
					}
					if (isInBounds(r + 4, c + 4) && stone[r + 1][c + 1] == turn && stone[r + 2][c + 2] == turn
							&& stone[r + 3][c + 3] == turn && stone[r + 4][c + 4] == turn) {
						return true;
					}
				}

			}
		}
		return false;
	}

	// ----------------------------------------------------
	// checkCapture
	// ----------------------------------------------------
	private int[] checkStone(int row, int col, int dir, int spaces) {

		switch (dir) {
		case UPPER:
			row = row - spaces;
			// code block
			break;
		case LOWER:
			row = row + spaces;
			// code block
			break;
		case RIGHT:
			col = col + spaces;
			// code block
			break;
		case LEFT:
			col = col - spaces;
			// code block
			break;
		case UPPERLEFT:
			row = row - spaces;
			col = col - spaces;
			// code block
			break;
		case UPPERRIGHT:
			row = row - spaces;
			col = col + spaces;
			// code block
			break;
		case LOWERRIGHT:
			row = row + spaces;
			col = col + spaces;
			// code block
			break;
		case LOWERLEFT:
			row = row + spaces;
			col = col - spaces;
			// code block
			break;
		default:
			// code block
		}

		if (isInBounds(row, col)) {
			// JOptionPane.showMessageDialog(null,
			// "checkStone()\nrow " + row + " col " + col+"\nStone state "+stone[row][col]);
			return new int[] { row, col };
		} else {
			// JOptionPane.showMessageDialog(null,
			// "checkStone()\nrow " + row + " col " + col+"\nout of bounds");
			return new int[] { -1, -1 };
		}

	}

	public void checkCapture(int row, int col, int turn) {
		for (int dir : directions) {
			int[] targetstone1 = checkStone(row, col, dir, 1);
			int[] targetstone2 = checkStone(row, col, dir, 2);
			int[] targetstone3 = checkStone(row, col, dir, 3);
			if (targetstone3[0] == -1) {
				continue;
			}
			int stone1 = stone[targetstone1[0]][targetstone1[1]];
			int stone2 = stone[targetstone2[0]][targetstone2[1]];
			int stone3 = stone[targetstone3[0]][targetstone3[1]];

			if (stone1 == -turn && stone2 == -turn && stone3 == turn) {
				stone[targetstone1[0]][targetstone1[1]] = EMPTY;
				stone[targetstone2[0]][targetstone2[1]] = EMPTY;
				changeCaptureCount(turn);
				AIMove move1 = new AIMove(targetstone1[0], targetstone1[1], 0);
				AIMove move2 = new AIMove(targetstone2[0], targetstone2[1], 0);
				movesequence.peek().setCaptures(move1, move2, -turn);
			}
		}
	}

	public boolean isInBounds(int row, int col) {
		if (row < 0 || row >= 19 || col < 0 || col >= 19) {
			return false;
		}
		return true;
	}

	public void changeCaptureCount(int color) {
		if (color == BLUE) {
			paircount_blue++;
		} else {
			paircount_red_tan++;
		}
	}

	public void updateAIMoves() {
		clearAIMoves();
		aimoves = new AllAIMoves(stone, paircount_red_tan, paircount_blue);
		for (AIMove move : aimoves.getAllAIMoves()) {
			if (stone[move.row][move.col] != RED_TAN && stone[move.row][move.col] != BLUE) {
				stone[move.row][move.col] = AIPOSSIBLEMOVE;
			}
		}
	}

	public void clearAIMoves() {
		for (AIMove move : aimoves.getAllAIMoves()) {
			if (stone[move.row][move.col] != RED_TAN && stone[move.row][move.col] != BLUE) {
				stone[move.row][move.col] = EMPTY;
			}
		}
	}

	public Boolean check_opponent_move(int row, int col) {

		String outputmsg = "";
		turn = RED_TAN;
		outputarea
				.append("check_opponent_move the Computer: oppponent move it is ROW: : " + row + " COL " + col + "\n");

		if (!checkSlot(row, col, RED_TAN)) {
			outputarea.append("Bad Computer move:  " + row + ", " + col + "\n");
			return false;// rww
		} else {
			outputarea
					.append("Your opponent's move row: " + row + " col:  " + col + " value: " + stone[row][col] + "\n");
		}

		mypentepanel.repaint();
		return true;
	}

	// --------------------------------------------------------
	// class pentepanel
	// --------------------------------------------------------
	class pentepanel extends JPanel implements MouseListener {
		int WIDTH = 570;// changed from 570 -PB
		String move;

		pentepanel() {
			super();

			this.addMouseListener(this);
			this.repaint();
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mouseClicked(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
			String outputmsg;
			Integer mycolumn;
			Integer myrow;
			int x;
			int y;
			x = e.getX();
			y = e.getY();

			int column = (int) (x / (WIDTH / 19));
			int row = (int) (y / (WIDTH / 19));
			int temp = paircount_red_tan;
			int temp2 = paircount_blue;

			if (turn == BLUE) { // player1 move
				if (!gameOver && checkSlot(row, column, turn)) {
					outputarea.append("Your move is:  " + row + ", " + column + "\n");
					checkCapture(row, column, turn);
					if (checkWin(turn)) {
						gameOver = true;
						mypentepanel.repaint();
						JOptionPane.showMessageDialog(null, "Blue wins!");
						return;
					}
					turn = RED_TAN;
					updateAIMoves();
					if (!debug) {
						AIMove themove = aimoves.getMove();
						checkSlot(themove.row, themove.col, turn);
						outputarea.append("AI move is:  " + themove.row + ", " + themove.col + "\n");
						checkCapture(themove.row, themove.col, turn);
						if (checkWin(turn)) {
							gameOver = true;
							mypentepanel.repaint();
							JOptionPane.showMessageDialog(null, "AI wins!");
							return;
						}
						turn = BLUE;
						updateAIMoves();
					}
				}
			} else { // player2 move
				if (column == 0) {
					AIMove themove = aimoves.getMove();
					checkSlot(themove.row, themove.col, turn);
					outputarea.append("AI move is:  " + themove.row + ", " + themove.col + "\n");
					checkCapture(themove.row, themove.col, turn);
					if (checkWin(turn)) {
						gameOver = true;
						mypentepanel.repaint();
						JOptionPane.showMessageDialog(null, "Tan Wins!");
						return;
					}
					turn = BLUE;
					updateAIMoves();

				} else if (!gameOver && checkSlot(row, column, turn)) {
					outputarea
							.append("Computer move is: " + row + ", " + column + " value " + stone[row][column] + "\n");
					checkCapture(row, column, turn);
					if (checkWin(turn)) {
						gameOver = true;
						mypentepanel.repaint();
						JOptionPane.showMessageDialog(null, "Tan Wins!");
						return;
					}
					turn = BLUE;
					updateAIMoves();
				}
			}
			mypentepanel.repaint();
		}

		// ----------------------------------------------------
		// GRAPHICS METHODS
		// ----------------------------------------------------

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(0, 0, getWidth(), getHeight());
			drawBoard(g);

			for (int row = 0; row < 19; row++) {
				for (int col = 0; col < 19; col++) {
					if (stone[row][col] != 0) {

						if (stone[row][col] == ERASED) {
							stone[row][col] = EMPTY;
						}
						drawStone(row, col, g);
					}
				}
			}
			drawCountStone(g);
		}

		// ----------------------------------------------------
		// Draw the Pente board
		// ----------------------------------------------------
		public void drawBoard(Graphics g) {
			g.setColor(new Color(179, 179, 126));
			g.fillRect(0, 0, WIDTH, WIDTH);

			g.setColor(Color.BLACK);
			for (int i = 0; i <= 19; i++) {
				int position = i * (WIDTH / 19);
				g.drawLine(position, 0, position, WIDTH);
				g.drawLine(0, position, WIDTH, position);
			}
			if (aimoves != null) {
				int numaimoves = aimoves.allaimoves.size();
				String num = " " + Integer.toString(numaimoves);
				String howmany = "Number of AI moves: " + num;
				g.drawString("                         ", 595, 50);
				g.drawString(howmany, 595, 65);
			}

		}

		// ----------------------------------------------------
		// Paint stones on the board.
		// ----------------------------------------------------

		public void drawStone(int row, int column, Graphics g) {

			if (stone[row][column] == ERASED || stone[row][column] == EMPTY) {
				// do not draw any stone

			} else {// rww put the stone image there

				if (stone[row][column] == BLUE) {
					g.drawImage(images[0], column * WIDTH / 19 + 1, row * WIDTH / 19 + 1, this);
				}
				if (stone[row][column] == RED_TAN) {
					g.drawImage(images[1], column * WIDTH / 19 + 1, row * WIDTH / 19 + 1, this);
				}

			}

			if (stone[row][column] == ERASED) {
				g.setColor(new Color(255, 0, 126)); //
				g.drawRect(column * WIDTH / 19 + 2, row * WIDTH / 19 + 2, 27, 27);
				g.fillRect(column * WIDTH / 19 + 2, row * WIDTH / 19 + 2, 27, 27);
			}

			if (stone[row][column] == EMPTY) {
				g.setColor(new Color(179, 179, 126)); // color = khaki
				g.drawRect(column * WIDTH / 19 + 2, row * WIDTH / 19 + 2, 27, 27);
				g.fillRect(column * WIDTH / 19 + 2, row * WIDTH / 19 + 2, 27, 27);
			}

			if (debug == true && stone[row][column] == AIPOSSIBLEMOVE) {
				String mystring = String.valueOf(aimoves.getMove(row, column).score);
				if (aimoves.getMove(row, column).score == 15) {
					g.drawImage(images[4], column * WIDTH / 19 + 1, row * WIDTH / 19 + 1, this);
				} else {
					g.drawString(mystring, column * WIDTH / 19 + 8, row * WIDTH / 19 + 20);
				}
			}
		}

		// ----------------------------------------------------
		// Display for the number of stones.
		// ----------------------------------------------------
		void drawCountStone(Graphics g) {
			g.setColor(Color.WHITE);
			g.fill3DRect(WIDTH + 15, 160, 60, 20, false);
			g.fill3DRect(WIDTH + 15, 410, 60, 20, false); // 30

			g.drawImage(images[0], WIDTH + 5, 130, this);
			g.drawImage(images[1], WIDTH + 5, 380, this);

			if (turn == BLUE) {
				g.drawImage(images[5], WIDTH + 60, 220, this);
			} else {
				g.drawImage(images[6], WIDTH + 60, 220, this);
			}

			g.setColor(Color.black);
			g.drawString("captures", WIDTH + 35, 397);
			g.drawString("captures", WIDTH + 35, 147);
			// ---------------------------------------------------
			if (paircount_blue == 1) {
				g.drawLine(WIDTH + 27, 165, WIDTH + 25, 175);
			}
			if (paircount_blue == 2) {
				g.drawLine(WIDTH + 27, 165, WIDTH + 25, 175);
				g.drawLine(WIDTH + 37, 165, WIDTH + 35, 175);
			}
			if (paircount_blue == 3) {
				g.drawLine(WIDTH + 27, 165, WIDTH + 25, 175);
				g.drawLine(WIDTH + 37, 165, WIDTH + 35, 175);
				g.drawLine(WIDTH + 47, 165, WIDTH + 45, 175);
			}
			if (paircount_blue == 4) {
				g.drawLine(WIDTH + 27, 165, WIDTH + 25, 175);
				g.drawLine(WIDTH + 37, 165, WIDTH + 35, 175);
				g.drawLine(WIDTH + 47, 165, WIDTH + 45, 175);
				g.drawLine(WIDTH + 57, 165, WIDTH + 55, 175);
			}
			if (paircount_blue > 4) {
				g.drawLine(WIDTH + 27, 165, WIDTH + 25, 175);
				g.drawLine(WIDTH + 37, 165, WIDTH + 35, 175);
				g.drawLine(WIDTH + 47, 165, WIDTH + 45, 175);
				g.drawLine(WIDTH + 57, 165, WIDTH + 55, 175);
				g.drawLine(WIDTH + 21, 166, WIDTH + 64, 174);
			}
			// Draw Computer Score
			if (paircount_red_tan == 1) {
				g.drawLine(WIDTH + 27, 415, WIDTH + 25, 425);
			}
			if (paircount_red_tan == 2) {
				g.drawLine(WIDTH + 27, 415, WIDTH + 25, 425);
				g.drawLine(WIDTH + 37, 415, WIDTH + 35, 425);
			}
			if (paircount_red_tan == 3) {
				g.drawLine(WIDTH + 27, 415, WIDTH + 25, 425);
				g.drawLine(WIDTH + 37, 415, WIDTH + 35, 425);
				g.drawLine(WIDTH + 47, 415, WIDTH + 45, 425);
			}
			if (paircount_red_tan == 4) {
				g.drawLine(WIDTH + 27, 415, WIDTH + 25, 425);
				g.drawLine(WIDTH + 37, 415, WIDTH + 35, 425);
				g.drawLine(WIDTH + 47, 415, WIDTH + 45, 425);
				g.drawLine(WIDTH + 57, 415, WIDTH + 55, 425);
			}
			if (paircount_red_tan > 4) {
				g.drawLine(WIDTH + 27, 415, WIDTH + 25, 425);
				g.drawLine(WIDTH + 37, 415, WIDTH + 35, 425);
				g.drawLine(WIDTH + 47, 415, WIDTH + 45, 425);
				g.drawLine(WIDTH + 57, 415, WIDTH + 55, 425);
				g.drawLine(WIDTH + 21, 416, WIDTH + 64, 424);
			}

		}

	} // end class pentepanel

} // end of class pente
//----------------------------------------------------
