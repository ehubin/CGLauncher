import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;



public class utg17State implements GameState {
	private static final long serialVersionUID = 1L;

	static class Edge {
		int p1;int p2;
		@Override
		public boolean equals(Object o) {
			Edge e=(Edge)o;
			return ((e.p1 == p1) && (e.p2 == p2)) || ((e.p1 == p2) && (e.p2 == p1));
		}
		@Override
		public int hashCode() { return p1 | p2<<16;}
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
		int player0=rnd.nextInt(s.nbP);
		int player1;
		do { player1=rnd.nextInt(s.nbP);} while(player1==player0);
		s.planets=new Player.Planet[s.nbP];
		for(int i=0;i<s.nbP;++i) s.planets[i]=new Player.Planet(i); 
		s.planets[player0].unit[0]=5;
		s.planets[player1].unit[1]=5;
		s.planets[player0].tolerance[0]=4;
		s.planets[player1].tolerance[1]=4;
		int nbE=0,nbEmpty=s.nbP,floorE =(int)(s.nbP*2.5);
		HashSet<Edge> ed=new HashSet<>();
		ArrayList<ArrayList<Player.Planet>> adj = new ArrayList<ArrayList<Player.Planet>>(s.nbP);
		for(int j=0;j<s.nbP;++j) adj.add(new ArrayList<Player.Planet>());
		do {
			Edge e=new Edge();
			e.p1=rnd.nextInt(s.nbP);
			e.p2=rnd.nextInt(s.nbP);
			if(e.p1==e.p2 || ed.contains(e)) continue;
			ed.add(e);
			adj.get(e.p1).add(s.planets[e.p2]);
			adj.get(e.p2).add(s.planets[e.p1]);
			if(adj.get(e.p1).size()==1) --nbEmpty;
			if(adj.get(e.p2).size()==1) --nbEmpty;
		} while(++nbE < floorE || nbEmpty>0 );
		
		int i=0;
		s.nbE = ed.size();
		s.edges = new int[ed.size()][2];
		
		for(Edge e:ed) {
			s.edges[i][0] = e.p1;
			s.edges[i++][1] = e.p2;
		}
		Player.Planet[] empty = new Player.Planet[0];
		for(int j=0;j<s.nbP;++j) s.planets[j].adj=adj.get(j).toArray(empty);
		actions = new Player.Action[s.nbP*2];
	}

	@Override
	public String getStateStr(int id) {
		int other = id^1;
		StringBuilder sb=new StringBuilder();
		for(Player.Planet p:s.planets) {
			sb.append(p.unit[id]+"\n");
			sb.append(p.tolerance[id]+"\n");
			sb.append(p.unit[other]+"\n");
			sb.append(p.tolerance[other]+"\n");
			sb.append(s.canAssign(p,id)?"1\n":"0\n");
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
		return sb.toString();
	}

	@Override
	public void readActions(Scanner in, int id) {
		actions[s.turn+id]= new Player.Action(in);
		System.err.println("read =>"+actions[s.turn+id]);
	}

	@Override
	public int resolveActions() {
		System.err.println("turn:"+s.turn);
		System.err.println("actions.length:"+actions.length);
		System.err.println("actions[s.turn]:"+actions[s.turn]);
		s.apply(actions[s.turn],actions[s.turn+1]);
		return 0;
	}

	@Override
	public void startTurn() {
		
		
	}

	@Override
	public GameState save() {
		return null;
	}
	
	@SuppressWarnings("serial")
	public static class ui extends GameUI<utg17State> {
		BufferedImage ship;
		JTextArea console;
		
		ui(Referee<utg17State> s, String title) { 
			super(s,title);
			//ship=loadSprite("ship.png");
		}
		ui(utg17State s, String title) { 
			super(s,title);
			//ship=loadSprite("ship.png");
		}
		@Override
		public void initUI() {
			Container c= getContentPane();
			c.setLayout(new BorderLayout());
			c.add(getGamePanelWithControls(new Dimension(800,450),new Dimension(16000,9000)),BorderLayout.CENTER);
			console=new JTextArea(100,60);
			JScrollPane sp = new JScrollPane(console);
			sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			sp.setPreferredSize(new Dimension(console.getPreferredSize().width+20,450));
			c.add(sp, BorderLayout.EAST);
			if(referee != null) {
				buttonPanel.add(new JButton(new AbstractAction("Reset") {

					@Override
					public void actionPerformed(ActionEvent e) {
						utg17State state = new utg17State();
						setCurrentState(state);
						saved.clear();
						sliderTouched=false;
						slider.setMaximum(0);
						referee.reset(state);
					}
				}));
			}
		}

		@Override
		void draw(Graphics2D g) {
			Color[] pcolors= {Color.BLACK,Color.RED};
			Player.State s =currentState.s;
			
		

		}
	}

}
