#!/bin/bash
#PBS -l select=1:ncpus=10:mpiprocs=10:mem=10MB
#PBS -l walltime=00:00:30
#PBS -N VibratingString
#PBS -o VibratingString.out
#PBS -e VibratingString.err
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
mpirun -np 10 $PBS_O_WORKDIR/VibratingString >& VibratingString.log.$PBS_JOBID
