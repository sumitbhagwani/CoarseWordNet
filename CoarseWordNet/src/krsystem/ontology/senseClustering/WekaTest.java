package krsystem.ontology.senseClustering;

import weka.attributeSelection.GainRatioAttributeEval;
import weka.classifiers.functions.LibSVM;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ConverterUtils.DataSource;

public class WekaTest {

	public static void test1() {
		try{
			 String arffPath = "/home/sumitb/Data/wekaRelated/nounAnalysis.arff";
			 DataSource source = new DataSource(arffPath);
			 Instances data = source.getDataSet();
			 // setting class attribute if the data format does not provide this information
			 // For example, the XRFF format saves the class attribute information as well
			 if (data.classIndex() == -1)
			   data.setClassIndex(data.numAttributes() - 1);
			GainRatioAttributeEval evaluator = new GainRatioAttributeEval();
			evaluator.buildEvaluator(data);
			for(int i=0; i<data.numAttributes()-1;i++)
			{
				System.out.println(evaluator.evaluateAttribute(i));
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);			
		}
	}
	
	public static void test2() {
		try{
			 String arffPath = "/home/sumitb/Data/wekaRelated/nounAnalysis.arff";
			 DataSource source = new DataSource(arffPath);
			 Instances data = source.getDataSet();
			 // setting class attribute if the data format does not provide this information
			 // For example, the XRFF format saves the class attribute information as well
			 if (data.classIndex() == -1)
				 data.setClassIndex(data.numAttributes() - 1);
			 
	        //initialize svm classifier
	        LibSVM svm = new LibSVM();	        
	        svm.buildClassifier(data);	        	        	                		
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);			
		}
	}
	
	public static void main(String[] args) 
	{		
		test2();
	}
			

}
