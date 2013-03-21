package ezgraph;

import java.io.*;
import java.util.*;
import java.lang.Math;
import it.unimi.dsi.fastutil.ints.*;

public class FindKCliques {
   
   private Graph graph;
   private int sizeClique;

   private List<List> cliques;
         
   public FindKCliques( Graph graph ) {      
      this.graph = graph;
      cliques = new ArrayList();
      sizeClique = (int)Math.floor(0.5 * Math.log((double) graph.numNodes()) / Math.log(2.0));
   }
   
   public FindKCliques( Graph graph , int sizeClique ) {      
      this.graph = graph;
      this.sizeClique = sizeClique;
      cliques = new ArrayList();
      doCliqueBT(new ArrayList(), 0);
   }
   
   private boolean isConnected(int i, int j) { return graph.isConnected(i,j); }
   
   private void doCliqueBT(List A, int j) {
      if (j == sizeClique) {         
         cliques.add(A);
         return;
      } else {
         j = j + 1;
         List Sj = new ArrayList();
         if (j <= sizeClique) { Sj = getCandidates(A); }
         if (!Sj.isEmpty()) {
            for (int i=0; i<Sj.size(); i++) {
               List a = (List)Sj.get(i);
               doCliqueBT(a, j);
            }            
         }         
      }
   }
   
   private List getCandidates(List A) {
      List candidates = new ArrayList();
      if (A.isEmpty()) {
         for (int i=0; i<graph.numNodes(); i++) {
            List sj = new ArrayList(1);
            sj.add(new Integer(i));
            candidates.add(sj);
         }
      } else {
         Integer last = (Integer)(A.get(A.size()-1));
         int q = last.intValue()+1; 
	 for (int j=q; j<graph.numNodes(); j++) {
	        boolean allConnected = true;
	        Iterator iter = A.iterator();
	        while (iter.hasNext()) {
	           Integer v = (Integer)iter.next();
	           int i = v.intValue();			   
	           if (!isConnected(i,j)) {
	              allConnected = false;
	              break;
	           }
	        }
	        if (allConnected) {
            	   List sj = new ArrayList();
		   sj.addAll(A);
	           sj.add(new Integer(j));
	           candidates.add(sj);
	        }        
	     }
      }
      return candidates;
   }   
   
}
