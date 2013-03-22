package krsystem.ontology.senseClustering.svm;

import java.util.HashSet;
import java.util.List;
import java.io.FileInputStream;

import krsystem.utility.OrderedPair;
import krsystem.utility.SynsetToWordIndexMap;

import net.sf.extjwnl.JWNL;
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
import edu.sussex.nlp.jws.*;


public class WNBasedSimilarity {	
	Dictionary dict;
//	EXTJWNLUtils tools;
	SynsetToWordIndexMap tools;
	
	JWS ws;
	public int MiMoHeuristicK; // K=3 gives us the 'twins' similarity measure
	
	//Paths between synsets based
	HirstAndStOnge hso;
	LeacockAndChodorow lch;
	WuAndPalmer wup;
	
	//Information Content Based
	JiangAndConrath jcn;
	Lin lin;
	Resnik res;
	
	//Gloss based
	AdaptedLesk adapLesk;				
	AdaptedLeskTanimoto adapLeskTani;
	AdaptedLeskTanimotoNoHyponyms adapLeskTaniNoHypo;
	
	
	public WNBasedSimilarity(String dir, String arg, int MiMoHeuristicKPassed, Dictionary dictionary, String wordIndexMappingPath)
	{
		dict = dictionary;
//		tools = new EXTJWNLUtils(dictionary);
		tools = new SynsetToWordIndexMap(wordIndexMappingPath);
		ws = new JWS(dir, arg);
		
		//Paths between synsets based
		hso = ws.getHirstAndStOnge();
		lch = ws.getLeacockAndChodorow();
		wup = ws.getWuAndPalmer();
		
		//Information Content Based
		jcn = ws.getJiangAndConrath();
		lin = ws.getLin();
		res = ws.getResnik();
		
		//Gloss based
		adapLesk = ws.getAdaptedLesk();				
		adapLeskTani = ws.getAdaptedLeskTanimoto();
		adapLeskTaniNoHypo = ws.getAdaptedLeskTanimotoNoHyponyms();
		
		MiMoHeuristicK = MiMoHeuristicKPassed;
	}
		
	
	public double[] getSimilarities(Synset syn1, Synset syn2)
	{		
		POS pos1 = syn1.getPOS();
		POS pos2 = syn2.getPOS();
		if(!pos1.equals(pos2))
		{
			//report error?
			System.out.println("POS Mismatch");
			System.exit(-1);
		}
		String posString = "";
		if(pos1.equals(POS.NOUN))
			posString = "n";
		if(pos1.equals(POS.VERB))
			posString = "v";
		OrderedPair<String, Integer> pair1 = tools.getWordIndexPair(syn1);
		OrderedPair<String, Integer> pair2 = tools.getWordIndexPair(syn2);
		
		if(posString.length()==0 || pair1==null || pair2==null)
		{
			//report error?
			System.out.println("Can't get required Synsets : "+syn1.getOffset()+ " "+syn2.getOffset());
			System.out.println(posString);
			System.out.println(pair1);
			System.out.println(pair2);
			System.exit(-1);
		}
				
		double[] similarities = new double[9];
		try{
			if(syn1.getOffset()==126264 && syn2.getOffset()==169806 && posString.equals("v"))
				similarities[0] = 0;
			else
				similarities[0] = hso.hso(pair1.getL(), pair1.getR().intValue(), pair2.getL(), pair2.getR().intValue(), posString);
		similarities[1] = lch.lch(pair1.getL(), pair1.getR().intValue(), pair2.getL(), pair2.getR().intValue(), posString);
		similarities[2] = wup.wup(pair1.getL(), pair1.getR().intValue(), pair2.getL(), pair2.getR().intValue(), posString);
		similarities[3] = jcn.jcn(pair1.getL(), pair1.getR().intValue(), pair2.getL(), pair2.getR().intValue(), posString);
		similarities[4] = lin.lin(pair1.getL(), pair1.getR().intValue(), pair2.getL(), pair2.getR().intValue(), posString);
		similarities[5] = res.res(pair1.getL(), pair1.getR().intValue(), pair2.getL(), pair2.getR().intValue(), posString);
		similarities[6] = adapLesk.lesk(pair1.getL(), pair1.getR().intValue(), pair2.getL(), pair2.getR().intValue(), posString);
		similarities[7] = adapLeskTani.lesk(pair1.getL(), pair1.getR().intValue(), pair2.getL(), pair2.getR().intValue(), posString);
		similarities[8] = adapLeskTaniNoHypo.lesk(pair1.getL(), pair1.getR().intValue(), pair2.getL(), pair2.getR().intValue(), posString);
		}
		catch(Error soe)
		{
			System.out.println(syn1);
			System.out.println(syn2);
			System.out.println(pair1.getL() +" "+pair1.getR());
			System.out.println(pair2.getL() +" "+pair2.getR());
			soe.printStackTrace();
			System.exit(-1);
		}
		return similarities;
	}
	
	public double[] getFeatures(Synset syn1, Synset syn2)
	{		
		POS pos1 = syn1.getPOS();
		POS pos2 = syn2.getPOS();
		if(!pos1.equals(pos2))
		{
			//report error?
			System.out.println("POS Mismatch");
			System.exit(-1);
		}
		String posString = "";
		if(pos1.equals(POS.NOUN))
			posString = "n";
		if(pos1.equals(POS.VERB))
			posString = "v";		
		
		if(posString.length() == 0)
			return null;		
							
		Dictionary dict = syn1.getDictionary();
		List<Word> words1 = syn1.getWords();
		List<Word> words2 = syn2.getWords();		
		
		// MiMo MergeSP2
		int commonAntonymCount = 0;
		try{
			HashSet<String> antonymOffsets1 = new HashSet<String>();
			PointerTargetNodeList list1 = PointerUtils.getAntonyms(syn1);
			for(PointerTargetNode ptn : list1)
			{
				long offsetLong = ptn.getSynset().getOffset();						
				String offset = String.format("%08d", offsetLong);				
				antonymOffsets1.add(offset);
			}
					
			PointerTargetNodeList list2 = PointerUtils.getAntonyms(syn2);
			for(PointerTargetNode ptn : list2)
			{
				long offsetLong = ptn.getSynset().getOffset();						
				String offset = String.format("%08d", offsetLong);				
				if(antonymOffsets1.contains(offset))
					commonAntonymCount++;
			} 
		}
		catch(Exception ex)
		{
			System.out.println("--------------------Error in finding antonym----------------");
			System.out.println(syn1);
			System.out.println(syn2);
			ex.printStackTrace(); // this throws "array index out of bounds sometimes"
			System.out.println("-----------------------------------------------");
		}
		
		// MiMo MergeSP3
//		Only for adjectives apparently 		
//		int commonPertainymCount = 0;	
//		HashSet<String> pertainymOffsets1 = new HashSet<String>();
//		list1 = PointerUtils.getPertainyms(syn1);
//		for(PointerTargetNode ptn : list1)
//		{
//			long offsetLong = ptn.getSynset().getOffset();						
//			String offset = String.format("%08d", offsetLong);				
//			pertainymOffsets1.add(offset);
//		}
//			
//		list2 = PointerUtils.getPertainyms(syn2);
//		for(PointerTargetNode ptn : list2)
//		{
//			long offsetLong = ptn.getSynset().getOffset();						
//			String offset = String.format("%08d", offsetLong);				
//			if(pertainymOffsets1.contains(offset))
//				commonPertainymCount++;
//		}						
		
		int commonWordsCount = 0;
		int commonWordSenseCount = 0;
		HashSet<String> commonWords = new HashSet<String>();		
		HashSet<String> lemmaSet1 = new HashSet<String>();
		for(Word word : words1)
			lemmaSet1.add(word.getLemma());
		for(Word word : words2)
			if(lemmaSet1.contains(word.getLemma()))
				commonWords.add(word.getLemma());
		commonWordsCount = commonWords.size();
		
		for(Word word1 : words1)
			for(Word word2 : words2)
				if(word1.equals(word2))
					commonWordSenseCount++;
		
		int senseCount = 0;// max number of senses in common words
		int senseNum = 0; // number of words with max number of senses
		try{			
			for(String lemma : commonWords)
			{
				IndexWord iw = dict.getIndexWord(syn1.getPOS(), lemma);
				List<Synset> senses = iw.getSenses();
				if(senses != null)
				{
					if(senses.size() > senseCount)
					{
						senseCount = senses.size();
						senseNum = 1;
					}
					else if(senses.size() == senseCount)
					{
						senseNum ++;
					}					
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}		
		
		//LexFile?
		String lexFile1 = syn1.getLexFileName();
		String lexFile2 = syn2.getLexFileName();
		int lexFileSimilarity = lexFile1.equalsIgnoreCase(lexFile2) ? 1 : 0;
		
		int verbGroup = 0;
		int verbFrame = 0;
		if(posString.equalsIgnoreCase("v"))
		{
			List<Pointer> pointers = syn1.getPointers(PointerType.VERB_GROUP);// MiMo MergeSP0
			for(Pointer pointer : pointers)
			{
				if(syn2.equals(pointer.getTargetSynset()))
					verbGroup = 1;			
			}
			
			HashSet<String> syn1Frames = new HashSet<String>();
			for(String frame : syn1.getVerbFrames())
				syn1Frames.add(frame.toLowerCase());
			for(String frame : syn2.getVerbFrames())
				if(syn1Frames.contains(frame.toLowerCase()))
					verbFrame++;										
		}
		
		// Setting Features
		double[] features;
		if(posString.equalsIgnoreCase("n"))
			features = new double[6];
		else if(posString.equalsIgnoreCase("v"))
			features = new double[6+2];
		else
			return null;
		
		int index = 0; 
		features[index++] = commonAntonymCount;
		features[index++] = commonWordsCount;
		features[index++] = commonWordSenseCount;
		features[index++] = senseCount;
		features[index++] = senseNum;
		features[index++] = lexFileSimilarity;
		
		if(posString.equalsIgnoreCase("v"))
		{				
			features[index++] = verbGroup;
			features[index++] = verbFrame;		
		}
		
		return features;
	}
	
	public double[] getFeaturesMiMoSP1(Synset s1, Synset s2)
	{				
		boolean mergeSP1_1 = true;
		boolean mergeSP1_2 = false;
		boolean mergeSP1_2_relaxed = false;
		boolean mergeSP1_3 = false;
		int numberOfCommonHypernyms = 0; // sisters ?
		boolean autohyponymy = false;
		
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
		
		HashSet<String> hypernymOffsets1 = new HashSet<String>();
		PointerTargetNodeList list1 = PointerUtils.getDirectHypernyms(s1);
		for(PointerTargetNode ptn : list1)
		{
			Synset temp = ptn.getSynset();
			if(temp.equals(s2))
				autohyponymy = true;
			long offsetLong = temp.getOffset();						
			String offset = String.format("%08d", offsetLong);				
			hypernymOffsets1.add(offset);
		}
		
		HashSet<String> hypernymOffsets2 = new HashSet<String>();
		PointerTargetNodeList list2 = PointerUtils.getDirectHypernyms(s2);
		for(PointerTargetNode ptn : list2)
		{
			Synset temp = ptn.getSynset();
			if(temp.equals(s1))
				autohyponymy = true;
			long offsetLong = temp.getOffset();						
			String offset = String.format("%08d", offsetLong);				
			hypernymOffsets2.add(offset);
		}
		
		hypernymOffsets1.retainAll(hypernymOffsets2);		
		numberOfCommonHypernyms = hypernymOffsets1.size();
		if(foundAll)
		{
			if(numberOfCommonHypernyms > 0)
				mergeSP1_2_relaxed = true ;
			if(numberOfCommonHypernyms == hypernymOffsets2.size()) // they have exactly same hypernyms
				mergeSP1_2 = true ;
		}
		
		
		words1.retainAll(words2);
		if(words1.size() > MiMoHeuristicK)
			mergeSP1_3 = true;			
		else
			mergeSP1_3 = false;
		
		double[] features = new double[6];
		features[0] = mergeSP1_1 ? 1 : 0;
		features[1] = mergeSP1_2 ? 1 : 0;
		features[2] = mergeSP1_2_relaxed ? 1 : 0;
		features[3] = mergeSP1_3 ? 1 : 0;
		features[4] = numberOfCommonHypernyms;
		features[5] = autohyponymy ? 1 :0;
		return features;
	}
	
	public static void main(String[] args) {
		String propsFile30 = "resources/file_properties.xml";
		String dir = "/home/sumitb/Data/";
		String arg = "WordNet-3.0";
		String mappingPath = "resources/Clustering/synsetWordIndexMap/verbMap.txt";
		try{			
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();
			Synset syn1 = dictionary.getSynsetAt(POS.VERB, 126264);
			Synset syn2 = dictionary.getSynsetAt(POS.VERB, 169806);
			System.out.println(syn1.getWords().size());
			EXTJWNLUtils tools = new EXTJWNLUtils(dictionary);
			OrderedPair<String, Integer> pair = tools.getWordIndexPair(syn1);
			System.out.println(pair.getL()+" "+pair.getR());
//			List<Synset> syns = dictionary.getIndexWord(POS.NOUN, "head").getSenses();
			JWS ws = new JWS(dir, arg);
			WNBasedSimilarity wnbs = new WNBasedSimilarity(dir, arg, 3, dictionary, mappingPath);
			
//			int index = 0;
//			double[] similarities = wnbs.getSimilarities(syns.get(0), syns.get(1));
//			for(double score : similarities)
//				System.out.println("1 : "+index++ +" "+ score);
//			
//			similarities = wnbs.getFeatures(syns.get(0), syns.get(1));
//			for(double score : similarities)
//				System.out.println("2 : "+index++ +" "+ score);
//			
//			similarities = wnbs.getFeaturesMiMoSP1(syns.get(0), syns.get(1));
//			for(double score : similarities)
//				System.out.println("3 : "+index++ +" "+ score);
			
			LeacockAndChodorow lch = ws.getLeacockAndChodorow();
//			EXTJWNLUtils tools = new EXTJWNLUtils(dictionary);
//			Synset syn1 = dictionary.getSynsetAt(POS.VERB, 169806);
//			Synset syn2 = dictionary.getSynsetAt(POS.VERB, 126264);
			OrderedPair<String, Integer> pair1 = tools.getWordIndexPair(syn1);
			OrderedPair<String, Integer> pair2 = tools.getWordIndexPair(syn2);
			HirstAndStOnge hso = ws.getHirstAndStOnge();
//			System.out.println(lch.lch("retain",1, "control",1, "v"));
//			System.out.println(hso.hso(pair1.getL(),pair1.getR().intValue(), pair2.getL(),pair2.getR().intValue(), "v"));
			System.out.println(lch.lch(pair1.getL(),pair1.getR().intValue(), pair2.getL(),pair2.getR().intValue(), "v"));
//			for(double d:wnbs.getSimilarities(syn1, syn2))
//				System.out.print(d+",");
//			System.out.println(lch.lch("fall over backwards", 1, "presume", 1, "v"));
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

	}

}
