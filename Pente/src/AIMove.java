import java.util.ArrayList;
import java.util.List;

public class AIMove {
	int row;
	int col;
	int dir;
	int score;
	boolean didCapture;
	int turn = 0;
	List<AIMove> captures = new ArrayList<>();
	List<int[]> mystones;
	int score2 = 0;

	public AIMove(int row, int col, int score)

	{
		this.row = row;
		this.col = col;
		this.score = score;
		
	}

	public int getScore() {
		return this.score;
	}
	public void setScore(int s) {
		this.score = s;
	}
	public int getScore2() {
		return this.score;
	}
	public void setScore2(int s) {
		this.score2 = s;
	}
	
	public int getTurn() {
		return this.turn;
	}

	public void setCaptures(AIMove stone1, AIMove stone2,int t) {
		didCapture = true;
		captures.add(stone1);
		captures.add(stone2);
		this.turn = t;
	}
	public void setMyStones(List<int[]> stonelist) {
		this.mystones=stonelist;
	}
	public List<int[]> getMyStones(){
		return this.mystones;
	}
	public List<AIMove> getCaptures() {
		return this.captures;
	}
}
