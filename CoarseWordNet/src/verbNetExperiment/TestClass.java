package verbNetExperiment;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import edu.mit.jverbnet.data.FrameType;
import edu.mit.jverbnet.data.IFrame;
import edu.mit.jverbnet.data.IMember;
import edu.mit.jverbnet.data.IVerbClass;
import edu.mit.jverbnet.data.IWordnetKey;
import edu.mit.jverbnet.data.WordnetKey;
import edu.mit.jverbnet.index.IVerbIndex;
import edu.mit.jverbnet.index.VerbIndex;
import krsystem.StaticValues;

public class TestClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String pathToVerbNet = StaticValues.verbNetPath;
		URL url;
		try {
			url = new URL("file", null, pathToVerbNet);
			IVerbIndex index = new VerbIndex(url);
			index.open();
			int count = 0;
			String senseKey = "alternate%2:30:01";
			String[] senseKeySplit = senseKey.split("[%:]+");
			WordnetKey wnkey = new WordnetKey(senseKeySplit[0], Integer.parseInt(senseKeySplit[1]), Integer.parseInt(senseKeySplit[2]), Integer.parseInt(senseKeySplit[3]));
			Set<IMember> members = index.getMembers(wnkey);
			if(members.size() > 0)
				System.out.println(((IMember)members.toArray()[0]).getVerbClass().getID());
//			Iterator<IVerbClass> it = index.iterator();
//			while(it.hasNext())
			{
//				IVerbClass verb = it.next();
//				System.out.println(verb.getID());
//				count++;
//				IVerbClass verb2 = index.getRootVerb("amalgamate-22.2");
//				IMember member = verb.getMembers().get(0);
//				Set<IWordnetKey> keys = member.getWordnetTypes().keySet();
//				IFrame frame = verb.getFrames().get(0);
//				FrameType type = frame.getPrimaryType();
//				String example = frame.getExamples().get(0);
//				System.out.println(verb.getID());
//				System.out.println(keys);
//				System.out.println(type.getID());
//				System.out.println(example);
			}
			System.out.println(count);
			index.close();
		} catch (Exception e) {		
			e.printStackTrace();
		}
		

	}

}
