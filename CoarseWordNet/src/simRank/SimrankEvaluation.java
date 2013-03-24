package simRank;


public class SimrankEvaluation {

	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();		 
		String pathHypernym = "resources/wn30Relations/hypernymNew";
		String pathHyponym = "resources/wn30Relations/hyponymNew";
		String pathMeronym = "resources/wn30Relations/meronymNew";
		String pathHolonym = "resources/wn30Relations/holonymNew";
		String simRankPath = "resources/wn30Relations/undirectedSimRank.txt";
		String idToVertexMapPath = "resources/wn30Relations/undirectedIdToVertexMap.txt";
		String simrankOutputPath = "resources/wn30Relations/simrankMatrixIteration";
		String[] paths = {pathHypernym, pathMeronym};
		
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
