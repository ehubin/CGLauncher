import java.util.Scanner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class test {
 
	public static void main(String[] args) {
		final UnleashTheGeek utg = new UnleashTheGeek();
		player1.State state=new player1.State();
		state.readInput(new Scanner(
				"-1 -1\n-1 -1\n1403 4625 -115 -89 0\n942 2150 -193 67 1\n2139 1252 188 279 0\n459 4847 -13 0 1\n"
		));
		utg.s=state;
		utg.a= new player1.Action[] 
				{ 		new player1.Action(2.846946275859237,97),
						new player1.Action(3.0844576701777253,500),
						new player1.Action(2.4979515419973675,100),
						new player1.Action(3.141592653589793,100)
				};
		state.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				UnleashTheGeek tmp = new UnleashTheGeek();
				tmp.s = new player1.State((player1.State)e.getSource());
				System.out.println(tmp.s);
				UnleashTheGeek.theUI.addSave(tmp);
			}
		});
		//show the UI
		try {javax.swing.SwingUtilities.invokeAndWait(new Runnable() {public void run() {utg.createAndShowGUI();UnleashTheGeek.theUI.addSave(utg); }});} catch (Exception e1) {e1.printStackTrace();}
		
		
		
		state.simulate(utg.a,true);
		System.out.println(state);
	}

}


