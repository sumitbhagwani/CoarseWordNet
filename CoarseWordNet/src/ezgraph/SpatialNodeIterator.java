package ezgraph;

import es.yrbcn.graph.weighted.*;
import it.unimi.dsi.webgraph.*;
import it.unimi.dsi.webgraph.labelling.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

public class SpatialNodeIterator extends NodeIterator {

	SpatialGraph graph;

	public SpatialNodeIterator ( SpatialGraph graph ) {
		super(graph);
		this.graph = graph;
	}

	public SpatialNodeIterator ( SpatialGraph graph , int n ) {
		super(graph,n);
		this.graph = graph;
	}

	public float spatialStrength( float latMin, float latMax, float lonMin, float lonMax ) { 
		return spatialOutStrength(latMin,latMax,lonMin,lonMax) + spatialInStrength(latMin,latMax,lonMin,lonMax); 
	}

	public float spatialOutStrength( float latMin, float latMax, float lonMin, float lonMax ) {
		float count = 0;
		int[] n = successorArray();
		Label[] l = successorLabelArray();
		for ( int i = 0; i < n.length; i++ ) if( graph.spatialContainment(n[i],latMin,latMax,lonMin,lonMax) ) count += l[i].getFloat();
		return count; 
	}

	public float spatialInStrength( float latMin, float latMax, float lonMin, float lonMax ) { 
		float count = 0;
		int[] n = ancestorArray();
		Label[] l = ancestorLabelArray();
		for ( int i = 0; i < n.length; i++ ) if( graph.spatialContainment(n[i],latMin,latMax,lonMin,lonMax) ) count += l[i].getFloat();
		return count; 
	}

	public float spatialStrength( float lat, float lon, float radius ) { 
		return spatialOutStrength(lat,lat,radius) + spatialInStrength(lat,lat,radius); 
	}

	public float spatialOutStrength( float lat, float lon, float radius ) {
		float count = 0;
		int[] n = successorArray();
		Label[] l = successorLabelArray();
		for ( int i = 0; i < n.length; i++ ) if( graph.spatialContainment(n[i],lat,lon,radius) ) count += l[i].getFloat();
		return count; 
	}

	public float spatialInStrength( float lat, float lon, float radius ) { 
		float count = 0;
		int[] n = ancestorArray();
		Label[] l = ancestorLabelArray();
		for ( int i = 0; i < n.length; i++ ) if( graph.spatialContainment(n[i],lat,lon,radius) ) count += l[i].getFloat();
		return count; 
	}

	public double spatialStrengthRatio( float lat, float lon, float radius ) { 
		return (double)spatialStrength(lat,lon,radius) / (double)strength();
	}

	public double spatialOutStrengthRatio( float lat, float lon, float radius ) {
		return (double)spatialOutStrength(lat,lon,radius) / (double)outstrength();
	}

	public double spatialInStrengthRatio( float lat, float lon, float radius ) { 
		return (double)spatialInStrength(lat,lon,radius) / (double)instrength();
	}

	public double spatialStrengthRatio( float latMin, float latMax, float lonMin, float lonMax ) { 
		return (double)spatialStrength(latMin,latMax,lonMin,lonMax) / (double)strength();
	}

	public double spatialOutStrengthRatio( float latMin, float latMax, float lonMin, float lonMax ) {
		return (double)spatialOutStrength(latMin,latMax,lonMin,lonMax) / (double)outstrength();
	}

	public double spatialInStrengthRatio( float latMin, float latMax, float lonMin, float lonMax ) { 
		return (double)spatialInStrength(latMin,latMax,lonMin,lonMax) / (double)instrength();
	}

	public int spatialDegree( float lat, float lon, float radius ) { 
		return spatialOutdegree(lat,lon,radius) + spatialIndegree(lat,lon,radius); 
	}

	public int spatialOutdegree( float lat, float lon, float radius ) {
		int count = 0; 
		for ( int n : successorArray() ) if( graph.spatialContainment(n,lat,lon,radius) ) count++;
		return count; 
	}

	public int spatialIndegree( float lat, float lon, float radius ) { 
		int count = 0; 
		for ( int n : ancestorArray() ) if( graph.spatialContainment(n,lat,lon,radius) ) count++;
		return count;
	}

	public double spatialDegreeRatio( float lat, float lon, float radius ) { 
		return (double)spatialDegree(lat,lon,radius) / (double)degree();
	}

	public double spatialOutdegreeRatio( float lat, float lon, float radius ) {
		return (double)spatialOutdegree(lat,lon,radius) / (double)outdegree();
	}

	public double spatialIndegreeRatio( float lat, float lon, float radius ) { 
		return (double)spatialIndegree(lat,lon,radius) / (double)indegree();
	}

	public int spatialDegree( float latMin, float latMax, float lonMin, float lonMax ) { 
		return spatialOutdegree(latMin,latMax,lonMin,lonMax) + spatialIndegree(latMin,latMax,lonMin,lonMax); 
	}

	public int spatialOutdegree( float latMin, float latMax, float lonMin, float lonMax ) {
		int count = 0; 
		for ( int n : successorArray() ) if( graph.spatialContainment(n,latMin,latMax,lonMin,lonMax) ) count++;
		return count; 
	}

	public int spatialIndegree( float latMin, float latMax, float lonMin, float lonMax ) { 
		int count = 0; 
		for ( int n : ancestorArray() ) if( graph.spatialContainment(n,latMin,latMax,lonMin,lonMax) ) count++;
		return count;
	}

	public double spatialDegreeRatio( float latMin, float latMax, float lonMin, float lonMax ) { 
		return (double)spatialDegree(latMin,latMax,lonMin,lonMax) / (double)degree();
	}

	public double spatialOutdegreeRatio( float latMin, float latMax, float lonMin, float lonMax ) {
		return (double)spatialOutdegree(latMin,latMax,lonMin,lonMax) / (double)outdegree();
	}

	public double spatialIndegreeRatio( float latMin, float latMax, float lonMin, float lonMax ) { 
		return (double)spatialIndegree(latMin,latMax,lonMin,lonMax) / (double)indegree();
	}

	public double spatialCloseness( ) { 
		double count = 0; 
		for ( int n : successorArray() ) count += graph.spatialDistance(current,n);
		for ( int n : ancestorArray() ) count += graph.spatialDistance(current,n);
		return count / (double)degree(); 
	}

	public double spatialOutCloseness( ) {
		double count = 0; 
		for ( int n : successorArray() ) count += graph.spatialDistance(current,n);
		return count / (double)indegree(); 
	}

	public double spatialInCloseness( ) { 
		double count = 0; 
		for ( int n : ancestorArray() ) count += graph.spatialDistance(current,n);
		return count / (double)outdegree();
	}

	public int[] successorArray( float latMin, float latMax, float lonMin, float lonMax ) { 
		it.unimi.dsi.fastutil.ints.IntList aux = new it.unimi.dsi.fastutil.ints.IntArrayList();
		for ( int n : successorArray() ) if( graph.spatialContainment(n,latMin,latMax,lonMin,lonMax) ) aux.add(n);
		return aux.toArray(new int[0]); 
	}

	public int[] ancestorArray( float latMin, float latMax, float lonMin, float lonMax ) { 
		it.unimi.dsi.fastutil.ints.IntList aux = new it.unimi.dsi.fastutil.ints.IntArrayList();
		for ( int n : ancestorArray() ) if( graph.spatialContainment(n,latMin,latMax,lonMin,lonMax) ) aux.add(n);
		return aux.toArray(new int[0]);
	}

	public Label[] labelArray( float latMin, float latMax, float lonMin, float lonMax ) { return successorLabelArray(latMin, latMax, lonMin, lonMax); }


	public Label[] successorLabelArray( float latMin, float latMax, float lonMin, float lonMax ) {
		List<Label> aux = new ArrayList<Label>();
		ArcLabelledNodeIterator.LabelledArcIterator it = successors(latMin,latMax,lonMin,lonMax);
		Integer i;
		while ( (i = it.nextInt()) != null && i >= 0 && ( i < graph.numNodes() ) ) try { aux.add(it.label()); } catch ( Exception ex ) { throw new Error(ex); }
		return aux.toArray(new Label[0]); 
	}

	public Label[] ancestorLabelArray( float latMin, float latMax, float lonMin, float lonMax ) {
		List<Label> aux = new ArrayList<Label>();
		ArcLabelledNodeIterator.LabelledArcIterator it = ancestors(latMin,latMax,lonMin,lonMax);
		Integer i;
		while ( (i = it.nextInt()) != null && i >= 0 && ( i < graph.numNodes() ) ) try { aux.add(it.label()); } catch ( Exception ex ) { throw new Error(ex); }
		return aux.toArray(new Label[0]);
	}

	public int[] successorArray( float lat, float lon, float radius ) { 
		it.unimi.dsi.fastutil.ints.IntList aux = new it.unimi.dsi.fastutil.ints.IntArrayList();
		for ( int n : successorArray() ) if( graph.spatialContainment(n,lat,lon,radius) ) aux.add(n);
		return aux.toArray(new int[0]); 
	}

	public int[] ancestorArray( float lat, float lon, float radius ) { 
		it.unimi.dsi.fastutil.ints.IntList aux = new it.unimi.dsi.fastutil.ints.IntArrayList();
		for ( int n : ancestorArray() ) if( graph.spatialContainment(n,lat,lon,radius) ) aux.add(n);
		return aux.toArray(new int[0]);
	}

	public Label[] labelArray( float lat, float lon, float radius ) { return successorLabelArray(lat, lon, radius); }


	public Label[] successorLabelArray( float lat, float lon, float radius ) {
		List<Label> aux = new ArrayList<Label>();
		ArcLabelledNodeIterator.LabelledArcIterator it = successors(lat,lon,radius);
		Integer i;
		while ( (i = it.nextInt()) != null && i >= 0 && ( i < graph.numNodes() ) ) try { aux.add(it.label()); } catch ( Exception ex ) { throw new Error(ex); }
		return aux.toArray(new Label[0]); 
	}

	public Label[] ancestorLabelArray( float lat, float lon, float radius ) {
		List<Label> aux = new ArrayList<Label>();
		ArcLabelledNodeIterator.LabelledArcIterator it = ancestors(lat,lon,radius);
		Integer i;
		while ( (i = it.nextInt()) != null && i >= 0 && ( i < graph.numNodes() ) ) try { aux.add(it.label()); } catch ( Exception ex ) { throw new Error(ex); }
		return aux.toArray(new Label[0]);
	}

	public ArcLabelledNodeIterator.LabelledArcIterator successors( float lat, float lon, float radius ) { return new SpatialLabelledArcIterator(it1.successors(), lat, lon, radius); }

	public ArcLabelledNodeIterator.LabelledArcIterator ancestors( float lat, float lon, float radius ) { return new SpatialLabelledArcIterator(it2.successors(), lat, lon, radius); }

	public ArcLabelledNodeIterator.LabelledArcIterator successors( float latMin, float latMax, float lonMin, float lonMax ) { return new SpatialLabelledArcIterator(it1.successors(),latMin,latMax,lonMin,lonMax); }

	public ArcLabelledNodeIterator.LabelledArcIterator ancestors( float latMin, float latMax, float lonMin, float lonMax ) { return new SpatialLabelledArcIterator(it2.successors(),latMin,latMax,lonMin,lonMax); }


	class SpatialLabelledArcIterator implements ArcLabelledNodeIterator.LabelledArcIterator {

		ArcLabelledNodeIterator.LabelledArcIterator it;

		Float radius = null;

		float lat1, lon1, lat2, lon2;

		public SpatialLabelledArcIterator ( ArcLabelledNodeIterator.LabelledArcIterator it, float lat, float lon, float radius ) {
			this.it = it;
			this.lat1 = lat;
			this.lon1 = lon;
			this.radius = radius;
		}

		public SpatialLabelledArcIterator ( ArcLabelledNodeIterator.LabelledArcIterator it, float latMin, float latMax, float lonMin, float lonMax ) {
			this.it = it;
			this.lat1 = latMin;
			this.lon1 = lonMin;
			this.lat2 = latMax;
			this.lon2 = lonMax;
			this.radius = null;
		}

		public Label label() { return it.label(); }

		public int nextInt() { 
			Integer i;
			while ( (i = it.nextInt()) != null && i >= 0 && ( i < graph.numNodes() ) ) try { 
				if( radius != null && graph.spatialContainment(i,lat1,lon1,radius) ) return i;
				if( radius != null && graph.spatialContainment(i,lat1,lat2,lon1,lon2) ) return i;
			} catch ( Exception ex ) { throw new Error(ex); }
			return i;
		}

		public int skip( int n ) { return it.skip(n); }

	}

}
