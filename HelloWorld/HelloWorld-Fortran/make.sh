#!/bin/bash --login
module purge
module load compiler/intel
module load mpi/intel

mpiifort HelloWorld.f90 -o HelloWorldF90

