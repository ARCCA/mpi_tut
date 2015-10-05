CXX=mpigxx
CXXFLAGS=-O2

all: VibratingString VibratingString-multiOutput

VibratingString-multiOutput: VibratingString-multiOutput.o
	$(CXX) $(CXXFLAGS)  -o VibratingString-multiOutput VibratingString-multiOutput.o

VibratingString: VibratingString.o
	$(CXX) $(CXXFLAGS)  -o VibratingString VibratingString.o

clean:
	rm *.o VibratingString VibratingString-multiOutput
