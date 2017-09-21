import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Referee<State extends GameState> {
	// WARNING: can't run the same class twice when in the same jvm
	static String player1Class="Player";
	static String player2Class="Player";
	BlockingQueue<Boolean> q= new ArrayBlockingQueue<Boolean>(2,true);
	Thread pl1,pl2;
	Wrapper p1,p2;
	OutputStream os1,os2;
	State gs;
	long produce;
	GameUI<State> ui;
	boolean inProcess=false;
	boolean[] HasAction= {false,false};
	public Referee(State state) { gs=state;}
	public Referee(State state,boolean inProcess) { gs=state;this.inProcess=inProcess;}
	public static void main(String[] arg) {
//		Referee<UnleashTheGeek> referee = new Referee<UnleashTheGeek>(new UnleashTheGeek());	
//		referee.gs.init();
//		referee.ui = new UnleashTheGeek.utgUI(referee.gs);
		
		Referee<utg17State> referee = new Referee<utg17State>(new utg17State(),false);	
		referee.gs.init();
		referee.ui = new utg17State.ui(referee,"Unleash the geek 2017");
		referee.start();
		long prev=System.currentTimeMillis();
		try { //play next turn on main thread (because of crappy pipe class)
			while(true) {
				synchronized(referee.q){referee.q.wait();}
				referee.q.take();
				long cur=System.currentTimeMillis();
				System.err.println("turn time:"+(cur-prev));
				prev=cur;
				referee.play();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void start() {
		
		//spawn UI
		try {
			javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
			    public void run() {
			       ui.pack();
			       ui.setVisible(true);
			    }
			});
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		startProcesses();
	}
	public void startProcesses() {	
		// start player processes
		p1=Wrapper.create(player1Class, inProcess);
		p2=Wrapper.create(player2Class, inProcess);
		p1.start();
		p2.start();
		pl1=new Thread(new PlayerListener(p1.getInputStream(),0));
		pl2=new Thread(new PlayerListener(p2.getInputStream(),1));
		os1=p1.getOutputStream();os2=p2.getOutputStream();
		pl1.start();
		pl2.start();		
		try{
			os1.write(gs.getInitStr(0).getBytes());
			os1.flush();
			System.err.println("flushed "+gs.getInitStr(0));
			os2.write(gs.getInitStr(1).getBytes());
			os2.flush();
		}
		catch(Exception e) { e.printStackTrace(System.err);}
		
		play();
	}
public void reset(State starting) {
		gs = starting;
		pl1.interrupt();
		pl2.interrupt();
		p1.stop();
		p2.stop();
		startProcesses();
	}
	
	
	
	public void play() {
		try {
			gs.startTurn();
			System.err.println(">>"+gs.getStateStr(0)+"<<");
			os1.write(gs.getStateStr(0).getBytes());
			os1.flush();	
			os2.write(gs.getStateStr(1).getBytes());
			os2.flush();
			System.err.println(">>state written<<");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public int getResult() // return 0 for draw ;1 for player 1 win; 2 for player 2 wins
	{
		return gs.getResult();
	}
	
	
	
	class PlayerListener implements Runnable {
		int id;
		Scanner sc;
		PlayerListener(InputStream is,int id) {
			sc=new Scanner(is);
			this.id=id;
		}
		@SuppressWarnings("unchecked")
		public void run() {
			try {
				while(gs.getResult() == GameState.UNDECIDED) {
					boolean IHaveAllActions=false;
					gs.readActions(sc,id);
					synchronized(gs) {
						int other = id^1;
						if(HasAction[other]) {
							IHaveAllActions=true;
							HasAction[0]=false;
							HasAction[1]=false;
						} else { HasAction[id]=true;} 
					}
					if(IHaveAllActions) { // all players gave their actions
						ui.addState((State)gs.save());
						gs.resolveActions();
						//for(ChangeListener cl:ll) cl.stateChanged(new ChangeEvent(saved));
						if(gs.getResult() == GameState.UNDECIDED) {
							q.add(true); //next turn will be triggered on main thread
							synchronized(q){q.notifyAll();}
						}
						else {//save last turn
							//for(ChangeListener cl:ll) cl.stateChanged(new ChangeEvent(gs.save()));
							ui.addState((State)gs.save());
						} 
					}
				}
				System.err.println("PLayerListener "+(id+1)+"exited!");
			} catch (Error e) {
				e.printStackTrace();
			}
		}
	}
	static abstract class Wrapper {
		static ThreadWrapper tw=null;
		static Wrapper create(String classname,boolean inProcess) {
			if(inProcess) return tw==null? new ThreadWrapper(classname):tw;
			else return new ProcessWrapper(classname);
		}
		public abstract void start();
		public abstract void stop();
		abstract InputStream getInputStream();
		abstract OutputStream getOutputStream();
	}
	static class ProcessWrapper extends Wrapper {
		String classname;
		Process p;
		public ProcessWrapper(String classname) {
			this.classname=classname;
		}

		@Override
		public void start() {
			ProcessBuilder player = new ProcessBuilder("java","-cp","bin",classname);
			player.redirectError(Redirect.INHERIT);
			try {
				p=player.start();
			} catch (IOException e) {e.printStackTrace();}
		}

		@Override
		public void stop() {
			p.destroy();
		}

		@Override
		public InputStream getInputStream() {
			return p!=null? p.getInputStream():null;
		}

		@Override
		public OutputStream getOutputStream() {
			return p!=null? p.getOutputStream():null;
		}
		
	}
	
	static class ThreadWrapper extends Wrapper implements Runnable{
		Method main;
		PipedInputStream is= new PipedInputStream();
		PipedOutputStream os = new PipedOutputStream();
		Thread t;

		@Override
		public void run() {
			try {
				main.invoke(null,(Object)new String[] {});
			} catch(InvocationTargetException i) {
				System.err.println("Player exception");
				i.getCause().printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		@Override
		public InputStream getInputStream() {return is;}
		@Override
		public OutputStream getOutputStream() {return os;}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public ThreadWrapper(String classname) {
			try {
				Class c=Class.forName(classname);
				main=c.getDeclaredMethod("main", String[].class);
				c.getDeclaredField("in").set(null, new PipedInputStream(os));
				c.getDeclaredField("out").set(null,new PrintStream(new PipedOutputStream(is),true));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		@Override
		public void start() {
			t=new Thread(this);
			t.start();
		}
		@Override
		public void stop() {
			t.interrupt();
		}
	}
}
