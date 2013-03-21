package ezgraph;

import es.yrbcn.graph.weighted.*;
import it.unimi.dsi.webgraph.*;
import it.unimi.dsi.webgraph.labelling.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import jdbm.*;

public class ChineseWhispersClustering {

    private final int numIterations;
    
    private Graph graph;

    private int[] finalAssignments;

    private int numClusters;

    public ChineseWhispersClustering(Graph graph, int numIterations) {
        this.numIterations = numIterations;
	this.graph = graph;
        int[] assignments = new int[graph.numNodes()];
        for (int i = 0; i < assignments.length; ++i) assignments[i] = i;
        for (int i = 0; i < numIterations; ++i) {
            int[] newAssignments = new int[graph.numNodes()];
            for (int c = 0; c < newAssignments.length; ++c) newAssignments[c] = maxIndex(assignments[c]);
            assignments = newAssignments;
        }
        numClusters = 0;
        finalAssignments = new int[graph.numNodes()];
        int[] clusterMap = new int[graph.numNodes()];
        for (int i = 0; i < assignments.length; ++i) {
            int rawAssignment = assignments[i];
            if (clusterMap[rawAssignment] == 0) clusterMap[rawAssignment] = ++numClusters;
            finalAssignments[i] = clusterMap[rawAssignment] - 1;
        }
    }

    public ChineseWhispersClustering ( Graph graph ) { this(graph,100); }

    public int numberOfClusters() { return numClusters; }

    public int getCluster(int node) { return finalAssignments[node]; }

    public int getCluster(String node) { return finalAssignments[graph.node(node)]; }

    private int maxIndex(int node) {
        int bestIndex = 0;
        double bestValue = 0.0;
	Integer index = null;
	ArcLabelledNodeIterator.LabelledArcIterator it = graph.successors(node);
	while ( (index = it.nextInt()) != null && index >= 0 && ( index < graph.numNodes() ) ) try {
                double value = it.label().getFloat();
                if (value >= bestValue) {
                    bestValue = value;
                    bestIndex = index;
                }
        } catch ( Exception ex ) { throw new Error(ex); }
        return bestIndex;
    }

}
