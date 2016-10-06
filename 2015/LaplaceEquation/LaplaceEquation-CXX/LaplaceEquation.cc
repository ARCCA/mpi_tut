// Pull in MPI
#include <mpi.h>

// Include files for PI and min...
#define _USE_MATH_DEFINES
#include <cmath>
#include <algorithm>

// Also let us print to the screen...
#include <iostream>

#include <sstream>

#include <fstream>

void setup_grid (double  **phi, int nptsx, int nptsy, int nprocx, int nprocy, 
int myposx, int myposy, int nlocalx, int nlocaly, int **mask)
{
  int i, j, global_x, global_y;
  
  for(j=0;j<=nlocaly+1;j++)
    for(i=0;i<=nlocalx+1;i++){
      phi[j][i]  = 0.0;
      mask[j][i] = 1;
    }
  
  if (myposy == 0)
    for(i=0;i<=nlocalx+1;i++) mask[1][i] = 0;
  
  if (myposy == nprocy-1)
    for(i=0;i<=nlocalx+1;i++) mask[nlocaly][i] = 0;
  
  if (myposx == 0)
    for(j=0;j<=nlocaly+1;j++) mask[j][1] = 0;
  
  if (myposx == nprocx-1)
    for(j=0;j<=nlocaly+1;j++) mask[j][nlocalx] = 0;
  
  for(j=1;j<=nlocaly;j++){
    global_y = nlocaly*myposy + j - 1;
    if (global_y == nptsy/2 || global_y == nptsy/2-1){
      for(i=1;i<=nlocalx;i++){
	global_x = nlocalx*myposx + i - 1;
	if (global_x == nptsx/2 || global_x == nptsx/2-1){
	  mask[j][i] = 0;
	  phi[j][i]  = 1.0;
	}
      }
    }
  }
}

/**
 * Outputs the data; all data sent to rank #0, which then outputs to disc. 
 * Arguments as follows:
 * double **phi = 2D array of sample points, stored in the form [y][x]
 * int rank = number of this process; rank==0 is used as "root" process to collect data for output
 * int nprocx = number of processes in the X direction (dimension 1)
 * int nprocy = number of processes in the Y direction (dimension 0)
 *
 * MPI_Comm new_commn = the MPI communications channel used (mapped to the cartesian topology)
 */
void output_array (double **phi, int rank, int nprocx, int nprocy, 
	int nlocalx, int nlocaly, int nprocs, 
    int myposx, int myposy, int nptsx, int nptsy, 
	MPI::Cartcomm &new_comm)
//	MPI_Comm new_comm)
{
  int i, j, m, n;
  int jmax, nsizey, count, source;
  int coords[2];
  MPI::Status status;
  std::ofstream outputFile;
  
  
  if(rank==0){
    outputFile.open("output.txt");
    outputFile << "num points in x = " << nptsx << std::endl;
    outputFile << "num points in y = " << nptsy << std::endl;
    outputFile << "num processors in x = " << nprocx << std::endl;
    outputFile << "num processors in y = " << nprocy << std::endl;
  }
  
  nsizey = (nptsy-1)/nprocy + 1;
  // Iterate over processes in Y direction;
  //
  for(m=0;m<nprocy;m++)
  {
	// Iterate over the Y point allocation... note correction of Y allocation
	// on the process at the edge (last process) - rounding of Y allocation
	// may result in fewer points allocated, so need to cope with this...
    jmax = (m==nprocy-1) ? nptsy - nsizey*(nprocy-1) : nsizey;
	outputFile << "max y = " << jmax << std::endl;

    for(j=1;j<=jmax;j++)
	{
	  // Iterate over processes in X direction...
      for(n=0;n<nprocx;n++)
	  {
		// If the process co-ordinate (n,m) matches our cartesian position (myposx, myposy)
		// then its our process; time to do something...
	    if (myposx == n && myposy == m)
		{
		  // We're not the root process - so send the data to root for processing
		  if (rank!=0)
	      {
//            MPI_Send (&phi[j][1], nlocalx, MPI_DOUBLE, 0, 115,MPI_COMM_WORLD);
            MPI::COMM_WORLD.Send (&phi[j][1], nlocalx, MPI_DOUBLE, 0, 115);
	      }
	      else
          {
            // Rank #0
	        // We are actually process rank 0, so we must have the data - just need to output to disc	  
            outputFile << "processor x = " << n << std::endl;
            outputFile << "processor y = " << m << std::endl;
		    outputFile << "max x = " << nlocalx << std::endl; 
			
	        for(i=1; i<=nlocalx; i++)
		    {
		      outputFile << "phi[" << j << "][" << i << "]=" << phi[j][i] << std::endl;
	        }
	      }
	    }
		// However, if the coordinates don't match ours, the root process still has to collect
		// the data and write it to disc...
	    else if (rank==0) 
	    {
		  // We're the root process, so we need to receive data & write to disc...
		  
		  // Convert the (m,n) coordinate to a process number...
    	  coords[0] = m;
	      coords[1] = n;
	      // MPI_Cart_rank (new_comm, coords, &source);
	      // MPI_Recv (&phi[0][1], nlocalx, MPI_DOUBLE, source, 115, MPI_COMM_WORLD, &status);
	      // MPI_Get_count(&status, MPI_DOUBLE, &count);
	      source = new_comm.Get_cart_rank(coords);
		  
		  // We can now read from that process, as we expect it to have sent its results over MPI...
	      MPI::COMM_WORLD.Recv (&phi[0][1], nlocalx, MPI_DOUBLE, source, 115, status);
		  
		  // Take a note of how much data was actually sent - it may be less than naively expected...
	      count = status.Get_count(MPI_DOUBLE);
		  
	      // need to check number received here - we may get less on the edge of the array
		  // as we can't send fractional numbers of samples, so if the number of processes
		  // doesn't divide evenly into the number of sample points, we will have a rounding
		  // off of the numbers - resulting in fewer samples at the last edge (rounding error
		  // is collected here by giving a smaller allocation).
		  
          outputFile << "processor x = " << n << std::endl;
          outputFile << "processor y = " << m << std::endl;
		  outputFile << "max x = " << count << std::endl; 
  	  
	      for(i=1; i<=count; i++)
	      {
	        outputFile << "phi[" << j << "][" << i << "]=" << phi[0][i] << std::endl;
	      }
	    }
      }
    }
  }
  
  if(rank==0) outputFile.close();
}

int main (int argc, char *argv[])
{
  double *sbuf, *rbuf;
  double **phi, **oldphi;
  int    **mask;
  int    i, j, k;
  int    nptsx = 200, nptsy = 200;
  int    nsteps  = 500;
  int    dims[2], coords[2];
  bool   periods[2];
  int    myposx, myposy, nprocx, nprocy;
  int    up, down, left, right;
  int    bufsize, nsizex, nsizey, nlocalx, nlocaly;
  
  /* Initialise and find rank and number of processes */
  MPI::Init(argc, argv);
  int nprocs = MPI::COMM_WORLD.Get_size();
  int rank = MPI::COMM_WORLD.Get_rank();
  
  /* Work out number of processes in each direction of the process mesh */
  dims[0] = dims[1] = 0;
  MPI::Compute_dims(nprocs, 2, dims);
  nprocy = dims[0];
  nprocx = dims[1];
  
  /* Set up 2D topology */
  periods[0] = periods[1] = false;
  MPI::Cartcomm new_comm = MPI::COMM_WORLD.Create_cart(2, dims, periods, true);
  new_comm.Get_coords(rank, 2, coords);
  myposy = coords[0];
  myposx = coords[1];
  
  /* Determine neighbouring processes for communication shifts */
  new_comm.Shift(0,-1, up, down);
  new_comm.Shift(1,-1, right, left);
  
  /* Initialise arrays */
  nsizex = (nptsx-1)/nprocx + 1;
  nsizey = (nptsy-1)/nprocy + 1;
  bufsize = (nsizex>nsizey) ? nsizex : nsizey;
  sbuf = new double[bufsize];
  rbuf = new double[bufsize];
  phi = new double*[nsizey+2];
  oldphi = new double*[nsizey+2];
  mask = new int*[nsizey+2];
  for (k=0;k<nsizey+2;k++){
    phi[k] = new double[nsizex+2];
    oldphi[k] = new double[nsizex+2];
    mask[k] = new int[nsizex+2];
  }
  nlocalx = (myposx==nprocx-1) ? nptsx-nsizex*(nprocx-1) : nsizex;
  nlocaly = (myposy==nprocy-1) ? nptsy-nsizey*(nprocy-1) : nsizey;
  
  std::cout 
    << "rank = " << rank
    << " (myposx,myposy)=(" << myposx << "," << myposy
    << "), (left,right,down,up)=(" << left << "," << right << "," << down << "," << up
    << "), (nlocalx,nlocaly)=(" << nlocalx << "," << nlocaly << ")" 
    << std::endl;
  
  setup_grid (phi, nptsx, nptsy, nprocx, nprocy, myposx, myposy, nlocalx, nlocaly, mask);
  
  /* Iterate to find solution */
  for(k=1;k<=nsteps;k++)
    {
      for(j=1;j<=nlocaly;j++)
	{
	  for(i=1;i<=nlocalx;i++)
	    {
	      oldphi[j][i] = phi[j][i];
	    }
	}
	
	  // Sending data up/down aligns with data storage;
	  // i.e. data elements in a row are stored contiguously,
	  // so they can be sent with a single send/recv call
      
	  // Send data to "down", receive from "up"...
      new_comm.Sendrecv (
			 &oldphi[1][1], nlocalx, MPI_DOUBLE, down, 111,
			 &oldphi[nlocaly+1][1], nlocalx, MPI_DOUBLE, up, 111);
			 
	  // Send data to "up", receive from "down"...
      new_comm.Sendrecv (
			 &oldphi[nlocaly][1], nlocalx, MPI_DOUBLE, up, 112,
			 &oldphi[0][1], nlocalx, MPI_DOUBLE, down, 112);
			 
	  // Sending data left/right needs to pick a elements that
	  // are not stored contiguously - so they need to be copied
	  // to contiguous space (a buffer) where they can then be
	  // sent/received; the buffer is then used to copy the data
	  // back in the array of sample points.
	  
	  // Copy data to buffer, send to "left", receive from "right",
	  // copy data back from buffer to array...
      for(i=1;i<=nlocaly;i++) sbuf[i-1] = oldphi[i][1];
      new_comm.Sendrecv (
			 sbuf, nlocaly, MPI_DOUBLE, left, 113,
			 rbuf, nlocaly, MPI_DOUBLE, right, 113);      
      for(i=1;i<=nlocaly;i++) oldphi[i][nlocalx+1] = rbuf[i-1];   
	  
	  // Copy data to buffer, send to "right", receive from "left",
	  // copy data back from buffer to array...
      for(i=1;i<=nlocaly;i++) sbuf[i-1] = oldphi[i][nlocalx];
      new_comm.Sendrecv (
			 sbuf, nlocaly, MPI_DOUBLE, right, 114,
			 rbuf, nlocaly, MPI_DOUBLE, left, 114);			 
      for(i=1;i<=nlocaly;i++) oldphi[i][0] = rbuf[i-1];
	  
	  // Loop across all elements in the array, calculating the new
	  // value (phi holds new values; oldphi has previous values,
	  // including those just updated from left/right/up/down
	  // neighbours)
      for(j=1;j<=nlocaly;j++)
		for(i=1;i<=nlocalx;i++)
			if (mask[j][i]) phi[j][i] = 0.25*(oldphi[j][i-1] +
					    oldphi[j][i+1] + oldphi[j-1][i] + oldphi[j+1][i]);
    }
	
  // All iterations complete, so now everyone outputs their results.
  // This sends all the data back to the root process (we'll use rank #0)
  // which then collects all results and outputs to disc.
  output_array (phi, rank, nprocx, nprocy, nlocalx, nlocaly, nprocs , myposx, myposy, nptsx, nptsy, new_comm);
  
  MPI_Finalize();
  return 0;
}
