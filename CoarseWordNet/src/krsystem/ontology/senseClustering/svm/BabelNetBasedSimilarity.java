package krsystem.ontology.senseClustering.svm;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import net.sf.extjwnl.data.Synset;

import edu.mit.jwi.item.POS;

import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelSense;
import it.uniroma1.lcl.babelnet.BabelSenseSource;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.jlt.util.Language;

public class BabelNetBasedSimilarity {

	BabelNet bn;
	
	public BabelNetBasedSimilarity() {
		bn = BabelNet.getInstance();
	}
	
	public double[] getFeatures(Synset syn1, Synset syn2)
	{		
		double[] features = null;
		try{
			if(!syn1.getPOS().equals(syn2.getPOS()))
			{
				System.out.println("POS Mismatch !");
				System.exit(-1);
			}
			
			String posString = syn1.getPOS().equals(net.sf.extjwnl.data.POS.NOUN) ? "noun" : "";
			if(syn1.getPOS().equals(net.sf.extjwnl.data.POS.VERB))
				posString = "verb";			
			
			if(posString.length()==0)
			{
				System.out.println("POS Error !");
				System.exit(-1);
			}
			
			String offset1 = String.format("%08d", syn1.getOffset()) + posString.charAt(0);
			List<BabelSynset> synsets1 = bn.getBabelSynsetsFromWordNetOffset(offset1);
			String offset2 = String.format("%08d", syn2.getOffset()) + posString.charAt(0);
			List<BabelSynset> synsets2 = bn.getBabelSynsetsFromWordNetOffset(offset2);
			
			if(synsets1==null || synsets2==null)
				return features;			
			
			HashSet<String> translationsEN = new HashSet<String>();
			HashSet<String> translationsDE = new HashSet<String>();
			HashSet<String> translationsES = new HashSet<String>();
			HashSet<String> translationsIT = new HashSet<String>();
			HashSet<String> translationsFR = new HashSet<String>();
			HashSet<String> translationsCA = new HashSet<String>();
			HashSet<String> translationsEN2 = new HashSet<String>();
			HashSet<String> translationsDE2 = new HashSet<String>();
			HashSet<String> translationsES2 = new HashSet<String>();
			HashSet<String> translationsIT2 = new HashSet<String>();
			HashSet<String> translationsFR2 = new HashSet<String>();
			HashSet<String> translationsCA2 = new HashSet<String>();
			
			HashSet<String> dbPediaEntries1 = new HashSet<String>();
			HashSet<String> dbPediaEntries2 = new HashSet<String>();			
			
			for(BabelSynset syn : synsets1)
			{
				if(syn.getPOS().toString().equalsIgnoreCase(posString))
				{
					dbPediaEntries1.addAll(syn.getDBPediaURIs(Language.EN));
					Collection<BabelSense> translations = syn.getTranslations().values();
					for(BabelSense bs : translations)
					{
						String toStore = bs.getLemma();
						if(bs.getLanguage().equals(Language.EN)) translationsEN.add(toStore);
						else if(bs.getLanguage().equals(Language.DE)) translationsDE.add(toStore);
						else if(bs.getLanguage().equals(Language.ES)) translationsES.add(toStore);
						else if(bs.getLanguage().equals(Language.IT)) translationsIT.add(toStore);
						else if(bs.getLanguage().equals(Language.FR)) translationsFR.add(toStore);
						else if(bs.getLanguage().equals(Language.CA)) translationsCA.add(toStore);
					}
					
				}
			}
			for(BabelSynset syn : synsets2)
			{
				if(syn.getPOS().toString().equalsIgnoreCase(posString))
				{
					dbPediaEntries2.addAll(syn.getDBPediaURIs(Language.EN));
					Collection<BabelSense> translations = syn.getTranslations().values();
					for(BabelSense bs : translations)
					{
						String toStore = bs.getLemma();						
						if(bs.getLanguage().equals(Language.EN)) translationsEN2.add(toStore);
						else if(bs.getLanguage().equals(Language.DE)) translationsDE2.add(toStore);
						else if(bs.getLanguage().equals(Language.ES)) translationsES2.add(toStore);
						else if(bs.getLanguage().equals(Language.IT)) translationsIT2.add(toStore);
						else if(bs.getLanguage().equals(Language.FR)) translationsFR2.add(toStore);
						else if(bs.getLanguage().equals(Language.CA)) translationsCA2.add(toStore);
					}
				}
			}
			
			translationsEN.retainAll(translationsEN2);
			translationsDE.retainAll(translationsDE2);
			translationsES.retainAll(translationsES2);
			translationsIT.retainAll(translationsIT2);
			translationsFR.retainAll(translationsFR2);
			translationsCA.retainAll(translationsCA2);	
			dbPediaEntries1.retainAll(dbPediaEntries2);
			
			
			int index = 0;
			features = new double[7];
			features[index++] = translationsEN.size();
			features[index++] = translationsDE.size();
			features[index++] = translationsES.size();
			features[index++] = translationsIT.size();
			features[index++] = translationsFR.size();
			features[index++] = translationsCA.size();
			features[index++] = dbPediaEntries1.size();// not useful I think !
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
		return features;
	}
	
	public static void main(String[] args)
	{				
		
			BabelNet bn = BabelNet.getInstance();
//			List<BabelSynset> senses = bn.getSynsets(Language.EN, "head", POS.NOUN);
//			for(BabelSynset syn: senses)
//			{	
////				
////				for(String uri : syn.getDBPediaURIs(Language.EN))
////				{
////					System.out.print(uri+"#");
////				}
////				System.out.println();
//				List<String> offsets = syn.getWordNetOffsets();				
//				if(offsets.size() > 0)
//				{
//					String offsetString = "";
//					for(String offset : offsets)
//					{						
//						offsetString += offset;
//					}
//					offsetString = offsetString.trim(); 
//					if(offsetString.length()>)0)
//					{
//						System.out.println(offsetString);
//						Multimap<BabelSense, BabelSense> tran = syn.getTranslations();
//						for(Map.Entry<BabelSense, Collection<BabelSense>> entry : tran.asMap().entrySet())
//						{
//							System.out.println(entry.getKey().getSensekey());
//							for(BabelSense bs : entry.getValue())
//								System.out.println(bs);
//						}
//						System.out.println("---------------------------------------------");
//					}
//				}									
//			}
			try{
			List<BabelSynset> list = bn.getBabelSynsetsFromWordNetOffset("08482271n");
			if(list == null)
			{
				System.out.println("no babelsynsets found !");
				System.exit(-1);
			}
			List<BabelSynset> senses = bn.getSynsets(Language.EN, "head", POS.NOUN, BabelSenseSource.WN);
			int count = 0;
			for(BabelSynset sense : senses)
			{			
				for(String offset : sense.getWordNetOffsets())
					System.out.println(offset);
				System.out.println(count++);
				HashSet<String> trans = new HashSet<String>();
				Collection<BabelSense> translations = sense.getTranslations().values();
				for(BabelSense bs : translations)
				{
					System.out.println(bs.toString());					
					trans.add(bs.getLemma()+"#"+bs.getLanguage());
				}
				for(String tran : trans)
					System.out.println(tran);
				System.out.println("--------------------------------------------------");				
			}			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

}
