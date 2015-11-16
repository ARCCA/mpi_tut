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

int main(int argc, char **argv) {
  // Initialise MPI...
  MPI::Init(argc, argv);

  // - //

  // Initialise data distribution
  bool periods[1];
  bool reorder = false;
  int myrank = MPI::COMM_WORLD.Get_rank();
  int nprocs = MPI::COMM_WORLD.Get_size();
  int dims[1];
  dims[0] = nprocs;
  periods[0] = false;
  MPI::Cartcomm comm1d = MPI::COMM_WORLD.Create_cart(1, dims, periods, reorder);
  int coords[1];
  comm1d.Get_coords(myrank, 1, coords);
  std::cout << "proc#" << myrank << " coords[0]=" << coords[0] << std::endl;

  int left = 0; // rank source; initialised to prevent warning
  int right = 0; // rank dest; initialised to prevent warning
  comm1d.Shift(0, 1, left, right);
  std::cout << "proc#" << myrank << " left=" << left << " right=" << right << std::endl;
  
  // - //

  // Initialising arrays
  int npoints = 100;
  int nlocal = npoints/nprocs;
  int nstart = coords[0]*nlocal;

  double *psi = new double[nlocal+2];
  double *oldpsi = new double[nlocal+2];
  double *newpsi = new double[nlocal+2];
  for (int i=0; i<nlocal+2; i++)
  {
    psi[i] = oldpsi[i] = newpsi[i] = 0.0;
  }

  double x;

  // Calculate the initial conditions for our string.
  for(int i=0; i<nlocal; i++)
  {
    x = 2.0*M_PI*(double)(nstart+i)/(double)(npoints-1);
    x = std::sin(x);
    psi[i+1] = oldpsi[i+1] = x;
  }

  // - //

  // Update loop
  double tau = 0.05;
  int start = 1;
  if (coords[0]==0) start = 2;
  int end = nlocal;
  if (coords[0]==nprocs-1) end = nlocal-1;
  MPI::Status s;

  // Output from each node goes direct to a local file...
  // Each process writes to its own output file
  std::stringstream ss;
  ss << "output-" << myrank << ".txt";
  std::ofstream outputFile;
  outputFile.open(ss.str().c_str());
  outputFile << "Rank=" << myrank << std::endl;
  outputFile << "Numprocs=" << nprocs << std::endl;
  outputFile << "Numpoints=" << npoints << std::endl;
  outputFile << "start=" << start << std::endl;
  outputFile << "end=" << end << std::endl;

  for(int j=0; j<1500; j++)	// Iterations of time steps...
  {
    // Pass data to and from the nearest neighbours.
    MPI::COMM_WORLD.Sendrecv(
	    &psi[1], 1, MPI::DOUBLE, left, 123,  
	    &psi[nlocal+1], 1, MPI::DOUBLE, right, 123, s);

    MPI::COMM_WORLD.Sendrecv(
	    &psi[nlocal], 1, MPI::DOUBLE, right, 123,  
	    &psi[0], 1, MPI::DOUBLE, left, 123, s);

    // Calculate new string position at each point from known positions
    // at t and (t - delta t) using finite difference method.
    for(int i=start;i<=end;i++)
      {
        newpsi[i] 
	  = (2.0 * psi[i]) 
	  - oldpsi[i]
	  + (tau * tau * (psi[i-1] - (2.0 * psi[i]) + psi[i+1]));
      }

    // Update position arrays.
    for(int i=1;i<=nlocal;i++)
      {
	    oldpsi[i] = psi[i];
	    psi[i] = newpsi[i];
      }

    // Output position of string at each point periodically within the
    // timestep loop.
    if ( (j % 50) == 0)
      {
	for(int i=start;i<=end;i++){
	  outputFile << "x[" << i << "] = " << psi[i] << std::endl;
	}
      }
  }

  // - //

  // Output phase
  outputFile.close();

  // - //

  // Shutdown...
  delete[] psi;
  delete[] oldpsi;
  delete[] newpsi;
  MPI::Finalize();
  return 0;
}
