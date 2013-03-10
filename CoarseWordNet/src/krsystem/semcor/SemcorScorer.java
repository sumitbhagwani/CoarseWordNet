package krsystem.semcor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

import krsystem.StaticValues;

public class SemcorScorer {
	
	Dictionary dict;
	POS posReqd;
	double alpha;
	HashMap<String, Integer> senseKeyFrequency;
	HashMap<String, Integer> lemmaFrequency;
	HashMap<String, Integer> synsetFrequency;
	
	public SemcorScorer(Dictionary dictPassed, POS posPassed) {
		posReqd = posPassed;
		dict = dictPassed;
		senseKeyFrequency = new HashMap<String, Integer>();
		lemmaFrequency = new HashMap<String, Integer>();
		synsetFrequency = new HashMap<String, Integer>();
		alpha = 0;
	}
	
	public SemcorScorer(Dictionary dictPassed, POS posPassed, HashMap<String, Integer> senseKeyFrequencyPassed,
			HashMap<String, Integer> lemmaFrequencyPassed, HashMap<String, Integer> synsetFrequencyPassed) {
		posReqd = posPassed;
		dict = dictPassed;
		senseKeyFrequency = senseKeyFrequencyPassed;
		lemmaFrequency = lemmaFrequencyPassed;
		synsetFrequency = synsetFrequencyPassed;
		alpha = 0;
	}
	
	public SemcorScorer(Dictionary dictPassed, POS posPassed, double alphaPassed) {
		posReqd = posPassed;
		dict = dictPassed;
		senseKeyFrequency = new HashMap<String, Integer>();
		lemmaFrequency = new HashMap<String, Integer>();
		synsetFrequency = new HashMap<String, Integer>();
		alpha = alphaPassed;
	}
	
	public SemcorScorer(Dictionary dictPassed, POS posPassed, HashMap<String, Integer> senseKeyFrequencyPassed,
			HashMap<String, Integer> lemmaFrequencyPassed, HashMap<String, Integer> synsetFrequencyPassed,
			double alphaPassed) {
		posReqd = posPassed;
		dict = dictPassed;
		senseKeyFrequency = senseKeyFrequencyPassed;
		lemmaFrequency = lemmaFrequencyPassed;
		synsetFrequency = synsetFrequencyPassed;
		alpha = alphaPassed;
	}
	
	public void processSentence(ArrayList<String> sentenceDescription)
	{				
		for(String s : sentenceDescription)
		{
			String[] parts = s.split("[<>\\s+]");
			String word = "", lemma = "", pos = "";
			ArrayList<String> lexsnList = new ArrayList<String>();
			ArrayList<Integer> wnsnList = new ArrayList<Integer>();
			boolean valid = false;
			word = parts[parts.length-2];
			//int wnsn = -1;
			for(int index=0; index<parts.length; index++)
			{				
				if(parts[index].startsWith("lemma="))
				{
					String[] lemmaParts = parts[index].split("=");
					lemma = lemmaParts[1];
				}
				else if(parts[index].startsWith("pos="))
				{
					String[] posParts = parts[index].split("=");
					pos = posParts[1];//.substring(0,2);
				}
				else if(parts[index].startsWith("wnsn="))
				{
					String[] wnsnParts = parts[index].split("=");
					String[] numbers = wnsnParts[1].split(";");
					for(int temp=0; temp<numbers.length; temp++)
						wnsnList.add(new Integer(Integer.parseInt(numbers[temp])));
					//wnsn = Integer.parseInt(wnsnParts[1]);
				}
				else if(parts[index].startsWith("lexsn="))
				{
					String[] lexsnParts = parts[index].split("=");
					String[] numbers = lexsnParts[1].split(";");
					for(int temp=0; temp<numbers.length; temp++)
						lexsnList.add(numbers[temp]);
					//lexsn = lexsnParts[1];//.substring(0,2);
				}				
				else if(parts[index].startsWith("cmd=done"))
				{
					valid = true;//check other validity things like rdf
				}
			}						
			//valid = valid && (wnsn>0);													
			if(valid)
			{
				//System.out.println(word+"-"+ lemma +"-"+ pos + "-"+ lexsn);
				if(wnsnList.size() != lexsnList.size())
				{
					System.out.println("Error in extraction at sentence level");
					System.exit(0);
				}
				for(int temp=0; temp<wnsnList.size(); temp++)
				{
					if(wnsnList.get(temp).intValue()  >0)
					{
						SemCorSense toQuery = new SemCorSense(word, pos, lemma, lexsnList.get(temp), wnsnList.get(temp).intValue());
						String lemmaSure = toQuery.getLemma();
						String senseKeySure = toQuery.getSenseKey();
						try{
							Word wordSense = dict.getWordBySenseKey(senseKeySure);
							if(wordSense != null)
							{
								if(wordSense.getPOS().equals(posReqd))
								{
									long offset = wordSense.getSynset().getOffset();
									String offsetString = String.format("%08d", offset);
									
									Integer synsetFreq = synsetFrequency.get(offsetString);
									if(synsetFreq == null)
										synsetFrequency.put(offsetString, 1);
									else
										synsetFrequency.put(offsetString, synsetFreq.intValue()+1);
									
									Integer lemmaFreq = lemmaFrequency.get(lemmaSure);
									if(lemmaFreq == null)
										lemmaFrequency.put(lemmaSure, 1);
									else
										lemmaFrequency.put(lemmaSure, lemmaFreq.intValue()+1);
									
									Integer senseKeyFreq = senseKeyFrequency.get(senseKeySure);
									if(senseKeyFreq == null)
										senseKeyFrequency.put(senseKeySure, 1);
									else
										senseKeyFrequency.put(senseKeySure, senseKeyFreq.intValue()+1);
								}
							}
							else
							{
								System.out.println(senseKeySure + " not found");
							}
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
//							System.exit(-1);
						}
					}
				}
												
			}
		}
								
	}
	
	public void processBrownFile(String text)
	{
		boolean found = true;
		int count = 1;
		while(found)
		{
			//System.out.println(count);
		    Pattern pattern = Pattern.compile("<s snum="+count+">.*?</s>", Pattern.DOTALL);		    
		    //System.out.println(pattern);
		    Matcher matcher = pattern.matcher(text);
		    //System.out.println(matcher);		    
		    found = false;
		    while (matcher.find()) 
		    {
		    	found = true;
		         //System.out.println(count+" : something !");
		         String matchedText = matcher.group();
		         String[] matchedTextSplitted = matchedText.split("[\n]+");
		         if(matchedTextSplitted.length>2)
		        	 processSentence(new ArrayList<String>(Arrays.asList(matchedTextSplitted).subList(1, matchedTextSplitted.length-2)));
		    }
		    count++;
//		    if(count == 5) break;
		}
	}
	
	public void processSemCor(String[] dirPaths)
	{
		try{
			for(String dirPath : dirPaths)
			{
				File folder = new File(dirPath);
				for(File file : folder.listFiles())
				{
//					System.out.println(file.getAbsolutePath());
					Scanner sc = new Scanner(file);
					sc.useDelimiter("\\Z");  
					String content = sc.next();
					processBrownFile(content);
					sc.close();
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void writeLemmaFreq(String lemmaFreqPath)
	{
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(lemmaFreqPath)));
			for(Map.Entry<String, Integer> entry : lemmaFrequency.entrySet())
			{
				bw.write(entry.getKey()+" "+entry.getValue()+"\n");
			}
			bw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void writeSenseKeyFreq(String senseKeyFreqPath)
	{
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(senseKeyFreqPath)));
			for(Map.Entry<String, Integer> entry : senseKeyFrequency.entrySet())
			{
				bw.write(entry.getKey()+" "+entry.getValue()+"\n");
			}
			bw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}
		
	public void writeSynsetFreq(String synsetFreqPath)
	{
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(synsetFreqPath)));
			for(Map.Entry<String, Integer> entry : synsetFrequency.entrySet())
			{
				bw.write(entry.getKey()+" "+entry.getValue()+"\n");
			}
			bw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}
	
	public double getWordScore(Word word)
	{
		double score = 0;
		Integer senseKeyCount = senseKeyFrequency.get(word.getSenseKey().toLowerCase());
		int senseKeyFreq = senseKeyCount==null ? 0 : senseKeyCount.intValue();
		
		String lemma = word.getLemma();
		Integer lemmaCount = lemmaFrequency.get(lemma);
		int lemmaFreq = lemmaCount==null ? 0 : lemmaCount.intValue();
		
		int numSenses = 0;
		try{
			numSenses = dict.getIndexWord(posReqd, lemma).getSenses().size();
		}
		catch(Exception ex)
		{
			System.out.println("Error in scoring word : "+word);
			System.exit(-1);
		}
		
		score = (senseKeyFreq + alpha)/(lemmaFreq + alpha*numSenses); 		
		return score;
	}
	
	public double getSynsetScore(Synset syn)
	{
		double score = 0;
		for(Word word : syn.getWords())
		{
			score += getWordScore(word);
		}
		return score;
	}
		
	public static SemcorScorer getSemcorScorer(String lemmaFreqPath, String senseKeyFreqPath, String synsetFreqPath, Dictionary dictPassed, POS posPassed, double alphaPassed)
	{					
		HashMap<String, Integer> senseKeyFrequency = new HashMap<String, Integer>();
		HashMap<String, Integer> lemmaFrequency = new HashMap<String, Integer>();
		HashMap<String, Integer> synsetFrequency = new HashMap<String, Integer>();
		
		String line;
		try{
			Scanner sc = new Scanner(new File(lemmaFreqPath));
			while(sc.hasNextLine())
			{
				line = sc.nextLine();
				String[] lineSplit = line.split("\\s+");
				lemmaFrequency.put(lineSplit[0], Integer.parseInt(lineSplit[1]));
			}
			sc.close();
			
			sc = new Scanner(new File(synsetFreqPath));
			while(sc.hasNextLine())
			{
				line = sc.nextLine();
				String[] lineSplit = line.split("\\s+");
				synsetFrequency.put(lineSplit[0], Integer.parseInt(lineSplit[1]));
			}
			sc.close();
			
			sc = new Scanner(new File(senseKeyFreqPath));
			while(sc.hasNextLine())
			{
				line = sc.nextLine();
				String[] lineSplit = line.split("\\s+");
				senseKeyFrequency.put(lineSplit[0], Integer.parseInt(lineSplit[1]));
			}
			sc.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	
		return new SemcorScorer(dictPassed, posPassed, senseKeyFrequency, lemmaFrequency, senseKeyFrequency, alphaPassed);
		
	}
	
	public static void getNounScoreDistribution(double alpha, String offsetFile)
	{
		String dataPath = StaticValues.dataPath;
		
		String posString = "Noun";
		POS pos = POS.NOUN;
		String posSmall = "n";
		
		String lemmaFreqPath = dataPath+ "semcor3.0/FrequencyAnalysis/"+posString+"/lemmaFreq.txt";
		String senseKeyFreqPath = dataPath+ "semcor3.0/FrequencyAnalysis/"+posString+"/senseKeyFreq.txt";
		String synsetFreqPath = dataPath+ "semcor3.0/FrequencyAnalysis/"+posString+"/synsetFreq.txt";
		String propsFile30 = "resources/file_properties.xml";
		
		String outputFilePath = dataPath+ "semcor3.0/FrequencyAnalysis/"+posString+"/scores"+alpha+".txt";
		
		try{
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();									
			
			SemcorScorer scorer = SemcorScorer.getSemcorScorer(lemmaFreqPath, senseKeyFreqPath, synsetFreqPath, dictionary, pos, alpha);			
			System.out.println("Scorer loaded...");
			
			Scanner sc = new Scanner(new File(offsetFile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFilePath)));
			String line;
			int count = 0;
			while(sc.hasNextLine())
			{
				line = sc.nextLine();
				String[] lineSplit = line.split("-");
				if(lineSplit[1].equalsIgnoreCase(posSmall))
				{
					long offset = Long.parseLong(lineSplit[0]);
					Synset syn = dictionary.getSynsetAt(pos, offset);
					if(syn != null)
					{
						double score = scorer.getSynsetScore(syn);
						bw.write(lineSplit[0]+" "+score+"\n");
						count++;
						if(count%1000 == 0)
							System.out.println(count);
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
	
	public static void getVerbScoreDistribution(double alpha, String offsetFile)
	{
		String dataPath = StaticValues.dataPath;
		
		String posString = "Verb";
		POS pos = POS.VERB;
		String posSmall = "v";
		
		String lemmaFreqPath = dataPath+ "semcor3.0/FrequencyAnalysis/"+posString+"/lemmaFreq.txt";
		String senseKeyFreqPath = dataPath+ "semcor3.0/FrequencyAnalysis/"+posString+"/senseKeyFreq.txt";
		String synsetFreqPath = dataPath+ "semcor3.0/FrequencyAnalysis/"+posString+"/synsetFreq.txt";
		String propsFile30 = "resources/file_properties.xml";
		
		String outputFilePath = dataPath+ "semcor3.0/FrequencyAnalysis/"+posString+"/scores"+alpha+".txt";
		
		try{
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();									
			
			SemcorScorer scorer = SemcorScorer.getSemcorScorer(lemmaFreqPath, senseKeyFreqPath, synsetFreqPath, dictionary, pos, alpha);			
			System.out.println("Scorer loaded...");
			
			Scanner sc = new Scanner(new File(offsetFile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFilePath)));
			String line;
			int count = 0;
			while(sc.hasNextLine())
			{
				line = sc.nextLine();
				String[] lineSplit = line.split("-");
				if(lineSplit[1].equalsIgnoreCase(posSmall))
				{
					long offset = Long.parseLong(lineSplit[0]);
					Synset syn = dictionary.getSynsetAt(pos, offset);
					if(syn != null)
					{
						double score = scorer.getSynsetScore(syn);
						bw.write(lineSplit[0]+" "+score+"\n");
						count++;
						if(count%1000 == 0)
							System.out.println(count);
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
	
	public static void test()
	{
		String dataPath = StaticValues.dataPath;
		String brown1Path = dataPath + "semcor3.0/brown1/tagfiles/";
		String brown2Path = dataPath + "semcor3.0/brown2/tagfiles/";
		String brownvPath = dataPath + "semcor3.0/brownv/tagfiles/";
		
//		String posString = "Noun";
//		POS pos = POS.NOUN;
		
		String posString = "Verb";
		POS pos = POS.VERB;
		
		String lemmaFreqPath = dataPath+ "semcor3.0/FrequencyAnalysis/"+posString+"/lemmaFreq.txt";
		String senseKeyFreqPath = dataPath+ "semcor3.0/FrequencyAnalysis/"+posString+"/senseKeyFreq.txt";
		String synsetFreqPath = dataPath+ "semcor3.0/FrequencyAnalysis/"+posString+"/synsetFreq.txt";
		String propsFile30 = "resources/file_properties.xml";
		
		try{
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();			
//			SemcorScorer extractor = new SemcorScorer(dictionary, pos);			
//					
//			String[] brownPaths = {brown1Path, brown2Path, brownvPath};						
//			
//			extractor.processSemCor(brownPaths);
//			extractor.writeLemmaFreq(lemmaFreqPath);
//			extractor.writeSenseKeyFreq(senseKeyFreqPath);
//			extractor.writeSynsetFreq(synsetFreqPath);
//			System.out.println(dictionary.getWordBySenseKey("conscious%5:00:00:aware:00"));
			
			double alpha = 1;
			
			SemcorScorer scorer = SemcorScorer.getSemcorScorer(lemmaFreqPath, senseKeyFreqPath, synsetFreqPath, dictionary, pos, alpha);			
			IndexWord iw = dictionary.getIndexWord(pos, "head");
			System.out.println(scorer.getSynsetScore(iw.getSenses().get(0)));
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
//		test();
		String dataPath = StaticValues.dataPath;		
		String offsetFile = dataPath+"/xwnd/offsets.txt";		
//		getNounScoreDistribution(1, offsetFile);
		getVerbScoreDistribution(1, offsetFile);
	}

}
