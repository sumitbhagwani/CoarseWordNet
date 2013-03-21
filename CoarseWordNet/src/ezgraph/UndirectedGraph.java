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

public class UndirectedGraph extends Graph {

  public UndirectedGraph ( ) { this( new WeightedArc[]{} ); }
  
  public UndirectedGraph ( String file ) throws IOException {
	try {
		File auxFile = File.createTempFile("graph-maps-" + System.currentTimeMillis(),"aux");
		auxFile.deleteOnExit();
        	RecordManager recMan = RecordManagerFactory.createRecordManager(auxFile.getAbsolutePath());
		nodes = recMan.hashMap("nodes");
		nodesReverse = recMan.hashMap("nodesReverse");
	} catch ( IOException ex ) { throw new Error(ex); }
	nodes.clear();
	nodesReverse.clear();
        Constructor[] cons = WeightedArc.class.getDeclaredConstructors();
        for ( int i = 0; i< cons.length; i++) cons[i].setAccessible(true);
	numArcs = 0;
	String aux = null;
	Float weight = (float)1.0;
	WeightedArcSet list = new WeightedArcSet();
	BufferedReader br;
	try { 
		br = new BufferedReader(new InputStreamReader( new GZIPInputStream( new FileInputStream(file) ))); 
	} catch ( Exception ex ) { 
		br = new BufferedReader(new FileReader(file));
	}
	while ( ( aux = br.readLine() ) != null ) try {
		if ( commit++ % COMMIT_SIZE == 0 ) { commit(); list.commit(); }
		String parts[] = aux.split("\t");
		String l1 = new String(parts[0]);
		String l2 = new String(parts[1]);
		if ( !nodesReverse.containsKey(l1) ) { nodesReverse.put(l1, nodesReverse.size()); nodes.put(nodes.size(), l1); }
		if ( !nodesReverse.containsKey(l2) ) { nodesReverse.put(l2, nodesReverse.size()); nodes.put(nodes.size(), l2); }
		if ( parts.length == 3 ) weight = new Float(parts[2]);
		list.add((WeightedArc)cons[0].newInstance(nodesReverse.get(l1),nodesReverse.get(l2),weight));
		list.add((WeightedArc)cons[0].newInstance(nodesReverse.get(l2),nodesReverse.get(l1),weight));
	} catch ( Exception ex ) { throw new Error(ex); }
	this.graph = new WeightedBVGraph( list.toArray( new WeightedArc[0] ) );
	this.reverse = graph;
	br.close();
	numArcs = list.size();
	iterator = nodeIterator();
	try {
		File auxFile = File.createTempFile("graph" + System.currentTimeMillis(),"aux");
		auxFile.deleteOnExit();
		String basename = auxFile.getAbsolutePath();
		store(basename);
	} catch ( IOException ex ) { throw new Error(ex); }
  }

  public UndirectedGraph ( WeightedArc[] arcs ) { this( new WeightedBVGraph( arcs ) ); }

  public UndirectedGraph ( WeightedArc[] arcs, String[] names ) { this( new WeightedBVGraph( arcs ) , names ); }

  public UndirectedGraph ( ArcLabelledImmutableGraph graph ) { }

  public UndirectedGraph ( ArcLabelledImmutableGraph graph , String names[] ) { }

  public UndirectedGraph ( WeightedBVGraph graph ) {
	try {
		File auxFile = File.createTempFile("graph-maps-" + System.currentTimeMillis(),"aux");
		auxFile.deleteOnExit();
        	RecordManager recMan = RecordManagerFactory.createRecordManager(auxFile.getAbsolutePath());
		nodes = recMan.hashMap("nodes");
		nodesReverse = recMan.hashMap("nodesReverse");
	} catch ( IOException ex ) { throw new Error(ex); }
	nodes.clear();
	nodesReverse.clear();
        Constructor[] cons = WeightedArc.class.getDeclaredConstructors();
        for ( int i = 0; i< cons.length; i++) cons[i].setAccessible(true);
	this.graph = graph;
	this.reverse  = graph;
	WeightedArcSet list = new WeightedArcSet();
	ArcLabelledNodeIterator it = graph.nodeIterator();
	numArcs = 0;
	while ( it.hasNext() ) {
		Integer aux1 = it.nextInt();
		Integer aux2 = null;
		ArcLabelledNodeIterator.LabelledArcIterator suc = it.successors();
		while ( (aux2 = suc.nextInt()) != null && aux2 >= 0 && ( aux2 < graph.numNodes() ) ) try {
		  if ( commit++ % COMMIT_SIZE == 0 ) { commit(); list.commit(); }
		  list.add((WeightedArc)cons[0].newInstance(aux1,aux2,suc.label().getFloat()));
		  list.add((WeightedArc)cons[0].newInstance(aux2,aux1,suc.label().getFloat()));
		  this.nodes.put(aux1, "" + aux1);
		  this.nodes.put(aux2, "" + aux2);
		  this.nodesReverse.put("" + aux1, aux1);
		  this.nodesReverse.put("" + aux2, aux2);
		  numArcs++;
                } catch ( Exception ex ) { throw new Error(ex); }
	}
	this.graph = new WeightedBVGraph( list.toArray( new WeightedArc[0] ) );
	this.reverse = this.graph;
	numArcs = list.size();
	iterator = nodeIterator();
	try {
		File auxFile = File.createTempFile("graph" + System.currentTimeMillis(),"aux");
		auxFile.deleteOnExit();
		String basename = auxFile.getAbsolutePath();
		store(basename);
	} catch ( IOException ex ) { throw new Error(ex); }
  }

  public UndirectedGraph ( WeightedBVGraph graph, String[] names ) {
	if ( names.length != graph.numNodes() ) throw new Error("Problem with the list of names for the nodes in the graph.");
	try {
		File auxFile = File.createTempFile("graph-maps-" + System.currentTimeMillis(),"aux");
		auxFile.deleteOnExit();
        	RecordManager recMan = RecordManagerFactory.createRecordManager(auxFile.getAbsolutePath());
		nodes = recMan.hashMap("nodes");
		nodesReverse = recMan.hashMap("nodesReverse");
	} catch ( IOException ex ) { throw new Error(ex); }
	nodes.clear();
	nodesReverse.clear();
        Constructor[] cons = WeightedArc.class.getDeclaredConstructors();
        for ( int i = 0; i< cons.length; i++) cons[i].setAccessible(true);
	this.graph = graph;
	this.reverse = graph;
	WeightedArcSet list = new WeightedArcSet();
	ArcLabelledNodeIterator it = graph.nodeIterator();
	numArcs = 0;
	while ( it.hasNext() ) {
		Integer aux1 = it.nextInt();
		Integer aux2 = null;
		ArcLabelledNodeIterator.LabelledArcIterator suc = it.successors();
		while ( (aux2 = suc.nextInt()) != null && aux2 >= 0 && ( aux2 < graph.numNodes() ) ) try {
		  if ( commit++ % COMMIT_SIZE == 0 ) { commit(); list.commit(); }
		  list.add((WeightedArc)cons[0].newInstance(aux1,aux2,suc.label().getFloat()));
		  list.add((WeightedArc)cons[0].newInstance(aux2,aux1,suc.label().getFloat()));
		  this.nodes.put(aux1, names[aux1]);
		  this.nodes.put(aux2, names[aux2]);
		  this.nodesReverse.put(names[aux1], aux1);
		  this.nodesReverse.put(names[aux2], aux2);
		  numArcs++;
                } catch ( Exception ex ) { throw new Error(ex); }
	}
	this.graph = new WeightedBVGraph( list.toArray( new WeightedArc[0] ) );
	this.reverse = this.graph;
	numArcs = list.size();
	iterator = nodeIterator();
	try {
		File auxFile = File.createTempFile("graph" + System.currentTimeMillis(),"aux");
		auxFile.deleteOnExit();
		String basename = auxFile.getAbsolutePath();
		store(basename);
	} catch ( IOException ex ) { throw new Error(ex); }
  }

  public UndirectedGraph ( BVGraph graph ) {
	try {
		File auxFile = File.createTempFile("graph-maps-" + System.currentTimeMillis(),"aux");
		auxFile.deleteOnExit();
        	RecordManager recMan = RecordManagerFactory.createRecordManager(auxFile.getAbsolutePath());
		nodes = recMan.hashMap("nodes");
		nodesReverse = recMan.hashMap("nodesReverse");
	} catch ( IOException ex ) { throw new Error(ex); }
	nodes.clear();
	nodesReverse.clear();
        Constructor[] cons = WeightedArc.class.getDeclaredConstructors();
        for ( int i = 0; i< cons.length; i++) cons[i].setAccessible(true);
	Integer aux1 = null;
	WeightedArcSet list = new WeightedArcSet();
	it.unimi.dsi.webgraph.NodeIterator it = graph.nodeIterator();
	while ( (aux1 = it.nextInt()) != null) {
		LazyIntIterator suc = it.successors();
		Integer aux2 = null;
		while ( (aux2 = suc.nextInt()) != null && aux2 >= 0 && ( aux2 < graph.numNodes() ) ) try {
		  if ( commit++ % COMMIT_SIZE == 0 ) { commit(); list.commit(); }
		  list.add((WeightedArc)cons[0].newInstance(aux1,aux2,(float)1.0));
		  list.add((WeightedArc)cons[0].newInstance(aux2,aux1,(float)1.0));
		  this.nodes.put(aux1, "" + aux1);
		  this.nodes.put(aux2, "" + aux2);
		  this.nodesReverse.put("" + aux1, aux1);
		  this.nodesReverse.put("" + aux2, aux2);
                } catch ( Exception ex ) { throw new Error(ex); }
	}
	this.graph = new WeightedBVGraph( list.toArray( new WeightedArc[0] ) );
	this.reverse = this.graph;
	numArcs = list.size();
	iterator = nodeIterator();
	try {
		File auxFile = File.createTempFile("graph" + System.currentTimeMillis(),"aux");
		auxFile.deleteOnExit();
		String basename = auxFile.getAbsolutePath();
		store(basename);
	} catch ( IOException ex ) { throw new Error(ex); }
  }

  public UndirectedGraph ( BVGraph graph, String[] names ) {
	if ( names.length != graph.numNodes() ) throw new Error("Problem with the list of names for the nodes in the graph.");
	try {
		File auxFile = File.createTempFile("graph-maps-" + System.currentTimeMillis(),"aux");
		auxFile.deleteOnExit();
        	RecordManager recMan = RecordManagerFactory.createRecordManager(auxFile.getAbsolutePath());
		nodes = recMan.hashMap("nodes");
		nodesReverse = recMan.hashMap("nodesReverse");
	} catch ( IOException ex ) { throw new Error(ex); }
	nodes.clear();
	nodesReverse.clear();
        Constructor[] cons = WeightedArc.class.getDeclaredConstructors();
        for ( int i = 0; i< cons.length; i++) cons[i].setAccessible(true);
	Integer aux1 = null;
	WeightedArcSet list = new WeightedArcSet();
	it.unimi.dsi.webgraph.NodeIterator it = graph.nodeIterator();
	while ( (aux1 = it.nextInt()) != null) {
		LazyIntIterator suc = it.successors();
		Integer aux2 = null;
		while ( (aux2 = suc.nextInt()) != null && aux2 >= 0 && ( aux2 < graph.numNodes() ) ) try {
		  if ( commit++ % COMMIT_SIZE == 0 ) { commit(); list.commit(); }
		  list.add((WeightedArc)cons[0].newInstance(aux1,aux2,(float)1.0));
		  list.add((WeightedArc)cons[0].newInstance(aux2,aux1,(float)1.0));
		  this.nodes.put(aux1, names[aux1]);
		  this.nodes.put(aux2, names[aux2]);
		  this.nodesReverse.put(names[aux1], aux1);
		  this.nodesReverse.put(names[aux2], aux2);
                } catch ( Exception ex ) { throw new Error(ex); }
	}
	this.graph = new WeightedBVGraph( list.toArray( new WeightedArc[0] ) );
	this.reverse = this.graph;
	numArcs = list.size();
	iterator = nodeIterator();
	try {
		File auxFile = File.createTempFile("graph" + System.currentTimeMillis(),"aux");
		auxFile.deleteOnExit();
		String basename = auxFile.getAbsolutePath();
		store(basename);
	} catch ( IOException ex ) { throw new Error(ex); }
  }

  public Graph transpose ( ) { return copy(); }

  public UndirectedGraph copy() {
        Constructor[] cons = WeightedArc.class.getDeclaredConstructors();
        for ( int i = 0; i< cons.length; i++) cons[i].setAccessible(true);
	WeightedArcSet list = new WeightedArcSet();
	ArcLabelledNodeIterator it = graph.nodeIterator();
	while ( it.hasNext() ) {
		Integer aux1 = it.nextInt();
		Integer aux2 = null;
		ArcLabelledNodeIterator.LabelledArcIterator suc = it.successors();
		while ( (aux2 = suc.nextInt()) != null && aux2 >= 0 && ( aux2 < graph.numNodes() ) ) try {
		  if ( commit++ % COMMIT_SIZE == 0 ) { list.commit(); }
		  WeightedArc arc = (WeightedArc)cons[0].newInstance(aux2, aux1, suc.label().getFloat());
		  list.add(arc);
                } catch ( Exception ex ) { throw new Error(ex); }
	}
	UndirectedGraph result = new UndirectedGraph( list.toArray( new WeightedArc[0] ) );
	result.nodes.clear();
	result.nodesReverse.clear();
	for ( Integer n : this.nodes.keySet() ) {
		if ( commit++ % COMMIT_SIZE == 0 ) { result.commit(); }
		result.nodesReverse.put(this.nodes.get(n) , n);
		result.nodes.put(n , this.nodes.get(n));
	}
	return result;
  }

  public boolean isConnected ( int x, int y ) { return isSuccessor(x,y); }

}
