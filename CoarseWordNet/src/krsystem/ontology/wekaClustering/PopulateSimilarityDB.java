package krsystem.ontology.wekaClustering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import jnisvmlight.FeatureVector;
import jnisvmlight.LabeledFeatureVector;
import jnisvmlight.SVMLightInterface;
import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

import krsystem.StaticValues;
import krsystem.ontology.senseClustering.svm.FeatureGenerator;
import krsystem.ontology.senseClustering.svm.Instance;
import krsystem.ontology.senseClustering.svm.MinMaxSVMModel;
import krsystem.utility.OrderedPair;

public class PopulateSimilarityDB {

	public static void populateNouns()
	{
		int errorCount = 0;
		int errorCount2 = 0;
		int lessCount = 0;
		int equalCount = 0;		
		int totalCount = 0;		
		String svmFolder = "resources/Clustering/svmBinaries/";
		String posString = "Noun";
		String pathForSVMModel = svmFolder+"modelLinearEqualTrainingMinMaxNormalizationNoun";
		String PathForMinMax   = svmFolder+"paramsLinearEqualTrainingMinMaxNormalizationNoun";
		
		String propsFile30 = StaticValues.propsFile30;
		String dir = StaticValues.dataPath;
		String arg = "WordNet-3.0";		
		String domainDataPath = dir+"xwnd/joinedPOSSeparated/joinedNoun.txt";
		String OEDMappingPath = dir+"navigli_sense_inventory/mergeData-30.offsets.noun";
		String sentimentFilePath = dir+"/SentiWordNet/SentiWordNet.n";		
		String wordNet30OffsetFile = dir+"/xwnd/offsets.txt";
		String synsetToWordIndexPairMap = "resources/Clustering/synsetWordIndexMap/nounMap.txt";
		
		String line;
		int i = 0;
		
		try {
		    System.out.println("Loading driver...");
		    Class.forName("com.mysql.jdbc.Driver");
		    System.out.println("Driver loaded!");
		} catch (ClassNotFoundException e) {
		    throw new RuntimeException("Cannot find the driver in the classpath!", e);
		}
		
		String ip = StaticValues.cseLabIP;
		String url = "jdbc:mysql://"+ip+":3306/synsetSimilarity";
		String username = StaticValues.sqlUsername;
		String password = StaticValues.sqlPassword;
		Connection connection = null;
		try {
		    System.out.println("Connecting database...");
		    connection = DriverManager.getConnection(url, username, password);
		    System.out.println("Database connected!");
		    
		    JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();
			MinMaxSVMModel svmModel = MinMaxSVMModel.readModel(pathForSVMModel, PathForMinMax);		
			SVMLightInterface trainer = new SVMLightInterface();
			FeatureGenerator fg = new FeatureGenerator(dir, arg, domainDataPath, dictionary, OEDMappingPath, sentimentFilePath, POS.NOUN, synsetToWordIndexPairMap);
			BufferedReader br = new BufferedReader(new FileReader(new File(wordNet30OffsetFile)));
			while((line=br.readLine())!=null)
			{				
				String[] lineSplit = line.split("-");
				if(lineSplit[1].equalsIgnoreCase("n"))
				{
//					System.out.println("Noun : "+line);
					long offset = Long.parseLong(lineSplit[0]);
					Synset synset = dictionary.getSynsetAt(POS.NOUN, offset);
					if(synset != null)
					{						
//						System.out.println("NounSynset : "+synset);
						String synsetOffset = String.format("%08d", synset.getOffset());
						List<Word> words = synset.getWords();
						if(words==null  || words.size()==0)
							errorCount++;
						for(Word word : words)
						{
							String lemma = word.getLemma();
							IndexWord iw = dictionary.getIndexWord(POS.NOUN, lemma);
							for(Synset syn : iw.getSenses())
							{
								totalCount++;
								String synOffset = String.format("%08d", syn.getOffset());
								if(synOffset.compareTo(synsetOffset) < 0)
								{
									lessCount++;
									FeatureVector fv = fg.getFeatureVector(syn, synset);
									double prediction = svmModel.classify(fv);
//									System.out.println(synOffset + " "+synsetOffset+" "+prediction);
								    String query = "INSERT INTO synsetSimilarityNoun VALUES ('"+synOffset+"', '"+synsetOffset+"', "+prediction+")";
								    PreparedStatement ps = connection.prepareStatement(query);
								    ps.executeUpdate();									
								}
								else if(synOffset.compareTo(synsetOffset) == 0)
									equalCount++;
							}
						}
					}
					else
					{
						errorCount2++;
					}
				}
				if(i%1000 == 0)
					System.out.println("POPULATING NOUNS: "+i+" ErrorCount : "+errorCount+" ErrorCount2 : "+errorCount2+" LessCount : "+lessCount+" EqualCount : "+equalCount+" TotalCount : "+totalCount);
				i++;				
//				if(i>10)
//					break;
			}
			br.close();	    
			System.out.println("ErrorCount : "+errorCount);
			System.out.println("LessCount : "+lessCount);
			System.out.println("EqualCount : "+equalCount);
			System.out.println("TotalCount : "+totalCount);
		}
		catch (SQLException e) {
			e.printStackTrace();
		    throw new RuntimeException("Cannot connect the database!", e);
		} catch(Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
		finally {
		    System.out.println("Closing the connection.");
		    if (connection != null) try { connection.close(); } catch (SQLException ignore) {}
		}
	}
	
	public static void test() {	
		try {
		    System.out.println("Loading driver...");
		    Class.forName("com.mysql.jdbc.Driver");
		    System.out.println("Driver loaded!");
		} catch (ClassNotFoundException e) {
		    throw new RuntimeException("Cannot find the driver in the classpath!", e);
		}
		
		String ip = StaticValues.cseLabIP;
		String url = "jdbc:mysql://"+ip+":3306/synsetSimilarity";
		String username = StaticValues.sqlUsername;
		String password = StaticValues.sqlPassword;
		Connection connection = null;
		try {
		    System.out.println("Connecting database...");
		    connection = DriverManager.getConnection(url, username, password);
		    System.out.println("Database connected!");
		    		    		    		    
		    String query = "INSERT INTO synsetSimilarityNoun VALUES ('00000000', '11111111', 2.0)";
		    PreparedStatement ps = connection.prepareStatement(query);
		    ps.executeUpdate();
		    
	      // Get a statement from the connection
	      Statement stmt = connection.createStatement() ;
	      // Execute the query
	      String smaller = "00000000";
	      String larger  = "11111111";
	      ResultSet rs = stmt.executeQuery( "SELECT * FROM synsetSimilarityNoun WHERE smallerSynsetOffset='"+smaller+"' AND largerSynsetOffset='"+larger+"'" ) ;

	      // Loop through the result set
	      while( rs.next() )
	      {
	    	  
	         System.out.println( rs.getString(1) + " "+ rs.getString(2) + " "+Double.parseDouble(rs.getString(3))) ;
	      }
		    
		    
		} catch (SQLException e) {
			e.printStackTrace();
		    throw new RuntimeException("Cannot connect the database!", e);
		} finally {
		    System.out.println("Closing the connection.");
		    if (connection != null) try { connection.close(); } catch (SQLException ignore) {}
		}
	}
	
	public static void test1()
	{
		String svmFolder = "resources/Clustering/svmBinaries/";
		String posString = "Noun";
		String pathForSVMModel = svmFolder+"modelLinearEqualTrainingMinMaxNormalizationNoun";
		String PathForMinMax   = svmFolder+"paramsLinearEqualTrainingMinMaxNormalizationNoun";
		
		String propsFile30 = StaticValues.propsFile30;
		String dir = StaticValues.dataPath;
		String arg = "WordNet-3.0";		
		String domainDataPath = dir+"xwnd/joinedPOSSeparated/joinedNoun.txt";
		String OEDMappingPath = dir+"navigli_sense_inventory/mergeData-30.offsets.noun";
		String sentimentFilePath = dir+"/SentiWordNet/SentiWordNet.n";		
		String wordNet30OffsetFile = dir+"/xwnd/offsets.txt";
		String synsetToWordIndexPairMap = "resources/Clustering/synsetWordIndexMap/nounMap.txt";
		String line;
		int i = 0;
		
		try{
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();
			MinMaxSVMModel svmModel = MinMaxSVMModel.readModel(pathForSVMModel, PathForMinMax);		
			SVMLightInterface trainer = new SVMLightInterface();
			FeatureGenerator fg = new FeatureGenerator(dir, arg, domainDataPath, dictionary, OEDMappingPath, sentimentFilePath, POS.NOUN, synsetToWordIndexPairMap);
			BufferedReader br = new BufferedReader(new FileReader(new File(wordNet30OffsetFile)));
			while((line=br.readLine())!=null)
			{				
				String[] lineSplit = line.split("-");
				if(lineSplit[1].equalsIgnoreCase("n"))
				{
					System.out.println("Noun : "+line);
					long offset = Long.parseLong(lineSplit[0]);
					Synset synset = dictionary.getSynsetAt(POS.NOUN, offset);
					if(synset != null)
					{						
						System.out.println("NounSynset : "+synset);
						String synsetOffset = String.format("%08d", synset.getOffset());
						for(Word word :synset.getWords())
						{
							String lemma = word.getLemma();
							IndexWord iw = dictionary.getIndexWord(POS.NOUN, lemma);
							for(Synset syn : iw.getSenses())
							{
								String synOffset = String.format("%08d", syn.getOffset());
								if(synOffset.compareTo(synsetOffset) < 0)
								{
									FeatureVector fv = fg.getFeatureVector(syn, synset);
									double prediction = svmModel.classify(fv);
									System.out.println(synOffset + " "+synsetOffset+" "+prediction);
								}
							}
						}
					}
				}
				System.out.println(i);
				i++;				
				if(i>10)
					break;
			}
			br.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void test2()
	{
		String svmFolder = "resources/Clustering/svmBinaries/";
		String posString = "Noun";
		String pathForSVMModel = svmFolder+"modelLinearEqualTrainingMinMaxNormalizationNoun";
		String PathForMinMax   = svmFolder+"paramsLinearEqualTrainingMinMaxNormalizationNoun";
		
		String propsFile30 = StaticValues.propsFile30;
		String dir = StaticValues.dataPath;
		String arg = "WordNet-3.0";		
		String domainDataPath = dir+"xwnd/joinedPOSSeparated/joinedNoun.txt";
		String OEDMappingPath = dir+"navigli_sense_inventory/mergeData-30.offsets.noun";
		String sentimentFilePath = dir+"/SentiWordNet/SentiWordNet.n";		
		String wordNet30OffsetFile = dir+"/xwnd/offsets.txt";
		String synsetToWordIndexPairMap = "resources/Clustering/synsetWordIndexMap/nounMap.txt";
		
		try{
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();
			MinMaxSVMModel svmModel = MinMaxSVMModel.readModel(pathForSVMModel, PathForMinMax);		
			SVMLightInterface trainer = new SVMLightInterface();
			FeatureGenerator fg = new FeatureGenerator(dir, arg, domainDataPath, dictionary, OEDMappingPath, sentimentFilePath, POS.NOUN, synsetToWordIndexPairMap);
			
			
			long synOffset = 8500433;
			long synsetOffset = 28651;
			Synset syn = dictionary.getSynsetAt(POS.NOUN, synOffset);
			Synset synset = dictionary.getSynsetAt(POS.NOUN, synsetOffset);
			
			OrderedPair<Integer, LabeledFeatureVector> pair = fg.getLabeledFeatureVector(new Instance(syn, synset, 0));
			LabeledFeatureVector lfv = pair.getR();
			for(int i=0; i<pair.getL().intValue(); i++)
				System.out.print(lfv.getValueAt(i)+" ");
			System.out.println();
			double prediction = svmModel.classify((FeatureVector)lfv);
			System.out.println(synOffset + " "+synsetOffset+" "+prediction);			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void writeSimValuesNoun(String filePath)
	{
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filePath)));
			try {
			    System.out.println("Loading driver...");
			    Class.forName("com.mysql.jdbc.Driver");
			    System.out.println("Driver loaded!");
			} catch (ClassNotFoundException e) {
			    throw new RuntimeException("Cannot find the driver in the classpath!", e);
			}
			
			String ip = StaticValues.cseLabIP;
			String url = "jdbc:mysql://"+ip+":3306/synsetSimilarity";
			String username = StaticValues.sqlUsername;
			String password = StaticValues.sqlPassword;
			Connection connection = null;
			try {
			    System.out.println("Connecting database...");
			    connection = DriverManager.getConnection(url, username, password);
			    System.out.println("Database connected!");			    
			    
		      // Get a statement from the connection
		      Statement stmt = connection.createStatement() ;		      
		      ResultSet rs = stmt.executeQuery( "SELECT distinct * FROM synsetSimilarityNoun" ) ;

		      // Loop through the result set
		      while( rs.next() )
		      {		    	  
		         bw.write(rs.getString(1) + " "+ rs.getString(2) + " "+rs.getString(3)+"\n") ;
		      }			    
			    
			} catch (SQLException e) {
				e.printStackTrace();
			    throw new RuntimeException("Cannot connect the database!", e);
			} finally {
			    System.out.println("Closing the connection.");
			    if (connection != null) try { connection.close(); } catch (SQLException ignore) {}
			}

			bw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void sanityCheck1()
	{
		String propsFile30 = StaticValues.propsFile30;	
		try{
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();
			int numReqd = 0;
			int iter = 0;
			System.out.println("Iterating over noun synsets.."+dictionary);
			Iterator<Synset> it1 = dictionary.getSynsetIterator(POS.NOUN);
			while(it1.hasNext())
			{
				iter++;
				Synset syn1 = it1.next();
				String syn1Offset = String.format("%08d", syn1.getOffset());
				for(Word word : syn1.getWords())
				{
					String lemma = word.getLemma();
					IndexWord iw = dictionary.getIndexWord(POS.NOUN, lemma);
					for(Synset syn2 : iw.getSenses())
					{
						String syn2Offset = String.format("%08d", syn2.getOffset());
						if(syn1Offset.compareTo(syn2Offset) < 0)
							numReqd++;
					}
				}
				if(iter %1000 == 0)
					System.out.println(iter+" "+numReqd);
			}
			System.out.println(iter+" "+numReqd);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void sanityCheck2() 
	{
		String svmPredictionsFile = "resources/Clustering/PopulatingDB/simValuesSVM.noun";
		HashSet<String> offsetPairs = new HashSet<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(svmPredictionsFile)));
			String line;
			while((line=br.readLine()) != null)
			{
				String[] lineSplit = line.split("\\s+");
				offsetPairs.add(lineSplit[0]+"-"+lineSplit[1]);
			}
			br.close();
			
			int found = 0;
			int notFound = 0;
			String posCleaned = "resources/Clustering/BinaryClassificationData/nounPositiveCleaned";
			String negCleaned = "resources/Clustering/BinaryClassificationData/nounNegativeCleaned";
			br = new BufferedReader(new FileReader(new File(posCleaned)));			
			while((line=br.readLine()) != null)
			{
				String[] lineSplit = line.split("\\s+");
				String offset0 = lineSplit[0].split("#")[1];
				String offset1 = lineSplit[1].split("#")[1];
				String key = "";
				if(offset0.compareTo(offset1) < 0)
					key = offset0+"-"+offset1;
				else
					key = offset1+"-"+offset0;
				if(offsetPairs.contains(key))
					found ++;
				else
					notFound ++;
			}
			br.close();
			System.out.println(found+" "+notFound);
			
			found = notFound = 0;
			br = new BufferedReader(new FileReader(new File(negCleaned)));			
			while((line=br.readLine()) != null)
			{
				String[] lineSplit = line.split("\\s+");
				String offset0 = lineSplit[0].split("#")[1];
				String offset1 = lineSplit[1].split("#")[1];
				String key = "";
				if(offset0.compareTo(offset1) < 0)
					key = offset0+"-"+offset1;
				else
					key = offset1+"-"+offset0;
				if(offsetPairs.contains(key))
					found ++;
				else
					notFound ++;
			}
			br.close();
			System.out.println(found+" "+notFound);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
//		String filePath = "resources/Clustering/PopulatingDB/simValuesSVM.noun";
//		writeSimValuesNoun(filePath);
//		long startTime = System.currentTimeMillis();
////		test1();	
//		populateNouns();		
//		long endTime = System.currentTimeMillis();
//		System.out.println("Took "+(endTime - startTime) + " ms");
//		test();
//		test2();
		sanityCheck2();
	}

}
