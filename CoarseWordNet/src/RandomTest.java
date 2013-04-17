import java.io.FileInputStream;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;


public class RandomTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String propsFile30 = "resources/file_properties.xml";
		try{
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();
			IndexWord iw = dictionary.getIndexWord(POS.NOUN, "stress");
			for(Synset syn : iw.getSenses())
			{
				System.out.println(syn);
			}
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

	}

}
