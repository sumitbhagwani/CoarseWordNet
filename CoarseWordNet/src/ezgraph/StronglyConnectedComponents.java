package ezgraph;

import es.yrbcn.graph.weighted.*;
import it.unimi.dsi.webgraph.*;
import it.unimi.dsi.webgraph.labelling.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

public class StronglyConnectedComponents {

	private it.unimi.dsi.webgraph.algo.StronglyConnectedComponents components;

	private Graph graph;

	public StronglyConnectedComponents ( Graph graph ) { this(graph,true); }

 	public StronglyConnectedComponents ( Graph graph, boolean computeBuckets ) {
		this.graph = graph;
		components = it.unimi.dsi.webgraph.algo.StronglyConnectedComponents.compute(graph, computeBuckets, null);
	}

	public int numberOfComponents() { return components.numberOfComponents; }

	public int getComponent(int node) { return components.component[node]; }

	public int getComponent(String node) { return components.component[graph.node(node)]; }

	public int[] components() { return components.component; }

}
