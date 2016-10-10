import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

public class Launcher {
	static String player1Class="player1";
	static String player2Class="player2";
	static Referee<Hypersonic> gm = new Referee<Hypersonic>(new Hypersonic());
	public static void main(String[] args) {
		ProcessBuilder player1 = new ProcessBuilder("java","-cp","bin",player1Class);
		player1.redirectError(Redirect.INHERIT);
		ProcessBuilder player2 = new ProcessBuilder("java","-cp","bin",player2Class);
		player2.redirectError(Redirect.INHERIT);
		try {
			final Process p1=player1.start();
			final Process p2=player2.start();
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				public void run() {
					System.out.println( "Shutdown hook ran." );
			}}));
			gm.setPlayerStream(	p1.getInputStream(),
								p1.getOutputStream(),
								p2.getInputStream(),
								p2.getOutputStream());
			gm.start();
			System.out.println(gm.getResult());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

}
