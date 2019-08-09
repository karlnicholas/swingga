package swingga;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import javax.swing.*;

/**
 * 
 * Massive silliness
 *
 */
public class SwingGa extends JFrame {
	List<Critter> critters = new ArrayList<>();
	private static final long serialVersionUID = 1L;
	private Thread thread;
	private final static int cSize = 10;

	public SwingGa() {
		super("SwingGa");
	}
	
	class MyThread implements Runnable {
		private Consumer<List<Critter>> reproduce;
		public boolean run = true;
		private int counter = 0;
		private MyPanel myPanel; 
		public MyThread(MyPanel myPanel, Consumer<List<Critter>> reproduce) {
			this.myPanel = myPanel;
			this.reproduce = reproduce;
		}
		@Override
		public void run() {
			while(run) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				critters.forEach(Critter::move);
//				System.out.println(critters.get(0).toString());
				myPanel.repaint();
				if ( counter % 200 == 0 ) {
					printAverages();
					reproduce.accept(critters);
				}
			}
		}
		private void printAverages() {
			int cSize = critters.size();
			int xRandMax = 0; 
			int yRandMax = 0;
			int xRandOff = 0;
			int yRandOff = 0;
			int xRandLimit = 0;
			int yRandLimit = 0;
			for ( int i = 0 ; i < cSize; ++i) {
				Critter cr = critters.get(i);
				xRandMax += cr.xRandMax; 
				yRandMax += cr.yRandMax;
				xRandOff += cr.xRandOff;
				yRandOff += cr.yRandOff;
				xRandLimit += cr.xRandLimit;
				yRandLimit += cr.yRandLimit;
			}
			
			System.out.println(
			xRandMax / cSize +":" +  
			yRandMax / cSize +":" + 
			xRandOff / cSize +":" + 
			yRandOff / cSize +":" + 
			xRandLimit / cSize +":" + 
			yRandLimit / cSize 
			);
		}
		
	}
	
	class MyPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		public MyPanel() {
	        setBorder(BorderFactory.createLineBorder(Color.black));
	    }

	    public Dimension getPreferredSize() {
	        return new Dimension(1000, 1000);
	    }

	    public void paintComponent(Graphics g) {
	        super.paintComponent(g);       
	    	Graphics2D g2D = (Graphics2D)  g;
	    	Iterator<Critter> cit = critters.iterator();
	    	while ( cit.hasNext() ) {
	    		Critter c1 = cit.next();
	    		c1.move();
	    		Rectangle r1 = new Rectangle(c1.x, c1.y, cSize, cSize);
	    		boolean collision = false;
		    	for ( Critter c2: critters ) {
		    		if ( c1 == c2) continue;
		    		Rectangle r2 = new Rectangle(c2.x, c2.y, cSize, cSize);
		    		if ( r1.intersects(r2) ) {
		    			collision = true;
		    			cit.remove();
		    			break;
		    		}
		    	}
		    	if ( !collision ) {
		    		g2D.fillOval(c1.x, c1.y, cSize, cSize);
		    	}
	    	}
	    }  
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked
	 * from the event-dispatching thread.
	 */
	public void createAndShowGUI() {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    MyPanel myPanel = new MyPanel();
        add(myPanel);
        
		// Display the window.
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		// Create and set up the window.

		for ( int i = 0; i < 100; ++i ) {
			critters.add(new Critter(
					cSize + (int)(Math.random() * 900), cSize + (int)(Math.random() * 900), 
					3, 3, 1, 1, 4, 4 
					));
		}
		Random rand = new Random();
		// create thread with genetic reproduction callback code.
		MyThread myThread  = new MyThread(myPanel, cs-> {
			// genetic reproduction callback code.
			int cSize = cs.size();
			for ( int i = 100 - cSize; i > 0; --i) {
				Critter cr = cs.get(rand.nextInt(cSize));
				Critter cn = new Critter(rand.nextInt(1000), rand.nextInt(1000), cr.xRandMax, cr.yRandMax, cr.xRandOff, cr.yRandOff, cr.xRandLimit, cr.yRandLimit );
				switch(rand.nextInt(6)) {
				case 0:
					cr.xRandMax = Math.max(1, (2 - rand.nextInt(5) + cr.xRandMax));
					break;
				case 1:
					cr.yRandMax = Math.max(1, (2 - rand.nextInt(5) + cr.yRandMax));
					break;
				case 2:
					cr.xRandOff = Math.max(1, (2 - rand.nextInt(5) + cr.xRandOff));
					break;
				case 3:
					cr.yRandOff = Math.max(1, (2 - rand.nextInt(5) + cr.yRandOff));
					break;
				case 4:
					cr.xRandLimit = Math.max(1, (2 - rand.nextInt(5) + cr.xRandLimit));
					break;
				case 5:
					cr.yRandLimit = Math.max(1, (2 - rand.nextInt(5) + cr.yRandLimit));
					break;
				}
				cs.add(cn);
			}
		});
		thread = new Thread(myThread);
		thread.start();
	}

	public static void main(String[] args) {
		final SwingGa swingGa = new SwingGa();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				swingGa.createAndShowGUI();
			}
		});
	}
}