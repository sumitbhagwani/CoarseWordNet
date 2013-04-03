package krsystem.ontology.senseClustering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;

public class PrepareBinaryClassificationData {

	  public static void shuffleArray(String[] a) 
	  {
		    int n = a.length;
		    Random random = new Random();
		    random.nextInt();
		    for (int i = 0; i < n; i++) {
		      int change = i + random.nextInt(n - i);
		      swap(a, i, change);
		    }
	  }

	  private static void swap(String[] a, int i, int change) 
	  {
		  String helper = a[i];
		  a[i] = a[change];
		  a[change] = helper;
	  }
	
	public static void prepareData(String inputFolder, String outputFilePositive, String outputFileNegative,  Dictionary dict)
	{
		System.out.println("Preparing dataset "+inputFolder);
		HashSet<String> distinctOffsets = new HashSet<String>();
		int positiveExamplesCount = 0;
		int negativeExamplesCount = 0;
		File filesFolder = new File(inputFolder);
		File[] listOfFiles = filesFolder.listFiles();
		System.out.println("Number of Word Sense Files : " + listOfFiles.length);
		try{			
			BufferedWriter bwp = new BufferedWriter(new FileWriter(new File(outputFilePositive)));
			BufferedWriter bwn = new BufferedWriter(new FileWriter(new File(outputFileNegative)));
			for(File file: listOfFiles)
			{
				String absolutePath = file.getAbsolutePath();
				String filePath = absolutePath.substring(0,absolutePath.lastIndexOf("/"));
				String fileName = absolutePath.substring(absolutePath.lastIndexOf("/")+1, absolutePath.length());
				String word = fileName.substring(0,fileName.lastIndexOf("-"));
				String pos = fileName.substring(fileName.lastIndexOf("-")+1,fileName.lastIndexOf("."));
				POS posReqd = pos.equalsIgnoreCase("n") ? POS.NOUN : POS.VERB ;
				
				//getting all synsets of the word-pos pair
				IndexWord iw = dict.getIndexWord(posReqd, word);
				List<Synset> synsets = iw.getSenses();
				HashSet<String> allSenseOffsets = new HashSet<String>();
				for(Synset sense : synsets)
				{
					long offsetLong = sense.getOffset();						
					String offset = String.format("%08d", offsetLong);
					allSenseOffsets.add(offset);
					distinctOffsets.add(offset);
				}
				
				//reading all the clusters
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;						
				while((line = br.readLine()) != null)
				{					
					HashSet<String> cluster = new HashSet<String>();
					for(String offset : line.split("\\s+"))
					{
						if(offset.trim().length()>0)
							cluster.add(offset);
					}
					for(String offset : cluster)
					{
						for(String otherOffset : allSenseOffsets)
							if(offset.compareTo(otherOffset) > 0)
							{
								
								if(cluster.contains(otherOffset))
								{
									bwp.write(pos+"#"+offset+" "+pos+"#"+otherOffset+" 1\n");
//									bw.write("1\n");
									positiveExamplesCount++;
								}
								else
								{
									bwn.write(pos+"#"+offset+" "+pos+"#"+otherOffset+" -1\n");
//									bw.write("-1\n");
									negativeExamplesCount++;
								}
							}
					}
				}
				br.close();											
			}
			bwp.close();
			bwn.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
		long size = distinctOffsets.size();
		int totalExamples = positiveExamplesCount + negativeExamplesCount;
		System.out.println("Distinct Offsets encountered : "+ size);
		System.out.println("DOChoose2 : " + size*(size-1)/2);
		System.out.println("Positive Examples : " + positiveExamplesCount);
		System.out.println("Negative Examples : " + negativeExamplesCount);
		System.out.println("Percentage of Positive examples : "+(positiveExamplesCount*100.0/totalExamples));
		System.out.println("----------------------------------------------------------");
	}
	
	public static void divideDataset(String posExamplesFile, String negExamplesFile, double ratio, String outputDirPath)
	{
		System.out.println("Dividing Dataset");
		try
		{
			Scanner scp = new Scanner(new File(posExamplesFile));
			String textPos = scp.useDelimiter("\\Z").next().trim();		
			scp.close();
			
			Scanner scn = new Scanner(new File(negExamplesFile));
			String textNeg = scn.useDelimiter("\\Z").next().trim();		
			scp.close();
			
			String[] posExamples = textPos.split("[\\r\\n]+");
			shuffleArray(posExamples);
			int numPosExample = posExamples.length;
			int numPosExampleFinal = (int) Math.ceil(numPosExample*ratio);
			int numPosExampleTest = numPosExample - numPosExampleFinal;
			System.out.println("numPosExample : "+ numPosExample);
			System.out.println("numPosExampleFinal : "+ numPosExampleFinal);
			System.out.println("numPosExampleTest : "+numPosExampleTest);
			
			String[] negExamples = textNeg.split("[\\r\\n]+");
			shuffleArray(negExamples);
			int numNegExample = negExamples.length;
			int numNegExampleFinal = (int) Math.ceil(numNegExample*ratio);
			System.out.println("numNegExample : "+ numNegExample);
			System.out.println("numNegExampleFinal : "+ numNegExampleFinal);
			
			File outputDir = new File(outputDirPath);
			outputDir.mkdirs();
			
			File posTrain = new File(outputDirPath+"PosTrain"+ratio+".txt");
			BufferedWriter bwPosTrain = new BufferedWriter(new FileWriter(posTrain));
			File posTest  = new File(outputDirPath+"PosTest"+ratio+".txt");
			BufferedWriter bwPosTest = new BufferedWriter(new FileWriter(posTest));
			
			File NegTrain = new File(outputDirPath+"NegTrain"+ratio+".txt");
			BufferedWriter bwNegTrain = new BufferedWriter(new FileWriter(NegTrain));
			File NegTrainEqual = new File(outputDirPath+"NegTrainEqual.txt");
			BufferedWriter bwNegTrainEqual = new BufferedWriter(new FileWriter(NegTrainEqual));
			File NegTest  = new File(outputDirPath+"NegTest"+ratio+".txt");
			BufferedWriter bwNegTest= new BufferedWriter(new FileWriter(NegTest));
			File NegTestEqual  = new File(outputDirPath+"NegTestEqual.txt");
			BufferedWriter bwNegTestEqual = new BufferedWriter(new FileWriter(NegTestEqual));
			
			for(int i=0; i<numPosExample;i++)
			{
				if(i<numPosExampleFinal)
					bwPosTrain.write(posExamples[i]+"\n");
				else
					bwPosTest.write(posExamples[i]+"\n");					
			}
			
			for(int i=0; i<numNegExample;i++)
			{
				if(i<numPosExampleFinal) // to generate equal number of training and testing examples
					bwNegTrainEqual.write(negExamples[i]+"\n");
				
				if(i<numNegExampleFinal)
					bwNegTrain.write(negExamples[i]+"\n");
				else
				{
					if(i<numNegExampleFinal+numPosExampleTest)
						bwNegTestEqual.write(negExamples[i]+"\n");
					bwNegTest.write(negExamples[i]+"\n");
				}
			}
			
			bwPosTrain.close();
			bwPosTest.close();
			bwNegTrain.close();
			bwNegTrainEqual.close();
			bwNegTest.close();
			bwNegTestEqual.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		System.out.println("----------------------------------------------------------");
	}
	
	public static void main(String[] args) {
		String inputPath = "resources/Clustering/OntonotesFinalCleaned/";
		String outputPath = "resources/Clustering/BinaryClassificationData/";
		String nounInputFolder = inputPath+"Noun/";
		String verbInputFolder = inputPath+"Verb/";
		String nounOutputFilePositive = outputPath+"nounPositiveCleaned";
		String nounOutputFileNegative = outputPath+"nounNegativeCleaned";
		String verbOutputFilePositive = outputPath+"verbPositiveCleaned";
		String verbOutputFileNegative = outputPath+"verbNegativeCleaned";				
		
		String propsFile30 = "resources/file_properties.xml";
		try{
			JWNL.initialize(new FileInputStream(propsFile30));
			Dictionary dictionary = Dictionary.getInstance();
			if(dictionary!=null)
			{
//				prepareData(nounInputFolder, nounOutputFilePositive, nounOutputFileNegative, dictionary);
//				prepareData(verbInputFolder, verbOutputFilePositive, verbOutputFileNegative, dictionary);
//				divideDataset(nounOutputFilePositive, nounOutputFileNegative, 0.7, outputPath+"Noun6/");
				divideDataset(verbOutputFilePositive, verbOutputFileNegative, 0.7, outputPath+"Verb3/");
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
	}

}
