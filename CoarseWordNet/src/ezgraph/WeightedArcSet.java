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

public class WeightedArcSet implements Set<WeightedArc> {

  protected PrimaryTreeMap<WeightedArc,Boolean> set;

  public WeightedArcSet (  ) {
	try {
		File auxFile = File.createTempFile("graph-arcs-" + System.currentTimeMillis(),"aux");
		auxFile.deleteOnExit();
        	RecordManager recMan = RecordManagerFactory.createRecordManager(auxFile.getAbsolutePath());
		Serializer<WeightedArc> ser = new WeightedArcSerializer();
		Comparator<WeightedArc> comp = new WeightedArcSerializer();
		set = recMan.treeMap("arc-set", comp, new DoNothingSerializer(), ser);
	} catch ( IOException ex ) { throw new Error(ex); }
  }

  public WeightedArcSet ( String file ) {
	try {
        	RecordManager recMan = RecordManagerFactory.createRecordManager(file);
		Serializer<WeightedArc> ser = new WeightedArcSerializer();
		Comparator<WeightedArc> comp = new WeightedArcSerializer();
		set = recMan.treeMap("arc-set", comp, new DoNothingSerializer(), ser);
	} catch ( IOException ex ) { throw new Error(ex); }
  }

  public WeightedArcSet ( String file, String name ) {
	try {
        	RecordManager recMan = RecordManagerFactory.createRecordManager(file);
		Serializer<WeightedArc> ser = new WeightedArcSerializer();
		Comparator<WeightedArc> comp = new WeightedArcSerializer();
		set = recMan.treeMap("arc-set", comp, new DoNothingSerializer(), ser);
	} catch ( IOException ex ) { throw new Error(ex); }
  }

  public void commit () { 
	try { set.getRecordManager().commit(); } catch ( IOException e ) { throw new Error(e); }
  };

  protected void finalize () throws Throwable {
	super.finalize();
	set.clear();
	set.getRecordManager().commit();
	set.getRecordManager().close();
  }

  public boolean add ( WeightedArc arc ) { 
	if ( contains(arc) ) return false;
	set.put(arc,true); 
	return true; 
  };

  public boolean addAll(java.util.Collection<? extends WeightedArc> c) { 
	for ( WeightedArc a : c ) set.put(a,true); 
	return true;
  };

  public void clear() { set.clear(); } 

  public boolean contains(Object o) { return set.containsKey(o); }

  public boolean containsAll(Collection c) { 
	for ( Object a : c ) if (!set.containsKey(a)) return false;
	return true;
  }

  public boolean equals(Object o) { 
	if ( o instanceof WeightedArcSet ) return ((WeightedArcSet)o).set.equals(set); 
	return false;
  }

  public int hashCode() { return set.hashCode(); }

  public boolean isEmpty() { return set.isEmpty(); }

  public Iterator<WeightedArc> iterator() { return set.keySet().iterator(); }

  public boolean remove(Object o) { return set.remove(o); }

  public boolean removeAll(Collection c) { 
	for ( Object a : c ) set.remove(a);
	return true;
  }

  public boolean retainAll(Collection c) { throw new UnsupportedOperationException(); }

  public int size() { return set.size(); }
  
  public Object[] toArray() { return set.keySet().toArray(); }

  public <T> T[] toArray(T[] a) { return set.keySet().toArray(a); }

}

class DoNothingSerializer implements Serializer<Boolean> {

 public void serialize( SerializerOutput out, Boolean obj ) throws IOException { }

 public Boolean deserialize( SerializerInput in ) throws IOException, ClassNotFoundException { return true; }

}

class WeightedArcSerializer implements Serializer<WeightedArc>, Comparator<WeightedArc>, Serializable {

 public int compare ( WeightedArc obj1 , WeightedArc obj2 ) {
	int c1 = obj1.src - obj2.src;
	int c2 = obj1.dest - obj2.dest;
	float w = (obj1.weight - obj2.weight);
	if ( w != 0 ) { if (w < 0) return -1; else return 1; }
	if ( c1 != 0 ) { if (c1 < 0) return -1; else return 1; }
	if ( c2 != 0 ) { if (c2 < 0) return -1; else return 1; }
	return 0;
 }

 public void serialize( SerializerOutput out, WeightedArc obj ) throws IOException {
	out.writeObject(obj.weight);
	out.writePackedInt(obj.src);
	out.writePackedInt(obj.dest);
 }

 public WeightedArc deserialize( SerializerInput in ) throws IOException, ClassNotFoundException {
        Constructor[] cons = WeightedArc.class.getDeclaredConstructors();
        for ( int i = 0; i< cons.length; i++) cons[i].setAccessible(true);
	Float weight = (Float)(in.readObject());	
	int i1 = in.readPackedInt();
	int i2 = in.readPackedInt();
	try {
		return (WeightedArc)cons[0].newInstance(i1,i2,weight);
	} catch ( Exception ex ) { throw new ClassNotFoundException(ex.toString()); }
 }

}
