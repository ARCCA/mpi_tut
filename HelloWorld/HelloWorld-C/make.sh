#!/bin/bash --login
module purge
module load compiler/intel
module load mpi/intel

mpicc HelloWorld.c -o HelloWorldC
