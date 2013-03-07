package krsystem.ontology.senseClustering;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Pointer;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.dictionary.Dictionary;

public class MiMo 
{
	public int k;
	
	public MiMo(int kPassed)
	{
		k = kPassed;
	}

	public static Dictionary wnTest(String propsFile, boolean test)
	{
		try{			
			JWNL.initialize(new FileInputStream(propsFile));
			Dictionary dictionary = Dictionary.getInstance();
			if(test)
			{
				IndexWord iw = dictionary.lookupIndexWord(POS.VERB, "head");
				int count = 0;
				if(iw!=null)
				{
					List<Synset> senses = iw.getSenses();
					if(senses != null)
					{
						System.out.println(senses.size());
						for(Synset syn : senses)
						{
							System.out.println(++count +" "+syn);								
						}
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
	
	public boolean mergeSP0(Dictionary dict, Synset s1, Synset s2)
	{
		PointerTargetNodeList list1 = PointerUtils.getVerbGroup(s1);
		for(PointerTargetNode ptn : list1)
		{					
			if(s2.equals(ptn.getSynset()))
				return true;
		}
		return false;
	}
	
	public boolean mergeSP1_1(Dictionary dict, Synset s1, Synset s2)
	{		
		List<Word> words1 = s1.getWords();
		List<Word> words2 = s2.getWords();
		
		if(words1.size() < 2 || words2.size() < 2 || words1.size() != words2.size())
			return false;
		
		HashSet<String> wordSet1 = new HashSet<String>();		
		HashSet<String> wordSet2 = new HashSet<String>();
		
		boolean foundAll = true;		
		for(Word w : words1)		
			wordSet1.add(w.getLemma());
		
		for(Word w : words2)		
			wordSet2.add(w.getLemma());
		
		for(Word w : words1)		
			if(!wordSet2.contains(w.getLemma()))
			{
				foundAll = false;
				break;
			}
		
		if(foundAll)
			for(Word w : words2)		
				if(!wordSet1.contains(w.getLemma()))
				{
					foundAll = false;
					break;
				}			
		
		return foundAll;
	}
	
	public boolean mergeSP1_2(Dictionary dict, Synset s1, Synset s2)
	{		
		List<Word> words1 = s1.getWords();
		List<Word> words2 = s2.getWords();
		
		if(words1.size() != words2.size())
			return false;
		
		HashSet<String> wordSet1 = new HashSet<String>();		
		HashSet<String> wordSet2 = new HashSet<String>();
		
		boolean foundAll = true;		
		for(Word w : words1)		
			wordSet1.add(w.getLemma());
		
		for(Word w : words2)		
			wordSet2.add(w.getLemma());
		
		for(Word w : words1)		
			if(!wordSet2.contains(w.getLemma()))
			{
				foundAll = false;
				break;
			}
		
		if(foundAll)
			for(Word w : words2)		
				if(!wordSet1.contains(w.getLemma()))
				{
					foundAll = false;
					break;
				}			
						
		if(foundAll)
		{
			HashSet<String> hypernymOffsets1 = new HashSet<String>();
			PointerTargetNodeList list1 = PointerUtils.getDirectHypernyms(s1);
			for(PointerTargetNode ptn : list1)
			{
				long offsetLong = ptn.getSynset().getOffset();						
				String offset = String.format("%08d", offsetLong);				
				hypernymOffsets1.add(offset);
			}
			
			HashSet<String> hypernymOffsets2 = new HashSet<String>();
			PointerTargetNodeList list2 = PointerUtils.getDirectHypernyms(s2);
			for(PointerTargetNode ptn : list2)
			{
				long offsetLong = ptn.getSynset().getOffset();						
				String offset = String.format("%08d", offsetLong);				
				hypernymOffsets2.add(offset);
			}
			
			hypernymOffsets1.retainAll(hypernymOffsets2);
			if(hypernymOffsets1.size() == hypernymOffsets2.size()) // they have exactly same hypernyms
				return true;
		}
		
		return false;
	}	
	
	public boolean mergeSP1_3(Dictionary dict, Synset s1, Synset s2)
	{		
		List<Word> words1 = s1.getWords();
		List<Word> words2 = s2.getWords();			
		
		HashSet<String> wordSet1 = new HashSet<String>();		
		HashSet<String> wordSet2 = new HashSet<String>();
		
		for(Word w : words1)		
			wordSet1.add(w.getLemma());
		
		for(Word w : words2)		
			wordSet2.add(w.getLemma());
		
		words1.retainAll(words2);
		if(words1.size() > k)
			return true;			
		else
			return false;
	}
	
	public boolean mergeSP1(Dictionary dict, Synset s1, Synset s2)
	{		
		boolean mergeSP1_1 = true;
		boolean mergeSP1_2 = false;
		boolean mergeSP1_3 = false;
		
		List<Word> words1 = s1.getWords();
		List<Word> words2 = s2.getWords();
		
		if(words1.size() < 2 || words2.size() < 2 || words1.size() != words2.size())
			mergeSP1_1 = false;
		
		if(words1.size() != words2.size())
			mergeSP1_2 = false;
		
		HashSet<String> wordSet1 = new HashSet<String>();		
		HashSet<String> wordSet2 = new HashSet<String>();
		
		boolean foundAll = true;		
		for(Word w : words1)		
			wordSet1.add(w.getLemma());
		
		for(Word w : words2)		
			wordSet2.add(w.getLemma());
		
		for(Word w : words1)		
			if(!wordSet2.contains(w.getLemma()))
			{
				foundAll = false;
				break;
			}
		
		if(foundAll)
			for(Word w : words2)		
				if(!wordSet1.contains(w.getLemma()))
				{
					foundAll = false;
					break;
				}			
		
		mergeSP1_1 = foundAll;
		
		if(foundAll)
		{
			HashSet<String> hypernymOffsets1 = new HashSet<String>();
			PointerTargetNodeList list1 = PointerUtils.getDirectHypernyms(s1);
			for(PointerTargetNode ptn : list1)
			{
				long offsetLong = ptn.getSynset().getOffset();						
				String offset = String.format("%08d", offsetLong);				
				hypernymOffsets1.add(offset);
			}
			
			HashSet<String> hypernymOffsets2 = new HashSet<String>();
			PointerTargetNodeList list2 = PointerUtils.getDirectHypernyms(s2);
			for(PointerTargetNode ptn : list2)
			{
				long offsetLong = ptn.getSynset().getOffset();						
				String offset = String.format("%08d", offsetLong);				
				hypernymOffsets2.add(offset);
			}
			
			hypernymOffsets1.retainAll(hypernymOffsets2);
			if(hypernymOffsets1.size() == hypernymOffsets2.size()) // they have exactly same hypernyms
				mergeSP1_2 = true;
		}
		
		words1.retainAll(words2);
		if(words1.size() > k)
			mergeSP1_3 = true;			
		else
			mergeSP1_3 = false;
		
		return mergeSP1_1 || mergeSP1_2 || mergeSP1_3;
	}

	public boolean mergeSP2(Dictionary dict, Synset s1, Synset s2)
	{
		//Atleast one word in common
		List<Word> words1 = s1.getWords();
		List<Word> words2 = s2.getWords();								
		
		HashSet<String> wordSet1 = new HashSet<String>();		
		HashSet<String> wordSet2 = new HashSet<String>();
				
		for(Word w : words1)		
			wordSet1.add(w.getLemma());
		
		for(Word w : words2)		
			wordSet2.add(w.getLemma());
		
		wordSet1.retainAll(wordSet2);
		if(wordSet1.size() == 0)
			return false;
		
		HashSet<String> offsets1 = new HashSet<String>();
		PointerTargetNodeList list1 = PointerUtils.getAntonyms(s1);
		for(PointerTargetNode ptn : list1)
		{
			long offsetLong = ptn.getSynset().getOffset();						
			String offset = String.format("%08d", offsetLong);				
			offsets1.add(offset);
		}
		
		HashSet<String> offsets2 = new HashSet<String>();
		PointerTargetNodeList list2 = PointerUtils.getAntonyms(s2);
		for(PointerTargetNode ptn : list2)
		{
			long offsetLong = ptn.getSynset().getOffset();						
			String offset = String.format("%08d", offsetLong);				
			offsets2.add(offset);
		}
		
		// If there is atleast one common OR all should be same?
		return false;
	}
	
	

	
	public static void main(String[] args) 
	{
		String propsFile30 = "resources/file_properties.xml";
		
		String outputFolder2 = "/home/sumitb/Desktop/output3/";
		String outputFolder2_2_1 = outputFolder2+"2.1/";
		String outputFolder2_2_0 = outputFolder2+"2.0/";
		String outputFolder2_3_0   = outputFolder2+"3.0/";
		String[] clusterPaths = {outputFolder2_2_0+"correct/",outputFolder2_2_1+"correct/",outputFolder2_3_0+"correct/"};
		
		String outputFolderMiMo = "/home/sumitb/Desktop/MiMo/";
		
//		Dictionary dict = wnTest(propsFile30, false);
		Dictionary dict = wnTest(propsFile30, false);
//		if(dict != null)
//		{			
//			for(String clustersDirPath : clusterPaths)
//			{
//				System.out.println(clustersDirPath);
//				File filesFolder = new File(clustersDirPath);
//				File[] listOfFiles = filesFolder.listFiles();
//				for(File file: listOfFiles)
//				{
//					String absolutePath = file.getAbsolutePath();			
//					String fileName = absolutePath.substring(absolutePath.lastIndexOf("/")+1, absolutePath.length());
//					String word = fileName.substring(0,fileName.lastIndexOf("-"));
//					String pos = fileName.substring(fileName.lastIndexOf("-")+1,fileName.lastIndexOf("."));
//					POS posReqd = pos.equalsIgnoreCase("n") ? POS.NOUN : POS.VERB;
//					
//					try 
//					{
//						IndexWord iw = dict.lookupIndexWord(posReqd, word);
//						HashSet<String> allSenseOffsets = new HashSet<String>();
//						for(Synset sense : iw.getSenses())
//						{
//							long offsetLong = sense.getOffset();						
//							String offset = String.format("%08d", offsetLong);
//							allSenseOffsets.add(offset);
//						}
//						
//					} catch (Exception e) {						
//						e.printStackTrace();
//					}
//					
//				}
//			}
//		}

		try{
			IndexWord iw = dict.getIndexWord(POS.VERB, "sit");
			for(Synset syn : iw.getSenses())
			{
				System.out.println("Synset : "+syn.getOffset());
				int count = 0;
				List<Pointer> pointers = syn.getPointers(PointerType.VERB_GROUP);
				for(Pointer pointer : pointers)
				{
					System.out.println(count++ +" "+ pointer.getTargetSynset().getOffset());
				}
				System.out.println("------------------------------------------");
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}

}
