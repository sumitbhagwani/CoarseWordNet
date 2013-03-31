package ezgraph.simrank.parallel;

import it.unimi.dsi.webgraph.labelling.ArcLabelledNodeIterator;
import ezgraph.Graph;
import ezgraph.NodeIterator;
import ezgraph.SparseMatrix;

public class UpdateSimRankTask implements Runnable {
	SparseMatrix simrank;
	SparseMatrix simrank2;
	Graph graph;
	int currentVertex1;
	int currentVertex2;
	NodeIterator it1;
	NodeIterator it2;
	double DEFAULT_C = 0.6;

	  UpdateSimRankTask(SparseMatrix simrank, SparseMatrix simrank2, Graph graph, int currentVertex1, int currentVertex2, NodeIterator it1, NodeIterator it2) {
		  this.simrank = simrank;
		  this.simrank2 = simrank2;
		  this.graph = graph;
		  this.currentVertex1 = currentVertex1;
		  this.currentVertex2 = currentVertex2;
		  this.it1 = it1;
		  this.it2 = it2;
	  }

	  @Override
	  public void run() {
		  double quantity = 0.0;
			Integer aux1 = null , aux2 = null;
			ArcLabelledNodeIterator.LabelledArcIterator anc1 = it1.ancestors();
			double sum1 = 0.0;
			while ( (aux1 = anc1.nextInt()) != null && aux1 >= 0 && aux1 < ( graph.numNodes() ) ) sum1 += anc1.label().getFloat();
			anc1 = it1.ancestors();
			while ( (aux1 = anc1.nextInt()) != null && aux1 >= 0 && aux1 < ( graph.numNodes() ) ) {
				double weight1 = anc1.label().getFloat() / sum1;
				ArcLabelledNodeIterator.LabelledArcIterator anc2 = it2.ancestors();
				double sum2 = 0.0;
				while ( (aux2 = anc2.nextInt()) != null && aux2 >= 0 && aux2 < ( graph.numNodes() ) ) sum2 += anc2.label().getFloat();
				anc2 = it2.ancestors();
				while ( (aux2 = anc2.nextInt()) != null && aux2 >= 0 && aux2 < ( graph.numNodes() ) ) {
					double weight2 = anc2.label().getFloat() / sum2;
					double simrankAux12 = 0.0;
					if(aux1 == aux2)
						simrankAux12 = 1.0;
					else if(aux1 < aux2)
						simrankAux12 = simrank.get(aux1,aux2);
					else
						simrankAux12 = simrank.get(aux2,aux1);
					quantity += weight1 * weight2 * simrankAux12;					
				}
			}
			if ( quantity != 0.0 ) {				
				simrank2.set(currentVertex1,currentVertex2, quantity * DEFAULT_C );				
			}
	  }
	} 