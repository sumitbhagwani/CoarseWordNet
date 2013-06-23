package connectedComponentAnalysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import simRank.Edge;
import simRank.SimpleUndirectedGraph;
import simRank.Vertex;

public class ConnectedComponents {
	
	List<Set<String>> clusterList;
	HashMap<String, Integer> vertexToId;
	int numSynsets = 82115;
	
	public ConnectedComponents(String[] similarityFiles, double threshold)
	{
		clusterList = new ArrayList<Set<String>>();
		vertexToId = new HashMap<String, Integer>();		
		SimpleUndirectedGraph g = new SimpleUndirectedGraph(similarityFiles, threshold);
		WeakComponentClusterer<Vertex, Edge> clusterer = new WeakComponentClusterer<Vertex, Edge>();
		Set<Set<Vertex>> clusters = clusterer.transform(g.graph);
		int i = 0;
		for(Set<Vertex> cluster : clusters)
		{
			HashSet<String> offsetCollection = new HashSet<String>();
			for(Vertex v : cluster)
			{
				vertexToId.put(v.getOffset(), i);				
				offsetCollection.add(v.getOffset());
			}
			clusterList.add(offsetCollection);
			i++;
		}
		int numVertices = g.graph.getVertexCount();
		System.out.println("Obtained "+clusters.size()+" clusters over "+numVertices+ " vertices.");		
		int numClusters = (numSynsets-numVertices) + clusters.size();
		System.out.println("Overall numClusters = "+numClusters);		
	}
		
	public ConnectedComponents(List<String> similarityPairs, double threshold)
	{
		clusterList = new ArrayList<Set<String>>();
		vertexToId = new HashMap<String, Integer>();		
		SimpleUndirectedGraph g = new SimpleUndirectedGraph(similarityPairs, threshold);
		WeakComponentClusterer<Vertex, Edge> clusterer = new WeakComponentClusterer<Vertex, Edge>();
		Set<Set<Vertex>> clusters = clusterer.transform(g.graph);
		int i = 0;
		for(Set<Vertex> cluster : clusters)
		{
			HashSet<String> offsetCollection = new HashSet<String>();
			for(Vertex v : cluster)
			{
				vertexToId.put(v.getOffset(), i);				
				offsetCollection.add(v.getOffset());
			}
			clusterList.add(offsetCollection);
			i++;
		}
		int numVertices = g.graph.getVertexCount();
		System.out.println("Obtained "+clusters.size()+" clusters over "+numVertices+ " vertices.");		
		int numClusters = (numSynsets-numVertices) + clusters.size();
		System.out.println("Overall numClusters = "+numClusters);		
	}
	
	public static void test(){
//		String[] files = {"resources/Clustering/PopulatingDB/simValuesSVM.noun"};
		String[] files = {"resources/Clustering/PopulatingDB/simValuesSVMTransformed.noun"};
		SimpleUndirectedGraph g = new SimpleUndirectedGraph(files, 1.0);
		WeakComponentClusterer<Vertex, Edge> clusterer = new WeakComponentClusterer<Vertex, Edge>();
		Set<Set<Vertex>> clusters = clusterer.transform(g.graph);
		HashMap<String, Integer> vertexToId = new HashMap<String, Integer>();
		int i = 0;
		for(Set<Vertex> cluster : clusters)
		{
			for(Vertex v : cluster)
			{
				vertexToId.put(v.getOffset(), i);
			}
			i++;
		}
		System.out.println(clusters.size());
		int numSynsets = 82115;
		int numClusters = (numSynsets-g.graph.getVertexCount()) + clusters.size();
		System.out.println(numClusters);
	}
	
	public Integer getVertexId(String synset)
	{
		return vertexToId.get(synset);
	}
	
}
