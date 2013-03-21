package ezgraph.examples;

import ezgraph.*;
import java.util.*;

public class Example {

	public static void main ( String args[] ) throws Exception {
		String file = "example-graph.txt";
		if ( args.length > 0 ) file = args[0];
		System.out.print("Loading graph...");
		Graph graph = new Graph(file);
		System.out.println(" done.");
		System.out.print("Computing a subgraph...");
		Graph graph2 = graph.neighbourhoodGraph(new int[]{1,2},2);
		System.out.println(" done.");
		System.out.print("Computing PageRank...");
		PageRank pagerank = new PageRank(graph);
		System.out.println(" done.");
		System.out.print("Computing PageRank on a Subgraph...");
		PageRank pagerank2 = new PageRank(graph2);
		System.out.println(" done.");
		System.out.print("Computing PageRank on transpose graph...");
		PageRank pagerank3 = new PageRank(graph.transpose());
		System.out.println(" done.");
		System.out.print("Computing HITS...");
		HITS hits = new HITS(graph);
		System.out.println(" done.");
		System.out.print("Computing SimRank on a Subgraph...");
		SimRank simrank = new SimRank(graph2);
		System.out.println(" done.");
		System.out.print("Computing a Graph Clustering...");
		ChineseWhispersClustering clustering = new ChineseWhispersClustering(graph);
		System.out.println(" done.");
		System.out.print("Computing Graph Statistics Through Sampling...");
		SamplingStatistics sstats = new SamplingStatistics(graph,5000);
		System.out.println(" done.");
		System.out.println("Computing Connected Components...");
		StronglyConnectedComponents components = new StronglyConnectedComponents(graph);
		System.out.println(" done.");

		System.out.println("Computing Degree Statistics...");
		DegreeStatistics stats = new DegreeStatistics(graph,true);

		System.out.println("Min Indegree = " + stats.minIndegree());
		System.out.println("Max Indegree = " + stats.maxIndegree());
		System.out.println("Avg Indegree = " + stats.avgIndegree());

		System.out.println("Min Outdegree = " + stats.minOutdegree());
		System.out.println("Max Outdegree = " + stats.maxOutdegree());
		System.out.println("Avg Outdegree = " + stats.avgOutdegree());

		System.out.println("Min Degree = " + stats.minDegree());
		System.out.println("Max Degree = " + stats.maxDegree());
		System.out.println("Avg Degree = " + stats.avgDegree());

		System.out.println("Number of Nodes = " + graph.numNodes());
		System.out.println("Number of Arcs = " + graph.numArcs());
		System.out.println("Number of Clusters = " + clustering.numberOfClusters());
		System.out.println("Number of Strongly Connected Components = " + components.numberOfComponents());
		System.out.println("Clustering Coefficient = " + sstats.clusteringCoefficient());
		System.out.println("Avg Neighbours = " + sstats.avgNumNeighbors());
		System.out.println("Avg Triangles = " + sstats.avgNumTriangles());
		System.out.println("Avg Distance = " + sstats.avgDistance());

		System.out.println("Top 10 Nodes Sorted By PageRank");
		for ( String node : pagerank.getSortedNodes().subList(0,Math.min(10,graph.numNodes()))) System.out.println(node + "\t" + pagerank.getPageRankScore(node));

		System.out.println("Top 10 Nodes Sorted By PageRank on transpose");
		for ( String node : pagerank.getSortedNodes().subList(0,Math.min(10,graph.numNodes()))) System.out.println(node + "\t" + pagerank3.getPageRankScore(node));

		System.out.println("Top 10 Nodes Sorted By HITS Hub Score");
		for ( String node : hits.getSortedHubNodes().subList(0,Math.min(10,graph.numNodes()))) System.out.println(node + "\t" + hits.getHubScore(node));

		System.out.println("Top 10 Nodes Sorted By HITS Authority Score");
		for ( String node : hits.getSortedAuthorityNodes().subList(0,Math.min(10,graph.numNodes()))) System.out.println(node + "\t" + hits.getAuthorityScore(node));

		System.out.println("SimRank similarity for 100 random nodes");
		for ( int i=0; i<100; i++) {
			int n1 = new Random().nextInt(graph2.numNodes());
			int n2 = new Random().nextInt(graph2.numNodes());
			System.out.println( graph2.node(n1) + "\t" + graph2.node(n2) + "\t" + simrank.getSimRankScore(n1,n2));
		}

		System.out.println("Graph Clusters for the 10 first nodes");
		for ( int i=0; i<graph.numNodes() && i < 10; i++) System.out.println(graph.node(i) + "\t" + clustering.getCluster(i));

		System.out.println("Top 10 Nodes on Subgraph Sorted By PageRank");
		for ( String node : pagerank2.getSortedNodes().subList(0,Math.min(10,graph2.numNodes()))) System.out.println(node + "\t" + pagerank2.getPageRankScore(node));
	}

}
