package ezgraph;

import es.yrbcn.graph.weighted.*;
import it.unimi.dsi.webgraph.*;
import it.unimi.dsi.webgraph.labelling.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import java.lang.reflect.*;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;
import it.unimi.dsi.logging.ProgressLogger;
import jdbm.*;

public class SpatialGraph extends Graph {

  protected PrimaryHashMap<Integer,Float> latitude;

  protected PrimaryHashMap<Integer,Float> longitude;

  public SpatialGraph ( ) { super(); }

  public SpatialGraph ( String file, String file2 ) throws IOException {
	super(file);
	iterator = nodeIterator();
	BufferedReader br;
	try { 
		br = new BufferedReader(new InputStreamReader( new GZIPInputStream( new FileInputStream(file2) ))); 
	} catch ( Exception ex ) { br = new BufferedReader(new FileReader(file)); }
	String aux;
	while ( ( aux = br.readLine() ) != null ) try {
		if ( commit++ % COMMIT_SIZE == 0 ) { commit(); }
		String parts[] = aux.split("\t");
		String name = new String(parts[0]);
		Float lat = new Float(parts[1]);
		Float lon = new Float(parts[2]);
		setLatitude(name,lat);
		setLongitude(name,lon);
	} catch ( Exception ex ) { throw new Error(ex); }
	commit();
  }

  public static SpatialGraph merge ( SpatialGraph g1 , SpatialGraph g2 ) {
	int commit = 0;
	Graph g = Graph.merge(g1,g2);
	SpatialGraph sg = new SpatialGraph();
	sg.nodes = g.nodes;
	sg.nodesReverse = g.nodesReverse;
	sg.graph = g.graph;
  	sg.reverse = g.reverse;
	sg.numArcs = g.numArcs;
	sg.iterator = new SpatialNodeIterator(sg);
	for ( String n : sg.nodesReverse.keySet() ) {
		if ( commit++ % COMMIT_SIZE == 0 ) { sg.commit(); }
		sg.setLatitude( n , g1.latitude(n) );
		sg.setLongitude( n , g1.longitude(n) );
	}
	sg.commit();
	return sg;
  }

  public SpatialGraph copy() {
	Graph g = super.copy();
	SpatialGraph sg = new SpatialGraph();
	sg.nodes = g.nodes;
	sg.nodesReverse = g.nodesReverse;
	sg.graph = g.graph;
  	sg.reverse = g.reverse;
	sg.numArcs = g.numArcs;
	sg.iterator = new SpatialNodeIterator(sg);
	for ( String n : sg.nodesReverse.keySet() ) {
		if ( commit++ % COMMIT_SIZE == 0 ) { sg.commit(); }
		sg.setLatitude( n , latitude(n) );
		sg.setLongitude( n , longitude(n) );
	}
	sg.commit();
	return sg;
  }

  private SpatialNodeIterator advanceIterator ( int x ) {
	if ( x >= graph.numNodes() ) throw new Error("Problem with the id for the node.");
	if ( !iterator.hasNext() || iterator.nextInt() >= x ) iterator = nodeIterator();
	Integer aux = null;
	while ( (aux = iterator.nextInt()) != x ) {  }
	return (SpatialNodeIterator)(iterator);
  }

  public float latitude ( int nodeNum ) { return this.latitude.get(nodeNum); }

  public float latitude ( String node ) { return this.latitude.get(nodesReverse.get(node)); }

  public void setLatitude ( int nodeNum , float latitude ) { this.latitude.put(nodeNum,latitude); }

  public void setLatitude ( String node , float latitude ) { this.latitude.put(nodesReverse.get(node),latitude); }

  public float longitude ( int nodeNum ) { return this.longitude.get(nodeNum); }

  public float longitude ( String node ) { return this.longitude.get(nodesReverse.get(node)); }

  public void setLongitude ( int nodeNum , float longitude ) { this.longitude.put(nodeNum,longitude); }

  public void setLongitude ( String node , float longitude ) { this.longitude.put(nodesReverse.get(node),longitude); }

  public SpatialGraph neighbourhoodGraph ( int node , int hops ) { return neighbourhoodGraph ( new int[]{ node } , hops ); }

  public SpatialGraph neighbourhoodGraph ( String node , int hops ) { return neighbourhoodGraph ( new String[]{ node } , hops ); }

  public SpatialGraph neighbourhoodGraph ( String nodes[] , int hops ) {
	int nnodes[] = new int[nodes.length];
	for ( int i = 0; i < nodes.length; i++ ) nnodes[i] = nodesReverse.get(nodes[i]);
	return neighbourhoodGraph(nnodes, hops);
  }

  public SpatialGraph neighbourhoodGraph ( int nnodes[] , int hops ) {
	Graph g = super.neighbourhoodGraph(nnodes,hops);
	SpatialGraph sg = new SpatialGraph();
	sg.nodes = g.nodes;
	sg.nodesReverse = g.nodesReverse;
	sg.graph = g.graph;
  	sg.reverse = g.reverse;
	sg.numArcs = g.numArcs;
	sg.iterator = new SpatialNodeIterator(sg);
	for ( String n : sg.nodesReverse.keySet() ) {
		if ( commit++ % COMMIT_SIZE == 0 ) { sg.commit(); }
		sg.setLatitude( n , latitude(n) );
		sg.setLongitude( n , longitude(n) );
	}
	sg.commit();
	return sg;
  } 

  public SpatialGraph transpose ( ) {
	SpatialGraph g = copy();
	try {
		g.graph = Transform.transposeOffline(g.graph,1000);
		g.reverse = Transform.transposeOffline(g.reverse,1000);
	} catch ( IOException ex ) { throw new Error(ex); }
	return g;
  }

  public void commit () { 
	super.commit();
	try { latitude.getRecordManager().commit(); } catch ( IOException e ) { throw new Error(e); }
	try { longitude.getRecordManager().commit(); } catch ( IOException e ) { throw new Error(e); }
  };

  protected void finalize () throws Throwable {
	super.finalize();
	latitude.clear();
	latitude.getRecordManager().commit();
	latitude.getRecordManager().close();
	longitude.clear();
	longitude.getRecordManager().commit();
	longitude.getRecordManager().close();
  }

  public double spatialDistance ( int x , int y ) {
	double lat1 = latitude(x);
	double lat2 = latitude(y);
	double lng1 = longitude(x);
	double lng2 = longitude(y);
        double earthRadiusKM = 6370.99;
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadiusKM * c;
        return dist;
  }

  public boolean spatialContainment ( int n , float latMin, float latMax, float lonMin, float lonMax ) {
	if ( latitude(n) >= latMin && latitude(n) <= latMax && 
	     longitude(n) >= lonMin && longitude(n) <= lonMin ) return true;
	return false;
  }

  public boolean spatialContainment ( int n , float lat1, float lng1, float radius ) {
	double lat2 = latitude(n);
	double lng2 = longitude(n);
        double earthRadiusKM = 6370.99;
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadiusKM * c;
	return dist <= radius;
  }

  public SpatialNodeIterator nodeIterator() { return new ezgraph.SpatialNodeIterator(this);  }

  public SpatialNodeIterator nodeIterator(int from) { return new ezgraph.SpatialNodeIterator(this, from); }

  public double spatialCloseness( int x ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialCloseness(); }

  public double spatialOutCloseness( int x ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialOutCloseness(); }

  public double spatialInCloseness( int x ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialInCloseness(); }

  public int spatialDegree( int x , float latMin, float latMax, float lonMin, float lonMax ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialDegree(latMin, latMax, lonMin, lonMax); }

  public int spatialOutdegree( int x , float latMin, float latMax, float lonMin, float lonMax ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialOutdegree(latMin, latMax, lonMin, lonMax); }

  public int spatialIndegree( int x , float latMin, float latMax, float lonMin, float lonMax) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialIndegree(latMin, latMax, lonMin, lonMax); }

  public double spatialDegreeRatio( int x , float latMin, float latMax, float lonMin, float lonMax ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialDegreeRatio(latMin, latMax, lonMin, lonMax); }

  public double spatialOutdegreeRatio( int x , float latMin, float latMax, float lonMin, float lonMax ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialOutdegreeRatio(latMin, latMax, lonMin, lonMax); }

  public double spatialIndegreeRatio( int x , float latMin, float latMax, float lonMin, float lonMax) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialIndegreeRatio(latMin, latMax, lonMin, lonMax); }

  public double spatialStrength( int x , float latMin, float latMax, float lonMin, float lonMax) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialStrength(latMin, latMax, lonMin, lonMax); }

  public double spatialOutStrength( int x , float latMin, float latMax, float lonMin, float lonMax) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialOutStrength(latMin, latMax, lonMin, lonMax); }

  public double spatialInStrength( int x , float latMin, float latMax, float lonMin, float lonMax) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialInStrength(latMin, latMax, lonMin, lonMax); }

  public double spatialStrengthRatio( int x , float latMin, float latMax, float lonMin, float lonMax) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialStrengthRatio(latMin, latMax, lonMin, lonMax); }

  public double spatialOutStrengthRatio( int x , float latMin, float latMax, float lonMin, float lonMax) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialOutStrengthRatio(latMin, latMax, lonMin, lonMax); }

  public double spatialInStrengthRatio( int x , float latMin, float latMax, float lonMin, float lonMax) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialInStrengthRatio(latMin, latMax, lonMin, lonMax); }

  public int[] successorArray( int x , float latMin, float latMax, float lonMin, float lonMax) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.successorArray(latMin, latMax, lonMin, lonMax); }

  public int[] ancestorArray( int x , float latMin, float latMax, float lonMin, float lonMax) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.ancestorArray(latMin, latMax, lonMin, lonMax); }

  public Label[] labelArray( int x , float latMin, float latMax, float lonMin, float lonMax) { return successorLabelArray(x,latMin, latMax, lonMin, lonMax); }

  public Label[] successorLabelArray( int x , float latMin, float latMax, float lonMin, float lonMax) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.successorLabelArray(latMin, latMax, lonMin, lonMax); }

  public Label[] ancestorLabelArray( int x , float latMin, float latMax, float lonMin, float lonMax) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.ancestorLabelArray(latMin, latMax, lonMin, lonMax); }

  public ArcLabelledNodeIterator.LabelledArcIterator successors(int x , float latMin, float latMax, float lonMin, float lonMax ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.successors(latMin, latMax, lonMin, lonMax); }

  public ArcLabelledNodeIterator.LabelledArcIterator ancestors(int x, float latMin, float latMax, float lonMin, float lonMax ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.ancestors(latMin, latMax, lonMin, lonMax); }

  public int spatialDegree( int x , float lat, float lon, float radius ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialDegree(lat, lon, radius); }

  public int spatialOutdegree( int x , float lat, float lon, float radius ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialOutdegree(lat, lon, radius); }

  public int spatialIndegree( int x , float lat, float lon, float radius ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialIndegree(lat, lon, radius); }

  public double spatialDegreeRatio( int x , float lat, float lon, float radius ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialDegreeRatio(lat, lon, radius); }

  public double spatialOutdegreeRatio( int x , float lat, float lon, float radius ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialOutdegreeRatio(lat, lon, radius); }

  public double spatialIndegreeRatio( int x , float lat, float lon, float radius) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialIndegreeRatio(lat, lon, radius); }

  public double spatialStrength( int x , float lat, float lon, float radius ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialStrength(lat, lon, radius); }

  public double spatialOutStrength( int x , float lat, float lon, float radius ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialOutStrength(lat, lon, radius); }

  public double spatialInStrength( int x , float lat, float lon, float radius ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialInStrength(lat, lon, radius); }

  public double spatialStrengthRatio( int x , float lat, float lon, float radius ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialStrengthRatio(lat, lon, radius); }

  public double spatialOutStrengthRatio( int x , float lat, float lon, float radius ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialOutStrengthRatio(lat, lon, radius); }

  public double spatialInStrengthRatio( int x , float lat, float lon, float radius ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.spatialInStrengthRatio(lat, lon, radius); }

  public int[] successorArray( int x , float lat, float lon, float radius ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.successorArray(lat, lon, radius); }

  public int[] ancestorArray( int x , float lat, float lon, float radius ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.ancestorArray(lat, lon, radius); }

  public Label[] labelArray( int x , float lat, float lon, float radius ) { return successorLabelArray(x,lat, lon, radius); }

  public Label[] successorLabelArray( int x , float lat, float lon, float radius ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.successorLabelArray(lat, lon, radius); }

  public Label[] ancestorLabelArray( int x , float lat, float lon, float radius ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.ancestorLabelArray(lat, lon, radius); }

  public ArcLabelledNodeIterator.LabelledArcIterator successors(int x , float lat, float lon, float radius ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.successors(lat, lon, radius); }

  public ArcLabelledNodeIterator.LabelledArcIterator ancestors(int x, float lat, float lon, float radius ) { SpatialNodeIterator iterator = advanceIterator(x); return iterator.ancestors(lat, lon, radius); }


}
