package ch.usi.inf.sape.hac.experiment;

import java.util.HashMap;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class MyExperiment implements Experiment{

	Instances instances;
	HashMap<String, Integer> offsetToIdMap = new HashMap<String, Integer>();
	
	public MyExperiment(String instancesPath)
	{		
		try{
			DataSource source = new DataSource(instancesPath);
			instances = source.getDataSet();
			Attribute offset = instances.attribute("offset");
			System.out.println("Working on "+instances.numInstances()+" instances");			
			for(int i=0; i<instances.numInstances(); i++)
			{							
				Instance instanceI = instances.instance(i);
				String offsetString = instanceI.stringValue(offset);
				offsetToIdMap.put(offsetString, i);
			}
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
	
	public int getIndex(String offsetString)
	{
		Integer intVal = offsetToIdMap.get(offsetString);
		if(intVal == null)
			return -1;
		else
			return intVal.intValue();
	}


}
