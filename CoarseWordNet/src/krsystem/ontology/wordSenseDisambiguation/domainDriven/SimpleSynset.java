package krsystem.ontology.wordSenseDisambiguation.domainDriven;

import net.sf.extjwnl.data.POS;

public class SimpleSynset {
	String pos;
	String word;
	String wnsn;
	String offset;
	double wordnetVersion;
	int index;
	
	public SimpleSynset(String pos, String word, String wnsn, String offset, double wordnetVersion) {
		this.pos = pos;
		this.offset = offset;
		this.wnsn = wnsn;
		this.word = word;
		this.wordnetVersion = wordnetVersion;
		this.index = -1;
	}
	
	public SimpleSynset(String pos, String word, String wnsn, String offset, double wordnetVersion, int index) {
		this.pos = pos;
		this.offset = offset;
		this.wnsn = wnsn;
		this.word = word;
		this.wordnetVersion = wordnetVersion;
		this.index = index;
	}
	
	public boolean equals(Object passed)
	{
		
		//return this.pos.equals(passed.pos) && this.word.equals(passed.word) && this.wnsn.equals(passed.wnsn) && this.offset.equals(passed.offset) && this.wordnetVersion==passed.wordnetVersion;
		return this.signature().equalsIgnoreCase(((SimpleSynset)passed).signature());
	}
	
	public String signature()
	{		
		return offset+"-"+pos;
	}
	
	public int hashCode()
	{
		return signature().hashCode();
	}	
	
	public String toString()
	{
		return word+"-"+signature();
	}
	
	public POS getPOS()
	{
		return pos.equals("v") ? POS.VERB : POS.NOUN;
	}
}
