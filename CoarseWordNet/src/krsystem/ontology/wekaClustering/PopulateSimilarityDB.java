package krsystem.ontology.wekaClustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import jnisvmlight.FeatureVector;
import jnisvmlight.SVMLightInterface;
import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

import krsystem.StaticValues;
import krsystem.ontology.senseClustering.svm.FeatureGenerator;
import krsystem.ontology.senseClustering.svm.MinMaxSVMModel;

public class PopulateSimilarityDB {

	public static void populateNouns()
	{
		String svmFolder = "resources/Clustering/svmBinaries/";
		String posString = "Noun";
		String pathForSVMModel = svmFolder+"modelLinearEqualTrainingMinMaxNormalization";
		String PathForMinMax   = svmFolder+"paramsLinearEqualTrainingMinMaxNormalization";
		
		String propsFile30 = StaticValues.propsFile30;
		String dir = StaticValues.dataPath;
		String arg = "WordNet-3.0";		
		String domainDataPath = dir+"xwnd/joinedPOSSeparated/joinedNoun.txt";
		String OEDMappingPath = dir+"navigli_sense_inventory/mergeData-30.offsets.noun";
		String sentimentFilePath = dir+"/SentiWordNet/SentiWordNet.n";		
		String wordNet30OffsetFile = dir+"/xwnd/offsets.txt";
		
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
			FeatureGenerator fg = new FeatureGenerator(dir, arg, domainDataPath, dictionary, OEDMappingPath, sentimentFilePath, POS.NOUN);
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
								    String query = "INSERT INTO synsetSimilarityNoun VALUES ('"+synOffset+"', '"+synsetOffset+"', "+prediction+")";
								    PreparedStatement ps = connection.prepareStatement(query);
								    ps.executeUpdate();
									System.out.println(i);
									i++;				
								}
							}
						}
					}
				}
				if(i>10)
					break;
			}
			br.close();	    		    		    		    
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
		String pathForSVMModel = svmFolder+"modelLinearEqualTrainingMinMaxNormalization";
		String PathForMinMax   = svmFolder+"paramsLinearEqualTrainingMinMaxNormalization";
		
		String propsFile30 = StaticValues.propsFile30;
		String dir = StaticValues.dataPath;
		String arg = "WordNet-3.0";		
		String domainDataPath = dir+"xwnd/joinedPOSSeparated/joinedNoun.txt";
		String OEDMappingPath = dir+"navigli_sense_inventory/mergeData-30.offsets.noun";
		String sentimentFilePath = dir+"/SentiWordNet/SentiWordNet.n";		
		String wordNet30OffsetFile = dir+"/xwnd/offsets.txt";
		
		String line;
		int i = 0;
		
		try{
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();
			MinMaxSVMModel svmModel = MinMaxSVMModel.readModel(pathForSVMModel, PathForMinMax);		
			SVMLightInterface trainer = new SVMLightInterface();
			FeatureGenerator fg = new FeatureGenerator(dir, arg, domainDataPath, dictionary, OEDMappingPath, sentimentFilePath, POS.NOUN);
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
	
	public static void main(String[] args) {
//		long startTime = System.currentTimeMillis();
////		test1();	
//		populateNouns();
//		long endTime = System.currentTimeMillis();
//		System.out.println("Took "+(endTime - startTime) + " ms");
		test();
	}

}
