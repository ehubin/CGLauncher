import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;




@SuppressWarnings("serial")
public abstract class GameUI<State extends GameState> extends JFrame {
	JSlider slider=new JSlider(0,0,0);
	ImagePanel imagePanel= null;
	JPanel buttonPanel=null;
	boolean sliderTouched=false;
    ArrayList<State> saved=new ArrayList<>();
    State currentState=null;
    Referee<State> referee=null;
    
    GameUI(Referee<State> r,String title) {
    	super(title);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        referee = r;
    	initUI();
    	setCurrentState(r.gs);
    }
    GameUI(State gs,String title) {
    	//Create and set up the window.
        super(title);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    	initUI();
    	setCurrentState(gs);
    }
    abstract public void initUI();
    abstract void draw(Graphics2D g); // draws the current state
    
    JPanel getGamePanelWithControls(Dimension dim,Dimension board) {
    	JPanel panel=new JPanel(new BorderLayout());
    	imagePanel=new ImagePanel(board, dim);
    	
    	panel.add(imagePanel,BorderLayout.CENTER);
		
        buttonPanel=new JPanel(new FlowLayout());
        buttonPanel.add(new JButton(new AbstractAction("prev") {			
			@Override
			public void actionPerformed(ActionEvent e) {
				int v=slider.getValue(),m=slider.getMinimum();
				if(v>m) slider.setValue(v-1);
			}
		}));
        buttonPanel.add(slider);
        buttonPanel.add(new JButton(new AbstractAction("next") {
			@Override
			public void actionPerformed(ActionEvent e) {
				int v=slider.getValue(),m=slider.getMaximum();
				if(v<m) slider.setValue(v+1);
				
			}
		}));
        buttonPanel.add(new JButton(new AbstractAction("State to clipboard") {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					StringSelection selection = new StringSelection(Helper.toString(currentState));
				    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				    clipboard.setContents(selection, selection);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
			}
		}));
        panel.add(buttonPanel,BorderLayout.PAGE_END);
        
        // make sure slider only fires changEvents when value changes
 		slider.setModel(new DefaultBoundedRangeModel(0,0,0,0) {
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
        slider.getModel().addChangeListener((evt -> {
				System.err.println(" Slider:"+ slider.getValue()+" | "+evt+" | "+evt.getSource());
				if(slider.getValue()>0) sliderTouched=true;
				if(sliderTouched) javax.swing.SwingUtilities.invokeLater(
                        () -> {setCurrentState(saved.get(slider.getValue())); imagePanel.repaint();}
			
				);
			}
		));
        return panel;
    }
    // always executed on dispatcher thread. to be overloaded to update the UI components at state change
    public void setCurrentState(State s) {
    	currentState=s;
    }
    void addState(State gs) {
    	javax.swing.SwingUtilities.invokeLater(() -> {
			saved.add(gs);
			slider.setMaximum(saved.size()-1);
			if(!sliderTouched) {setCurrentState(gs); imagePanel.repaint();}
		});
    }
    void resetState(State s) {
    	javax.swing.SwingUtilities.invokeLater(() -> {
			saved.clear();
    		saved.add(s);
			slider.setMaximum(saved.size()-1);
			sliderTouched=false;
			setCurrentState(s); 
			imagePanel.repaint();}
		);
		
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
	        if(currentState!= null) draw(gr); 
	    }
    }
    
    static BufferedImage loadSprite(String path) {
    	try {
			BufferedImage src=ImageIO.read(new File(path));
			BufferedImage img= new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
		    Graphics2D g2d= img.createGraphics();
		    g2d.drawImage(src, 0, 0, null);
		    g2d.dispose();
		    return img;
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    static BufferedImage changeColor(BufferedImage bi,Color from,Color to) {
    	BufferedImageOp lookup = new LookupOp(new ColorMapper(from, to), null);
    	return lookup.filter(bi, null);
    }
    
    public static class ColorMapper
    extends LookupTable {

        private final int[] from;
        private final int[] to;

        ColorMapper(Color from,
                           Color to) {
            super(0, 4);

            this.from = new int[] {
                from.getRed(),
                from.getGreen(),
                from.getBlue(),
                from.getAlpha(),
            };
            this.to = new int[] {
                to.getRed(),
                to.getGreen(),
                to.getBlue(),
                to.getAlpha(),
            };
        }

        @Override
        public int[] lookupPixel(int[] src,
                                 int[] dest) {
            if (dest == null) {
                dest = new int[src.length];
            }

            int[] newColor = (Arrays.equals(src, from) ? to : src);
            System.arraycopy(newColor, 0, dest, 0, newColor.length);

            return dest;
        }
    }

}