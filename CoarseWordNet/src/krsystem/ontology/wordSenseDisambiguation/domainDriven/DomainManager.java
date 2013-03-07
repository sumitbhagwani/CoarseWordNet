package krsystem.ontology.wordSenseDisambiguation.domainDriven;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;

public class DomainManager {
	Dictionary dictionary;
	HashMap<SimpleSynset, ArrayList<SimpleSynset>> versionMap01;//2.0 to 2.1
	HashMap<SimpleSynset, ArrayList<SimpleSynset>> versionMap23;//2.1 to 3.0
	HashMap<SimpleSynset, ArrayList<SimpleSynset>> versionMapFinal;//2.0 to 3.0
	HashMap<Synset, HashSet<String>> senseDomainMap;
	HashMap<String, ArrayList<String>> synDomainMap;
	
	public DomainManager(Dictionary dictionaryPassed)
	{
		dictionary = dictionaryPassed;
		versionMap01 = new HashMap<SimpleSynset, ArrayList<SimpleSynset>>();
		versionMap23 = new HashMap<SimpleSynset, ArrayList<SimpleSynset>>();
		versionMapFinal = new HashMap<SimpleSynset, ArrayList<SimpleSynset>>();
		senseDomainMap = new HashMap<Synset, HashSet<String>>();
		synDomainMap = new HashMap<String, ArrayList<String>>();
	}
	
	public void loadVersionMapMono01(String versionMappingPath, String pos)
	{
		File file = new File(versionMappingPath);		 
        try { 
            Scanner scanner = new Scanner(file); 
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] lineSplit = line.split("[%\\s]+");
                if(lineSplit.length == 6)
                {
                	SimpleSynset v2 = new SimpleSynset(pos, lineSplit[0], lineSplit[1], lineSplit[2], 2.0);
                	SimpleSynset v3 = new SimpleSynset(pos, lineSplit[3], lineSplit[4], lineSplit[5], 2.1);
                	if(versionMap01.get(v2) == null)
                	{
                		ArrayList<SimpleSynset> toPass = new ArrayList<SimpleSynset>();
                		toPass.add(v3);
                		versionMap01.put(v2, toPass);
                	}
                	else
                	{
                		versionMap01.get(v2).add(v3);
                	}
                }
                else
                	System.out.println(lineSplit.length);
            }            
            scanner.close();
            System.out.println("Number of Mappings across versions loaded :"+versionMap01.size());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
	}
	
	public void loadVersionMapMono23(String versionMappingPath, String pos)
	{
		File file = new File(versionMappingPath);		 
        try { 
            Scanner scanner = new Scanner(file); 
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] lineSplit = line.split("[%\\s]+");
                if(lineSplit.length == 6)
                {
                	SimpleSynset v2 = new SimpleSynset(pos, lineSplit[0], lineSplit[1], lineSplit[2], 2.1);
                	SimpleSynset v3 = new SimpleSynset(pos, lineSplit[3], lineSplit[4], lineSplit[5], 3.0);
                	if(versionMap23.get(v2) == null)
                	{
                		ArrayList<SimpleSynset> toPass = new ArrayList<SimpleSynset>();
                		toPass.add(v3);
                		versionMap23.put(v2, toPass);
                	}
                	else
                	{
                		versionMap23.get(v2).add(v3);
                	}
                }
                else
                	System.out.println(lineSplit.length);
            }            
            scanner.close();
            System.out.println("Number of Mappings across versions loaded :"+versionMap23.size());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
	}

	public void loadVersionMapPoly01(String versionMappingPath, String pos)
	{
		File file = new File(versionMappingPath);		 
        try { 
            Scanner scanner = new Scanner(file); 
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] lineSplit = line.split("\\s+");                
                int score = Integer.parseInt(lineSplit[0]);
                if(score<50) break;
                String[] v2split = lineSplit[1].split("[;%\\s]+");
            	SimpleSynset v2 = new SimpleSynset(pos, v2split[0], v2split[1], v2split[2], 2.0, Integer.parseInt(v2split[3]));
            	if(versionMap01.get(v2)==null)
            		versionMap01.put(v2, new ArrayList<SimpleSynset>());
                for(int i=2; i<lineSplit.length; i++)
                {
                	String[] v3split = lineSplit[i].split("[;%\\s]+");                	
                	SimpleSynset v3 = new SimpleSynset(pos, v3split[0], v3split[1], v3split[2], 2.1, Integer.parseInt(v3split[3]));
                	versionMap01.get(v2).add(v3);
                }                
            }
            scanner.close();
            System.out.println("Number of Mappings across versions loaded :"+versionMap01.size());            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
	}		
	
	public void loadVersionMapPoly23(String versionMappingPath, String pos)
	{
		File file = new File(versionMappingPath);		 
        try { 
            Scanner scanner = new Scanner(file); 
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] lineSplit = line.split("\\s+");                
                int score = Integer.parseInt(lineSplit[0]);
                if(score<50) break;
                String[] v2split = lineSplit[1].split("[;%\\s]+");
            	SimpleSynset v2 = new SimpleSynset(pos, v2split[0], v2split[1], v2split[2], 2.1, Integer.parseInt(v2split[3]));
            	if(versionMap23.get(v2)==null)
            		versionMap23.put(v2, new ArrayList<SimpleSynset>());
                for(int i=2; i<lineSplit.length; i++)
                {
                	String[] v3split = lineSplit[i].split("[;%\\s]+");                	
                	SimpleSynset v3 = new SimpleSynset(pos, v3split[0], v3split[1], v3split[2], 3.0, Integer.parseInt(v3split[3]));
                	versionMap23.get(v2).add(v3);
                }                
            }
            scanner.close();
            System.out.println("Number of Mappings across versions loaded :"+versionMap23.size());            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
	}	
	
	public void loadDomainMap(String domainMappingPath)
	{
		File file = new File(domainMappingPath);		 
        try { 
            Scanner scanner = new Scanner(file); 
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] lineSplit = line.split("\\s+");
                if(lineSplit.length<=1) continue;                
                String init = lineSplit[0];                
                if(!init.endsWith("-v") && !init.endsWith("-n")) continue;                
                ArrayList<String> domains = new ArrayList<String>();
                synDomainMap.put(init, domains);
                for(int i=1; i<lineSplit.length; i++)
                {
                	domains.add(lineSplit[i]);                	
                }                                
            }
            scanner.close();
            System.out.println("Number of Synset to Domain mapping : "+synDomainMap.size());            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
	}
	
	public void createFinalMap()
	{
		long notFound = 0;
		for(Map.Entry<SimpleSynset, ArrayList<SimpleSynset>> entry : versionMap01.entrySet())
		{
			SimpleSynset key = entry.getKey();
			HashSet<SimpleSynset> validMappings = new HashSet<SimpleSynset>();
			for(SimpleSynset syn : entry.getValue())
			{
				ArrayList<SimpleSynset> syns = versionMap23.get(syn);
				if(syns==null) 
				{
					notFound++;
					//System.out.println("NOT FOUND in 2.1 to 3.0 : "+syn );
					continue;
				}
				validMappings.addAll(syns);				
			}
			if(validMappings.size() > 0) 
				versionMapFinal.put(key, new ArrayList<SimpleSynset>(validMappings));
		}
		System.out.println("Final Version Map has size = "+versionMapFinal.size()+" and notFound = "+notFound);
	}
	
	public void createDomainMap()
	{
		long count = 0;
		long notFound = 0;
		for(Map.Entry<SimpleSynset, ArrayList<SimpleSynset>> entry : versionMapFinal.entrySet())
		{
			String domainQuery = entry.getKey().offset+"-"+entry.getKey().pos;
			//System.out.println(domainQuery);
			ArrayList<String> domains = synDomainMap.get(domainQuery);
			if(domains != null)
			{
				//System.out.println(domainQuery+" "+domains.size());
				for(SimpleSynset v3 : entry.getValue())
				{
					POS sensePOS = v3.getPOS();
					long senseOffset = Long.parseLong(v3.offset);				
					//System.out.println("FINDING : "+v3.word +" "+v3.pos +" "+ sensePOS +" "+senseOffset );
					try {
						Synset currSynset = dictionary.getSynsetAt(sensePOS, senseOffset);
						if(currSynset!=null)
						{
							if(senseDomainMap.get(currSynset)==null)
							{
								HashSet<String> domainLabels = new HashSet<String>();
								senseDomainMap.put(currSynset, domainLabels);
							}
							
							senseDomainMap.get(currSynset).addAll(domains);
							count++;
							//System.out.println(currSynset.getWords()+ " ----> "+domains);
						}
						else
						{
							notFound++;
							//System.out.println("NOT FOUND : "+v3.word +" "+v3.pos);
						}
					} catch (Exception e) {															
						System.out.println("Err : "+sensePOS +" "+ senseOffset);
						e.printStackTrace();
						System.exit(-1);
					}				
				}
			}
//			else
//				System.out.println(domainQuery);
		}
		System.out.println("Created domain map with count = "+senseDomainMap.size() +" and notFound = "+notFound);
	}
	
	public void init(String domainMappingPath, String versionMappingPathMono01, String versionMappingPathPoly01,
			String versionMappingPathMono23, String versionMappingPathPoly23, String pos)
	{
		loadDomainMap(domainMappingPath);
		loadVersionMapMono01(versionMappingPathMono01, pos);
		loadVersionMapPoly01(versionMappingPathPoly01, pos);			
		loadVersionMapMono23(versionMappingPathMono23, pos);
		loadVersionMapPoly23(versionMappingPathPoly23, pos);
		createFinalMap();
		createDomainMap();
	}
	
	public HashSet<String> getLabels(Synset syn)
	{
		return senseDomainMap.get(syn);
	}
	
	public static void writeBasicSenseMap01(DomainManager dg, String filePath)
	{
		try {			
			File file = new File(filePath);			
			if (!file.exists()) file.createNewFile();			
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for(Map.Entry<SimpleSynset, ArrayList<SimpleSynset>> entry : dg.versionMap01.entrySet())
			{
				
				bw.write(entry.getKey().offset +" ");
				HashSet<String> offsets = new HashSet<String>();						
				for(SimpleSynset syn : entry.getValue())
				{
					offsets.add(syn.offset);
				}				
				for(String offset : offsets)
				{
					bw.write(offset+ " ");
				}
				bw.write("\n");
			}
			bw.close();
			System.out.println("Sense Map 01 writing Done");
 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void writeBasicSenseMap23(DomainManager dg, String filePath)
	{
		try {			
			File file = new File(filePath);			
			if (!file.exists()) file.createNewFile();			
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for(Map.Entry<SimpleSynset, ArrayList<SimpleSynset>> entry : dg.versionMap23.entrySet())
			{
				
				bw.write(entry.getKey().offset +" ");
				HashSet<String> offsets = new HashSet<String>();						
				for(SimpleSynset syn : entry.getValue())
				{
					offsets.add(syn.offset);
				}				
				for(String offset : offsets)
				{
					bw.write(offset+ " ");
				}
				bw.write("\n");
			}
			bw.close();
			System.out.println("Sense Map 23 writing Done");
 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void writeFinalSenseMap(DomainManager dg, String filePath)
	{
		try {			
			File file = new File(filePath);			
			if (!file.exists()) file.createNewFile();			
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for(Map.Entry<SimpleSynset, ArrayList<SimpleSynset>> entry : dg.versionMapFinal.entrySet())
			{
				
				bw.write(entry.getKey().offset +" ");
				HashSet<String> offsets = new HashSet<String>();						
				for(SimpleSynset syn : entry.getValue())
				{
					offsets.add(syn.offset);
				}				
				for(String offset : offsets)
				{
					bw.write(offset+ " ");
				}
				bw.write("\n");
			}
			bw.close();
			System.out.println("Final Sense Map writing Done");
 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void writeDomainMap(DomainManager dg, String filePath)
	{
		try {			
			File file = new File(filePath);			
			if (!file.exists()) file.createNewFile();			
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for(Map.Entry<Synset, HashSet<String>> entry : dg.senseDomainMap.entrySet())
			{
				String pos = entry.getKey().getPOS().equals(POS.NOUN) ? "n" : "v";
				bw.write(entry.getKey().getOffset() + "-" + pos);
				for(String domain : entry.getValue())
					bw.write(" "+domain);
				bw.write("\n");
			}
			bw.close();
			System.out.println("Sense Domain Map writing Done");
 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	public static HashMap<String, ArrayList<String>> loadVersionMap(String pathForMapping)
	{
		HashMap<String, ArrayList<String>> finalVersionMap = new HashMap<String, ArrayList<String>>();
		try{
			File file= new File(pathForMapping);
			Scanner scan= new Scanner(file);
			while(scan.hasNextLine())
			{
				String line = scan.nextLine();
				String[] lineSplit = line.split("\\s+");
				if(lineSplit.length <= 1)
				{
					System.out.println("Error in reading senseMap : "+ pathForMapping);
				}
				else
				{
					String key = lineSplit[0];
					ArrayList<String> values = new ArrayList<String>();					
					for(int i=1; i<lineSplit.length;i++)
						values.add(lineSplit[i]);		
					finalVersionMap.put(key, values);
				}
			}
			scan.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
		return finalVersionMap;		
		
	}
	
	public static HashMap<String, ArrayList<String>> loadVersionMaps(String pathForNounMapping, String pathForVerbMapping)
	{
		HashMap<String, ArrayList<String>> finalVersionMap = new HashMap<String, ArrayList<String>>();
		try{
			File fileNoun = new File(pathForNounMapping);
			File fileVerb = new File(pathForVerbMapping);
			Scanner scanNoun = new Scanner(fileNoun);
			Scanner scanVerb = new Scanner(fileVerb);
			while(scanNoun.hasNextLine())
			{
				String line = scanNoun.nextLine();
				String[] lineSplit = line.split("\\s+");
				if(lineSplit.length <= 1)
				{
					System.out.println("Error in reading senseMapNoun");
				}
				else
				{
					String key = lineSplit[0];
					ArrayList<String> values = new ArrayList<String>();					
					for(int i=1; i<lineSplit.length;i++)
						values.add(lineSplit[i]);		
					finalVersionMap.put(key, values);
				}
			}
			
			while(scanVerb.hasNextLine())
			{
				String line = scanVerb.nextLine();
				String[] lineSplit = line.split("\\s+");
				if(lineSplit.length <= 1)
				{
					System.out.println("Error in reading senseMapVerb");
				}
				else
				{
					String key = lineSplit[0];
					ArrayList<String> values = new ArrayList<String>();					
					for(int i=1; i<lineSplit.length;i++)
						values.add(lineSplit[i]);
					finalVersionMap.put(key, values);
				}
			}	
			
			scanNoun.close();
			scanVerb.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
		return finalVersionMap;		
	}
	
	public static void analysis(DomainManager dg, String outputFilePath, String annotatedFile)
	{		
		try {					
	        Scanner scanner = new Scanner(new File(annotatedFile));	 	       
			File file = new File(outputFilePath);			
			if (!file.exists()) file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			POS lemmaPOS = POS.NOUN;									 	         	            	            
            while (scanner.hasNextLine()) 
			{
                String line = scanner.nextLine();
                String lemma = "";
                if(line.startsWith("###Lemma:"))
                {
                	String[] splitLine = line.split("\\s+");
                	lemma = splitLine[1];
                }
                else
                	continue;
				bw.write("#############################################\n");
				bw.write("Lemma : "+lemma+"\n");		
				IndexWord iw = dg.dictionary.lookupIndexWord(lemmaPOS, lemma);
				if(iw!=null)
				{
					List<Synset> lemmaSenses = iw.getSenses();
					for(Synset synset : lemmaSenses)
					{
						bw.write("--------------------------------------------\n");
						bw.write(synset+"\n LABELS : ");
						HashSet<String> labels = dg.getLabels(synset);
						if(labels != null)
							for(String label : labels)
							{
								bw.write(label +" ");
							}
						bw.write("\n--------------------------------------------\n");					
					}
				}
				else
					System.out.println("NOT FOUND : "+lemma + " "+lemmaPOS);
				bw.write("#############################################\n");
			}
			bw.close();
			scanner.close();
			System.out.println("Analysis writing Done");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}
	
	//External Call to load the final synset to domain labels mapping
	public static HashMap<String, ArrayList<String>> loadDomainMappingNoun(String filePath)
	{
		File file = new File(filePath);
		HashMap<String, ArrayList<String>> mapping = new HashMap<String, ArrayList<String>>();
		try {
			Scanner sc = new Scanner(file);
			String line = "";
			while(sc.hasNextLine())
			{
				line = sc.nextLine();
				String[] lineSplit = line.split("\\s+");
				if(lineSplit.length >= 1)
				{
					String syn = lineSplit[0];
					if(!mapping.containsKey(syn))
						mapping.put(syn, new ArrayList<String>());					
					for(int i=1; i<lineSplit.length;i++)
					{
						mapping.get(syn).add(lineSplit[i]);
					}
				}
			}
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}
		return mapping;
	}
	
	public static void nounExperiment(Dictionary dictionary)
	{
		String versionMappingPathMono01 = "resources/sensemap01/2.0to2.1.noun.mono";
		String versionMappingPathPoly01 = "resources/sensemap01/2.0to2.1.noun.poly";		
		String versionMappingPathMono23 = "resources/sensemap23/2.1to3.0.noun.mono";
		String versionMappingPathPoly23 = "resources/sensemap23/2.1to3.0.noun.poly";
		String domainMappingPath  = "resources/wn-domains-3.2/wn-domains-3.2-20070223";
		String outputFilePath = "resources/DomainExperiment/domainLabelsNouns.txt";
		String analysisFilePath = "resources/DomainExperiment/domainLabelAnalysisNoun.txt";
		String finalMapPath = "resources/DomainExperiment/wordnet2.0to3.0MapNoun.txt";
		String annotatedFile = "resources/Evaluation/WSD/ValidationSet.txt";
		String mapPath23 = "resources/DomainExperiment/map23Noun.txt";
		
		DomainManager dg = new DomainManager(dictionary);			
		dg.init(domainMappingPath, versionMappingPathMono01, versionMappingPathPoly01,
				versionMappingPathMono23, versionMappingPathPoly23, "n");
//		writeDomainMap(dg, outputFilePath);
		writeFinalSenseMap(dg, finalMapPath);
		writeBasicSenseMap23(dg,mapPath23);
		//analysis(dg, analysisFilePath, annotatedFile);
	}
	
	public static void verbExperiment(Dictionary dictionary)
	{
		String versionMappingPathMono01 = "resources/sensemap01/2.0to2.1.verb.mono";
		String versionMappingPathPoly01 = "resources/sensemap01/2.0to2.1.verb.poly";		
		String versionMappingPathMono23 = "resources/sensemap23/2.1to3.0.verb.mono";
		String versionMappingPathPoly23 = "resources/sensemap23/2.1to3.0.verb.poly";
		String domainMappingPath  = "resources/wn-domains-3.2/wn-domains-3.2-20070223";
		String outputFilePath = "resources/DomainExperiment/domainLabelsVerbs.txt";
		String analysisFilePath = "resources/DomainExperiment/domainLabelAnalysisVerb.txt";
		String finalMapPath = "resources/DomainExperiment/wordnet2.0to3.0MapVerb.txt";
		String annotatedFile = "resources/Evaluation/WSD/ValidationSet.txt";
		String mapPath23 = "resources/DomainExperiment/map23Verb.txt";
		
		DomainManager dg = new DomainManager(dictionary);			
		dg.init(domainMappingPath, versionMappingPathMono01, versionMappingPathPoly01,
				versionMappingPathMono23, versionMappingPathPoly23, "v");
//		writeDomainMap(dg, outputFilePath);
		writeFinalSenseMap(dg, finalMapPath);
		writeBasicSenseMap23(dg,mapPath23);
		//analysis(dg, analysisFilePath, annotatedFile);
	}	
	
	public static void main(String[] args) {
		
		String propsFile = "resources/file_properties.xml";
		
		try{			
			JWNL.initialize(new FileInputStream(propsFile));
			Dictionary dictionary = Dictionary.getInstance();
//			nounExperiment(dictionary);
//			verbExperiment(dictionary);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}

}
