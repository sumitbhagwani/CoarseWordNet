package connectedComponentAnalysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import krsystem.StaticValues;
import krsystem.ontology.senseClustering.UPCMappings;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

public class Senseval3Processing {

	public static void processing1(String attemptFile, boolean withLemma){
		String path = "resources/Clustering/Senseval3/NounExtractedFiles/";
		String propsFile171 = "resources/file_properties_1_7_1.xml";
		String upcMapping171To30 = StaticValues.dataPath+"SenseMappings/mappings-upc-2007/mapping-171-30/wn171-30.noun";		
		String inputFile = path+attemptFile;
		String outputFile = path+attemptFile+".synsets";
		if(withLemma)
			outputFile += ".lemmas";
		HashMap<String, ArrayList<String>> mapping = UPCMappings.loadVersionMapping(upcMapping171To30);
		try{			
			JWNL.initialize(new FileInputStream(propsFile171));
			Dictionary dictionary = Dictionary.getInstance();
			BufferedReader br = new BufferedReader(new FileReader(new File(inputFile)));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFile)));
			String line;
			int lineNo = 0;
			while((line = br.readLine()) != null)
			{
				lineNo ++;
				String[] lineSplit = line.split("\\s+");
				String outputLine = lineSplit[0]+" "+lineSplit[1]; 	
				boolean wrote = false;
				for(int i=2; i<lineSplit.length; i++)
				{
					String senseKey = lineSplit[i].toLowerCase();
					String lemma = senseKey.split("%")[0];
					Word w = dictionary.getWordBySenseKey(senseKey);
					if(w == null)
					{
						System.out.println(senseKey);
						System.out.println(lineNo);
						IndexWord iw = dictionary.getIndexWord(POS.NOUN, lemma);
						for(Synset syn : iw.getSenses())
						{
							int index = syn.indexOfWord(lemma);
							Word w2 = syn.getWords().get(index);
							System.out.println(w2.getSenseKey());
						}
					}
					Synset syn = w.getSynset();
					long offsetLong = syn.getOffset();						
					String offset = String.format("%08d", offsetLong);
					ArrayList<String> mappedOffsets = mapping.get(offset);
					if(mappedOffsets != null)
					{
						for(String mappedOffset : mappedOffsets)
						{
							wrote = true;
							if(withLemma)
								outputLine = outputLine + " " +lemma+"#"+mappedOffset;
							else
								outputLine = outputLine + " " +mappedOffset;
						}
					}
					else
					{						
						System.out.println(lineNo);
						System.out.println(senseKey);
						System.out.println(syn);
						HashSet<String> allowed = new HashSet<String>();
						allowed.add("something");
						allowed.add("anything");
						if(! allowed.contains(lemma))
							System.exit(-1);
					}
				}		
				outputLine = outputLine + "\n";
				if(wrote)
					bw.write(outputLine);
			}
			br.close();
			bw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String attemptFile = "EnglishAW.test.key";
		processing1(attemptFile, true);		
	}

}
