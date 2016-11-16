#!/bin/env python
# Example of blocking point-to-point MPI communication. Pairs of processes
# will send and receive data to and from each other in the form of
# pickled python dictionaries.

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

    # Create a python dictionary containing the rank and world size which we
    # will send via MPI point-to-point communication methods.
    send_data = {'rank': rank, 'world': nprocs}

    # Even numbered processes send data to process rank + 1, whilst odd
    # numbered processes send data to process rank - 1. Note that even
    # processes send first and odd processes receive first - two processes
    # sending or receiving simultaneously results in a 'deadlock'.
    if rank % 2 == 0:
        # Determine rank of process to exchange data with and set communication
        # tags.
        exch_rank = (rank + 1)
        sendtag = rank
        recvtag = 100 + exch_rank

        # Even numbered processes send first, then wait for acknowledgement
        # that the data was received. Once sent they wait to receive data.
        sendreq = comm.isend(send_data, dest=exch_rank, tag=sendtag)
        sendreq.wait()
        recvreq = comm.irecv(source=exch_rank, tag=recvtag)
        recv_data = recvreq.wait()
    else:
        exch_rank = (rank - 1)
        sendtag = 100 + rank
        recvtag = exch_rank

        # Odd numbered processes receive data first, then send.
        recvreq = comm.irecv(source=exch_rank, tag=recvtag)
        recv_data = recvreq.wait()
        sendreq = comm.isend(send_data, dest=exch_rank, tag=sendtag)
        sendreq.wait()

    # Print out the data sent and received by this process.
    print "Rank: %d, sent: %s, received: %s" % (rank, send_data, recv_data)
