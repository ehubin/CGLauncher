import java.io.Serializable;
import java.util.Scanner;


public interface GameState extends Serializable {
	static final int UNDECIDED=-1,DRAW=0,P1WINS=1,P2WINS=2;
	public int getResult(); // return -1 for undecided;0 for draw ;1 for player 1 win; 2 for player 2 wins
	public void init();	// set-up the state before teh game starts
	public String getStateStr(int id); // returns the string to be sent to the player's program at the begining of the turn
	public String getInitStr(int id);  // returns the string to be sent to the player's program at the begining of the game
	public void readActions(Scanner s,int id); // read player actions from the scanner and store them
	public int resolveActions();	// update state with actions that have been read from he players
	public void startTurn();  // init the state before a new turn starts
	public GameState save(); // clones current state.
}
