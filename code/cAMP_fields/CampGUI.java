import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class CampGUI {

	CampField k;
	int l = 100;
	int wdd, hdd;
	int boxSize = 2;

	JPanel mainPanel;
	JcAMPPanel cAMPPanel;

	public CampGUI() {

		k = new CampField(l);
		wdd = k.l * boxSize;
		hdd = k.l * boxSize;

		mainPanel = new JPanel();
		cAMPPanel = new JcAMPPanel();
		cAMPPanel.setPreferredSize(new Dimension(wdd + 1, hdd + 1));
		mainPanel.add(cAMPPanel, BorderLayout.CENTER);
	}

	public void go() {
		k.iterate();
		drawBufferedcAMP();
	}

	public void drawBufferedcAMP() {

		int x;
		int y;
		int l = k.l;
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

	class JcAMPPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		public JcAMPPanel() {
		}

		public void paint(Graphics g) {
			int x;
			int y;
			int l = k.l;

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

		float value = k.c[x][y][k.flop];
		float maxcAMP = 1.0f;

		if (value < 0.0f) {
			value = 0.0f;
		}

		else if (value > maxcAMP) {
			value = maxcAMP;
		}

		value = 1.0f - (value / maxcAMP);

		return new Color(value, value, value);

	}

	public static void main(String[] args) {
		// Create a new instance of the Ising GUI
		CampGUI mProj = new CampGUI();

		// Create and set up the window.
		JFrame cAMPFrame = new JFrame("Dicty Model");
		cAMPFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		cAMPFrame.setContentPane(mProj.mainPanel);

		// Display the window.
		cAMPFrame.pack();
		cAMPFrame.setVisible(true);
		//System.out.println("Simulation Starting...");
		for (int i = 0; i < 15000; i++){
			mProj.k.perturb();
			mProj.go();
		}
		//mProj.k.dump();
		//System.out.println("...Simulation Done");
		

	}
}
