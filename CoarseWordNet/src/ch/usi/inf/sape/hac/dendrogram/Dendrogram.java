/*
 * This file is licensed to You under the "Simplified BSD License".
 * You may not use this software except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/bsd-license.php
 * 
 * See the COPYRIGHT file distributed with this work for information
 * regarding copyright ownership.
 */
package ch.usi.inf.sape.hac.dendrogram;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.StringTokenizer;

import ch.usi.inf.sape.hac.experiment.Experiment;


/**
 * A Dendrogram represents the results of hierachical agglomerative clustering.
 * The root represents a single cluster containing all observations.
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class Dendrogram {

    private final DendrogramNode root;


    public Dendrogram(DendrogramNode root) {
        this.root = root;
    }

    public DendrogramNode getRoot() {
        return root;
    }

    public void dump() {
        dumpNode("  ", root);
    }

    private void dumpNode(final String indent, final DendrogramNode node) {
        if (node==null) {
            System.out.println(indent+"<null>");
        } else if (node instanceof ObservationNode) {
            System.out.println(indent+"Observation: "+((ObservationNode)node).getObservation());
        } else if (node instanceof MergeNode) {
            System.out.println(indent+"Merge:"+((MergeNode)node).getDissimilarity());
            dumpNode(indent+"  ", ((MergeNode)node).getLeft());
            dumpNode(indent+"  ", ((MergeNode)node).getRight());
        }
    }
    
    public void writeToFile(String file) throws IOException
    {
    	BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file)));
    	writeTree(root, bw);
    	bw.close();
    }
    
    private void writeTree(DendrogramNode node, BufferedWriter bw) throws IOException
    {
    	if(node instanceof ObservationNode)
    		bw.write("O#"+((ObservationNode) node).getObservation()+"\n");
    	else
    	{
    		MergeNode mNode = ((MergeNode)node); 
    		bw.write("M#"+mNode.getDissimilarity()+"#"+mNode.getObservationCount()+"\n");
    		writeTree(node.getLeft(), bw);
    		writeTree(node.getRight(), bw);
    	}
    }
    
    private static DendrogramNode readTree(BufferedReader br) throws Exception
    {    	
    	String token = br.readLine();
    	if(token != null)
    	{
    		if(token.startsWith("O"))
    		{
    			return new ObservationNode(Integer.parseInt(token.split("#")[1]));    			
    		}
    		else // mergeNode
    		{
    			DendrogramNode left = readTree(br);    	
    			DendrogramNode right = readTree(br);    					
    			double dissimilarity = Double.parseDouble(token.split("#")[1]);
    			int obsCount = Integer.parseInt(token.split("#")[2]);
    			return new MergeNode(left, right, dissimilarity, obsCount);    			    			    			
    		}
    	}
    	return null; // Should never happen
    }
    
    public static Dendrogram readFromFile(String file) throws Exception
    {
    	BufferedReader br = new BufferedReader(new FileReader(new File(file))); 
    	DendrogramNode root = readTree(br);
    	br.close();    	
    	return new Dendrogram(root);
    }
    
}
