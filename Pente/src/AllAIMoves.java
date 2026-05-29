import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

public class AllAIMoves {

	final int EMPTY = 0;
	final int BLUE = 1;
	final int RED_TAN = -1;
	final int ERASED = 2;
	final int AIPOSSIBLEMOVE = 9;

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
	List<AIMove> allaimoves = new ArrayList<>();
	int[][] boardstate;
	Random rand = new Random();
	int tancapturecount;
	int bluecapturecount;

	public AllAIMoves(int[][] board, int tancaps, int bluecaps) {
		boardstate = board;
		this.tancapturecount = tancaps;
		this.bluecapturecount = bluecaps;
		findMoves();
		evaluateDuplicateMoves();// this method attempts to determine the better move if there are 2+ moves
									// of equal value as the best move and adds +1 to the value of that move.

	}
	// 15 = Tan will win the game
	// 14 = capture for tan that will prevent blue win
	// 13 = Block blue from winning
	// 12 = tan creates unblocked 4 in a row
	// 11 = block blue open 4 in a row
	// 10 = capture opportunity for tan
	// 9 = block a capture for blue
	// 8 = tan creates WIDE open 3 in a row. unblocked 2 spaces on either side.
	// 7 = block blue from creating WIDE open 3 in a row
	// 6 = tan creates open 3 in a row. unblocked 1 space on both sides.
	// 5 = block blue open 3 in a row / start a capture (if blue doesn't have
	// consecutive stone occupancy this would not be a capturestart)
	// 4 = tan creates 2 in a row unblocked
	// 3 = UNUSED
	// 2 = capped 4 in a row
	// 1 = EMPTY slot adjacent to a blue stone
	// 0 = self capture. blue will capture my stones if i go here.

	private void findMoves() {
		tryToWin(RED_TAN);// 15
		tryToWin(BLUE);// 14
		fourInARow(RED_TAN);// 12
		fourInARow(BLUE);// 11
		findCapture(RED_TAN, tancapturecount, bluecapturecount);// 14,10
		findCapture(BLUE, tancapturecount, bluecapturecount);// 13,9
		openTria(RED_TAN);// 8,6
		openTria(BLUE);// 7,5
		openTwo();// 4
		cappedFourInARow();// 2
		findLowPrioMove(RED_TAN);// 1
		startCapturePriorityOverride();// 10 if TAN has 4 captures
		selfCapture();// 0
		// pressureIfOpponentHasOpenTria();
	}

	private void cappedFourInARow() {
		for (int r = 0; r <= 18; r++) {
			for (int c = 0; c <= 18; c++) {
				for (int dir : directions) {

					int[] targetstone1 = checkStone(r, c, dir, 0);
					int[] targetstone2 = checkStone(r, c, dir, 1);
					int[] targetstone3 = checkStone(r, c, dir, 2);
					int[] targetstone4 = checkStone(r, c, dir, 3);
					int[] targetstone5 = checkStone(r, c, dir, 4);
					if (targetstone5[0] == -1) {
						continue;
					}
					int stone1 = boardstate[targetstone1[0]][targetstone1[1]];
					int stone2 = boardstate[targetstone2[0]][targetstone2[1]];
					int stone3 = boardstate[targetstone3[0]][targetstone3[1]];
					int stone4 = boardstate[targetstone4[0]][targetstone4[1]];
					int stone5 = boardstate[targetstone5[0]][targetstone5[1]];

					if (stone1 == EMPTY && stone2 == RED_TAN && stone3 == RED_TAN && stone4 == RED_TAN) {
						AIMove newmove = new AIMove(targetstone1[0], targetstone1[1], 2);
						addMove(newmove);// tan can play capped 4 in a row
					}
				}
			}
		}
	}

	private void pressureIfOpponentHasOpenTria() {
		boolean opponenthasopentria = false;

		for (AIMove move : allaimoves) {
			if (move.score == 11) {
				opponenthasopentria = true;
			}
		}
		if (opponenthasopentria) {
			for (AIMove move : allaimoves) {
				if (move.score == 2) {
					move.setScore(11);
				}
			}
		}

	}

	private void addMove(AIMove move) {

		for (int i = 0; i < allaimoves.size(); i++) {
			AIMove m = allaimoves.get(i);
			if (m.row == move.row && m.col == move.col) {
				if (m.score < move.score || move.score == 0) {
					allaimoves.set(i, move);
				}
				return;
			}
		}
		allaimoves.add(move);
	}

	public List<AIMove> getAllAIMoves() {
		return allaimoves;
	}

	public AIMove getMove() {
		Optional<AIMove> bestmove = allaimoves.stream().max(Comparator.comparingInt(AIMove::getScore));
		List<AIMove> duplicatemoves = allaimoves.stream().filter(move -> move.getScore() == bestmove.get().score)
				.toList();
		if (duplicatemoves.isEmpty() && !bestmove.isPresent()) {
			boolean going = true;
			while (going) {
				int randomrow = rand.nextInt(19);
				int randomcol = rand.nextInt(19);
				if (checkStone(randomrow, randomcol, 0, 0)[0] != -1) {
					going = false;
					return new AIMove(randomrow, randomcol, 1);
				}
			}
		}
		int random = rand.nextInt(0, duplicatemoves.size());
		return duplicatemoves.get(random);
	}

	public AIMove getMove(int row, int col) {
		for (AIMove move : allaimoves) {
			if (move.row == row && move.col == col) {
				return move;
			}
		}
		return new AIMove(row, col, 0);
	}

	public void checkCapture(int row, int col) {
		for (int dir : directions) {
			int[] targetstone1 = checkStone(row, col, dir, 1);
			int[] targetstone2 = checkStone(row, col, dir, 2);
			int[] targetstone3 = checkStone(row, col, dir, 3);
			if (targetstone3[0] == -1) {
				continue;
			}
			int stone1 = boardstate[targetstone1[0]][targetstone1[1]];
			int stone2 = boardstate[targetstone2[0]][targetstone2[1]];
			int stone3 = boardstate[targetstone3[0]][targetstone3[1]];

			if (stone1 == BLUE && stone2 == BLUE && stone3 == RED_TAN) {
				boardstate[targetstone1[0]][targetstone1[1]] = EMPTY;
				boardstate[targetstone2[0]][targetstone2[1]] = EMPTY;
				tancapturecount += 1;
			}

		}
	}

	private void evaluateDuplicateMoves() {
		// System.out.println(themove.row + "," + themove.col + "score2: " +
		// themove.score2);
		Optional<AIMove> bestmove = allaimoves.stream().max(Comparator.comparingInt(AIMove::getScore));
		List<AIMove> duplicatemoves = allaimoves.stream().filter(move -> move.getScore() == bestmove.get().score)
				.toList();
		if (duplicatemoves.isEmpty() || duplicatemoves.size() == 1) {
			System.out.println("Only one best move available. Did not execute futuremoves.");
			return;
		}
		List<List<AIMove>> allfuturemoves = new ArrayList<>();
		List<AIMove> themoves = new ArrayList<>();
		List<AIMove> originalaimoves = new ArrayList<>();
		allaimoves.forEach(move -> originalaimoves.add(move));

		int[][] originalboard = new int[19][19];
		for (int r = 0; r <= 18; r++) {
			for (int c = 0; c <= 18; c++) {
				originalboard[r][c] = boardstate[r][c];
			}
		}
		int originalcapturecount = tancapturecount;
		System.out.println("\noriginal capturecount= " + tancapturecount);
		for (AIMove themove : duplicatemoves) {

			if (themove.score == 15) {
				System.out.println("\nTheMove was a 15. Did not execute futuremoves.");
				return;
			}
			for (int r = 0; r <= 18; r++) {
				for (int c = 0; c <= 18; c++) {
					boardstate[r][c] = originalboard[r][c];
				}
			}
			boardstate[themove.row][themove.col] = RED_TAN;
			// checkCapture(themove.row,themove.col);
			// System.out.println("\nnew capturecount= "+tancapturecount);
			allaimoves.clear();
			findMoves();
			List<AIMove> futuremoves = new ArrayList<>();
			allaimoves.forEach(move -> futuremoves.add(move));
			allfuturemoves.add(futuremoves);
			themoves.add(themove);// so i can index reference the move to allfuturemoves
		}

		System.out.println("\n\nAll future moves size :" + allfuturemoves.size());
		List<AIMove> currentbestset = new ArrayList<>();
		int currentriskvalue = 0;
		int totalsetvalue = 0;
		int totalriskvalue = 0;
		int currentbestvalue = 0;
		int duplicatecount = 0;
		AIMove currentbestmove = new AIMove(0, 0, 0);

		for (List<AIMove> moveset : allfuturemoves) {

			System.out.println("\nSetID: " + moveset.hashCode());
			System.out.println("move: " + themoves.get(allfuturemoves.indexOf(moveset)).row + ","
					+ themoves.get(allfuturemoves.indexOf(moveset)).col);
			System.out.println("SetSize: " + moveset.size());

			currentbestmove = new AIMove(0, 0, 0);
			AIMove futurebestmove = new AIMove(0, 0, 0);

			totalsetvalue = 0;
			totalriskvalue = 0;
			for (AIMove move : moveset) {
				if (move.score % 2 == 0 && move.score >= 6) {
					totalsetvalue += move.score;
				}
				if (move.score % 2 != 0 && move.score >= 6) {
					totalriskvalue += move.score;
				}
			}
			System.out.println("Total moveset value: " + totalsetvalue);
			System.out.println("Total risk: " + totalriskvalue);

			if (currentbestvalue < totalsetvalue) {
				System.out.println("totalmovesetvalue was higher - " + totalsetvalue);
				currentbestmove = new AIMove(futurebestmove.row, futurebestmove.col, futurebestmove.score);
				currentbestset.clear();
				currentbestset.addAll(moveset);
				currentbestvalue = totalsetvalue;
				currentriskvalue = totalriskvalue;
				duplicatecount = 1;
			} else if (Math.abs(currentbestvalue - totalsetvalue) <= 10 && totalriskvalue < currentriskvalue
					&& futurebestmove.score > currentbestmove.score) {
				System.out.println(
						"moveset total was not higher but was within 10 and riskvalue was lower - totalmovesetvalue: "
								+ totalsetvalue);
				System.out.println(futurebestmove.score + ">" + currentbestmove.score);
				currentbestmove = new AIMove(futurebestmove.row, futurebestmove.col, futurebestmove.score);
				currentbestset.clear();
				currentbestset.addAll(moveset);
				currentbestvalue = totalsetvalue;
				currentriskvalue = totalriskvalue;
				duplicatecount = 1;
			} else if (currentbestvalue == totalsetvalue) {
				duplicatecount++;
				System.out.println("moveset value was the same. value: " + totalsetvalue);
			}
			System.out.println("currentbestID:" + currentbestset.hashCode() + " Value: " + currentbestvalue);
		}
		// end future
		allaimoves.clear();
		originalaimoves.forEach(move -> allaimoves.add(move));
		for (int r = 0; r <= 18; r++) {
			for (int c = 0; c <= 18; c++) {
				boardstate[r][c] = originalboard[r][c];
			}
		}
		// tancapturecount = originalcapturecount;
		// System.out.println("\noriginal capturecount= "+tancapturecount);
		if (duplicatecount != allfuturemoves.size()) {

//			allfuturemoves.forEach(
//					moveset -> System.out.println("\nallfutureIDs: " + moveset.hashCode() + " size: " + moveset.size()));
			int bestmoveindex = allfuturemoves.indexOf(currentbestset);
			if (bestmoveindex == -1) {
				System.out.println("\nAll move priorities were less than the threshold to be considered.");
				return;
			}
			System.out.println("\nThe best move was selected");
			System.out.println("currentbestID:" + currentbestset.hashCode());
			System.out.println("duplicatecount:" + duplicatecount);
			System.out.println("allfuturemovescount:" + allfuturemoves.size());
			int thebestmovescore = themoves.get(bestmoveindex).getScore();
			if (thebestmovescore > 1 && thebestmovescore < 14) {
				themoves.get(bestmoveindex).setScore(thebestmovescore + 1);
			}
		} else {
			System.out.println("\nAll moves resulted in the same outcome value");
			System.out.println("duplicatecount:" + duplicatecount);
			System.out.println("allfuturemovescount:" + (allfuturemoves.size()));
		}

	}

	private void selfCapture() {

		for (AIMove themove : allaimoves) {

			if (themove.score >= 11) {
				continue;
			}

			int r = themove.row;
			int c = themove.col;

			for (int dir : directions) {

				int[] rearstone = checkStone(r, c, dir, -1);
				int[] forwardstone = checkStone(r, c, dir, 1);
				int[] endstone = checkStone(r, c, dir, 2);

				if (rearstone[0] == -1 || endstone[0] == -1) {
					continue;
				}

				int rear = boardstate[rearstone[0]][rearstone[1]];
				int forward = boardstate[forwardstone[0]][forwardstone[1]];
				int end = boardstate[endstone[0]][endstone[1]];
				// This move would result in tan setting itself up to be captured.
				if (rear == BLUE && forward == RED_TAN && end == EMPTY) {
					AIMove newmove = new AIMove(r, c, 0);
					addMove(newmove);
				}
				if (rear == EMPTY && forward == RED_TAN && end == BLUE) {
					AIMove newmove = new AIMove(r, c, 0);
					addMove(newmove);
				}
			}
		}
	}

	private void findLowPrioMove(int turn) {
		for (int r = 0; r <= 18; r++) {
			for (int c = 0; c <= 18; c++) {
				if (boardstate[r][c] == -turn) {
					for (int dir : directions) {
						int[] t = checkStone(r, c, dir, 1);

						if (t[0] == -1) {
							continue;
						}
						if (boardstate[t[0]][t[1]] == EMPTY) {
							AIMove newmove = new AIMove(t[0], t[1], 1);
							addMove(newmove);
						}
					}
				}
			}
		}
	}

	private void fourInARow(int turn) {
		for (int r = 0; r <= 18; r++) {
			for (int c = 0; c <= 18; c++) {
				for (int dir : directions) {

					int openrow = 0;
					int opencol = 0;
					int samecount = 0;
					int emptycount = 0;

					int[] targetstone1 = checkStone(r, c, dir, 0);
					int[] targetstone2 = checkStone(r, c, dir, 1);
					int[] targetstone3 = checkStone(r, c, dir, 2);
					int[] targetstone4 = checkStone(r, c, dir, 3);
					int[] targetstone5 = checkStone(r, c, dir, 4);
					int[] targetstone6 = checkStone(r, c, dir, 5);
					int[][] targetstones = { targetstone2, targetstone3, targetstone4, targetstone5 };

					if (targetstone6[0] == -1) {
						continue;
					}

					List<int[]> mystones = new ArrayList<>();
					for (int[] stone : targetstones) {
						if (boardstate[stone[0]][stone[1]] == turn) {
							mystones.add(stone);
							samecount++;
						}
						if (boardstate[stone[0]][stone[1]] == EMPTY) {
							emptycount++;
							openrow = stone[0];
							opencol = stone[1];
						}
					}

					int stone1 = boardstate[targetstone1[0]][targetstone1[1]];
					int stone2 = boardstate[targetstone2[0]][targetstone2[1]];
					int stone3 = boardstate[targetstone3[0]][targetstone3[1]];
					int stone4 = boardstate[targetstone4[0]][targetstone4[1]];
					int stone5 = boardstate[targetstone5[0]][targetstone5[1]];
					int stone6 = boardstate[targetstone6[0]][targetstone6[1]];

					if (samecount == 3 && emptycount == 1 && (stone1 == EMPTY || stone1 == turn)
							&& (stone6 == EMPTY || stone6 == turn)) {
						if (turn == RED_TAN) {
							AIMove newmove = new AIMove(openrow, opencol, 12);
							newmove.setMyStones(mystones);
							addMove(newmove);
						} else {
							AIMove newmove = new AIMove(openrow, opencol, 11);
							newmove.setMyStones(mystones);
							addMove(newmove);
						}
					}
				}
			}
		}
	}

	private boolean canCreateOpenTwo(AIMove move) {

		int r = move.row;
		int c = move.col;

		for (int dir : directions) {

			int[] targetstone1 = checkStone(r, c, dir, 1);
			int[] targetstone2 = checkStone(r, c, dir, 2);
			int[] targetstone3 = checkStone(r, c, dir, -1);

			if (targetstone2[0] != -1 && targetstone3[0] != -1) {

				int stone1 = boardstate[targetstone1[0]][targetstone1[1]];
				int stone2 = boardstate[targetstone2[0]][targetstone2[1]];
				int stone3 = boardstate[targetstone3[0]][targetstone3[1]];

				if (stone1 == RED_TAN && stone2 == EMPTY && stone3 == EMPTY) {
					return true;
				}
			}
		}
		return false;
	}

	private void openTwo() {
		for (int r = 0; r <= 18; r++) {
			for (int c = 0; c <= 18; c++) {
				if (boardstate[r][c] != RED_TAN) {
					continue;
				}
				for (int dir : directions) {
					int[] targetstone1 = checkStone(r, c, dir, 1);
					int[] targetstone2 = checkStone(r, c, dir, 2);
					int[] targetstone3 = checkStone(r, c, dir, -1);

					if (targetstone2[0] != -1 && targetstone3[0] != -1) {

						int stone1 = boardstate[targetstone1[0]][targetstone1[1]];
						int stone2 = boardstate[targetstone2[0]][targetstone2[1]];
						int stone3 = boardstate[targetstone3[0]][targetstone3[1]];

						if (stone1 == EMPTY && stone2 == EMPTY && stone3 == EMPTY) {
							AIMove newmove = new AIMove(targetstone1[0], targetstone1[1], 4);
							addMove(newmove);
						}
					}
				}
			}
		}
	}

	private boolean canCreateOpenTria(AIMove themove) {
		for (int dir : directions) {

		}
		return false;
	}

	private void openTria(int turn) {
		for (int r = 0; r <= 18; r++) {
			for (int c = 0; c <= 18; c++) {
				for (int dir : directions) {

					int[] targetstone1 = checkStone(r, c, dir, 0);
					int[] targetstone2 = checkStone(r, c, dir, 1);
					int[] targetstone3 = checkStone(r, c, dir, 2);
					int[] targetstone4 = checkStone(r, c, dir, 3);
					int[] targetstone5 = checkStone(r, c, dir, 4);

					if (targetstone5[0] == -1) {
						continue;
					}

					List<int[]> targetstones = new ArrayList<>();
					targetstones.add(targetstone2);
					targetstones.add(targetstone3);
					targetstones.add(targetstone4);

					int stone1 = boardstate[targetstone1[0]][targetstone1[1]];
					int stone2 = boardstate[targetstone2[0]][targetstone2[1]];
					int stone3 = boardstate[targetstone3[0]][targetstone3[1]];
					int stone4 = boardstate[targetstone4[0]][targetstone4[1]];
					int stone5 = boardstate[targetstone5[0]][targetstone5[1]];

					List<Integer> stones = new ArrayList<>();
					stones.add(stone2);
					stones.add(stone3);
					stones.add(stone4);

					List<int[]> samestones = targetstones.stream()
							.filter(t -> stones.get(targetstones.indexOf(t)) == turn).toList();
					List<int[]> openstone = targetstones.stream()
							.filter(t -> stones.get(targetstones.indexOf(t)) == EMPTY).toList();

					if (samestones.size() == 2 && openstone.size() == 1 && stone1 == EMPTY && stone5 == EMPTY) {

						AIMove emptymove = new AIMove(openstone.getFirst()[0], openstone.getFirst()[1], 0);
						if (turn == RED_TAN) {
							emptymove.setScore(5);
						} else {
							emptymove.setScore(6);
						}

						int[] os1 = checkStone(r, c, dir, -1);
						int[] os2 = checkStone(r, c, dir, 5);

						if (os1[0] == -1 || os2[0] == -1) {
							if (emptymove.score > 0) {
								addMove(emptymove);
								continue;
							}
						}

						int outerstone1 = boardstate[os1[0]][os1[1]];
						int outerstone2 = boardstate[os2[0]][os2[1]];

						if ((outerstone1 == EMPTY || outerstone1 == turn)
								&& (outerstone2 == EMPTY || outerstone1 == turn)) {
							if (turn == RED_TAN) {
								emptymove.setScore(8);
							} else {
								emptymove.setScore(7);
							}
						}
						if (emptymove.score > 0) {
							addMove(emptymove);
						}
					}
				}
			}
		}
	}

	private void startCapturePriorityOverride() {

		if (tancapturecount != 4) {
			return;
		}
		for (int r = 0; r <= 18; r++) {
			for (int c = 0; c <= 18; c++) {
				for (int dir : directions) {

					int[] targetstone1 = checkStone(r, c, dir, 0);
					int[] targetstone2 = checkStone(r, c, dir, 1);
					int[] targetstone3 = checkStone(r, c, dir, 2);
					int[] targetstone4 = checkStone(r, c, dir, 3);
					if (targetstone4[0] == -1) {
						continue;
					}
					int stone1 = boardstate[targetstone1[0]][targetstone1[1]];
					int stone2 = boardstate[targetstone2[0]][targetstone2[1]];
					int stone3 = boardstate[targetstone3[0]][targetstone3[1]];
					int stone4 = boardstate[targetstone4[0]][targetstone4[1]];
					if (stone1 == EMPTY && stone2 == BLUE && stone3 == BLUE && stone4 == EMPTY
							&& tancapturecount == 4) {
						AIMove newmove = new AIMove(targetstone1[0], targetstone1[1], 10);
						addMove(newmove);
						AIMove newmove2 = new AIMove(targetstone4[0], targetstone4[1], 10);
						addMove(newmove2);
					}
				}
			}
		}

	}

	private void findCapture(int turn, int tancapturecount, int bluecapturecount) {
		for (int r = 0; r <= 18; r++) {
			for (int c = 0; c <= 18; c++) {
				for (int dir : directions) {

					int[] targetstone1 = checkStone(r, c, dir, 0);
					int[] targetstone2 = checkStone(r, c, dir, 1);
					int[] targetstone3 = checkStone(r, c, dir, 2);
					int[] targetstone4 = checkStone(r, c, dir, 3);
					if (targetstone4[0] == -1) {
						continue;
					}
					int stone1 = boardstate[targetstone1[0]][targetstone1[1]];
					int stone2 = boardstate[targetstone2[0]][targetstone2[1]];
					int stone3 = boardstate[targetstone3[0]][targetstone3[1]];
					int stone4 = boardstate[targetstone4[0]][targetstone4[1]];

					if (stone1 == EMPTY && stone2 == -turn && stone3 == -turn && stone4 == turn) {
						if (turn == RED_TAN) {

							List<AIMove> temp = allaimoves.stream().filter(move -> move.getScore() == 11).toList();
							if (!temp.isEmpty()) {
								for (AIMove move : temp) {
									if (move.getMyStones().isEmpty()) {
										continue;
									}
									for (int[] t : move.getMyStones()) {
										if (Arrays.equals(targetstone2, t) || Arrays.equals(targetstone3, t)) {
											AIMove newmove = new AIMove(targetstone1[0], targetstone1[1], 12);
											addMove(newmove);// capture for tan that will prevent blue open 4inarow
										}
									}
								}
							}
							List<AIMove> temp1 = allaimoves.stream().filter(move -> move.getScore() == 13).toList();
							if (!temp1.isEmpty()) {
								for (AIMove move : temp1) {
									if (move.getMyStones().isEmpty()) {
										continue;
									}
									for (int[] t : move.getMyStones()) {
										if (Arrays.equals(targetstone2, t) || Arrays.equals(targetstone3, t)) {
											AIMove newmove = new AIMove(targetstone1[0], targetstone1[1], 14);
											addMove(newmove);// capture for tan that will prevent blue win
										}
									}
								}
							}
							if (tancapturecount == 4) {
								AIMove newmove = new AIMove(targetstone1[0], targetstone1[1], 15);// tan will win on
																									// this capture
								addMove(newmove);
							} else {
								AIMove newmove = new AIMove(targetstone1[0], targetstone1[1], 10);// tan will capture on

								// this move
								addMove(newmove);
							}
						} else {
							if (bluecapturecount == 4) {
								AIMove newmove = new AIMove(targetstone1[0], targetstone1[1], 14);// block bluewin on
																									// this capture
								addMove(newmove);
							} else {
								AIMove newmove = new AIMove(targetstone1[0], targetstone1[1], 9);// block bluecapture on
																									// this move
								addMove(newmove);
							}
						}
					}
				}
			}
		}
	}

	private void tryToWin(int turn) {
		for (int r = 0; r <= 18; r++) {
			for (int c = 0; c <= 18; c++) {
				for (int dir : directions) {

					int[] targetstone1 = checkStone(r, c, dir, 0);
					int[] targetstone2 = checkStone(r, c, dir, 1);
					int[] targetstone3 = checkStone(r, c, dir, 2);
					int[] targetstone4 = checkStone(r, c, dir, 3);
					int[] targetstone5 = checkStone(r, c, dir, 4);

					if (targetstone5[0] == -1) {
						continue;
					}
					int stone1 = boardstate[targetstone1[0]][targetstone1[1]];
					int stone2 = boardstate[targetstone2[0]][targetstone2[1]];
					int stone3 = boardstate[targetstone3[0]][targetstone3[1]];
					int stone4 = boardstate[targetstone4[0]][targetstone4[1]];
					int stone5 = boardstate[targetstone5[0]][targetstone5[1]];

					List<Integer> stones = new ArrayList<>();
					stones.add(stone1);
					stones.add(stone2);
					stones.add(stone3);
					stones.add(stone4);
					stones.add(stone5);

					List<int[]> targetstones = new ArrayList<>();
					targetstones.add(targetstone1);
					targetstones.add(targetstone2);
					targetstones.add(targetstone3);
					targetstones.add(targetstone4);
					targetstones.add(targetstone5);

					List<int[]> samestones = targetstones.stream()
							.filter(t -> stones.get(targetstones.indexOf(t)) == turn).toList();
					List<int[]> openstone = targetstones.stream()
							.filter(t -> stones.get(targetstones.indexOf(t)) == EMPTY).toList();

//					if (samestones.size() == 4) {
//						System.out.println("\nTargetstones");
//						targetstones.forEach(t -> System.out.println(t[0] + "," + t[1]));
//						System.out.println("\nStones/Boardstate");
//						stones.forEach(stone -> System.out.println(stone));
//						System.out.println("\nSameStones");
//						samestones.forEach(stone -> System.out.println(stone[0]+","+stone[1]));
//						System.out.println("\nOpenStone");
//						openstone.forEach(stone -> System.out.println(stone[0]+","+stone[1]));
//					}

					if (samestones.size() == 4 && openstone.size() == 1) {

						if (turn == RED_TAN) {
							AIMove newmove = new AIMove(openstone.getFirst()[0], openstone.getFirst()[1], 15);
							addMove(newmove);
						} else {
							AIMove newmove = new AIMove(openstone.getFirst()[0], openstone.getFirst()[1], 13);
							newmove.setMyStones(samestones);
							addMove(newmove);
						}
					}
				}
			}
		}
	}

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
			return new int[] { row, col };
		} else {
			return new int[] { -1, -1 };
		}

	}

	private int[] checkStone(int row, int col, int dir1, int spaces1, int dir2, int spaces2) {
		int dir = 0;
		int spaces = 0;
		for (int i = 0; i <= 1; i++) {
			if (i == 0) {
				dir = dir1;
				spaces = spaces1;
			} else {
				dir = dir2;
				spaces = spaces2;
			}
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
		}
		if (isInBounds(row, col)) {
			return new int[] { row, col };
		} else {
			return new int[] { -1, -1 };
		}

	}

	private boolean isInBounds(int row, int col) {
		if (row < 0 || row >= 19 || col < 0 || col >= 19) {
			return false;
		}
		return true;
	}
}
