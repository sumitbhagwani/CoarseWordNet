package simRank;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class PartitionSimRankFiles {
	
	public static void main(String[] args) {
		String filePath = "/home/sumitb/Data/simrankExperiment/Unsupervised/"; 
		String file = filePath+"simrankMatrixIteration0";
//		String[] outputFiles = {file+"0001", file+"0102",file+"0203",file+"0304",file+"0405",file+"0506"};
//		String[] outputFiles = {file+"000005", file+"005006",file+"006007",file+"007008",file+"008009",file+"009010"};
		String[] outputFiles = {file+"000001", file+"001002",file+"002003",file+"003004",file+"004005"};
		double[] thresholds = {0.01,0.02,0.03,0.04,0.05};
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(file)));
			BufferedWriter bw0 = new BufferedWriter(new FileWriter(new File(outputFiles[0])));
			BufferedWriter bw1 = new BufferedWriter(new FileWriter(new File(outputFiles[1])));
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File(outputFiles[2])));
			BufferedWriter bw3 = new BufferedWriter(new FileWriter(new File(outputFiles[3])));
			BufferedWriter bw4 = new BufferedWriter(new FileWriter(new File(outputFiles[4])));
//			BufferedWriter bw5 = new BufferedWriter(new FileWriter(new File(outputFiles[5])));
			
			String line;
			while((line=br.readLine()) != null)
			{
				String[] lineSplit = line.split("\\s+");
				double simvalue = Double.parseDouble(lineSplit[2]);
//				if(simvalue < 0.1)
//					bw0.write(line+"\n");
//				else if(simvalue < 0.2)
//					bw1.write(line+"\n");
//				else if(simvalue < 0.3)
//					bw2.write(line+"\n");
//				else if(simvalue < 0.4)
//					bw3.write(line+"\n");
//				else if(simvalue < 0.5)
//					bw4.write(line+"\n");
//				else if(simvalue <= 0.6)
//					bw5.write(line+"\n");
				if(simvalue < thresholds[0])
					bw0.write(line+"\n");
				else if(simvalue < thresholds[1])
					bw1.write(line+"\n");
				else if(simvalue < thresholds[2])
					bw2.write(line+"\n");
				else if(simvalue < thresholds[3])
					bw3.write(line+"\n");
				else if(simvalue < thresholds[4])
					bw4.write(line+"\n");
//				else if(simvalue < 0.10)
//					bw5.write(line+"\n");
			}
			
			br.close();
			bw0.close();
			bw1.close();
			bw2.close();
			bw3.close();
			bw4.close();
//			bw5.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

	}

}
