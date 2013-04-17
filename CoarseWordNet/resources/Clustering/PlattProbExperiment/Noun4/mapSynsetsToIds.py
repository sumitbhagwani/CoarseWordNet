f = open('undirectedIdToVertexMapSVMTransformed.txt','r')
vertexToId = {};
for line in f:
	lineSplit = line.strip().split()
	vertexToId[lineSplit[1]] = lineSplit[0]
f.close()

g = open('LinearEqualTrainingMinMaxNormalizationWithSynsets','r')
h = open('LinearEqualTrainingMinMaxNormalizationWithSynsetsMappedToIds','w')
for line in g:
	lineSplit = line.strip().split()
	id0 = vertexToId[lineSplit[0]]
	id1 = vertexToId[lineSplit[1]]
	h.write(id0+" "+id1+" "+lineSplit[2]+" "+lineSplit[3]+"\n");
h.close()
g.close()
