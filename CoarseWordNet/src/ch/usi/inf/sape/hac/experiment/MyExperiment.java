package ch.usi.inf.sape.hac.experiment;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class MyExperiment implements Experiment{

	Instances instances;
	
	public MyExperiment(String instancesPath)
	{		
		try{
			DataSource source = new DataSource(instancesPath);
			instances = source.getDataSet();
			System.out.println("Working on "+instances.numInstances()+" instances");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();			
		}
	}
	
	@Override
	public int getNumberOfObservations() { 
		return instances.numInstances();
	}

	@Override
	public Instance getObservation(int index) {
		return instances.instance(index);
	}


}
