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
import krsystem.utility.OrderedPair;

public class MinMaxSVMModel extends ModelSVM{
		
	SVMLightModel svmModel;
	double[] min;
	double[] max;
	int featureNum;
	double minReqd = -1;
	double maxReqd = 1;		
	
	public MinMaxSVMModel(SVMLightModel modelPassed, int numberOfFeatures, double minReqdPassed, double maxReqdPassed) {	
		svmModel = modelPassed;
		featureNum = numberOfFeatures;
		min = new double[numberOfFeatures];
		max = new double[numberOfFeatures];
		minReqd = minReqdPassed;
		maxReqd = maxReqdPassed;
	}
	
	public MinMaxSVMModel(SVMLightModel modelPassed, int numberOfFeatures, double[] minPassed, double[] maxPassed, double minReqdPassed, double maxReqdPassed) {		
		svmModel = modelPassed;
		featureNum = numberOfFeatures;
		min = minPassed;
		max = maxPassed;
		minReqd = minReqdPassed;
		maxReqd = maxReqdPassed;
	}
			
	public static MinMaxSVMModel train(LabeledFeatureVector[] examples, int numFeatures, SVMLightInterface trainer, double minReqd, double maxReqd)
	{				
		double min[] = new double[numFeatures];		
		double max[] = new double[numFeatures];
		for(int i=0; i<numFeatures; i++)
		{
			min[i] =  Double.MAX_VALUE;
			max[i] =  Double.MIN_VALUE;
		}
		for(LabeledFeatureVector lfv : examples)
		{
			for(int i=1; i<=numFeatures; i++)
			{
				double value = lfv.getValueAt(i);
				min[i-1] = Math.min(min[i-1], value);
				max[i-1] = Math.max(max[i-1], value);
			}
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
				if(min[i]==max[i])
					vals[i] = val;
				else
					vals[i] = minReqd + (((val-min[i])/(max[i]-min[i]))*(maxReqd-minReqd));					
			}
			normalizedExamples[j] = new LabeledFeatureVector(lfv.getLabel(), dims, vals);
			j++;
		}						

		SVMLightModel model = trainer.trainModel(normalizedExamples);
		MinMaxSVMModel minMaxModel = new MinMaxSVMModel(model, numFeatures, min, max, minReqd, maxReqd);
		return minMaxModel;		
	}
		
	public static MinMaxSVMModel train(LabeledFeatureVector[] examples, int numFeatures, SVMLightInterface trainer, TrainingParameters params , double minReqd, double maxReqd)
	{		
		double min[] = new double[numFeatures];		
		double max[] = new double[numFeatures];
		for(int i=0; i<numFeatures; i++)
		{
			min[i] =  Double.MAX_VALUE;
			max[i] =  Double.MIN_VALUE;
		}
		for(LabeledFeatureVector lfv : examples)
		{
			for(int i=0; i<numFeatures; i++)
			{				
				double value = lfv.getValueAt(i);
				min[i] = Math.min(min[i], value);
				max[i] = Math.max(max[i], value);
			}
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
				if(min[i]==max[i])
					vals[i] = val;
				else
					vals[i] = minReqd + (((val-min[i])/(max[i]-min[i]))*(maxReqd-minReqd));					
			}
			normalizedExamples[j] = new LabeledFeatureVector(lfv.getLabel(), dims, vals);
			j++;
		}						

		SVMLightModel model = trainer.trainModel(normalizedExamples, params);
		MinMaxSVMModel minMaxModel = new MinMaxSVMModel(model, numFeatures, min, max, minReqd, maxReqd);
		return minMaxModel;		
	}
		
	public static MinMaxSVMModel train(LabeledFeatureVector[] examples, int numFeatures, SVMLightInterface trainer, TrainingParameters params , double minReqd, double maxReqd, String arffOutputPath, String SVMLightOutputPath)
	{		
		double min[] = new double[numFeatures];		
		double max[] = new double[numFeatures];
		for(int i=0; i<numFeatures; i++)
		{
			min[i] =  Double.MAX_VALUE;
			max[i] =  Double.MIN_VALUE;
		}
		for(LabeledFeatureVector lfv : examples)
		{
			for(int i=0; i<numFeatures; i++)
			{				
				double value = lfv.getValueAt(i);
				min[i] = Math.min(min[i], value);
				max[i] = Math.max(max[i], value);
			}
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
				if(min[i]==max[i])
					vals[i] = val;
				else
					vals[i] = minReqd + (((val-min[i])/(max[i]-min[i]))*(maxReqd-minReqd));					
			}
			normalizedExamples[j] = new LabeledFeatureVector(lfv.getLabel(), dims, vals);
			j++;
		}						

		if(arffOutputPath.length() > 0)
		{
			Training.writeDataARFF(normalizedExamples, numFeatures, arffOutputPath);
		}
		
		if(SVMLightOutputPath.length() > 0)
		{
			Training.writeDATASVMLight(normalizedExamples, numFeatures, SVMLightOutputPath);
		}
		
		SVMLightModel model = trainer.trainModel(normalizedExamples, params);
		MinMaxSVMModel minMaxModel = new MinMaxSVMModel(model, numFeatures, min, max, minReqd, maxReqd);
		return minMaxModel;		
	}	
	
	public double classify(FeatureVector fv)
	{
		int[] dims = new int[featureNum];
		double[] vals = new double[featureNum];
		for(int i=0; i<featureNum;i++)
		{
			dims[i] = i+1;
			double val = fv.getValueAt(i);
			if(min[i]==max[i])
				vals[i] = val;
			else
				vals[i] = minReqd + (((val-min[i])/(max[i]-min[i]))*(maxReqd-minReqd));					
		}
		FeatureVector newFV = new FeatureVector(dims, vals);
		return svmModel.classify(newFV);
	}		
	
	public void writeModel(String pathForSVMModel, String PathForMinMax)
	{
		svmModel.writeModelToFile(pathForSVMModel);
		try{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(PathForMinMax)));
		bw.write("#featureNum "+featureNum+"\n");
		bw.write("#minReqd "+minReqd+"\n");
		bw.write("#maxReqd "+maxReqd+"\n");		
		for(int i=0; i<featureNum; i++)
		{
			bw.write(i+" "+min[i]+" "+max[i]+"\n");
		}
		bw.close();		
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static MinMaxSVMModel readModel(String pathForSVMModel, String PathForMinMax)
	{
		try{		
		SVMLightModel svmModel = SVMLightModel.readSVMLightModelFromURL(new File(pathForSVMModel).toURL());
		BufferedReader br = new BufferedReader(new FileReader(new File(PathForMinMax)));
		
		String featureNumString = br.readLine();
		int featureNum = Integer.parseInt(featureNumString.split("\\s+")[1]);
		
		String minReqdString = br.readLine();
		double minReqd = Double.parseDouble(minReqdString.split("\\s+")[1]);
		
		String maxReqdString = br.readLine();
		double maxReqd = Double.parseDouble(maxReqdString.split("\\s+")[1]);
		
		double[] min = new double[featureNum];
		double[] max = new double[featureNum];
		
		for(int i=0;i<featureNum;i++)
		{
			String[] minMaxLine = br.readLine().split("\\s+");
			min[i] = Double.parseDouble(minMaxLine[1]);
			max[i] = Double.parseDouble(minMaxLine[2]);
		}		
		br.close();
		
		return new MinMaxSVMModel(svmModel, featureNum, min, max, minReqd, maxReqd);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
		return null;
	}		
	
}
