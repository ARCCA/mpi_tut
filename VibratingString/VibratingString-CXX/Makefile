CXX=mpigxx
CXXFLAGS=-O2

all: VibratingString VibratingStringmulti

VibratingStringmulti: VibratingStringmulti.o
	$(CXX) $(CXXFLAGS)  -o VibratingStringmulti VibratingStringmulti.o

VibratingString: VibratingString.o
	$(CXX) $(CXXFLAGS)  -o VibratingString VibratingString.o

clean:
	rm *.o VibratingString VibratingStringmulti
