package verbNetExperiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

import krsystem.StaticValues;

public class VerbNetMapConstruction {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String verbNetMapPath = StaticValues.verbNetPath;
		String propsFile30 = "resources/file_properties.xml";
		String line = "";
		try{						
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();
			BufferedReader br = new BufferedReader(new FileReader(new File(verbNetMapPath+"verbNetExtractedMap")));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(verbNetMapPath+"verbNetExtractedMap.synsets")));			
			while((line=br.readLine()) != null)
			{
				String[] lineSplit = line.split("\\s+");
				String classVal = lineSplit[0];
				String senseKey = lineSplit[1].toLowerCase()+"::";
				if(senseKey.startsWith("?"))
					senseKey = senseKey.substring(1);
				Word word = dictionary.getWordBySenseKey(senseKey);
				if(word != null)
				{
					long offsetLong = word.getSynset().getOffset();
					String offset = String.format("%08d", offsetLong);
					bw.write(classVal+" "+offset+"\n");
				}
				else
				{
					System.out.println("Sensekey not found : "+senseKey);
				}
					
			}
			bw.close();
			dictionary.close();
			br.close();			
		}
		catch(Exception ex)
		{
			System.out.println(line);
			ex.printStackTrace();
		}

	}

}
