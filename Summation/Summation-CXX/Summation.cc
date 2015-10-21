// Pull in MPI
#include <mpi.h>

// Include files for PI and min...
#define _USE_MATH_DEFINES
#include <cmath>
#include <algorithm>

// Also let us print to the screen...
#include <iostream>

void master(double *dataArray, int dataSize)
{
  int nprocs = MPI::COMM_WORLD.Get_size();

  int localSize = dataSize / (nprocs-1); // Assumes exact division no remainder

  for (int rank=1; rank<nprocs; rank++)
    {
      // Inform slave node amount of data to expect...
      MPI::COMM_WORLD.Send(&localSize, 1, MPI_INTEGER, rank, 0);

      // ...then send it
      MPI::COMM_WORLD.Send(&dataArray[(rank-1)*localSize], localSize, MPI::DOUBLE, rank, 1);
    }

  double sendDoubleBuffer = 0.0;
  double globalSum;

  MPI::COMM_WORLD.Reduce(&sendDoubleBuffer, &globalSum, 1, MPI::DOUBLE, MPI::SUM, 0);

  std::cout << "Global sum = " << globalSum << std::endl;
}

void slave(void)
{
  int localSize;

  int myrank = MPI::COMM_WORLD.Get_rank();

  MPI::COMM_WORLD.Recv(&localSize, 1, MPI::INTEGER, 0, 0);

  double *arrayData;
  arrayData = new double[localSize];

  std::cout << "processor #" << myrank << " receiving " << localSize
  	    << " doubles" << std::endl;

  MPI::COMM_WORLD.Recv(arrayData, localSize, MPI::DOUBLE, 0, 1);

  double sum = 0.0;
  for (int i=0; i<localSize; i++)
    {
      sum += arrayData[i];
    }

  delete[] arrayData;

  double receiveDoubleBuffer;

  std::cout << "Local sum @ processor " << myrank << " = " << sum << std::endl;

  MPI::COMM_WORLD.Reduce(&sum, &receiveDoubleBuffer, 1, MPI::DOUBLE, MPI::SUM, 0);
}

int main(int argc, char **argv) {
  MPI::Init(argc, argv);
  int myrank = MPI::COMM_WORLD.Get_rank();

  // If we're the root process, send data to everyone else
  if (myrank == 0)
    {
      // NOTE!!! Lazy coding - processor rank #0 does not
      // carry out any compute - only slaves carry out processing
      int arraySize = 15; // 384;
      double *arrayData;
      arrayData = new double[arraySize];
      for (int i=0; i<arraySize; i++)
	{
	  arrayData[i] = (double)i;
	}

      double sum = 0.0;
      for (int i=0; i<arraySize; i++)
	{
	  sum += arrayData[i];
	}
      std::cout << "master processor #0; sum=" << sum << std::endl;

      master(arrayData, arraySize);
      delete[] arrayData;
    }
  else
    {
      slave();
    }


  MPI::Finalize();
  return 0;
}
