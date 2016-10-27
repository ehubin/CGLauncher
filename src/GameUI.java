import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;




@SuppressWarnings("serial")
public abstract class GameUI<State extends GameState> extends JFrame {
	private JSlider slider=new JSlider(0,0,0);
	private ImagePanel imagePanel= null;
	boolean sliderTouched=false;
    ArrayList<State> saved=new ArrayList<State>();
    State currentState=null;
    
    public GameUI(State gs,String title) {
    	//Create and set up the window.
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	initUI();
    	setCurrentState(gs);
    }
    abstract public void initUI();
    
    public JPanel getGamePanelWithControls(Dimension dim,Dimension board) {
    	JPanel panel=new JPanel(new BorderLayout());
    	imagePanel=new ImagePanel(board, dim);
    	
    	panel.add(imagePanel,BorderLayout.CENTER);
		
        JPanel slide=new JPanel(new FlowLayout());
        slide.add(new JButton(new AbstractAction("prev") {			
			@Override
			public void actionPerformed(ActionEvent e) {
				int v=slider.getValue(),m=slider.getMinimum();
				if(v>m) slider.setValue(v-1);
			}
		}));
        slide.add(slider);
        slide.add(new JButton(new AbstractAction("next") {
			@Override
			public void actionPerformed(ActionEvent e) {
				int v=slider.getValue(),m=slider.getMaximum();
				if(v<m) slider.setValue(v+1);
				
			}
		}));
        panel.add(slide,BorderLayout.PAGE_END);
        
        // make sure slider only fires changEvents when value changes
 		slider.setModel(new DefaultBoundedRangeModel() {
 			final ChangeEvent theOne=new ChangeEvent(this);
 			@Override
 		    public void setRangeProperties(int newValue, int newExtent, int newMin,int newMax, boolean adjusting) {
 		        changeEvent= (getValue() != newValue ? theOne:null);
 		        super.setRangeProperties(newValue, newExtent, newMin, newMax, adjusting);
 		    }
 			protected void fireStateChanged()
 		    {
 				if(changeEvent==null) return;
 		        super.fireStateChanged();
 		    }
 		});
        slider.getModel().addChangeListener(new ChangeListener() {	
			@Override
			public void stateChanged(ChangeEvent evt)  {
				System.err.println(" Slider:"+ slider.getValue()+" | "+evt+" | "+evt.getSource());
				if(slider.getValue()>0) sliderTouched=true;
				if(sliderTouched) javax.swing.SwingUtilities.invokeLater(new Runnable() {
		            public void run() {setCurrentState(saved.get(slider.getValue())); imagePanel.repaint();}
			
				});
			}
		});
        return panel;
    }
    // always executed on dispatcher thread. to be overloaded to update the UI components at state change
    public void setCurrentState(State s) {
    	currentState=s;
    }
    void addState(State gs) {
    	javax.swing.SwingUtilities.invokeLater(new Runnable() {public void run() {
			saved.add(gs);
			slider.setMaximum(saved.size()-1);
			if(!sliderTouched) {setCurrentState(gs); imagePanel.repaint();}
		}});
    }
    
    class ImagePanel extends JPanel {
    	Dimension boardsize;
    	ImagePanel (Dimension boardsize,Dimension preferred) {
    		setPreferredSize(preferred);
    		this.boardsize = boardsize;
    	}
	    @Override
	    protected void paintComponent(Graphics g) {
	        super.paintComponent(g);
	        Graphics2D gr=(Graphics2D)g;
	        gr.setTransform(new AffineTransform(1.0*getWidth()/boardsize.width, 0, 0, 1.0*getHeight()/boardsize.height, 0, 0));
	        if(currentState!= null) currentState.draw(gr); 
	    }
    }

}