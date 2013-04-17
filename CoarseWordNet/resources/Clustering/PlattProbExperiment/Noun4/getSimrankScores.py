import sys
num = sys.argv[1]
f = file('LinearEqualTrainingMinMaxNormalizationWithSynsetsMappedToIdsPlattProbSorted','r')
g = file('LinearEqualTrainingMinMaxNormalizationSimrankScores'+num,'w')
h = file('simrankMatrixIterationSVMTransformed'+num,'r')

lineh = h.readline()
lineh = lineh.strip()
linehSplit = lineh.split()
offseth0 = int(linehSplit[0])
offseth1 = int(linehSplit[1])
i = 0
for line in f:
	line = line.strip()
	lineSplit = line.split()
	offset0 = int(lineSplit[0])
	offset1 = int(lineSplit[1])	
	while(offset0 > offseth0):
		lineh = h.readline()
		if(lineh == ''):
			sys.exit()
		lineh = lineh.strip()
		linehSplit = lineh.split()
		offseth0 = int(linehSplit[0])
		offseth1 = int(linehSplit[1])
		i += 1
	if(offset0 == offseth0):
		while(offset1 > offseth1):
			lineh = h.readline()
			if(lineh == ''):
				sys.exit()
			lineh = lineh.strip()
			linehSplit = lineh.split()
			offseth0 = int(linehSplit[0])
			offseth1 = int(linehSplit[1])
			i += 1
	if(offset0 == offseth0 and offset1 == offseth1):
		score = linehSplit[2]
		g.write(line+" "+score+"\n")		
	else:
		g.write(line+" "+"0\n")		
	if(i%1000000000)
		print i
f.close()
g.close()
h.close()
	
	
	
	


