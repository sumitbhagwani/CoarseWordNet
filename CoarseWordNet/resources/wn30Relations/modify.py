import sys
myfile = sys.argv[1]
f = open(myfile,'r');
g = open(myfile+"New",'w');
for line in f:
	g.write(myfile+"\t"+line);
f.close();
g.close();
