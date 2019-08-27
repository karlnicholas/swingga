package swingga;

import java.awt.*;

import javax.swing.*;

/**
 * 
 * Main UI class
 *
 */
public class SwingGa extends JFrame {
	private static final long serialVersionUID = 1L;
	private Thread thread;
	private SimulationThread simulationThread;

	public SwingGa() {
		super("SwingGa");
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
	    	Graphics2D g2d = (Graphics2D)  g;
	    	simulationThread.drawScreenItems(g2d);
	    	g2d.setFont( new Font(Font.DIALOG,  Font.BOLD, 20) );
	    	g2d.setColor(Color.BLACK);
	    	g2d.drawString("Hunter", 300, 30);
	    	g2d.setColor(Color.WHITE);
	    	g2d.fillRect(375, 10, 50, 25);
	    	g2d.setColor(Color.BLACK);
	    	g2d.drawString(Integer.toString( simulationThread.screenItems.hunterCritters.size()), 375, 30);
	    	g2d.setColor(Color.WHITE);
	    	g2d.fillRect(575, 10, 50, 25);
	    	g2d.setColor(Color.BLACK);
	    	g2d.drawString("Gather", 500, 30);
	    	g2d.drawString(Integer.toString( simulationThread.screenItems.gatheringCritters.size()), 575, 30);
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

		MyPanel myPanel = new MyPanel();
        add(myPanel);
        
		// Display the window.
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		// Create and set up the window.

		// create thread with genetic reproduction callback code.
		simulationThread = new SimulationThread(myPanel);
		thread = new Thread(simulationThread);
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