package krsystem.ontology.senseClustering;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class ProcessSentiWordNet {	
	
	public static void separatePOSWise(String filePath, String outputPath, String posString)
	{
		String line;
		try{
			Scanner sc = new Scanner(new File(filePath));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPath+"SentiWordNet."+posString)));
			while(sc.hasNextLine())
			{
				line = sc.nextLine();
				if(line.charAt(0) == '\n' || line.charAt(0) == '#')
					continue;
				String[] lineSplit = line.split("\\s+");
				String pos = lineSplit[0];
				if(pos.equalsIgnoreCase(posString))
				{
					String offset = lineSplit[1];
					double posScore = Double.parseDouble(lineSplit[2]);
					double negScore = Double.parseDouble(lineSplit[3]);
					double objective = 1 - (posScore+negScore);
					bw.write(posString+"#"+offset+" "+posScore+" "+negScore+" "+objective+"\n");					
				}
			}
			sc.close();
			bw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	public static void main(String[] args) {
		String path = "/home/sumitb/Data/SentiWordNet/SentiWordNet_3.0.txt";
		String outputFolder = "/home/sumitb/Data/SentiWordNet/";
//		separatePOSWise(path, outputFolder, "n");
		separatePOSWise(path, outputFolder, "v");

	}

}
