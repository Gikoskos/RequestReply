SERVER_SRC = distsys/rr/*.java

demo: lib
	javac -cp ".:./RR.jar" ServerDemo.java
	java -cp ".:./RR.jar" ServerDemo

lib:
	javac $(SERVER_SRC)
	jar cvf RR.jar distsys/rr/*.class
	rm distsys/rr/*.class
