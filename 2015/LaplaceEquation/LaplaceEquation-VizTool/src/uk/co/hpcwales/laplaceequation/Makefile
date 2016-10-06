JCC = javac
JFLAGS = -g

default: FileTokeniser.class VizTool.class

FileTokeniser.class: FileTokeniser.java
	$(JCC) $(JFLAGS) FileTokeniser.java

VizTool.class: VizTool.java
	$(JCC) $(JFLAGS) VizTool.java

clean:
	$(RM) *.class
