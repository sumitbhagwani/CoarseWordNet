package krsystem.ontology.senseClustering.svm;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import jnisvmlight.FeatureVector;
import jnisvmlight.LabeledFeatureVector;
import jnisvmlight.SVMLightInterface;
import jnisvmlight.SVMLightModel;
import jnisvmlight.TrainingParameters;
import krsystem.StaticValues;
import krsystem.utility.OrderedPair;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;

public class Evaluation {
	
	Dictionary dict;
	Training trainingModule;
//	SVMLightModel model;
	ModelSVM model;
	FeatureGenerator fg;
	
//	public Evaluation(Dictionary dictPassed, Training trainingModulePassed, SVMLightModel modelpassed, FeatureGenerator fgPassed)
	public Evaluation(Dictionary dictPassed, Training trainingModulePassed, ModelSVM modelpassed, FeatureGenerator fgPassed)
	{
		dict = dictPassed;
		trainingModule = trainingModulePassed;
		model = modelpassed;
		fg = fgPassed;
	}
		
	public void test(String[] testFilePaths)
	{
		int classificationCount = 0;
		int posAsPos=0, posAsNeg=0, negAsPos=0, negAsNeg = 0;					
		for(String testFilePath : testFilePaths)
		{
			List<Instance> instances = trainingModule.getInstances(testFilePath);
			for(Instance instance : instances)
			{				
				OrderedPair<Integer, LabeledFeatureVector> dimNumLFVPair = fg.getLabeledFeatureVector(instance); 
				LabeledFeatureVector lfv = dimNumLFVPair.getR();				
				double prediction = model.classify((FeatureVector)lfv);
				if(prediction>0)
					if(lfv.getLabel()>0)
						posAsPos++;
					else
						negAsPos++;
				else
					if(lfv.getLabel()>0)
						posAsNeg++;
					else
						negAsNeg++;
				if(classificationCount%100 == 0)
					System.out.println(classificationCount);
				classificationCount++;
			}
		}
		
		System.out.println("posAsPos : "+ posAsPos);
		System.out.println("posAsNeg : "+ posAsNeg);
		System.out.println("negAsPos : "+ negAsPos);
		System.out.println("negAsNeg : "+ negAsNeg);
		
		double accuracy = (posAsPos + negAsNeg)*1.0/(posAsPos + posAsNeg + negAsPos + negAsNeg);
		
		double precisionP = posAsPos*1.0 / (posAsPos + negAsPos);
		double recallP = posAsPos*1.0/ (posAsPos + posAsNeg);
		double fScoreP = 2*precisionP*recallP/ (precisionP + recallP);
		
		double precisionN = negAsNeg*1.0 / (negAsNeg + posAsNeg);
		double recallN = negAsNeg*1.0/ (negAsNeg + negAsPos);
		double fScoreN = 2*precisionN*recallN/ (precisionN + recallN);
		
		System.out.println("Accuracy : "+ accuracy);
		System.out.println("PrecisionP : "+precisionP);
		System.out.println("RecallP : "+recallP);
		System.out.println("FScoreP : "+fScoreP);
		System.out.println("PrecisionN : "+precisionN);
		System.out.println("RecallN : "+recallN);
		System.out.println("FScoreN : "+fScoreN);		
	}
	
	public void sanityCheck(String offsetFile, String posStringReqd, int maxOffsetsToCheck)
	{
		int classificationCount = 0;							
		int classificationError = 0;		
		int i = 0;
		List<Instance> instances = new ArrayList<Instance>();
		try{
			Scanner sc = new Scanner(new File(offsetFile));
			String line;
			while(sc.hasNextLine())
			{
				line = sc.nextLine().toLowerCase().trim();//00001740-a
				String[] lineSplit = line.split("-");
				long offset = Long.parseLong(lineSplit[0]);
				String posString = lineSplit[1];
				if(posStringReqd.equalsIgnoreCase(posString))
				{
					POS pos = posStringReqd.equalsIgnoreCase("n") ? POS.NOUN : POS.VERB;
					Synset syn = dict.getSynsetAt(pos, offset);
					instances.add(new Instance(syn, syn, 1));					
					if(i>maxOffsetsToCheck)
						break;
					i++;
				}
			}
			sc.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		for(Instance instance : instances)
		{				
			OrderedPair<Integer, LabeledFeatureVector> dimNumLFVPair = fg.getLabeledFeatureVector(instance); 
			LabeledFeatureVector lfv = dimNumLFVPair.getR();			
			double prediction = model.classify((FeatureVector)lfv);
			if(prediction<0)
				classificationError++;
			if(classificationCount%100 == 0)
				System.out.println(classificationCount);
			classificationCount++;
		}
		System.out.println("Total : "+classificationCount);
		System.out.println("Error : "+classificationError);
	}
	
	public static void printFeatureWeights(String modelPath)
	{
		try{
			SVMLightModel model = SVMLightModel.readSVMLightModelFromURL(new java.io.File(modelPath).toURL());
			for(double weight : model.getLinearWeights())
				System.out.println(weight);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}		
	
	public static void evalVerb() {
		System.out.println("Evaluation Running...");
		String propsFile30 = StaticValues.propsFile30;
		String dir = "/home/sumitb/Data/";
		String arg = "WordNet-3.0";
		String svmFolder = "resources/Clustering/svmBinaries/";
		String domainDataPath = "/home/sumitb/Data/xwnd/joinedPOSSeparated/joinedVerb.txt";
		String OEDMappingPath = "/home/sumitb/Data/navigli_sense_inventory/mergeData-30.offsets.verb";
		String sentimentFilePath = "/home/sumitb/Data/SentiWordNet/SentiWordNet.v";
		String sensevalDataPath = "resources/Clustering/SensevalBinaryClassificationData/";
		String wordNet30OffsetFile = "/home/sumitb/Data/xwnd/offsets.txt";
		String arffPathTrain = "/home/sumitb/Data/wekaRelated/verbAnalysisTrain.arff";
		String arffPathTest = "/home/sumitb/Data/wekaRelated/verbAnalysisTest.arff";
		String lightPathTrain = "/home/sumitb/Data/wekaRelated/verbAnalysisTrain.light";
		String lightPathTest = "/home/sumitb/Data/wekaRelated/verbAnalysisTest.light";
		String synsetToWordIndexPairMap = "resources/Clustering/synsetWordIndexMap/verbMap.txt";
		int folderNum = 2;
		String posString = "Verb";
		try{			
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();
			String path = "resources/Clustering/BinaryClassificationData/";
			
			String[] trainingFiles = {path+posString+folderNum+"/NegTrain0.7.txt", path+posString+folderNum+"/PosTrain0.7.txt"};
			String[] trainingFilesEqual = {path+posString+folderNum+"/PosTrain0.7.txt", path+posString+folderNum+"/NegTrainEqual.txt"};//, sensevalDataPath+"vPositive30.txt"};
			String[] testingFiles = {path+posString+folderNum+"/PosTest0.7.txt", path+posString+folderNum+"/NegTest0.7.txt"};
			String[] testingFilesEqual = {path+posString+folderNum+"/PosTest0.7.txt", path+posString+folderNum+"/NegTestEqual.txt"};
			String[] sample = {path+posString+folderNum+"/sample.txt"};
			
			SVMLightInterface trainer = new SVMLightInterface();
			FeatureGenerator fg = new FeatureGenerator(dir, arg, domainDataPath, dictionary, OEDMappingPath, sentimentFilePath, POS.VERB, synsetToWordIndexPairMap);
			Training trainingModule = new Training(dictionary, fg);
//			trainingModule.generateARFFFormat(trainingFilesEqual, arffPathTrain);
//			trainingModule.generateARFFFormat(testingFilesEqual, arffPathTest);
//			trainingModule.generateSVMLightFormat(trainingFilesEqual, lightPathTrain);
//			trainingModule.generateSVMLightFormat(testingFilesEqual, lightPathTest);
			TrainingParameters tp = new TrainingParameters();
			tp.getLearningParameters().verbosity = 1;		
//			tp.getKernelParameters().kernel_type = 2;
//			ModelSVM model = trainingModule.train(trainingFiles, trainer, tp);
//			ModelSVM model = trainingModule.train(trainingFilesEqual, trainer, tp);
			ModelSVM model = trainingModule.trainMinMaxNormal(trainingFilesEqual, trainer, tp, arffPathTrain, "");
//			ModelSVM model = trainingModule.trainZScoreNormal(trainingFilesEqual, trainer, tp);
//			ModelSVM model = trainingModule.trainMinMaxNormal(sample, trainer, tp);			
			System.out.println("Training completed...");
			model.writeModel(svmFolder+"modelLinearEqualTrainingMinMaxNormalization"+posString, svmFolder+"paramsLinearEqualTrainingMinMaxNormalization"+posString);
			
//			SVMLightModel model = SVMLightModel.readSVMLightModelFromURL(new java.io.File(svmFolder+"model4").toURL());
//			System.out.println("Model loaded....");
			
			Evaluation evaluationModule = new Evaluation(dictionary, trainingModule, model, fg);
//			evaluationModule.test(trainingFilesEqual);
			System.out.println("-----------------------------------------");
			evaluationModule.test(testingFilesEqual);
//			evaluationModule.sanityCheck(wordNet30OffsetFile, "n", 1000);
//			evaluationModule.sanityCheck(wordNet30OffsetFile, "v", 1000);
			
			dictionary.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void evalNoun() {
		System.out.println("Evaluation Running...");
		String propsFile30 = "resources/file_properties.xml";
		String dir = "/home/sumitb/Data/";
		String arg = "WordNet-3.0";
		String svmFolder = "resources/Clustering/svmBinaries/";
		String domainDataPathNoun = "/home/sumitb/Data/xwnd/joinedPOSSeparated/joinedNoun.txt";
		String OEDMappingPathNoun = "/home/sumitb/Data/navigli_sense_inventory/mergeData-30.offsets.noun";
		String sentimentFilePath = "/home/sumitb/Data/SentiWordNet/SentiWordNet.n";
		String sensevalDataPath = "resources/Clustering/SensevalBinaryClassificationData/";
		String wordNet30OffsetFile = "/home/sumitb/Data/xwnd/offsets.txt";
		String arffPathTrain = "/home/sumitb/Data/wekaRelated/nounAnalysisTrain.arff";
		String arffPathTest = "/home/sumitb/Data/wekaRelated/nounAnalysisTest.arff";
		String lightPathTrain = "/home/sumitb/Data/wekaRelated/nounAnalysisTrain.light";
		String lightPathTest = "/home/sumitb/Data/wekaRelated/nounAnalysisTest.light";
		String synsetToWordIndexPairMap = "resources/Clustering/synsetWordIndexMap/nounMap.txt";
		int folderNum = 3;
		try{			
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();
			String path = "resources/Clustering/BinaryClassificationData/";
			
			String[] trainingFiles = {path+"Noun"+folderNum+"/NegTrain0.7.txt", path+"Noun"+folderNum+"/PosTrain0.7.txt"};
			String[] trainingFilesEqual = {path+"Noun"+folderNum+"/PosTrain0.7.txt", path+"Noun"+folderNum+"/NegTrainEqual.txt"};//, sensevalDataPath+"nPositive30.txt"};
			String[] testingFiles = {path+"Noun"+folderNum+"/PosTest0.7.txt", path+"Noun"+folderNum+"/NegTest0.7.txt"};
			String[] testingFilesEqual = {path+"Noun"+folderNum+"/PosTest0.7.txt", path+"Noun"+folderNum+"/NegTestEqual.txt"};
			String[] sample = {path+"Noun"+folderNum+"/sample.txt"};
			
			SVMLightInterface trainer = new SVMLightInterface();
			FeatureGenerator fg = new FeatureGenerator(dir, arg, domainDataPathNoun, dictionary, OEDMappingPathNoun, sentimentFilePath, POS.NOUN, synsetToWordIndexPairMap);
			Training trainingModule = new Training(dictionary, fg);
			TrainingParameters tp = new TrainingParameters();
			tp.getLearningParameters().verbosity = 1;		
//			tp.getKernelParameters().kernel_type = 2;
//			ModelSVM model = trainingModule.train(trainingFiles, trainer, tp);
//			ModelSVM model = trainingModule.train(trainingFilesEqual, trainer, tp);
			ModelSVM model = trainingModule.trainMinMaxNormal(trainingFilesEqual, trainer, tp);
//			ModelSVM model = trainingModule.trainZScoreNormal(trainingFilesEqual, trainer, tp);
//			ModelSVM model = trainingModule.trainMinMaxNormal(sample, trainer, tp);			
			System.out.println("Training completed...");
			model.writeModel(svmFolder+"modelLinearEqualTrainingMinMaxNormalizationVerb", svmFolder+"paramsLinearEqualTrainingMinMaxNormalizationVerb");
			
//			SVMLightModel model = SVMLightModel.readSVMLightModelFromURL(new java.io.File(svmFolder+"model4").toURL());
//			System.out.println("Model loaded....");
			
			Evaluation evaluationModule = new Evaluation(dictionary, trainingModule, model, fg);
//			evaluationModule.test(trainingFilesEqual);
			System.out.println("-----------------------------------------");
			evaluationModule.test(testingFilesEqual);
//			evaluationModule.sanityCheck(wordNet30OffsetFile, "n", 1000);
			
			dictionary.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
//		Evaluation.evalNoun();
		Evaluation.evalVerb();
	}

}
