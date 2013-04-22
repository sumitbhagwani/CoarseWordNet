package simRank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class SimpleUndirectedGraph {

	public Graph<Vertex, Edge> graph;
	
	public SimpleUndirectedGraph(String[] files, double threshold)
	{
		graph = new UndirectedSparseGraph<Vertex, Edge>();
			
		System.out.println("Reading graph..");
		for(String path : files)
		{				
			try{
			System.out.println("Reading : "+path);	
			BufferedReader br = new BufferedReader(new FileReader(new File(path)));
			String line;
			while((line=br.readLine())!=null)
			{
				String[] lineSplit = line.split("\\s+");
				String label = "svmPrediction";
				String offset1 = lineSplit[0];
				String offset2 = lineSplit[1];
				double weight = Double.parseDouble(lineSplit[2]);
				if(weight > threshold)
					graph.addEdge(new Edge(label, weight), new Vertex("n",offset1) , new Vertex("n",offset2), EdgeType.UNDIRECTED);
			}			
			br.close();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				System.exit(-1);
			}
		}
		System.out.println("Graph read..");
		System.out.println("NumNodes : "+graph.getVertexCount());
		System.out.println("NumEdges : "+graph.getEdgeCount());
	}

}
