#!/bin/bash
#PBS -l select=1:ncpus=3:mpiprocs=3:mem=10MB
#PBS -l walltime=00:00:30
#PBS -N Summation
#PBS -o Summation.out
#PBS -e Summation.err
#PBS -P PR39
#PBS -q workq

# Load required modules.
module purge
module load mpi/intel

# Create an output directory on the fast scratch filesystem, and
# run from this directory.
WDPATH=/scratch/$USER/raven_training/$PBS_JOBID
mkdir -p $WDPATH
cd $WDPATH

# Run a number of copies of the code equal to the number of
# MPI processes requested.
mpirun -np 16 $PBS_O_WORKDIR/Summation >& Summation.log.$PBS_JOBID
