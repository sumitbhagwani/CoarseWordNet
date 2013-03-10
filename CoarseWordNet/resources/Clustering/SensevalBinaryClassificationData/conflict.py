import sys
print sys.argv[1]
print sys.argv[2]

f = open(sys.argv[1]);
fSet = {};
for fLine in f:
	fSplit = fLine.split(" ");
	key = fSplit[0]+"-"+fSplit[1];
	fSet[key] = 1;
f.close();
print len(fSet)

g = open(sys.argv[2]);
gSet = {};
for gLine in g:
	gSplit = gLine.split(" ");
	key = gSplit[0]+"-"+gSplit[1];
	gSet[key] = 1;
g.close();
print len(gSet)

common = 0;
for key in fSet:
	if key in gSet:
		common = common +1;
		
print common
