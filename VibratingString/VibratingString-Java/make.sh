#!/bin/bash --login
module purge
module load compiler/intel
module load mpi/intel

mpicxx VibratingString.cxx -o VibratingStringCXX
