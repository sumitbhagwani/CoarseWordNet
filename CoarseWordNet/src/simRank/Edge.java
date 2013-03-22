package simRank;

public class Edge
{
	double weight;
	String label;
	
	public Edge(String labelPassed, double weightPassed)
	{
		label = labelPassed;
		weight = weightPassed;
	}
	
	public String toString()
	{
		return "E#"+label+"#"+weight;
	}
	
	@Override
	public int hashCode()
	{
		return this.toString().hashCode();
	}
	
}
