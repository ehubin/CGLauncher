import java.util.Scanner;

public class utg17State implements GameState {
	Player.State s;
	@Override
	public int getResult() {
		// TODO Auto-generated method stub
		return UNDECIDED;
	}

	@Override
	public void init() {
		s=new Player.State();

	}

	@Override
	public String getStateStr(int id) {
		int other = id^1;
		StringBuilder sb=new StringBuilder();
		for(Player.Planet p:s.planets) {
			sb.append(p.unit[id]);
			sb.append(p.tolerance[id]);
			sb.append(p.unit[other]);
			sb.append(p.tolerance[other]);
			sb.append(s.canAssign(p,id)?"1":"0");
		}
		return sb.toString();
	}

	@Override
	public String getInitStr(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void readActions(Scanner s, int id) {
		// TODO Auto-generated method stub

	}

	@Override
	public int resolveActions() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void startTurn() {
		// TODO Auto-generated method stub

	}

	@Override
	public GameState save() {
		// TODO Auto-generated method stub
		return null;
	}

}
