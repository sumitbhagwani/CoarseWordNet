package ch.usi.inf.sape.hac.experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import ch.usi.inf.sape.hac.HierarchicalAgglomerativeClusterer;
import ch.usi.inf.sape.hac.agglomeration.AgglomerationMethod;
import ch.usi.inf.sape.hac.agglomeration.AverageLinkage;
import ch.usi.inf.sape.hac.agglomeration.SingleLinkage;
import ch.usi.inf.sape.hac.dendrogram.Dendrogram;
import ch.usi.inf.sape.hac.dendrogram.DendrogramBuilder;

public class MyClusteringExperiment {

	private static double[][] computeDissimilarityMatrix(MyExperiment experiment, String simScoresFile) {
		System.out.println("Computing Dissimilarity Matrix ...");
        final double[][] dissimilarityMatrix = new double[experiment.getNumberOfObservations()][experiment.getNumberOfObservations()];
        // fill diagonal
        for (int o = 0; o<dissimilarityMatrix.length; o++) {
            dissimilarityMatrix[o][o] = 0.0;
        }
        
        try{
        	BufferedReader br = new BufferedReader(new FileReader(new File(simScoresFile)));
        	String line;
        	int i = 0;
        	while((line = br.readLine()) != null)
        	{
        		String[] lineSplit = line.split("\\s+");
        		int index0 = experiment.getIndex(lineSplit[0]);
        		int index1 = experiment.getIndex(lineSplit[1]);
        		double similarity = Double.parseDouble(lineSplit[2]);
        		dissimilarityMatrix[index0][index1] = -similarity;
        		dissimilarityMatrix[index1][index0] = -similarity;
        		i++;
        		if(i%1000 == 0)
        			System.out.println(i);
        	}
        	br.close();
        } catch(Exception ex)
        {
        	ex.printStackTrace();
        }
        System.out.println("Computed Dissimilarity Matrix ...");
        return dissimilarityMatrix;
	}
	
	public static void main(String[] args) throws Exception {
		String instancesPath = "resources/Clustering/HAC/offsetsNoun.arff";
		String simScoresFile = "resources/Clustering/PopulatingDB/simValuesSVM.noun";
		Experiment experiment = new MyExperiment(instancesPath);
		DissimilarityMeasure dissimilarityMeasure = new MyDissimilarityMeasureSQL();
		AgglomerationMethod agglomerationMethod = new AverageLinkage();
		DendrogramBuilder dendrogramBuilder = new DendrogramBuilder(experiment.getNumberOfObservations());
		HierarchicalAgglomerativeClusterer clusterer = new HierarchicalAgglomerativeClusterer(experiment, dissimilarityMeasure, agglomerationMethod);		
		clusterer.cluster(dendrogramBuilder, computeDissimilarityMatrix((MyExperiment)experiment, simScoresFile));
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
