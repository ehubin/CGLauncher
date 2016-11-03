import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.JComponent;



public class CSB implements GameState {
	CSBPlayer.State s= new CSBPlayer.State();
	public CSB() {}
	public CSB(CSB o) { s=new CSBPlayer.State(o.s); }
	
	@Override
	public int getResult() {
		return UNDECIDED;
	}

	@Override
	public void init() {
	}

	@Override
	public String getStateStr(int id) {
		StringBuffer sb=new StringBuffer();
		CSBPlayer.PlayerState p =s.player[id];
		CSBPlayer.PlayerState other = id==0 ? s.player[1] :s.player[0];
		CSBPlayer.Obj nextCheck = s.checkpoint[p.nextCheck];
		sb.append(p.p1.x+" "+p.p1.y+" "+nextCheck.x+" "+nextCheck.y+" "+(int)Math.rint(p.p1.dist(nextCheck))+" "+p.p1.angleDeg(nextCheck));
		sb.append("\n"+other.p1.x+" "+other.p1.y+"\n");
		return sb.toString();
	}

	@Override
	public String getInitStr(int id) {
		return "";
	}

	@Override
	public void readActions(Scanner sc, int id) {
		s.a[id] = CSBPlayer.Action.readFrom(sc);
	}

	@Override
	public int resolveActions() {
		s.simulate();
		return getResult();
	}

	@Override
	public void startTurn() {
		Arrays.fill(s.a, null);
	}

	@Override
	public void draw(Graphics2D g,JComponent jc) {
		g.setColor(Color.BLUE);
		for(CSBPlayer.Obj o:s.checkpoint) {
			Shape theCircle = new Ellipse2D.Double(o.x-o.radius,o.y-o.radius,2*o.radius, 2*o.radius);
			g.draw(theCircle);
		}
		g.setColor(Color.BLACK);
		drawPlayer(s.player[0],g);
		g.setColor(Color.RED);
		drawPlayer(s.player[1],g);
		
		String outcome=null;
		switch(getResult())  {
			case DRAW: outcome="Draw";break;
			case P1WINS: outcome="Player 1 wins";break;
			case P2WINS: outcome="Player 2 wins";break;
			default:
		}
		if(outcome != null) {
			g.setFont(new Font("arial", Font.BOLD, 1500));
			FontMetrics metrics = g.getFontMetrics();
		    // Determine the X coordinate for the text
		    int x =  jc.getWidth()/2- (metrics.stringWidth(outcome) / 2);
		    // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
		    int y = jc.getHeight()/2 + metrics.getAscent()/2;
			g.drawString(outcome, x, y);
		}

	}
	public void drawPlayer(CSBPlayer.PlayerState p,Graphics2D g) {
		drawPod(p.p1, g);
	}
	public void drawPod(CSBPlayer.Pod p,Graphics2D g) {
		Shape theCircle = new Ellipse2D.Double(p.x-p.radius,p.y-p.radius,2*p.radius, 2*p.radius);
	    g.draw(theCircle);
	}
	
	
	@Override
	public GameState save() {
		return new CSB(this);
	}
	@SuppressWarnings("serial")
	public static class ui extends GameUI<CSB> {
		ui(CSB s, String title) { super(s,title);}

		@Override
		public void initUI() {
			Container c= getContentPane();
			c.setLayout(new BorderLayout());
			c.add(getGamePanelWithControls(new Dimension(800,450),new Dimension(16000,9000)),BorderLayout.CENTER);
			
		}
	}

}
