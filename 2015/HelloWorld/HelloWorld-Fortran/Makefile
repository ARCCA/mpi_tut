FC=mpif90
FCFLAGS=-O2

HelloWorld: HelloWorld.o
	$(FC) $(FCFLAGS) -o HelloWorld HelloWorld.o

HelloWorld.o: HelloWorld.f90
	$(FC) $(FCFLAGS) -c $<

clean:
	rm *.o HelloWorld
