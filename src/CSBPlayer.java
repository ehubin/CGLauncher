import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;




class CSBPlayer {

	static boolean usedBoost=false;
	final static int WIDTH=16000,HEIGHT=9000;
	static final double D2R=Math.PI/180;
	final static Random Rnd=new Random(System.currentTimeMillis());
	
	// input/output magic for local invocation
	 	
	 	static InputStream in=System.in;
	 	static PrintStream out=System.out;
	 	
	public static void main(String args[]) {
		Scanner sc = new Scanner(in);
		//ArrayList<State> state=new ArrayList<State>();
		AI theAI=new AI();
		State prev=null,cur,sim=null;
		// game loop
		while (true) {
			cur = new State(sc,prev,sim);
			System.err.println("Angle-->"+cur.player[0].p1.angle);
			if(sim!= null && !sim.equals(cur)) {
				System.err.println(">"+sim.player[0].p1+"\n>>"+cur.player[0].p1+"\n");
				try {System.err.println(toString(prev));} catch (Exception e) {e.printStackTrace();}
			}
			//state.add(cur);

			Pod me = cur.player[0].p1;
			Obj check = cur.player[0].next;
			String actionStr="";
			//if(!usedBoost && me.dist(check) >3000)  {actionStr=check.x + " " + check.y +" BOOST";usedBoost=true;} else
			{
			    System.err.println();
				actionStr=check.x + " " + check.y + (Math.abs(me.angleDeg(check)-me.angle)<70 ? " 100":" 0");
			}
			Action[] best=theAI.getBestMove(cur);
			actionStr= best[0].toString();
			System.err.println(actionStr);
			System.err.println("Best seq"+theAI.candidates.best());
			// compute next move in sim and save cur into prev
			cur.a[0] = Action.readFrom(new Scanner(actionStr),cur.player[0].p1);
			cur.a[1] = new Action();
			prev=new State(cur);
			cur.simulate(); 
			sim=cur;
			out.println(actionStr);
		}
	}
	@SuppressWarnings("serial")
	public static class myPQ<T> extends PriorityQueue<T> {
    	Object[] q=null;
    	public myPQ(int setSize) {
			super(setSize);
		}
		@SuppressWarnings("unchecked")
		public T getRandomItem() {
    		if(q==null) {
    			Field f=null;
				try {  f = this.getClass().getSuperclass().getDeclaredField("queue"); } catch (Exception e) {
					e.printStackTrace();
				}
    			f.setAccessible(true);
    			try {
					q=(Object[])f.get(this);
				} catch (Exception e) {
					e.printStackTrace();
				}
    		}
    		return (T) q[Rnd.nextInt(size())];
    	}
		T best() {
			Iterator<T> i=iterator();
			T e=null;
			while(i.hasNext()) e=i.next();
			return e;
		}
    }
	public static class AI {
	    static final int SET_SIZE=50,DEPTH=4;
	    static final long MAX_TIME=100L;
	    
		myPQ<Sequence> candidates = new myPQ<Sequence>(SET_SIZE); 
	    
	    void generateDefaultActions(State s,int depth) {
	    	candidates.clear();
	    	Sequence seq= new Sequence(s);
	    	for(int i=0;i<DEPTH;++i) seq.add(new Action[] { new Action(100,s.player[0].p1.angle*D2R),new Action(100,0)});
	    	candidates.add(seq);
	    	
	    }
	    Action[] getBestMove(State s) {
	        generateDefaultActions(s,DEPTH);
	        int worse = candidates.peek().getScore();
	        long start = System.currentTimeMillis(),cur;
	        double progress=0;
	        do {
	            Sequence seq = candidates.getRandomItem();
	            Sequence tmp = seq.mutate(1.0-(progress/MAX_TIME));
	            if(tmp.getScore() >= worse) {
	              if(candidates.size() == SET_SIZE) candidates.poll();
	              candidates.add(tmp);
	            }
	            cur = System.currentTimeMillis();
	            progress = cur-start;     
	        } while(progress < MAX_TIME);
	        System.err.println("CSBDummyPlayer time "+progress);
	        return candidates.best().actions.get(0); // return first action of best sequence
	    }
	}
	
	public static class Sequence implements Comparable<Sequence> {
		int A_CHANGE = 2*30;
	    ArrayList<Action[]> actions=new ArrayList<Action[]>();
		State begin,end;
	    public int getScore() { return end.getScore(0); }
	    Sequence mutate(double progress) { // return new Sequence mutated from the current one
	    	//System.err.println("Mutating ("+progress+") "+this);
	    	Sequence res = new Sequence(this.begin);
	    	for (Action[] a : actions) {
	    		Action[] changed = new Action[a.length];
	    		for(int i=0;i<a.length;++i) {
	    			changed[i] = new Action(a[i]);
	    			changed[i].angle = changed[i].angle+(progress*D2R*(Rnd.nextInt(A_CHANGE)-A_CHANGE/2) );
	    		}
	    		
	    		res.add(changed);
	    	}
	    	//System.err.println("After Mutate "+res);
	    	return res;
	    }
	    Sequence(State s) {
	    	begin=s;
	    	end=s;
	    }
	    void add(Action[] a) {
	    	end.a=a;
	    	end.simulate();
	    	actions.add(a);
	    }
		@Override
		public int compareTo(Sequence o) {
			return o.getScore()-this.getScore();
		}
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("Score:"+getScore());sb.append("\n");
			for(Action[] a:actions) {sb.append(a[0].angle/D2R); sb.append("\n");}
			sb.append(end);
			return sb.toString();
		}
	}
	
	public static class State implements Serializable{
		private static final long serialVersionUID = 1L;
		Action[] a= new Action[2];
		static final int NB_CHECKPOINT=3;
		static final int TOTAL_LAP=3;
		static final int MIN_CHK_DIST2=4*700*700;
		static final int POD_RAD=400;
		static final int CHECK_RAD=600;
		static final int CHECK_POD_DIST2=(POD_RAD+CHECK_RAD)*(POD_RAD+CHECK_RAD);
		Obj[] checkpoint = new Obj[NB_CHECKPOINT];
		PlayerState[] player= {new PlayerState(),new PlayerState()};
		
		@Override
		public boolean equals(Object o) {
		    State other = (State)o;
		    return player[0].p1.equals(other.player[0].p1);
		}
		State() {
			int idx=0;
			chk:
			while(idx<NB_CHECKPOINT) {
				Obj candidate = Obj.getRandomCheckpoint();
				for(int i=0;i<idx;i++) if(candidate.dist2(checkpoint[i]) <= MIN_CHK_DIST2) continue chk;
				checkpoint[idx++]=candidate;
			}
			int c01=  checkpoint[0].angleDeg(checkpoint[1]);
			player[0].p1.angle = c01;
			player[1].p1.angle = c01;
			player[0].p1.x = checkpoint[0].x+(int)(600*Math.cos(D2R*c01+Math.PI/2));
			player[0].p1.y = checkpoint[0].y+(int)(600*Math.sin(D2R*c01+Math.PI/2));
			player[1].p1.x = checkpoint[0].x+(int)(600*Math.cos(D2R*c01-Math.PI/2));
			player[1].p1.y = checkpoint[0].y+(int)(600*Math.sin(D2R*c01-Math.PI/2));
		}
		
		int getScore(int p) {
			Pt next = checkpoint[player[p].nextCheck];
			int prevIdx = player[p].nextCheck ==0 ?checkpoint.length-1:player[p].nextCheck -1;
			Pt prev= checkpoint[prevIdx];
			int tot = prev.dist2(next);
		    int res= player[p].getScore()+ (int) Math.round(20.0 * (tot-player[p].p1.dist2(next))/tot);
		    return res;
		}
		
		public State(Scanner in,State prev,State sim) {
			player[0].p1.x=in.nextInt();
			player[0].p1.y=in.nextInt();
			player[0].next = new Obj(in.nextInt(),in.nextInt(),600);
			checkpoint[1] = player[0].next;
			in.nextInt(); // distance to the next checkpoint
			player[0].p1.angle =player[0].p1.angleDeg(player[0].next)-in.nextInt() ;
			player[1].p1.x=in.nextInt();
			player[1].p1.y=in.nextInt();
			
			if(prev != null) {
		        for(int i=0;i<checkpoint.length;++i) checkpoint[i] = prev.checkpoint[i];
		        player[0].p1.vx = sim.player[0].p1.vx;
		        player[0].p1.vy = sim.player[0].p1.vy;
		        player[0].nextCheck = prev.player[0].nextCheck;
		        
		    } else {
		        for(int i=0;i<checkpoint.length;++i) checkpoint[i] = new Obj();
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
			
			// @TODO implement proper intersection between pod and checkpoint 
			for(PlayerState pl:player) {
				if(pl.p1.dist2(checkpoint[pl.nextCheck]) <= CHECK_POD_DIST2 && pl.p1.x >=0 && pl.p1.x <= WIDTH && pl.p1.y>=0 && pl.p1.y <= HEIGHT ) {
					pl.nextCheck++;
					if(pl.nextCheck >= NB_CHECKPOINT) { pl.nextCheck=0; }
					if(pl.nextCheck == 1) pl.curLap++;
					//System.err.println("New checkpoint:"+pl.nextCheck+" for:"+pl);
				}
			}
		}
		void simulate(ArrayList<Action[]> seq) {
		    for(Action[] a: seq) this.a=a;
		    simulate();
		}
		@Override
		public String toString() {
			StringBuffer sb=new StringBuffer();
			for(Obj o:checkpoint) sb.append(o+" ");
			sb.append("\n");
			for(PlayerState p:player) sb.append(p+"\n");
			return sb.toString();
		}
	}
	
	public static class Action implements Serializable {
		private static final long serialVersionUID = 1L;
		static final int BOOST=600,DIST=Integer.MAX_VALUE/2;
		double angle;
		int thrust;
		boolean shield;
		public Action(Action a) {
			angle=a.angle; thrust = a.thrust; shield = a.shield;
		}
		public Action() {}
		public Action(int t,double a) {thrust=t;angle=a;}
		void setThrust(int t) {
			if(t<0 || t>100) return;
			thrust=t;
		}
		static Action readFrom(Scanner in,Pod p)
		{
			Action res = new Action();
			int tx=in.nextInt();
			int ty=in.nextInt();
			res.angle=Math.atan2(ty-p.y,tx-p.x);
			String t = in.next();
			if(t.equals("SHIELD")) {res.shield=true; res.thrust=0;}
			else if (t.equals("BOOST")) res.thrust=BOOST;
			else { 
				try {res.setThrust(Integer.parseInt(t));}
				catch(NumberFormatException nfe) {nfe.printStackTrace(System.err);}
			}
			return res;
		}
		@Override
		public String toString() {
			return (int)(DIST*Math.cos(angle))+" "+(int)(DIST*Math.sin(angle))+" "+ (thrust==BOOST?"BOOST":thrust);
		}
	}
	
	public static class PlayerState implements Serializable{
		private static final long serialVersionUID = 1L;
		public PlayerState() { p1=new Pod(); p2=new Pod();}
		public PlayerState(PlayerState p) {
			this.curLap = p.curLap;
			this.nextCheck = p.nextCheck;
			p1=new Pod(p.p1);
			p2=new Pod(p.p2);
		}
		int curLap=0;
		int nextCheck=1;
		transient Obj next=null;
		Pod p1;
		Pod p2;
		public String toString() {
			return p1+"\n"+p2;
		}
		public int getScore() {
			int nbcp = nextCheck == 0 ? State.NB_CHECKPOINT :nextCheck;
			return 100* curLap+ 20*(nbcp-1);
		}
		
	}
	public static class Pt implements Serializable{
		private static final long serialVersionUID = 1L;
		int x,y;
		public Pt() {}
		Pt(int x,int y) {this.x=x;this.y=y;}
		Pt(Pt o) {x=o.x;y=o.y;}
		public double dist(Pt o) {
			return Math.sqrt(dist2(o));
		}
		public int dist2(Pt o) {
			return (o.x-x)*(o.x-x)+(o.y-y)*(o.y-y);
		}
		public double angle(Pt o) {
			return Math.atan2(o.y-y, o.x-x); 
		}
		public int angleDeg(Pt o) {
			return (int)Math.rint(Math.atan2(o.y-y, o.x-x)/D2R); 
		}
		
	}
	public static class Obj  extends Pt implements Serializable{
		private static final long serialVersionUID = 1L;

		static Obj getRandomCheckpoint() {
			return new Obj(600);
		}
		public Obj() {}
		Obj(int rad) {
			super(rad+Rnd.nextInt(WIDTH-2*rad),rad+Rnd.nextInt(HEIGHT-2*rad));
			radius=rad;
		}
		
		public Obj(Obj obj) {
			super(obj);
			this.radius = obj.radius;
		}
		public Obj(int x, int y, int r) {
			this.x=x;this.y=y;this.radius=r;
		}
		int radius;
		public String toString() {
			return x+" "+y+" "+radius;
		}
	}
	public static class Pod extends Obj implements Serializable {
		private static final long serialVersionUID = 1L;
		static final int POD_RAD=400,MAX_ANGLE_CHG=18;
		double angle=0;
		int vx=0,vy=0;
		boolean usedBoost=false;
		int shieldIn=0;
		String name="";
		@Override
	    public boolean equals(Object o) {
		    Pod other = (Pod)o;
		    return x==other.x && y==other.y && vx==other.vx && vy==other.vy;
		}
		public Pod() { super(POD_RAD); }
		public void simulate(Action a) {
			double delta = deltaDeg(a.angle);
			
			if(Math.abs(delta) <= MAX_ANGLE_CHG) angle+=delta;
			else {
				angle += (delta>0? MAX_ANGLE_CHG : -MAX_ANGLE_CHG); 
				//System.err.println("Sature angle"+delta);
			}
			double dvx = vx + Math.cos(angle*D2R)*a.thrust;
			double dvy = vy + Math.sin(angle*D2R)*a.thrust;
			//System.err.println("Before:"+x+","+y+"|"+vx+","+vy+"|"+angle);
			x=(int)Math.round(x+dvx);
			y=(int)Math.round(y+dvy);
			//System.err.println(x+","+y+"|"+dvx+"("+(Math.cos(angle*D2R)*a.thrust)+"),"+dvy+"("+(Math.sin(angle*D2R)*a.thrust)+")");
			vx=(int)(dvx*0.85);
			vy=(int)(dvy*0.85);
			//angle=Math.rint(angle);
			//System.err.println("newV "+vx+","+vy);
		}
		public Pod(Pod p) {
			super(p);
			this.vx=p.vx; this.vy=p.vy;
			this.usedBoost=p.usedBoost;
			this.shieldIn= p.shieldIn;
			this.name=p.name;
			this.angle=p.angle;
		}
		// computes smallest angle to change my angle toward the target Point p
		public double deltaDeg(Pt p) {
			//int delta = angleDeg(p)-angle;
			//int sign = (delta >= 0 && delta <= 180) || (delta <=-180 && delta>= -360) ? 1 : -1;
			//int d = Math.abs(delta) % 360;
			//delta = d>180? 360-d:d;
			//return sign*delta;
			double delta = (angle(p)/D2R-angle)%360;
			if(delta >180) return delta - 360;
			if(delta < -180) return delta +360;
			return delta;
		}
		// computes smallest angle to change my angle toward the direction in radian
		public double deltaDeg(double direction) {

			double delta = (direction/D2R-angle)%360;
			if(delta >180) return delta - 360;
			if(delta < -180) return delta +360;
			return delta;
		}
		public String toString() {
			return super.toString()+" "+angle+" "+vx+" "+vy;
		}
	}
	
	/** Write the object to a Base64 string. */
    public static String toString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray()); 
    }
}