package krsystem.ontology.senseClustering;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Scanner;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.DictionaryElementType;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

public class Senseval2Processing {

	public static void mappingSanityCheck17(HashMap<String, ArrayList<String>> versionMap, Dictionary dict, POS pos)
	{
		int validCount = 0;
		int invalidCount = 0;
		for(String offset17 : versionMap.keySet())
		{
			try{
				dict.getSynsetAt(pos, Long.parseLong(offset17));
				validCount++;
			}
			catch(Exception ex)
			{
				invalidCount++;
			}
		}
		System.out.println("valid : "+validCount); // noun : 74323
		System.out.println("invalid : "+invalidCount); // noun : 2
	}
	
	public static void mappingSanityCheck30(HashMap<String, ArrayList<String>> versionMap, Dictionary dict, POS pos)
	{
		int validCount = 0;
		int invalidCount = 0;
		for(Entry<String, ArrayList<String>> entry: versionMap.entrySet())
		{
			for(String offset30 : entry.getValue())
			{
				try{
					dict.getSynsetAt(pos, Long.parseLong(offset30));
					validCount++;
				}
				catch(Exception ex)
				{
					invalidCount++;
				}
			}
		}
		System.out.println("valid : "+validCount);
		System.out.println("invalid : "+invalidCount);
	}
	
	
	public static void separatePOSWise(String inputfile, Dictionary dictionary, String outputfolder)
	{
		try{
			Scanner sc = new Scanner(new File(inputfile));
			BufferedWriter bwNoun = new BufferedWriter(new FileWriter(new File(outputfolder+"eng-lex-sample.training.senses.noun.txt")));
			BufferedWriter bwVerb = new BufferedWriter(new FileWriter(new File(outputfolder+"eng-lex-sample.training.senses.verb.txt")));
			String line;
			while(sc.hasNextLine())
			{				
				line = sc.nextLine().toLowerCase().trim();					
				String[] lineSplit = line.split("\\s+");				
				Word word1 = dictionary.getWordBySenseKey(lineSplit[0]);
				if(word1!=null)
				{
					POS pos = word1.getPOS();				
					if(pos.equals(POS.NOUN))
					{
						bwNoun.write(line+"\n");
					}
					else if(pos.equals(POS.VERB))
					{
						bwVerb.write(line+"\n");
					}
				}
				else
				{					
					System.out.println("Error : "+ line);
				}
			}
			sc.close();
			bwNoun.close();
			bwVerb.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void writeInstances(String posSpecificSenseFile, Dictionary dictionary, HashMap<String, ArrayList<String>> versionMap, String outputFolder, String posString)
	{		
		String BinaryDataNeg = outputFolder+posString+"Negative30.txt";
		String BinaryDataPos = outputFolder+posString+"Positive30.txt";
		HashSet<String> distinctLemmas = new HashSet<String>();		
		try{
			Scanner sc = new Scanner(new File(posSpecificSenseFile));
			BufferedWriter bwPos = new BufferedWriter(new FileWriter(new File(BinaryDataPos)));
			BufferedWriter bwNeg = new BufferedWriter(new FileWriter(new File(BinaryDataNeg)));
			String line;
			String lastWord = "";
			String lastKey = "";
			HashSet<String> lastOffsets = new HashSet<String>();
			HashSet<String> mergedOffsetPairs = new HashSet<String>();
			HashSet<String> toMergeOffsets = new HashSet<String>();
			while(sc.hasNextLine())
			{								
				line = sc.nextLine().toLowerCase().trim();					
				String[] lineSplit = line.split("\\s+");				
				Word word1 = dictionary.getWordBySenseKey(lineSplit[0]);
				String offsetString1 = String.format("%08d", word1.getSynset().getOffset());
				distinctLemmas.add(word1.getLemma());
				if(! word1.getLemma().equalsIgnoreCase(lastWord)) // change of word
				{ 
					if(toMergeOffsets.size()>1)
					{
						HashSet<String> toMergeOffsets30 = new HashSet<String>();
						for(String offset : toMergeOffsets)
						{							
							ArrayList<String> mappedOffsets = versionMap.get(offset);
							if(mappedOffsets != null && mappedOffsets.size()>0)
							{									
								for(String mappedOffset : mappedOffsets)
									toMergeOffsets30.add(mappedOffset);
							}
						}
						for(String s1 : toMergeOffsets30)
							for(String s2 : toMergeOffsets30)
								if(s1.compareToIgnoreCase(s2) < 0)
								{
									mergedOffsetPairs.add(s1+"#"+s2);
									bwPos.write(posString+"#"+s1+" "+posString+"#"+s2+" 1\n");
								}
					}
					HashSet<String> lastOffsets30 = new HashSet<String>();
					for(String offset : lastOffsets)
					{
						ArrayList<String> mappedOffsets = versionMap.get(offset);
						if(mappedOffsets != null && mappedOffsets.size()>0)
						{
							for(String mappedOffset : mappedOffsets)
								lastOffsets30.add(mappedOffset);
						}
					}
					for(String offset1 : lastOffsets30)	//write the old word pairs in negative examples
					{
						for(String offset2 : lastOffsets30)
						{
							if(offset1.compareToIgnoreCase(offset2) < 0)
							{
								if(!mergedOffsetPairs.contains(offset1+"#"+offset2))
									bwNeg.write(posString+"#"+offset1+" "+posString+"#"+offset2+" -1\n");
							}
						}
					}
					//reinitializing variables
					lastOffsets = new HashSet<String>();
					mergedOffsetPairs = new HashSet<String>();
					toMergeOffsets = new HashSet<String>();
					lastWord = word1.getLemma();					
				}					
				if(lineSplit.length == 3)
				{					
					Word word2 = dictionary.getWordBySenseKey(lineSplit[2]);						
					String offsetString2 = String.format("%08d", word2.getSynset().getOffset());					 										
					if(lastKey.equalsIgnoreCase(offsetString2))
					{
						toMergeOffsets.add(offsetString1);
						toMergeOffsets.add(offsetString2);
					}				
					else
					{		
						if(toMergeOffsets.size()>1)
						{
							HashSet<String> toMergeOffsets30 = new HashSet<String>();
							for(String offset : toMergeOffsets)
							{							
								ArrayList<String> mappedOffsets = versionMap.get(offset);
								if(mappedOffsets != null && mappedOffsets.size()>0)
								{									
									for(String mappedOffset : mappedOffsets)
										toMergeOffsets30.add(mappedOffset);
								}
							}
							for(String s1 : toMergeOffsets30)
								for(String s2 : toMergeOffsets30)
									if(s1.compareToIgnoreCase(s2) < 0)
									{
										mergedOffsetPairs.add(s1+"#"+s2);
										bwPos.write(posString+"#"+s1+" "+posString+"#"+s2+" 1\n");
									}
						}
						toMergeOffsets = new HashSet<String>();
						toMergeOffsets.add(offsetString1);
						toMergeOffsets.add(offsetString2);
					}
					lastOffsets.add(offsetString1);
					lastOffsets.add(offsetString2);
					lastKey = offsetString2;
				}
				else if(lineSplit.length == 1)
				{
					lastOffsets.add(offsetString1);
					lastKey = offsetString1;
				}
				else
					System.out.println("Error : " + lineSplit.length);				
			}
			//for the last example
			if(toMergeOffsets.size()>1)
			{
				HashSet<String> toMergeOffsets30 = new HashSet<String>();
				for(String offset : toMergeOffsets)
				{
					ArrayList<String> mappedOffsets = versionMap.get(offset);
					if(mappedOffsets != null && mappedOffsets.size()>0)
					{
						for(String mappedOffset : mappedOffsets)
							toMergeOffsets30.add(mappedOffset);
					}
				}
				for(String s1 : toMergeOffsets30)
					for(String s2 : toMergeOffsets30)
						if(s1.compareToIgnoreCase(s2) < 0)
						{
							mergedOffsetPairs.add(s1+"#"+s2);
							bwPos.write(posString+"#"+s1+" "+posString+"#"+s2+" 1\n");
						}
			}
			if(lastOffsets.size()>1)
			{
				HashSet<String> lastOffsets30 = new HashSet<String>();
				for(String offset : lastOffsets)
				{
					ArrayList<String> mappedOffsets = versionMap.get(offset);
					if(mappedOffsets != null && mappedOffsets.size()>0)
					{
						for(String mappedOffset : mappedOffsets)
							lastOffsets30.add(mappedOffset);
					}
				}
				for(String offset1 : lastOffsets30)	//write the old word pairs in negative examples
				{
					for(String offset2 : lastOffsets30)
					{
						if(offset1.compareToIgnoreCase(offset2) < 0)
						{
							if(!mergedOffsetPairs.contains(offset1+"#"+offset2))
								bwNeg.write(posString+"#"+offset1+" "+posString+"#"+offset2+" -1\n");
						}
					}
				}
			}
			sc.close();
			bwPos.close();
			bwNeg.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}	
		System.out.println("Distinct lemmas encountered : "+distinctLemmas.size());
	}
	
	public static void main(String[] args) {
		String propsFile17 = "resources/file_properties_1_7.xml";
		String propsFile30 = "resources/file_properties.xml";
		String versionMapPathNoun = "/home/sumitb/Data/SenseMappings/mappings-upc-2007/mapping-17-30/wn17-30.noun";
		String versionMapPathVerb= "/home/sumitb/Data/SenseMappings/mappings-upc-2007/mapping-17-30/wn17-30.verb";
		String coarseSenses = "/home/sumitb/workspace/KRSystem/KRSystem/resources/Clustering/SensevalBinaryClassificationData/eng-lex-sample.training.senses";
		String outputFolder = "/home/sumitb/workspace/KRSystem/KRSystem/resources/Clustering/SensevalBinaryClassificationData/";
		String coarseSensesNoun = coarseSenses+".noun";
		String coarseSensesVerb = coarseSenses+".verb";
		HashMap<String, ArrayList<String>> versionMappingNoun = UPCMappings.loadVersionMapping(versionMapPathNoun);
//		System.out.println(versionMappingNoun.size());
//		HashMap<String, ArrayList<String>> versionMappingVerb = loadVersionMapping(versionMapPathVerb);
		try{			
			JWNL.initialize(new FileInputStream(propsFile17));
			Dictionary dictionary = Dictionary.getInstance();
//			mappingSanityCheck30(versionMappingNoun, dictionary, POS.NOUN);
//			separatePOSWise(coarseSenses, dictionary, outputFolder);
			writeInstances(coarseSensesNoun, dictionary, versionMappingNoun, outputFolder, "n");
//			writeInstances(coarseSensesVerb, dictionary, versionMappingVerb, outputFolder, "v");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}

	}

}
