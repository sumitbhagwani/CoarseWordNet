package simRank;

import java.io.IOException;
import java.util.Random;

import ezgraph.Graph;
import ezgraph.SimRank;

public class EZGraphTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String sampleFile = "resources/wn30Relations/sample";
//		String hypernymFile = "resources/wn30Relations/hypernym";
//		String hyponymFile = "resources/wn30Relations/hyponym";
//		String[] files = {hypernymFile, hyponymFile};		
		String[] files = {sampleFile};
		System.out.print("Loading graph...");
		Graph graph = new Graph(files);
		System.out.println(" done.");
		System.out.print("Computing SimRank on a Graph ...");
		long startTime = System.currentTimeMillis();
		SimRank simrank = new SimRank(graph);
		long endTime = System.currentTimeMillis();
		System.out.println(" done in "+(endTime-startTime)+" milliseconds");
		System.out.println("SimRank similarity for random node pairs");
		
		for ( int i=0; i<graph.numNodes(); i++) {
			for ( int j=0; j<graph.numNodes(); j++) 
				System.out.print(simrank.getSimRankScore(i,j)+" ");
			System.out.println();
		}	
	}

}
