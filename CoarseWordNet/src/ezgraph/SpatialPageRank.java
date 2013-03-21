package ezgraph;

import es.yrbcn.graph.weighted.*;
import it.unimi.dsi.fastutil.doubles.*;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.io.*;
import it.unimi.dsi.util.*;
import it.unimi.dsi.webgraph.labelling.*;
import it.unimi.dsi.law.Util;
import java.io.*;
import java.util.*;

public class SpatialPageRank extends WeightedPageRankPowerMethod {

	private Int2DoubleMap scores;

	private SpatialGraph graph;

	private Float radius;

	private float latMin, latMax, lonMin, lonMax;

 	public SpatialPageRank ( SpatialGraph graph, float latMin, float latMax, float lonMin, float lonMax ) { this(graph, latMin, latMax, lonMin, lonMax, 0.0000000001, 1000); }

 	public SpatialPageRank ( SpatialGraph graph, float lat, float lon, float radius ) { this(graph, lat, lon, radius, 0.0000000001, 1000); }

 	public SpatialPageRank ( SpatialGraph graph, float lat, float lon, float radius, double threshold, int maxIter ) {
		this(graph, lat, lat, lon, lon, 0.0000000001, 1000);
		this.radius = radius;
	}

 	public SpatialPageRank ( SpatialGraph graph, float latMin, float latMax, float lonMin, float lonMax, double threshold, int maxIter ) {
		super(graph);
		this.graph = graph;
		this.radius = null;
		this.latMin = latMin;
		this.latMax = latMax;
		this.lonMin = lonMin;
		this.lonMax = lonMax;
		scores = new Int2DoubleOpenHashMap(graph.numNodes());
		try {
			if ( threshold > 0 && maxIter > 0 ) stepUntil(SpatialPageRank.or( new SpatialPageRank.NormDeltaStoppingCriterion(threshold), new SpatialPageRank.IterationNumberStoppingCriterion(maxIter)));
			else if ( threshold > 0 && maxIter < 0 ) stepUntil(new SpatialPageRank.NormDeltaStoppingCriterion(threshold));
			else if ( threshold < 0 && maxIter > 0 ) stepUntil(new SpatialPageRank.IterationNumberStoppingCriterion(maxIter));
			else stepUntil(SpatialPageRank.or( new SpatialPageRank.NormDeltaStoppingCriterion(0.0000000001), new SpatialPageRank.IterationNumberStoppingCriterion(1000)));
			int pos = 0;
			for ( double r : rank ) scores.put(pos++, r);
			clear();
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
			
	@SuppressWarnings("unused")
	public void init() throws IOException {
		super.init();
		iterationNumber = 0;
		if ( rank == null ) rank = new double[ numNodes ];
		if ( start != null ) {
			if ( ! isStochastic( start ) ) throw new IllegalArgumentException( "The start vector is not a stochastic vector." );
			start.toDoubleArray( rank );
		} else {
			if ( preference != null ) preference.toDoubleArray( rank );
			else DoubleArrays.fill( rank, 1.0 / numNodes );
		}
		if ( preference != null ) {
			if ( preference.size() != numNodes ) throw new IllegalArgumentException( "The preference vector size (" + preference.size() + ") is different from graph dimension (" + numNodes + ")." );
			if ( !isStochastic( preference) ) throw new IllegalArgumentException( "The preference vector is not a stochastic vector. " );
		}
		if ( stronglyPreferential ) {
			if ( preference == null ) throw new IllegalArgumentException( "The strongly flag is true but the preference vector is null." );
			preferentialAdjustment = preference;
		} else preferentialAdjustment = null;
		if ( sumoutweight == null ) {
			sumoutweight = new float[ numNodes ];
			Arrays.fill( sumoutweight, (float)0 );
			SpatialNodeIterator nodeIterator = graph.nodeIterator();
			int curr;
			int d;
			int suc[];
			Label lab[];
			float weight;
			while (nodeIterator.hasNext()) {
				curr = nodeIterator.nextInt();
				d = radius == null ? nodeIterator.spatialOutdegree(latMin,latMax,lonMin,lonMax) : nodeIterator.spatialOutdegree(latMin,lonMin,radius);
				suc = radius == null ? nodeIterator.successorArray(latMin,latMax,lonMin,lonMax) : nodeIterator.successorArray(latMin,lonMin,radius);
				lab = radius == null ? nodeIterator.labelArray(latMin,latMax,lonMin,lonMax) : nodeIterator.labelArray(latMin,lonMin,radius);
				for (int j = 0; j < d; j++) sumoutweight[curr] += lab[j].getFloat();
			}			
		}
		if ( start != null && ( coeffBasename != null || order.length > 0 ) ) throw new IllegalArgumentException( "You cannot choose a preference vector when computing coefficients or derivatives" );
		if ( previousRank == null ) previousRank = new double[ numNodes ];
		derivative = new double[ order.length ][ subset != null ? subset.length : graph.numNodes() ];
		if ( IntArrayList.wrap( order ).indexOf( 0 ) != -1 ) throw new IllegalArgumentException( "You cannot compute the derivative of order 0 (use PageRank instead)" );
		if ( coeffBasename != null ) BinIO.storeDoubles( rank, coeffBasename + "-" + 0 );
	}

	public void step() throws IOException {
		double[] oldRank = rank, newRank = previousRank;
		DoubleArrays.fill( newRank, 0.0 );
		double accum = 0.0;
		final SpatialNodeIterator nodeIterator = graph.nodeIterator();
		int i, outdegree, j, n = numNodes;
		int[] succ;
		Label[] lab;
		while( n-- != 0 ) {
			i = nodeIterator.nextInt();
			outdegree = radius == null ? nodeIterator.spatialOutdegree(latMin,latMax,lonMin,lonMax) : nodeIterator.spatialOutdegree(latMin,lonMin,radius);			
			if ( outdegree == 0 || buckets != null && buckets.get( i ) ) accum += oldRank[ i ]; else {
				j = outdegree;
				succ = radius == null ? nodeIterator.successorArray(latMin,latMax,lonMin,lonMax) : nodeIterator.successorArray(latMin,lonMin,radius);
				lab = radius == null ? nodeIterator.labelArray(latMin,latMax,lonMin,lonMax) : nodeIterator.labelArray(latMin,lonMin,radius);
				while ( j-- != 0 ) newRank[ succ[ j ] ] += ( oldRank[ i ] * lab[j].getFloat() ) / sumoutweight[i];
			}
		}
		final double accumOverNumNodes = accum / numNodes;		
		final double oneOverNumNodes = 1.0 / numNodes;
		if ( preference != null )
			if ( preferentialAdjustment == null )
				for( i = numNodes; i-- != 0; ) newRank[ i ] = alpha * newRank[ i ] + ( 1 - alpha ) * preference.getDouble( i ) + alpha * accumOverNumNodes;
			else
				for( i = numNodes; i-- != 0; ) newRank[ i ] = alpha * newRank[ i ] + ( 1 - alpha ) * preference.getDouble( i ) + alpha * accum * preferentialAdjustment.getDouble( i );
		else
			if ( preferentialAdjustment == null )
				for( i = numNodes; i-- != 0; ) newRank[ i ] = alpha * newRank[ i ] + ( 1 - alpha ) * oneOverNumNodes + alpha * accumOverNumNodes;
			else
				for( i = numNodes; i-- != 0; ) newRank[ i ] = alpha * newRank[ i ] + ( 1 - alpha ) * oneOverNumNodes + alpha * accum * preferentialAdjustment.getDouble( i );
		rank = newRank;
		previousRank = oldRank;
		n = iterationNumber;
		if ( subset == null ) {
			for( i = 0; i < order.length; i++ ) {
				final int k = order[ i ];
				final double alphak = Math.pow( alpha, k );
				final double nFallingK = Util.falling( n, k );
				for( j = 0; j < numNodes; j++ ) derivative[ i ][ j ] += nFallingK * ( rank[ j ] - previousRank[ j ] ) / alphak;
			}
		} else {
			for( i = 0; i < order.length; i++ ) {
				final int k = order[ i ];
				final double alphak = Math.pow( alpha, k );
				final double nFallingK = Util.falling( n, k );
				for( int t: subset ) derivative[ i ][ t ] += nFallingK * ( rank[ t ] - previousRank[ t ] ) / alphak;
			}
		}
		if ( coeffBasename != null ) { 
			final DataOutputStream coefficients = new DataOutputStream( new FastBufferedOutputStream( new FileOutputStream( coeffBasename + "-" + ( iterationNumber ) ) ) );
			final double alphaN = Math.pow( alpha, n );
			for( i = 0; i < numNodes; i++ ) coefficients.writeDouble( ( rank[ i ] - previousRank[ i ] ) / alphaN );
			coefficients.close();			
		}
	}
	
	public void stepUntil( final StoppingCriterion stoppingCriterion ) throws IOException {
		init();
		do step(); while ( !stoppingCriterion.shouldStop( this ) );
		for( int i = 0; i < order.length; i++ ) {
			if ( !( iterationNumber < order[ i ] / ( 1 - alpha ) ) ) {
				final int k = order[ i ];
				final double delta = alpha * iterationNumber / ( iterationNumber + k );
				final double alphak = Math.pow( alpha, k );
				final double nFallingK = Util.falling( iterationNumber, k );
				double infinityNorm = 0;
				for( int j = 0; j < numNodes; j++ ) infinityNorm = Math.max( infinityNorm, nFallingK * ( rank[ j ] - previousRank[ j ] ) / alphak );
			}
		}
	}
	
}
