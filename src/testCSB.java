import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.Base64;

import javax.swing.AbstractAction;
import javax.swing.JButton;

public class testCSB {
 
	public static void main(String[] args) {
			
		
		testUI ui = new testUI(null);
		/*state.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				UnleashTheGeek tmp = new UnleashTheGeek();
				tmp.s = new player1.State((player1.State)e.getSource());
				System.out.println(tmp.s);
				ui.addState(tmp);
			}
		}); */
		//show the UI
		try {javax.swing.SwingUtilities.invokeAndWait(new Runnable() {public void run() {ui.pack();ui.setVisible(true); }});} catch (Exception e1) {e1.printStackTrace();}
		
		
		
	}
	
	@SuppressWarnings("serial")
	static class testUI extends CSB.ui {
		testUI(CSB csb) {super(csb,"test UI");}
		@Override
		public void initUI() {
			super.initUI();	
			JButton b = new JButton(new AbstractAction("State from clipboard") {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					String result = "";
				    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				    //odd: the Object param of getContents is not currently used
				    Transferable contents = clipboard.getContents(null);
				    boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
				    if (hasTransferableText) {
				    try {
				        result = (String)contents.getTransferData(DataFlavor.stringFlavor);
				        Object o = Helper.fromString(result,"Player","CSBPlayer");
				        CSB csb=null;
				        if (o instanceof CSB) csb=(CSB)o;
				        else if(o instanceof CSBPlayer.State[]) { csb = new CSB(); csb.s=((CSBPlayer.State[])o)[0];}
				        if(csb != null) {
				        	resetState(csb);
				        	CSB next=(CSB)csb.save();
					        next.s.simulate();
					        addState(next);
				        }
				        
				     }
				     catch (Exception ex){
				    	System.err.println(new String(Base64.getDecoder().decode(result))); 
				        ex.printStackTrace(System.err);
				      }
				    }
				}
			});
			buttonPanel.add(b);
			buttonPanel.revalidate();
			buttonPanel.repaint();
			
		}
	}

}


