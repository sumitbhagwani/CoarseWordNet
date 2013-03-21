package ezgraph;

import es.yrbcn.graph.weighted.*;
import it.unimi.dsi.webgraph.*;
import it.unimi.dsi.webgraph.labelling.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;

public class SpatialHITS {

	private Int2DoubleMap authorityScores;

	private Int2DoubleMap hubScores;

	private SpatialGraph graph;

 	public SpatialHITS ( SpatialGraph graph, float lat, float lon, float radius ) { this(graph, lat, lon, radius, 0.0000000001, 1000); }

 	public SpatialHITS ( SpatialGraph graph, float lat, float lon, float radius, double threshold, int maxIter ) {
		this.graph = graph;
	        authorityScores = new Int2DoubleOpenHashMap(graph.numNodes());;
        	hubScores = new Int2DoubleOpenHashMap(graph.numNodes());;
		Int2DoubleMap newAuthorityScores = new Int2DoubleOpenHashMap(graph.numNodes());
		Int2DoubleMap newHubScores = new Int2DoubleOpenHashMap(graph.numNodes());
		SpatialNodeIterator it = graph.nodeIterator();
		int currentVertex = -1;
		while ( it.hasNext() ) {
		    currentVertex = it.nextInt();
	            authorityScores.put(currentVertex, 1.0);
        	    hubScores.put(currentVertex, 1.0);
	            newAuthorityScores.put(currentVertex, 0.0);
        	    newHubScores.put(currentVertex, 0.0);
		}
		for ( int step=0; step < maxIter && maxIter > 0; step++ ) {
			it = graph.nodeIterator();
			while ( it.hasNext() ) {
				currentVertex = it.nextInt();
				ArcLabelledNodeIterator.LabelledArcIterator anc = it.ancestors(lat, lon, radius);
				ArcLabelledNodeIterator.LabelledArcIterator suc = it.successors(lat, lon, radius);
				Integer aux = null;
				newAuthorityScores.put(currentVertex, 0.0);
				newHubScores.put(currentVertex, 0.0);
				while ( (aux = anc.nextInt()) != null && aux >= 0 && ( aux < graph.numNodes() ) ) newAuthorityScores.put(currentVertex, newAuthorityScores.get(currentVertex) + ( hubScores.get(aux) * anc.label().getFloat() ) );
				while ( (aux = suc.nextInt()) != null && aux >= 0 && ( aux < graph.numNodes() ) ) newHubScores.put(currentVertex, newHubScores.get(currentVertex) + ( authorityScores.get(aux) * suc.label().getFloat() ) );
			}
			double hub_ssum = 0.0;
    			double auth_ssum = 0.0;
			for (Integer node : newHubScores.keySet()) {
    				double hub_val = newHubScores.get(node);
    				double auth_val = newAuthorityScores.get(node);
    				hub_ssum += (hub_val * hub_val);
    				auth_ssum += (auth_val * auth_val);
    			}
    			hub_ssum = Math.sqrt(hub_ssum);
    			auth_ssum = Math.sqrt(auth_ssum);
			double maxDelta = Double.MIN_VALUE;
			for ( Integer node : newHubScores.keySet() ) {
				maxDelta = Math.max(maxDelta, Math.abs( hubScores.get(node) - (newHubScores.get(node) / hub_ssum) ) );
				maxDelta = Math.max(maxDelta, Math.abs( authorityScores.get(node) - (newAuthorityScores.get(node) / auth_ssum) ) );
				hubScores.put(node, new Double(newHubScores.get(node) / hub_ssum ));
				authorityScores.put(node, new Double(newAuthorityScores.get(node) / auth_ssum ));
			}
			if ( maxDelta < threshold && threshold > 0 ) break;
		}
 	}

 	public SpatialHITS ( SpatialGraph graph, float latMin, float latMax, float lonMin, float lonMax ) { this(graph, latMin, latMax, lonMin, lonMax, 0.0000000001, 1000); }

 	public SpatialHITS ( SpatialGraph graph, float latMin, float latMax, float lonMin, float lonMax, double threshold, int maxIter ) {
		this.graph = graph;
	        authorityScores = new Int2DoubleOpenHashMap(graph.numNodes());;
        	hubScores = new Int2DoubleOpenHashMap(graph.numNodes());;
		Int2DoubleMap newAuthorityScores = new Int2DoubleOpenHashMap(graph.numNodes());
		Int2DoubleMap newHubScores = new Int2DoubleOpenHashMap(graph.numNodes());
		SpatialNodeIterator it = graph.nodeIterator();
		int currentVertex = -1;
		while ( it.hasNext() ) {
		    currentVertex = it.nextInt();
	            authorityScores.put(currentVertex, 1.0);
        	    hubScores.put(currentVertex, 1.0);
	            newAuthorityScores.put(currentVertex, 0.0);
        	    newHubScores.put(currentVertex, 0.0);
		}
		for ( int step=0; step < maxIter && maxIter > 0; step++ ) {
			it = graph.nodeIterator();
			while ( it.hasNext() ) {
				currentVertex = it.nextInt();
				ArcLabelledNodeIterator.LabelledArcIterator anc = it.ancestors(latMin, latMax, lonMin, lonMax);
				ArcLabelledNodeIterator.LabelledArcIterator suc = it.successors(latMin, latMax, lonMin, lonMax);
				Integer aux = null;
				newAuthorityScores.put(currentVertex, 0.0);
				newHubScores.put(currentVertex, 0.0);
				while ( (aux = anc.nextInt()) != null && aux >= 0 && ( aux < graph.numNodes() ) ) newAuthorityScores.put(currentVertex, newAuthorityScores.get(currentVertex) + ( hubScores.get(aux) * anc.label().getFloat() ) );
				while ( (aux = suc.nextInt()) != null && aux >= 0 && ( aux < graph.numNodes() ) ) newHubScores.put(currentVertex, newHubScores.get(currentVertex) + ( authorityScores.get(aux) * suc.label().getFloat() ) );
			}
			double hub_ssum = 0.0;
    			double auth_ssum = 0.0;
			for (Integer node : newHubScores.keySet()) {
    				double hub_val = newHubScores.get(node);
    				double auth_val = newAuthorityScores.get(node);
    				hub_ssum += (hub_val * hub_val);
    				auth_ssum += (auth_val * auth_val);
    			}
    			hub_ssum = Math.sqrt(hub_ssum);
    			auth_ssum = Math.sqrt(auth_ssum);
			double maxDelta = Double.MIN_VALUE;
			for ( Integer node : newHubScores.keySet() ) {
				maxDelta = Math.max(maxDelta, Math.abs( hubScores.get(node) - (newHubScores.get(node) / hub_ssum) ) );
				maxDelta = Math.max(maxDelta, Math.abs( authorityScores.get(node) - (newAuthorityScores.get(node) / auth_ssum) ) );
				hubScores.put(node, new Double(newHubScores.get(node) / hub_ssum ));
				authorityScores.put(node, new Double(newAuthorityScores.get(node) / auth_ssum ));
			}
			if ( maxDelta < threshold && threshold > 0 ) break;
		}
 	}

	public double getHubScore ( int node ) { return hubScores.get(node); }

	public double getHubScore ( String node ) { int id = graph.node(node); return hubScores.get(id); }

	public double getAuthorityScore ( int node ) { return authorityScores.get(node); }

	public double getAuthorityScore ( String node ) { int id = graph.node(node); return authorityScores.get(id); }

	public double[] getHubScores ( int node[] ) { double scores[] = new double[node.length]; for ( int i = 0; i < node.length; i++ ) scores[i] = hubScores.get(node[i]); return scores; }

	public double[] getHubScores ( String node[] ) { double scores[] = new double[node.length]; for ( int i = 0; i < node.length; i++ ) scores[i] = hubScores.get(graph.node(node[i])); return scores; }

	public double[] getAuthorityScores ( int node[] ) { double scores[] = new double[node.length]; for ( int i = 0; i < node.length; i++ ) scores[i] = authorityScores.get(node[i]); return scores; }

	public double[] getAuthorityScores ( String node[] ) { double scores[] = new double[node.length]; for ( int i = 0; i < node.length; i++ ) scores[i] = authorityScores.get(graph.node(node[i])); return scores; }

	public List<String> getSortedHubNodes (  ) { 
		List<String> list = new ArrayList<String>();
  		Iterator<Integer> iterator = hubScores.keySet().iterator();
  		while (iterator.hasNext()) list.add(graph.node(iterator.next()));
		java.util.Collections.sort(list, new Comparator<String>(){
            		public int compare(String entry, String entry1) {
		                int i1 = graph.node(entry);
				int i2 = graph.node(entry1);
				return hubScores.get(i1) == hubScores.get(i2) ? 0 : hubScores.get(i1) < hubScores.get(i2) ? 1 : -1;
            		}
        	});
		return list;
	}

	public List<String> getSortedAuthorityNodes (  ) { 
		List<String> list = new ArrayList<String>();
  		Iterator<Integer> iterator = authorityScores.keySet().iterator();
  		while (iterator.hasNext()) list.add(graph.node(iterator.next()));
		java.util.Collections.sort(list, new Comparator<String>(){
            		public int compare(String entry, String entry1) {
		                int i1 = graph.node(entry);
				int i2 = graph.node(entry1);
				return authorityScores.get(i1) == authorityScores.get(i2) ? 0 : authorityScores.get(i1) < authorityScores.get(i2) ? 1 : -1;
            		}
        	});
		return list;
	}

}
