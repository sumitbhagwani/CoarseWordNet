package krsystem.ontology.senseClustering;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class UPCMappings {

	// UPC Mappings
		public static HashMap<String, ArrayList<String>> loadVersionMapping(String mapFile)
		{
			HashMap<String, ArrayList<String>> versionMapping = new HashMap<String, ArrayList<String>>();
			try{
				Scanner sc = new Scanner(new File(mapFile));
				while(sc.hasNextLine())
				{
					String line = sc.nextLine();
					String[] linesplit = line.split("\\s+");
					String offsetOld = linesplit[0];
					ArrayList<String> mappedOffsets = new ArrayList<String>();
					for(int i=1; i<linesplit.length;i=i+2)
					{
						String offset = linesplit[i];
						double score = Double.parseDouble(linesplit[i+1]);
						if(score>0.5)
						{
							mappedOffsets.add(offset);
						}
					}
					if(mappedOffsets.size() > 0)
						versionMapping.put(offsetOld, mappedOffsets);
				}
				sc.close();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				System.exit(-1);
			}
			return versionMapping;
		}
}
