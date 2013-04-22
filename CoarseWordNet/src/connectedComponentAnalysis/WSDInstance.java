package connectedComponentAnalysis;

import java.util.HashSet;
import java.util.Set;

public class WSDInstance {

	String documentId;
	String instanceId;
	String lemma;
	Set<String> answersFine;
	Set<String> answersCoarse;
	double randomBaselineScore;
	
	public WSDInstance(String docId, String instID, String lemmaPassed, Set<String> fineAnswers, Set<String> coarseAnswers, double randomScore) {
		documentId = docId;
		instanceId = instID;
		lemma = lemmaPassed;
		answersFine = fineAnswers;
		answersCoarse = coarseAnswers;
		randomBaselineScore = randomScore;
	}
	
	public WSDInstance(String docId, String instID, String lemmaPassed) {
		documentId = docId;
		instanceId = instID;
		lemma = lemmaPassed;
		answersFine = new HashSet<String>();
		answersCoarse = new HashSet<String>();
		randomBaselineScore = 0.0;
	}
	
	public void addFineAnswer(String answer)
	{
		answersFine.add(answer);
	}
	
	public void addCoarseAnswer(String answer)
	{
		answersCoarse.add(answer);
	}
	
	public boolean fineCorrect(String answer)
	{
		return answersFine.contains(answer);
	}
	
	public boolean coarseCorrect(String answer)
	{
		return answersCoarse.contains(answer);
	}
	
	public double getBaselineScore()
	{
		return randomBaselineScore;
	}
	
	public void setBaselineScore(double score)
	{
		randomBaselineScore = score;
	}
	
}
