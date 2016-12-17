
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class CSBDummyPlayer {
    static boolean usedBoost=false;
	
 	static InputStream in=System.in;
 	static PrintStream out=System.out;
 	
    @SuppressWarnings("unused")
	public static void main(String args[]) {
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(in);
		System.err.println(">>"+in.getClass().getName());
        // game loop
        while (true) {
            int x = sc.nextInt();
            int y = sc.nextInt();
            int nextCheckpointX = sc.nextInt(); // x position of the next check point
            int nextCheckpointY = sc.nextInt(); // y position of the next check point
            int nextCheckpointDist = sc.nextInt(); // distance to the next checkpoint
            int nextCheckpointAngle = sc.nextInt(); // angle between your pod orientation and the direction of the next checkpoint
            int opponentX = sc.nextInt();
            int opponentY = sc.nextInt();
            //System.err.println("opponent "+opponentX+","+opponentY);
         
            if(!usedBoost && nextCheckpointDist >3000)  {out.println(nextCheckpointX + " " + nextCheckpointY +" BOOST");usedBoost=true;}
            else {
            	 out.println(nextCheckpointX + " " + nextCheckpointY + " "+(Math.min(100,Math.max(0,100-(int)(1.2*(nextCheckpointAngle-18))))));
            }
        }
    }
}
