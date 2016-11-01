import java.util.Scanner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class test {
 
	public static void main(String[] args) {
		final UnleashTheGeek utg = new UnleashTheGeek();
		player1.State state=new player1.State();
		
		state.readInput(new Scanner(
				"-1 -1\n9000 4000\n2036 2673 89 334 0\n1719 1021 643 26 0\n3901 2744 181 -42 0\n786 6595 -73 591 1\n"
		));
		utg.s=state;
		UnleashTheGeek.utgUI ui = new UnleashTheGeek.utgUI(utg);
		utg.a= new player1.Action[] 
				{ 		new player1.Action(1.889700589067614,98),
						new player1.Action(-2.3116992087575565,500),
						new player1.Action(-2.528539797266605,100),
						new player1.Action(3.141592653589793,100)
				};
		state.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				UnleashTheGeek tmp = new UnleashTheGeek();
				tmp.s = new player1.State((player1.State)e.getSource());
				System.out.println(tmp.s);
				ui.addState(tmp);
			}
		});
		//show the UI
		try {javax.swing.SwingUtilities.invokeAndWait(new Runnable() {public void run() {ui.pack();ui.setVisible(true); }});} catch (Exception e1) {e1.printStackTrace();}
		
		
		
		state.simulate(utg.a,true);
		System.out.println(state);
	}

}


