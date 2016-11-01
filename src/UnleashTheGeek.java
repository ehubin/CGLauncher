import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.Scanner;

import javax.swing.JPanel;
import javax.swing.JTextArea;




class UnleashTheGeek implements GameState {
	player1.State s=null;
	player1.Action[] a=new player1.Action[4];
	
	
	public UnleashTheGeek(UnleashTheGeek utg) {
		s=new player1.State(utg.s);
		for(int i=0;i<4;++i) {
			player1.Action o=utg.a[i];
			a[i] = new player1.Action();
			a[i].angle=o.angle;
			a[i].thrust=o.thrust;
			a[i].boost=o.boost;
		}	
	}
	public UnleashTheGeek() {for(int i=0;i<4;++i) { a[i]=new player1.Action();}}

	@Override
	public int getResult() {
		if(s.p1.nbFlagsCaptured >= 4) return(s.p2.nbFlagsCaptured >=4? DRAW:P1WINS);
		if(s.p2.nbFlagsCaptured >= 4) return(P2WINS);
		return s.turn <400 ? UNDECIDED:DRAW;
	}

	@Override
	public void init() {
		s=new player1.State();
		s.p1.p1.x=500;
		s.p1.p1.y=2500;
		s.p1.p2.x=500;
		s.p1.p2.y=5500;
		s.p1.flagx=9000;
		s.p1.flagy=4000;
		s.p1.myBase=1000;
		
		s.p2.p1.x=9500;
		s.p2.p1.y=2500;
		s.p2.p2.x=9500;
		s.p2.p2.y=5500;
		s.p2.flagx=1000;
		s.p2.flagy=4000;
		s.p2.myBase=9000;

	}

	@Override
	public String getStateStr(int id) {
		StringBuffer sb=new StringBuffer();
		player1.PlayerState p1,p2;
		if(id==0) {
			p1=s.p1;p2=s.p2;
		} else {
			p1=s.p2;p2=s.p1;
		}
		sb.append(p1.flagx+" "+p1.flagy+"\n");
		sb.append(p2.flagx+" "+p2.flagy+"\n");
		sb.append(p1.p1+"\n"+p1.p2+"\n"+p2.p1+"\n"+p2.p2+"\n");		
		//System.err.println("STATE:\n"+sb.toString());
		return sb.toString();
	}

	@Override
	public String getInitStr(int id) {
		return "";
	}

	
	public boolean readActions(Scanner sc, int id) {
		boolean res=false;
		try{
			//System.err.println("new action "+input);
			player1.PlayerState ps= (id==0?s.p1:s.p2);
			int x=sc.nextInt(),y=sc.nextInt();
			String str=sc.next();
			int thrust=str.equals("BOOST") ? 500: Integer.parseInt(str);
			double angle= Math.atan2(y-ps.p1.y,x-ps.p1.x);
			player1.Action a1=new player1.Action(angle,thrust);
			x=sc.nextInt();y=sc.nextInt();
			str=sc.next();
			thrust=str.equals("BOOST") ? 500: Integer.parseInt(str);
			angle= Math.atan2(y-ps.p2.y,x-ps.p2.x);
			player1.Action a2=new player1.Action(angle,thrust);
			if(id==0) {
				a[0]=a1;a[1]=a2;
				res= a[3]==null?false:true;
			} else {
				a[2]=a1;a[3]=a2;
				res= a[1]==null?false:true;
			}
			}
			catch(Exception e) {
				e.printStackTrace(System.err);
			}
			return res;
	}
	
	
		@Override
		public void startTurn() {
			for(int i=0;i<4;++i) a[i]=null;
			
		}

		@Override
		public void draw(Graphics2D g) {
			
			g.setColor(Color.BLACK);
			drawPlayer(s.p1,g);
			g.setColor(Color.RED);
			drawPlayer(s.p2,g);
			
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
			    int x = 5000 - (metrics.stringWidth(outcome) / 2);
			    // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
			    int y = 4000 + metrics.getAscent()/2;
				g.drawString(outcome, x, y);
			}
			
		}
		public void drawPlayer(player1.PlayerState ps,Graphics2D g) {
			drawPod(ps.p1,g);
			drawPod(ps.p2,g);
			final int rad =250;
			if(ps.flagx != -1) { // flag is in the arena
				Shape theCircle = new Ellipse2D.Double(ps.flagx-rad,ps.flagy-rad, 2*rad, 2*rad);
				g.setStroke(new BasicStroke(50));
			    g.draw(theCircle);
				g.setStroke(new BasicStroke(1));
			}
		}
		
		public void drawPod(player1.Pod p,Graphics2D g) {
			final int rad=400;
			final float coeff=5.f;
			//System.err.println(p.x+" "+p.vx+" "+p.y+" "+p.vy);
			Shape theCircle = new Ellipse2D.Double(p.x-rad,p.y-rad, 2*rad, 2*rad);
			if (p.hasFlag) g.setStroke(new BasicStroke(50));
		    g.draw(theCircle);
		    g.drawLine((int)p.x, (int)p.y,(int)( (p.x+coeff*p.vx)),(int)((p.y+coeff*p.vy)));
		    if (p.hasFlag) g.setStroke(new BasicStroke(1));
		}

		@Override
		public GameState save() {
			UnleashTheGeek utg=new UnleashTheGeek(this);
			return utg;
			
		}
		@Override
		public int resolveActions() {
			try{
				s.simulate(a,true);
			} catch(Exception e) {
				e.printStackTrace(System.err);
			}
			return UNDECIDED;
		}
		
		
		@SuppressWarnings("serial")
		public static class utgUI extends GameUI<UnleashTheGeek> {
			private JTextArea  state,actions;

			
			utgUI(UnleashTheGeek utg) {
				super(utg,"Unleash the geek");
				
				
		        
		        
		       
		        

			}
			public void initUI() {
				Container c= getContentPane();
				c.setLayout(new BorderLayout());
				c.add(getGamePanelWithControls(new Dimension(500,400),new Dimension(10000,8000)),BorderLayout.CENTER);
				 //Add the ubiquitous "Hello World" label.
		        JPanel jp =new JPanel();
		        c.add(jp,BorderLayout.LINE_END);
		        state=new JTextArea();
		        actions = new JTextArea();
		        jp.add(state);
		        jp.add(actions);
		        state.setPreferredSize(new Dimension(300, 500));
		        actions.setPreferredSize(new Dimension(300, 500));
		        
			}
			public void setCurrentState(UnleashTheGeek utg) {
				super.setCurrentState(utg);				
				StringBuffer sb=new StringBuffer();
				for(player1.Action a:utg.a) sb.append(a+"\n");
				sb.append("\n\n");
				for(player1.Action act:utg.a) sb.append("new player1.Action("+act.angle+","+act.thrust+"),\n");
				actions.setText(sb.toString());
				state.setText(utg.s.toString()+"\n\n"+utg.s.toInitString());
			}
			
		}


}
