package krsystem.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

import krsystem.StaticValues;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

public class SynsetToWordIndex {

	public static void main(String[] args) {
		String propsFile30 = StaticValues.propsFile30;
		String dataPath = StaticValues.dataPath;
		String wordNet30OffsetFile = dataPath+"xwnd/offsets.txt";
		String output = "resources/Clustering/synsetWordIndexMap/verbMap.txt";
		try{			
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();
			BufferedReader br = new BufferedReader(new FileReader(new File(wordNet30OffsetFile)));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output)));
			String line;
			POS pos = POS.VERB;
			while((line=br.readLine())!=null)
			{
				if(line.endsWith("v")){
					String offsetString = line.split("-")[0];
					long offset = Long.parseLong(offsetString);
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
						boolean found = false;
						for(Synset s : syns)
						{
							if(s.equals(syn))
							{
								lemma = lemma.replace(" ", "_");
								bw.write(offsetString+" "+lemma + " "+ count+"\n");
								found = true;
								break;
							}					
							count++;
						}
						//if reached here, throw error
						if(!found)
							System.out.println("Error in getting lemma and sense number : "+offsetString);
					}
					else
						System.out.println("Synset not found : "+offsetString);
				}
			}
			br.close();
			bw.close();
			dictionary.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

	}

}
