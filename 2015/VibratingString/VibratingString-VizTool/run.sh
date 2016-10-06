#!/bin/bash --login
# Load the modules we require
module load java
module load ImageMagick

# Plot the output using a Java application
java -classpath src uk/co/hpcwales/vibratingstring/VizTool output-0.txt

# Convert the separate image files into a single animation using ImageMagick
convert -delay 10 -loop 0 image* stringanim.gif
