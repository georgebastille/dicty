import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DictyPDEGUI {

	JPanel mainPanel;
	JButton pauseB;

	JCellPanel cellPanel;

	JcAMPPanel cAMPPanel;

	JSlider animation;
	
	JPanel tempPanel;
	JSlider tempSlider;
	JLabel tempTitle;
	JLabel tempValue;
	
	JPanel diffPanel;
	JSlider diffSlider;
	JLabel diffTitle;
	JLabel diffValue;
	
	JPanel cThrPanel;
	JSlider cThrSlider;
	JLabel cThrTitle;
	JLabel cThrValue;

	JPanel taxiPanel;
	JSlider taxiSlider;
	JLabel taxiTitle;
	JLabel taxiValue;

	JPanel cDecPanel;
	JSlider cDecSlider;
	JLabel cDecTitle;
	JLabel cDecValue;
	
	JFrame controlFrame;

	DictyPDE b;

	int wdd, hdd;

	int boxSize = 3;

	int l = 120;

	int n = 540;

	int types = 3;

	int vol = 16;

	int itsPerFrame = 1;
	
	int running = 0;

	public DictyPDEGUI() {
		b = new DictyPDE(l, n, types, vol);
		b.initTest();
		b.populateWorld();

		wdd = b.l * boxSize;
		hdd = b.l * boxSize;

		mainPanel = new JPanel();
		cellPanel = new JCellPanel();
		cAMPPanel = new JcAMPPanel();
		controlFrame = new JFrame("Dicty Controls");
		controlFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		controlFrame.setLocation(wdd, hdd + 40);
		animation = new JSlider(JSlider.VERTICAL, 1, 50, itsPerFrame);

		animation.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				itsPerFrame = animation.getValue();
			}
		});
		
		pauseB = new JButton("Pause");
		pauseB.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				running = 1 - running;
			}
		});


		animation.setPreferredSize(new Dimension(20, hdd));
		cellPanel.setPreferredSize(new Dimension(wdd + 1, hdd + 1));
		cAMPPanel.setPreferredSize(new Dimension(wdd + 1, hdd + 1));
		
		makeControls();

		mainPanel.add(cellPanel, BorderLayout.EAST);
		mainPanel.add(animation, BorderLayout.CENTER);
		mainPanel.add(cAMPPanel, BorderLayout.WEST);
		controlFrame.setVisible(true);
	}
	
	public void makeControls(){
		tempPanel = new JPanel(new BorderLayout());
		tempSlider = new JSlider(JSlider.VERTICAL, 0, 10, (int)b.t);
		tempSlider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg0) {
				b.t = (float) tempSlider.getValue();
				tempValue.setText(""+b.t+"");
			}});
		tempTitle = new JLabel("Mobility",0); // 0 means centred
		tempValue = new JLabel(""+b.t+"",0);
		
		tempPanel.add(tempTitle, BorderLayout.NORTH);
		tempPanel.add(tempValue, BorderLayout.SOUTH);
		tempPanel.add(tempSlider, BorderLayout.CENTER);
		
		
		
		diffPanel = new JPanel(new BorderLayout());
		diffSlider = new JSlider(JSlider.VERTICAL, 0, 14, (int)(b.aDiff*100));
		diffSlider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg0) {
				b.aDiff = (float) (diffSlider.getValue()/100.0);
				diffValue.setText(""+(b.aDiff)+"");
			}});
		diffTitle = new JLabel("cAMP Diff",0); // 0 means centred
		diffValue = new JLabel(""+(b.aDiff)+"",0);
		
		diffPanel.add(diffTitle, BorderLayout.NORTH);
		diffPanel.add(diffValue, BorderLayout.SOUTH);
		diffPanel.add(diffSlider, BorderLayout.CENTER);
		
		
		
		cThrPanel = new JPanel(new BorderLayout());
		cThrSlider = new JSlider(JSlider.VERTICAL, 0, 20, (int)(b.cThresh*20));
		cThrSlider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg0) {
				b.cThresh = (float) (cThrSlider.getValue()/20.0);
				cThrValue.setText(""+(b.cThresh)+"");
			}});
		cThrTitle = new JLabel("cAMP Thrs",0); // 0 means centred
		cThrValue = new JLabel(""+(b.cThresh)+"",0);
		
		cThrPanel.add(cThrTitle, BorderLayout.NORTH);
		cThrPanel.add(cThrValue, BorderLayout.SOUTH);
		cThrPanel.add(cThrSlider, BorderLayout.CENTER);
		
		taxiPanel = new JPanel(new BorderLayout());
		taxiSlider = new JSlider(JSlider.VERTICAL, 0, 50, (int)b.nu);
		taxiSlider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg0) {
				b.nu = (float) (taxiSlider.getValue());
				taxiValue.setText(""+(b.nu)+"");
			}});
		taxiTitle = new JLabel("Chemotaxis",0); // 0 means centred
		taxiValue = new JLabel(""+(b.nu)+"",0);
		
		taxiPanel.add(taxiTitle, BorderLayout.NORTH);
		taxiPanel.add(taxiValue, BorderLayout.SOUTH);
		taxiPanel.add(taxiSlider, BorderLayout.CENTER);
		
		
		cDecPanel = new JPanel(new BorderLayout());
		cDecSlider = new JSlider(JSlider.VERTICAL, 0, 20, (int)(b.theta*1000));
		cDecSlider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent arg0) {
				b.theta = (float) (cDecSlider.getValue()/1000.0);
				cDecValue.setText(""+(b.theta)+"");
			}});
		cDecTitle = new JLabel("cAMP Decay",0); // 0 means centred
		cDecValue = new JLabel(""+(b.theta)+"",0);
		
		cDecPanel.add(cDecTitle, BorderLayout.NORTH);
		cDecPanel.add(cDecValue, BorderLayout.SOUTH);
		cDecPanel.add(cDecSlider, BorderLayout.CENTER);
		
		controlFrame.setLayout(new GridLayout());
		controlFrame.add(tempPanel);
		controlFrame.add(diffPanel);
		controlFrame.add(cThrPanel);
		controlFrame.add(taxiPanel);
		controlFrame.add(cDecPanel);
		controlFrame.add(pauseB);
		
		controlFrame.pack();
		
		
	
	}

	public void drawBufferedCells() {
		int x;
		int y;
		int l = b.l;
		int bs;

		Graphics2D gdt, bigdt;
		BufferedImage bi;

		gdt = (Graphics2D) cellPanel.getGraphics();
		bi = new BufferedImage(wdd, hdd, BufferedImage.TYPE_INT_RGB);
		bigdt = bi.createGraphics();

		// Fill the background in white
		bigdt.setColor(Color.white);
		bigdt.fillRect(0, 0, wdd, hdd);

		// Colour the dark cells
		for (x = 1; x < (l + 1); x++) {
			for (y = 1; y < (l + 1); y++) {

				bigdt.setColor(cellColor(x,y));
				bigdt.fillRect((x - 1) * boxSize, (y - 1) * boxSize,
						boxSize, boxSize);
			}
		}

		// Draw the border
		bigdt.setColor(Color.black);
		bigdt.drawRect(0, 0, wdd, hdd);

		// Draw the Cell Boundarys
		for (x = 1; x < (l + 1); x++) {
			for (y = 1; y < (l + 1); y++) {

				bs = b.s[x][y];

				// Horizontal Lines
				if (bs != b.s[x][y + 1]) {
					bigdt.drawLine((x - 1) * boxSize, y * boxSize, x * boxSize,
							y * boxSize);
				}
				// Vertical Lines
				if (bs != b.s[x + 1][y]) {
					bigdt.drawLine(x * boxSize, (y - 1) * boxSize, x * boxSize,
							y * boxSize);
				}
			}
		}

		// And paint the image to screen.

		gdt.drawImage(bi, null, 0, 0);

		gdt.dispose();
	}

	public void drawBufferedcAMP() {

		int x;
		int y;
		int l = b.l;
		Graphics2D gdt, bigdt;
		BufferedImage bi;

		gdt = (Graphics2D) cAMPPanel.getGraphics();
		bi = new BufferedImage(wdd, hdd, BufferedImage.TYPE_INT_RGB);
		bigdt = bi.createGraphics();

		// Fill the background in white
		bigdt.setColor(Color.white);
		bigdt.fillRect(0, 0, wdd, hdd);

		// Draw the cAMP Field
		for (x = 1; x < (l + 1); x++) {
			for (y = 1; y < (l + 1); y++) {
				bigdt.setColor(cAMPColor(x, y));
				bigdt.fillRect((x - 1) * boxSize, (y - 1) * boxSize, boxSize,
						boxSize);

			}
		}

		// Draw the border
		bigdt.setColor(Color.black);
		bigdt.drawRect(0, 0, wdd, hdd);

		gdt.drawImage(bi, null, 0, 0);

		gdt.dispose();

	}
	
	private void go() {
		

		int i = 0;
		while(true){
			if (running == 1){

			b.iterate();

			if (i % itsPerFrame == 0) {

				drawBufferedCells();
				drawBufferedcAMP();

			}
			i++;
		} else {
		 try {Thread.sleep(5);} catch (InterruptedException e) {}
		}
		
	}
	}


	class JCellPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		public JCellPanel() {
		}

		public void paint(Graphics g) {
			int x;
			int y;
			int l = b.l;
			int bs;

			// Fill the background in white
			g.setColor(Color.white);
			g.fillRect(0, 0, wdd, hdd);

			// Colour the dark cells
			for (x = 1; x < (l + 1); x++) {
				for (y = 1; y < (l + 1); y++) {
					
					g.setColor(cellColor(x,y));
					g.fillRect((x - 1) * boxSize, (y - 1) * boxSize,
							boxSize, boxSize);
				
				}
			}

			// Draw the border
			g.setColor(Color.black);
			g.drawRect(0, 0, wdd, hdd);

			for (x = 1; x < (l + 1); x++) {
				for (y = 1; y < (l + 1); y++) {

					bs = b.s[x][y];

					// Horizontal Lines
					if (bs != b.s[x][y + 1]) {
						g.drawLine((x - 1) * boxSize, y * boxSize, x * boxSize,
								y * boxSize);
					}
					// Vertical Lines
					if (bs != b.s[x + 1][y]) {
						g.drawLine(x * boxSize, (y - 1) * boxSize, x * boxSize,
								y * boxSize);
					}
				}
			}
		}
	}

	class JcAMPPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		public JcAMPPanel() {
		}

		public void paint(Graphics g) {
			int x;
			int y;
			int l = b.l;

			// Fill the background in white
			g.setColor(Color.white);
			g.fillRect(0, 0, wdd, hdd);

			// Draw the cAMP Field
			for (x = 1; x < (l + 1); x++) {
				for (y = 1; y < (l + 1); y++) {
					g.setColor(cAMPColor(x, y));
					g.fillRect((x - 1) * boxSize, (y - 1) * boxSize, boxSize,
							boxSize);

				}
			}

			// Draw the border
			g.setColor(Color.black);
			g.drawRect(0, 0, wdd, hdd);
		}
	}

	public Color cAMPColor(int x, int y) {

		float value = b.c[x][y][b.flop];
		float maxcAMP = b.maxcAMPConc;

		if (value < 0.0f) {
			value = 0.0f;
		}

		else if (value > maxcAMP) {
			value = maxcAMP;
		}

		value = 1.0f - (value / maxcAMP);

		return new Color(value, value, value);

	}

	public Color cellColor(int x, int y) {

		int type = b.typeOf[b.s[x][y]];
		if (type == 3)
			return Color.yellow;
		else if (type == 2)
			return Color.green;
		else if (type == 0)
			return Color.white;
		else
			return Color.red;
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Create a new instance of the Ising GUI
		DictyPDEGUI mProj = new DictyPDEGUI();

		// Create and set up the window.
		JFrame dictyFrame = new JFrame("Dicty Model");
		dictyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		dictyFrame.setContentPane(mProj.mainPanel);

		// Display the window.
		dictyFrame.pack();
		dictyFrame.setVisible(true);

		mProj.go();

	}
}
