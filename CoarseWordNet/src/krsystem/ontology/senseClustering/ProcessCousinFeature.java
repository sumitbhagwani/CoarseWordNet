package krsystem.ontology.senseClustering;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class ProcessCousinFeature {
	
	public static void main(String[] args) {
		// makes sense only for nouns
		String cousinFile16 = "/home/sumitb/Data/cousinFeature/cousin16.tops";
		String cousinFile30 = "/home/sumitb/Data/cousinFeature/cousin30.tops.noun";
		String versionMapPathNoun = "/home/sumitb/Data/SenseMappings/mappings-upc-2007/mapping-16-30/wn16-30.noun";		
		HashMap<String, ArrayList<String>> versionMappingNoun = UPCMappings.loadVersionMapping(versionMapPathNoun);
		try{
			Scanner sc = new Scanner(new File(cousinFile16));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(cousinFile30)));
			String line;
			while(sc.hasNextLine())
			{
				line = sc.nextLine();
				String[] lineSplit = line.toLowerCase().trim().split("\\s+");
				List<String> offsets1 = versionMappingNoun.get(lineSplit[0]);
				List<String> offsets2 = versionMappingNoun.get(lineSplit[1]);
				if(offsets1!=null && offsets2!=null && offsets1.size()>0 && offsets2.size()>0)
				{
					for(String offset1 : offsets1)
						for(String offset2 : offsets2)
							bw.write(offset1+" "+offset2+"\n");
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

}
