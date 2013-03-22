package krsystem.ontology.senseClustering.svm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.List;

import jnisvmlight.LabeledFeatureVector;

import krsystem.StaticValues;
import krsystem.utility.OrderedPair;
import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

public class EXTJWNLUtils {

	Dictionary dict;
	
	public EXTJWNLUtils(Dictionary dictionary)
	{
		dict = dictionary;
	}
	
	public OrderedPair<String, Integer> getWordIndexPair(Synset syn)
	{		
		OrderedPair<String, Integer> wordIndexPair = null;		
		try{
			syn = dict.getSynsetAt(syn.getPOS(), syn.getOffset());
			List<Word> words = syn.getWords();
			if(words == null) System.out.println("words is NULL !");
			if(words.size() == 0) System.out.println("words is empty !");			
			Word word = words.get(0);
			String lemma = word.getLemma();
			List<Synset> syns = syn.getDictionary().getIndexWord(syn.getPOS(), lemma).getSenses();
			int count = 1;
			for(Synset s : syns)
			{
				if(s.equals(syn))
				{
//					System.out.println(lemma + " "+ count);
					return new OrderedPair<String, Integer>(lemma, count);
				}					
				count++;
			}
			//if reached here, throw error
			System.out.println("Error in getting lemma and sense number");					
		}
		catch(Exception ex)
		{			
//			System.out.println(syn.getWords().size());
//			System.out.println(syn.getPOS());
//			System.out.println(syn.getOffset());
//			System.out.println(syn.getGloss());
			return check(syn);
//			ex.printStackTrace();
//			System.exit(-1);
		}
		return wordIndexPair;
	}	
	
	public OrderedPair<String, Integer> check(Synset synPassed)
	{		
		OrderedPair<String, Integer> wordIndexPair = null;	
		String propsFile30 = StaticValues.propsFile30;		
		try{			
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();
			POS pos = synPassed.getPOS();
			long offset = synPassed.getOffset();
			System.out.println(pos+" "+offset);
			Synset syn = dictionary.getSynsetAt(pos, offset);			
			if(syn!=null)
			{
				List<Word> words = syn.getWords();
				if(words == null) System.out.println("words is NULL !");
				if(words.size() == 0) System.out.println("words is empty !");			
				Word word = words.get(0);
				String lemma = word.getLemma();
				List<Synset> syns = dictionary.getIndexWord(syn.getPOS(), lemma).getSenses();
				int count = 1;
				for(Synset s : syns)
				{
					if(s.equals(syn))
					{
//						System.out.println(lemma + " "+ count);
						dictionary.close();
						return new OrderedPair<String, Integer>(lemma, count);
					}					
					count++;
				}
				//if reached here, throw error
				System.out.println("Error in getting lemma and sense number");					
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
		return wordIndexPair;
	}
	
	public static void main(String[] args) {
		String propsFile30 = "resources/file_properties.xml";
		String dir = "/home/sumitb/Data/";
		String arg = "WordNet-3.0";		
		String offsets = "/home/sumitb/Data/xwnd/offsets.txt";
		try{			
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();					
			BufferedReader br = new BufferedReader(new FileReader(new File(offsets)));
			String line;
			int zeroCount = 0;
			int nullCount = 0;
			int i = 0;
			while((line = br.readLine())!=null)
//			for(Synset syn : syns)
			{
//				long offset = 167278;
				POS pos = POS.NOUN;
				if(line.endsWith("n")){
					i++;
					if(i%1000 == 0)
						System.out.println(i+ " "+ zeroCount+ " "+ nullCount);
					long offset = Long.parseLong(line.split("-")[0]);
					Synset syn= dictionary.getSynsetAt(pos, offset);
					if(syn!=null)
					{					
//						System.out.println(syn.getGloss());					
						List<Word> words = syn.getWords();				
						if(words != null)
						{
//							System.out.println("Size of words : "+words.size());
//							for(Word word : words)
//							{							
//								System.out.println(word);
//								System.out.println(word.getSenseKey());
//							}
							if(words.size()==0)
								zeroCount++;
						}
						else{
							System.out.println("No words found !");
							nullCount++;
						}						
					}				
				}
			}
			System.out.println(zeroCount);
			System.out.println(nullCount);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

	}

}
