import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;





public class CSB implements GameState,Serializable {
	private static final long serialVersionUID = 1L;
	CSBPlayer.State s= new CSBPlayer.State();
	public CSB() {}
	public CSB(CSB o) { s=new CSBPlayer.State(o.s); }
	
	@Override
	public String toString() {
		return s.toString();
	}
	
	
	@Override
	public int getResult() {
		HashMap<CSBPlayer.PlayerState,Integer> pn = new HashMap<CSBPlayer.PlayerState,Integer>();
		pn.put(s.player[0], 1);
		pn.put(s.player[1], 2);
		for(CSBPlayer.PlayerState pl:s.player) {
			if(pl.curLap == CSBPlayer.State.TOTAL_LAP) return pn.get(pl);
		}
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
		sb.append(p.p1.x+" "+p.p1.y+" "+nextCheck.x+" "+nextCheck.y+" "+(int)Math.rint(p.p1.dist(nextCheck))+" "+(int)Math.round(p.p1.deltaDeg(nextCheck)));
		sb.append("\n"+other.p1.x+" "+other.p1.y+"\n");
		return sb.toString();
	}

	@Override
	public String getInitStr(int id) {
		return "";
	}

	@Override
	public void readActions(Scanner sc, int id) {
		s.a[id] = CSBPlayer.Action.readFrom(sc,s.player[id].p1);
		System.err.println("Player "+(id+1)+" action: "+s.a[id]);
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
	public GameState save() {
		return new CSB(this);
	}
	
	
	@SuppressWarnings("serial")
	public static class ui extends GameUI<CSB> {
		BufferedImage ship;
		JTextArea console;
		
		ui(Referee<CSB> s, String title) { 
			super(s,title);
			ship=loadSprite("ship.png");
		}
		ui(CSB s, String title) { 
			super(s,title);
			ship=loadSprite("ship.png");
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
						CSB csb = new CSB();
						setCurrentState(csb);
						saved.clear();
						sliderTouched=false;
						slider.setMaximum(0);
						referee.reset(csb);
					}
				}));
			}
		}

		@Override
		void draw(Graphics2D g) {
			Color[] pcolors= {Color.BLACK,Color.RED};
			CSBPlayer.State s =currentState.s;
			
			int idx=1;
			for(CSBPlayer.Obj o:s.checkpoint) {
				g.setColor(Color.BLUE);
				Shape theCircle = new Ellipse2D.Double(o.x-o.radius,o.y-o.radius,2*o.radius, 2*o.radius);
				g.draw(theCircle);
				
				
				Shape cCircle =null;
				for(int i=0;i<pcolors.length;++i) { 
					if(s.player[i].nextCheck == idx-1) {
						g.setColor(pcolors[i]);
						cCircle= new Ellipse2D.Double(o.x+o.radius+i*200,o.y+o.radius,180, 180);
						g.fill(cCircle);
					}
				}
				g.setColor(Color.BLUE);
				g.setFont(new Font("arial", Font.BOLD, 400));
				FontMetrics metrics = g.getFontMetrics();
				String nb=""+(idx++);
				g.drawString(nb, o.x- (metrics.stringWidth(nb) / 2), o.y+metrics.getAscent()/2);
				
			}
			for(int i=0;i<pcolors.length;++i) {
				g.setColor(pcolors[i]);
				drawPlayer(s.player[i],g);
			}
			
			String outcome=null;
			switch(currentState.getResult())  {
				case DRAW: outcome="Draw";break;
				case P1WINS: outcome="Player 1 wins";break;
				case P2WINS: outcome="Player 2 wins";break;
				default:
			}
			if(outcome != null) {
				g.setFont(new Font("arial", Font.BOLD, 1500));
				FontMetrics metrics = g.getFontMetrics();
				g.setColor(Color.BLUE);
				Dimension r=imagePanel.boardsize;
			    int x =  r.width/2 - metrics.stringWidth(outcome) / 2;
			    int y = r.height/2 + metrics.getAscent()/2;
				g.drawString(outcome, x, y);
			}

		}
		public void drawPlayer(CSBPlayer.PlayerState p,Graphics2D g) {
			drawPod(p.p1, g);
		}
		public void drawPod(CSBPlayer.Pod p,Graphics2D g) {
			BufferedImage bi=changeColor(ship, Color.BLACK, g.getColor());
			AffineTransform at = new AffineTransform();
            at.translate(p.x,p.y);
            at.rotate(p.angle*Math.PI/180);
            at.translate(-bi.getWidth()/2, -bi.getHeight()/2);
		    g.drawImage(bi,at,null);
			
		}
		@Override
		public void setCurrentState(CSB s) {
	    	super.setCurrentState(s);
			if(s!= null) console.setText(s.toString());
			else console.setText("");
	    }
	}

}
