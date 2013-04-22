package simRank;

public class Vertex
{
	String pos;
	String offset;		
	
	public Vertex(String posPassed, String offsetPassed)
	{
		pos = posPassed;
		offset = offsetPassed;
	}
	
	public String toString()
	{
		return pos+"#"+offset;
	}	
	
	@Override
	public int hashCode()
	{
		return this.toString().hashCode();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		Vertex v = (Vertex)obj;
		if(v.pos.equalsIgnoreCase(pos) && v.offset.equalsIgnoreCase(offset))
			return true;
		return false;
	}
	
	public String getOffset()
	{
		return offset;
	}
	
	public String getPOS()
	{
		return pos;
	}			
}
