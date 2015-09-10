#!/bin/bash --login
module purge
module load compiler/intel
module load mpi/intel

mpicxx Summation.cxx -o SummationCXX
