#!/bin/bash
#PBS -l select=1:ncpus=16:mpiprocs=16:mem=10MB
#PBS -l walltime=00:00:30
#PBS -N HelloPython
#PBS -o HelloPython.out
#PBS -e HelloPython.err
#PBS -P PR39
#PBS -q workq

# Load required modules.
module purge
module load python/2.7.9-mpi

# Create an output directory on the fast scratch filesystem, and
# run from this directory.
WDPATH=/scratch/$USER/raven_training/$PBS_JOBID
mkdir -p $WDPATH
cd $WDPATH

# Run a number of copies of the code equal to the number of
# MPI processes requested.
mpirun -np 16 $PBS_O_WORKDIR/hello_parallel.py >& hello_parallel.log.$PBS_JOBID
