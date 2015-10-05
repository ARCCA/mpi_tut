import mpi.*;

class LaplaceEquation {
  static public void main(String[] args) {
    MPI.Init(args) ;

	// - //
	
	// Initialise data distribution
    boolean [] periods = new boolean[2];
    boolean reorder = false;
    int myrank = MPI.COMM_WORLD.Rank();
    int nprocs = MPI.COMM_WORLD.Size();
    int [] dims = new int[2];
    MPI.COMM_WORLD.Dims_create(nprocs, dims);
    periods[0] = periods[1] = true;
    Cartcomm comm2d = MPI.COMM_WORLD.Create_cart(dims, periods,
                                                 reorder);
    int [] coords = new int[2];
    coords = comm2d.Coords(myrank);
    ShiftParms vshift = comm2d.Shift(0, 1);
    int up    = vshift.rankSource;
    int down  = vshift.rankDest;
    ShiftParms hshift = comm2d.Shift(1, 1);
    int left  = hshift.rankSource;
    int right = hshift.rankDest;
	
	// - //
	
	// Initialise arrays
    int nlocalx = 100;
    int nlocaly = 100;
    double [] phi = new double[102][102];
    double [] oldphi = new double[102][102];
    boolean [] mask = new boolean[102][102];
    double [] sendbuf = new double[102];
    double [] recvbuf = new double[102];

    for (int j=0;j<=nlocaly+1;j++){
       for (int i=0;i<=nlocalx+1;i++){
          phi[j][i]  = 0.0;
          mask[j][i] = true;
       }
    }
    
    if (coords[0]==0){
       for(int i=0;i<=nlocalx+1;i++){
          mask[1][i] = false;
       }
    }
    if (coords[0]==dims[0]-1){
       for(int i=0;i<=nlocalx+1;i++){
          mask[nlocaly][i] = false;
       }
    }
    if (coords[1]==0){
       for(int j=0;j<=nlocaly+1;j++){
          mask[j][1] = false;
       }
    }
    if (coords[1]==dims[1]-1){
       for(int j=0;j<=nlocaly+1;j++){
          mask[j][nlocalx] = false;
       }
    }  

    // Conducting object…
    if (coords[0]==dims[0]/2-1 && coords[1]==dims[1]/2-1){
       phi[nlocaly][nlocalx] = 1.0;
       mask[nlocaly][nlocalx] = false;
    }
    if (coords[0]==dims[0]/2-1 && coords[1]==dims[1]/2){
       phi[nlocaly][1] = 1.0;
       mask[nlocaly][1] = false;
    }
    if (coords[0]==dims[0]/2 && coords[1]==dims[1]/2-1){
       phi[1][nlocalx] = 1.0;
       mask[1][nlocalx] = false;
    }
    if (coords[0]==dims[0]/2 && coords[1]==dims[1]/2){
       phi[1][1] = 1.0;
       mask[1][1] = false;
    }
	
	// - //
	
	// Update loop; "k" timesteps...
    for(int k=0;k<500;k++){
	
		// Copy current phi to oldphi...
        for(int j=1;j<=nlocaly;j++){
            for(int i=1;i<=nlocalx;i++){
                oldphi[j][i]=phi[j][i];
            }
        }

		// Shift up
		Status s;
		for(int i=1;i<=nlocalx;i++){
		  sendbuf[i] = phi[1][i];
		}

		s = MPI.COMM_WORLD.Sendrecv(
		 sendbuf,1,nlocalx,MPI.DOUBLE,up,99,
		 recvbuf,1,102,MPI.DOUBLE,down,99); 

		for(int i=1;i<=nlocalx;i++){
		  oldphi[nlocaly+1][i] = recvbuf[i];
		}     

		// Shift down
		Status s;
		for(int i=1;i<=nlocalx;i++){
		  sendbuf[i] = phi[nlocaly][i];
		}

		s = MPI.COMM_WORLD.Sendrecv(
		 sendbuf,1,nlocalx,MPI.DOUBLE,down,99,
		 recvbuf,1,102,MPI.DOUBLE,up,99); 

		for(int i=1;i<=nlocalx;i++){
		  oldphi[0][i] = recvbuf[i];
		}     
		
		// Shift right
		Status s;
		for(int j=1;j<=nlocaly;j++){
		  sendbuf[j] = phi[j][nlocalx];
		}

		s = MPI.COMM_WORLD.Sendrecv(
		 sendbuf,1,nlocaly,MPI.DOUBLE,right,99,
		 recvbuf,1,102,MPI.DOUBLE,left,99); 

		for(int j=1;j<=nlocaly;j++){
		  oldphi[j][0] = recvbuf[j];
		}     
		
		// Shift left
		Status s;
		for(int j=1;j<=nlocaly;j++){
		  sendbuf[j] = phi[j][1];
		}

		s = MPI.COMM_WORLD.Sendrecv(
		 sendbuf,1,nlocaly,MPI.DOUBLE,left,99,
		 recvbuf,1,102,MPI.DOUBLE,right,99);

		for(int j=1;j<=nlocaly;j++){
		  oldphi[j][nlocalx+1] = recvbuf[j];
		}     

		// Ripple update to phi from oldphi
		for(int j=1;j<=nlocaly;j++){
			for(int i=1;i<=nlocalx;i++){
				if (mask[j][i]){
					phi[j][i]=0.25*(oldphi[j-1][i]+oldphi[j+1][i]+
									oldphi[j][i-1]+oldphi[j][i+1]);
            }
		}
	}

	// - //
	
	// Output phase...
	
	// - //
	
    MPI.Finalize();
  }
}   
