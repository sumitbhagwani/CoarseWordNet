package krsystem.ontology.senseClustering.svm;

import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;

public class Instance {

	Synset smaller = null;
	Synset larger = null;
	int label = 0;
	
	public Instance(String posOffsetPair1, String posOffsetPair2, Dictionary dict, int labelPassed)
	{
		label = labelPassed;
		String[] pairSplit1 = posOffsetPair1.split("#");
		String[] pairSplit2 = posOffsetPair2.split("#");
		
		POS pos1 = pairSplit1[0].equalsIgnoreCase("n") ? POS.NOUN : POS.VERB;
		POS pos2 = pairSplit2[0].equalsIgnoreCase("n") ? POS.NOUN : POS.VERB;
		
		long offset1 = Long.parseLong(pairSplit1[1]);
		long offset2 = Long.parseLong(pairSplit2[1]);
		
		try{
			if(pairSplit1[1].compareTo(pairSplit2[1]) < 0)
			{
				smaller = dict.getSynsetAt(pos1, offset1);
				larger = dict.getSynsetAt(pos2, offset2);
			}
			else
			{
				larger = dict.getSynsetAt(pos1, offset1);
				smaller = dict.getSynsetAt(pos2, offset2);
			}			
		}
		catch(Exception ex)
		{
			System.out.println("posOffsetPair1 : "+posOffsetPair1);
			System.out.println("posOffsetPair2 : "+posOffsetPair2);
			ex.printStackTrace();
			System.exit(-1);
		}
	}
	
	public Instance(Synset synSmaller, Synset synLarger, int labelPassed)
	{
		smaller = synSmaller;
		larger = synLarger;
		label = labelPassed;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
