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

import com.sun.org.apache.bcel.internal.generic.FCONST;

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
	
	public HashMap<String, Double> score(String attemptFile)
	{
		HashMap<String, Double> scores = new HashMap<String, Double>();
		double attempted = 0;
		double correctFine = 0;
		double correctCoarse = 0;
		double total = instances.size();
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
				String fineAnswer = lineSplit[2];
				Integer id = clusterData.vertexToId.get(fineAnswer);
				String coarseAnswer = "";
				if(id != null)
					coarseAnswer = id.toString();
				else
					coarseAnswer = fineAnswer;
				WSDInstance wsdInstance = instances.get(instanceID);
				if(wsdInstance != null)
				{
					attempted++;
					if(wsdInstance.fineCorrect(fineAnswer))
					{
						correctFine += 1.0;
						correctCoarse += 1.0;
						baselineScore += 1.0;
					}
					else if(wsdInstance.coarseCorrect(coarseAnswer))
					{
						correctCoarse += 1.0;
						baselineScore += wsdInstance.getBaselineScore();
					}
					else
						baselineScore += wsdInstance.getBaselineScore();
				}
			}
			br.close();
						
			scores.put("attempted", attempted/total);
			
			double precision_fine = correctFine/attempted;
			scores.put("precision_fine",precision_fine);
			
			double recall_fine = correctFine/total;
			scores.put("recall_fine",precision_fine);			
			
			double fscore_fine = (2.0*precision_fine*recall_fine)/(precision_fine + recall_fine);
			scores.put("fscore_fine", fscore_fine);
			
			double precision_coarse = correctCoarse/attempted;
			scores.put("precision_coarse",precision_coarse);
			
			double recall_coarse = correctCoarse/total;
			scores.put("recall_coarse",precision_fine);			
			
			double fscore_coarse = (2.0*precision_coarse*recall_coarse)/(precision_coarse + recall_coarse);
			scores.put("fscore_coarse", fscore_coarse);
			
			double random_baseline_precision = baselineScore/attempted;
			double random_baseline_recall = baselineScore/total;
			double random_baseline_fscore = (2.0*random_baseline_precision*random_baseline_recall)/(random_baseline_precision+random_baseline_recall);
			scores.put("random_baseline", random_baseline_fscore);
			
			double improvement = fscore_coarse - random_baseline_fscore;
			scores.put("improvement", improvement);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return scores;
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
						wsdInstance.addCoarseAnswer(id.toString());
//						Set<String> cluster = clusterData.clusterList.get(id.intValue());						
//						for(String str : cluster)
//						{
//							Synset synsetStr = dictionary.getSynsetAt(POS.NOUN, Long.parseLong(str));
//							for(Word w : synsetStr.getWords())
//							{
//								String fetchedLemma = w.getLemma();
//								fetchedLemma = fetchedLemma.replaceAll(" ", "_");
//								if(fetchedLemma.compareToIgnoreCase(lemma) == 0)
//								{
//									wsdInstance.addCoarseAnswer(str);
//									break;
//								}
//							}
//						}
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
