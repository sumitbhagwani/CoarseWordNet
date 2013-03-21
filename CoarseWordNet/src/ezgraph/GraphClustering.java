package ezgraph;

import es.yrbcn.graph.weighted.*;
import it.unimi.dsi.webgraph.*;
import it.unimi.dsi.webgraph.labelling.*;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;
import it.unimi.dsi.fastutil.booleans.*;
import it.unimi.dsi.fastutil.doubles.*;
import it.unimi.dsi.*;
import it.unimi.dsi.bits.*;
import it.unimi.dsi.fastutil.io.*;
import it.unimi.dsi.util.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.atomic.*;
import jdbm.*;

// Change AtomicArray

public class GraphClustering {

	private AtomicIntegerArray results;

	private Graph graph;

	private int numClusters;

  	protected Int2IntMap clusters;

 	public GraphClustering ( Graph graph ) { this(graph,true,0.5,0.5,null,(long)1); }

 	public GraphClustering ( Graph graph, final boolean ratio, final double alpha, final double beta, final double[] score, final long seed ) {
		this.clusters = new Int2IntOpenHashMap(graph.numNodes());
		this.graph = graph;
		this.n = graph.numNodes();
		this.m = (int)graph.numArcs();
		this.seed = seed;
		this.label = new AtomicIntegerArray( this.m );
		this.updateList = Util.identity( n );
		simpleUncaughtExceptionHandler = new SimpleUncaughtExceptionHandler();
		this.numberOfThreads = Runtime.getRuntime().availableProcessors();
		this.nextNode = new AtomicInteger();
		this.modified = new AtomicInteger( 0 );
		this.offsets = new int[ n ];
		this.outdegree = new int[ n ];
		int incr = 0;
		for ( NodeIterator iterator = graph.nodeIterator(); iterator.hasNext(); ) {
			final int node = iterator.nextInt();
			final int deg = iterator.outdegree();
			offsets[ node ] = incr;
			outdegree[ node ] = deg;
			incr += iterator.outdegree();
		}
		this.indegree = new int[ n ];
		for ( it.unimi.dsi.webgraph.labelling.ArcLabelledNodeIterator iterator = graph.reverse.nodeIterator(); iterator.hasNext(); ) {
			final int node = iterator.nextInt();
			final int deg = iterator.outdegree();
			indegree[ node ] = deg;
		}
		this.beta = beta;
		this.r = new XorShiftStarRandom( seed );
		this.canChange = new boolean[ m ];
		this.score = score != null ? score : new double[ m ];
		if ( score == null ) DoubleArrays.fill( this.score, 1 );
		this.alpha = alpha;
		this.ratio = ratio;
		results = computeClustering();
		numClusters = 0;
		int pos = 0;
		ezgraph.NodeIterator it = graph.nodeIterator();		
		while ( it.hasNext() ) {
			int node = it.nextInt();
			Integer node2 = null;
			it.unimi.dsi.webgraph.LazyIntIterator suc = it.successors();
			while ( (node2 = suc.nextInt()) != null && node2 >= 0 && ( node2 < graph.numNodes() ) ) {
				clusters.put(node,results.get(pos));
				clusters.put(node2.intValue(),results.get(pos));
				numClusters = Math.max(numClusters,results.get(pos));
				pos++;
			}
		}
	}

	public int numberOfClusters( ) { return numClusters; }

	public int getCluster( int node ) { return clusters.get(node); }

	public int getCluster( String node ) { return clusters.get(graph.node(node)); }

	private static final int MAX_UPDATES = 20;
	
	private static final double MODIFIED_THRESHOLD = 0.001;

	private final int n;

	private final int m;

	private AtomicIntegerArray label;

	private final double beta;

	private final double alpha;

	private final int[] updateList;

	private final int[] outdegree;

	private final int[] indegree;

	private final int[] offsets;

	private final long seed;

	private final XorShiftStarRandom r;

	private final int numberOfThreads;

	private final AtomicInteger nextNode;

	private final SimpleUncaughtExceptionHandler simpleUncaughtExceptionHandler;

	private volatile Throwable threadException;

	private final AtomicInteger modified;

	private int update;

	private final boolean[] canChange;

	private final double[] score;

	private final boolean ratio;

	private final static class OpenHashTableCounter {
		private int[] key;

		private double[] count;

		private int[] location;

		private int mask;

		private int n;

		public OpenHashTableCounter() {
			mask = -1;
			count = DoubleArrays.EMPTY_ARRAY;
			key = IntArrays.EMPTY_ARRAY;
			location = IntArrays.EMPTY_ARRAY;
		}

		public void incr( final int k, final double val ) {
			int pos = ( k * 2056437379 ) & mask;
			while ( count[ pos ] != 0 && key[ pos ] != k )
				pos = ( pos + 1 ) & mask;
			if ( count[ pos ] == 0 ) {
				key[ pos ] = k;
				location[ n++ ] = pos;
			}
			count[ pos ] += val;
		}

		private final static class Entry extends AbstractInt2DoubleMap.BasicEntry {
			public Entry() {
				super( 0, 0 );
			}

			public void setKey( final int key ) {
				this.key = key;
			}

			public double setValue( final double value ) {
				this.value = value;
				return -1;
			}
		}

		public Iterator<Int2DoubleMap.Entry> entries() {
			return new AbstractObjectIterator<Int2DoubleMap.Entry>() {
				private int i;

				private Entry entry = new Entry();

				@Override
				public boolean hasNext() {
					return i < n;
				}

				@Override
				public it.unimi.dsi.fastutil.ints.Int2DoubleMap.Entry next() {
					if ( !hasNext() )
						throw new NoSuchElementException();
					final int l = location[ i++ ];
					entry.setKey( key[ l ] );
					entry.setValue( count[ l ] );
					return entry;
				}
			};
		}

		public void clear( final int size ) {
			if ( mask + 1 < ( 1 << ( Fast.ceilLog2( size ) + 1 ) ) ) {
				mask = ( 1 << ( Fast.ceilLog2( size ) + 1 ) ) - 1;
				count = new double[ mask + 1 ];
				key = new int[ mask + 1 ];
				location = new int[ mask + 1 ];
			}
			else
				while ( n-- != 0 )
					count[ location[ n ] ] = 0;
			n = 0;
		}
	}

	private final class SimpleUncaughtExceptionHandler implements UncaughtExceptionHandler {
		@Override
		public void uncaughtException( Thread t, Throwable e ) {
			threadException = e;
		}
	}

	protected final class IterationThreadRatio extends Thread {

		private final ezgraph.Graph graph;

		private IterationThreadRatio( final ezgraph.Graph graph ) { this.graph = graph; }

		public void run() {
			final XorShiftStarRandom r = new XorShiftStarRandom( GraphClustering.this.seed );
			final AtomicIntegerArray label = GraphClustering.this.label;
			final Graph graph = this.graph;
			final int n = GraphClustering.this.n;
			final int[] updateList = GraphClustering.this.updateList;
			final int[] outdegree = GraphClustering.this.outdegree;
			final int[] indegree = GraphClustering.this.indegree;
			final int granularity = Math.max( 1024, n >>> 10 );
			final double beta = GraphClustering.this.beta;
			final int[] offsets = GraphClustering.this.offsets;
			final double[] score = GraphClustering.this.score;
			final int m = GraphClustering.this.m;
			final double alpha = GraphClustering.this.alpha;
			final boolean[] canChange = GraphClustering.this.canChange;
			int start, end;
			OpenHashTableCounter map = new OpenHashTableCounter();
			for ( ;; ) {
				final int next = nextNode.getAndAdd( granularity );
				if ( next >= n ) {
					nextNode.getAndAdd( -granularity ); // Put the candle back
					return;
				}
				start = next;
				end = (int)( Math.min( n, (long)start + granularity ) );

				int sum = 0;
				for ( int k = start; k < end; k++ ) {
					final int x = updateList[ k ];
					final int degX = outdegree[ x ];
					final int innDegX = indegree[ x ];

					int[] succX = null;
					int[] innX = null;
					int[] innLabel = null;
					int[] innArcsID = null;
					boolean[][] innTriangle = null;

					sum += degX;
					for ( int i = 0; i < degX; i++ ) {
						final int currentArc = offsets[ x ] + i;
						if ( canChange[ currentArc ] ) {
							if ( succX == null ) {
								succX = graph.successorArray( x );
								innX = graph.ancestorArray( x );
								innLabel = new int[ innDegX ];
								innArcsID = new int[ innDegX ];
								innTriangle = new boolean[ innDegX ][ degX ];

								for ( int j = 0; j < innDegX; j++ ) {
									final int z = innX[ j ];
									final int degZ = outdegree[ z ];
									final int[] succZ = graph.successorArray( z );

									for ( int a = 0; a < degZ; a++ ) {
										if ( succZ[ a ] == x ) {
											innLabel[ j ] = label.get( offsets[ z ] + a );
											innArcsID[ j ] = offsets[ z ] + a;
										}
										final int idx = IntArrays.binarySearch( succX, 0, degX, succZ[ a ] );
										if ( idx >= 0 )
											innTriangle[ j ][ idx ] = true;
									}
								}
							}

							final int y = succX[ i ];
							final int degY = outdegree[ y ];
							final int[] succY = graph.successorArray( y );
							

							final int currentLabel = label.get( currentArc );
							map.clear( degY + innDegX );
							int a, b;
							// /// inserting in maps
							for ( a = 0, b = 0; a < degX && b < degY; ) {
								if ( succX[ a ] < succY[ b ] )
									a++;
								else if ( succY[ b ] < succX[ a ] ) {
									map.incr( label.get( offsets[ y ] + b ), score[ currentArc ] * ( ( alpha * beta ) + ( 1 - alpha ) / m )  );
									b++;
								}
								else {
									map.incr( label.get( offsets[ y ] + b ), score[ currentArc ] * ( ( alpha * 1 ) + ( 1 - alpha ) / m ) );
									a++;
									b++;
								}
							}
							for ( ; b < degY; b++ )
								map.incr( label.get( offsets[ y ] + b ), score[ currentArc ] * ( ( alpha * beta ) + ( 1 - alpha ) / m ) );


							// /// inserting in maps
							for ( a = 0; a < innDegX; a++ ) {
								final int l = innLabel[ a ];
								if ( innTriangle[ a ][ i ] )
									map.incr( l, 1 );
								else
									map.incr( l, beta );
							}


							double max = Double.NEGATIVE_INFINITY;
							IntArrayList majorities = new IntArrayList();

							for ( Iterator<Int2DoubleMap.Entry> entries = map.entries(); entries.hasNext(); ) {
								Int2DoubleMap.Entry entry = entries.next();
								final int l = entry.getIntKey();
								final double val = entry.getDoubleValue();

								if ( max == val )
									majorities.add( l );

								if ( max < val ) {
									majorities.clear();
									max = val;
									majorities.add( l );
								}
							}


							if ( majorities.isEmpty() )
								majorities.add( currentLabel );

							if ( majorities.size() < 2 )
								canChange[ currentArc ] = false;

							final int nextLabel = majorities.getInt( r.nextInt( majorities.size() ) );
							if ( nextLabel != currentLabel ) {
								modified.addAndGet( 1 );
								label.set( currentArc, nextLabel );
								for ( a = 0; a < innDegX; a++ )
									canChange[ innArcsID[ a ] ] = true;
								for ( b = 0; b < degY; b++ )
									canChange[ offsets[ y ] + b ] = true;
							}
						}
					}
				}
			}
		}
	}

	protected final class IterationThreadMass extends Thread {

		private final Graph graph;

		private IterationThreadMass( final Graph graph ) { this.graph = graph; }

		public void run() {
			final XorShiftStarRandom r = new XorShiftStarRandom( GraphClustering.this.seed );
			final AtomicIntegerArray label = GraphClustering.this.label;
			final Graph graph = this.graph;
			final int n = GraphClustering.this.n;
			final int[] updateList = GraphClustering.this.updateList;
			final int[] outdegree = GraphClustering.this.outdegree;
			final int[] indegree = GraphClustering.this.indegree;
			final int granularity = Math.max( 1024, n >>> 10 );
			final double beta = GraphClustering.this.beta;
			final double[] score = GraphClustering.this.score;
			final int[] offsets = GraphClustering.this.offsets;
			final int m = GraphClustering.this.m;
			final double alpha = GraphClustering.this.alpha;
			final boolean[] canChange = GraphClustering.this.canChange;
			int start, end;
			OpenHashTableCounter map = new OpenHashTableCounter();

			for ( ;; ) {
				final int next = nextNode.getAndAdd( granularity );
				if ( next >= n ) {
					nextNode.getAndAdd( -granularity ); // Put the candle back
					return;
				}
				start = next;
				end = (int)( Math.min( n, (long)start + granularity ) );

				int sum = 0;
				for ( int k = start; k < end; k++ ) {
					final int x = updateList[ k ];
					final int degX = outdegree[ x ];
					final int innDegX = indegree[ x ];

					int[] succX = null;
					int[] innX = null;
					int[] innLabel = null;
					int[] innArcsID = null;
					int[] innTr = null;
					int[] innNonTr = null;
					boolean[][] innTriangle = null;

					sum += degX;
					for ( int i = 0; i < degX; i++ ) {
						final int currentArc = offsets[ x ] + i;
						if ( canChange[ currentArc ] ) {
							if ( succX == null ) {
								succX = graph.successorArray( x );
								innX = graph.ancestorArray( x );
								innLabel = new int[ innDegX ];
								innArcsID = new int[ innDegX ];
								innTr = new int[ innDegX ];
								innNonTr = new int[ innDegX ];
								innTriangle = new boolean[ innDegX ][ degX ];

								for ( int j = 0; j < innDegX; j++ ) {
									final int z = innX[ j ];
									final int degZ = outdegree[ z ];
									final int[] succZ = graph.successorArray( z );

									int a, b;
									// /// calculating the masses
									for ( a = 0, b = 0; a < degZ && b < degX; ) {
										if ( succZ[ a ] == x ) {
											innLabel[ j ] = label.get( offsets[ z ] + a );
											innArcsID[ j ] = offsets[ z ] + a;
										}
										final int idx = IntArrays.binarySearch( succX, 0, degX, succZ[ a ] );
										if ( idx >= 0 )
											innTriangle[ j ][ idx ] = true;

										if ( succZ[ a ] < succX[ b ] )
											a++;
										else if ( succX[ b ] < succZ[ a ] ) {
											innNonTr[ j ]++;
											b++;
										}
										else {
											innTr[ j ]++;
											a++;
											b++;
										}
									}

									for ( ; a < degZ; a++ ) {
										if ( succZ[ a ] == x ) {
											innLabel[ j ] = label.get( offsets[ z ] + a );
											innArcsID[ j ] = offsets[ z ] + a;
										}
										final int idx = IntArrays.binarySearch( succX, 0, degX, succZ[ a ] );
										if ( idx >= 0 )
											innTriangle[ j ][ idx ] = true;
									}

									for ( ; b < degX; b++ )
										innNonTr[ j ]++;
								}
							}

							final int y = succX[ i ];
							final int degY = outdegree[ y ];
							final int[] succY = graph.successorArray( y );

							double tr = 0;
							double nonTr = 0;

							final int currentLabel = label.get( currentArc );
							map.clear( degY + innDegX );
							int a, b;
							// //// calculating the masses
							for ( a = 0, b = 0; a < degX && b < degY; ) {
								if ( succX[ a ] < succY[ b ] )
									a++;
								else if ( succY[ b ] < succX[ a ] ) {
									nonTr++;
									b++;
								}
								else {
									tr++;
									a++;
									b++;
								}
							}
							for ( ; b < degY; b++ )
								nonTr++;


							double tmpBeta = beta;
							if ( tr < 1 ) tmpBeta = 1;
							if ( nonTr < 1 ) tmpBeta = 0;
							 	
							// //// inserting in maps
							for ( a = 0, b = 0; a < degX && b < degY; ) {
								if ( succX[ a ] < succY[ b ] )
									a++;
								else if ( succY[ b ] < succX[ a ] ) {
									map.incr( label.get( offsets[ y ] + b ), score[ currentArc ] * ( ( alpha * tmpBeta / nonTr ) + ( 1 - alpha ) / m ) );
									b++;
								}
								else {
									map.incr( label.get( offsets[ y ] + b ), score[ currentArc ] * ( ( alpha * ( 1.0 - tmpBeta ) / tr ) + ( 1 - alpha ) / m ) );
									a++;
									b++;
								}
							}
							for ( ; b < degY; b++ )
								map.incr( label.get( offsets[ y ] + b ), score[ currentArc ] * ( ( alpha * tmpBeta / nonTr ) + ( 1 - alpha ) / m ) );


							// //// inserting in maps
							for ( a = 0; a < innDegX; a++ ) {
								final int l = innLabel[ a ];
								tmpBeta = beta;
								if ( innTr[ a ] < 1 ) tmpBeta = 1;
								if ( innNonTr[ a ] < 1 ) tmpBeta = 0;
									
								if ( innTriangle[ a ][ i ] )
									map.incr( l, score[ innArcsID[ a ] ] * ( ( alpha * ( 1.0 - tmpBeta ) / innTr[ a ] ) + ( 1 - alpha ) / m ) );
								else
									map.incr( l, score[ innArcsID[ a ] ] * ( ( alpha * tmpBeta / innNonTr[ a ] ) + ( 1 - alpha ) / m ) );
							}


							double max = Double.NEGATIVE_INFINITY;
							IntArrayList majorities = new IntArrayList();

							for ( Iterator<Int2DoubleMap.Entry> entries = map.entries(); entries.hasNext(); ) {
								Int2DoubleMap.Entry entry = entries.next();
								final int l = entry.getIntKey();
								final double val = entry.getDoubleValue();

								if ( max == val )
									majorities.add( l );

								if ( max < val ) {
									majorities.clear();
									max = val;
									majorities.add( l );
								}
							}


							if ( majorities.isEmpty() )
								majorities.add( currentLabel );

							if ( majorities.size() < 2 )
								canChange[ currentArc ] = false;

							final int nextLabel = majorities.getInt( r.nextInt( majorities.size() ) );
							if ( nextLabel != currentLabel ) {
								modified.addAndGet( 1 );
								label.set( currentArc, nextLabel );
								for ( a = 0; a < innDegX; a++ )
									canChange[ innArcsID[ a ] ] = true;
								for ( b = 0; b < degY; b++ )
									canChange[ offsets[ y ] + b ] = true;
							}
						}
					}
				}
			}
		}
	}

	private void update() {
		final int m = this.m;
		final int[] updateList = this.updateList;
		modified.set( 0 );
		IntArrays.shuffle( updateList, r );
		final Thread[] thread = new Thread[ numberOfThreads ];
		nextNode.set( 0 );
		for ( int i = 0; i < numberOfThreads; i++ ) {
			thread[ i ] = ratio ? new IterationThreadRatio( graph.copy() ) : new IterationThreadMass( graph.copy() );
			thread[ i ].setUncaughtExceptionHandler( simpleUncaughtExceptionHandler );
			thread[ i ].start();
		}
		for ( int i = 0; i < numberOfThreads; i++ ) {
			try {
				thread[ i ].join();
			}
			catch ( InterruptedException e ) {
				throw new RuntimeException( e );
			}
		}
		if ( threadException != null ) throw new RuntimeException( threadException );
	}

	public AtomicIntegerArray computeClustering() {
		final int m = this.m;
		Util.identity( updateList );
		for ( int i = 0; i < m; i++ ) label.set( i, i );
		BooleanArrays.fill( canChange, true );
		update = 0;
		do {
			update();
			update++;
		} while ( modified.get() > MODIFIED_THRESHOLD * m  && update < MAX_UPDATES );
		return label;
	}

}
