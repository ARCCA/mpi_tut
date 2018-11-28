#!/bin/bash
#PBS -l select=1:ncpus=12:mpiprocs=12:mem=10MB
#PBS -l walltime=00:00:30
#PBS -N HelloPython
#PBS -P PR66
#PBS -q training

# Load required modules.
module purge
module load python/2.7.9-mpi

# Create an output directory on the fast scratch filesystem, and
# run from this directory.
WDPATH=/scratch/$USER/raven_training/$PBS_JOBID
mkdir -p $WDPATH
cd $WDPATH

# Copy the python code to the run directory
cp $PBS_O_WORKDIR/hello_parallel.py .

# Run a number of copies of the code equal to the number of
# MPI processes requested.
mpirun -np 12 ./hello_parallel.py
