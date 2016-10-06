#include <mpi.h>
#include <iostream>

int main(int argc, char **argv) {
  MPI::Init(argc, argv);
  int procs = MPI::COMM_WORLD.Get_size();
  int rank = MPI::COMM_WORLD.Get_rank();
  std::cout << "Hello from " << rank 
	    << " of " << procs << std::endl;
  MPI::Finalize();
  return 0;
}
