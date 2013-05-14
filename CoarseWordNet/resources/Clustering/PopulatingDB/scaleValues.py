import sys
svmProbName = sys.argv[1]
svmProbfile = file(svmProbName,'r')
constant = 0.9
outputFileName = svmProbName+"Scaled"+str(constant)
outputFile = file(outputFileName,'w')

for line in svmProbfile:
	line = line.strip()
	lineSplit = line.split()
	svmValue = float(lineSplit[2])
	newSvmValue = svmValue*constant
	outputFile.write(lineSplit[0]+' '+lineSplit[1]+' '+str(newSvmValue)+'\n')	
svmProbfile.close()
outputFile.close()
