import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ImagePanel extends JPanel{

    GameState<?> gs=null;
    public ImagePanel() {
    	setPreferredSize(new Dimension(500,400));
    }
    void setState(GameState<?> gs) {this.gs=gs;}

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D gr=(Graphics2D)g;
        gr.setTransform(new AffineTransform(getWidth()/10000., 0, 0, getHeight()/8000., 0, 0));
        if(gs!= null)gs.draw(gr); 
    }

}