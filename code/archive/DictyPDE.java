/**
 * @author Richard Hanes
 * 
 */
public class DictyPDE {

	// User Variables
	int l; // Linear World Size
	int n; // Number of Cells in Simulation
	int types; // Number of distinct cell types
	int vol; // Number of CA sites in each cell
	int noOfStates; // Number of times states the cells can be in

	// Administration Variables
	int clock; // Iteration number, to keep track of life cycles
	int flop = 0; // Controls diffusive timesteps

	// Lookup Tables
	int sizeOf[]; // Size Lookup Table
	int typeOf[]; // Cell Type Lookup Table
	int stateOf[][]; // Contains current state and time went into state
	int stateLength[]; // How long each state should last
	int adhesion[][]; // Type Adhesion
	int nLT[][]; // Neighbour Coordinate Lookup Table
	float j[][]; // Adhesion Lookup Table
	int s[][]; // State Field
	float c[][][]; // cAMP Field
	float r[][][]; // Refractoriness Field

	int noise = 3; // Number of sites noise added to each iteration

	// Computational constants
	float aDiff = 0.1f;// cAMP diffusion constant
	float maxcAMPConc = 1.0f; // Maximum cAMP Concentration
	float cThresh = 0.1f; // cAMP Ready->Excited state threshold
	float rThresh = 0.2f;
	float nu = 20.0f; // Chemotaxis Constant
	float theta = 0.005f; // cAMP decay constant
	float t = 1.0f; // Temperature (Mobility)
	float lambda = 1.0f; // Size Lagrange Multiplier
	float cEmitter = 0.1f; // cAMP emission constant

	// cAMP PDE Constants
	// C constants
	float a1 = 20.0f;
	float a2 = 3.0f;
	float a3 = 15.0f;

	// c constants
	float c1 = 0.0065f;
	float c2 = 0.841f;

	float a = 0.15f;
	float d = 1.0f;

	float e1 = 0.5f;
	float e2 = 0.0589f;
	float e3 = 0.5f;

	float k = 3.5f;

	float dL = 0.37f;
	float dT = 0.01f;

	float pComp;

	// Random Number generator (1/3 faster than java.util.random)
	final MersenneTwisterFast generator;

	/**
	 * Constructor for Dicty object
	 */
	public DictyPDE(int l, int n, int types, int vol) {
		this.l = l;
		this.n = n;
		this.types = types;
		this.vol = vol;

		clock = 0;
		noOfStates = 5;

		s = new int[l + 2][l + 2]; // Include space for Boundary
		sizeOf = new int[n + 1]; // Include type for Bare Ground
		typeOf = new int[n + 1];
		adhesion = new int[types + 1][types + 1];
		j = new float[n + 1][n + 1];
		c = new float[l + 2][l + 2][2]; // Extra dimension for flip-flop (time)
		r = new float[l + 2][l + 2][2]; // Extra dimension for flip-flop (time)
		stateOf = new int[n + 1][2];
		stateLength = new int[noOfStates];
		nLT = new int[8][2];
		generator = new MersenneTwisterFast();
		pComp = d / (dL * dL);

		// fillcAMPTestField();
	}

	public void initTest() {

		// Set up the adhesion levels between cell types
		adhesion[0][0] = 0;
		adhesion[1][1] = 3;
		adhesion[2][2] = 3;
		adhesion[3][3] = 4;
		adhesion[1][2] = adhesion[2][1] = 4;
		adhesion[1][3] = adhesion[3][1] = 4;
		adhesion[2][3] = adhesion[3][2] = 4;
		adhesion[1][0] = adhesion[0][1] = 2;
		adhesion[2][0] = adhesion[0][2] = 2;
		adhesion[3][0] = adhesion[0][3] = 2;

		// Set the 'Medium' Type
		typeOf[0] = 0;

		// One cell is started in the auto-cycling state
		typeOf[1] = 1;

		// Of the rest, one fifth are prestalk
		for (int i = 2; i < (n / 5) + 2; i++)
			typeOf[i] = 2;

		// And the remainder are preprespore cells
		for (int i = (n / 5) + 2; i <= n; i++)
			typeOf[i] = 3;

		// Fill the look up table with these values
		fillJ();

		stateLength[0] = 460; // AutoCycling Resting
		stateLength[1] = 20; // AutoCycling Emitting
		stateLength[2] = 10000; // Ready
		stateLength[3] = 50; // Excited
		stateLength[4] = 200; // Refractory

		stateOf[0][0] = -1;

		// Set the autocycling cell state
		stateOf[1][0] = 1;
		stateOf[1][1] = clock;

		// Set the rest of the cells to the ready state
		for (int i = 2; i <= n; i++) {
			stateOf[i][0] = 2;
			stateOf[i][1] = clock;
		}

		// Store the x & y offset of the neighbours in a LUT
		nLT[0][0] = 0;
		nLT[0][1] = -1;
		nLT[1][0] = 1;
		nLT[1][1] = -1;
		nLT[2][0] = 1;
		nLT[2][1] = 0;
		nLT[3][0] = 1;
		nLT[3][1] = 1;
		nLT[4][0] = 0;
		nLT[4][1] = 1;
		nLT[5][0] = -1;
		nLT[5][1] = 1;
		nLT[6][0] = -1;
		nLT[6][1] = 0;
		nLT[7][0] = -1;
		nLT[7][1] = -1;

	}

	// Fill adhesion table
	public void fillJ() {
		for (int i = 0; i <= n; i++) {
			for (int k = 0; k <= n; k++) {

				j[i][k] = adhesion[typeOf[i]][typeOf[k]];

				if (i == k)
					j[i][k] = 0;
			}
		}
	}

	// Used for experiments, fill cAMP with smooth gradient
	public void fillcAMPTestField() {
		for (int i = 1; i < l + 1; i++) {
			for (int j = 1; j < l + 1; j++) {
				c[i][j][flop] = maxcAMPConc * ((float) i / l);
			}
		}
	}

	// Add noise to the cAMP field
	public void fillcAMPTestFieldRandom() {

		int x, y;
		for (int i = 0; i < noise; i++) {
			do {
				x = (int) (l * generator.nextFloat() + 1);
				y = (int) (l * generator.nextFloat() + 1);
			} while (s[x][y] == 0);

			c[x][y][flop] = maxcAMPConc;// * generator.nextFloat();
		}
	}

	// Distribute n amoeba of size v in the state lattice
	public void populateWorld() {
		int x, y, i, neighbour, currentSize, currentSite, crashCounter;
		int[] coords = new int[2 * vol];

		// Loop over cells (State 0 is bareground)
		for (i = 1; i <= n; i++) {

			// Pick Random Initial Location
			do {
				x = (int) (l * generator.nextFloat() + 1);
				y = (int) (l * generator.nextFloat() + 1);
			} while (s[x][y] != 0);

			// Put the Pacesetter in the middle
			if (i == 1) {
				x = l / 2;
				y = l / 2;
			}

			// Set the state field and store the initial location in an array
			s[x][y] = i;
			coords[0] = x;
			coords[1] = y;
			currentSize = 1;
			crashCounter = 0;

			do {
				// Pick a random Site from the array
				currentSite = (int) (generator.nextFloat() * currentSize);

				// Load the coordinates from the array
				x = coords[currentSite * 2];
				y = coords[currentSite * 2 + 1];

				// Pick a random Neighbour (0-N 1-E 2-S 3-W)
				neighbour = (int) (generator.nextFloat() * 4);

				// Modify coordinates to point to Neighbour
				switch (neighbour) {
					case 0 :
						y = y - 1;
						break;
					case 1 :
						x = x + 1;
						break;
					case 2 :
						y = y + 1;
						break;
					case 3 :
						x = x - 1;
						break;
				}

				// Check conditions on selected site and update if passed
				if (s[x][y] == 0 && x > 0 && x < (l + 1) && y > 0
						&& y < (l + 1)) {
					s[x][y] = i;
					coords[currentSize * 2] = x;
					coords[currentSize * 2 + 1] = y;
					currentSize++;
				}
				crashCounter++; // Do not get caught in an infinte loop
			} while ((currentSize < vol) && (crashCounter < (vol * vol)));

			// Update the Size LUT
			sizeOf[i] = currentSize;
		}
		System.out.println("Initialised Population.");
	}

	// Perform one time step
	final public void iterate() {
		int hits, rIndex, x1, y1, x2, y2, neighbour, id1, id2;
		int state, timeInState, state2;
		boolean chemotaxis;
		float k, eB4, eAft, dE, con;
		double rand;

		hits = l * l;
		con = -1.0f / t;
		k = 1.0f / l; // Do one division and multiply later

		// Diffuse cAMP Field
		fillcAMPTestFieldRandom();
		iteratecAMP();

		for (int i = 0; i < hits; i++) {

			// Pick a site in the lattice
			rand = hits * generator.nextDouble();
			rIndex = (int) rand;
			x1 = rIndex % l + 1;
			y1 = (int) (rIndex * k + 1);

			// Pick a random Neighbour (0-N 1-NE 2-E 3-SE 4-S 5-SW 6-W 7-NW)
			rand = 8 * (rand - rIndex); // Reuse decimal part for performance
			neighbour = (int) rand;
			x2 = x1 + nLT[neighbour][0]; // Look up neighbour Coordinates
			y2 = y1 + nLT[neighbour][1];

			// Calculate commly used variables
			id1 = s[x1][y1];
			id2 = s[x2][y2];
			state = stateOf[id1][0];
			state2 = stateOf[id2][0];
			timeInState = clock - stateOf[id1][1];
			chemotaxis = false;

			// In the refractory state
			if (state == 4) {
				// If you've been resting for too long, become ready
				if (timeInState > stateLength[4] && c[x1][y1][flop] < cThresh) {
					stateOf[id1][0] = 2;
					stateOf[id1][1] = clock;
					// System.out.println("Refractory->Ready: " + timeInState);
				}

			}

			// In the excited chemotaxis state
			else if (state == 3) {
				// If you've been excited for too long, rest
				if (timeInState > stateLength[3] && r[x1][y1][flop] > rThresh) {
					stateOf[id1][0] = 4;
					stateOf[id1][1] = clock;
					// System.out.println("Excited->Refractory: " +
					// timeInState);
				} else {
					chemotaxis = true;
				}

			}

			// In the ready state
			else if (state == 2) {
				// If cAMP detected, become excited
				if (c[x1][y1][flop] > cThresh && r[x1][y1][flop] < rThresh) {
					// if (c[x1][y1][flop] > cThresh) {
					stateOf[id1][0] = 3;
					stateOf[id1][1] = clock;
				}
			}

			// Auto Cycling Cells - Resting
			else if ((state == 0) && (timeInState >= stateLength[0])) {
				stateOf[id1][0] = 1;
				stateOf[id1][1] = clock;
			}

			// Auto Cycling Cells - Emitting
			else if (state == 1) {
				if (timeInState >= stateLength[1]) {
					stateOf[id1][0] = 0;
					stateOf[id1][1] = clock;
				} else {
					c[x1][y1][flop] = maxcAMPConc;
				}
			}

			// If id1 or id2 are in excited state, turn on chemotaxis
			if (state2 == 3)
				chemotaxis = true;

			// If the two sites are different
			if (id1 != id2) {

				// Calculate the Hamiltonian of the original site
				eB4 = hamiltonian(x1, y1, id1);

				// Copy the neighbours state onto the original site
				s[x1][y1] = id2;

				// Update the Size of Cell id1 for H calculations LUT
				sizeOf[id1]--;

				// Calculate the Hamiltonian of the modified site
				eAft = hamiltonian(x1, y1, id1);

				// Calculate dE, including the chemotaxis term
				dE = eAft - eB4;
				if (chemotaxis) {
					dE = dE + nu * (c[x2][y2][flop] - c[x1][y1][flop]);
				}

				// If dE positive and expontenial fails, switch back to original
				// Again, reusing the decimal part of the random number
				if (dE >= 0.0 && (Math.exp(con * (dE)) < (rand - neighbour))) {
					s[x1][y1] = id1;
					sizeOf[id1]++;

				} else {
					// Update the size of id2 if sucessful
					sizeOf[id2]++;
				}

			}
		}

		// System.out.println("Current Time: " + clock);
		clock = clock + 1; // Update the system 'time'
	}

	// Calculate the hamiltonian of a site in the lattice
	final float hamiltonian(int x, int y, int id) {
		int current, pressure;
		float h;

		current = s[x][y];

		h = 0.5f * (j[current][s[x][y - 1]] + j[current][s[x + 1][y - 1]]
				+ j[current][s[x + 1][y]] + j[current][s[x + 1][y + 1]]
				+ j[current][s[x][y + 1]] + j[current][s[x - 1][y + 1]]
				+ j[current][s[x - 1][y]] + j[current][s[x - 1][y - 1]]);

		// If the site is part of an amoeba, include volume term
		if (id != 0) {
			pressure = (sizeOf[id] - vol);
			h += lambda * (pressure * pressure);
		}
		return h;
	}

	// Diffuse cAMP to 4 nearest neighbours
	final void firstOrdercAMPDiffuse() {

		float inConst = 1 - 4.0f * aDiff;

		for (int x = 1; x <= l; x++) {
			for (int y = 1; y <= l; y++) {

				c[x][y][1 - flop] = c[x][y][flop]
						* inConst
						+ (c[x][y - 1][flop] + c[x + 1][y][flop]
								+ c[x][y + 1][flop] + c[x - 1][y][flop])
						* aDiff;
			}
		}

		flop = 1 - flop; // Current cAMP field is c[][][flop]
	}

	// Diffuse cAMP to 8 nearest neighbours
	final void secondOrdercAMPDiffuse() {

		float outConst = 1.0f - 6.8284271f * aDiff;
		float d = aDiff * (1.0f - theta);
		float dDiagonal = 0.7071067f * d;

		for (int x = 1; x <= l; x++) {
			for (int y = 1; y <= l; y++) {

				c[x][y][1 - flop] = c[x][y][flop]
						* outConst

						+ d
						* (c[x][y - 1][flop] + c[x + 1][y][flop]
								+ c[x][y + 1][flop] + c[x - 1][y][flop])

						+ dDiagonal
						* (c[x + 1][y - 1][flop] + c[x + 1][y + 1][flop]
								+ c[x - 1][y + 1][flop] + c[x - 1][y - 1][flop]);
			}
		}

		flop = 1 - flop; // Current cAMP field is c[][][flop]
	}

	// Iterate cAMP using FHN Equations
	public void iteratecAMP() {
		float e, f, conc, diff;
		for (int i = 1; i < (l + 1); i++) {
			for (int j = 1; j < (l + 1); j++) {

				conc = c[i][j][flop];

				// Calculate the diffusive part
				diff = c[i - 1][j][flop] + c[i + 1][j][flop]
						+ c[i][j - 1][flop] + c[i][j + 1][flop] - 4 * conc;

				// If we are inside a cell
				if (s[i][j] != 0) {

					// First find the values of e(c) & f(c)
					if (conc < c1) {
						e = e1;
						f = a1 * conc;
					} else if (conc < c2) {
						e = e2;
						f = -1 * a2 * conc + a;
					} else {
						e = e3;
						f = a3 * (conc - 1);
					}
					// Calculate the refractoriness field
					r[i][j][1 - flop] = r[i][j][flop] + dT * e
							* (k * conc - r[i][j][flop]);

					// Calculate the concentration field
					// Calculate the field
					c[i][j][1 - flop] = conc + dT
							* (pComp * diff - f - r[i][j][1 - flop]);
				} else {
					c[i][j][1 - flop] = (conc + dT * pComp * diff)
							* (1 - theta);
					// THIS IS A TEST, what happens to r when cells move???
					// r[i][j][1 - flop] = 0.0f;
				}
			}
		}
		flop = 1 - flop;
		// System.out.println(c[3*l/4][l/2][flop] + "," + r[3*l/4][l/2][flop]);
	}

	/**
	 * Print the current state of the world to the default Output
	 */
	public void dump() {
		int b = s.length;
		for (int i = 1; i < (b - 1); i++) {
			for (int j = 1; j < (b - 1); j++) {
				System.out.print(s[i][j] + " ");
			}
			System.out.print("\n");
		}
		System.out.print("\n");
		b = sizeOf.length;
		for (int i = 1; i < b; i++)
			System.out.println("Size of " + i + ": " + sizeOf[i]);

	}

	/**
	 * Main Method for Dicty object
	 * 
	 * @param args
	 *            Command Line Arguments
	 */
	public static void main(String[] args) {

		DictyPDE b = new DictyPDE(20, 10, 2, 10);
		b.initTest();
		b.populateWorld();

		for (int i = 0; i < 2000; i++)
			b.iterate();

	}
}
