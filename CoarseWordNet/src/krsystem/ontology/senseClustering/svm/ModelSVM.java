package krsystem.ontology.senseClustering.svm;

import jnisvmlight.FeatureVector;

public abstract class ModelSVM {		
		
	public abstract double classify(FeatureVector lfv);
	
	public abstract void writeModel(String pathForSVMModel, String PathForOtherVariables);

//	public abstract ModelSVM train(LabeledFeatureVector[] examples);
	
//	public abstract ModelSVM train(LabeledFeatureVector[] examples, TrainingParameters tp);
	
}
