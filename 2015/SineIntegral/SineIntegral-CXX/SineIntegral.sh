#!/bin/bash
#PBS -l select=1:ncpus=16:mpiprocs=16:mem=10MB
#PBS -l walltime=00:00:30
#PBS -N SineIntegral
#PBS -o SineIntegral.out
#PBS -e SineIntegral.err
#PBS -P PR66
#PBS -q training

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
mpirun -np 16 $PBS_O_WORKDIR/SineIntegral >& SineIntegral.log.$PBS_JOBID
