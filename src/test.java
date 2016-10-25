import java.awt.datatransfer.StringSelection;
import java.util.Scanner;

public class test {

	public static void main(String[] args) {
		//Sorti 9143 135 -222 -162 0
		player1.State s=new player1.State();
		s.readInput(new Scanner(
"1000 4000\n-1 -1\n846 5456 7 -7 0\n624 641 -632 -152 1\n2865 430 -33 -68 0\n1509 697 67 99 0\n"
				));
		player1.Action[] a = new player1.Action[] 
				{ 		new player1.Action(-1.6859595144243893,2),
						new player1.Action(3.1304919994588576,500),
						new player1.Action(3.047745583903113,100),
						new player1.Action(-2.7086749982693488,100)
				};
		s.simulate(a,true);
		System.out.println(s);
	}

}


