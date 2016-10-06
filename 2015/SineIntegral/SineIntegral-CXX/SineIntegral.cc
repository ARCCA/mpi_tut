// Pull in MPI
#include <mpi.h>

// Include files for PI and min...
#define _USE_MATH_DEFINES
#include <cmath>
#include <algorithm>

// Also let us print to the screen...
#include <iostream>

int main(int argc, char **argv) {
  MPI::Init(argc, argv);
  int nprocs = MPI::COMM_WORLD.Get_size();
  int myrank = MPI::COMM_WORLD.Get_rank();

  int numPoints = 0;
  int receiveIntBuffer[1];

  if (myrank == 0)
    {
      numPoints = 1024;
      receiveIntBuffer[0] = numPoints;
    }
  MPI::COMM_WORLD.Bcast(receiveIntBuffer, 1, MPI::INT, 0);
  numPoints = receiveIntBuffer[0];

  int nlocal = ((numPoints-1) / nprocs) + 1;
  int nbeg = (myrank*nlocal) + 1;
  int nend = std::min(nbeg+nlocal-1,numPoints);

  std::cout << "Hello from " << myrank 
  	    << " of " << nprocs 
	    << "; I'm working on steps " << nbeg << " to " << nend << std::endl;

  double delta = M_PI / numPoints;
  double psum = 0.0;
  for(int i=nbeg; i<=nend; i++)
    {
      psum += (std::sin((i-0.5)*delta))*delta;
    }

  double sendDoubleBuffer[1];
  sendDoubleBuffer[0] = psum;

  double receiveDoubleBuffer[1];

  MPI::COMM_WORLD.Reduce(sendDoubleBuffer,receiveDoubleBuffer,1,MPI::DOUBLE,MPI::SUM,0);

  if (myrank == 0)
    {
      std::cout << std::endl
		<< "Result at processor 0: integral = " 
		<< receiveDoubleBuffer[0] << std::endl;
  }        

  MPI::Finalize();
  return 0;
}
