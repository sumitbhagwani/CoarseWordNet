package ezgraph;

import es.yrbcn.graph.weighted.*;
import it.unimi.dsi.webgraph.*;
import it.unimi.dsi.webgraph.labelling.*;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

public class PageRank {

	private Int2DoubleMap scores;

	private Graph graph;

 	public PageRank ( Graph graph ) { this(graph, 0.0000000001, 1000); }

 	public PageRank ( Graph graph, double threshold, int maxIter ) {
		org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("es.yrbcn.graph.weighted.WeightedPageRankPowerMethod");
		logger.setLevel(org.apache.log4j.Level.FATAL);
		logger = org.apache.log4j.Logger.getLogger("es.yrbcn.graph.weighted.WeightedPageRank");
		logger.setLevel(org.apache.log4j.Level.FATAL);
		this.graph = graph;
		scores = new Int2DoubleOpenHashMap(graph.numNodes());
		try {
			WeightedPageRankPowerMethod pr = new WeightedPageRankPowerMethod(graph.graph);
			if ( threshold > 0 && maxIter > 0 ) pr.stepUntil(WeightedPageRank.or( new WeightedPageRank.NormDeltaStoppingCriterion(threshold), new WeightedPageRankPowerMethod.IterationNumberStoppingCriterion(maxIter)));
			else if ( threshold > 0 && maxIter < 0 ) pr.stepUntil(new WeightedPageRank.NormDeltaStoppingCriterion(threshold));
			else if ( threshold < 0 && maxIter > 0 ) pr.stepUntil(new WeightedPageRankPowerMethod.IterationNumberStoppingCriterion(maxIter));
			else pr.stepUntil(WeightedPageRank.or( new WeightedPageRank.NormDeltaStoppingCriterion(0.0000000001), new WeightedPageRankPowerMethod.IterationNumberStoppingCriterion(1000)));
			int pos = 0;
			for ( double rank : pr.rank ) scores.put(pos++, rank);
			pr.clear();
		} catch ( IOException ex ) { throw new Error(ex); }
	}


	public double getPageRankScore ( int node ) { return scores.get(node); }

	public double getPageRankScore ( String node ) { int id = graph.node(node); return scores.get(id); }

	public double[] getPageRankScores ( int node[] ) { double scores[] = new double[node.length]; for ( int i = 0; i < node.length; i++ ) scores[i] = this.scores.get(node[i]); return scores; }

	public double[] getPageRankScores ( String node[] ) { double scores[] = new double[node.length]; for ( int i = 0; i < node.length; i++ ) scores[i] = this.scores.get(graph.node(node[i])); return scores; }

	public List<String> getSortedNodes (  ) { 
		List<String> list = new ArrayList<String>();
  		Iterator<Integer> iterator = scores.keySet().iterator();
  		while (iterator.hasNext()) list.add(graph.node(iterator.next()));
		java.util.Collections.sort(list, new Comparator<String>(){
            		public int compare(String entry, String entry1) {
		                int i1 = graph.node(entry);
				int i2 = graph.node(entry1);
				return scores.get(i1) == scores.get(i2) ? 0 : scores.get(i1) < scores.get(i2) ? 1 : -1;
            		}
        	});
		return list;
	}

}
