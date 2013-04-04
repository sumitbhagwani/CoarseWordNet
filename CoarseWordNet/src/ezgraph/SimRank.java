package ezgraph;

import es.yrbcn.graph.weighted.*;
import it.unimi.dsi.webgraph.*;
import it.unimi.dsi.webgraph.labelling.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

import com.sun.org.apache.xalan.internal.xsltc.dom.StepIterator;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;

public class SimRank {

	private SparseMatrix simrank;

	private Graph graph;

	private double DEFAULT_C = 0.6;
	
	private int stepSizeForSimRankWriting = 1;
	
	private static double maxDeltaThreshold = 0.0000000001;

 	public SimRank ( Graph graph ) { this(graph, maxDeltaThreshold , 5, ""); }
 	
 	public SimRank ( Graph graph , String writePath) { this(graph, maxDeltaThreshold, 5, writePath); }
 	
 	public SimRank ( Graph graph, double threshold, int maxIter, String writePath) {
		this.graph = graph;
		simrank = new SparseMatrix(graph.numNodes());
		SparseMatrix simrank2 = new SparseMatrix(graph.numNodes());
		for ( int step=0; step < maxIter && maxIter > 0; step++ ) 
		{
			System.out.println("Iteration : "+step);
			double maxDelta = -1.0 * Double.MAX_VALUE;
//			for ( int i = 0 ; i < graph.numNodes() ; i++ ) { simrank.set(i,i,1.0); simrank2.set(i,i,1.0); }
			int iterate1Count = 0;
			ezgraph.NodeIterator it1 = graph.nodeIterator();
			while ( it1.hasNext() ) {
				int currentVertex1 = it1.nextInt();
				iterate1Count++;
				if(iterate1Count%1000 == 0)
					System.out.println("Iterate1Count : "+iterate1Count);
//				int inDegreeCurrentVertex1 = graph.indegree(currentVertex1);
//				if(inDegreeCurrentVertex1 == 0) continue;
				ezgraph.NodeIterator it2 = graph.nodeIterator();
				while ( it2.hasNext() ) {
					int currentVertex2 = it2.nextInt();
//					int inDegreeCurrentVertex2 = graph.indegree(currentVertex2);
//					if(inDegreeCurrentVertex2 == 0) continue;
					if ( currentVertex1 == currentVertex2 ) continue;
					if(currentVertex1 > currentVertex2) continue;
					double quantity = 0.0;
					Integer aux1 = null , aux2 = null;
					ArcLabelledNodeIterator.LabelledArcIterator anc1 = it1.ancestors();
					double sum1 = 0.0;
					while ( (aux1 = anc1.nextInt()) != null && aux1 >= 0 && aux1 < ( graph.numNodes() ) ) sum1 += anc1.label().getFloat();
					anc1 = it1.ancestors();
					while ( (aux1 = anc1.nextInt()) != null && aux1 >= 0 && aux1 < ( graph.numNodes() ) ) {
						double weight1 = anc1.label().getFloat() / sum1;
						ArcLabelledNodeIterator.LabelledArcIterator anc2 = it2.ancestors();
						double sum2 = 0.0;
						while ( (aux2 = anc2.nextInt()) != null && aux2 >= 0 && aux2 < ( graph.numNodes() ) ) sum2 += anc2.label().getFloat();
						anc2 = it2.ancestors();
						while ( (aux2 = anc2.nextInt()) != null && aux2 >= 0 && aux2 < ( graph.numNodes() ) ) {
							double weight2 = anc2.label().getFloat() / sum2;
							double simrankAux12 = 0.0;
							if(aux1 == aux2)
								simrankAux12 = 1.0;
							else if(aux1 < aux2)
								simrankAux12 = simrank.get(aux1,aux2);
							else
								simrankAux12 = simrank.get(aux2,aux1);
							quantity += weight1 * weight2 * simrankAux12;
//							quantity += weight1 * weight2 * simrank.get(aux1, aux2);
						}
					}
					if ( quantity != 0.0 ) {
//						simrank2.set(currentVertex1,currentVertex2, quantity * ( DEFAULT_C / ( 1.0 * it1.indegree() * it2.indegree() )));
						simrank2.set(currentVertex1,currentVertex2, quantity * DEFAULT_C );
						maxDelta = Math.max(maxDelta, Math.abs( simrank2.get(currentVertex1,currentVertex2) - simrank.get(currentVertex1,currentVertex2) ) );
					}
				}
			}
//			simrank = simrank2.clone();
			simrank = simrank2;
			simrank2 = new SparseMatrix(graph.numNodes());
			System.gc();
			System.out.println("MaxDelta for iteration "+step+" was "+maxDelta);
			if((step % stepSizeForSimRankWriting == 0) && writePath.length()>0)
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
