package krsystem.ontology.senseClustering;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.SysexMessage;
import javax.sql.CommonDataSource;

import krsystem.ontology.wordSenseDisambiguation.domainDriven.DomainManager;
import krsystem.utility.OrderedPair;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.MorphologicalProcessor;

public class OntonotesSenseReader {

	public static String cleanWNString(String wnString)
	{
		if(wnString.equals("<wn version=\"3.0\"></wn>") ||
			wnString.equals("<wn version=\"2.1\"></wn>") ||
			wnString.equals("<wn version=\"2.0\"></wn>") )
		return null;
		
		StringBuilder toRet = new StringBuilder();
		String[] splitArr = wnString.substring(1).split("[<>\"//]+");
		if(splitArr.length < 4)
		{
    		System.out.println("Error 1 in extracting clean WN String : "+wnString);
    		System.exit(-1);
		}
    	else if(splitArr.length == 4)
    	{
    		double wnVersion = Double.parseDouble(splitArr[1]);
    		toRet.append(wnVersion);
    		String[] senses = splitArr[2].split("[,\\s]+");    		
    		for(String sense:senses)
    			toRet.append(" "+sense);
    	}
    	else
    	{
//    		if(!splitArr[2].equalsIgnoreCase("wn"))
    		{
    			System.out.println("Error 2 in extracting clean WN String : "+wnString);
//    			System.exit(-1);
    		}    		
    	}		
		String toReturn = toRet.toString().trim(); 
		return toReturn.length()>0? toReturn : null;
	}
	
	public static OrderedPair<String, String> readSenses(String fileDir, String fileName)
	{		
		StringBuilder toRet = new StringBuilder();
		HashSet<String> possibleVersions = new HashSet<String>();
		String word = fileName.substring(0,fileName.lastIndexOf("-"));
		String pos = fileName.substring(fileName.lastIndexOf("-")+1,fileName.lastIndexOf("."));
//		System.out.println(fileName+" "+word+" "+pos);
		try 
		{						
			String filePath = fileDir.endsWith("/")||fileName.startsWith("/") ? fileDir+fileName : fileDir+"/"+fileName;
			Scanner sc = new Scanner(new File(filePath));
			String text = sc.useDelimiter("\\Z").next(); 						
			sc.close();
			
			Pattern PATTERN_AUTO = Pattern.compile("automatically created due to .* ?reference", Pattern.DOTALL);
			Matcher autoMatch = PATTERN_AUTO.matcher(text);
			if(autoMatch.find())
			{
				return new OrderedPair<String, String>("", "0");
			}
			
			Pattern MY_PATTERN_S = Pattern.compile("<sense .*?</sense>",Pattern.DOTALL);			
			Matcher ms = MY_PATTERN_S.matcher(text);
						
			while(ms.find())
			{	
				boolean wrote = false;				
				String subtext = ms.group();
//				Pattern MY_PATTERN_1 = Pattern.compile("<wn lemma=\".+?\" version=\".*?</wn>", Pattern.DOTALL);
//				Matcher m1 = MY_PATTERN_1.matcher(subtext);
//				while (m1.find()) {
//				    String s = m1.group();
//				    s = s.replaceAll("\n", "");
//				    s = cleanWNString(s);
//				    System.out.println(s);
//				    if(s!=null)
//				    	bw0.write(s+"\n");
//				}
				
				Pattern MY_PATTERN_2 = Pattern.compile("<wn version=\"[0-9]\\.[0-9]\">[0-9 ,\n]+?</wn>", Pattern.DOTALL);
				Matcher m2 = MY_PATTERN_2.matcher(subtext);
				while (m2.find()) {
				    String s = m2.group();
				    s = s.replaceAll("\n", "");
				    s = cleanWNString(s);
//				    System.out.println(s);
				    if(s!=null && s.length()>0)
				    {
				    	String[] sSplit = s.split("\\s+");
				    	String possVersion = sSplit[0];
				    	possibleVersions.add(possVersion);
				    	s = s.substring(possVersion.length()+1); // remove the version from string
				    	toRet.append(s+"\n");
				    	wrote = true;
				    }
				}				
				if(wrote)
					toRet.append("\n");//separating senses
			}										
		} 
		catch (Exception e)  
		{			
			e.printStackTrace();
		}	
		String toReturn = toRet.toString();								
		if(possibleVersions.size()==1)
		{
			String possibleVersion="";
			for(String version : possibleVersions)
				possibleVersion = version;
			return new OrderedPair<String, String>(toReturn, possibleVersion);
		}
		else if(possibleVersions.size()==0)
		{
			System.out.println("No senses found for word : "+word+" and pos : "+pos);
		}
		else
		{
			System.out.println("Error(Multiple Versions) in sense extraction for word : "+word+" and pos : "+pos+" with versions : ");
			for(String version : possibleVersions)
				System.out.println(version +" ");
			System.exit(-1);
		}
		return null;
	}	
	
	public static void processSenseClusters(String senseFolder, String outputFolder, Dictionary dict)
	{
		try{			
			File folder = new File(senseFolder);
			File[] listOfFiles = folder.listFiles();
			System.out.println(listOfFiles.length);
			int count = 0;
			for(File file: listOfFiles)
			{
				String absolutePath = file.getAbsolutePath();
				String filePath = absolutePath.substring(0,absolutePath.lastIndexOf("/"));
				String fileName = absolutePath.substring(absolutePath.lastIndexOf("/")+1, absolutePath.length());
				String word = fileName.substring(0,fileName.lastIndexOf("-"));
				String pos = fileName.substring(fileName.lastIndexOf("-")+1,fileName.lastIndexOf("."));
				POS posReqd = pos.equalsIgnoreCase("n") ? POS.NOUN : POS.VERB ;
				if(!fileName.endsWith("~"))
				{
					IndexWord iw = dict.getIndexWord(posReqd, word); 
					if(iw!=null && iw.getSenses().size()==1) // monosemous in 3.0
					{
						File fileOut = new File(outputFolder+"3.0/"+fileName);
						fileOut.getParentFile().mkdirs();
						if (!fileOut.exists()) 
							fileOut.createNewFile();
						FileWriter fw = new FileWriter(fileOut);
						fw.write("1\n");
						fw.close();
						count++;
					}
					else
					{
						OrderedPair<String, String> descVersionPair = readSenses(filePath, fileName);	
						if(descVersionPair != null)
						{
							String version = descVersionPair.getR();
							File fileOut = new File(outputFolder+version+"/"+fileName);
							fileOut.getParentFile().mkdirs();
							if (!fileOut.exists()) 
								fileOut.createNewFile();
							FileWriter fw = new FileWriter(fileOut);
							fw.write(descVersionPair.getL());
							fw.close();
							
							if(!version.equals("0"))
								count++;
						}			
						
					}
	//				if(count>=20)
	//					break;					
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}	
	
//	public static void generateWn2_0to3_0SenseClusters(String pathForNounMapping, String pathForVerbMapping, String senses2_0FilesPath, String outputPath, Dictionary dict)
//	{		
//		HashMap<String, ArrayList<String>> offsetMapping = DomainManager.loadVersionMapsFinal(pathForNounMapping, pathForVerbMapping);
//		File files2_0Folder = new File(senses2_0FilesPath);
//		File[] listOfFiles = files2_0Folder.listFiles();
//		System.out.println(listOfFiles.length);
//		
//		for(File file: listOfFiles)
//		{
//			String absolutePath = file.getAbsolutePath();
//			String filePath = absolutePath.substring(0,absolutePath.lastIndexOf("/"));
//			String fileName = absolutePath.substring(absolutePath.lastIndexOf("/")+1, absolutePath.length());
//			String word = fileName.substring(0,fileName.lastIndexOf("-"));
//			String pos = fileName.substring(fileName.lastIndexOf("-")+1,fileName.lastIndexOf("."));
//			POS posReqd = pos.equalsIgnoreCase("n") ? POS.NOUN : POS.VERB ;
//			try
//			{
//				String outputFilePath = outputPath.endsWith("/") ? outputPath + fileName : outputPath +"/"+fileName ; 
//				File fileOut = new File(outputFilePath);
//				fileOut.getParentFile().mkdirs();
//				if (!fileOut.exists()) 
//					fileOut.createNewFile();
//				FileWriter fwOut = new FileWriter(fileOut.getAbsoluteFile());
//				BufferedWriter bwOut = new BufferedWriter(fwOut);				
//				IndexWord iw = dict.getIndexWord(posReqd, word);
//				List<Synset> senses = iw.getSenses();
//				
//				Scanner sc = new Scanner(file);
//				while(sc.hasNextLine())
//				{
//					String line = sc.nextLine().trim();
//					if(line.length()>0)
//					{
//						String[] senseNumbers = line.split("\\s+");
//						boolean wrote = false;
//						for(String s : senseNumbers)
//						{
//							int index = Integer.parseInt(s) - 1;
//							String offset = Long.toString(senses.get(index).getOffset());
//							ArrayList<String> offsetsFinal = offsetMapping.get(offset);
//							if(offsetsFinal!=null && offsetsFinal.size()>0)
//								for(String offsetFinal : offsetsFinal)
//								{
//									bwOut.write(offsetFinal+" ");
//									wrote = true;
//								}
//							else
//							{
//								System.out.println("No offset matched for "+word+" at offset "+offset);
//							}
//						}
//						if(wrote)
//							bwOut.write("\n");
//					}
//				}
//				bwOut.close();
//				fwOut.close();
//			}
//			catch(Exception ex)
//			{
//				ex.printStackTrace();				
//			}
//			
//		}
//	}
	
	public static void generateWn3_0SenseClusters(String senseFilesPath, String outputPath, Dictionary dict)
	{		
		File filesFolder = new File(senseFilesPath);
		File[] listOfFiles = filesFolder.listFiles();
		System.out.println(listOfFiles.length);
		
		for(File file: listOfFiles)
		{
			String absolutePath = file.getAbsolutePath();
			String filePath = absolutePath.substring(0,absolutePath.lastIndexOf("/"));
			String fileName = absolutePath.substring(absolutePath.lastIndexOf("/")+1, absolutePath.length());
			String word = fileName.substring(0,fileName.lastIndexOf("-"));
			String pos = fileName.substring(fileName.lastIndexOf("-")+1,fileName.lastIndexOf("."));
			POS posReqd = pos.equalsIgnoreCase("n") ? POS.NOUN : POS.VERB ;
//			System.out.println(word+" "+pos);
			
			try
			{
				boolean noError = true;								
				IndexWord iw = dict.lookupIndexWord(posReqd, word);
				List<Synset> senses = iw.getSenses();
				StringBuilder toWrite = new StringBuilder();
				
				Scanner sc = new Scanner(file);
				while(sc.hasNextLine())
				{
					String line = sc.nextLine().trim();
					if(line.length()>0)
					{
						String[] senseNumbers = line.split("\\s+");
						boolean wrote = false;
						for(String s : senseNumbers)
						{
							int index = Integer.parseInt(s) - 1;
							long offsetLong = senses.get(index).getOffset();
//							String offset = Long.toString(offsetLong);
							String offset = String.format("%08d", offsetLong);

							toWrite.append(offset+" ");
							wrote = true;
						}
						if(wrote)
						{
							toWrite.append("\n");
//							bwOut.write("\n");
						}
					}
				}
				
				if(!outputPath.endsWith("/"))
					outputPath = outputPath +"/";
				String outputFilePath;
				if(noError)
					outputFilePath = outputPath +"auto/"+ fileName;
				else
					outputFilePath = outputPath +"manual/"+ fileName;
				File fileOut = new File(outputFilePath);
				fileOut.getParentFile().mkdirs();
				if (!fileOut.exists()) 
					fileOut.createNewFile();
				FileWriter fwOut = new FileWriter(fileOut.getAbsoluteFile());
				BufferedWriter bwOut = new BufferedWriter(fwOut);
				bwOut.write(toWrite.toString());
				bwOut.close();
				fwOut.close();
			}
			catch(Exception ex)
			{				
				System.out.println("Exception in file : "+word+" - "+pos+" :: "+ex.toString());
//				ex.printStackTrace();		
//				System.exit(-1);
			}
			
		}

	}
	
	public static void generateWn3_0SenseClusters(String pathForNounMapping, String pathForVerbMapping, String senseFilesPath, String outputPath, Dictionary dict)
	{
		HashMap<String, ArrayList<String>> offsetMapping = DomainManager.loadVersionMaps(pathForNounMapping, pathForVerbMapping);
		File filesFolder = new File(senseFilesPath);
		File[] listOfFiles = filesFolder.listFiles();
		System.out.println(listOfFiles.length);
		
		for(File file: listOfFiles)
		{
			String absolutePath = file.getAbsolutePath();
			String filePath = absolutePath.substring(0,absolutePath.lastIndexOf("/"));
			String fileName = absolutePath.substring(absolutePath.lastIndexOf("/")+1, absolutePath.length());
			String word = fileName.substring(0,fileName.lastIndexOf("-"));
			String pos = fileName.substring(fileName.lastIndexOf("-")+1,fileName.lastIndexOf("."));
			POS posReqd = pos.equalsIgnoreCase("n") ? POS.NOUN : POS.VERB ;
//			System.out.println(word+" "+pos);
			
			try
			{
				boolean noError = true;								
				IndexWord iw = dict.lookupIndexWord(posReqd, word);
				List<Synset> senses = iw.getSenses();
				StringBuilder toWrite = new StringBuilder();
				
				Scanner sc = new Scanner(file);
				while(sc.hasNextLine())
				{
					String line = sc.nextLine().trim();
					if(line.length()>0)
					{
						String[] senseNumbers = line.split("\\s+");
						boolean wrote = false;
						for(String s : senseNumbers)
						{
							int index = Integer.parseInt(s) - 1;
							long offsetLong = senses.get(index).getOffset();
//							String offset = Long.toString(offsetLong);
							String offset = String.format("%08d", offsetLong);
							ArrayList<String> offsetsFinal = offsetMapping.get(offset);							
							if(offsetsFinal!=null && offsetsFinal.size()>0)
								for(String offsetFinal : offsetsFinal)
								{
//									bwOut.write(offsetFinal+" ");
									toWrite.append(offsetFinal+" ");
									wrote = true;
								}
							else
							{
								System.out.println("No offset matched for "+word+"-"+pos+" at offset "+offset);
								noError = false;
							}
						}
						if(wrote)
						{
							toWrite.append("\n");
//							bwOut.write("\n");
						}
					}
				}
				
				if(!outputPath.endsWith("/"))
					outputPath = outputPath +"/";
				String outputFilePath;
				if(noError)
					outputFilePath = outputPath +"auto/"+ fileName;
				else
					outputFilePath = outputPath +"manual/"+ fileName;
				File fileOut = new File(outputFilePath);
				fileOut.getParentFile().mkdirs();
				if (!fileOut.exists()) 
					fileOut.createNewFile();
				FileWriter fwOut = new FileWriter(fileOut.getAbsoluteFile());
				BufferedWriter bwOut = new BufferedWriter(fwOut);
				bwOut.write(toWrite.toString());
				bwOut.close();
				fwOut.close();
			}
			catch(Exception ex)
			{				
				System.out.println("Exception in file : "+word+" - "+pos+" :: "+ex.toString());
//				ex.printStackTrace();		
//				System.exit(-1);
			}
			
		}
	}
	
	public static void validateSense3_0Clusters(String clustersDirPath, String outputDirPath, Dictionary dict)
	{
		String pathCorrect = outputDirPath+"correct/";
		String pathClash = outputDirPath+"clash/";
		String pathNewSenses = outputDirPath+"newSenses/";
		
		File filesFolder = new File(clustersDirPath);
		File[] listOfFiles = filesFolder.listFiles();		
		
		for(File file: listOfFiles)
		{
			String absolutePath = file.getAbsolutePath();
			String filePath = absolutePath.substring(0,absolutePath.lastIndexOf("/"));
			String fileName = absolutePath.substring(absolutePath.lastIndexOf("/")+1, absolutePath.length());
			String word = fileName.substring(0,fileName.lastIndexOf("-"));
			String pos = fileName.substring(fileName.lastIndexOf("-")+1,fileName.lastIndexOf("."));
			POS posReqd = pos.equalsIgnoreCase("n") ? POS.NOUN : POS.VERB ;
//			System.out.println(word+" "+pos);
			
			try
			{
				String outputPath = outputDirPath.endsWith("/") ? outputDirPath : outputDirPath +"/" ;		
				
				IndexWord iw = dict.getIndexWord(posReqd, word);
				List<Synset> senses = iw.getSenses();
				HashMap<String, Integer> offsetCount = new HashMap<String, Integer>();
				for(Synset syn : senses)
				{
					long offsetLong = syn.getOffset();					
					String offset = String.format("%08d", offsetLong);
					offsetCount.put(offset, 0);
				}
				
				HashSet<String> uniqueClusters = new HashSet<String>();
				
				Scanner sc = new Scanner(file);
				while(sc.hasNextLine())
				{					
					String line = sc.nextLine().trim();
					if(line.length()>0)
					{
						String[] senseOffsets = line.split("\\s+");
						Arrays.sort(senseOffsets);
						StringBuilder signature = new StringBuilder();
						for(String offset : senseOffsets)
						{							
							signature.append(offset+"#");
						}
						String signatureString = signature.toString();
						uniqueClusters.add(signatureString.substring(0, signatureString.length()-1)); // removing last '#'
					}					
				}
				
				int status = 0;
				for(String cluster : uniqueClusters)
				{
					String[] senseOffsets = cluster.split("#");
					for(String offset : senseOffsets)
					{
						Integer count = offsetCount.get(offset);
						if(count == null) // invalidEntry
						{							
							status = 1;
							System.out.println("Offset not found : "+offset+" in word : "+word+" - "+pos);
							break;
						}
						if(count.intValue() == 0)
							offsetCount.put(offset, 1);
						else // seen more than once -> clash
						{	
							offsetCount.put(offset, count.intValue()+1);
							status = 2;
//							break;
						}												
					}
				}
				
				int newSenses = 0;
				if(status == 0)
				{
					for(Map.Entry<String, Integer> entry : offsetCount.entrySet())
					{
						if(entry.getValue().intValue() == 0)
							newSenses++;
					}
					if(newSenses > 0)
						status = 3;
				}
				
				if(status == 0)
					outputPath += "correct/";
				else if(status == 1)
					outputPath += "invalid/"; // invalid offsets
				else if(status == 2)
					outputPath += "clash/"; // failed intersection check
				else if(status == 3)
					outputPath += "newSenses/"; // failed exhaustive check
				else
					outputPath += "codeBug/"; // should never happen
				
				String outputFilePath = outputPath + fileName ; 
				File fileOut = new File(outputFilePath);
				fileOut.getParentFile().mkdirs();
				if (!fileOut.exists()) 
					fileOut.createNewFile();
				FileWriter fwOut = new FileWriter(fileOut.getAbsoluteFile());
				BufferedWriter bwOut = new BufferedWriter(fwOut);
				for(String cluster : uniqueClusters)
				{
					String[] senseOffsets = cluster.split("#");
					for(String offset : senseOffsets)
						bwOut.write(offset+" ");
					bwOut.write("\n");
				}
				
				if(status == 3)
				{
					bwOut.write("-----------------------------------\n"); //separator for new senses
					for(Map.Entry<String, Integer> entry : offsetCount.entrySet())
					{
						if(entry.getValue().intValue() == 0)
							bwOut.write(entry.getKey()+"\n");
					}					
				}
				
				bwOut.close();
				fwOut.close();
				
			}
			catch(Exception ex)
			{
				//ex.printStackTrace();
				System.out.println("Exception in file : "+word+" - "+pos+" :: "+ex.toString());
			}
			
		}
	}	
	
	public static HashMap<String,Double> getStats(String[] clustersDirPaths, Dictionary dict)
	{
		HashMap<String,Double> stats = new HashMap<String, Double>();		
		
		int nounCount = 0;
		int verbCount = 0;
		double nounFineSum = 0;
		double verbFineSum = 0;
		double nounCoarseSum = 0;
		double verbCoarseSum = 0;
		
		for(String clustersDirPath : clustersDirPaths)
		{
			System.out.println(clustersDirPath);
			File filesFolder = new File(clustersDirPath);
			File[] listOfFiles = filesFolder.listFiles();
			for(File file: listOfFiles)
			{
				String absolutePath = file.getAbsolutePath();			
				String fileName = absolutePath.substring(absolutePath.lastIndexOf("/")+1, absolutePath.length());
				String word = fileName.substring(0,fileName.lastIndexOf("-"));
				String pos = fileName.substring(fileName.lastIndexOf("-")+1,fileName.lastIndexOf("."));
				POS posReqd = pos.equalsIgnoreCase("n") ? POS.NOUN : POS.VERB ;
				try {				
					IndexWord iw = dict.lookupIndexWord(posReqd, word);
					List<Synset> senses = iw.getSenses();				
					int freq = 0;
					for(Synset sense : senses)
					{
						List<Word> words = sense.getWords();					
						for(Word w : words)
						{
							if(w.getLemma().equalsIgnoreCase(word))
							{
								freq += w.getUseCount();
								break;
							}
						}
					}
									
					Scanner sc = new Scanner(file);
					int numClusters = 0;
					int numSenses = 0;
					HashSet<String> senseOffsets = new HashSet<String>();
					while(sc.hasNextLine())
					{
						String line = sc.nextLine().trim();
						if(line.length() > 0)
						{
							String[] split = line.split("\\s+");
							for(String offset : split)
							{
								senseOffsets.add(offset);
							}
							numSenses += split.length;
							numClusters++;
						}
					}
					
					if(senseOffsets.size() != numSenses)
						System.out.println("Problem in "+word+" - "+pos);
					
					if(pos.equals("n"))
					{
						nounCount += freq;
						nounFineSum += numSenses*freq;
						nounCoarseSum += numClusters*freq;
					}
					else
					{
						verbCount += freq;
						verbFineSum += numSenses*freq;
						verbCoarseSum += numClusters*freq;
					}	
									
					String key = pos;//+"-"+numSenses;
					if(stats.get(key) == null)
						stats.put(key, 0.0);
					
					stats.put(key, stats.get(key).doubleValue()+1);
					
				} catch (Exception e) {				
					e.printStackTrace();
				}
			}
		}
		
		stats.put("Average Noun Polysemy in Fine Senses", nounFineSum/nounCount);
		stats.put("Average Noun Polysemy in Coarse Senses", nounCoarseSum/nounCount);
		stats.put("Average Verb Polysemy in Fine Senses", verbFineSum/verbCount);
		stats.put("Average Verb Polysemy in Coarse Senses", verbCoarseSum/verbCount);
		
		return stats;
	}
	
	public static int findDisagreements(String[] clustersDirPaths, Dictionary dict)
	{
		HashSet<String> erroneousPairs = new HashSet<String>();		
		HashMap<String,HashSet<String>> offsetToClusterMappingOverall = new HashMap<String, HashSet<String>>();
		// Create Offset to Cluster Map 
		for(String clustersDirPath : clustersDirPaths)
		{
			System.out.println(clustersDirPath);
			File filesFolder = new File(clustersDirPath);
			File[] listOfFiles = filesFolder.listFiles();
			for(File file: listOfFiles)
			{
				String absolutePath = file.getAbsolutePath();			
				String fileName = absolutePath.substring(absolutePath.lastIndexOf("/")+1, absolutePath.length());
				String word = fileName.substring(0,fileName.lastIndexOf("-"));
				String pos = fileName.substring(fileName.lastIndexOf("-")+1,fileName.lastIndexOf("."));
				POS posReqd = pos.equalsIgnoreCase("n") ? POS.NOUN : POS.VERB;
				try 
				{					
					Scanner sc = new Scanner(file);
					int clusterIndex = 0;
					while(sc.hasNextLine())
					{
						String line = sc.nextLine();
						String[] senseOffsets = line.split("\\s+");							
						
						for(String offset : senseOffsets)
						{
							HashSet<String> mapped = offsetToClusterMappingOverall.get(offset);
							if(mapped == null)
							{
								mapped = new HashSet<String>();
								offsetToClusterMappingOverall.put(offset, mapped);								
							}
							mapped.add(word+"-"+pos+":"+clusterIndex);							
						}
						clusterIndex++;
					}
					sc.close();
				} 
				catch (Exception e) 
				{					
					e.printStackTrace();
					System.exit(-1);
				}
				
			}
		}
		
		// For every cross cluster pair, find number of intersection
		for(String clustersDirPath : clustersDirPaths)
		{
			System.out.println(clustersDirPath);
			File filesFolder = new File(clustersDirPath);
			File[] listOfFiles = filesFolder.listFiles();
			for(File file: listOfFiles)
			{
				String absolutePath = file.getAbsolutePath();			
				String fileName = absolutePath.substring(absolutePath.lastIndexOf("/")+1, absolutePath.length());
				String word = fileName.substring(0,fileName.lastIndexOf("-"));
				String pos = fileName.substring(fileName.lastIndexOf("-")+1,fileName.lastIndexOf("."));
				POS posReqd = pos.equalsIgnoreCase("n") ? POS.NOUN : POS.VERB;
				try 
				{
					IndexWord iw = dict.getIndexWord(posReqd, word);
					HashSet<String> allSenseOffsets = new HashSet<String>();
					for(Synset sense : iw.getSenses())
					{
						long offsetLong = sense.getOffset();						
						String offset = String.format("%08d", offsetLong);
						allSenseOffsets.add(offset);
					}
					Scanner sc = new Scanner(file);					
					while(sc.hasNextLine())
					{
						String line = sc.nextLine();
						String[] senseOffsets = line.split("\\s+");							
						
						HashSet<String> cluster = new HashSet<String>();
						for(String offset : senseOffsets)
						{
							cluster.add(offset);																			
						}
						
						for(String offset : senseOffsets)
						{
							for(String offsetOrig : allSenseOffsets)
							{
								if(!cluster.contains(offsetOrig)) // cross cluster pair
								{
									HashSet<String> mapped1 = offsetToClusterMappingOverall.get(offset);
									HashSet<String> mapped2 = offsetToClusterMappingOverall.get(offsetOrig);
									//check if there is any intersection
									HashSet<String> temp = new HashSet<String>(mapped1);
									temp.retainAll(mapped2);
									if(temp.size() > 0)
									{										
//										System.out.print(word + "-" + pos + " : ");
										for(String common : temp)
										{
//											System.out.print(common+" ");
											String w = common.split("-")[0];
											if(w.compareTo(word) > 0)
											{
												erroneousPairs.add(w+" "+word);
											}
											else
											{
												erroneousPairs.add(word+" "+w);
											}												
										}
//										System.out.println();																				
										
									}
								}
							}
						}						
					}
					sc.close();
				} 
				catch (Exception e) 
				{					
					e.printStackTrace();
					System.exit(-1);
				}
				
			}
		}
		for(String error : erroneousPairs)
		{
			System.out.println(error);
		}
		return erroneousPairs.size();
	}
	
	public static Dictionary wnTest(String propsFile, boolean test)
	{
		try{			
			JWNL.initialize(new FileInputStream(propsFile));
			Dictionary dictionary = Dictionary.getInstance();
			if(test)
			{
				IndexWord iw = dictionary.lookupIndexWord(POS.NOUN, "bill ");
				int count = 0;
				if(iw!=null)
				{
					List<Synset> senses = iw.getSenses();
					if(senses != null)
					{
						System.out.println(senses.size());
						for(Synset syn : senses)
							System.out.println(++count +" "+syn);
					}
				}
				else
				{
					System.out.println("No Index Word");
				}
			}
			return dictionary;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
		
	
	public static void main(String[] args) {
		
		String propsFile30 = "resources/file_properties.xml";
		String propsFile21 = "resources/file_properties_2_1.xml";
		String propsFile20 = "resources/file_properties_2_0.xml";
		
		String fileDir = "/home/sumitb/Desktop/Sense Clustering/Corpora/ontonotes/data/english/metadata/sense-inventories/";
		String outputFolder = "/home/sumitb/Desktop/output1/";		
		
		String outputFolder1 = "/home/sumitb/Desktop/output2/";
		String pathForNounMapping0 = "resources/DomainExperiment/wordnet2.0to3.0MapNoun.txt";
		String pathForVerbMapping0 = "resources/DomainExperiment/wordnet2.0to3.0MapVerb.txt";
		String pathForNounMapping1 = "resources/DomainExperiment/map23Noun.txt";
		String pathForVerbMapping1 = "resources/DomainExperiment/map23Verb.txt";
		String senses2_0FilesPath = outputFolder+"2.0/";
		String senses2_1FilesPath = outputFolder+"2.1/";
		String senses3_0FilesPath = outputFolder+"3.0/";
		String outputPath2_0 = outputFolder1+"2.0/";
		String outputPath2_1 = outputFolder1+"2.1/";
		String outputPath3_0 = outputFolder1+"3.0/";
		
		String outputFolder2 = "/home/sumitb/Desktop/output3/";
		String outputFolder2_2_1 = outputFolder2+"2.1/";
		String outputFolder2_2_0 = outputFolder2+"2.0/";
		String outputFolder2_3_0   = outputFolder2+"3.0/";
		
		Dictionary dict = wnTest(propsFile30, false);		
		if(dict != null)
		{
//			processSenseClusters(fileDir, outputFolder, dict);	
//			generateWn3_0SenseClusters(senses3_0FilesPath, outputPath3_0, dict);
//			validateSense3_0Clusters(outputPath2_1+"auto/", outputFolder2_2_1, dict);
//			validateSense3_0Clusters(outputPath2_0+"auto/", outputFolder2_2_0, dict);
//			validateSense3_0Clusters(outputPath3_0+"auto/", outputFolder2_3_0, dict);
		}
		
//		Dictionary dict0 = wnTest(propsFile20, false);		
//		if(dict0 != null)
//			generateWn3_0SenseClusters(pathForNounMapping0, pathForVerbMapping0, senses2_0FilesPath, outputPath2_0, dict0);						
//	
//		Dictionary dict1 = wnTest(propsFile21, false);		
//		if(dict1 != null)
		{
//			generateWn3_0SenseClusters(pathForNounMapping1, pathForVerbMapping1, senses2_1FilesPath, outputPath2_1, dict1);
		}
		
		String[] clusterPaths = {outputFolder2_2_0+"correct/",outputFolder2_2_1+"correct/",outputFolder2_3_0+"correct/"};
		
//		HashMap<String,Double> stats0 = getStats(clusterPaths, dict);		
//		for(Map.Entry<String, Double> entry : stats0.entrySet())
//		{
//			System.out.println(entry.getKey() +" "+entry.getValue());
//		}
		
		System.out.println(findDisagreements(clusterPaths, dict));
		
	}

}
