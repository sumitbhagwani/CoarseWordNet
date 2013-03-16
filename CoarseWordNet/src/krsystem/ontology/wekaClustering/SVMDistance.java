package krsystem.ontology.wekaClustering;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Enumeration;

import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;

import jnisvmlight.FeatureVector;

import krsystem.ontology.senseClustering.svm.FeatureGenerator;
import krsystem.ontology.senseClustering.svm.ModelSVM;

import weka.clusterers.HierarchicalClusterer;
import weka.core.Attribute;
import weka.core.DistanceFunction;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.PerformanceStats;

public class SVMDistance implements DistanceFunction{

	ModelSVM modelSVM;
	Dictionary dict;
	FeatureGenerator fg;
	
	public static POS getPOSFromString(String posString)
	{
		if(posString.equalsIgnoreCase("n"))
			return POS.NOUN;
		else if(posString.equalsIgnoreCase("v"))
			return POS.VERB;
		else return null;
	}	
	
	@Override
	public String[] getOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration listOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOptions(String[] arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double distance(Instance arg0, Instance arg1) {
		// Instance is just a synsetOffset and a pos
		Attribute offset = new Attribute("offset");
		String offsetString0 = arg0.stringValue(offset);
		long offset0 = Long.parseLong(offsetString0);
		String offsetString1 = arg1.stringValue(offset);
		long offset1 = Long.parseLong(offsetString1);
		
		Attribute pos = new Attribute("pos");
		String pos0  = arg0.stringValue(pos);
		String pos1  = arg1.stringValue(pos);
		if(!pos0.equalsIgnoreCase(pos1))
		{
			System.out.println("Error : POS Mismatch while finding distance");
		}
		POS posReqd = getPOSFromString(pos0);		
		return 0;
	}

	@Override
	public double distance(Instance arg0, Instance arg1, PerformanceStats arg2)
			throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double distance(Instance arg0, Instance arg1, double arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double distance(Instance arg0, Instance arg1, double arg2,
			PerformanceStats arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getAttributeIndices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Instances getInstances() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getInvertSelection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void postProcessDistances(double[] arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAttributeIndices(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInstances(Instances arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInvertSelection(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Instance arg0) {
		// no update to be done
	}
	
	public static void main(String[] args)
	{
	}

}
