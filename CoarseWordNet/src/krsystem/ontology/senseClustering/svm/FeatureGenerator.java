package krsystem.ontology.senseClustering.svm;

import java.io.FileInputStream;
import java.util.List;

import jnisvmlight.LabeledFeatureVector;
import jnisvmlight.SVMLightInterface;
import krsystem.utility.OrderedPair;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;

public class FeatureGenerator 
{
	Dictionary dict;
	WNBasedSimilarity wnbs;
	CorporaBasedSimilarity cbs;
	BabelNetBasedSimilarity bnbs;
	int MiMoHeuristicK = 3;
	
	public FeatureGenerator(String dir, String arg, int MiMoHeuristicKPassed, String domainDataPath, Dictionary dictionary, String OEDMappingPathNoun, String sentimentFilePath)
	{
		wnbs = new WNBasedSimilarity(dir, arg, MiMoHeuristicKPassed, dictionary);
		cbs = new CorporaBasedSimilarity(domainDataPath, OEDMappingPathNoun, sentimentFilePath);
		bnbs = new BabelNetBasedSimilarity();
	}
	
	public FeatureGenerator(String dir, String arg, String domainDataPath, Dictionary dictionary, String OEDMappingPathNoun, String sentimentFilePath)
	{
		wnbs = new WNBasedSimilarity(dir, arg, MiMoHeuristicK, dictionary);
		cbs = new CorporaBasedSimilarity(domainDataPath, OEDMappingPathNoun, sentimentFilePath);
		bnbs = new BabelNetBasedSimilarity();
	}
	
	public OrderedPair<Integer, LabeledFeatureVector> getLabeledFeatureVector(Instance instance)	
	{		
		int label = instance.label;
		Synset smaller = instance.smaller;
		Synset larger = instance.larger;
//		String smallerGloss = smaller.getGloss();
//		String largerGloss = larger.getGloss();
//		
//		HashSet<String> smallerSet = new HashSet<String>();
//		for(String s : smallerGloss.split("\\s+"))
//			smallerSet.add(s);
//		
//		HashSet<String> largerSet = new HashSet<String>();
//		for(String s : largerGloss.split("\\s+"))
//			largerSet.add(s);
//		
//		smallerSet.retainAll(largerSet);
		
		
		int index = 0;
		int dimNum = 0;
		
		double[] features1 = wnbs.getSimilarities(smaller, larger);
		dimNum += features1.length;
		
		double[] features2 = wnbs.getFeatures(smaller, larger);
		dimNum += features2.length;
		
		double[] features3 = wnbs.getFeaturesMiMoSP1(smaller, larger);
		dimNum += features3.length;
		
		double[] features4 = cbs.getFeatures(smaller, larger);
		dimNum += features4.length;
				
		double[] features5 = bnbs.getFeatures(smaller, larger);
		dimNum += features5.length;
		
		int[] dims = new int[dimNum];
		for(int i=1; i<=dimNum;i++)
			dims[i-1] = i;
		
		double[] vals = new double[dimNum];
		System.arraycopy(features1, 0, vals, index, features1.length); 
		index+=features1.length;
		System.arraycopy(features2, 0, vals, index, features2.length); 
		index+=features2.length;
		System.arraycopy(features3, 0, vals, index, features3.length); 
		index+=features3.length;
		System.arraycopy(features4, 0, vals, index, features4.length); 
		index+=features4.length;
		System.arraycopy(features5, 0, vals, index, features5.length); 
		index+=features5.length;
		
		LabeledFeatureVector features = new LabeledFeatureVector(label, dims, vals);		
		return new OrderedPair<Integer, LabeledFeatureVector>(dimNum, features);		
	}

	public static void main(String[] args) {
		String propsFile30 = "resources/file_properties.xml";
		String dir = "/home/sumitb/Data/";
		String arg = "WordNet-3.0";		
		String domainDataPathNoun = "/home/sumitb/Data/xwnd/joinedPOSSeparated/joinedNoun.txt";
		String OEDMappingPathNoun = "/home/sumitb/Data/navigli_sense_inventory/mergeData-30.offsets.noun";
		String sentimentFilePath = "/home/sumitb/Data/SentiWordNet/SentiWordNet.n";
		try{			
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();			
			FeatureGenerator fg = new FeatureGenerator(dir, arg, 3, domainDataPathNoun, dictionary, OEDMappingPathNoun, sentimentFilePath);
			List<Synset> syns = dictionary.getIndexWord(POS.NOUN, "head").getSenses();
			Instance instance = new Instance(syns.get(0), syns.get(1), 1);
			OrderedPair<Integer, LabeledFeatureVector> dimNumLFVPair = fg.getLabeledFeatureVector(instance); 
			LabeledFeatureVector lfv = dimNumLFVPair.getR();
			for(int i=0; i<lfv.size(); i++)
				System.out.println(lfv.getValueAt(i));
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

	}

}
