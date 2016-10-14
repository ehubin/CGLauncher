import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Launcher {
	static String player1Class="player1";
	static String player2Class="player2";
	static ImagePanel ip=new ImagePanel();
	static Referee<UnleashTheGeek> gm = new Referee<UnleashTheGeek>(new UnleashTheGeek());
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
		ip.setState(gm.gs);
		gm.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				ip.repaint();
			}
		});
		ProcessBuilder player1 = new ProcessBuilder("java","-cp","bin",player1Class);
		player1.redirectError(Redirect.INHERIT);
		ProcessBuilder player2 = new ProcessBuilder("java","-cp","bin",player2Class);
		player2.redirectError(Redirect.INHERIT);
		try {
			
			final Process p1=player1.start();
			final Process p2=player2.start();
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
	private static void createAndShowGUI() {
	        //Create and set up the window.
	        JFrame frame = new JFrame("Unleash the geek");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	        //Add the ubiquitous "Hello World" label.
	        frame.getContentPane().add(ip);

	        //Display the window.
	        frame.pack();
	        frame.setVisible(true);
	    }

	public static class ImagePanel extends JPanel{

	    GameState gs=null;
	    public ImagePanel() {
	    	setPreferredSize(new Dimension(500,400));
	    }
	    void setState(GameState gs) {this.gs=gs;}

	    @Override
	    protected void paintComponent(Graphics g) {
	        super.paintComponent(g);
	        Graphics2D gr=(Graphics2D)g;
	        gr.setTransform(new AffineTransform(getWidth()/10000., 0, 0, getHeight()/8000., 0, 0));
	        if(gs!= null)gs.draw(gr); 
	    }

	}
}
