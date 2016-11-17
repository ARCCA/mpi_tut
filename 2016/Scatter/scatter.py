#!/bin/env python
# Scatter data across multiple processes. The data consists of a
# two-dimensional numpy array, which each process receiving a row.

# load the required modules
from mpi4py import MPI
import numpy as np

if __name__ == "__main__":

    # Initialise a communicator and get the rank of this process.
    try:
        comm = MPI.COMM_WORLD
        rank = comm.Get_rank()
        nprocs = comm.size
    except Exception as err:
        sys.exit("Error: %s" % err)

    # This variable defined the length of each row in the array. In a
    # real work case this would depend on the amount of data.
    allocsize = 4

    # The root process initially holds the data array. It is populated with
    # integers and shaped to match the number of processes. Note, this is
    # for convenience - in reality allocating your data amongst processes
    # can be a major challenge.
    if rank == 0:
        senddata = np.arange(
            nprocs * allocsize, dtype='i').reshape(nprocs, allocsize)
    else:
        senddata = None

    # Each process has an initially empty array set up to receive its share
    # of the data.
    recvdata = np.empty(allocsize, dtype='i')

    # Break up the two-dimensional array amongst processes.
    comm.Scatter(senddata, recvdata, root=0)

    # Each process prints out the data it has received.
    print "rank = ", rank, "recvdata = ", recvdata
