import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.Scanner;

public class Referee {
	static String player1Class="player1";
	static String player2Class="player2";
	
	
	PlayerListener pl1,pl2;
	OutputStream os1,os2;
	GameState<?> gs;
	public Referee(GameState<?> state) { gs=state;}
	
	public static void main(String[] arg) {
		Referee gm = new Referee(new UnleashTheGeek());		
		gm.start();
	}
	
	public void start() {
		gs.init();
		//spawn UI
		try {
			javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
			    public void run() {
			       gs.createAndShowGUI();
			    }
			});
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		
		// start player processes
		ProcessBuilder player1 = new ProcessBuilder("java","-cp","bin",player1Class);
		player1.redirectError(Redirect.INHERIT);
		ProcessBuilder player2 = new ProcessBuilder("java","-cp","bin",player2Class);
		player2.redirectError(Redirect.INHERIT);
		try {
			
			final Process p1=player1.start();
			final Process p2=player2.start();
			setPlayerStream(	p1.getInputStream(),
								p1.getOutputStream(),
								p2.getInputStream(),
								p2.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		Thread pl1t=new Thread(pl1);
		pl1t.start();
		Thread pl2t=new Thread(pl2);
		pl2t.start();
		try{
			os1.write(gs.getInitStr(0).getBytes());
			os1.flush();
			os2.write(gs.getInitStr(1).getBytes());
			os2.flush();
		}
		catch(Exception e) { e.printStackTrace(System.err);}
		play();
	}
	
	
	
	
	public void play() {
		try {
			gs.startTurn();
			os1.write(gs.getStateStr(0).getBytes());
			os1.flush();	
			os2.write(gs.getStateStr(1).getBytes());
			os2.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void setPlayerStream(InputStream player1,OutputStream p1o,InputStream player2,OutputStream p2o) {
		pl1=new PlayerListener(player1,0);pl2=new PlayerListener(player2,1);
		os1=p1o;os2=p2o;
		
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
		public void run() {
			try {
				while(gs.getResult() == GameState.UNDECIDED) {
					if(gs.readActions(sc,id)) { // all players gave their actions
						gs.save();
						gs.resolveActions();
						//for(ChangeListener cl:ll) cl.stateChanged(new ChangeEvent(saved));
						if(gs.getResult() == GameState.UNDECIDED) play(); //next turn
						else {//save last turn
							//for(ChangeListener cl:ll) cl.stateChanged(new ChangeEvent(gs.save()));
							gs.save();
						} 
					}
				}
				System.err.println("PLayerListener "+(id+1)+"exited!");
			} catch (Error e) {
				e.printStackTrace();
			}
		}
	}
}
