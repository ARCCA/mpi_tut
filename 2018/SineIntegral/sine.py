#!/bin/env python
# This code demonstrates collective communication, specifically the Broadcast
# and Reduce functions. It distributes the iterative calculation of sin(x)
# over an arbitrary number of processes, and then collates the result at
# the end.

# load the required modules
from mpi4py import MPI
import numpy as np
import sys

if __name__ == "__main__":

    # Initialise a communicator and get the rank of this process.
    try:
        comm = MPI.COMM_WORLD
        rank = comm.Get_rank()
        nprocs = comm.size
    except Exception as err:
        sys.exit("Error: %s" % err)

    # This is the number of discrete points to use for this calculation.
    numpoints = 1024

    # Assume rank 0 is our root process. Only it knows the value of numpoints.
    if rank == 0:
        recvbuffer = np.array(numpoints)
    else:
        recvbuffer = np.array(0)

    # The root process broadcasts the value of numpoints to all the other
    # processes.
    comm.Bcast([recvbuffer, MPI.INT], root=0)

    # Calculate the range of data points this process will be using. The
    # block of values is broken up into a number equal to the number of
    # processes and each process calculates the range of values it will
    # be working on.
    nlocal = (numpoints - 1) / nprocs + 1
    nbeg = (rank * nlocal) + 1
    nend = min((nbeg + nlocal - 1), numpoints)

    delta = np.pi / numpoints

    # Iterate from the first to the last data point.
    psum = np.array(0.0, 'd')
    for i in range(nbeg, nend):
        psum += np.sin((i - 0.5) * delta) * delta

    # Construct a result buffer to receive the summation of the individual
    # parts of the calculation, and use the Reduce function to return this
    # value to the root process.
    resbuffer = np.array(0.0, 'd')
    comm.Reduce([psum, MPI.DOUBLE], resbuffer, op=MPI.SUM, root=0)

    # The root process prints out the result.
    if rank == 0:
        print "Result =", resbuffer

