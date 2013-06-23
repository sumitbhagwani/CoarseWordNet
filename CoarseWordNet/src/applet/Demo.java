package applet;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import connectedComponentAnalysis.Analysis;
import connectedComponentAnalysis.ConnectedComponents;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;

public class Demo {

	private static final Map<String, String> scaledSimValuesPaths = createScaledSimValuesPathMap();
	private static final Map<String, String> simRankValuesPaths = createSimRankValuesPathMap();

    private static Map<String, String> createScaledSimValuesPathMap() {
        Map<String, String> result = new HashMap<String, String>();
        result.put("0.6", "resources/Clustering/PopulatingDB/svmProbScaled/simValuesSVMTransformed.nounScaled0.6");
        result.put("0.7", "resources/Clustering/PopulatingDB/svmProbScaled/simValuesSVMTransformed.nounScaled0.7");
        result.put("0.8", "resources/Clustering/PopulatingDB/svmProbScaled/simValuesSVMTransformed.nounScaled0.8");
        return Collections.unmodifiableMap(result);
    }
	    
    private static Map<String, String> createSimRankValuesPathMap() {
        Map<String, String> result = new HashMap<String, String>();
        result.put("0.6", "resources/Clustering/SimilarityModels/Scaled0.6/simrankMatrixIterationSVMProbScaled8-3-6.synsets");
        result.put("0.7", "resources/Clustering/SimilarityModels/Scaled0.7/simrankMatrixIterationSVMProbScaled9-4-7.synsets");
        result.put("0.8", "resources/Clustering/SimilarityModels/Scaled0.8/simrankMatrixIterationSVMProbScaled8-4-8.synsets");       
        return Collections.unmodifiableMap(result);
    }
    
	/** decayConstantC has to be 0.6, 0.7 or 0.8
	 *  threshold ranges are as follows:
	 *  0.6 -> 0.3-0.6
	 *  0.7 -> 0.4-0.7
	 *  0.8 -> 0.4-0.8
	*/
	public static Collection<HashSet<String>> getCoarseSenses(String word, POS pos, String decayConstantC, double threshold) // for now pos == noun
	{					
		String[] files = {scaledSimValuesPaths.get(decayConstantC), simRankValuesPaths.get(decayConstantC)};
		List<String> scores = Analysis.readFiles(files);
		
		System.out.println("Finding Connected Components ...");
		ConnectedComponents components = new ConnectedComponents(scores, threshold);
		System.out.println("Connected Components found ...");
		
		HashMap<String, HashSet<String>> clusters = new HashMap<String, HashSet<String>>();
		
		String propsFile30 = "resources/file_properties.xml";
		try{
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();
			IndexWord iw = dictionary.getIndexWord(pos, word);
			Set<String> senses = new HashSet<String>();
			for(Synset syn : iw.getSenses())
			{
				long offsetLong = syn.getOffset();
				String offsetString = String.format("%08d", offsetLong);
				senses.add(offsetString);
				Integer offsetId = components.getVertexId(offsetString);
				System.out.println(offsetId);
				if(offsetId == null)
				{
					HashSet<String> senseCluster = new HashSet<String>();
					senseCluster.add(offsetString);
					clusters.put(offsetString, senseCluster);
				}
				else
				{
					if(clusters.containsKey(offsetId))
					{
						clusters.get(offsetId).add(offsetString);
					}
					else
					{
						HashSet<String> senseCluster = new HashSet<String>();
						senseCluster.add(offsetString);
						clusters.put(offsetId.toString(), senseCluster);
					}
				}
			}
			dictionary.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
				
		return clusters.values();
	}
	
	public static Collection<HashSet<String>> getCoarseSensesNoun(String word, String decayConstantC, double threshold)
	{
		return getCoarseSenses(word, POS.NOUN, decayConstantC, threshold);
	}
	
	public static void main(String[] args) {
		Collection<HashSet<String>> coarseSenses = getCoarseSensesNoun("word", "0.7", 0.65);
		System.out.println("**********************************************");
		for(HashSet<String> coarseSense : coarseSenses)
		{
			for(String sense:coarseSense)
			{
				System.out.println(sense);
			}
			System.out.println("**********************************************");
		}		
	}

}
