package simRank;


public class SimrankEvaluation {

	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();		 
		String path = "resources/wn30Relations/sampleNew";
		String simRankPath = "resources/wn30Relations/sampleSimRank.txt";
		String idToVertexMapPath = "resources/wn30Relations/sampleIdToVertexMap.txt";
		String simrankOutputPath = "resources/wn30Relations/simrankMatrixIteration";
		String[] paths = {path};
		
		MyGraph mygraph = new MyGraph(paths, false);
		
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
	    System.out.println("Loaded Graph : elapsedTime : "+elapsedTime+" millisecs");
	    
	    System.out.println("Writing Graph IdToVertexMap files ...");
	    mygraph.writeIdToVertexMap(idToVertexMapPath);
	    
	    System.out.println("Computing Simrank ...");
	    SimRank sr = new SimRank(mygraph, simrankOutputPath);
	    
//	    System.out.println("Writing Simrank files ...");
//	    sr.writeSimRank(simRankPath);
	}
}
