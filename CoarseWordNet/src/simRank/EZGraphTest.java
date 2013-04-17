package simRank;

import java.io.IOException;
import java.util.Random;

import krsystem.StaticValues;

import ezgraph.Graph;
import ezgraph.SimRank;
import ezgraph.UndirectedGraph;

public class EZGraphTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String sampleFile = "resources/wn30Relations/sample";
		String hypernymFile = "resources/wn30Relations/hypernym";
		String hyponymFile = "resources/wn30Relations/hyponym";
		String meronymFile = "resources/wn30Relations/meronym";
		String holonymFile = "resources/wn30Relations/holonym";
		String simrankExperimentPath = StaticValues.dataPath+"simrankExperiment/";
		String idToVertexMapPath = simrankExperimentPath+"undirectedIdToVertexMapSVMTransformed.txt";
		String simrankOutputPath = simrankExperimentPath+"simrankMatrixIterationSVMTransformed";
		String initFile = "resources/Clustering/PopulatingDB/simValuesSVMTransformed.noun";
		
		String addition_1_0 = "resources/Clustering/PopulatingDB/simValuesSVMTransformed.noun.1.0";
		String addition_0_8 = "resources/Clustering/PopulatingDB/simValuesSVMTransformed.noun.0.8";
		
		double threshold = 0.00001;
		int maxIter = 10;
		
//		String[] files = {hypernymFile, hyponymFile, holonymFile, meronymFile};
//		String[] files = {hypernymFile, meronymFile};
		String[] files = {hypernymFile, meronymFile, addition_1_0};
//		String[] files = {holonymFile};
		
		System.out.print("Loading graph...");
		Graph graph = new UndirectedGraph(files);
		System.out.println(" done.");
		
		System.out.println("Writing Graph IdToVertexMap files ...");
	    graph.writeIdToVertexMap(idToVertexMapPath);
		
		System.out.println("Computing SimRank on a Graph ...");
		long startTime = System.currentTimeMillis();
		SimRank simrank = new SimRank(graph,threshold,maxIter, simrankOutputPath, initFile);
		long endTime = System.currentTimeMillis();
		System.out.println(" done in "+(endTime-startTime)+" milliseconds");
		
		System.out.println("Writing Final SimRank values ...");
		simrank.writeScores(simrankOutputPath+"Final");		
	}

}
