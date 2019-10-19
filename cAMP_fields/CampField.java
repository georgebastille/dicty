public class CampField {

	float[][][] c;
	float[][][] r;

	int l;

	int flop = 0;

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
	
	int clock;
	
	int x1, y1, x2, y2; // coordinates for speed measurement
	boolean start, finish;
	int startTime, finishTime;

	public CampField(int l) {
		this.l = l;
		c = new float[l + 2][l + 2][2];
		r = new float[l + 2][l + 2][2];
		pComp = d /(dL*dL);
		clock = 0;
		x1 = (int)(3.0 * l / 4.0);
		x2 = l;
		y1 = y2 = (int)((double)l / 2.0);
		start = finish = false;
	}

	public void iterate() {
		float e, f, conc, diff;
		for (int i = 1; i < (l + 1); i++) {
			for (int j = 1; j < (l + 1); j++) {

				conc = c[i][j][flop];

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
				// Calculate the diffusive part
				diff = c[i-1][j][flop] + c[i+1][j][flop] +
				       c[i][j-1][flop] + c[i][j+1][flop] -
				       4 * conc;
				// Calculate the field
				// Turn off special features to check diffusion
				c[i][j][1-flop] = conc + dT*(pComp*diff - f - r[i][j][1-flop]);
				// c[i][j][1-flop] = conc + dT*d*diff/(dL*dL);
			}
		}
		flop = 1-flop;
		clock = clock + 1;
		speed();
		// System.out.println(c[3*l/4][l/2][flop] + "," + r[3*l/4][l/2][flop]);
	}
	
	public void perturb(){
		c[l/2+1][l/2+1][flop] = 1.0f;
		// This line is a test of seeding the wave at the boundary
		//c[5][l/2+1][flop] = 1.0f;
	}
	
	public void speed(){
		if (start == false && c[x1][y1][flop] > 0.2)
		{
			start = true;
			startTime = clock;
		}
		
		if (finish == false && c[x2][y2][flop] > 0.2)
		{
			finish = true;
			finishTime = clock;
			System.out.println("WaveSpeed: " + (double)(x2-x1) / (double)(finishTime - startTime));
		}
	}
	
	public void dump(){
		for (int i =1; i<(l+1); i++)
		System.out.println(c[i][l/2][flop] + ", " + r[i][l/2][flop]);
	
	}

}
