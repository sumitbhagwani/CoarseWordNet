package simRank;


public class SimrankEvaluation {

	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();		 
		String path = "resources/wn30Relations/sample";
		String simRankPath = "resources/wn30Relations/sampleSimRank.txt";
		String idToVertexMapPath = "resources/wn30Relations/sampleIdToVertexMap.txt";
		String[] paths = {path};
		
		MyGraph mygraph = new MyGraph(paths, false);
		
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
	    System.out.println(elapsedTime);
	    
	    SimRank sr = new SimRank(mygraph);
	    sr.writeSimRank(simRankPath, idToVertexMapPath);
	}
}
