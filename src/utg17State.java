import java.util.Scanner;

public class utg17State implements GameState {
	Player.State s;
	Player.Action[] actions;
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
		StringBuilder sb= new StringBuilder();
		sb.append(s.nbP+"\n");
		sb.append(s.nbE+"\n");
		for(int i=0;i<s.edges.length;++i) {
			sb.append(s.edges[i][0]+" "+s.edges[i][1]+"\n");
		}
		return null;
	}

	@Override
	public void readActions(Scanner in, int id) {
		actions[s.turn+id]= new Player.Action(in);
	}

	@Override
	public int resolveActions() {
		s.apply(actions[s.turn] );
		s.apply(actions[s.turn]);
		return 0;
	}

	@Override
	public void startTurn() {
		// TODO Auto-generated method stub

	}

	@Override
	public GameState save() {
		return null;
	}

}
