import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.Scanner;



class UnleashTheGeek implements GameState {
	player1.State s=null;
	player1.Action[] a=new player1.Action[4];
	@Override
	public int getResult() {
		// TODO Auto-generated method stub
		return 0;
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

	@Override
	public boolean setPlayerAction(int id, String input) {
		boolean res=false;
		Scanner sc=null;
		try{
		//System.err.println("new action "+input);
		player1.PlayerState ps=id==0?s.p1:s.p2;
		sc= new Scanner(input);
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
			res= a[2]==null?false:true;
		} else {
			a[2]=a1;a[3]=a2;
			res= a[0]==null?false:true;
		}
		}
		catch(Exception e) {
			System.err.println("Exception <"+input+">");
		} finally {
			if(sc!= null) sc.close();
		}
		if(res) { // resolve turn
			s.simulate(a);
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
			
		}
		public void drawPlayer(player1.PlayerState ps,Graphics2D g) {
			drawPod(ps.p1,g);
			drawPod(ps.p2,g);
			int rad =250;
			Shape theCircle = new Ellipse2D.Double(ps.flagx-rad,ps.flagy-rad, 2*rad, 2*rad);
			g.setStroke(new BasicStroke(50));
		    g.draw(theCircle);
			g.setStroke(new BasicStroke(1));
		}
		
		public void drawPod(player1.Pod p,Graphics2D g) {
			final int rad=400;
			final float coeff=5.f;
			System.err.println(p.x+" "+p.vx+" "+p.y+" "+p.vy);
			Shape theCircle = new Ellipse2D.Double(p.x-rad,p.y-rad, 2*rad, 2*rad);
		    g.draw(theCircle);
		    g.drawLine((int)p.x, (int)p.y,(int)( (p.x+coeff*p.vx)),(int)((p.y+coeff*p.vy)));
		}


}
