package krsystem.ontology.wekaClustering;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;

import net.sf.extjwnl.data.POS;

import krsystem.StaticValues;

import weka.core.Attribute;
import weka.core.DistanceFunction;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.PerformanceStats;

public class SQLDistance implements DistanceFunction{
	
	Connection connection = null;
	
	public SQLDistance()
	{
		try {
		    System.out.println("Loading driver...");
		    Class.forName("com.mysql.jdbc.Driver");
		    System.out.println("Driver loaded!");
		} catch (ClassNotFoundException e) {
		    throw new RuntimeException("Cannot find the driver in the classpath!", e);
		}
		
		String url = "jdbc:mysql://localhost:3306/synsetSimilarity";
		String username = StaticValues.sqlUsername;
		String password = StaticValues.sqlPassword;		
		try {
		    System.out.println("Connecting database...");
		    connection = DriverManager.getConnection(url, username, password);
		    System.out.println("Database connected!");
		}
		catch (SQLException e) {
			e.printStackTrace();
		    throw new RuntimeException("Cannot connect the database!", e);
		} catch(Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}
	}
	
	@Override
	public String[] getOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration listOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOptions(String[] arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double distance(Instance arg0, Instance arg1) {
		Attribute offset = arg0.dataset().attribute("offset");
		Attribute pos = arg0.dataset().attribute("pos");
		
		String offsetString0 = arg0.stringValue(offset);
		long offset0 = Long.parseLong(offsetString0);
		String offsetString1 = arg1.stringValue(offset);
		long offset1 = Long.parseLong(offsetString1);
				
		String pos0  = arg0.stringValue(pos);
		String pos1  = arg1.stringValue(pos);
		if(!pos0.equalsIgnoreCase(pos1))
		{
			System.out.println("Error : POS Mismatch while finding distance");
		}
					
		try{
		// Get a statement from the connection
	      Statement stmt = connection.createStatement() ;
	      // Execute the query
	      String smaller, larger;
	      if(offsetString0.compareToIgnoreCase(offsetString1) < 0)
	      {
	    	  smaller = offsetString0;
	    	  larger  = offsetString1;
	      }
	      else
	      {
	    	  smaller = offsetString1;
	    	  larger  = offsetString0;
	      }
	      ResultSet rs = stmt.executeQuery( "SELECT * FROM synsetSimilarityNoun WHERE smallerSynsetOffset='"+smaller+"' AND largerSynsetOffset='"+larger+"'" ) ;
	      if(rs != null)
	      {
		      // Loop through the result set
		      while( rs.next() )
		      {	 
		    	 double score = Double.parseDouble(rs.getString(3));
		         System.out.println("Score : "+ rs.getString(1) + " "+ rs.getString(2) + " "+score) ;
		         return score;
		      }
	      }
		}
		catch (SQLException e) {
			e.printStackTrace();
		    throw new RuntimeException("Cannot connect the database!", e);
		}
		return 0;
	}

	@Override
	public double distance(Instance arg0, Instance arg1, PerformanceStats arg2)
			throws Exception {
		return distance(arg0, arg1);
	}

	@Override
	public double distance(Instance arg0, Instance arg1, double arg2) {
		return distance(arg0, arg1);
	}

	@Override
	public double distance(Instance arg0, Instance arg1, double arg2,
			PerformanceStats arg3) {
		return distance(arg0, arg1);
	}

	@Override
	public String getAttributeIndices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Instances getInstances() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getInvertSelection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void postProcessDistances(double[] arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAttributeIndices(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInstances(Instances arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInvertSelection(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Instance arg0) {
		// TODO Auto-generated method stub
		
	}

	public static POS getPOSFromString(String posString)
	{
		if(posString.equalsIgnoreCase("n"))
			return POS.NOUN;
		else if(posString.equalsIgnoreCase("v"))
			return POS.VERB;
		else return null;
	}

}
