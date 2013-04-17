import heapq
import sys

heap = []
k = 1000
num = int(sys.argv[1])

f = file('simrankMatrixIterationSVMTransformed'+str(num),'r')
for line in f:
	lineSplit = line.strip().split()
	score = float(lineSplit[2])
	item = (score, lineSplit[0], lineSplit[1])
	if len(heap) < k or item[0] > heap[0][0]:
		if len(heap)==k :
			heapq.heappop(heap)
		heapq.heappush(heap, item) 
f.close()

vertexToIdMap = {}
h = file('undirectedIdToVertexMapSVMTransformed.txt', 'r')
for line in h:
	lineSplit = line.strip().split()
	vertexToIdMap[lineSplit[0]] = lineSplit[1]
h.close()

g = file('simrankMatrixIterationSVMTransformed'+str(num)+'Top'+str(k),'w')
for item in heap:
	offset1 = vertexToIdMap[item[1]]
	offset2 = vertexToIdMap[item[2]]
	g.write(offset1+" "+offset2+" "+str(item[0])+"\n")	
g.close()
