package connectedComponentAnalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

public class Analysis {

	public static void main(String[] args) {
//		String[] files = {"resources/Clustering/PopulatingDB/simValuesSVM.noun"};
//		String[] files = {"resources/Clustering/PopulatingDB/simValuesSVMTransformed.noun"};//, "/home/sumitb/Desktop/simrankMatrixIterationSVMTransformedSemisupervised8-3-6.synsets"};		
		String[] files = {"resources/Clustering/SimilarityModels/Scaled0.7/simrankMatrixIterationSVMProbScaled9-5-7.synsets", "resources/Clustering/PopulatingDB/svmProbScaled/simValuesSVMTransformed.nounScaled0.7"};

		double minthreshold = 0.5;
		double maxthreshold = 0.7;
		String answerKey = "resources/Clustering/Senseval3/NounExtractedFiles/EnglishAW.test.key.synsets.lemmas";		
		
		String attemptPath = "resources/Clustering/Senseval3/NounExtractedFiles/";
		String[] attemptFiles = {attemptPath+"GAMBL.synsets", attemptPath+"SenseLearner.synsets", attemptPath+"kuaw.synsets", attemptPath+"IRST-DDD-00.synsets",};
		
		String scoresFile = attemptPath+"results/testScaled0.7.txt";
		String[] hashKeys = {"precision_fine", "random_baseline","recall_coarse","fscore_coarse","fscore_fine","improvement","precision_coarse","recall_fine","attempted"};
		double[] bestImprovement = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
		double[] bestImprovementThreshold = {0,0,0,0};
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(scoresFile)));
			for(double threshold = minthreshold; threshold <= maxthreshold; threshold += 0.01)
			{
				bw.write(threshold+"");
				System.out.println("Finding Connected Components ...");
				ConnectedComponents components = new ConnectedComponents(files, threshold);
				System.out.println("Connected Components found ...");
				
				System.out.println("Constructing Scorer ...");				
				WSDScorer scorer = new WSDScorer(components, answerKey);
				System.out.println("Scorer constructed  ...");
				
				System.out.println("Evaluating using scorer ...");	
				HashMap<String, Double>[] scoresArray = new HashMap[4];
				scoresArray[0] = scorer.score(attemptFiles[0]);						
				scoresArray[1] = scorer.score(attemptFiles[1]);
				scoresArray[2] = scorer.score(attemptFiles[2]);
				scoresArray[3] = scorer.score(attemptFiles[3]);
				for(String key: hashKeys)
				{
					for(int index=0; index<4;index++)
						bw.write(" "+scoresArray[index].get(key));//+" "+scoresArray[1].get(key)+" "+scoresArray[2].get(key)+" "+scoresArray[3].get(key));
				}
				for(int index=0; index<4;index++)
				{
					double improvement = scoresArray[index].get("improvement");
					if(improvement > bestImprovement[index])
					{
						bestImprovement[index] = improvement;
						bestImprovementThreshold[index] = threshold;
					}
				}
				bw.write("\n");
			}
			bw.close();
			System.out.println("-----------------------------------------------------------------");
			for(int index=0; index<4;index++)
				System.out.println(index+" "+bestImprovementThreshold[index]+" "+bestImprovement[index]);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}		
	}
}
