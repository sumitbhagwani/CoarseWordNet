package simRankEZGraph;

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
		String file = "example-graph.txt";
		if ( args.length > 0 ) file = args[0];
		System.out.print("Loading graph...");
		Graph graph = new Graph(file);
		System.out.println(" done.");
		System.out.print("Computing SimRank on a Graph ...");
		SimRank simrank = new SimRank(graph);
		System.out.println(" done.");
		System.out.println("SimRank similarity for 100 random nodes");
		for ( int i=0; i<10; i++) {
			int n1 = new Random().nextInt(graph.numNodes());
			int n2 = new Random().nextInt(graph.numNodes());
			System.out.println( graph.node(n1) + "\t" + graph.node(n2) + "\t" + simrank.getSimRankScore(n1,n2));
		}
	}

}
