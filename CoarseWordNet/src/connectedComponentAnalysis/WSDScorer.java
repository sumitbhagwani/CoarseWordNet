package connectedComponentAnalysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import krsystem.StaticValues;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

public class WSDScorer {

	ConnectedComponents clusterData;
	HashMap<String, WSDInstance> instances;
	
	public WSDScorer(ConnectedComponents clustersPassed) {
		clusterData = clustersPassed;
		instances = new HashMap<String, WSDInstance>();
	}	
	
	public void addWSDInstance(WSDInstance instance)
	{
		instances.put(instance.instanceId, instance);
	}
	
	public void score(String attemptFile)
	{
		int attempted = 0;
		int correctFine = 0;
		int correctCoarse = 0;
		int total = instances.size();
		double baselineScore = 0;
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(attemptFile)));
			String line;
			while((line=br.readLine()) != null)
			{
				String[] lineSplit = line.split("\\s+");
				//lineSplit[0] is document id
				//lineSplit[1] is instance id
				//lineSplit[2] is the answer marked by the system
				String instanceID = lineSplit[1];
				String answerMarked = lineSplit[2];
				WSDInstance wsdInstance = instances.get(instanceID);
				if(wsdInstance != null)
				{
					attempted++;
					if(wsdInstance.fineCorrect(answerMarked))
					{
						correctFine++;
						correctCoarse++;
						baselineScore += 1.0;
					}
					else if(wsdInstance.coarseCorrect(answerMarked))
					{
						correctCoarse++;
						baselineScore += wsdInstance.getBaselineScore();
					}
				}
			}
			br.close();
			
			System.out.println("Attempted : "+ attempted +" / "+total);
			System.out.println("Precision Fine : "+ correctFine + " / "+attempted);
			System.out.println("Recall Fine : "+ correctFine + " / "+total);
			System.out.println("Precision Coarse : "+ correctCoarse + " / "+attempted);
			System.out.println("Recall Coarse : "+ correctCoarse + " / "+total);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public WSDScorer(ConnectedComponents clustersPassed, String answerKeyFile) {
		clusterData = clustersPassed;
		instances = new HashMap<String, WSDInstance>();
		String propsFile30 = StaticValues.propsFile30;
		try{				
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();
			BufferedReader br = new BufferedReader(new FileReader(new File(answerKeyFile)));			
			String line;
			while((line = br.readLine()) != null)
			{
				String[] lineSplit = line.split("\\s+");
				if(lineSplit.length < 2)
					System.out.println("Error in line : "+line);				
				String lemma = lineSplit[2].split("#")[0];
				
				WSDInstance wsdInstance = new WSDInstance(lineSplit[0], lineSplit[1], lemma);
				
				// Map the fine senses to coarse senses by adding other fine senses in the same cluster
				for(int index = 2; index<lineSplit.length; index++)
				{					
					String offset = lineSplit[index].split("#")[1];
					wsdInstance.addFineAnswer(offset);
					Integer id = clusterData.vertexToId.get(offset);
					if(id == null)
					{						
						wsdInstance.addCoarseAnswer(offset);
					}
					else
					{
						Set<String> cluster = clusterData.clusterList.get(id.intValue());						
						for(String str : cluster)
						{
							Synset synsetStr = dictionary.getSynsetAt(POS.NOUN, Long.parseLong(str));
							for(Word w : synsetStr.getWords())
							{
								String fetchedLemma = w.getLemma();
								fetchedLemma = fetchedLemma.replaceAll(" ", "_");
								if(fetchedLemma.compareToIgnoreCase(lemma) == 0)
								{
									wsdInstance.addCoarseAnswer(str);
									break;
								}
							}
						}
					}				
				}
				
				// Evaluate the random baseline for the word over this clustering
				HashMap<Integer, Integer> counts = new HashMap<Integer, Integer>();
				IndexWord iw = dictionary.getIndexWord(POS.NOUN, lemma);
				int numSenses = 0;
				int numClusters = 0;
				for(long offsetLong : iw.getSynsetOffsets())
				{
					String offset = String.format("%08d", offsetLong);
					Integer id = clusterData.vertexToId.get(offset);
					if(id != null)
					{
						Integer value = counts.get(id);
						if(value == null)
							counts.put(id, 1);
						else
							counts.put(id, value.intValue()+1);
					}
					else
						numClusters++;
					numSenses++;
				}
								
				double baselineScore = 0;
				for(Map.Entry<Integer, Integer> entry : counts.entrySet())
				{
					int c = entry.getValue().intValue();
					baselineScore += c*(c-1);
					numClusters++;
				}
				
				if(numSenses == 1) // monosemous
					baselineScore = 1.0;
				else if (numSenses == numClusters) // polysemous but  no clustering
					baselineScore = 0.0;
				else
					baselineScore = baselineScore / (numSenses * (numSenses-1));
				
				wsdInstance.setBaselineScore(baselineScore);
				instances.put(wsdInstance.instanceId, wsdInstance);
			}			
			br.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

	}
	
}
