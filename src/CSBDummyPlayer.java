
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class CSBDummyPlayer {
    static boolean usedBoost=false;

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
            //System.err.println("opponent "+opponentX+","+opponentY);

            if(!usedBoost && nextCheckpointDist >3000)  {System.out.println(nextCheckpointX + " " + nextCheckpointY +" BOOST");usedBoost=true;}
            else {
                System.out.println(nextCheckpointX + " " + nextCheckpointY + (Math.abs(nextCheckpointAngle)<70 ? " 100":" 0"));
            }
        }
    }
}
