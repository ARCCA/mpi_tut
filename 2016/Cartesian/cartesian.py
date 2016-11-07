#!/bin/env python
# Demonstration of the imposition of a simple cartesian geometry on a set of
# processes. In this case we generate a uniform 2-D grid in which each
# element passes a value to its nearest neighbour to the right.

from mpi4py import MPI
import numpy as np

# Initialise the MPI communicator and get the rank and total number of
# processes.
comm = MPI.COMM_WORLD
rank = comm.Get_rank()
nprocs = comm.size

# We want to create a square array of elements, so take the square root
# of the number of processes to get the length of each axis.
axis = np.sqrt([nprocs])

# Create a cartesian geometry communicator, with no wrapping at the edges. 
cart = comm.Create_cart(dims=(axis, axis),
                        periods=(False, False), reorder=True)

# Return the process this rank will be receiving data from, and the one it
# will be sending it to. Then print out a summary.
source, dest = cart.Shift(direction=0, disp=1)
coord = tuple(cart.Get_coords(rank))
print "Rank: %d, Co-ords: %s : Source = %d, Dest = %d" % (
    rank, coord, source, dest) 

# Create buffers for sending and receiving data. The send buffer contains
# an integer representing the coordinates of this process and the receive
# buffer is empty.
sendbuf = np.array(int("%d%d" % (coord[0], coord[1])))
recvbuf = np.array(0)

# Send and receive data in one operation, and print out the result.
cart.Sendrecv(sendbuf, dest=dest, recvbuf=recvbuf, source=source)
print "Rank: %d, Sent: %d, Received: %d" % (rank, sendbuf, recvbuf)
