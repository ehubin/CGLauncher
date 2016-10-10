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
		String st = gs.getStateStr();
		try {
			pl1.reset();
			os1.write(st.getBytes());
			pl2.reset();
			os2.write(st.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public void setPlayerStream(InputStream player1,OutputStream p1o,InputStream player2,OutputStream p2o) {
		pl1=new PlayerListener(player1);pl2=new PlayerListener(player2);
		os1=p1o;os2=p2o;
	}
	public int getResult() // return 0 for draw ;1 for player 1 win; 2 for player 2 wins
	{
		return gs.getResult();
	}
	
	public void addChangeListener(ChangeListener cl){ // register to be notified when game is finished
		ll.add(cl);
	}
	class PlayerListener implements Runnable {
		InputStream i;
		boolean hasResponse=false;
		PlayerListener(InputStream is) {
			i=is;
		}
		public void run() {
			int len=-1;
			byte[] buf=new byte[8192];
			try {
				while( (len=i.read(buf,0,8192))>0) {
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public void reset() {
			hasResponse=false;
		}
	}
}
