f = file('offsets.txt','r')

listOffsets = []
for line in f:
	lineSplit = line.strip().split("-");
	if(lineSplit[1] == 'n'):
		listOffsets.append(lineSplit[0])
f.close()

g = file('offsetsNoun.arff','w');
g.write("@relation 'nounOffsets'\n@attribute offset {");
for offset in listOffsets:
	g.write("'"+offset+"',");
g.write("}\n@attribute pos {'n','v'}\n@data\n")
for offset in listOffsets:
	g.write("'"+offset+"','n'\n");
g.close()


