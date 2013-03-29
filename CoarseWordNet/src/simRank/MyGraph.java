package simRank;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class MyGraph {

	Graph graph;
	
	HashMap<Vertex, Integer> vertexToIdMap = new HashMap<Vertex, Integer>();
	HashMap<Integer, Vertex> idToVertexMap = new HashMap<Integer, Vertex>();
	
	public MyGraph(String[] files, boolean directedEdges)
	{
		if(directedEdges)
			graph = new DirectedSparseMultigraph<Vertex, Edge>();
		else
			graph = new UndirectedSparseMultigraph<Vertex, Edge>();
			
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
				String label = lineSplit[0];
				String offset1 = lineSplit[1];
				String offset2 = lineSplit[2];
				double weight = Double.parseDouble(lineSplit[3]);	
				if(directedEdges)
					graph.addEdge(new Edge(label, weight), new Vertex("n",offset1) , new Vertex("n",offset2), EdgeType.DIRECTED);
				else
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
		int index = 0;
		for(Object vertex : graph.getVertices())
		{
			vertexToIdMap.put((Vertex)vertex, index);
			idToVertexMap.put(index, (Vertex)vertex);
			index++;
		}
	}
	
	public void writeIdToVertexMap(String idToVertexMapPath)
	{
		int numNodes = graph.getVertexCount();
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(idToVertexMapPath)));						
			for(int i=0; i <numNodes; i++)			
				bw.write(i+" "+idToVertexMap.get(i)+"\n");			
			bw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
