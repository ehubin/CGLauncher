import java.awt.Graphics2D;
import java.util.Scanner;

public interface GameState<UI> {
	static final int UNDECIDED=-1,DRAW=0,P1WINS=1,P2WINS=2;
	public int getResult(); // return -1 for undecided;0 for draw ;1 for player 1 win; 2 for player 2 wins
	public void init();
	public String getStateStr(int id);
	public String getInitStr(int id);
	public boolean  readActions(Scanner s,int id);
	public int resolveActions();
	public void startTurn();
	public void draw(Graphics2D g); // display this state on a Graphics 2D for a graphical visualisation of the state
	public GameState<UI> save();
	public UI createAndShowGUI();
	public void draw(UI ui); // draws this state on its own UI
}
