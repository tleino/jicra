JAVA	= java
JAVAC	= javac
APPV	= appletviewer

.SUFFIXES: .java .class .html

.java.class:
	$(JAVAC) -deprecation $<

.class.html:
	$(APPLV) $@

all: main

main:	Jicra.class ForwardServer.class

clean:	
	rm -f *~ */*~ *.class */*.class

javadoc:
	javadoc -d doc/ -version -author *.java

dist:	all javadoc

# dependencies

Jicra.class: \
	Protocol.class \
	Net.class

ForwardServer.class: \
	ForwardServerNet.class \
	ForwardServerThread.class
