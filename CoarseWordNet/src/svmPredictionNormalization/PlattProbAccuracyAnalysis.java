package svmPredictionNormalization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

public class PlattProbAccuracyAnalysis {


	public static void main(String[] args) {
		String synsetSimilarityFilePath = "resources/Clustering/PopulatingDB/simValuesSVMTransformed.noun";
		String path = "resources/Clustering/BinaryClassificationData/";
		String pathPos = path+"nounPositiveCleaned";
		String pathNeg = path+"nounNegativeCleaned";
		String[] files = {pathPos, pathNeg};
		String outputPath = "resources/Clustering/PlattProbExperiment/AccuracyEstimation/svmProbAndClass.txt";
		
		String line;
		HashMap<String, Double> svmProb = new HashMap<String, Double>();
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(synsetSimilarityFilePath)));
			
			while((line=br.readLine()) != null)
			{
				String[] lineSplit = line.split("\\s+");
				svmProb.put(lineSplit[0]+"#"+lineSplit[1], Double.parseDouble(lineSplit[2])); // smaller#larger
			}
			br.close();					
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPath)));
			for(String file : files)
			{
				br = new BufferedReader(new FileReader(new File(file)));				
				while((line=br.readLine()) != null)
				{
					//n#08656893 n#01056411 1
					String[] lineSplit = line.split("\\s+");
					String offset0 = lineSplit[0].split("#")[1];
					String offset1 = lineSplit[1].split("#")[1];
					int classVal = Integer.parseInt(lineSplit[2]);
					Double value = svmProb.get(offset0+"#"+offset1);
					if(value == null)
						value = svmProb.get(offset1+"#"+offset0);
					if(value!=null)
						bw.write(classVal+" "+value.doubleValue()+"\n");
					else
						System.out.println("Error !");
				}				
				br.close();		
			}
			bw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}

}
