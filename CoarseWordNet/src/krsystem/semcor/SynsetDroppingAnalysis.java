package krsystem.semcor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Scanner;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

public class SynsetDroppingAnalysis {

	public static void genSynsetKey(String keyFile, String outputFile)
	{
		String propsFile30 = "resources/file_properties.xml";		
		String line;
		int i = 0;
		try{		
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFile)));
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();
			Scanner sc = new Scanner(new File(keyFile));
			while(sc.hasNextLine())
			{
				line = sc.nextLine().trim();
				if(line.endsWith("#n"))
				{
					i++;
					String[] lineSplit = line.split("\\s+");
					StringBuilder outputLine = new StringBuilder(); 					
					for(int index=2; index<lineSplit.length; index++)
					{
						if(lineSplit[index].equalsIgnoreCase("!!"))
							break;
						else
						{
							Word w = dictionary.getWordBySenseKey(lineSplit[index].toLowerCase());
							long offset = w.getSynset().getOffset();
							String offsetString = String.format("%08d", offset);
							outputLine.append(" "+offsetString);
						}						
					}
					String stringBuilt = outputLine.toString();
					if(stringBuilt.length()>0)
					{
						bw.write(lineSplit[0]+" "+lineSplit[1]+stringBuilt+"\n");
					}
					else
						System.out.println(lineSplit[0]+" "+lineSplit[1]);
					
				}
				System.out.println("Noun found : " +i);
			}
			bw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

	}
	
	public static void evaluateDropping(String synsetKeyFile, String scoresFile, double threshold)
	{
		String line;
		HashMap<String, Double> synsetScores = new HashMap<String, Double>();
		double[] affected = new double[5];
		double[] instancesNum = new double[5];
		try{
			// Read Synset Scores
			BufferedReader br = new BufferedReader(new FileReader(new File(scoresFile)));
			while((line=br.readLine()) != null)
			{
				String[] lineSplit = line.split("\\s+");
				long offset = Long.parseLong(lineSplit[0]);
				String offsetString = String.format("%08d", offset);
				double score = Double.parseDouble(lineSplit[1]);
				synsetScores.put(offsetString, score);
			}
			br.close();
						
			br = new BufferedReader(new FileReader(new File(synsetKeyFile)));
			while((line=br.readLine()) != null)
			{
				String[] lineSplit = line.split("\\s+");				
				String doc = lineSplit[0];
				//String instanceId = lineSplit[1];				
				double localAffectance = 0;
				double correctAnswers = 0;
				for(int index=2; index<lineSplit.length; index++)
				{
					double score = synsetScores.get(lineSplit[index]);
					correctAnswers ++;
					if(score <= threshold)
						localAffectance++;
				}		
				double dropAffect = localAffectance/correctAnswers;
				
				if(doc.equalsIgnoreCase("d001"))
				{
					affected[0] += dropAffect;
					instancesNum[0] ++;
				}
				else if(doc.equalsIgnoreCase("d002")) {
					affected[1] += dropAffect;
					instancesNum[1] ++;
				}
				else if(doc.equalsIgnoreCase("d003")) {
					affected[2] += dropAffect;
					instancesNum[2] ++;
				}
				else if(doc.equalsIgnoreCase("d004")) {
					affected[3] += dropAffect;
					instancesNum[3] ++;
				}
				else if(doc.equalsIgnoreCase("d005")) {
					affected[4] += dropAffect;
					instancesNum[4] ++;
				}
			}
			br.close();
			
			for(int i=0; i<5; i++)
			{
				System.out.println(i+" "+instancesNum[i]+" "+affected[i]);
			}
			
			double wsj = (instancesNum[0]*affected[0] + instancesNum[1]*affected[1] + instancesNum[2]*affected[2]) / (instancesNum[0] + instancesNum[1] + instancesNum[2]);
			double domain = (instancesNum[3]*affected[3] + instancesNum[4]*affected[4]) / (instancesNum[3] + instancesNum[4]);
			System.out.println(wsj+" "+domain);
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String senseKeyFile = "resources/Clustering/SynsetDroppingExperiment/dataset21.test.key";
		String synsetKeyFile = "resources/Clustering/SynsetDroppingExperiment/dataset30.synsets.test.key";
		String scores1 = "resources/Clustering/SynsetDroppingExperiment/NounScores/scores1.0Sorted.txt";
		String scores2 = "resources/Clustering/SynsetDroppingExperiment/NounScores/scorestrue1.0Sorted.txt";
//		genSynsetKey(keyFile, outputFile);
		evaluateDropping(synsetKeyFile, scores1, 0.06);
		
	}

}
