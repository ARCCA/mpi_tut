#!/bin/bash --login
module load Java
module load mencoder
java -classpath src uk/co/hpcwales/vibratingstring/VizTool output-0.txt
mencoder mf://*.png -ovc lavc -nosound -ffourcc DX50 -o VibratingString.avi
