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

    # Say hello
    print "Hello World from process: %d" % rank
