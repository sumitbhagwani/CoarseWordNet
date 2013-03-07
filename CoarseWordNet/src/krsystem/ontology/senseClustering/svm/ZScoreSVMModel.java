package krsystem.ontology.senseClustering.svm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import jnisvmlight.FeatureVector;
import jnisvmlight.LabeledFeatureVector;
import jnisvmlight.SVMLightInterface;
import jnisvmlight.SVMLightModel;
import jnisvmlight.TrainingParameters;

public class ZScoreSVMModel extends ModelSVM{
		
	SVMLightModel svmModel;
	double[] mean;
	double[] std;
	int featureNum;		
	
	public ZScoreSVMModel(SVMLightModel modelPassed, int numberOfFeatures) {	
		svmModel = modelPassed;
		featureNum = numberOfFeatures;
		mean = new double[numberOfFeatures];
		std = new double[numberOfFeatures];		
	}
	
	public ZScoreSVMModel(SVMLightModel modelPassed, int numberOfFeatures, double[] meanPassed, double[] stdPassed) {		
		svmModel = modelPassed;
		featureNum = numberOfFeatures;
		mean = meanPassed;
		std = stdPassed;
	}
			
	public static ZScoreSVMModel train(LabeledFeatureVector[] examples, int numFeatures, SVMLightInterface trainer)
	{				
		double mean[] = new double[numFeatures];		
		double std[] = new double[numFeatures];
		int numExamples = examples.length;
		for(LabeledFeatureVector lfv : examples)
		{
			for(int i=0; i<numFeatures; i++)
			{
				double value = lfv.getValueAt(i);
				mean[i] = mean[i] + (value/numFeatures);				
			}
		}				
		
		for(LabeledFeatureVector lfv : examples)
		{
			for(int i=0; i<numFeatures; i++)
			{
				double value = lfv.getValueAt(i);
				std[i] += (value - mean[i])*(value - mean[i]);				
			}
		}
		
		for(int i=0; i<numFeatures; i++)
		{
			std[i] = Math.sqrt(std[i]/(numExamples-1)); // unbiased estimator
		}		
		
		LabeledFeatureVector[] normalizedExamples = new LabeledFeatureVector[examples.length];
		int j=0;
		for(LabeledFeatureVector lfv : examples)
		{
			int[] dims = new int[numFeatures];
			double[] vals = new double[numFeatures];
			for(int i=0; i<numFeatures;i++)
			{
				dims[i] = i+1;
				double val = lfv.getValueAt(i+1);
				if(std[i] == 0)
					vals[i] = val;
				else
					vals[i] = (val-mean[i])/std[i];					
			}
			normalizedExamples[j] = new LabeledFeatureVector(lfv.getLabel(), dims, vals);
			j++;
		}						

		SVMLightModel model = trainer.trainModel(normalizedExamples);
		ZScoreSVMModel zScoreSVMModel = new ZScoreSVMModel(model, numFeatures, mean, std);
		return zScoreSVMModel;		
	}
			
	public static ZScoreSVMModel train(LabeledFeatureVector[] examples, int numFeatures, SVMLightInterface trainer, TrainingParameters params)
	{				
		double mean[] = new double[numFeatures];		
		double std[] = new double[numFeatures];
		int numExamples = examples.length;
		for(LabeledFeatureVector lfv : examples)
		{
			for(int i=0; i<numFeatures; i++)
			{
				double value = lfv.getValueAt(i);
				mean[i] = mean[i] + (value/numFeatures);				
			}
		}				
		
		for(LabeledFeatureVector lfv : examples)
		{
			for(int i=0; i<numFeatures; i++)
			{
				double value = lfv.getValueAt(i);
				std[i] += (value - mean[i])*(value - mean[i]);				
			}
		}
		
		for(int i=0; i<numFeatures; i++)
		{
			std[i] = Math.sqrt(std[i]/(numExamples-1)); // unbiased estimator
		}		
		
		LabeledFeatureVector[] normalizedExamples = new LabeledFeatureVector[examples.length];
		int j=0;
		for(LabeledFeatureVector lfv : examples)
		{
			int[] dims = new int[numFeatures];
			double[] vals = new double[numFeatures];
			for(int i=0; i<numFeatures;i++)
			{
				dims[i] = i+1;
				double val = lfv.getValueAt(i);
				if(std[i] == 0)
					vals[i] = val;
				else
					vals[i] = (val-mean[i])/std[i];					
			}
			normalizedExamples[j] = new LabeledFeatureVector(lfv.getLabel(), dims, vals);
			j++;
		}						

		SVMLightModel model = trainer.trainModel(normalizedExamples);
		ZScoreSVMModel zScoreSVMModel = new ZScoreSVMModel(model, numFeatures, mean, std);
		return zScoreSVMModel;		
	}			
	public double classify(FeatureVector fv)
	{
		int[] dims = new int[featureNum];
		double[] vals = new double[featureNum];
		for(int i=0; i<featureNum;i++)
		{
			dims[i] = i+1;
			double val = fv.getValueAt(i);
			if(std[i] == 0)
				vals[i] = val;
			else
				vals[i] = (val-mean[i])/std[i];					
		}
		FeatureVector newFV = new FeatureVector(dims, vals);
		return svmModel.classify(newFV);
	}		
	
	public void writeModel(String pathForSVMModel, String PathForMeanStd)
	{
		svmModel.writeModelToFile(pathForSVMModel);
		try{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(PathForMeanStd)));
		bw.write("#featureNum "+featureNum+"\n");			
		for(int i=0; i<featureNum; i++)
		{
			bw.write(i+" "+mean[i]+" "+std[i]+"\n");
		}
		bw.close();		
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static ZScoreSVMModel readModel(String pathForSVMModel, String PathForMeanStd)
	{
		try{		
		SVMLightModel svmModel = SVMLightModel.readSVMLightModelFromURL(new File(pathForSVMModel).toURL());
		BufferedReader br = new BufferedReader(new FileReader(new File(PathForMeanStd)));
		
		String featureNumString = br.readLine();
		int featureNum = Integer.parseInt(featureNumString.split("\\s+")[1]);
			
		double[] mean = new double[featureNum];
		double[] std = new double[featureNum];
		
		for(int i=0;i<featureNum;i++)
		{
			String[] minMaxLine = br.readLine().split("\\s+");
			mean[i] = Double.parseDouble(minMaxLine[1]);
			std[i] = Double.parseDouble(minMaxLine[2]);
		}		
		br.close();
		
		return new ZScoreSVMModel(svmModel, featureNum, mean, std);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
		return null;
	}
	
}
