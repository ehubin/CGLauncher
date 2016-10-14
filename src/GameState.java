import java.awt.Graphics2D;

public interface GameState {
	public int getResult(); // return -1 for undecided;0 for draw ;1 for player 1 win; 2 for player 2 wins
	public void init();
	public String getStateStr(int id);
	public String getInitStr(int id);
	public boolean setPlayerAction(int id,String s);
	public void startTurn();
	public void draw(Graphics2D g);
}
