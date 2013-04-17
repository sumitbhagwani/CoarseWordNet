import sys

filePath = sys.argv[1]
mapPath = "undirectedIdToVertexMapSVMTransformed.txt"
mappedPath = filePath+".mapped"

offsetToIdMap = {}
g = open(mapPath,'r')
for line in g:
	line = line.strip()
	lineSplit = line.split()
	offsetToIdMap[lineSplit[1]] = lineSplit[0];
g.close()

f = open(filePath,'r')
h = open(mappedPath,'w')
for line in f :
	line = line.strip()
	lineSplit = line.split()
	id0 = offsetToIdMap[lineSplit[0]]
	id1 = offsetToIdMap[lineSplit[1]]
	h.write(id0+" "+id1+" "+lineSplit[2]+"\n")
h.close()
f.close()

