package krsystem.ontology.senseClustering.svm;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;


public class CorporaBasedSimilarity {

	HashMap<String, double[]> domainMap = new HashMap<String, double[]>();
	HashSet<String> OEDMergeData = new HashSet<String>(); 
	HashMap<String, double[]> SentiWNData = new HashMap<String, double[]>(); 
	
	public CorporaBasedSimilarity(String domainMapFilePath, String OEDMappingPath, String sentimentFilePath)
	{		
		try{
			// Loading Extended Domain Label Dataset
			System.out.print("Loading Domain Values...");
			Scanner sc = new Scanner(new File(domainMapFilePath));
			while(sc.hasNextLine())
			{
				String line = sc.nextLine();
				String[] split = line.split("\\s+");
				String key = split[0];
//				long offset = Long.parseLong(split[0].split("-")[0]);
				String posString = split[0].split("-")[1];
				if(posString.equalsIgnoreCase("n") || posString.equalsIgnoreCase("v")) // no adjectives and adverbs
				{
					double[] weightVector = new double[split.length-1];
					for(int i=0; i<split.length-1;i++)
					{
						weightVector[i] = Double.parseDouble(split[i+1]);
					}
					domainMap.put(key, weightVector);
				}
			}
			sc.close();
			System.out.println("Loaded Domain values !");
			
			// Loading OED Data
			System.out.print("Loading OED Merged Pairs ...");
			sc = new Scanner(new File(OEDMappingPath));
			while(sc.hasNextLine())
			{
				String line = sc.nextLine().trim();
				OEDMergeData.add(line);
			}
			sc.close();
			System.out.println("Loaded OED Merged Pairs!");
			
			// Loading SentiWordNet Dataset
			System.out.print("Loading Sentiment Values...");
			sc = new Scanner(new File(sentimentFilePath));
			while(sc.hasNextLine())
			{
				String line = sc.nextLine();
				String[] split = line.split("\\s+");
				String key = split[0];
//				long offset = Long.parseLong(split[0].split("-")[0]);
				String posString = split[0].split("#")[0];
				if(posString.equalsIgnoreCase("n") || posString.equalsIgnoreCase("v")) // no adjectives and adverbs
				{
					double[] weightVector = new double[split.length-1];
					for(int i=0; i<split.length-1;i++)
					{
						weightVector[i] = Double.parseDouble(split[i+1]);
					}
					SentiWNData.put(key, weightVector);
				}
			}
			sc.close();
			System.out.println("Loaded Sentiment values !");
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public double[] getFeatures(Synset syn1, Synset syn2)
	{
		String offsetString1 = String.format("%08d", syn1.getOffset());
		String offsetString2 = String.format("%08d", syn2.getOffset());
		String posString1 = syn1.getPOS().equals(POS.NOUN) ? "n" : "v";
		String posString2 = syn2.getPOS().equals(POS.NOUN) ? "n" : "v";
		String key1Minus = offsetString1+"-"+posString1;
		String key2Minus = offsetString2+"-"+posString2;
		String key1Hash = offsetString1+"#"+posString1;
		String key2Hash = offsetString2+"#"+posString2;
		
		int featuresNum = 0;
		
		double[] features1 = getDomainFeatures(key1Minus, key2Minus);
		featuresNum += features1.length;
		double[] features2 = getOEDFeature(offsetString1, offsetString2);
		featuresNum += features2.length;
		double[] features3 = getSentimentFeatures(key1Hash, key2Hash);
		featuresNum += features3.length;
		
		double[] features = new double[featuresNum];
				
		int index = 0;		
		System.arraycopy(features1, 0, features, index, features1.length); 
		index+=features1.length;
		System.arraycopy(features2, 0, features, index, features2.length); 
		index+=features2.length;
		System.arraycopy(features3, 0, features, index, features3.length); 
		index+=features3.length;
		
		return features;
	}
	
	public double[] getDomainFeatures(String key1, String key2)
	{				

		double[] features1 = domainMap.get(key1);
		double[] features2 = domainMap.get(key2);
		double cosineSimilarity = 0;
		double l1Distance = 0;
		double l2Distance = 0;
		double norm1 = 0;
		double norm2 = 0;
//		double sum1 = 0;
//		double sum2 = 0;
		
		if(features1!=null && features2!=null)
		{
			for(int j=0; j<features1.length; j++)
			{
//				sum1 += features1[j];
//				sum2 += features2[j];
				norm1 += features1[j]*features1[j];
				norm2 += features2[j]*features2[j];
				cosineSimilarity += features1[j]*features2[j];
				l1Distance += Math.abs(features1[j]-features2[j]);
				l2Distance += Math.pow(features1[j]-features2[j] , 2);
			}
			norm1 = Math.sqrt(norm1);
			norm2 = Math.sqrt(norm2);
			cosineSimilarity /= (norm1*norm2); 
		}
		
//		System.out.println("Sum 1 : "+sum1);
//		System.out.println("Sum 2 : "+sum2);		
		
		double[] features = new double[3];
		features[0] = cosineSimilarity;
		features[1] = l1Distance;
		features[2] = l2Distance;		
		return features;
	}
	
	public double[] getOEDFeature(String offsetString1, String offsetString2)
	{
		double[] features = new double[1];
		// Check if the mapping is present in Navigli-OED Mappings
		String toCheck = "";
		if(offsetString1.compareToIgnoreCase(offsetString2) < 0)
			toCheck = offsetString1+"#"+offsetString2;
		else
			toCheck = offsetString2+"#"+offsetString1;
		boolean found = OEDMergeData.contains(toCheck);
		features[0] = found ? 1 : 0;
		return features;
	}

	public double[] getSentimentFeatures(String key1, String key2)
	{				

		double[] features1 = SentiWNData.get(key1);
		double[] features2 = SentiWNData.get(key2);
		double cosineSimilarity = 0;
		double l1Distance = 0;
		double l2Distance = 0;
		double norm1 = 0;
		double norm2 = 0;
//		double sum1 = 0;
//		double sum2 = 0;
		
		if(features1!=null && features2!=null)
		{
			for(int j=0; j<features1.length; j++)
			{
//				sum1 += features1[j];
//				sum2 += features2[j];
				norm1 += features1[j]*features1[j];
				norm2 += features2[j]*features2[j];
				cosineSimilarity += features1[j]*features2[j];
				l1Distance += Math.abs(features1[j]-features2[j]);
				l2Distance += Math.pow(features1[j]-features2[j] , 2);
			}
			norm1 = Math.sqrt(norm1);
			norm2 = Math.sqrt(norm2);
			cosineSimilarity /= (norm1*norm2); 
		}
		
//		System.out.println("Sum 1 : "+sum1);
//		System.out.println("Sum 2 : "+sum2);		
		
		double[] features = new double[3];
		features[0] = cosineSimilarity;
		features[1] = l1Distance;
		features[2] = l2Distance;		
		return features;
	}
	
	
/*	HashMap<String, List<String>> domainMapNoun;
	HashMap<String, List<String>> domainMapVerb;
	
	public CorporaBasedSimilarity(String domainMapNounPath, String domainMapVerbPath)
	{
		domainMapNoun = new HashMap<String, List<String>>();
		domainMapVerb = new HashMap<String, List<String>>();
		try{
			Scanner sc = new Scanner(new File(domainMapNounPath));
			while(sc.hasNextLine())
			{
				String line = sc.nextLine();
				String[] split = line.split("\\s+");
				long offset = Long.parseLong(split[0].split("-")[0]);
				List<String> labels = new ArrayList<String>();
				for(int i=1; i<split.length; i++)
				{
					if(! split[i].equalsIgnoreCase("factotum"))
						labels.add(split[i]);
				}
				if(labels.size()>0)
				{
					String offsetString = String.format("%08d", offset);	
					domainMapNoun.put(offsetString, labels); // change key to offsetString#n ?
				}
			}
			sc.close();
			
			sc = new Scanner(new File(domainMapVerbPath));
			while(sc.hasNextLine())
			{
				String line = sc.nextLine().trim();
				String[] split = line.split("\\s+");
				long offset = Long.parseLong(split[0].split("-")[0]);
				List<String> labels = new ArrayList<String>();
				for(int i=1; i<split.length; i++)
				{
					if(! split[i].equalsIgnoreCase("factotum"))
						labels.add(split[i]);
				}
				if(labels.size()>0)
				{
					String offsetString = String.format("%08d", offset);	
					domainMapVerb.put(offsetString, labels); // change key to offsetString#v ?
				}
			}
			sc.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public double[] getFeatures(Synset syn1, Synset syn2, String pos)
	{
		double[] features = new double[9];
		String offset1 = String.format("%08d", syn1.getOffset());
		String offset2 = String.format("%08d", syn2.getOffset());
		
		int commonLabels = 0;
				
		HashMap<String, List<String>> domainMap = null;
		if(pos.equalsIgnoreCase("n"))
		{
			domainMap = domainMapNoun;
		}
		else if(pos.equalsIgnoreCase("v"))
		{
			domainMap = domainMapVerb;			
		}
		
		if(domainMap != null)
		{
			List<String> labels = domainMap.get(offset1);
			HashSet<String> domains1= new HashSet<String>(labels);			
			for(String label : domainMap.get(offset2))
			{
				if(domains1.contains(label))
					commonLabels++;
			}
		}
				
		
		return features;
	}
	*/
	
	public static void main(String[] args)
	{
		String propsFile30 = "resources/file_properties.xml";		
		String domainDataPathNoun = "/home/sumitb/Data/xwnd/joinedPOSSeparated/joinedNoun.txt";
		String OEDMappingPathNoun = "/home/sumitb/Data/navigli_sense_inventory/mergeData-30.offsets.noun";
		String sentimentFilePath = "/home/sumitb/Data/SentiWordNet/SentiWordNet.n";
		try{			
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dict = Dictionary.getInstance();
			IndexWord iw = dict.getIndexWord(POS.NOUN, "head");
			List<Synset> syn = iw.getSenses();
			CorporaBasedSimilarity cbs = new CorporaBasedSimilarity(domainDataPathNoun, OEDMappingPathNoun, sentimentFilePath);
			double[] features = cbs.getFeatures(syn.get(0),syn.get(1));
			for(double feature : features)
			{
				System.out.println(feature);
			}
			features = cbs.getFeatures(syn.get(1),syn.get(2));
			for(double feature : features)
			{
				System.out.println(feature);
			}
//			for(String frame : syn.get(0).getVerbFrames())
//				System.out.println(frame);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	
}
