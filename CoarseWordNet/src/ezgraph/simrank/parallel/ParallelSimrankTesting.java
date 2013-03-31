package ezgraph.simrank.parallel;

import java.io.IOException;

import ezgraph.Graph;
import ezgraph.UndirectedGraph;

public class ParallelSimrankTesting {

	public static void main(String[] args) throws IOException {
		String sampleFile = "resources/wn30Relations/sample";
		String hypernymFile = "resources/wn30Relations/hypernym";
		String hyponymFile = "resources/wn30Relations/hyponym";
		String meronymFile = "resources/wn30Relations/meronym";
		String holonymFile = "resources/wn30Relations/holonym";
		String idToVertexMapPath = "resources/wn30Relations/directedIdToVertexMap2.txt";
		String simrankOutputPath = "resources/wn30Relations/simrankMatrixIteration";
		
		double threshold = 0.0000000001;
		int maxIter = 10;
		
//		String[] files = {hypernymFile, hyponymFile, holonymFile, meronymFile};
//		String[] files = {hypernymFile, meronymFile};
		String[] files = {holonymFile};
		
		System.out.print("Loading graph...");
		Graph graph = new UndirectedGraph(files);
		System.out.println(" done.");
		
		System.out.println("Writing Graph IdToVertexMap files ...");
	    graph.writeIdToVertexMap(idToVertexMapPath);
		
		System.out.println("Computing SimRank on a Graph ...");
		long startTime = System.currentTimeMillis();
		ParallelSimRank simrank = new ParallelSimRank(graph,threshold,maxIter, simrankOutputPath);
		long endTime = System.currentTimeMillis();
		System.out.println(" done in "+(endTime-startTime)+" milliseconds");
		
		System.out.println("Writing Final SimRank values ...");
		simrank.writeScores(simrankOutputPath+"Final");		
	}

}
