package krsystem.ontology.wekaClustering;

import java.io.File;
import java.io.FileReader;

import weka.clusterers.HierarchicalClusterer;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import weka.core.converters.ConverterUtils.DataSource;

public class ClusterSynsets {

	public static void main(String[] args) {
		HierarchicalClusterer hc = new HierarchicalClusterer();
//		// Create empty instance with three attribute values 
//		Instance inst = new Instance(3);
//		Instance inst1 = new Instance(3);
//		Instance inst2 = new Instance(3);
//		Attribute offset = new Attribute("offset");
//		Attribute pos = new Attribute("pos");
//
//		inst.setValue(offset, "00000000"); 
//		inst.setValue(pos, "n");
//		inst1.setValue(offset, "11111111"); 
//		inst1.setValue(pos, "n"); 
//		inst2.setValue(offset, "00019613"); 
//		inst2.setValue(pos, "n"); 

		String instancesPath = "resources/Clustering/sample.arff";
		try{
			DataSource source = new DataSource(instancesPath);
			Instances instances = source.getDataSet();
			Instance zero = instances.instance(0);
			System.out.println(zero);			
//			Attribute offset = instances.attribute("offset");
//			Attribute offset = zero.dataset().attribute("offset");
//			String offsetString0 = zero.toString(offset);
//			System.out.println(offsetString0);
			SQLDistance sqld = new SQLDistance();
			hc.setDistanceFunction(sqld);
			hc.setDebug(true);
			hc.setPrintNewick(true);			
			hc.buildClusterer(instances);		
			System.out.println("1 " +hc);
			System.out.println("2 " +hc.getNumClusters());
			System.out.println("3 " +hc.graph());
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}

}
