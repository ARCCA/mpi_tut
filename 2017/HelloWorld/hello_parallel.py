#!/bin/env python
#
# Hello World MPI

from mpi4py import MPI
import sys

if __name__ == "__main__":

    # What rank process is this?
    try:
        comm = MPI.COMM_WORLD
        rank = comm.Get_rank()
    except Exception as err:
        sys.exit("Error: %s" % err)

    # Get the size of the world and ask rank 0 process to print it
    size = comm.Get_size()
    if rank == 0:
        print "There are %d processes in the World" % size

    # Say hello
    print "Hello World from process: %d" % rank
