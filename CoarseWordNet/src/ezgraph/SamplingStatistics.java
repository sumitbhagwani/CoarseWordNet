package ezgraph;

import es.yrbcn.graph.weighted.*;
import it.unimi.dsi.webgraph.*;
import it.unimi.dsi.webgraph.labelling.*;
import es.yrbcn.graph.triangles.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import it.unimi.dsi.webgraph.algo.*;

public class SamplingStatistics {

  private SamplingTrianglesAlgorithm triangles;

  private double[] approximateNeighbourhoodFunction;

  private Graph graph;

  public SamplingStatistics ( Graph graph, int size ) { 
	org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("es.yrbcn.graph.triangles.SamplingTrianglesAlgorithm");
	logger.setLevel(org.apache.log4j.Level.FATAL);
	logger = org.apache.log4j.Logger.getLogger("it.unimi.dsi.webgraph.algo.HyperApproximateNeighbourhoodFunction");
	logger.setLevel(org.apache.log4j.Level.FATAL);
	triangles = new SamplingTrianglesAlgorithm(graph.graph,1,(short)1);
	triangles.setSampleSize(size);	
	triangles.setRandomSampling();
	triangles.init();
	while ( !triangles.done() ) triangles.step();
	triangles.countTriangles();
	try {
		HyperApproximateNeighbourhoodFunction neighbourhoodFunction = new HyperApproximateNeighbourhoodFunction(graph, 16);
		neighbourhoodFunction.init();
		approximateNeighbourhoodFunction = neighbourhoodFunction.approximateNeighbourhoodFunction();
		neighbourhoodFunction.close();
	} catch ( IOException ex ) { throw new Error(ex); }; 
  }

  public int getSampleSize() { return triangles.sampleSize; }

  public double clusteringCoefficient ( ) { 
	double num = 0.0;
	double num2 = 0.0;
	for ( int i = 0 ; i < getSampleSize(); i++ ) {
		double aux =  triangles.sampledDegree[i] * ( triangles.sampledDegree[i] - 1.0 );
		if ( aux != 0.0 ) {	
			num += ( 2.0 * triangles.sampledTriangles[i] ) / aux;
			num2++;
		}
	}
	return num / num2; 
  }

  public double avgNumTriangles ( ) { 
	double num = 0.0;
	for ( double value : triangles.sampledTriangles ) num += value;
	return num / (double)getSampleSize(); 
  }

  public double avgNumNeighbors ( ) { 
	double num = 0.0;
	for ( double value : triangles.sampledNeighbors ) num += value;
	return num / (double)getSampleSize(); 
  }

  public double avgDistance ( ) { return NeighbourhoodFunction.averageDistance(approximateNeighbourhoodFunction); }

  public double[] distanceCumulativeDistributionFunction( ) { return NeighbourhoodFunction.distanceCumulativeDistributionFunction(approximateNeighbourhoodFunction); }

  public double[] distanceProbabilityMassFunction( ) { return NeighbourhoodFunction.distanceCumulativeDistributionFunction(approximateNeighbourhoodFunction); }

  public double effectiveDiameter( double alpha ) { return NeighbourhoodFunction.effectiveDiameter(alpha,approximateNeighbourhoodFunction); }

  public double harmonicDiameter( ) { return NeighbourhoodFunction.harmonicDiameter(graph.numNodes(),approximateNeighbourhoodFunction); }
 
  public double medianDistance( ) { return NeighbourhoodFunction.medianDistance(graph.numNodes(),approximateNeighbourhoodFunction); }

  public double spid( ) { return NeighbourhoodFunction.spid(approximateNeighbourhoodFunction); }

}
