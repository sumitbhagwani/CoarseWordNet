package krsystem.ontology.senseClustering.svm;

import jnisvmlight.FeatureVector;
import jnisvmlight.SVMLightInterface;
import jnisvmlight.SVMLightModel;

public class NormalSVMModel extends ModelSVM{
		
	SVMLightModel svmModel;

	public NormalSVMModel(SVMLightModel modelPassed)
	{
		svmModel = modelPassed;
	}
	
	public double classify(FeatureVector fv)
	{
		return svmModel.classify(fv);
	}
	
	public void writeModel(String pathForSVMModel, String PathForOtherVariables) //second variable in unnecessary
	{
		svmModel.writeModelToFile(pathForSVMModel);
	}
	
}
