import sys

positiveExamplesPath = sys.argv[1];
print "positiveExamplesPath : "+positiveExamplesPath;
negativeExamplesPath = sys.argv[2];
print "negativeExamplesPath : "+negativeExamplesPath;

positiveExamplesFile = file(positiveExamplesPath,'r');
positiveExamplesCleanedFile = file(positiveExamplesPath+"Cleaned",'w');
negativeExamplesFile = file(negativeExamplesPath,'r');
negativeExamplesCleanedFile = file(negativeExamplesPath+"Cleaned",'w');

positiveExamples = {};
negativeExamples = {};

positiveLines = positiveExamplesFile.readlines();
negativeLines = negativeExamplesFile.readlines();
positiveExamplesFile.close();
negativeExamplesFile.close();

for line in positiveLines : 
	lineSplit = line.split();
	key = lineSplit[0]+" "+lineSplit[1];
	positiveExamples[key] = 1;
	
for line in negativeLines : 
	lineSplit = line.split();
	key = lineSplit[0]+" "+lineSplit[1];
	negativeExamples[key] = 1;	
	
for key in positiveExamples : 	
	if key not in negativeExamples:
		positiveExamplesCleanedFile.write(key+" 1\n");

for key in negativeExamples : 	
	if key not in positiveExamples:
		negativeExamplesCleanedFile.write(key+" -1\n");

positiveExamplesCleanedFile.close();
negativeExamplesCleanedFile.close();
