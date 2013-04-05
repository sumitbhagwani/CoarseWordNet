package svmPredictionNormalization;

public class PlattProb {

	double A, B;
	
	private static double maxIter = 100;
	private static double minstep = Math.pow(10, -10);
	private static double sigma = Math.pow(10, -12);
	private static double stoppingCriteria = Math.pow(10, -5);
	
	public PlattProb(double[] deci, double[] label, int prior1, int prior0)
	{
		int len = prior1 + prior0;
		System.out.println("Learning Transformation Model on "+len+" instances..");
		double hiTarget = (prior1+1.0)/(prior1+2.0);
		double loTarget = 1.0/(prior0+2.0);
				
		double[] t = new double[deci.length];
		
		for(int i=0; i<len; i++)
		{
			t[i] = label[i]>0 ? hiTarget : loTarget;
		}
		
		A = 0.0;
		B = Math.log((prior0+1.0)/(prior1+1.0));
		double fVal = 0.0;
		
		for(int i=0; i<len; i++)
		{
			double fApB = deci[i]*A + B;
			if(fApB >= 0)
				fVal += t[i]*fApB + Math.log(1+Math.exp(-fApB));
			else
				fVal += (t[i]-1)*fApB + Math.log(1+Math.exp(fApB));
		}
				
		int it; 
		for(it = 0; it<maxIter; it++)
		{
			//Update Gradient and Hessian (use H' = H + sigma I)
			double h11 = sigma;
			double h22 = sigma;
			double h21=0.0, g1=0.0, g2=0.0, p=0.0, q=0.0, d1=0.0, d2=0.0;
			
			System.out.println("Iteration "+it+" ---- A : "+A+" ---- B : "+B);
			for(int i=0; i<len; i++)
			{
				double fApB = deci[i]*A + B;
				if(fApB >= 0)
				{
					p = Math.exp(-fApB) / (1+Math.exp(-fApB));
					q = 1.0 / (1+Math.exp(-fApB));
				}
				else
				{
					p = 1.0 / (1+Math.exp(fApB));
					q = Math.exp(fApB) / (1+Math.exp(fApB));
				}
				d2 = p*q;
				h11 += deci[i]*deci[i]*d2;
				h22+= d2;
				h21 += deci[i]*d2;
				d1 = t[i]-p;
				g1 += deci[i]*d1;
				g2 += d1;					
			}
			if(Math.abs(g1) < stoppingCriteria && Math.abs(g2) < stoppingCriteria)
				break;		
		
			// Compute modified Newton directions
			double det = h11*h22 - h21*h21;
			double dA = -(h22*g1 - h21*g2)/det;
			double dB = -(h21*g1 + h11*g2)/det;
			double gd = g1*dA + g2*dB;
			double stepsize = 1;
			while(stepsize >= minstep) // Line Search
			{
				double newA = A + stepsize*dA;
				double newB = B + stepsize*dB;
				
				//New Function value
				double newf = 0.0;
				for(int i=0; i<len; i++)
				{
					double fApB = deci[i]*newA + newB;
					if(fApB >= 0)
						newf += t[i]*fApB + Math.log(1+Math.exp(-fApB));
					else
						newf += (t[i]-1)*fApB + Math.log(1+Math.exp(fApB));
				}
				
				//check sufficient decrease
				if(newf < fVal + 0.0001*stepsize*gd){
					A = newA;
					B = newB;
					fVal = newf;
					break; // Sufficient decrease satisfied
				}
				else
					stepsize /= 2.0;
			}
			if(stepsize < minstep)
			{
				System.out.println("Line Search Fails! with stepsize : "+stepsize+" minstep : "+minstep);
				break;
			}
		}
		if(it >= maxIter)
			System.out.println("Reaching maximum iterations");		
	}
	
	public double reportPosteriorProb(double deci)
	{
		// both expressions are same -- caution in writing because of overflow errors
		double fApB = deci*A + B; 
		if(fApB >= 0)
			return Math.exp(-fApB)/(1.0 + Math.exp(-fApB));
		else
			return 1.0/(1.0 + Math.exp(fApB));			
	}		

}
