package ch.usi.inf.sape.hac.experiment;

import ch.usi.inf.sape.hac.HierarchicalAgglomerativeClusterer;
import ch.usi.inf.sape.hac.agglomeration.AgglomerationMethod;
import ch.usi.inf.sape.hac.agglomeration.AverageLinkage;
import ch.usi.inf.sape.hac.agglomeration.SingleLinkage;
import ch.usi.inf.sape.hac.dendrogram.Dendrogram;
import ch.usi.inf.sape.hac.dendrogram.DendrogramBuilder;

public class MyClusteringExperiment {

	public static void main(String[] args) throws Exception {
		String instancesPath = "resources/Clustering/HAC/offsetsNoun.arff";
		Experiment experiment = new MyExperiment(instancesPath);
		DissimilarityMeasure dissimilarityMeasure = new MyDissimilarityMeasure();
		AgglomerationMethod agglomerationMethod = new AverageLinkage();
		DendrogramBuilder dendrogramBuilder = new DendrogramBuilder(experiment.getNumberOfObservations());
		HierarchicalAgglomerativeClusterer clusterer = new HierarchicalAgglomerativeClusterer(experiment, dissimilarityMeasure, agglomerationMethod);
		clusterer.cluster(dendrogramBuilder);
		Dendrogram dendrogram = dendrogramBuilder.getDendrogram();
//		System.out.println("---------------------------");
//		dendrogram.dump();
		String treePath = "resources/Clustering/HAC/offsetsNoun.tree";
		try{
			dendrogram.writeToFile(treePath);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
//		System.out.println("---------------------------");
//		Dendrogram.readFromFile(treePath).dump();
	}
}
