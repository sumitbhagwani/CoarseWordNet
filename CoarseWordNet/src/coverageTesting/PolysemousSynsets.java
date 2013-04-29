package coverageTesting;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;

public class PolysemousSynsets {

	public static void coverageTest(POS pos)
	{
		String propsFile30 = "resources/file_properties.xml";
		try{
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();		
			HashSet<String> polysemousSynsets = new HashSet<String>();
			Iterator<IndexWord> it = dictionary.getIndexWordIterator(pos);
			int index = 0;
			while(it.hasNext())
			{
				IndexWord iw = it.next();
				List<Synset> senses = iw.getSenses();
				if(senses.size()>1)
				{
					for(Synset syn : senses)
					{
						polysemousSynsets.add(syn.getOffset()+"");
					}
				}			
				if(index%1000 == 0)
					System.out.println(index+" "+polysemousSynsets.size());
				index++;
			}
			System.out.println(index+" "+polysemousSynsets.size());
			dictionary.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		//coverageTest(POS.VERB); // 11322 out of 13767
//		coverageTest(POS.NOUN); // 33155 out of 82115		

	}

}
