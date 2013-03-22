package krsystem.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.HashMap;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;

public class SynsetToWordIndexMap {

	HashMap<String, String> mapping = new HashMap<String, String>();
	
	public SynsetToWordIndexMap(String mappingPath) {
		try{
			System.out.println("Loading SynsetToWordIndexMap : "+mappingPath);
			BufferedReader br = new BufferedReader(new FileReader(new File(mappingPath)));
			String line;
			while((line=br.readLine())!=null)
			{
				String[] lineSplit = line.split("\\s+");
				String key = lineSplit[0];
				String value = lineSplit[1]+"#"+lineSplit[2];
				mapping.put(key, value);
			}
			br.close();
			System.out.println("Loaded SynsetToWordIndexMap...");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();			
		}
	}
	
	public OrderedPair<String, Integer> getWordIndexPair(Synset syn)
	{
		long offsetLong = syn.getOffset();
		String offsetString = String.format("%08d", offsetLong);	
		String value = mapping.get(offsetString);
		if(value == null)
			return null;
		String[] valueSplit = value.split("#");
		String word = valueSplit[0];
		Integer index = Integer.parseInt(valueSplit[1]);
		return new OrderedPair<String, Integer>(word, index);
	}
	
	public OrderedPair<String, Integer> getWordIndexPair(String offsetString)
	{
		String value = mapping.get(offsetString);
		if(value == null)
			return null;
		String[] valueSplit = value.split("#");
		String word = valueSplit[0];
		Integer index = Integer.parseInt(valueSplit[1]);
		return new OrderedPair<String, Integer>(word, index);
	}
	
	public String getWordIndexPairString(String offsetString)
	{
		String value = mapping.get(offsetString);
		return value;
	}
	
	public static void main(String[] args) {		
		
	}

}
