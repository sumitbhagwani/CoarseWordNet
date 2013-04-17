package svmPredictionNormalization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jnisvmlight.FeatureVector;
import jnisvmlight.LabeledFeatureVector;
import jnisvmlight.SVMLightInterface;
import jnisvmlight.TrainingParameters;
import krsystem.StaticValues;
import krsystem.ontology.senseClustering.svm.Evaluation;
import krsystem.ontology.senseClustering.svm.FeatureGenerator;
import krsystem.ontology.senseClustering.svm.Instance;
import krsystem.ontology.senseClustering.svm.MinMaxSVMModel;
import krsystem.ontology.senseClustering.svm.ModelSVM;
import krsystem.ontology.senseClustering.svm.Training;
import krsystem.utility.OrderedPair;
import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;

public class GeneratePlattProb {

	public static double[] toPrimitiveDoubleArray(List<Double> array) {
		  if (array == null) {
		    return null;
		  } else if (array.size() == 0) {
		    return new double[0];
		  }
		  final double[] result = new double[array.size()];
		  int i = 0;
		  for (Double d : array) {
		    result[i] = d.doubleValue();
		    i++;
		  }
		  return result;
	}
		
	public static void writeSVMPredictionsNoun()
	{
		String propsFile30 = StaticValues.propsFile30;
		String dir = "/home/sumitb/Data/";
		String arg = "WordNet-3.0";
		String svmFolder = "resources/Clustering/svmBinaries/";
		String domainDataPathNoun = "/home/sumitb/Data/xwnd/joinedPOSSeparated/joinedNoun.txt";
		String OEDMappingPathNoun = "/home/sumitb/Data/navigli_sense_inventory/mergeData-30.offsets.noun";
		String sentimentFilePath = "/home/sumitb/Data/SentiWordNet/SentiWordNet.n";
		String sensevalDataPath = "resources/Clustering/SensevalBinaryClassificationData/";
		String wordNet30OffsetFile = "/home/sumitb/Data/xwnd/offsets.txt";
		String synsetToWordIndexPairMap = "resources/Clustering/synsetWordIndexMap/nounMap.txt";		
		int folderNum = 4;
		String plattProbExpPath = "resources/Clustering/PlattProbExperiment/Noun"+folderNum+"/";
		try{			
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();
			String path = "resources/Clustering/BinaryClassificationData/";
			
			String nounPath = path+"Noun"+folderNum;
			String[] trainingFiles = {nounPath+"/NegTrain0.7.txt", path+"Noun"+folderNum+"/PosTrain0.7.txt"};
			String[] trainingFilesEqual = {nounPath+"/PosTrain0.7.txt", path+"Noun"+folderNum+"/NegTrainEqual.txt"};//, sensevalDataPath+"nPositive30.txt"};
			String[] testingFiles = {nounPath+"/PosTest0.7.txt", path+"Noun"+folderNum+"/NegTest0.7.txt"};
			String[] testingFilesEqual = {nounPath+"/PosTest0.7.txt", path+"Noun"+folderNum+"/NegTestEqual.txt"};
			String[] sample = {nounPath+"/sample.txt"};
			String[] sensevalNounFiles = {sensevalDataPath+"nPositive30.txt",sensevalDataPath+"nNegative30.txt"};
			
			SVMLightInterface trainer = new SVMLightInterface();
			FeatureGenerator fg = new FeatureGenerator(dir, arg, domainDataPathNoun, dictionary, OEDMappingPathNoun, sentimentFilePath, POS.NOUN, synsetToWordIndexPairMap);
			Training trainingModule = new Training(dictionary, fg);						
			System.out.println("Model loading...");
			ModelSVM model = MinMaxSVMModel.readModel(svmFolder+"modelLinearEqualTrainingMinMaxNormalizationNoun", svmFolder+"paramsLinearEqualTrainingMinMaxNormalizationNoun");
			System.out.println("Model loaded....");
												
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(plattProbExpPath+"LinearEqualTrainingMinMaxNormalizationWithSynsets")));			
			for(String testFilePath : trainingFilesEqual)
			{
				System.out.println("Working on "+testFilePath);
				int i = 0;				
				List<Instance> instances = trainingModule.getInstances(testFilePath);
				for(Instance instance : instances)
				{				
					OrderedPair<Integer, LabeledFeatureVector> dimNumLFVPair = fg.getLabeledFeatureVector(instance); 
					LabeledFeatureVector lfv = dimNumLFVPair.getR();				
					double prediction = model.classify((FeatureVector)lfv);
					bw.write(instance.getSmallerSynsetOffset()+" "+instance.getLargerSynsetOffset()+" "+lfv.getLabel()+" "+prediction+"\n");
					i++;
					if(i%100 == 0)
						System.out.println(i);
				}
			}
			bw.close();
			dictionary.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void genPlattProbNoun()
	{
		System.out.println("genPlattProbNoun Running...");
		int folderNum = 4;
		String plattProbExpPath = "resources/Clustering/PlattProbExperiment/Noun"+folderNum+"/";
		int prior1 = 0, prior0 = 0;
		List<Double> predictions = new ArrayList<Double>();
		List<Double> classes = new ArrayList<Double>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(plattProbExpPath+"LinearAllTraining0.7MinMaxNormalization")));
			String line;
			while((line = br.readLine()) != null)
			{
				String[] lineSplit = line.split("\\s+");
				double classVal = Double.parseDouble(lineSplit[0]); 
				classes.add(classVal);
				predictions.add(Double.parseDouble(lineSplit[1]));
				if(classVal>=0)
					prior1++;
				else
					prior0++;
			}
			br.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}		
		double[] predictionsArr = toPrimitiveDoubleArray(predictions);
		double[] classArr = toPrimitiveDoubleArray(classes);
		PlattProb transformationModel = new PlattProb(predictionsArr, classArr, prior1, prior0);
		System.out.println("transformationModel.A : "+transformationModel.A);
		System.out.println("transformationModel.B : "+transformationModel.B);
			
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(plattProbExpPath+"LinearAllTraining0.7MinMaxNormalizationPlattProb")));			
			for(int i=0; i<classArr.length; i++)
			{
				double prob = transformationModel.reportPosteriorProb(predictionsArr[i]);
				bw.write(classArr[i]+" "+prob+"\n");
			}
			bw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		writeSVMPredictionsNoun();
//		genPlattProbNoun();
	}

}
