package ezgraph.examples;

import ezgraph.*;
import java.util.*;

public class PageRankOnSubGraph {

	public static void main ( String args[] ) throws Exception {
		int numExperiments = 10;
		int numNodes = 500;
		long totalTime = 0;
		String file = "example-graph.txt";
		if ( args.length > 0 ) file = args[0];
		System.out.print("Loading graph...");
		Graph graph = new Graph(file);
		System.out.println(" done.");
		System.out.println("Graph has " + graph.numNodes() + " nodes and " + graph.numArcs() + " arcs.");
		for ( int i = 0 ; i < numExperiments ; i++ ) {
			int nodes[] = new int[numNodes];
			for ( int j = 0 ; j < numNodes ; j++ ) nodes[j] = new Random().nextInt(graph.numNodes() - 1);
			long time1 = System.currentTimeMillis();
			System.out.print("Computing a subgraph...");
			Graph graph2 = graph.neighbourhoodGraph(nodes,1);
			System.out.println(" done.");
			System.out.print("Computing PageRank on a subgraph with " + graph2.numNodes() + " nodes...");
			PageRank pagerank2 = new PageRank(graph2);
			System.out.println(" done.");
			long time2 = System.currentTimeMillis();
			totalTime += Math.abs(time2 - time1);
			if ( i+1 == numExperiments ) {
			  System.out.println("Top 10 Nodes on Subgraph Sorted By PageRank");
			  for ( String node : pagerank2.getSortedNodes().subList(0,Math.min(10,graph2.numNodes()))) System.out.println(node + "\t" + pagerank2.getPageRankScore(node));
			}
		}
		System.out.println("Average duration for each experiment : " + (long)( totalTime / numExperiments ) );
	}

}
