import java.util.Random;
import java.util.Scanner;

public class utg17State implements GameState {
	private static final long serialVersionUID = 1L;

	static class Edge {
		int p1;int p2;
	}
	public static Random rnd=new Random();
	Player.State s;
	Player.Action[] actions;
	@Override
	public int getResult() {
		// TODO Auto-generated method stub
		return s.whoWins();
	}

	@Override
	public void init() {
		s=new Player.State();
		s.nbP=50+rnd.nextInt(101);
		s.nbE=(int)((3+rnd.nextDouble())*s.nbP);
		int player0=rnd.nextInt(s.nbP);
		int player1;
		do { player1=rnd.nextInt(s.nbP);} while(player1==player0);
		s.planets[player0].unit[0]=5;
		s.planets[player1].unit[1]=5;
		
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
		
		
	}

	@Override
	public GameState save() {
		return null;
	}

}
