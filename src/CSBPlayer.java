import java.util.Random;
import java.util.Scanner;



public class CSBPlayer {

	static boolean usedBoost=false;
	final static int WIDTH=16000,HEIGHT=9000;
	final static Random Rnd=new Random(System.currentTimeMillis());
	
	public static void main(String args[]) {
		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);

		// game loop
		while (true) {
			int x = in.nextInt();
			int y = in.nextInt();
			int nextCheckpointX = in.nextInt(); // x position of the next check point
			int nextCheckpointY = in.nextInt(); // y position of the next check point
			int nextCheckpointDist = in.nextInt(); // distance to the next checkpoint
			int nextCheckpointAngle = in.nextInt(); // angle between your pod orientation and the direction of the next checkpoint
			int opponentX = in.nextInt();
			int opponentY = in.nextInt();
			// Write an action using System.out.println()
			// To debug: System.err.println("Debug messages...");


			// You have to output the target position
			// followed by the power (0 <= thrust <= 100)
			// i.e.: "x y thrust"
			if(!usedBoost && nextCheckpointDist >3000)  {System.out.println(nextCheckpointX + " " + nextCheckpointY +" BOOST");usedBoost=true;}
			else {
				System.out.println(nextCheckpointX + " " + nextCheckpointY + (Math.abs(nextCheckpointAngle)<70 ? " 100":" 0"));
			}
		}
	}
	
	public static class State {
		
		Action[] a= new Action[2];
		static final int NB_CHECKPOINT=3;
		static final int TOTAL_LAP=3;
		static final int MIN_CHK_DIST2=600*600;
		Obj[] checkpoint = new Obj[NB_CHECKPOINT];
		PlayerState[] player= new PlayerState[2];
		State() {
			player[0]=new PlayerState();
			player[1]= new PlayerState();
			int idx=0;
			while(idx<NB_CHECKPOINT) {
				Obj candidate = Obj.getRandomCheckpoint();
				for(int i=0;i<idx;i++) if(candidate.dist2(checkpoint[i]) <= MIN_CHK_DIST2) continue;
				checkpoint[idx++]=candidate;
			}
		}
		State(State other) {
			player[0]=new PlayerState(other.player[0]);
			player[1]=new PlayerState(other.player[1]);
			a[0]=new Action(other.a[0]);
			a[1]=new Action(other.a[1]);
			for(int i=0;i<checkpoint.length;++i) checkpoint[i] = new Obj(other.checkpoint[i]);
		}
		void simulate() {
			player[0].p1.simulate(a[0]);
			player[1].p1.simulate(a[1]);
		}
	}
	
	public static class Action {
		static final int BOOST=200;
		int tx,ty;
		int thrust;
		boolean shield;
		public Action(Action a) {
			tx=a.tx; ty=a.ty; thrust = a.thrust; shield = a.shield;
		}
		public Action() {}
		void setThrust(int t) {
			if(t<0 || t>100) return;
			thrust=t;
		}
		static Action readFrom(Scanner in)
		{
			Action res = new Action();
			res.tx=in.nextInt();
			res.ty=in.nextInt();
			String t = in.next();
			if(t.equals("SHIELD")) {res.shield=true; res.thrust=0;}
			else if (t.equals("BOOST")) res.thrust=BOOST;
			else { 
				try {res.setThrust(Integer.parseInt(t));}
				catch(NumberFormatException nfe) {nfe.printStackTrace(System.err);}
			}
			return res;
		}
	}
	
	public static class PlayerState {
		public PlayerState() { p1=new Pod(); p2=new Pod();}
		public PlayerState(PlayerState p) {
			this.curLap = p.curLap;
			this.nextCheck = p.nextCheck;
			p1=new Pod(p.p1);
			p2=new Pod(p.p2);
		}
		int curLap=0;
		int nextCheck=0;
		Pod p1;
		Pod p2;
		
	}
	public static class Obj {
		static Obj getRandomCheckpoint() {
			return new Obj(600);
		}
		Obj(int rad) {
			radius=rad;
			x=rad+Rnd.nextInt(WIDTH-2*rad);
			y=rad+Rnd.nextInt(HEIGHT-2*rad);
		}
		
		public Obj(Obj obj) {
			this.x=obj.x;
			this.y=obj.y;
			this.radius = obj.radius;
		}
		int x,y;
		int radius;
		public double dist(Obj o) {
			return Math.sqrt(dist2(o));
		}
		public int dist2(Obj o) {
			return (o.x-x)*(o.x-x)+(o.y-y)*(o.y-y);
		}
		public double angle(Obj o) {
			return Math.atan2(o.y-y, o.x-x); 
		}
		public int angleDeg(Obj o) {
			return (int)Math.rint(Math.atan2(o.y-y, o.x-x)*180/Math.PI); 
		}
	}
	public static class Pod extends Obj{
		public Pod() { super(400); }
		public void simulate(Action a) {
			int dx=x-a.tx,dy=y-a.ty;
			double n=Math.sqrt(dx*dx+dy*dy);
			vx += Math.rint(dx*a.thrust/n);
			vy += Math.rint(dy*a.thrust/n);
			x+=vx;
			y+=vy;
			
		}
		public Pod(Pod p) {
			super(p);
			this.vx=p.vx; this.vy=p.vy;
			this.usedBoost=p.usedBoost;
			this.shieldIn= p.shieldIn;
			this.name=p.name;
		}
		int vx=0,vy=0;
		boolean usedBoost=false;
		int shieldIn=0;
		String name="";
	}
}