package simRankEZGraph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Iterator;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import net.sf.extjwnl.dictionary.Dictionary;
import krsystem.StaticValues;

public class ExtractWNRelations {

	public static void main(String[] args) {
		String propsFile30 = StaticValues.propsFile30;
		try{
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();
			Iterator<Synset> iterator = dictionary.getSynsetIterator(POS.NOUN);
			String wn30Relations = StaticValues.wnRelationsPath;
			String relation = "hyponym";
			double relationWeight = 1.0;
			
			int i=0;
			int count = 0;
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(wn30Relations+relation)));
			while(iterator.hasNext())
			{
				Synset synset = iterator.next();
				String synsetOffset = String.format("%08d", synset.getOffset());
				PointerTargetNodeList ptnl = PointerUtils.getDirectHyponyms(synset);
				for(PointerTargetNode ptn : ptnl)
				{
					Synset syn = ptn.getSynset();
					String synOffset = String.format("%08d", syn.getOffset());
					bw.write(synsetOffset+"\t"+synOffset+"\t"+relationWeight+"\n");
					count++;
				}									
				i++;
				if(i%1000 == 0)
					System.out.println(i+" : "+count);
			}
			System.out.println(i+" : "+count);
			bw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		

	}

}
