import sys;

thresholdStr = sys.argv[1]
threshold = float(thresholdStr)

f = open('simValuesSVMTransformed.noun','r')
possibleAdditions = {}
for line in f:
	line = line.strip()
	lineSplit = line.split()
	score = float(lineSplit[2])
	if(score >= threshold):
		key = lineSplit[0]+" "+lineSplit[1]; #smaller <space> larger
		possibleAdditions[key] = 1
f.close()
		
pos = open('nounPositiveCleaned','r')
for line in pos:
	line = line.strip()
	lineSplit = line.split()
	offset0 = lineSplit[0].split('#')[1]
	offset1 = lineSplit[1].split('#')[1]
	key = ""
	if(offset0 < offset1):
		key = offset0+" "+offset1
	else:
		key = offset1+" "+offset0
	possibleAdditions[key] = 1
pos.close()

neg = open('nounNegativeCleaned','r')
for line in neg:
	line = line.strip()
	lineSplit = line.split()
	offset0 = lineSplit[0].split('#')[1]
	offset1 = lineSplit[1].split('#')[1]
	key = ""
	if(offset0 < offset1):
		key = offset0+" "+offset1
	else:
		key = offset1+" "+offset0
	possibleAdditions[key] = 0
neg.close()
		
g = open('simValuesSVMTransformed.noun.'+thresholdStr,'w')
for key, value in possibleAdditions.iteritems():
	if(value == 1):
		g.write(key +"\n");
g.close()

