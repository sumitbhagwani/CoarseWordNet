package ezgraph.simrank.parallel;

import es.yrbcn.graph.weighted.*;
import ezgraph.Graph;
import ezgraph.SparseMatrix;
import it.unimi.dsi.webgraph.*;
import it.unimi.dsi.webgraph.labelling.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;
import java.lang.reflect.*;

import com.sun.org.apache.xalan.internal.xsltc.dom.StepIterator;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;

public class ParallelSimRank {

	private SparseMatrix simrank;

	private Graph graph;

	private double DEFAULT_C = 0.6; 	

	private static final int NTHREDS = 1;
	
 	public ParallelSimRank ( Graph graph, double threshold, int maxIter, String writePath) { 		
		this.graph = graph;
		simrank = new SparseMatrix(graph.numNodes());
		SparseMatrix simrank2 = new SparseMatrix(graph.numNodes());
		for ( int step=0; step < maxIter && maxIter > 0; step++ ) 
		{
			System.out.println("Iteration : "+step);
			ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
			double maxDelta = -1.0 * Double.MAX_VALUE;
			for ( int i = 0 ; i < graph.numNodes() ; i++ ) { simrank.set(i,i,1.0); simrank2.set(i,i,1.0); }
			int iterate1Count = 0;
			ezgraph.NodeIterator it1 = graph.nodeIterator();
			while ( it1.hasNext() ) {
				int currentVertex1 = it1.nextInt();
				iterate1Count++;
				if(iterate1Count%1000 == 0)
					System.out.println("Iterate1Count : "+iterate1Count);
				int inDegreeCurrentVertex1 = graph.indegree(currentVertex1);
				if(inDegreeCurrentVertex1 == 0) continue;
				ezgraph.NodeIterator it2 = graph.nodeIterator();
				while ( it2.hasNext() ) {
					int currentVertex2 = it2.nextInt();
//					int inDegreeCurrentVertex2 = graph.indegree(currentVertex2);
//					if(inDegreeCurrentVertex2 == 0) continue;
					if ( currentVertex1 == currentVertex2 ) continue;
					if(currentVertex1 > currentVertex2) continue;
					Runnable worker = new UpdateSimRankTask(simrank, simrank2, graph, currentVertex1, currentVertex2, it1, it2);
				    executor.execute(worker);
				}
			}
			executor.shutdown();
		    // Wait until all threads are finish
		    while (!executor.isTerminated()) {
		    }
			simrank = simrank2.clone();
			simrank2 = new SparseMatrix(graph.numNodes());
			if(step % 5 == 0)
				writeScores(writePath+step);
			if ( maxDelta < threshold && threshold > 0 ) break;
		} 
 	}

 	
	public double getSimRankScore ( int node1, int node2 ) { return simrank.get(node1,node2); }

	public double getSimRankScore ( String node1, String node2 ) { 
		int id1 = graph.node(node1), id2 = graph.node(node2); 
		return simrank.get(id1,id2); 
	}

	public void writeScores(String file)
	{
		System.out.print("Writing SimRank values ...");
		try{			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file)));
			for ( int i=0; i<graph.numNodes(); i++) {
				for ( int j=0; j<graph.numNodes(); j++)
				{
					double score = getSimRankScore(i,j);
					if(score != 0)
						bw.write(i+" "+j+" "+score+"\n");
				}				
			}	
			bw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		System.out.println(" done");
	}
	
}
