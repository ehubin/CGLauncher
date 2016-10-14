import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.swing.event.ChangeListener;

public class Referee<GS extends GameState> {
	PlayerListener pl1,pl2;
	OutputStream os1,os2;
	ArrayList<ChangeListener> ll=new ArrayList<ChangeListener>();
	GS gs;
	public Referee(GS state) { gs=state;}
	public void start() {
		gs.init();
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
		for(ChangeListener cl:ll) cl.stateChanged(null);
	}
	
	public void setPlayerStream(InputStream player1,OutputStream p1o,InputStream player2,OutputStream p2o) {
		pl1=new PlayerListener(player1,0);pl2=new PlayerListener(player2,1);
		os1=p1o;os2=p2o;
		Thread pl1t=new Thread(pl1);
		pl1t.start();
		Thread pl2t=new Thread(pl2);
		pl2t.start();
	}
	public int getResult() // return 0 for draw ;1 for player 1 win; 2 for player 2 wins
	{
		return gs.getResult();
	}
	
	public void addChangeListener(ChangeListener cl){ // register to be notified when game is finished
		ll.add(cl);
	}
	class PlayerListener implements Runnable {
		int id;
		InputStream i;
		PlayerListener(InputStream is,int id) {
			i=is;
			this.id=id;
		}
		public void run() {
			int len=-1;
			byte[] buf=new byte[8192];
			try {
				while( (len=i.read(buf,0,8192))>0) {
					String s=new String(buf, 0, len);
					//System.err.println("Action "+(id+1)+"=>"+s);
					if(gs.setPlayerAction(id,s)) {
						play();
					}
				}
				System.err.println("PLayerListener "+(id+1)+"exited!");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
