
public interface GameState {
	public int getResult(); // return -1 for undecided;0 for draw ;1 for player 1 win; 2 for player 2 wins
	public void init();
	public String getStateStr();
	public void setPlayerAction(int id,String s);
}
