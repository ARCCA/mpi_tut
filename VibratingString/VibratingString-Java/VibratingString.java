import mpi.*

class VibratingString {
  static public void main(String[] args) {
    MPI.Init(args) ;

	// - //
	
	// Initialise data distribution
	
	boolean [] periods = new boolean[1];
    boolean reorder = false;
    int myrank = MPI.COMM_WORLD.Rank();
    int nprocs = MPI.COMM_WORLD.Size();
    int [] dims = new int[1];
    dims[0] = nprocs;
    periods[0] = false;
    Cartcomm comm1d = MPI.COMM_WORLD.Create_cart(dims, periods,
                                                 reorder);
    int [] coords = new int[1];
    coords = comm1d.Coords(myrank);
    ShiftParms shift1d = comm1d.Shift(0, 1);
    int left  = shift1d.rankSource;
    int right = shift1d.rankDest;

	// - //
	
	// Initialise arrays
    int npoints = 100;
    double psi = new double[102];
    double oldpsi = new double[102];
    double newpsi = new double[102];
    int nlocal = npoints/nprocs;
    int nstart = coords[0]*nlocal;
    double x;
    for(int i=0;i<nlocal;i++){
        x = 2.0*Math.PI*(double)(nstart+i)/(double)(npoints-1);
        x = Math.sin(x);
        psi[i+1] = oldpsi[i+1] = x;
    }
	
	// - //
	
	// Update loop
    double tau = 0.05;
    int start = 1;
    if (coords[0]==0) start = 2;
    int end = nlocal;
    if (coords[0]==nprocs-1) end = nlocal-1;
    Status s;
    for(int j=0;j<500;j++){
      s = MPI.COMM_WORLD.Sendrecv(psi,1,1,MPI.DOUBLE,left,123,  
                           psi,nlocal+1,1,MPI.DOUBLE,right,123);
      s = MPI.COMM_WORLD.Sendrecv(psi,nlocal,1,MPI.DOUBLE,right,123,  
                                       psi,0,1,MPI.DOUBLE,left,123);
      for(int i=start;i<=end;i++){
        newpsi[i] = 2.0*psi[i]-oldpsi[i]+
                      tau*tau*(psi[i-1]-2.0*psi[i]+psi[i+1]);
      }
      for(int i=1;i<=nlocal;i++){
        oldpsi[i] = psi[i];
        psi[i] = newpsi[i];
      }
    }
	
	// - //
	
	// Output phase...
	
	// - //
	
    MPI.Finalize();
  }
}   
