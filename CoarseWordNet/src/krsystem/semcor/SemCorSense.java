package krsystem.semcor;

import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;

/**
 * @author sumitb
 *
 */
public class SemCorSense 
{
	private String word;
	private String pos;
	private String lemma;
	private String lexsn;
	private int wnsn;    //always > 0 for valid senses
	
	public SemCorSense(String wordPassed, String posPassed, String lemmaPassed, String lexsnPassed, int wnsnPassed)
	{
		word = wordPassed;
		pos = posPassed;
		lemma = lemmaPassed;
		lexsn = lexsnPassed.replaceAll("\\([a|p]\\):", ":").toLowerCase();
		wnsn = wnsnPassed;
	}	
	
	public Synset getSense(Dictionary dictionary)
	{		
		try
		{
			POS posObtained = this.getPOS(); // check lexsn
			return dictionary.lookupIndexWord(posObtained, lemma).getSenses().get(wnsn-1);//since arraylist has indices from 0 whereas we have them from 1
		}
		catch(Exception ex)
		{
			//System.out.println("Identifying sense of "+this.toString());
			ex.printStackTrace();
		}
		return null;
	}
	
	public String getSenseKey()
	{		
		String senseKey = lemma+"%"+lexsn;
		return senseKey.toLowerCase();
	}
	
	public POS getPOS()
	{
		if(pos.substring(0,2).equalsIgnoreCase("NN"))
			return POS.NOUN;
		else if (pos.substring(0, 1).equalsIgnoreCase("V"))
			return POS.VERB;
		else if(pos.substring(0, 2).equalsIgnoreCase("JJ"))
			return POS.ADJECTIVE;
		else if(pos.substring(0, 2).equalsIgnoreCase("RB"))
			return POS.ADVERB;
		else
		{
			System.out.println("Error in finding POS of "+this.toString());
			return null;
		}
			
	}
	
	public String getWord()
	{
		return word;
	}

	public String getLemma()
	{
		return lemma;
	}
	
	public String getPos()
	{
		return pos;
	}
	
	public String getLexsn()
	{
		return lexsn;
	}
	
	public int getWnsn()
	{
		return wnsn;	
	}
	
	@Override
	public boolean equals(Object otherSenseObject)
	{
		SemCorSense otherSense = (SemCorSense) otherSenseObject;
		boolean toRet = true;
		toRet = toRet && word.equalsIgnoreCase(otherSense.getWord()); // You can remove word -- this will remove inflections -- because word need not be the same		
		toRet = toRet && pos.equalsIgnoreCase(otherSense.getPos());
		toRet = toRet && lemma.equalsIgnoreCase(otherSense.getLemma());
		toRet = toRet && lexsn.equalsIgnoreCase(otherSense.getLexsn());
		toRet = toRet && (wnsn==otherSense.getWnsn());
		return toRet;
	}
	
	@Override
	public int hashCode() {
		return this.pos.hashCode()+ this.getLemma().hashCode() +  this.lexsn.hashCode(); // no hasCode() function for int !
	}
	
	public String toString()
	{
		return word + "#" + pos + "#" + lemma + "#" + lexsn + "#" + wnsn ;
	}
	
}
