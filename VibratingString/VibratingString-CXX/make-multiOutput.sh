#!/bin/bash --login
module purge
module load compiler/intel
module load mpi/intel

mpicxx VibratingString-multiOutput.cxx -o VibratingString-multiOutputCXX
