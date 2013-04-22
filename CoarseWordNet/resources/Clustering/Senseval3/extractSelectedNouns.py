import sys;

idList = {}
h = file('EnglishAW.test.key.extractedNouns','r')
for line in h:
	line = line.strip()
	lineSplit = line.split()
	if(lineSplit[2] != 'U'):
		sensekey = lineSplit[2]
		pos = sensekey.split("%")[1][0]
		if(pos == '1'):
			idList[lineSplit[1]] = 1
h.close()

fileName = sys.argv[1]
f = file(fileName,'r')
extractedNouns = fileName+".extractedNouns";
g = file(extractedNouns,'w')
i = 0
for line in f :
	i = i +1
	#print i
	line = line.strip()
	lineSplit = line.split()
	#print lineSplit
	index = 2		
	lemma = lineSplit[2].split('%')[0]
	toWrite = True;
	instanceId = lineSplit[1]
	if(instanceId in idList):		
		while(index < len(lineSplit)):
			sensekey = lineSplit[index]
			if(sensekey != 'U'):
				pos = sensekey.split("%")[1][0]
				if(pos != '1'):
						toWrite = False;	
				senseLemma = sensekey.split('%')[0]
				if(senseLemma != lemma) :
					toWrite = False;
			else:
				toWrite = False;			
			index += 1
		if(toWrite):
			g.write(line+"\n");
				
f.close()
g.close()
	
