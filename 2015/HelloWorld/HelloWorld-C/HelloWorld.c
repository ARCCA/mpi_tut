#include <stdio.h>
#include <mpi.h>

int main(int argc, char **argv) {
    int mpierr, procs, rank;
    mpierr = MPI_Init(&argc, &argv);
    mpierr = MPI_Comm_size(MPI_COMM_WORLD, &procs);
    mpierr = MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    printf("Hello from %d of %d\n", rank, procs);
    mpierr = MPI_Finalize();
    return 0;
}
