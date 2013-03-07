package krsystem.utility;

public class Statistics {

	public static double Mean(double[] values) {
		double sum = 0;
		for (double value : values)
			sum += value;
		return sum/values.length;
	}
	
	public static double StandardDeviation(double[] values) {
		double[] newValues = new double[values.length];
		double mean = Mean(values);
		for (int i=0; i<values.length; i++)
			newValues[i] = Math.pow(values[i]-mean, 2);
		return Math.sqrt(Mean(newValues));
	}
	
	public static double Kurtosis(double[] values) {
		double[] newValues = new double[values.length];
		double mean = Mean(values);
		for (int i=0; i<values.length; i++)
			newValues[i] = Math.pow(values[i]-mean, 4);
		double standardDeviation = StandardDeviation(values);
		if (standardDeviation == 0)
			return Double.POSITIVE_INFINITY;
		else
			return Mean(newValues)/Math.pow(standardDeviation, 4) - 3;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
