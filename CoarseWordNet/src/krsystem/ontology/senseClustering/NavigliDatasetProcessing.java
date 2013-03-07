package krsystem.ontology.senseClustering;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import javax.sound.midi.SysexMessage;

import krsystem.ontology.wordSenseDisambiguation.domainDriven.DomainManager;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

public class NavigliDatasetProcessing {
	
	public static void preprocess(String filePath, String outputFile, Dictionary dict, boolean toWrite) {
		long pairCount = 0;
		int numNouns = 0;
		int numVerbs = 0;
		double nounCount = 0; 
		double verbCount = 0;
		double nounFineSum = 0;
		double verbFineSum = 0;
		double nounCoarseSum = 0;// summation f_i * COARSEdeg_i
		double verbCoarseSum = 0;		
		String line = "";
		
		if(dict != null)
		{			
			try 
			{
				Scanner sc = new Scanner(new File(filePath));
				BufferedWriter bw = null;
				if(toWrite)
				{
					bw = new BufferedWriter(new FileWriter(outputFile));
				}
				String lastWordString = ""; 
				String lastPOSString = ""; // would be a number
				int success = 0;
				int failure = 0;
				double numClusters = 0;
				double numSenses = 0;				
				double freq = 0;
				double totalCount = 0;				
				while(sc.hasNextLine())
				{
					line = sc.nextLine().toLowerCase().trim();					
					String[] senseKeys = line.split("\\s+");
					String wordString = senseKeys[0].split("%")[0];
					String posString = senseKeys[0].split("%")[1].split(":")[0];
					int sensesNum = senseKeys.length;
					
					pairCount += sensesNum*(sensesNum-1)/2;
					boolean wrote = false;
					for(String senseKey : senseKeys)
					{						
						Word word = dict.getWordBySenseKey(senseKey.toLowerCase());
						if(word == null)
						{
							failure ++;
						}
						else
						{
							if(toWrite)
							{
								String offsetString = String.format("%08d", word.getSynset().getOffset()); 
								bw.write(offsetString+" ");
							}
							wrote = true;
							freq += word.getUseCount();
							success++;							
						}						
					}
					if(wrote && toWrite)
						bw.write("\n");										
					
					if(!wordString.equalsIgnoreCase(lastWordString) 
							|| !posString.equalsIgnoreCase(lastPOSString))
					{
						if(!lastWordString.equals(""))
						{
							if(lastPOSString.equals("1")) // NOUN
							{
								nounCoarseSum += numClusters*totalCount;
								nounFineSum += numSenses*totalCount;
								nounCount += totalCount;
								numNouns++;
							}
							else if(lastPOSString.equals("2")) // VERB
							{
								verbCoarseSum += numClusters*totalCount;
								verbFineSum += numSenses*totalCount;
								verbCount += totalCount;
								numVerbs++;
							}
						}
																						
						numClusters = 1;
						numSenses = senseKeys.length;
						lastWordString = wordString;
						lastPOSString = posString;
						totalCount = freq;
					}							
					else 
					{
						numClusters += 1;
						numSenses += senseKeys.length;	
						totalCount += freq;
					}																											
				}
				
				System.out.println("Average Noun Polysemy in Fine Senses "+ nounFineSum/nounCount);
				System.out.println("Average Noun Polysemy in Coarse Senses "+ nounCoarseSum/nounCount);
				System.out.println("Average Verb Polysemy in Fine Senses "+ verbFineSum/verbCount);
				System.out.println("Average Verb Polysemy in Coarse Senses "+ verbCoarseSum/verbCount);
				System.out.println("Nouns "+numNouns);
				System.out.println("Verbs "+numVerbs);
				
				System.out.println("success : "+success);
				System.out.println("failure : "+failure);
				
				System.out.println("Pair count : "+pairCount);
				
				sc.close();
				if(toWrite)
					bw.close();
			} catch (Exception e) {
				System.out.println(line);
				e.printStackTrace();
			}			
		}
	}

	public static void migrate(String clusters21, String clusters30, String mapPath)
	{
		HashMap<String, ArrayList<String>> mapping = DomainManager.loadVersionMap(mapPath);		
		try{
			Scanner sc = new Scanner(new File(clusters21));
			BufferedWriter bw = new BufferedWriter(new FileWriter(clusters30));
			while(sc.hasNextLine())
			{				
				String line = sc.nextLine();
				String[] lineSplit = line.split("\\s+");
				if(lineSplit.length > 1)
				{
					HashSet<String> offsets30 = new HashSet<String>();					
					for(String offsetString : lineSplit)
					{
						List<String> offset30List = mapping.get(offsetString);
						if(offset30List != null)
						{
							for(String offset30 : offset30List)
							{
								offsets30.add(offset30);
							}
						}						
					}
					if(offsets30.size() > 0)
					{
						for(String offset : offsets30)
						{
							bw.write(offset+" ");
						}
						bw.write("\n");
					}
				}
			}			
			sc.close();
			bw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void prepareMergeData(String clusterOffsets, String mergedPairsData)
	{
		int pairCount = 0;
		try{
			Scanner sc = new Scanner(new File(clusterOffsets));
			BufferedWriter bw = new BufferedWriter(new FileWriter(mergedPairsData));
			
			while(sc.hasNextLine())
			{
				String line = sc.nextLine();
				String[] lineSplit = line.split("\\s+");
				for(String offset1 : lineSplit)
					for(String offset2 : lineSplit)
						if(offset1.compareToIgnoreCase(offset2) < 0)
						{
							bw.write(offset1+"#"+offset2+"\n");
							pairCount ++;
						}
			}
			System.out.println(pairCount);
			sc.close();
			bw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void main(String[] args) 
	{
		String filePath = "/home/sumitb/Data/navigli_sense_inventory/sense_clusters-21.senses.caselessSorted";
		String propsFile21 = "resources/file_properties_2_1.xml";
		String outputFile = "/home/sumitb/Data/navigli_sense_inventory/sense_clusters-21.offsets";
		String outputFile2Noun = "/home/sumitb/Data/navigli_sense_inventory/sense_clusters-30.offsets.noun";
		String outputFile2Verb = "/home/sumitb/Data/navigli_sense_inventory/sense_clusters-30.offsets.verb";
		String map23Noun = "/home/sumitb/Data/DomainExperiment/map23Noun.txt";
		String map23Verb = "/home/sumitb/Data/DomainExperiment/map23Verb.txt";
		String finalOutputNoun = "/home/sumitb/Data/navigli_sense_inventory/mergeData-30.offsets.noun";
		String finalOutputVerb = "/home/sumitb/Data/navigli_sense_inventory/mergeData-30.offsets.verb";
		Dictionary dict21 = OntonotesSenseReader.wnTest(propsFile21, false);
//		preprocess(filePath, outputFile, dict21, true);
//		migrate(outputFile,outputFile2Noun, map23Noun);
//		migrate(outputFile,outputFile2Verb, map23Verb);
		prepareMergeData(outputFile2Noun, finalOutputNoun);
		prepareMergeData(outputFile2Verb, finalOutputVerb);
		
	}	
}
