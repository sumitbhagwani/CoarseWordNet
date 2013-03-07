package krsystem.ontology.senseClustering.svm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import jnisvmlight.LabeledFeatureVector;
import jnisvmlight.SVMLightInterface;
import jnisvmlight.SVMLightModel;
import jnisvmlight.TrainingParameters;
import krsystem.utility.OrderedPair;


import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.dictionary.Dictionary;

public class Training {	
	
	Dictionary dict;
	FeatureGenerator fg;
	
	public Training(Dictionary dictionary, FeatureGenerator fgPassed)
	{
		dict = dictionary;
		fg = fgPassed;
	}
	
	public List<Instance> getInstances(String dataPath)
	{			
		List<Instance> instances = new ArrayList<Instance>();
		try{					
			Scanner sc = new Scanner(new File(dataPath));			
			while(sc.hasNextLine())
			{
				String line = sc.nextLine();
				String[] lineSplit = line.split("\\s+");
				int label = Integer.parseInt(lineSplit[2]);
				Instance instance = new Instance(lineSplit[0], lineSplit[1], dict, label);
				instances.add(instance);
			}
			sc.close();			
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(-1);
		}
		return instances;
	}
	
	public List<Instance> getInstances(String[] dataPaths)
	{			
		List<Instance> instances = new ArrayList<Instance>();
		try{		
			for(String dataPath : dataPaths)
			{
				Scanner sc = new Scanner(new File(dataPath));			
				while(sc.hasNextLine())
				{
					String line = sc.nextLine();
					String[] lineSplit = line.split("\\s+");
					int label = Integer.parseInt(lineSplit[2]);
					Instance instance = new Instance(lineSplit[0], lineSplit[1], dict, label);
					instances.add(instance);
				}
				sc.close();
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(-1);
		}
		return instances;
	}

	public OrderedPair<Integer, LabeledFeatureVector[]> getFeatureVectors(String[] dataPaths)
	{			
		List<Instance> instances = new ArrayList<Instance>();
		int dimNum = 0;
		try{		
			for(String dataPath : dataPaths)
			{
				System.out.println("Working on "+dataPath);
				Scanner sc = new Scanner(new File(dataPath));			
				while(sc.hasNextLine())
				{
					String line = sc.nextLine().toLowerCase().trim();
					String[] lineSplit = line.split("\\s+");
					int label = Integer.parseInt(lineSplit[2]);
					Instance instance = new Instance(lineSplit[0], lineSplit[1], dict, label);
					instances.add(instance);
				}
				sc.close();
			}
			System.out.println("Generating Features...");
			LabeledFeatureVector[] featureVectors = new LabeledFeatureVector[instances.size()];
			int count = 0;
			for(Instance instance : instances)
			{
				OrderedPair<Integer, LabeledFeatureVector> dimNumLFVPair = fg.getLabeledFeatureVector(instance); 
				LabeledFeatureVector lfv = dimNumLFVPair.getR();
				dimNum = dimNumLFVPair.getL().intValue();
				featureVectors[count] = lfv;
				count++;
				if(count%100 == 0)
					System.out.println(count);
			}
			return new OrderedPair<Integer, LabeledFeatureVector[]>(dimNum, featureVectors);
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(-1);
		}
		return null;		
	}	
	
	public static void writeDATASVMLight(LabeledFeatureVector[] lfvs, int featureNum, String outputPath)
	{
		try{					
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPath)));			
			for(LabeledFeatureVector lfv : lfvs)
			{
				String label = lfv.getLabel()>0 ? "1" : "-1";
				bw.write(label+" ");
				for(int i=0; i<featureNum; i++)
				{
					bw.write((i+1)+":"+lfv.getValueAt(i)+" ");
				}
				bw.write("\n");				
			}
			bw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}		
	}
	
	public static void writeDataARFF(LabeledFeatureVector[] lfvs, int featureNum, String outputPath)
	{
		try{					
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPath)));
			bw.write("@relation wnClustering\n");
			for(int i=0; i<featureNum; i++)
			{
				bw.write("@attribute attr"+i+" real\n");
			}
			bw.write("@attribute class {-1,1}\n");
			bw.write("@data\n");
			/*@relation 'cpu'
			@attribute MYCT real
			@attribute class real
			@data*/
			for(LabeledFeatureVector lfv : lfvs)
			{
				for(int i=0; i<featureNum; i++)
				{
					bw.write(lfv.getValueAt(i)+",");
				}
				String label = lfv.getLabel()>0 ? "1" : "-1";
				bw.write(label+"\n");
			}
			bw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}		
	}
	
//	public static void train(String[] trainingDataPaths, Dictionary dict, String svmFolder)
//	{
//		try{
//			List<Instance> instances = Training.readExamples(trainingDataPaths, dict);
//			String finalTrainingFile = svmFolder+"trainingExamples.txt";
//			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(finalTrainingFile)));
//			for(Instance instance : instances)
//			{
//				StringBuilder sb = new StringBuilder();
//				int count = 1;
//				double[] features = FeatureGenerator.getFeatureVector(instance);
//				sb.append(instance.label+" ");
//				for(double feature : features)
//				{
//					sb.append(count+":"+feature+" ");
//					count++;
//				}				
//				sb.append("\n");
//				bw.write(sb.toString());
//			}
//			bw.close();
//			
//			Runtime rt = Runtime.getRuntime();
//			Process pr = rt.exec("./"+svmFolder+"svm_learn "+finalTrainingFile+" "+svmFolder+"model");
//			System.out.println(pr.waitFor());
//		}
//		catch(Exception ex)
//		{
//			ex.printStackTrace();
//			System.exit(-1);
//		}
//	}
//
//	public static void train(String[] trainingDataPaths, Dictionary dict, String svmFolder)
//	{
//		try{
//			List<Instance> instances = Training.readExamples(trainingDataPaths, dict);						
//			for(Instance instance : instances)
//			{
//				StringBuilder sb = new StringBuilder();
//				int count = 1;
//				double[] features = FeatureGenerator.getFeatureVector(instance);
//				sb.append(instance.label+" ");
//				for(double feature : features)
//				{
//					sb.append(count+":"+feature+" ");
//					count++;
//				}				
//				sb.append("\n");
//				bw.write(sb.toString());
//			}
//			bw.close();
//			
//			Runtime rt = Runtime.getRuntime();
//			Process pr = rt.exec("./"+svmFolder+"svm_learn "+finalTrainingFile+" "+svmFolder+"model");
//			System.out.println(pr.waitFor());
//		}
//		catch(Exception ex)
//		{
//			ex.printStackTrace();
//			System.exit(-1);
//		}
//	}
	
	
//	public SVMLightModel train(String[] trainingDataPaths, SVMLightInterface trainer)
//	{
//		OrderedPair<Integer , LabeledFeatureVector[]> dimNumExamplesPair = getFeatureVectors(trainingDataPaths);
//		LabeledFeatureVector[] examples = dimNumExamplesPair.getR();		
//		return trainer.trainModel(examples);
//	}
//	
//	public SVMLightModel train(String[] trainingDataPaths, SVMLightInterface trainer, TrainingParameters params)
//	{
//		OrderedPair<Integer , LabeledFeatureVector[]> dimNumExamplesPair = getFeatureVectors(trainingDataPaths);
//		LabeledFeatureVector[] examples = dimNumExamplesPair.getR();
//		return trainer.trainModel(examples, params);
//	}
	
	public NormalSVMModel train(String[] trainingDataPaths, SVMLightInterface trainer)
	{
		OrderedPair<Integer , LabeledFeatureVector[]> dimNumExamplesPair = getFeatureVectors(trainingDataPaths);
		LabeledFeatureVector[] examples = dimNumExamplesPair.getR();		
		SVMLightModel model = trainer.trainModel(examples);
		return new NormalSVMModel(model);
	}
	
	public NormalSVMModel train(String[] trainingDataPaths, SVMLightInterface trainer, TrainingParameters params)
	{
		OrderedPair<Integer , LabeledFeatureVector[]> dimNumExamplesPair = getFeatureVectors(trainingDataPaths);
		LabeledFeatureVector[] examples = dimNumExamplesPair.getR();
		SVMLightModel model = trainer.trainModel(examples, params);
		return new NormalSVMModel(model);
	}
	
	public MinMaxSVMModel trainMinMaxNormal(String[] trainingDataPaths, SVMLightInterface trainer, TrainingParameters params)
	{
		OrderedPair<Integer , LabeledFeatureVector[]> dimNumExamplesPair = getFeatureVectors(trainingDataPaths);
		LabeledFeatureVector[] examples = dimNumExamplesPair.getR();
		int numFeatures = dimNumExamplesPair.getL();
		return MinMaxSVMModel.train(examples, numFeatures, trainer, params , -1 , 1);		
	}
	
	public MinMaxSVMModel trainMinMaxNormal(String[] trainingDataPaths, SVMLightInterface trainer)
	{
		OrderedPair<Integer , LabeledFeatureVector[]> dimNumExamplesPair = getFeatureVectors(trainingDataPaths);
		LabeledFeatureVector[] examples = dimNumExamplesPair.getR();
		int numFeatures = dimNumExamplesPair.getL();
		return MinMaxSVMModel.train(examples, numFeatures, trainer, -1 , 1);		
	}

	public ZScoreSVMModel trainZScoreNormal(String[] trainingDataPaths, SVMLightInterface trainer, TrainingParameters params)
	{
		OrderedPair<Integer , LabeledFeatureVector[]> dimNumExamplesPair = getFeatureVectors(trainingDataPaths);
		LabeledFeatureVector[] examples = dimNumExamplesPair.getR();
		int numFeatures = dimNumExamplesPair.getL();
		return ZScoreSVMModel.train(examples, numFeatures, trainer, params);		
	}
	
	public ZScoreSVMModel trainZScoreNormal(String[] trainingDataPaths, SVMLightInterface trainer)
	{
		OrderedPair<Integer , LabeledFeatureVector[]> dimNumExamplesPair = getFeatureVectors(trainingDataPaths);
		LabeledFeatureVector[] examples = dimNumExamplesPair.getR();
		int numFeatures = dimNumExamplesPair.getL();
		return ZScoreSVMModel.train(examples, numFeatures, trainer);		
	}
	
	public void generateARFFFormat(String[] trainingDataPaths, String outputPath)
	{
		OrderedPair<Integer , LabeledFeatureVector[]> dimNumExamplesPair = getFeatureVectors(trainingDataPaths);
		int featureNum = dimNumExamplesPair.getL().intValue();
		LabeledFeatureVector[] lfvs = dimNumExamplesPair.getR();
		writeDataARFF(lfvs, featureNum, outputPath);
	}
	
	public void generateSVMLightFormat(String[] trainingDataPaths, String outputPath)
	{
		OrderedPair<Integer , LabeledFeatureVector[]> dimNumExamplesPair = getFeatureVectors(trainingDataPaths);
		int featureNum = dimNumExamplesPair.getL().intValue();
		LabeledFeatureVector[] lfvs = dimNumExamplesPair.getR();
		writeDATASVMLight(lfvs, featureNum, outputPath);
	}
	
	public static void main(String[] args) {
		System.out.println("Training Running...");
		String propsFile30 = "resources/file_properties.xml";
		String dir = "/home/sumitb/Data/";
		String arg = "WordNet-3.0";
		String domainDataPathNoun = "/home/sumitb/Data/xwnd/joinedPOSSeparated/joinedNoun.txt";
		String OEDMappingPathNoun = "/home/sumitb/Data/navigli_sense_inventory/mergeData-30.offsets.noun";
		String sentimentFilePath = "/home/sumitb/Data/SentiWordNet/SentiWordNet.n";
		try{			
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();
			String svmFolder = "resources/Clustering/svmBinaries/";
			String path = "resources/Clustering/BinaryClassificationData/";
//			String[] trainingFiles = {path+"Noun1/PosTrain0.7.txt", path+"Noun1/NegTrain0.7.txt"};
			String[] trainingFiles = {path+"Noun1/PosTrain0.7.txt", path+"Noun1/NegTrainEqual.txt"};
//			Training.train(trainingFiles, dictionary, svmFolder);
			
			SVMLightInterface trainer = new SVMLightInterface();
			FeatureGenerator fg = new FeatureGenerator(dir, arg, domainDataPathNoun,dictionary, OEDMappingPathNoun, sentimentFilePath);
			Training trainingModule = new Training(dictionary, fg);
//			TrainingParameters tp = new TrainingParameters();
//			tp.getLearningParameters().verbosity = 1;
//			SVMLightModel model = trainingModule.train(trainingFiles, trainer, tp);
//			SVMLightModel model = trainingModule.train(trainingFiles, trainer);
//			model.writeModelToFile(svmFolder+"model2");
		}
		catch(Exception ex)
		{			
			ex.printStackTrace();
		}

	}

}
